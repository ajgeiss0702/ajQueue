package us.ajg0702.queue.common;

import org.spongepowered.configurate.ConfigurateException;
import us.ajg0702.queue.api.*;
import us.ajg0702.queue.api.events.Event;
import us.ajg0702.queue.api.events.utils.EventReceiver;
import us.ajg0702.queue.api.premium.Logic;
import us.ajg0702.queue.api.premium.LogicGetter;
import us.ajg0702.queue.api.queues.QueueServer;
import us.ajg0702.queue.api.util.QueueLogger;
import us.ajg0702.queue.common.utils.LogConverter;
import us.ajg0702.queue.logic.LogicGetterImpl;
import us.ajg0702.utils.common.Config;
import us.ajg0702.utils.common.Messages;
import us.ajg0702.utils.common.SimpleConfig;
import us.ajg0702.utils.common.UpdateManager;

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

    @Override
    public Map<String, List<String>> getQueueServers() {
        List<String> rawQueueServers = getConfig().getStringList("queue-servers");
        Map<String, List<String>> r = new HashMap<>();
        for(String rawQueueServer : rawQueueServers) {
            if(!rawQueueServer.contains(":")) continue;
            String[] parts = rawQueueServer.split(":");
            String fromName = parts[0];
            String toName = parts[1];
            QueueServer toServer = getQueueManager().findServer(toName);
            if(toServer == null) continue;

            List<String> existing = r.computeIfAbsent(fromName, key -> new ArrayList<>());
            existing.add(toName);
            r.put(fromName, existing);
        }
        return r;
    }

    private UpdateManager updateManager;
    public UpdateManager getUpdateManager() {
        return updateManager;
    }

    private SimpleConfig updaterConfig;
    public SimpleConfig getUpdaterConfig() {
        return updaterConfig;
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

        try {
            config = new Config(dataFolder, new LogConverter(logger));
        } catch (ConfigurateException e) {
            logger.warning("Unable to load config:");
            e.printStackTrace();
            return;
        }


        try {
            updaterConfig = new SimpleConfig(dataFolder, "updater-config.yml", new LogConverter(logger));
        } catch (ConfigurateException e) {
            logger.warning("Unable to load config:");
            e.printStackTrace();
            return;
        }

        constructMessages();

        getQueueHolderRegistry().register("default", DefaultQueueHolder.class);

        logic = logicGetter.constructLogic();
        aliasManager = logicGetter.constructAliasManager(config);

        slashServerManager = new SlashServerManager(this);


        //noinspection ResultOfMethodCallIgnored
        messages.getComponent("one").replaceText(b -> b.match(Pattern.compile("\\e")).replacement("a"));

        setTimeBetweenPlayers();

        queueManager = new QueueManagerImpl(this);

        protocolNameManager = new ProtocolNameManagerImpl(messages, platformMethods);

        taskManager.rescheduleTasks();

        String plugin = isPremium() ? "ajQueuePlus" : "ajQueue";
        updateManager = new UpdateManager(logger, platformMethods.getPluginVersion(), plugin, plugin, isPremium() ? updaterConfig.getString("updater-token") : null, dataFolder.getParentFile(), "ajQueue update");
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
        d.put("status.now-in-queue", "&aYou are now{EXPRESS}&aqueued for {SERVER}! &7You are in position &f{POS}&7 of &f{LEN}&7.\n&7Type &f/leavequeue&7 or &f<click:run_command:/leavequeue {SERVERNAME}>click here</click>&7 to leave the queue!");
        d.put("status.now-in-empty-queue", "");
        d.put("status.express", " &6express ");
        d.put("status.non-express", " ");

        d.put("status.sending-now", "&aSending you to &f{SERVER} &anow..");
        d.put("status.making-room", "<gold>Making room for you..");
        d.put("status.priority-increased", "<gold>You now have higher priority! <green>Moving you up in the queue..");
        d.put("status.skipping-queue-server", "");

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

        d.put("list.single-format", "&b{SERVER} &7({COUNT}): {LIST}");
        d.put("list.both-format", "&b{SERVER} &7({STANDARD_COUNT}+&6{EXPRESS_COUNT}&7): {LIST}");
        d.put("list.playerlist-format", "&9{EXPRESS_COLOR}{NAME}&7, ");
        d.put("list.playerlist-and-more", "&7and &f{MORE} &7more");
        d.put("list.total", "&7Total players in queues: &f{TOTAL}");
        d.put("list.none", "&7None");

        d.put("actionbar.online", "&7You are{EXPRESS}&7queued for &f{SERVER}&7. You are in position &f{POS}&7 of &f{LEN}&7. Estimated time: {TIME}");
        d.put("actionbar.offline", "&7You are{EXPRESS}&7queued for &f{SERVER}&7. &7You are in position &f{POS}&7 of &f{LEN}&7.");
        d.put("actionbar.express", " &6express ");
        d.put("actionbar.non-express", " ");


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
        d.put("commands.listqueues.single-format", "<hover:show_text:'&7Status: {STATUS}'>{COLOR}{NAME}&7: {COUNT} queued</hover>");
        d.put("commands.listqueues.both-format", "<hover:show_text:'&7Status: {STATUS}'>{COLOR}{NAME}&7: {STANDARD_COUNT}+&6{EXPRESS_COUNT} &7queued</hover>");

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
        d.put("updater.already-downloaded", "&aYou have already downloaded an update! &7Restart the server to apply it");
        d.put("updater.slow-feedback", "&7Checking for update and downloading...");
        d.put("updater.disabled", "&cThe updater is disabled! &7Please enable it in the config to download updates.");
        d.put("updater.warnings.could-not-delete-old-jar", "&aUpdate downloaded&e but the old jar could not be deleted. &7Please delete the old jar before restarting the server.");
        d.put("updater.errors.while-checking", "&eAn error occurred while checking for an update. &7See the console for more info.");
        d.put("updater.errors.unknown", "&eAn unknown error occurred: {ERROR}");
        d.put("updater.errors.could-not-find-jar", "&cCould not find the old jar!&7 Make sure it is named similar to ajQueue-x.x.x.jar");
        d.put("updater.errors.while-downloading", "&eAn error occurred while downloading an update. &7See the console for more info.");
        d.put("updater.errors.missing-update-token", "&cMissing update token! &7See the&f updater-config.yml&f file for more info.");
        d.put("updater.errors.invalid-update-token", "&cInvalid update token! &7See the&f updater-config.yml&f file for more info. If you are lost, please contact support.");
        d.put("updater.errors.uncaught", "&cAn error occurred while executing this command. &7See the console.");
        d.put("velocity-built-in-kick-messages.already-connecting", "Already connecting");
        d.put("velocity-built-in-kick-messages.already-connected", "Already connected");
        d.put("velocity-built-in-kick-messages.success", "Success");
        d.put("velocity-built-in-kick-messages.cancelled", "Connection canceled");
        d.put("velocity-built-in-kick-messages.disconnected", "Connection failed with unknown reason");

        List<String> oldProtocolNames = config.getStringList("protocol-names");
        for (String oldProtocolName : oldProtocolNames) {
            String[] parts = oldProtocolName.split(":");
            if(parts.length != 2) {
                logger.warn("Invalid old (in the config) protocol name '" + oldProtocolName + "'. Skipping.");
                continue;
            }
            String protocol = parts[0];
            String name = parts[1];

            d.put("protocol-names." + protocol, name);
        }


        d.putIfAbsent("protocol-names.5", "1.7.10");
        d.putIfAbsent("protocol-names.47", "1.8.9");
        d.putIfAbsent("protocol-names.107", "1.9");
        d.putIfAbsent("protocol-names.108", "1.9.1");
        d.putIfAbsent("protocol-names.109", "1.9.2");
        d.putIfAbsent("protocol-names.110", "1.9.4");
        d.putIfAbsent("protocol-names.210", "1.10.2");
        d.putIfAbsent("protocol-names.315", "1.11");
        d.putIfAbsent("protocol-names.316", "1.11.2");
        d.putIfAbsent("protocol-names.335", "1.12");
        d.putIfAbsent("protocol-names.338", "1.12.1");
        d.putIfAbsent("protocol-names.340", "1.12.2");
        d.putIfAbsent("protocol-names.393", "1.13");
        d.putIfAbsent("protocol-names.401", "1.13.1");
        d.putIfAbsent("protocol-names.404", "1.13.2");
        d.putIfAbsent("protocol-names.477", "1.14");
        d.putIfAbsent("protocol-names.480", "1.14.1");
        d.putIfAbsent("protocol-names.485", "1.14.2");
        d.putIfAbsent("protocol-names.490", "1.14.3");
        d.putIfAbsent("protocol-names.498", "1.14.4");
        d.putIfAbsent("protocol-names.573", "1.15");
        d.putIfAbsent("protocol-names.575", "1.15.1");
        d.putIfAbsent("protocol-names.578", "1.15.2");
        d.putIfAbsent("protocol-names.735", "1.16");
        d.putIfAbsent("protocol-names.736", "1.16.1");
        d.putIfAbsent("protocol-names.751", "1.16.2");
        d.putIfAbsent("protocol-names.753", "1.16.3");
        d.putIfAbsent("protocol-names.754", "1.16.5");
        d.putIfAbsent("protocol-names.755", "1.17");
        d.putIfAbsent("protocol-names.756", "1.17.1");
        d.putIfAbsent("protocol-names.757", "1.18.1");
        d.putIfAbsent("protocol-names.758", "1.18.2");
        d.putIfAbsent("protocol-names.759", "1.19");
        d.putIfAbsent("protocol-names.760", "1.19.2");
        d.putIfAbsent("protocol-names.761", "1.19.3");
        d.putIfAbsent("protocol-names.762", "1.19.4");
        d.putIfAbsent("protocol-names.763", "1.20.1");
        d.putIfAbsent("protocol-names.764", "1.20.2");
        d.putIfAbsent("protocol-names.765", "1.20.4");
        d.putIfAbsent("protocol-names.766", "1.20.6");
        d.putIfAbsent("protocol-names.767", "1.21.1");
        d.putIfAbsent("protocol-names.768", "1.21.3");
        d.putIfAbsent("protocol-names.769", "1.21.4");
        d.putIfAbsent("protocol-names.770", "1.21.5");
        d.putIfAbsent("protocol-names.771", "1.21.6");
        d.putIfAbsent("protocol-names.772", "1.21.8");

        messages = new Messages(dataFolder, new LogConverter(logger), d);
    }
}
