package us.ajg0702.queue.spigot.placeholders.placeholders;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import us.ajg0702.queue.api.spigot.AjQueueSpigotAPI;
import us.ajg0702.queue.api.spigot.MessagedResponse;
import us.ajg0702.queue.spigot.SpigotMain;
import us.ajg0702.queue.spigot.placeholders.Placeholder;
import us.ajg0702.queue.spigot.placeholders.RefetchablePlaceholder;
import us.ajg0702.queue.spigot.utils.UUIDStringKey;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

public class QueuedStatus extends Placeholder implements RefetchablePlaceholder {
    public QueuedStatus(SpigotMain plugin) {
        super(plugin);
    }

    private final Map<UUID, String> cache = new ConcurrentHashMap<>();
    private final Map<UUID, Long> lastFetch = new ConcurrentHashMap<>();

    @Override
    public String getRegex() {
        return "queued_status";
    }

    @Override
    public String parse(Matcher matcher, OfflinePlayer p) {
        UUID key = p.getUniqueId();

        if(!p.isOnline()) return "You aren't online!?!";

        if(System.currentTimeMillis() - lastFetch.getOrDefault(key, 0L) > 3000) {
            refetch(p, key);
        }

        return cache.getOrDefault(key, "...");
    }

    @Override
    public void cleanCache(Player player) {
        cache.entrySet().removeIf(entry -> entry.getKey().equals(player.getUniqueId()));
        lastFetch.entrySet().removeIf(entry -> entry.getKey().equals(player.getUniqueId()));
    }

    @Override
    public void refetch(OfflinePlayer p) {
        List<UUID> keys = cache.keySet().stream()
                .filter(key -> key.equals(p.getUniqueId()))
                .collect(Collectors.toList());
        for (UUID uuid : keys) {
            refetch(p, uuid);
        }
    }

    public void refetch(OfflinePlayer p, UUID key) {
        lastFetch.put(key, System.currentTimeMillis());
        plugin.getScheduler().runTaskAsynchronously(() -> {
            if (!p.isOnline()) return;
            try {
                MessagedResponse<String> queueNameResponse = AjQueueSpigotAPI.getInstance()
                        .getRawQueueName(p.getUniqueId())
                        .get(30, TimeUnit.SECONDS);

                String queueName = queueNameResponse.getResponse();
                if(queueName == null) {
                    cache.put(p.getUniqueId(), queueNameResponse.getEither());
                    return;
                }

                String queueStatusResponse = AjQueueSpigotAPI.getInstance()
                        .getServerStatusString(queueName, p.getUniqueId())
                        .get(30, TimeUnit.SECONDS);

                cache.put(p.getUniqueId(), queueStatusResponse);

            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            } catch (TimeoutException | IllegalArgumentException ignored) {
            }
        });
    }
}
