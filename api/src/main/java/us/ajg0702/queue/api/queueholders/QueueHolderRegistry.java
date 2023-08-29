package us.ajg0702.queue.api.queueholders;

import us.ajg0702.queue.api.AjQueueAPI;
import us.ajg0702.queue.api.queues.QueueServer;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class QueueHolderRegistry {

    private Map<String, Class<? extends QueueHolder>> holders = new ConcurrentHashMap<>();

    /**
     * Register a QueueHolder that can be used
     * @param holder The QueueHolder to register
     */
    public void register(String identifier, Class<? extends QueueHolder> holder) {
        holders.put(identifier, holder);
    }

    public QueueHolder getQueueHolder(QueueServer queueServer) {
        String queueHolderName = AjQueueAPI.getInstance().getConfig().getString("queue-holder");
        QueueHolder queueHolder = getQueueHolder(queueHolderName, queueServer);
        if(queueHolder == null) {
            AjQueueAPI.getInstance().getLogger().warn("Invalid queue-holder '" + queueHolderName + "'! Using the default one");
            return getQueueHolder("default", queueServer);
        }
        return queueHolder;
    }

    public QueueHolder getQueueHolder(String identifier, QueueServer queueServer) {
        Class<? extends QueueHolder> holder = holders.get(identifier);
        if(holder == null) return null;
        try {
            return holder.getConstructor(QueueServer.class).newInstance(queueServer);
        } catch(NoSuchMethodException e) {
            throw new IllegalArgumentException("QueueHolder " + identifier + " is missing the required constructor!");
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }
}
