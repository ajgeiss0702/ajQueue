package us.ajg0702.queue.common;

import us.ajg0702.queue.api.AliasManager;
import us.ajg0702.queue.api.EventHandler;
import us.ajg0702.queue.api.PlatformMethods;
import us.ajg0702.queue.api.QueueManager;
import us.ajg0702.queue.api.premium.Logic;
import us.ajg0702.queue.api.premium.LogicGetter;
import us.ajg0702.queue.api.util.QueueLogger;
import us.ajg0702.utils.common.Config;
import us.ajg0702.utils.common.Messages;

public abstract class AjQueueAPI {

    public static AjQueueAPI INSTANCE;

    /**
     * Gets the instance of the ajQueue API
     * @return the ajQueue API
     */
    @SuppressWarnings("unused")
    public static AjQueueAPI getInstance() {
        return INSTANCE;
    }


    /**
     * Gets the time that ajQueue will wait between sending players. In seconds
     * @return The time, in seconds, ajQueue will wait between attempting to send players
     */
    public abstract double getTimeBetweenPlayers();

    /**
     * Updates the time between players. Takes it from the config.
     */
    public abstract void setTimeBetweenPlayers();

    /**
     * Gets the ajQueue config
     * @return the ajQueue config
     */
    public abstract Config getConfig();

    /**
     * Gets the ajQueue messages manager
     * @return the messages manager
     */
    public abstract Messages getMessages();

    /**
     * Gets the alias manager. Used to get aliases of servers set in ajqueue's config.
     * Note that the alias manager on the free version will just return the server's name
     * @return The alias manager
     */
    public abstract AliasManager getAliasManager();

    /**
     * Gets the priority logic. Note that the priority logic for the free version does nothing.
     * @return The priority logic
     */
    public abstract Logic getLogic();

    /**
     * Checks if the plugin is the premium version or not.
     * @return True if ajQueuePlus, false if ajQueue
     */
    public abstract boolean isPremium();

    /**
     * Gets the PlatformMethods for the platform (e.g. bungee, velocity)
     * The methods in this class do things that the code for each is different on the platform.
     * @return the PlatformMethods
     */
    public abstract PlatformMethods getPlatformMethods();

    /**
     * Gets the ajQueue logger. If you are using this, please add your own prefix to it.
     * @return The ajQueue logger
     */
    public abstract QueueLogger getLogger();

    /**
     * Gets the repeating task manager
     * @return The TaskManager
     */
    public abstract TaskManager getTaskManager();

    /**
     * Gets the event handler.
     *
     * This class will probably be replaced in the future with an actual event system
     * @return the EventHandler
     */
    public abstract EventHandler getEventHandler();

    /**
     * Gets the queue manager. Most things you do interacting with queues will be through this.
     * @return the QueueManager
     */
    public abstract QueueManager getQueueManager();

    /**
     * Gets the logic getter.
     * @return The logic getter
     */
    public abstract LogicGetter getLogicGetter();

    /**
     * Tells ajQueue to shut down.
     */
    public abstract void shutdown();
}
