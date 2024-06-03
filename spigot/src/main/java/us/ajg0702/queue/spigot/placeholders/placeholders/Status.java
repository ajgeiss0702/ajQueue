package us.ajg0702.queue.spigot.placeholders.placeholders;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import us.ajg0702.queue.api.spigot.AjQueueSpigotAPI;
import us.ajg0702.queue.spigot.SpigotMain;
import us.ajg0702.queue.spigot.placeholders.Placeholder;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.regex.Matcher;

public class Status extends Placeholder {
    public Status(SpigotMain plugin) {
        super(plugin);
    }

    private final String invalidMessage = "Invalid queue name";

    private final Map<String, String> cache = new ConcurrentHashMap<>();
    private final Map<String, Long> lastFetch = new ConcurrentHashMap<>();

    @Override
    public String getRegex() {
        return "status_(.*)";
    }

    @Override
    public String parse(Matcher matcher, OfflinePlayer p) {
        String queue = matcher.group(1);
        String cached = cache.getOrDefault(queue, "...");

        if(System.currentTimeMillis() - lastFetch.getOrDefault(queue, 0L) > 2000) {
            lastFetch.put(queue, System.currentTimeMillis());

            plugin.getScheduler().runTaskAsynchronously(() -> {
                if (!p.isOnline()) return;
                try {
                    String response = AjQueueSpigotAPI.getInstance()
                            .getServerStatusString(queue)
                            .get(30, TimeUnit.SECONDS);

                    cache.put(queue, response);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                } catch (ExecutionException e) {
                    if (e.getCause() instanceof IllegalArgumentException) {
                        cache.put(queue, invalidMessage);
                    } else {
                        throw new RuntimeException(e);
                    }
                } catch (TimeoutException | IllegalArgumentException ignored) {
                }
            });
        }

        return cached;
    }

    @Override
    public void cleanCache(Player player) {}
}
