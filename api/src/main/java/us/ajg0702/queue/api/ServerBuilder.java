package us.ajg0702.queue.api;

import us.ajg0702.queue.api.queues.QueueServer;
import us.ajg0702.queue.api.server.AdaptedServer;

import java.util.List;

public interface ServerBuilder {
    List<QueueServer> getServers();

    QueueServer buildGroup(String name, List<AdaptedServer> servers);
}
