package us.ajg0702.queue.commands.commands.manage;

import com.google.common.collect.ImmutableList;
import us.ajg0702.queue.api.commands.ICommandSender;
import us.ajg0702.queue.api.commands.ISubCommand;
import us.ajg0702.queue.api.players.AdaptedPlayer;
import us.ajg0702.queue.commands.SubCommand;
import us.ajg0702.queue.common.QueueMain;
import us.ajg0702.queue.common.utils.Debug;
import us.ajg0702.utils.common.Messages;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class PauseQueueServer extends SubCommand {

    public static final List<AdaptedPlayer> pausedPlayers = new CopyOnWriteArrayList<>();

    final QueueMain main;
    public PauseQueueServer(QueueMain main) {
        this.main = main;
    }

    @Override
    public String getName() {
        return "pausequeueserver";
    }

    @Override
    public ImmutableList<String> getAliases() {
        return ImmutableList.of("pauseqs");
    }

    @Override
    public ImmutableList<ISubCommand> getSubCommands() {
        return ImmutableList.of();
    }

    @Override
    public String getPermission() {
        return "ajqueue.manage.pausequeueserver";
    }

    @Override
    public boolean showInTabComplete() {
        return true;
    }

    @Override
    public Messages getMessages() {
        return main.getMessages();
    }

    @Override
    public void execute(ICommandSender sender, String[] args) {
        if(!checkPermission(sender)) return;

        if(!sender.isPlayer()) {
            sender.sendMessage(getMessages().getComponent("errors.player-only"));
            return;
        }

        AdaptedPlayer player = main.getPlatformMethods().getPlayer(sender.getUniqueId());
        if(pausedPlayers.contains(player)) {
            pausedPlayers.remove(player);
            sender.sendMessage(getMessages().getComponent("commands.pausequeueserver.unpaused"));
            return;
        }

        pausedPlayers.add(player);
        sender.sendMessage(getMessages().getComponent("commands.pausequeueserver.paused"));
    }

    @Override
    public List<String> autoComplete(ICommandSender sender, String[] args) {
        return Collections.emptyList();
    }
}
