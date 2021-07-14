package us.ajg0702.queue.common;

import org.spongepowered.configurate.ConfigurateException;
import us.ajg0702.queue.api.*;
import us.ajg0702.queue.api.server.ServerBuilder;
import us.ajg0702.queue.logic.LogicGetter;
import us.ajg0702.utils.common.Config;
import us.ajg0702.utils.common.Messages;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.*;
import java.util.logging.Logger;

public class QueueMain {


    private double timeBetweenPlayers;
    public double getTimeBetweenPlayers() {
        return timeBetweenPlayers;
    }
    public void setTimeBetweenPlayers() {
        this.timeBetweenPlayers = config.getDouble("wait-time");
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

    private final PlatformMethods platformMethods;
    public PlatformMethods getPlatformMethods() {
        return platformMethods;
    }

    private final Logger logger;
    public Logger getLogger() {
        return logger;
    }

    private final TaskManager taskManager = new TaskManager(this);
    public TaskManager getTaskManager() {
        return taskManager;
    }

    private final EventHandler eventHandler = new EventHandlerImpl(this);
    public EventHandler getEventHandler() {
        return eventHandler;
    }

    private final List<CompletableFuture<ServerBuilder>> serverCompletableFutures = new ArrayList<>();
    private ServerBuilder serverBuilder;
    public ServerBuilder getServerBuilder() {
        return serverBuilder;
    }
    public CompletableFuture<ServerBuilder> getFutureServerBuilder() {
        CompletableFuture<ServerBuilder> completableFuture = new CompletableFuture<>();
        if(serverBuilder != null) {
            completableFuture.complete(serverBuilder);
        }
        serverCompletableFutures.add(completableFuture);
        return completableFuture;
    }
    public void setServerBuilder(ServerBuilder serverBuilder) {
        if(this.serverBuilder != null) throw new IllegalStateException("SeverBuilder already set");
        this.serverBuilder = serverBuilder;
        for(CompletableFuture<ServerBuilder> future : serverCompletableFutures) {
            future.complete(serverBuilder);
        }
    }

    private QueueManager queueManager;
    public QueueManager getQueueManager() {
        return queueManager;
    }


    private final File dataFolder;


    public QueueMain(Logger logger, PlatformMethods platformMethods, File dataFolder) {
        this.logger = logger;
        this.platformMethods = platformMethods;
        this.dataFolder = dataFolder;

        constructMessages();

        try {
            config = new Config(dataFolder, logger);
        } catch (ConfigurateException e) {
            logger.warning("Unable to load config:");
            e.printStackTrace();
            return;
        }

        setTimeBetweenPlayers();

        queueManager = new QueueManagerImpl(this);

        logic = new LogicGetter().constructLogic();
        aliasManager = new LogicGetter().constructAliasManager(config);

        taskManager.rescheduleTasks();

    }

    private void constructMessages() {
        LinkedHashMap<String, String> d = new LinkedHashMap<>();

        d.put("status.offline.base", "&c{SERVER} is {STATUS}. &7You are in position &f{POS}&7 of &f{LEN}&7.");

        d.put("status.offline.offline", "offline");
        d.put("status.offline.restarting", "restarting");
        d.put("status.offline.full", "full");
        d.put("status.offline.restricted", "restricted");
        d.put("status.offline.paused", "paused");

        d.put("status.online.base", "&7You are in position &f{POS}&7 of &f{LEN}&7. Estimated time: {TIME}");
        d.put("status.left-last-queue", "&aYou left the last queue you were in.");
        d.put("status.now-in-queue", "&aYou are now queued for {SERVER}! &7You are in position &f{POS}&7 of &f{LEN}&7.\n&7Type &f/leavequeue&7 or &f<click:run_command:/leavequeue {SERVERNAME}>click here</click>&7 to leave the queue!");
        d.put("status.now-in-empty-queue", "");
        d.put("status.sending-now", "&aSending you to &f{SERVER} &anow..");

        d.put("errors.server-not-exist", "&cThe server {SERVER} does not exist!");
        d.put("errors.already-queued", "&cYou are already queued for that server!");
        d.put("errors.player-only", "&cThis command can only be executed as a player!");
        d.put("errors.already-connected", "&cYou are already connected to this server!");
        d.put("errors.cant-join-paused", "&cYou cannot join the queue for {SERVER} because it is paused.");
        d.put("errors.deny-joining-from-server", "&cYou are not allowed to join queues from this server!");

        d.put("commands.leave-queue", "&aYou left the queue for {SERVER}!");
        d.put("commands.reload", "&aConfig and messages reloaded successfully!");
        d.put("commands.joinqueue.usage", "&cUsage: /joinqueue <server>");

        d.put("noperm", "&cYou do not have permission to do this!");

        d.put("format.time.mins", "{m}m {s}s");
        d.put("format.time.secs", "{s} seconds");

        d.put("list.format", "&b{SERVER} &7({COUNT}): {LIST}");
        d.put("list.playerlist", "&9{NAME}&7, ");
        d.put("list.total", "&7Total players in queues: &f{TOTAL}");
        d.put("list.none", "&7None");

        d.put("spigot.actionbar.online", "&7You are queued for &f{SERVER}&7. You are in position &f{POS}&7 of &f{LEN}&7. Estimated time: {TIME}");
        d.put("spigot.actionbar.offline", "&7You are queued for &f{SERVER}&7. &7You are in position &f{POS}&7 of &f{LEN}&7.");

        d.put("send", "&aAdded &f{PLAYER}&a to the queue for &f{SERVER}");
        d.put("remove", "&aRemoved &f{PLAYER} from all queues they were in.");

        d.put("placeholders.queued.none", "None");
        d.put("placeholders.position.none", "None");

        d.put("commands.leave.more-args", "&cPlease specify which queue you want to leave! &7You are in these queues: {QUEUES}");
        d.put("commands.leave.queues-list-format", "&f{NAME}&7, ");
        d.put("commands.leave.not-queued", "&cYou are not queued for that server! &7You are in these queues: {QUEUES}");
        d.put("commands.leave.no-queues", "&cYou are not queued!");

        d.put("commands.pause.more-args", "&cUsage: /ajqueue pause <server> [on/off]");
        d.put("commands.pause.no-server", "&cThat server does not exist!");
        d.put("commands.pause.success", "&aThe queue for &f{SERVER} &ais now {PAUSED}");
        d.put("commands.pause.paused.true", "&epaused");
        d.put("commands.pause.paused.false", "&aun-paused");

        d.put("commands.send.player-not-found", "&cThat player could not be found. Make sure they are online!");

        d.put("commands.listqueues.header", "&9Queues:");
        d.put("commands.listqueues.format", "<hover:show_text:'&7Status: {STATUS}'>{COLOR}{NAME}&7: {COUNT} queued</hover>");

        d.put("max-tries-reached", "&cUnable to connect to {SERVER}. Max retries reached.");
        d.put("auto-queued", "&aYou've been auto-queued for {SERVER} because you were kicked.");

        messages = new Messages(dataFolder, logger, d);
    }
}
