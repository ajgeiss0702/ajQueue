package us.ajg0702.queue.api;

import us.ajg0702.queue.api.players.AdaptedPlayer;
import us.ajg0702.queue.api.players.QueuePlayer;
import us.ajg0702.queue.api.queues.QueueServer;
import us.ajg0702.queue.api.server.AdaptedServer;

public interface PlatformMethods {
    /**
     * BungeeUtils.sendCustomData(p, "position", pos+"");
     *         BungeeUtils.sendCustomData(p, "positionof", len+"");
     *         BungeeUtils.sendCustomData(p, "queuename", pl.aliases.getAlias(s));
     *         BungeeUtils.sendCustomData(p, "inqueue", "true");
     *         BungeeUtils.sendCustomData(p, "inqueueevent", "true");
     */
    void sendJoinQueueChannelMessages(QueueServer queueServer, QueuePlayer queuePlayer);

    /**
     * Sends a plugin message on the plugin messaging channel
     * @param player The player to send the message through
     * @param channel The (sub)channel
     * @param data The data
     */
    void sendPluginMessage(AdaptedPlayer player, String channel, String... data);

}
