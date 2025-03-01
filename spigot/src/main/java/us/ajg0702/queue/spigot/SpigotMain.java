package us.ajg0702.queue.spigot;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.ServerListPingEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.jetbrains.annotations.NotNull;
import us.ajg0702.queue.api.AjQueueAPI;
import us.ajg0702.queue.api.communication.ComResponse;
import us.ajg0702.queue.api.spigot.AjQueueSpigotAPI;
import us.ajg0702.queue.spigot.api.SpigotAPI;
import us.ajg0702.queue.spigot.communication.ResponseManager;
import us.ajg0702.queue.spigot.placeholders.Placeholder;
import us.ajg0702.queue.spigot.placeholders.PlaceholderExpansion;
import us.ajg0702.queue.spigot.placeholders.RefetchablePlaceholder;
import us.ajg0702.utils.common.ConfigFile;
import us.ajg0702.utils.foliacompat.CompatScheduler;

import java.io.File;
import java.util.HashMap;
import java.util.UUID;
import java.util.logging.Level;

@SuppressWarnings("UnstableApiUsage")
public class SpigotMain extends JavaPlugin implements PluginMessageListener,Listener {
	
	private boolean papi = false;
	private PlaceholderExpansion placeholders;

	private ResponseManager responseManager = new ResponseManager();

	private final CompatScheduler compatScheduler = new CompatScheduler(this);
	
	private ConfigFile config;

	private boolean hasProxy = false;
	
	@SuppressWarnings("ConstantConditions")
	public void onEnable() {
		getServer().getMessenger().registerIncomingPluginChannel(this, "ajqueue:tospigot", this);
		getServer().getMessenger().registerOutgoingPluginChannel(this, "ajqueue:toproxy");

		AjQueueAPI.SPIGOT_INSTANCE = new SpigotAPI(responseManager, this);
		AjQueueSpigotAPI.INSTANCE = AjQueueAPI.SPIGOT_INSTANCE;
		
		this.getCommand("move").setExecutor(new Commands(this));
		this.getCommand("leavequeue").setExecutor(new Commands(this));
		
		Bukkit.getPluginManager().registerEvents(this, this);
		
		papi = Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null;
		
		if(papi) {
			placeholders = new PlaceholderExpansion(this);
			placeholders.register();
			getLogger().info("Registered PlaceholderAPI placeholders");
		}
		
		getScheduler().runTaskTimerAsynchronously(() -> {
			if(Bukkit.getOnlinePlayers().size() <= 0 || queuebatch.size() <= 0) return;
			StringBuilder msg = new StringBuilder();
			for(Player p : queuebatch.keySet()) {
				if(p == null || !p.isOnline()) continue;
				msg.append(p.getName()).append(":").append(queuebatch.get(p)).append(",");
			}
			if(msg.length() > 1) {
				msg = new StringBuilder(msg.substring(0, msg.length() - 1));
			}
			queuebatch.clear();
			sendMessage("massqueue", msg.toString());
		}, 2*20, 20);

		File oldConfig = new File(getDataFolder(), "config.yml");
		if(oldConfig.exists()) {
			//noinspection ResultOfMethodCallIgnored
			oldConfig.renameTo(new File(getDataFolder(), "spigot-config.yml"));
		}

		try {
			config = new ConfigFile(getDataFolder(), getLogger(), "spigot-config.yml");
		} catch (Exception e) {
			getLogger().severe("Unable to read config:");
			e.printStackTrace();
		}

		getLogger().info("Spigot side enabled! v"+getDescription().getVersion());
	}

	public boolean hasProxy() {
		return hasProxy;
	}

	final HashMap<Player, String> queuebatch = new HashMap<>();

	@Override
	public void onPluginMessageReceived(@NotNull String channel, @NotNull Player player, byte[] message) {
		if (!channel.equals("ajqueue:tospigot")) return;
		
		ByteArrayDataInput in = ByteStreams.newDataInput(message);
		
	    String subchannel = in.readUTF();

	    if(subchannel.equals("ack")) {
	    	hasProxy = true;
			return;
		}

	    if(subchannel.equals("inqueueevent")) {
	    	QueueScoreboardActivator e = new QueueScoreboardActivator(player);
	    	Bukkit.getPluginManager().callEvent(e);
			return;
	    }

		if(subchannel.equals("player-joined-queue")) {
			for (Placeholder placeholderImplementation : placeholders.getPlaceholderImplementations()) {
				if(!(placeholderImplementation instanceof RefetchablePlaceholder)) continue;
				RefetchablePlaceholder placeholder = (RefetchablePlaceholder) placeholderImplementation;
				placeholder.refetch(player);
			}
			return;
		}

		try {
			ComResponse response = ComResponse.from(subchannel, in);

			responseManager.executeResponse(response);
		} catch (IllegalStateException ignored) {
			// This seems to happen often when a player leaves. So, we'll ignore it.
		} catch (Exception e) {
			getLogger().log(Level.SEVERE, "Error while processing proxy response " + subchannel + ": ", e);
		}
	}
	
	
	public void sendMessage(Player player, String subchannel, String data) {
		ByteArrayDataOutput out = ByteStreams.newDataOutput();
		out.writeUTF(subchannel);
		out.writeUTF(data);
		
		player.sendPluginMessage(this, "ajqueue:toproxy", out.toByteArray());
	}
	
	public void sendMessage(String subchannel, String data) {
		ByteArrayDataOutput out = ByteStreams.newDataOutput();
		out.writeUTF(subchannel);
		out.writeUTF(data);
		
		Bukkit.getOnlinePlayers().iterator().next()
		.sendPluginMessage(this, "ajqueue:toproxy", out.toByteArray());
	}
	
	@EventHandler
	public void onLeave(PlayerQuitEvent e) {
		if(!papi) return;
		placeholders.cleanCache(e.getPlayer());
	}

	@EventHandler
	public void onJoin(PlayerJoinEvent e) {
		if(hasProxy || !config.getBoolean("check-proxy-response")) return;
		getScheduler().runTaskLaterAsynchronously(() -> sendMessage(e.getPlayer(), "ack", ""), 5);
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onServerPing(ServerListPingEvent e) {
		if(config == null) {
			getLogger().warning("Server ping before plugin load!");
			return;
		}
		if(!config.getBoolean("take-over-motd-for-whitelist")) return;
		if(!Bukkit.hasWhitelist()) return;

		StringBuilder whitelist = new StringBuilder();
		for(OfflinePlayer player : Bukkit.getWhitelistedPlayers()) {
			whitelist.append(player.getUniqueId()).append(",");
		}
		if(whitelist.length() > 1) {
			whitelist.deleteCharAt(whitelist.length()-1);
		}
		e.setMotd("ajQueue;whitelisted="+whitelist);
	}

	public CompatScheduler getScheduler() {
		return compatScheduler;
	}

	public ConfigFile getAConfig() {
		return config;
	}
}
