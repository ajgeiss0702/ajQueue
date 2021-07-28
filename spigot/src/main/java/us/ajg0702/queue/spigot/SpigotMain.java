package us.ajg0702.queue.spigot;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.jetbrains.annotations.NotNull;
import us.ajg0702.queue.spigot.utils.VersionSupport;

import java.util.HashMap;

public class SpigotMain extends JavaPlugin implements PluginMessageListener,Listener {
	
	boolean papi = false;
	Placeholders placeholders;
	
	Config config;
	
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
		
		Bukkit.getScheduler().runTaskTimer(this, new Runnable() {
			public void run() {
				if(Bukkit.getOnlinePlayers().size() <= 0 || queuebatch.size() <= 0) return;
				String msg = "";
				for(Player p : queuebatch.keySet()) {
					if(p == null || !p.isOnline()) continue;
					msg += p.getName()+":"+queuebatch.get(p)+",";
				}
				if(msg.length() > 1) {
					msg = msg.substring(0, msg.length()-1);
				}
				queuebatch.clear();
				sendMessage("massqueue", msg);
			}
		}, 2*20, 20);
		
		config = new Config(this);
		
		getLogger().info("Spigot side enabled! v"+getDescription().getVersion());
	}
	
	HashMap<Player, String> queuebatch = new HashMap<>();

	@Override
	public void onPluginMessageReceived(@NotNull String channel, @NotNull Player player, @NotNull byte[] message) {
		if (!channel.equals("ajqueue:tospigot")) return;
		
		ByteArrayDataInput in = ByteStreams.newDataInput(message);
		
	    String subchannel = in.readUTF();

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
	    	
	    	int number = Integer.valueOf(in.readUTF());
	    	HashMap<String, String> phs = placeholders.responseCache.get(p);
	    	if(phs == null) phs = new HashMap<>();
	    	phs.put("queuedfor_"+queuename, number+"");
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
}
