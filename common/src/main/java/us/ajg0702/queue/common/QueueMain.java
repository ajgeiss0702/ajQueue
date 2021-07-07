package us.ajg0702.queue.common;

import us.ajg0702.queue.api.AliasManager;
import us.ajg0702.queue.api.Logic;
import us.ajg0702.queue.api.PlatformMethods;
import us.ajg0702.queue.api.ServerBuilder;
import us.ajg0702.utils.common.Config;
import us.ajg0702.utils.common.Messages;

import java.util.logging.Logger;

public class QueueMain {


    private double timeBetweenPlayers;
    public double getTimeBetweenPlayers() {
        return timeBetweenPlayers;
    }

    private Config config;
    public Config getConfig() {
        return config;
    }

    private Messages messages;
    public Messages getMessages() {
        return messages;
    }

    private AliasManager aliasManager;
    public AliasManager getAliasManager() {
        return aliasManager;
    }

    private Logic logic;
    public Logic getLogic() {
        return logic;
    }

    public boolean isPremium() {
        return getLogic().isPremium();
    }

    private PlatformMethods platformMethods;
    public PlatformMethods getPlatformMethods() {
        return platformMethods;
    }

    private Logger logger;
    public Logger getLogger() {
        return logger;
    }

    private ServerBuilder serverBuilder;
    public ServerBuilder getServerBuilder() {
        return serverBuilder;
    }
}
