package us.ajg0702.queue.common;

import org.spongepowered.configurate.ConfigurateException;
import us.ajg0702.queue.api.*;
import us.ajg0702.queue.api.events.Event;
import us.ajg0702.queue.api.events.utils.EventReceiver;
import us.ajg0702.queue.api.premium.Logic;
import us.ajg0702.queue.api.premium.LogicGetter;
import us.ajg0702.queue.api.util.QueueLogger;
import us.ajg0702.queue.common.utils.LogConverter;
import us.ajg0702.queue.logic.LogicGetterImpl;
import us.ajg0702.utils.common.Config;
import us.ajg0702.utils.common.Messages;
import us.ajg0702.utils.common.Updater;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.regex.Pattern;

public class QueueMain extends AjQueueAPI {

    private static QueueMain instance;
    public static QueueMain getInstance() {
        return instance;
    }

    private double timeBetweenPlayers;

    protected ServerTimeManagerImpl serverTimeManager = new ServerTimeManagerImpl();

    @Override
    public ServerTimeManager getServerTimeManager() {
        return serverTimeManager;
    }

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

    private Updater updater;
    public Updater getUpdater() {
        return updater;
    }

    private final Implementation implementation;
    public Implementation getImplementation() {
        return implementation;
    }

    private SlashServerManager slashServerManager;
    public SlashServerManager getSlashServerManager() {
        return slashServerManager;
    }

    @Override
    public void shutdown() {
        taskManager.shutdown();
        updater.shutdown();
    }


    private final Map<Class<?>, ArrayList<EventReceiver<Event>>> listeners = new ConcurrentHashMap<>();

    @SuppressWarnings("unchecked")
    @Override
    public <E> void listen(Class<E> event, EventReceiver<E> handler) {
        if(!Arrays.asList(event.getInterfaces()).contains(Event.class)) {
            throw new IllegalArgumentException("You can only listen to ajQueue events!");
        }
        List<EventReceiver<Event>> existingList = listeners.computeIfAbsent(event, (k) -> new ArrayList<>());
        existingList.add((e) -> handler.execute((E) e));
    }

    public void call(Event event) {
        List<EventReceiver<Event>> list = listeners.computeIfAbsent(event.getClass(), (k) -> new ArrayList<>());
        list.forEach(eventReceiver -> {
            try {
                eventReceiver.execute(event);
            } catch(Exception e) {
                logger.severe("An external plugin threw an error while handling an event (this is probably not the fault of ajQueue!)", e);
            }
        });

    }

    @Override
    public ExecutorService getServersUpdateExecutor() {
        return taskManager.getServersUpdateExecutor();
    }


    private final File dataFolder;


    public QueueMain(Implementation implementation, QueueLogger logger, PlatformMethods platformMethods, File dataFolder) {
        this.implementation = implementation;

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

        logic = logicGetter.constructLogic();
        aliasManager = logicGetter.constructAliasManager(config);

        slashServerManager = new SlashServerManager(this);


        //noinspection ResultOfMethodCallIgnored
        messages.getComponent("one").replaceText(b -> b.match(Pattern.compile("\\e")).replacement("a"));

        setTimeBetweenPlayers();

        queueManager = new QueueManagerImpl(this);

        protocolNameManager = new ProtocolNameManagerImpl(config, platformMethods);

        taskManager.rescheduleTasks();

        updater = new Updater(logger, platformMethods.getPluginVersion(), isPremium() ? "ajQueuePlus" : "ajQueue", config.getBoolean("enable-updater"), isPremium() ? 79123 : 78266, dataFolder.getParentFile(), "ajQueue update");

    }

