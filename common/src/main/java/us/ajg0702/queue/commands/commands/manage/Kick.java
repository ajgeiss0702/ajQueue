package us.ajg0702.queue.commands.commands.manage;

import com.google.common.collect.ImmutableList;
import us.ajg0702.queue.api.commands.ICommandSender;
import us.ajg0702.queue.api.players.QueuePlayer;
import us.ajg0702.queue.api.queues.QueueServer;
import us.ajg0702.queue.commands.SubCommand;
import us.ajg0702.queue.common.QueueMain;
import us.ajg0702.utils.common.Messages;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Kick extends SubCommand {

    final QueueMain main;
    public Kick(QueueMain main) {
        this.main = main;
    }

    @Override
    public String getName() {
        return "kick";
    }

    @Override
    public ImmutableList<String> getAliases() {
        return ImmutableList.of();
    }

    @Override
    public String getPermission() {
        return "ajqueue.manage.kick";
    }

    @Override
    public Messages getMessages() {
        return main.getMessages();
    }

    @Override
    public void execute(ICommandSender sender, String[] args) {
        if(!checkPermission(sender)) return;

        if(args.length < 1) {
            sender.sendMessage(getMessages().getComponent("commands.kick.usage"));
            return;
        }

        List<QueuePlayer> kickPlayers;

        if(args.length == 1) {
            kickPlayers = main.getQueueManager().findPlayerInQueuesByName(args[0]);
        } else {
            QueueServer queue = main.getQueueManager().findServer(args[1]);
            if(queue == null) {
                sender.sendMessage(getMessages().getComponent("commands.kick.unknown-server", "QUEUE:"+args[1]));
                return;
            }
            QueuePlayer player = queue.findPlayer(args[0]);
            if(player == null) {
                kickPlayers = Collections.emptyList();
            } else {
                kickPlayers = Collections.singletonList(player);
            }
        }

        if(kickPlayers.isEmpty()) {
            sender.sendMessage(getMessages().getComponent("commands.kick.no-player", "PLAYER:"+args[0]));
            return;
        }

        for(QueuePlayer player : kickPlayers) {
            player.getQueueServer().removePlayer(player);
        }

        sender.sendMessage(getMessages().getComponent(
                "commands.kick.success",
                "PLAYER:"+args[0],
                "NUM:"+kickPlayers.size(),
                "s:"+ (kickPlayers.size() == 1 ? "" : "s")
        ));
    }

    @Override
    public List<String> autoComplete(ICommandSender sender, String[] args) {
        if(args.length == 1) {
            return filterCompletion(main.getPlatformMethods().getPlayerNames(false), args[0]);
        }
        if(args.length == 2) {
            return filterCompletion(main.getQueueManager().getServerNames(), args[1]);
        }
        return new ArrayList<>();
    }
}

