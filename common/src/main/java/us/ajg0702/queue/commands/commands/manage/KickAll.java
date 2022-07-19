package us.ajg0702.queue.commands.commands.manage;

import com.google.common.collect.ImmutableList;
import us.ajg0702.queue.api.commands.ICommandSender;
import us.ajg0702.queue.api.players.QueuePlayer;
import us.ajg0702.queue.api.queues.QueueServer;
import us.ajg0702.queue.commands.SubCommand;
import us.ajg0702.queue.common.QueueMain;
import us.ajg0702.utils.common.Messages;

import java.util.ArrayList;
import java.util.List;

public class KickAll extends SubCommand {

    final QueueMain main;
    public KickAll(QueueMain main) {
        this.main = main;
    }

    @Override
    public String getName() {
        return "kickall";
    }

    @Override
    public ImmutableList<String> getAliases() {
        return ImmutableList.of();
    }

    @Override
    public String getPermission() {
        return "ajqueue.manage.kickall";
    }

    @Override
    public Messages getMessages() {
        return main.getMessages();
    }

    @Override
    public void execute(ICommandSender sender, String[] args) {
        if(!checkPermission(sender)) return;

        if(args.length < 1) {
            sender.sendMessage(getMessages().getComponent("commands.kickall.usage"));
            return;
        }

        QueueServer server = main.getQueueManager().findServer(args[0]);
        List<QueuePlayer> kickPlayers = new ArrayList<>(server.getQueue());

        for(QueuePlayer player : kickPlayers) {
            player.getQueueServer().removePlayer(player);
        }

        sender.sendMessage(getMessages().getComponent(
                "commands.kickall.success",
                "SERVER:"+args[0],
                "NUM:"+kickPlayers.size(),
                "s:"+ (kickPlayers.size() == 1 ? "" : "s")
        ));
    }

    @Override
    public List<String> autoComplete(ICommandSender sender, String[] args) {
        if(args.length == 1) {
            return main.getQueueManager().getServerNames();
        }
        return new ArrayList<>();
    }
}

