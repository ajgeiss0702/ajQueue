package us.ajg0702.queue.api.queueholders;

import us.ajg0702.queue.api.AjQueueAPI;
import us.ajg0702.queue.api.queues.QueueServer;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class QueueHolderRegistry {

    private Map<String, Class<? extends QueueHolder>> holders = new ConcurrentHashMap<>();
    private Map<String, Runnable> shutdownHooks = new ConcurrentHashMap<>();

    /**
     * Registers a QueueHolder that can be used.
     * @param identifier the config key used to select this holder
     * @param holder     the holder class (must have a {@code QueueServer} constructor)
     */
    public void register(String identifier, Class<? extends QueueHolder> holder) {
        holders.put(identifier, holder);
    }

    /**
     * Registers a QueueHolder with a shutdown hook that is run when {@link #shutdown()} is called.
     * Use this for holders that manage shared resources (e.g. a static connection pool) that need
     * to be released on plugin disable regardless of how many servers are configured.
     *
     * @param identifier   the config key used to select this holder
     * @param holder       the holder class (must have a {@code QueueServer} constructor)
     * @param shutdownHook called once during {@link #shutdown()}
     */
    public void register(String identifier, Class<? extends QueueHolder> holder, Runnable shutdownHook) {
        holders.put(identifier, holder);
        shutdownHooks.put(identifier, shutdownHook);
    }

    /**
     * Runs the shutdown hook for every registered holder that provided one.
     * Should be called from the plugin's disable/shutdown routine.
     */
    public void shutdown() {
        shutdownHooks.values().forEach(Runnable::run);
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
