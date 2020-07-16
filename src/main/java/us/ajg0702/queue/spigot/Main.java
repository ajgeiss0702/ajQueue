package us.ajg0702.queue.spigot;

import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

import us.ajg0702.queue.spigot.utils.VersionSupport;

public class Main extends JavaPlugin implements PluginMessageListener,Listener {
	
	boolean papi = false;
	Placeholders placeholders;
	public void onEnable() {
		getServer().getMessenger().registerIncomingPluginChannel(this, "ajqueue:tospigot", this);
		getServer().getMessenger().registerOutgoingPluginChannel(this, "ajqueue:tobungee");
		
		this.getCommand("move").setExecutor(new Commands(this));
		
		Bukkit.getPluginManager().registerEvents(this, this);
		
		papi = Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null;
		
		if(papi) {
			placeholders = new Placeholders(this);
			placeholders.register();
			getLogger().info("Registered PlaceholderAPI placeholders");
		}
		
		getLogger().info("Spigot side enabled! v"+getDescription().getVersion());
		/*Bukkit.getScheduler().runTaskTimer(this, new Runnable() {
			public void run() {
				sendMessage
			}
		}, 0, 2*20);*/
	}

	@Override
	public void onPluginMessageReceived(String channel, Player player, byte[] message) {
		if (!channel.equals("ajqueue:tospigot")) return;
		
		ByteArrayDataInput in = ByteStreams.newDataInput(message);
		
	    String subchannel = in.readUTF();
	    
	    if(subchannel.equals("actionbar")) {
	    	String playername = in.readUTF();
	    	Player p = Bukkit.getPlayer(playername);
	    	if(p == null) return;
	    	
	    	String data = in.readUTF();
	    	final String text = data.split(";time=")[0];
	    	//getLogger().info("recieved actionbar for "+player.getName()+": "+text);
	    	VersionSupport.sendActionBar(p, text);
	    	
	    	QueueActionbarUpdateEvent e = new QueueActionbarUpdateEvent(p);
	    	Bukkit.getPluginManager().callEvent(e);
	    	return;
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
	    	
	    	int number = in.readInt();
	    	HashMap<String, String> phs = placeholders.responseCache.get(p);
	    	if(phs == null) phs = new HashMap<>();
	    	phs.put("queuedfor_"+queuename, number+"");
	    	placeholders.responseCache.put(p, phs);
	    }
	}
	
	
	public void sendMessage(Player player, String subchannel, String data) {
		//getLogger().info("Sending message. "+subchannel+" "+data);
		ByteArrayDataOutput out = ByteStreams.newDataOutput();
		out.writeUTF(subchannel);
		out.writeUTF(data);
		
		player.sendPluginMessage(this, "ajqueue:tobungee", out.toByteArray());
	}
	
	@EventHandler
	public void onLeave(PlayerQuitEvent e) {
		if(!papi) return;
		placeholders.cleanCache();
	}
}
