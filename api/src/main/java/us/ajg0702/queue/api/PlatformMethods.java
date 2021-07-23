package us.ajg0702.queue.api;

import us.ajg0702.queue.api.commands.IBaseCommand;
import us.ajg0702.queue.api.commands.ICommandSender;
import us.ajg0702.queue.api.players.AdaptedPlayer;
import us.ajg0702.queue.api.players.QueuePlayer;
import us.ajg0702.queue.api.queues.QueueServer;

import java.util.List;

public interface PlatformMethods {

    /**
     * Sends a plugin message on the plugin messaging channel
     * @param player The player to send the message through
     * @param channel The (sub)channel
     * @param data The data
     */
    @SuppressWarnings("EmptyMethod")
    void sendPluginMessage(AdaptedPlayer player, String channel, String... data);

    /**
     * Converts a command sender to an AdaptedPlayer
     * @param sender the commandsender
     * @return the AdaptedPlayer
     */
    AdaptedPlayer senderToPlayer(ICommandSender sender);

    String getPluginVersion();

    List<AdaptedPlayer> getOnlinePlayers();
    List<String> getPlayerNames(boolean lowercase);
    AdaptedPlayer getPlayer(String name);

    List<String> getServerNames();


    List<IBaseCommand> getCommands();
}
