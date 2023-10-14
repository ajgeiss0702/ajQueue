package us.ajg0702.queue.commands.commands.SlashServer;

import com.google.common.collect.ImmutableList;
import us.ajg0702.queue.api.commands.IBaseCommand;
import us.ajg0702.queue.api.commands.ICommandSender;
import us.ajg0702.queue.commands.BaseCommand;
import us.ajg0702.queue.common.QueueMain;
import us.ajg0702.utils.common.Messages;

import java.util.Collections;
import java.util.List;

public class SlashServerCommand extends BaseCommand {

    final QueueMain main;
    final String server;
    final String command;

    private IBaseCommand moveCommand;


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
        return main.getConfig().getBoolean("require-permission") ? "ajqueue.queue."+server : null;
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
        if(moveCommand == null) {
            moveCommand = main.getPlatformMethods().getCommands().get(0);
        }
        moveCommand.execute(sender, new String[]{server});
    }

    @Override
    public List<String> autoComplete(ICommandSender sender, String[] args) {
        return Collections.emptyList();
    }
}
