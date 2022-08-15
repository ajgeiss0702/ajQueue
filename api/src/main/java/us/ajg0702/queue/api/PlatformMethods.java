package us.ajg0702.queue.api;

import us.ajg0702.queue.api.commands.IBaseCommand;
import us.ajg0702.queue.api.commands.ICommandSender;
import us.ajg0702.queue.api.players.AdaptedPlayer;
import us.ajg0702.queue.api.server.AdaptedServer;

import java.util.List;
import java.util.UUID;

public interface PlatformMethods {

    /**
     * Sends a plugin message on the plugin messaging channel
     * @param player The player to send the message through
     * @param channel The (sub)channel
     * @param data The data
     */
    void sendPluginMessage(AdaptedPlayer player, String channel, String... data);

    /**
     * Converts a command sender to an AdaptedPlayer
     * @param sender the commandsender
     * @return the AdaptedPlayer
     */
    AdaptedPlayer senderToPlayer(ICommandSender sender);

    String getPluginVersion();

    @SuppressWarnings("unused")
    List<AdaptedPlayer> getOnlinePlayers();
    List<String> getPlayerNames(boolean lowercase);

    /**
     * Gets an online player by their name
     * @param name The players name
     * @return The AdaptedPlayer for this player
     */
    AdaptedPlayer getPlayer(String name);

    /**
     * Gets an online player by their UUID
     * @param uuid their UUID
     * @return the AdaptedPlayer for this player
     */
    AdaptedPlayer getPlayer(UUID uuid);

    /**
     * Gets a list of the server names
     * @return A list of the server names
     */
    List<String> getServerNames();

    /**
     * Gets the name of the implementation. E.g. bungeecord, velocity
     * @return the name of the implementation
     */
    String getImplementationName();

    List<IBaseCommand> getCommands();

    /**
     * Checks if a plugin is installed
     * @param pluginName The name of the plugin to check for (case in-sensitive)
     * @return if the plugin is on the server
     */
    boolean hasPlugin(String pluginName);

    /**
     * Gets an AdaptedServer from the server name
     * @param name The name of the server
     * @return The AdaptedServer
     */
    AdaptedServer getServer(String name);

    List<? extends AdaptedServer> getServers();

    String getProtocolName(int protocol);
}
