package us.ajg0702.queue.platforms.bungeecord.commands;


import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;
import us.ajg0702.queue.commands.BaseCommand;
import us.ajg0702.queue.common.QueueMain;

public class BungeeCommand extends Command implements TabExecutor {
    QueueMain main;
    BaseCommand command;
    public BungeeCommand(QueueMain main, BaseCommand command) {
        super(command.getName(), command.getPermission(), command.getAliases().toArray(new String[0]));
        this.main = main;
        this.command = command;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        command.execute(new BungeeSender(sender), args);
    }

    @Override
    public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
        return command.autoComplete(new BungeeSender(sender), args);
    }
}
