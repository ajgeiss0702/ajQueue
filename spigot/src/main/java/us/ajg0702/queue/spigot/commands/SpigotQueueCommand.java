package us.ajg0702.queue.spigot.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import us.ajg0702.queue.api.spigot.AjQueueSpigotAPI;
import us.ajg0702.queue.spigot.SpigotMain;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static us.ajg0702.utils.common.Messages.color;

public class SpigotQueueCommand extends BukkitCommand {

    final SpigotMain pl;
    public SpigotQueueCommand(SpigotMain pl, boolean serverAlias, boolean spigotQueueAlias) {
        super("queue");
        this.pl = pl;

        this.description = "Queue for a server";
        this.usageMessage = "/queue [player] <server>";

        List<String> aliases = new ArrayList<>(Arrays.asList("move", "joinq", "joinqueue"));
        if(serverAlias) aliases.add("server");
        if(spigotQueueAlias) aliases.add("spigotqueue");
        this.setAliases(aliases);
    }
    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
        if(pl.checkProxyResponse(sender)) return true;
        Player player = null;
        if(sender instanceof Player) {
            player = (Player) sender;
        }
        if(args.length < 1) return false;

        String serverName = args[0];

        boolean sudo = true;

        // /queue <player> <server>
        if(args.length > 1) {
            sudo = false;
            if(!sender.hasPermission("ajqueue.send")) {
                sender.sendMessage(color("&cYou do not have permission to do this!"));
                return true;
            }
            pl.getLogger().info("Sending "+args[0]+" to queue '" + args[1] + "'");
            Player playerToSend = Bukkit.getPlayer(args[0]);
            if(playerToSend == null) {
                sender.sendMessage(color("&cCannot find that player!"));
                return true;
            }
            player = playerToSend;
            serverName = args[1];
        }

        if(player == null) {
            sender.sendMessage("I need to know what player to send!");
            return true;
        }

        if(sudo) {
            if(pl.getAConfig().getBoolean("send-queue-commands-in-batches")) {
                pl.getQueueBatch().put(player, serverName);
            } else {
                AjQueueSpigotAPI.getInstance().sudoQueue(player.getUniqueId(), serverName);
            }
        } else {
            AjQueueSpigotAPI.getInstance().addToQueue(player.getUniqueId(), serverName);
        }

        return true;
    }
}
