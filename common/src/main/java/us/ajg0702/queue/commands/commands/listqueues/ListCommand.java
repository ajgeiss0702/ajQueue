package us.ajg0702.queue.commands.commands.listqueues;

import com.google.common.collect.ImmutableList;
import net.kyori.adventure.text.Component;
import us.ajg0702.queue.api.commands.ICommandSender;
import us.ajg0702.queue.api.commands.ISubCommand;
import us.ajg0702.queue.api.players.AdaptedPlayer;
import us.ajg0702.queue.api.queues.QueueServer;
import us.ajg0702.queue.commands.BaseCommand;
import us.ajg0702.queue.common.QueueMain;
import us.ajg0702.utils.common.Messages;

import java.util.ArrayList;
import java.util.List;

public class ListCommand extends BaseCommand {

    private final QueueMain main;

    public ListCommand(QueueMain main) {
        this.main = main;
    }

    @Override
    public String getName() {
        return "listqueues";
    }

    @Override
    public ImmutableList<String> getAliases() {
        return ImmutableList.of("listq");
    }

    @Override
    public ImmutableList<ISubCommand> getSubCommands() {
        return ImmutableList.<ISubCommand>builder().build();
    }

    @Override
    public String getPermission() {
        return "ajqueue.listqueues";
    }

    @Override
    public Messages getMessages() {
        return main.getMessages();
    }

    @Override
    public void execute(ICommandSender sender, String[] args) {
        if(!checkPermission(sender)) return;

        AdaptedPlayer spp = null;
        if(sender.isPlayer()) {
            spp = main.getPlatformMethods().senderToPlayer(sender);
        }


        Component m = main.getMessages().getComponent("commands.listqueues.header");
        for(QueueServer s : main.getQueueManager().getServers()) {
            String color = "&a";
            if(!s.isOnline()) {
                color = "&c";
            } else if(!s.isJoinable(spp)) {
                color = "&e";
            }

            m = m.append(Component.text("\n"));
            m = m.append(main.getMessages().getComponent("commands.listqueues.format",
                    "COLOR:" + main.getMessages().color(color),
                    "NAME:" + s.getName(),
                    "COUNT:" + s.getQueue().size(),
                    "STATUS:" + s.getStatusString(spp)
            ));
        }
        sender.sendMessage(m);
    }

    @Override
    public List<String> autoComplete(ICommandSender sender, String[] args) {
        return new ArrayList<>();
    }
}
