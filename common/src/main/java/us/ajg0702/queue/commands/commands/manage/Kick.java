package us.ajg0702.queue.commands.commands.manage;

import com.google.common.collect.ImmutableList;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.spongepowered.configurate.ConfigurateException;
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
            kickPlayers = Collections.singletonList(queue.findPlayer(args[0]));
        }

        if(kickPlayers.size() == 0) {
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
            return main.getPlatformMethods().getPlayerNames(false);
        }
        if(args.length == 2) {
            return main.getQueueManager().getServerNames();
        }
        return new ArrayList<>();
    }
}

