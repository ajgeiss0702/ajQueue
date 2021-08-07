package us.ajg0702.queue.commands.commands.manage;

import com.google.common.collect.ImmutableList;
import net.kyori.adventure.text.Component;
import us.ajg0702.queue.api.commands.ICommandSender;
import us.ajg0702.queue.commands.SubCommand;
import us.ajg0702.queue.common.QueueMain;
import us.ajg0702.utils.common.Messages;

import java.util.ArrayList;
import java.util.List;

public class Version extends SubCommand {

    final QueueMain main;
    public Version(QueueMain main) {
        this.main = main;
    }

    @Override
    public String getName() {
        return "version";
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
        if(!checkPermission(sender)) return;
        sender.sendMessage(Component.text(main.getPlatformMethods().getPluginVersion()));
    }

    @Override
    public List<String> autoComplete(ICommandSender sender, String[] args) {
        return new ArrayList<>();
    }
}
