package us.ajg0702.queue.api.server;

import us.ajg0702.queue.api.queues.QueueServer;

import java.util.List;

public interface ServerBuilder {
    List<AdaptedServer> getServers();

    AdaptedServer getServer(String name);
}
