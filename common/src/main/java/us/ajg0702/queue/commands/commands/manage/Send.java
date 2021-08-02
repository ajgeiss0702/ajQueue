package us.ajg0702.queue.commands.commands.manage;

import com.google.common.collect.ImmutableList;
import net.kyori.adventure.text.Component;
import us.ajg0702.queue.api.commands.ICommandSender;
import us.ajg0702.queue.api.players.AdaptedPlayer;
import us.ajg0702.queue.api.server.AdaptedServer;
import us.ajg0702.queue.commands.SubCommand;
import us.ajg0702.queue.common.QueueMain;
import us.ajg0702.utils.common.Messages;

import java.util.ArrayList;
import java.util.List;

public class Send extends SubCommand {

    final QueueMain main;
    public Send(QueueMain main) {
        this.main = main;
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
        return "ajqueue.manage.send";
    }

    @Override
    public Messages getMessages() {
        return main.getMessages();
    }

    @Override
    public void execute(ICommandSender sender, String[] args) {
        if(!checkPermission(sender)) return;

        if(main.getQueueManager().findServer(args[1]) == null) {
            sender.sendMessage(getMessages().getComponent("errors.server-not-exist", "SERVER:"+args[2]));
            return;
        }

        List<String> playerNames = main.getPlatformMethods().getPlayerNames(true);
        if(playerNames.contains(args[0].toLowerCase())) {

            AdaptedPlayer ply = main.getPlatformMethods().getPlayer(args[0]);
            if(ply == null) {
                sender.sendMessage(Component.text("player not found"));
                return;
            }
            if(ply.getName() == null) {
                sender.sendMessage(Component.text("name null"));
            }
            main.getQueueManager().addToQueue(ply, args[1]);
            sender.sendMessage(getMessages().getComponent("send",
                    "PLAYER:"+ply.getName(),
                    "SERVER:"+args[1])
            );
        } else if(main.getQueueManager().getServerNames().contains(args[0])) {

            AdaptedServer from = main.getPlatformMethods().getServer(args[0]);
            if(from == null) {
                sender.sendMessage(getMessages().getComponent("errors.server-not-exist", "SERVER:"+args[0]));
                return;
            }
            List<AdaptedPlayer> players = new ArrayList<>(from.getPlayers());
            for(AdaptedPlayer ply : players) {
                main.getQueueManager().addToQueue(ply, args[1]);
            }

            sender.sendMessage(getMessages().getComponent("send", "PLAYER:"+args[0], "SERVER:"+args[1]));

        } else {
            sender.sendMessage(getMessages().getComponent("commands.send.player-not-found"));
        }
    }

    @Override
    public List<String> autoComplete(ICommandSender sender, String[] args) {
        if(args.length == 1) {
            List<String> options = new ArrayList<>(main.getPlatformMethods().getServerNames());
            options.addAll(main.getPlatformMethods().getPlayerNames(false));
            return options;
        }
        if(args.length == 2) {
            return main.getQueueManager().getServerNames();
        }
        return new ArrayList<>();
    }



}