    private void constructMessages() {
        LinkedHashMap<String, Object> d = new LinkedHashMap<>();

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
        d.put("status.making-room", "<gold>Making room for you..");

        d.put("errors.server-not-exist", "&cThe server {SERVER} does not exist!");
        d.put("errors.already-queued", "&cYou are already queued for that server!");
        d.put("errors.player-only", "&cThis command can only be executed as a player!");
        d.put("errors.already-connected", "&cYou are already connected to this server!");
        d.put("errors.cant-join-paused", "&cYou cannot join the queue for {SERVER} because it is paused.");
        d.put("errors.deny-joining-from-server", "&cYou are not allowed to join queues from this server!");
        d.put("errors.wrong-version.base", "<red>You must be on {VERSIONS} to join this server!");
        d.put("errors.wrong-version.or", " or ");
        d.put("errors.wrong-version.comma", ", ");
        d.put("errors.too-fast-queue", "<red>You're queueing too fast!");
        d.put("errors.kicked-to-make-room", "<red>You were moved to the lobby to make room for another player.");
        d.put("errors.make-room-failed.player", "<red>Failed to make room for you in that server.");
        d.put("errors.make-room-failed.admin", "<red>Failed to make room for you in that server. Check the console for more information.");


        d.put("commands.leave-queue", "&aYou left the queue for {SERVER}!");
        d.put("commands.reload", "&aConfig and messages reloaded successfully!");
        d.put("commands.joinqueue.usage", "&cUsage: /joinqueue <server>");
        d.put("commands.kick.usage", "<red>Usage: /ajqueue kick <player> [queue]");
        d.put("commands.kick.no-player", "&cCould not find {PLAYER}! Make sure they are in a queue!");
        d.put("commands.kick.unknown-server", "&cCould not find queue {QUEUE}. Make sure you spelled it correctly!");
        d.put("commands.kick.success", "<green>Kicked <white>{PLAYER} <green>from {NUM} queue{s}!");
        d.put("commands.kickall.usage", "<red>Usage: /ajqueue kickall <queue>");
        d.put("commands.kickall.success", "<green>Kicked <white>{NUM} <green>player{s} from <white>{SERVER}<green>!");
        d.put("commands.pausequeueserver.unpaused", "<green>You are no longer paused! <gray>You can now use queue-servers normally.");
        d.put("commands.pausequeueserver.paused", "<green>You are now paused! <gray>You will no longer be sent using queue-servers.");
        d.put("commands.pausequeueserver.reminder", "<gold>Reminder: <yellow>You are currently paused for queue-servers, so you will not be sent using them!<gray> Use <white>/ajQueue pausequeueserver</white> to un-pause and return to normal behaviour");

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
        d.put("placeholders.estimated_time.none", "None");

        d.put("placeholders.status.online", "&aOnline");
        d.put("placeholders.status.offline", "&cOffline");
        d.put("placeholders.status.restarting", "&cRestarting");
        d.put("placeholders.status.full", "&eFull");
        d.put("placeholders.status.restricted", "&eRestricted");
        d.put("placeholders.status.paused", "&ePaused");
        d.put("placeholders.status.whitelisted", "&eWhitelisted");

        d.put("title.title", "");
        d.put("title.subtitle", "<gold>You are <green>#{POS} <gold>in the queue!");
        d.put("title.sending-now.title", "");
        d.put("title.sending-now.subtitle", "<green>Sending you to <white>{SERVER} <green>now..");

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
        d.put("commands.send.usage", "<red>Usage: /ajqueue send <player> <server>");

        d.put("commands.listqueues.header", "&9Queues:");
        d.put("commands.listqueues.format", "<hover:show_text:'&7Status: {STATUS}'>{COLOR}{NAME}&7: {COUNT} queued</hover>");

        d.put("max-tries-reached", "&cUnable to connect to {SERVER}. Max retries reached.");
        d.put("auto-queued", "&aYou've been auto-queued for {SERVER} because you were kicked.");

        d.put("velocity-kick-message", "<red>You were kicked while trying to join {SERVER}: <white>{REASON}");

        d.put("updater.update-available",
                "<gray><strikethrough>                                                         <reset>\n" +
                        "  <green>An update is available for ajQueue!\n" +
                        "  <dark_green>You can download it by " +
                        "<click:run_command:/ajqueue update><bold>clicking here</bold>\n    or running <gray>/ajQueue update</click>\n" +
                        "<gray><strikethrough>                                                         <reset>"
        );
        d.put("updater.no-update", "<red>There is not an update available");
        d.put("updater.success", "<green>The update has been downloaded! Now just restart the server");
        d.put("updater.fail", "<red>An error occurred while downloading the update. Check the console for more info.");
        d.put("updater.already-downloaded", "<red>The update has already been downloaded.");

        messages = new Messages(dataFolder, new LogConverter(logger), d);
    }
}
