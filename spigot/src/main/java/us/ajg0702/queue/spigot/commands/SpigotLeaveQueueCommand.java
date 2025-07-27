package us.ajg0702.queue.spigot.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import us.ajg0702.queue.spigot.SpigotMain;

import java.util.Collections;

public class SpigotLeaveQueueCommand extends BukkitCommand {

    final SpigotMain pl;
    public SpigotLeaveQueueCommand(SpigotMain pl) {
        super("leavequeue");
        this.pl = pl;

        this.description = "Leaves a queue";
        this.usageMessage = "/leavequeue [queue]";
        setAliases(Collections.singletonList("leaveq"));
    }
    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
        if(pl.checkProxyResponse(sender)) return true;
        Player player = null;
        if(sender instanceof Player) {
            player = (Player) sender;
        }
        if(player == null) return true;
        StringBuilder arg = new StringBuilder();
        for(String a : args) {
            arg.append(" ");
            arg.append(a);
        }
        pl.sendMessage(player, "leavequeue", arg.toString());
        return true;
    }
}
