package us.ajg0702.queue.api;

import us.ajg0702.queue.api.players.AdaptedPlayer;

public interface PlatformMethods {
    /**
     * BungeeUtils.sendCustomData(p, "position", pos+"");
     *         BungeeUtils.sendCustomData(p, "positionof", len+"");
     *         BungeeUtils.sendCustomData(p, "queuename", pl.aliases.getAlias(s));
     *         BungeeUtils.sendCustomData(p, "inqueue", "true");
     *         BungeeUtils.sendCustomData(p, "inqueueevent", "true");
     */
    void sendJoinQueueChannelMessages();

    /**
     * Sends a plugin message on the plugin messaging channel
     * @param player The player to send the message through
     * @param channel The (sub)channel
     * @param data The data
     */
    void sendPluginMessage(AdaptedPlayer player, String channel, String... data);

}
