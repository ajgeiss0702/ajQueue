package us.ajg0702.queue.commands.commands.leavequeue;

import com.google.common.collect.ImmutableList;
import us.ajg0702.queue.api.commands.ICommandSender;
import us.ajg0702.queue.api.commands.ISubCommand;
import us.ajg0702.queue.api.players.AdaptedPlayer;
import us.ajg0702.queue.api.players.QueuePlayer;
import us.ajg0702.queue.api.queues.QueueServer;
import us.ajg0702.queue.commands.BaseCommand;
import us.ajg0702.queue.common.QueueMain;
import us.ajg0702.utils.common.Messages;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LeaveCommand extends BaseCommand {


    private final QueueMain main;

    public LeaveCommand(QueueMain main) {
        this.main = main;
    }

    @Override
    public String getName() {
        return "leavequeue";
    }

    @Override
    public ImmutableList<String> getAliases() {
        return ImmutableList.of("leaveq");
    }

    @Override
    public ImmutableList<ISubCommand> getSubCommands() {
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
        AdaptedPlayer player = main.getPlatformMethods().senderToPlayer(sender);
        List<QueueServer> servers = main.getQueueManager().getPlayerQueues(player);

        if(servers.size() == 0) {
            sender.sendMessage(getMessages().getComponent("commands.leave.no-queues"));
            return;
        }


        if(servers.size() == 1) {
            servers.get(0).removePlayer(player);
            sender.sendMessage(getMessages().getComponent("commands.leave-queue", "SERVER:"+servers.get(0).getAlias()));
            return;
        }


        if(args.length <= 0) {
            sender.sendMessage(getMessages().getComponent("commands.leave.more-args", "QUEUES:"+getQueueList(servers)));
            return;
        }

        String leaving = args[0];
        QueueServer leavingServer = main.getQueueManager().findServer(leaving);
        if(leavingServer == null) {
            sender.sendMessage(getMessages().getComponent("commands.leave.not-queued", "QUEUES:"+getQueueList(servers)));
            return;
        }
        QueuePlayer queuePlayer = leavingServer.findPlayer(player);
        if(queuePlayer == null) {
            sender.sendMessage(getMessages().getComponent("commands.leave.not-queued", "QUEUES:"+getQueueList(servers)));
            return;
        }


        leavingServer.removePlayer(queuePlayer);
        sender.sendMessage(getMessages().getComponent("commands.leave-queue", "SERVER:"+leavingServer.getAlias()));

    }

    private String getQueueList(List<QueueServer> servers) {
        StringBuilder queueList = new StringBuilder();
        for(QueueServer server : servers) {
            queueList.append(getMessages().getString("commands.leave.queues-list-format").replaceAll("\\{NAME}", server.getName()));
        }
        if(queueList.length() > 2) {
            queueList = new StringBuilder(queueList.substring(0, queueList.length() - 2));
        }
        return queueList.toString();
    }

    @Override
    public List<String> autoComplete(ICommandSender sender, String[] args) {
        if(args.length > 1) return Collections.emptyList();
        List<QueuePlayer> servers = main.getQueueManager().findPlayerInQueues(main.getPlatformMethods().senderToPlayer(sender));
        List<String> serverNames = new ArrayList<>();
        for(QueuePlayer queuePlayer : servers) {
            serverNames.add(queuePlayer.getQueueServer().getName());
        }
        return filterCompletion(serverNames, args[0]);
    }
}
