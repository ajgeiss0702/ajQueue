package us.ajg0702.queue.common;

import org.spongepowered.configurate.ConfigurateException;
import us.ajg0702.queue.api.*;
import us.ajg0702.queue.api.premium.Logic;
import us.ajg0702.queue.api.premium.LogicGetter;
import us.ajg0702.queue.api.util.QueueLogger;
import us.ajg0702.queue.common.utils.LogConverter;
import us.ajg0702.queue.logic.LogicGetterImpl;
import us.ajg0702.utils.common.Config;
import us.ajg0702.utils.common.Messages;

import java.io.File;
import java.util.LinkedHashMap;

public class QueueMain extends AjQueueAPI {

    private static QueueMain instance;
    public static QueueMain getInstance() {
        return instance;
    }

    private double timeBetweenPlayers;
    @Override
    public double getTimeBetweenPlayers() {
        return timeBetweenPlayers;
    }
    @Override
    public void setTimeBetweenPlayers() {
        this.timeBetweenPlayers = config.getDouble("wait-time");
    }

    private Config config;
    @Override
    public Config getConfig() {
        return config;
    }

    private Messages messages;
    @Override
    public Messages getMessages() {
        return messages;
    }

    private AliasManager aliasManager;
    @Override
    public AliasManager getAliasManager() {
        return aliasManager;
    }

    private Logic logic;
    @Override
    public Logic getLogic() {
        return logic;
    }

    @Override
    public boolean isPremium() {
        return getLogic().isPremium();
    }

    private final PlatformMethods platformMethods;
    @Override
    public PlatformMethods getPlatformMethods() {
        return platformMethods;
    }

    private final QueueLogger logger;
    @Override
    public QueueLogger getLogger() {
        return logger;
    }

    private final TaskManager taskManager = new TaskManager(this);
    public TaskManager getTaskManager() {
        return taskManager;
    }

    private final EventHandler eventHandler = new EventHandlerImpl(this);
    @Override
    public EventHandler getEventHandler() {
        return eventHandler;
    }

    private QueueManager queueManager;
    @Override
    public QueueManager getQueueManager() {
        return queueManager;
    }

    private final LogicGetter logicGetter;
    @Override
    public LogicGetter getLogicGetter() {
        return logicGetter;
    }

    private ProtocolNameManager protocolNameManager;
    @Override
    public ProtocolNameManager getProtocolNameManager() {
        return protocolNameManager;
    }

    @Override
    public void shutdown() {
        taskManager.shutdown();
    }


    private final File dataFolder;


    public QueueMain(QueueLogger logger, PlatformMethods platformMethods, File dataFolder) {

        logicGetter = new LogicGetterImpl();

        if(instance != null) {
            try {
                throw new Exception("ajQueue QueueMain is being initialized when there is already one! Still initializing it, but this can cause issues.");
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
        instance = this;

        AjQueueAPI.INSTANCE = this;


        this.logger = logger;
        this.platformMethods = platformMethods;
        this.dataFolder = dataFolder;

        constructMessages();

        try {
            config = new Config(dataFolder, new LogConverter(logger));
        } catch (ConfigurateException e) {
            logger.warning("Unable to load config:");
            e.printStackTrace();
            return;
        }

        setTimeBetweenPlayers();

        queueManager = new QueueManagerImpl(this);

        logic = logicGetter.constructLogic();
        aliasManager = logicGetter.constructAliasManager(config);

        protocolNameManager = new ProtocolNameManagerImpl(config, platformMethods);

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
        d.put("status.offline.whitelisted", "whitelisted");

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
        d.put("errors.wrong-version.base", "<red>You must be on {VERSIONS} to join this server!");
        d.put("errors.wrong-version.or", " or ");
        d.put("errors.wrong-version.comma", ", ");

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

        d.put("title.title", "");
        d.put("title.subtitle", "<gold>You are <green>#{POS} <gold>in the queue!");

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

        d.put("velocity-kick-message", "<red>You were kicked while trying to join {SERVER}: <white>{REASON}");

        messages = new Messages(dataFolder, new LogConverter(logger), d);
    }
}
