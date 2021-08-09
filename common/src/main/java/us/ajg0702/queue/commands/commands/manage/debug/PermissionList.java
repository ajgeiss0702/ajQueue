package us.ajg0702.queue.commands.commands.manage.debug;

import com.google.common.collect.ImmutableList;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import us.ajg0702.queue.api.commands.ICommandSender;
import us.ajg0702.queue.commands.SubCommand;
import us.ajg0702.queue.common.QueueMain;
import us.ajg0702.utils.common.Messages;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PermissionList extends SubCommand {

    final QueueMain main;
    public PermissionList(QueueMain main) {
        this.main = main;
    }

    @Override
    public String getName() {
        return "permissionlist";
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
        if(!sender.isPlayer()) {
            sender.sendMessage(
                    Component.text("You need to run this as a player with priority!")
                    .color(NamedTextColor.RED)
            );
        }

        List<String> permissions = main.getLogicGetter()
                .getPermissions(main.getPlatformMethods().senderToPlayer(sender));
        if(permissions == null) {
            sender.sendMessage(Component.text("no permission handler"));
            return;
        }

        permissions.forEach(s -> {
            if(!s.toLowerCase(Locale.ROOT).contains("ajqueue")) return;
            sender.sendMessage(Component.text(s));
        });
    }

    @Override
    public List<String> autoComplete(ICommandSender sender, String[] args) {
        return new ArrayList<>();
    }
}
