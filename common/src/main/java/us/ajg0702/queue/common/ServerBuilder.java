package us.ajg0702.queue.common;

import us.ajg0702.queue.api.queues.QueueServer;

import java.util.List;

public interface ServerBuilder {
    List<QueueServer> getServers();
}
