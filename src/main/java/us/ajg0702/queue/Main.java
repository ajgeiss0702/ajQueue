package us.ajg0702.queue;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.Callable;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.event.ServerSwitchEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.event.EventHandler;
import us.ajg0702.queue.commands.LeaveCommand;
import us.ajg0702.queue.commands.ManageCommand;
import us.ajg0702.queue.commands.MoveCommand;
import us.ajg0702.queue.utils.BungeeConfig;
import us.ajg0702.queue.utils.BungeeMessages;
import us.ajg0702.queue.utils.BungeeStats;
import us.ajg0702.queue.utils.BungeeUtils;

public class Main extends Plugin implements Listener {
	
	static Main plugin = null;
	
	public int timeBetweenPlayers = 5;
	
	BungeeStats metrics;
	
	BungeeMessages msgs;
	
	BungeeConfig config;
	
	Manager man;
	
	boolean isp;
	
	@Override
	public void onEnable() {
		plugin = this;
		
		msgs = BungeeMessages.getInstance(this);
		
		config = new BungeeConfig(this);
		checkConfig();
		
		this.getProxy().getPluginManager().registerCommand(this, new MoveCommand(this));
		this.getProxy().getPluginManager().registerCommand(this, new ManageCommand(this));
		this.getProxy().getPluginManager().registerCommand(this, new LeaveCommand(this));
		
		this.getProxy().getPluginManager().registerListener(this, this);
		
		getProxy().registerChannel("ajqueue:tospigot");
		getProxy().registerChannel("ajqueue:tobungee");
		
		timeBetweenPlayers = config.getInt("wait-time");
		
		try {
			Class.forName("us.ajg0702.queue.Logic");
			isp = true;
		} catch(ClassNotFoundException e) {
			isp = false;
		}
		
		man = Manager.getInstance(this);
		
		
		metrics = new BungeeStats(this, 7404);
		metrics.addCustomChart(new BungeeStats.SimplePie("premium", new Callable<String>() {
	        @Override
	        public String call() throws Exception {
	            return isp+"";
	        }
	    }));
		
	}
	
	public boolean isp() {
		return isp;
	}
	
	
	
	public void checkConfig() {
		List<String> svs = getConfig().getStringList("queue-servers");
		for(String s : svs) {
			if(!s.contains(":")) {
				getLogger().warning("The queue-servers section in the config has been set up incorrectly! Please read the comment above the setting and make sure you have a queue server and a destination server separated by a colon (:)");
				break;
			}
		}
	}
	
	public BungeeConfig getConfig() {
		return config;
	}
	
	public static BaseComponent[] formatMessage(String text) {
		return TextComponent.fromLegacyText(net.md_5.bungee.api.ChatColor.translateAlternateColorCodes('&', text));
	}
	
	
	@EventHandler
	public void moveServer(ServerSwitchEvent e) {
		ProxiedPlayer p = e.getPlayer();
		Server alreadyqueued = man.findPlayerInQueue(p);
		if(alreadyqueued != null) {
			alreadyqueued.getQueue().remove(p);
		}
		
		String servername = e.getPlayer().getServer().getInfo().getName();
		List<String> svs = config.getStringList("queue-servers");
		for(String s : svs) {
			if(!s.contains(":")) continue;
			String[] parts = s.split("\\:");
			String from = parts[0];
			String to = parts[1];
			if(from.equalsIgnoreCase(servername)) {
				man.addToQueue(p, to);
			}
		}
	}
	
	@EventHandler
	public void onLeave(PlayerDisconnectEvent e) {
		ProxiedPlayer p = e.getPlayer();
		Server server = man.findPlayerInQueue(p);
		if(server != null) {
			server.getQueue().remove(p);
		}
	}
	
	
	@EventHandler
	public void onMessage(PluginMessageEvent e) {
		//getLogger().info("Recieved message of "+e.getTag());
		if(!e.getTag().equals("ajqueue:tobungee")) return;
		DataInputStream in = new DataInputStream(new ByteArrayInputStream(e.getData()));
		try {
			String subchannel = in.readUTF();
			ProxiedPlayer player = (ProxiedPlayer) e.getReceiver();
			
			
			if(subchannel.equals("queue")) {
				String data = in.readUTF();
				man.addToQueue(player, data);
			}
			if(subchannel.equals("queuename")) {
				BungeeUtils.sendCustomData(player, "queuename", man.getQueuedName(player));
			}
			if(subchannel.equals("position")) {
				Server server = man.findPlayerInQueue(player);
				String pos = msgs.get("placeholders.position.none");
				if(server != null) {
					pos = server.getQueue().indexOf(player)+1+"";
				}
				BungeeUtils.sendCustomData(player, "position", pos);
			}
			if(subchannel.equals("positionof")) {
				Server server = man.findPlayerInQueue(player);
				String pos = msgs.get("placeholders.position.none");
				if(server != null) {
					pos = server.getQueue().size()+"";
				}
				BungeeUtils.sendCustomData(player, "positionof", pos);
			}
			
		} catch (IOException e1) {
			getLogger().warning("An error occured while reading data from spigot side:");
			e1.printStackTrace();
		}
	}
	
}
