package us.ajg0702.queue.commands.commands.SlashServer;

import com.google.common.collect.ImmutableList;
import us.ajg0702.queue.api.commands.ICommandSender;
import us.ajg0702.queue.commands.BaseCommand;
import us.ajg0702.queue.common.QueueMain;
import us.ajg0702.utils.common.Messages;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SlashServerCommand extends BaseCommand {

    final QueueMain main;
    final String server;
    final String command;
    public SlashServerCommand(QueueMain main, String server) {
        this.main = main;
        this.server = server;
        this.command = server;
    }
    public SlashServerCommand(QueueMain main, String command, String server) {
        this.main = main;
        this.server = server;
        this.command = command;
    }

    @Override
    public String getName() {
        return command;
    }

    @Override
    public ImmutableList<String> getAliases() {
        return ImmutableList.of();
    }

    @Override
    public String getPermission() {
        return null;
    }

    @Override
    public Messages getMessages() {
        return main.getMessages();
    }

    @Override
    public void execute(ICommandSender sender, String[] args) {
        if(!sender.isPlayer()) {
            sender.sendMessage(getMessages().getComponent("errors.player-only"));
            return;
        }
        if(main.getConfig().getBoolean("require-permission") && !sender.hasPermission("ajqueue.queue."+server)) {
            sender.sendMessage(getMessages().getComponent("noperm"));
            return;
        }
        main.getQueueManager().addToQueue(main.getPlatformMethods().senderToPlayer(sender), server);
    }

    @Override
    public List<String> autoComplete(ICommandSender sender, String[] args) {
        return new ArrayList<>();
    }
}
