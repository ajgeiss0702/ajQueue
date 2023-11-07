package us.ajg0702.queue.commands.commands.send;

import com.google.common.collect.ImmutableList;
import us.ajg0702.queue.api.commands.IBaseCommand;
import us.ajg0702.queue.api.commands.ICommandSender;
import us.ajg0702.queue.commands.BaseCommand;
import us.ajg0702.queue.common.QueueMain;
import us.ajg0702.utils.common.Messages;

import java.util.Collections;
import java.util.List;

public class SendAlias extends BaseCommand {

    final QueueMain main;

    private final IBaseCommand sendCommand;


    public SendAlias(QueueMain main) {
        this.main = main;
        sendCommand = main.getPlatformMethods().getCommands().get(3).getSubCommands().get(9);
    }

    @Override
    public String getName() {
        return "send";
    }

    @Override
    public ImmutableList<String> getAliases() {
        return ImmutableList.of();
    }

    @Override
    public String getPermission() {
        return sendCommand.getPermission();
    }

    @Override
    public Messages getMessages() {
        return main.getMessages();
    }

    @Override
    public void execute(ICommandSender sender, String[] args) {
        if(!checkPermission(sender)) return;
        sendCommand.execute(sender, args);
    }

    @Override
    public List<String> autoComplete(ICommandSender sender, String[] args) {
        if(!checkPermission(sender)) return Collections.emptyList();
        return sendCommand.autoComplete(sender, args);
    }

}