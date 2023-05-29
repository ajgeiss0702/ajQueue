package us.ajg0702.queue.spigot.placeholders;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import us.ajg0702.queue.spigot.SpigotMain;
import us.ajg0702.queue.spigot.placeholders.placeholders.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

public class PlaceholderExpansion extends me.clip.placeholderapi.expansion.PlaceholderExpansion {

    private final List<Placeholder> placeholders = new ArrayList<>();

    private final SpigotMain plugin;

    @SuppressWarnings("deprecated")
    public PlaceholderExpansion(SpigotMain plugin) {

        this.plugin = plugin;

        placeholders.add(new EstimatedTime(plugin));
        placeholders.add(new InQueue(plugin));
        placeholders.add(new Position(plugin));
        placeholders.add(new PositionOf(plugin));
        placeholders.add(new Queued(plugin));
        placeholders.add(new QueuedFor(plugin));
        placeholders.add(new Status(plugin));
        placeholders.add(new StatusPlayer(plugin));

    }

    Map<String, CachedPlaceholder> placeholderCache = new HashMap<>();

    @Override
    public String onRequest(OfflinePlayer p, @NotNull String params) {

        if(p == null || !p.isOnline()) {
            return "No player";
        }

        CachedPlaceholder cachedPlaceholder = placeholderCache.computeIfAbsent(params, s -> {
            for(Placeholder placeholder : placeholders) {
                Matcher matcher = placeholder.getPattern().matcher(params);
                if(!matcher.matches()) continue;
                return new CachedPlaceholder(matcher, placeholder);
            }
            return null;
        });
        if(cachedPlaceholder == null) return null;

        return cachedPlaceholder.getPlaceholder().parse(cachedPlaceholder.getMatcher(), p);
    }



    @Override
    public @NotNull String getIdentifier() {
        return "ajqueue";
    }

    @Override
    public @NotNull String getAuthor() {
        return "ajgeiss0702";
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist(){
        return true;
    }

    @Override
    public boolean canRegister() {
        return true;
    }

    public void cleanCache(Player player) {
        placeholders.forEach(p -> p.cleanCache(player));
    }
}