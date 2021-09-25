package us.ajg0702.queue.commands.commands.manage;

import com.google.common.collect.ImmutableList;
import us.ajg0702.queue.api.commands.ICommandSender;
import us.ajg0702.queue.api.queues.QueueServer;
import us.ajg0702.queue.commands.SubCommand;
import us.ajg0702.queue.common.QueueMain;
import us.ajg0702.utils.common.Messages;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Pause extends SubCommand {

    final QueueMain main;
    public Pause(QueueMain main) {
        this.main = main;
    }

    @Override
    public String getName() {
        return "pause";
    }

    @Override
    public ImmutableList<String> getAliases() {
        return ImmutableList.of();
    }

    @Override
    public String getPermission() {
        return "ajqueue.manage.pause";
    }

    @Override
    public Messages getMessages() {
        return main.getMessages();
    }

    @Override
    public void execute(ICommandSender sender, String[] args) {
        if(!checkPermission(sender)) return;

        if(args.length < 1) {
            sender.sendMessage(getMessages().getComponent("commands.pause.more-args"));
            return;
        }

        List<QueueServer> servers;
        QueueServer queueServer = main.getQueueManager().findServer(args[0]);
        if(queueServer == null && !args[0].equalsIgnoreCase("all")) {
            sender.sendMessage(getMessages().getComponent("commands.pause.no-server", "SERVER:"+args[0]));
            return;
        } else if(queueServer == null && args[0].equalsIgnoreCase("all")) {
            servers = main.getQueueManager().getServers();
        } else {
            servers = Collections.singletonList(queueServer);
        }

        for(QueueServer server : servers) {
            if(args.length == 1) {
                server.setPaused(!server.isPaused());
            } else {
                server.setPaused(args[1].equalsIgnoreCase("on") || args[1].equalsIgnoreCase("true"));
            }
            sender.sendMessage(getMessages().getComponent("commands.pause.success",
                    "SERVER:"+server.getName(),
                    "PAUSED:"+getMessages().getString("commands.pause.paused."+server.isPaused())
            ));
        }
    }

    @Override
    public List<String> autoComplete(ICommandSender sender, String[] args) {
        if(args.length == 1) {
            List<String> servers = new ArrayList<>(main.getQueueManager().getServerNames());
            servers.add("all");
            return servers;
        }
        if(args.length == 2) {
            return Arrays.asList("on", "off", "true", "false");
        }
        return new ArrayList<>();
    }
}
