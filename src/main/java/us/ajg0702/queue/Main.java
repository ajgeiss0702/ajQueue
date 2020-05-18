package us.ajg0702.queue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import net.md_5.bungee.api.Callback;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.ServerPing;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.ServerSwitchEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.event.EventHandler;
import us.ajg0702.queue.utils.BungeeConfig;
import us.ajg0702.queue.utils.BungeeMessages;
import us.ajg0702.queue.utils.BungeeStats;
import us.ajg0702.queue.utils.BungeeUtils;

public class Main extends Plugin implements Listener {
	
	static Main plugin = null;
	
	int timeBetweenPlayers = 5;
	
	BungeeStats metrics;
	
	BungeeMessages msgs;
	
	BungeeConfig config;
	
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
		
		timeBetweenPlayers = config.getInt("wait-time");
		
		updateOnlineServers();
		
		try {
			Class.forName("us.ajg0702.queue.Logic");
			isp = true;
		} catch(ClassNotFoundException e) {
			isp = false;
		}
		
		getProxy().getScheduler().schedule(this, new Runnable() {
			public void run() {
				updateOnlineServers();
				sendPlayers();
			}
		}, 5, timeBetweenPlayers, TimeUnit.SECONDS);
		
		
		metrics = new BungeeStats(this, 7404);
		
		
	}
	
	public static Main getInstance() {
		return plugin;
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
	
	
	HashMap<String, Integer> offlineTime = new HashMap<>();
	boolean notif = false;
	public void sendPlayers(String sv) {
		for(String server : queues.keySet()) {
			if(sv != null && !server.equalsIgnoreCase(sv)) continue;
			List<ProxiedPlayer> plys = queues.get(server);
			if(plys.size() <= 0) continue;
			while(!plys.get(0).isConnected()) {
				plys.remove(0);
				if(plys.size() <= 0) break;
			}
			if(plys.size() <= 0) continue;
			ProxiedPlayer p = plys.get(0);
			if(!servers.get(server)) {
				int ot;
				if(!offlineTime.containsKey(server)) {
					ot = timeBetweenPlayers;
				} else {
					ot = offlineTime.get(server)+timeBetweenPlayers;
				}
				offlineTime.put(server, ot);
				for(ProxiedPlayer ply : plys) {
					int pos = plys.indexOf(ply)+1;
					if(pos == 0) {
						plys.remove(ply);
						continue;
					}
					int len = plys.size();
					String or = msgs.get("status.offline.restarting");
					if(ot > config.getInt("offline-time")) {
						or = msgs.get("status.offline.offline");
					} else {
						//ply.sendMessage(formatMessage(ot + " <= "+offlineSecs));
					}
					if(notif) {
						ply.sendMessage(formatMessage(
								msgs.get("status.offline.base")
								.replaceAll("\\{STATUS\\}", or)
								.replaceAll("\\{POS\\}", pos+"")
								.replaceAll("\\{LEN\\}", len+"")
								));
					}
					if(getConfig().getBoolean("send-actionbar")) {
						BungeeUtils.sendCustomData(ply, "actionbar", msgs.get("spigot.actionbar.offline")
								.replaceAll("\\{POS\\}", pos+"")
								.replaceAll("\\{LEN\\}", len+"")
								.replaceAll("\\{STATUS\\}", or)+";time="+timeBetweenPlayers);
					}
				}
				if(!notif) {
					notif = true;
				} else {
					notif = false;
				}
				continue;
			} else {
				offlineTime.put(server, 0);
				ServerInfo target = ProxyServer.getInstance().getServerInfo(server);
				p.connect(target);
				//plys.remove(p);
			}
			
			for(ProxiedPlayer ply : plys) {
				int pos = plys.indexOf(ply)+1;
				int len = plys.size();
				int time = pos*timeBetweenPlayers;
				int min = (int) Math.floor((time) / (60));
	        	int sec = (int) Math.floor((time % (60)));
	        	String timeStr;
	        	if(min <= 0) {
	        		timeStr = msgs.get("format.time.secs")
	        				.replaceAll("\\{m\\}", "0")
	        				.replaceAll("\\{s\\}", sec+"");
	        	} else {
	        		timeStr = msgs.get("format.time.mins")
	        				.replaceAll("\\{m\\}", min+"")
	        				.replaceAll("\\{s\\}", sec+"");
	        	}
				if(notif) {
					ply.sendMessage(formatMessage(
							msgs.get("status.online.base")
							.replaceAll("\\{POS\\}", pos+"")
							.replaceAll("\\{LEN\\}", len+"")
							.replaceAll("\\{TIME\\}", timeStr)
							));
				}
				if(getConfig().getBoolean("send-actionbar")) {
					BungeeUtils.sendCustomData(ply, "actionbar", msgs.get("spigot.actionbar.online")
							.replaceAll("\\{POS\\}", pos+"")
							.replaceAll("\\{LEN\\}", len+"")
							.replaceAll("\\{TIME\\}", timeStr)+";time="+timeBetweenPlayers);
				}
			}
			if(!notif) {
				notif = true;
			} else {
				notif = false;
			}
		}
		
	}
	public void sendPlayers() {
		sendPlayers(null);
	}
	
	
	@EventHandler
	public void moveServer(ServerSwitchEvent e) {
		ProxiedPlayer p = e.getPlayer();
		String queue = getPlayerInQueue(p);
		if(queue != null) {
			queues.get(queue).remove(p);
		}
		
		String servername = e.getPlayer().getServer().getInfo().getName();
		List<String> svs = config.getStringList("queue-servers");
		for(String s : svs) {
			if(!s.contains(":")) continue;
			String[] parts = s.split("\\:");
			String from = parts[0];
			String to = parts[1];
			if(from.equalsIgnoreCase(servername)) {
				addToQueue(p, to);
			}
		}
	}
	
	@EventHandler
	public void onLeave(PlayerDisconnectEvent e) {
		ProxiedPlayer p = e.getPlayer();
		String queue = getPlayerInQueue(p);
		if(queue != null) {
			queues.get(queue).remove(p);
		}
	}
	
	
	HashMap<String, Boolean> servers = new HashMap<>();
	public void updateOnlineServers() {
		for(final String server : getProxy().getServers().keySet()) {
			if(!servers.containsKey(server)) {
				servers.put(server, false);
			}
			getProxy().getServers().get(server).ping(new Callback<ServerPing>() {
		         
	            @Override
	            public void done(ServerPing result, Throwable error) {
	                servers.put(server, error == null);
	            }
	        });
		}
	}
	
	
	HashMap<String, List<ProxiedPlayer>> queues = new HashMap<>();
	public void addToQueue(ProxiedPlayer p, String server) {
		//getLogger().info("adding "+p.getDisplayName()+" to queue "+server);
		if(!servers.containsKey(server)) {
			p.sendMessage(msgs.getBC("errors.server-not-exist"));
			return;
		}
		if(!queues.containsKey(server)) {
			queues.put(server, new ArrayList<ProxiedPlayer>());
		}
		if(p.getServer().getInfo().getName().equals(server)) {
			p.sendMessage(msgs.getBC("errors.already-connected"));
			return;
		}
		String currentQueued = getPlayerInQueue(p);
		List<ProxiedPlayer> list = queues.get(server);
		if(list.indexOf(p) != -1) {
			int pos = list.indexOf(p)+1;
			int len = list.size();
			p.sendMessage(formatMessage(
					msgs.get("errors.already-queued")
					.replaceAll("\\{POS\\}", pos+"")
					.replaceAll("\\{LEN\\}", len+"")
					));
			return;
		}
		if(currentQueued != null) {
			queues.get(currentQueued).remove(p);
			p.sendMessage(msgs.getBC("status.left-last-queue"));
		}
		if(isp) {
			us.ajg0702.queue.Logic.priorityLogic(list, server, p);
		} else {
			if((p.hasPermission("ajqueue.priority") || p.hasPermission("ajqueue.serverpriority."+server)) && list.size() > 0) {
				int i = 0;
				for(ProxiedPlayer ply : list) {
					if(!(ply.hasPermission("ajqueue.priority") || ply.hasPermission("ajqueue.serverpriority."+server))) {
						list.add(i, p);
						break;
					}
					i++;
				}
				if(list.size() == 0) {
					list.add(p);
				}
			} else {
				list.add(p);
			}
		}
		int pos = list.indexOf(p)+1;
		int len = list.size();
		p.sendMessage(formatMessage(
				msgs.get("status.now-in-queue")
				.replaceAll("\\{POS\\}", pos+"")
				.replaceAll("\\{LEN\\}", len+"")
				));
		
		if(list.size() <= 1) {
			sendPlayers(server);
		}
	}
	
	
	public void sendPlayer(ProxiedPlayer p) {
		String server = getPlayerInQueue(p);
		if(server == null) return;
		sendPlayers(server);
	}
	
	
	public String getPlayerInQueue(ProxiedPlayer p) {
		for(String server : queues.keySet()) {
			if(queues.get(server).indexOf(p) != -1) {
				return server;
			}
		}
		return null;
	}
	
}
