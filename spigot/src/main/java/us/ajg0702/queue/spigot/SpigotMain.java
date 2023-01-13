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
import us.ajg0702.utils.common.ConfigFile;

import java.io.File;
import java.util.HashMap;

@SuppressWarnings("UnstableApiUsage")
public class SpigotMain extends JavaPlugin implements PluginMessageListener,Listener {
	
	private boolean papi = false;
	private Placeholders placeholders;
	
	private ConfigFile config;

	private boolean hasProxy = false;
	
	@SuppressWarnings("ConstantConditions")
	public void onEnable() {
		getServer().getMessenger().registerIncomingPluginChannel(this, "ajqueue:tospigot", this);
		getServer().getMessenger().registerOutgoingPluginChannel(this, "ajqueue:toproxy");
		
		this.getCommand("move").setExecutor(new Commands(this));
		this.getCommand("leavequeue").setExecutor(new Commands(this));
		
		Bukkit.getPluginManager().registerEvents(this, this);
		
		papi = Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null;
		
		if(papi) {
			placeholders = new Placeholders(this);
			placeholders.register();
			getLogger().info("Registered PlaceholderAPI placeholders");
		}
		
		Bukkit.getScheduler().runTaskTimer(this, () -> {
			if(Bukkit.getOnlinePlayers().size() == 0 || queuebatch.size() == 0) return;
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
		}

	    if(subchannel.equals("inqueueevent")) {
	    	String playername = in.readUTF();
	    	Player p = Bukkit.getPlayer(playername);
	    	if(p == null) return;
	    	QueueScoreboardActivator e = new QueueScoreboardActivator(p);
	    	Bukkit.getPluginManager().callEvent(e);
	    }
	    if(subchannel.equals("queuename") && papi) {
	    	String playername = in.readUTF();
	    	Player p = Bukkit.getPlayer(playername);
	    	if(p == null) return;
	    	if(!p.isOnline()) return;
	    	
	    	String data = in.readUTF();
	    	HashMap<String, String> phs = placeholders.responseCache.get(p);
	    	if(phs == null) phs = new HashMap<>();
	    	phs.put("queued", data);
	    	placeholders.responseCache.put(p, phs);
	    }
	    if(subchannel.equals("position") && papi) {
	    	String playername = in.readUTF();
	    	Player p = Bukkit.getPlayer(playername);
	    	if(p == null) return;
	    	if(!p.isOnline()) return;
	    	
	    	String data = in.readUTF();
	    	HashMap<String, String> phs = placeholders.responseCache.get(p);
	    	if(phs == null) phs = new HashMap<>();
	    	phs.put("position", data);
	    	placeholders.responseCache.put(p, phs);
	    }
	    if(subchannel.equals("positionof") && papi) {
	    	String playername = in.readUTF();
	    	Player p = Bukkit.getPlayer(playername);
	    	if(p == null) return;
	    	if(!p.isOnline()) return;
	    	
	    	String data = in.readUTF();
	    	HashMap<String, String> phs = placeholders.responseCache.get(p);
	    	if(phs == null) phs = new HashMap<>();
	    	phs.put("of", data);
	    	placeholders.responseCache.put(p, phs);
	    }
	    if(subchannel.equals("inqueue") && papi) {
	    	String playername = in.readUTF();
	    	Player p = Bukkit.getPlayer(playername);
	    	if(p == null) return;
	    	if(!p.isOnline()) return;
	    	
	    	String data = in.readUTF();
	    	HashMap<String, String> phs = placeholders.responseCache.get(p);
	    	if(phs == null) phs = new HashMap<>();
	    	phs.put("inqueue", data);
	    	placeholders.responseCache.put(p, phs);
	    }
	    if(subchannel.equals("queuedfor")) {
	    	String playername = in.readUTF();
	    	String queuename = in.readUTF();
	    	
	    	Player p = Bukkit.getPlayer(playername);
	    	if(p == null) return;
	    	if(!p.isOnline()) return;
	    	
	    	int number = Integer.parseInt(in.readUTF());
	    	HashMap<String, String> phs = placeholders.responseCache.get(p);
	    	if(phs == null) phs = new HashMap<>();
	    	phs.put("queuedfor_"+queuename, number+"");
	    	placeholders.responseCache.put(p, phs);
	    }
		if(subchannel.equals("estimated_time")) {
			String playername = in.readUTF();

			Player p = Bukkit.getPlayer(playername);
			if(p == null) return;
			if(!p.isOnline()) return;

			String time = in.readUTF();
			HashMap<String, String> phs = placeholders.responseCache.get(p);
			if(phs == null) phs = new HashMap<>();
			phs.put("estimated_time", time);
			placeholders.responseCache.put(p, phs);
		}
		if(subchannel.equals("status")) {
			String playername = in.readUTF();
			String server = in.readUTF();

			Player p = Bukkit.getPlayer(playername);
			if(p == null) return;
			if(!p.isOnline()) return;

			String status = in.readUTF();
			HashMap<String, String> phs = placeholders.responseCache.get(p);
			if(phs == null) phs = new HashMap<>();
			phs.put("status_"+server, status+"");
			placeholders.responseCache.put(p, phs);
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
		placeholders.cleanCache();
	}

	@EventHandler
	public void onJoin(PlayerJoinEvent e) {
		if(hasProxy) return;
		Bukkit.getScheduler().runTask(this, () -> sendMessage(e.getPlayer(), "ack", ""));
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


	public ConfigFile getAConfig() {
		return config;
	}
}
