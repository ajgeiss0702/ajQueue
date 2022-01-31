package us.ajg0702.queue.commands.commands.manage.debug;

import com.google.common.collect.ImmutableList;
import net.kyori.adventure.text.Component;
import us.ajg0702.queue.api.commands.ICommandSender;
import us.ajg0702.queue.commands.SubCommand;
import us.ajg0702.queue.common.QueueMain;
import us.ajg0702.utils.common.Messages;

import java.util.ArrayList;
import java.util.List;

public class Protocol extends SubCommand {

    final QueueMain main;
    public Protocol(QueueMain main) {
        this.main = main;
    }

    @Override
    public String getName() {
        return "protocol";
    }

    @Override
    public ImmutableList<String> getAliases() {
        return ImmutableList.of();
    }

    @Override
    public boolean showInTabComplete() {
        return false;
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
        if(!checkPermission(sender)) return;
        if(!sender.isPlayer()) return;
        sender.sendMessage(Component.text(main.getPlatformMethods().senderToPlayer(sender).getProtocolVersion()));
    }

    @Override
    public List<String> autoComplete(ICommandSender sender, String[] args) {
        return new ArrayList<>();
    }
}
