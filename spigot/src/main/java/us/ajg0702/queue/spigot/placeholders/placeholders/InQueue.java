package us.ajg0702.queue.spigot.placeholders.placeholders;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import us.ajg0702.queue.api.spigot.AjQueueSpigotAPI;
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

public class InQueue extends Placeholder {
    public InQueue(SpigotMain plugin) {
        super(plugin);
    }

    private final Map<UUID, String> cache = new ConcurrentHashMap<>();

    @Override
    public String getRegex() {
        return "inqueue";
    }

    @Override
    public String parse(Matcher matcher, OfflinePlayer p) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            if(!p.isOnline()) return;
            try {
                Boolean response = AjQueueSpigotAPI.getInstance()
                        .isInQueue(p.getUniqueId())
                        .get(30, TimeUnit.SECONDS);

                cache.put(p.getUniqueId(), response + "");
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            } catch (TimeoutException | IllegalArgumentException ignored) {}
        });

        return cache.getOrDefault(p.getUniqueId(), "...");
    }

    @Override
    public void cleanCache(Player player) {
        cache.remove(player.getUniqueId());
    }
}
