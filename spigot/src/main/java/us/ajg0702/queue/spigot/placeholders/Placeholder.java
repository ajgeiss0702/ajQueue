package us.ajg0702.queue.spigot.placeholders;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import us.ajg0702.queue.spigot.SpigotMain;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class Placeholder {

    protected final SpigotMain plugin;

    private Pattern pattern;

    public Placeholder(SpigotMain plugin) {
        this.plugin = plugin;
    }

    public abstract String getRegex();

    public Pattern getPattern() {
        if(pattern == null) {
            pattern = Pattern.compile(getRegex());
        }
        return pattern;
    }

    public abstract String parse(Matcher matcher, OfflinePlayer p);

    public abstract void cleanCache(Player player);
}
