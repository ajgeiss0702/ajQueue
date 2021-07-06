package us.ajg0702.queue.common;

public interface PlatformMethods {
    /**
     * BungeeUtils.sendCustomData(p, "position", pos+"");
     *         BungeeUtils.sendCustomData(p, "positionof", len+"");
     *         BungeeUtils.sendCustomData(p, "queuename", pl.aliases.getAlias(s));
     *         BungeeUtils.sendCustomData(p, "inqueue", "true");
     *         BungeeUtils.sendCustomData(p, "inqueueevent", "true");
     */
    void sendJoinQueueChannelMessages();

}
