package us.ajg0702.queue.spigot.placeholders.placeholders;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import us.ajg0702.queue.api.spigot.AjQueueSpigotAPI;
import us.ajg0702.queue.api.spigot.MessagedResponse;
import us.ajg0702.queue.spigot.SpigotMain;
import us.ajg0702.queue.spigot.placeholders.Placeholder;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.regex.Matcher;

public class Position extends Placeholder {
    public Position(SpigotMain plugin) {
        super(plugin);
    }

    private final Map<UUID, String> cache = new ConcurrentHashMap<>();

    @Override
    public String getRegex() {
        return "position";
    }

    @Override
    public String parse(Matcher matcher, OfflinePlayer p) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                MessagedResponse<Integer> response = AjQueueSpigotAPI.getInstance()
                        .getPosition(p.getUniqueId())
                        .get(30, TimeUnit.SECONDS);

                cache.put(p.getUniqueId(), response.getEither());
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            } catch (TimeoutException e) {
                plugin.getLogger().log(Level.WARNING, "Timed out while trying to get placeholder data from proxy: ", e);
            }
        });

        return cache.getOrDefault(p.getUniqueId(), "...");
    }

    @Override
    public void cleanCache(Player player) {
        cache.remove(player.getUniqueId());
    }
}
