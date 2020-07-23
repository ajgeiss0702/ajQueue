package us.ajg0702.queue;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import us.ajg0702.utils.bungee.BungeeMessages;
import us.ajg0702.utils.bungee.BungeeUtils;

public class Manager {
	
	static Manager INSTANCE = null;
	public static Manager getInstance(Main pl) {
		if(INSTANCE == null) {
			INSTANCE = new Manager(pl);
		}
		return INSTANCE;
	}
	public static Manager getInstance() {
		return INSTANCE;
	}
	
	BungeeMessages msgs;
	
	
	Main pl;
	private Manager(Main pl) {
		this.pl = pl;
		msgs = BungeeMessages.getInstance();
		reloadIntervals();
		if(!pl.config.getBoolean("wait-to-load-servers")) {
			reloadServers();
		} else {
			pl.getProxy().getScheduler().schedule(pl, new Runnable() {
				public void run() {
					reloadServers();
				}
			}, pl.config.getInt("wait-to-load-servers-delay"), TimeUnit.MILLISECONDS);
		}
	}
	
	/*
	 * Returns all servers
	 */
	public List<Server> getServers() {
		return servers;
	}
	
	/**
	 * Returns the name of all servers
	 * @return The names of all servers
	 */
	public List<String> getServerNames() {
		List<String> names = new ArrayList<>();
		for(Server s : servers) {
			names.add(s.getName());
		}
		return names;
	}
	
	
	
	int sendId = -1;
	int updateId = -1;
	int messagerId = -1;
	int actionbarId = -1;
	int srvRefId = -1;
	/**
	 * Clears all intervals and re-makes them
	 */
	public void reloadIntervals() {
		if(sendId != -1) {
			pl.getProxy().getScheduler().cancel(sendId);
		}
		if(updateId != -1) {
			pl.getProxy().getScheduler().cancel(updateId);
		}
		if(messagerId != -1) {
			pl.getProxy().getScheduler().cancel(messagerId);
		}
		if(actionbarId != -1) {
			pl.getProxy().getScheduler().cancel(actionbarId);
		}
		if(srvRefId != -1) {
			pl.getProxy().getScheduler().cancel(srvRefId);
		}
		
		sendId = pl.getProxy().getScheduler().schedule(pl, new Runnable() {
			public void run() {
				sendPlayers();
			}
		}, 2, Math.round(pl.timeBetweenPlayers*1000), TimeUnit.MILLISECONDS).getId();
		
		updateId = pl.getProxy().getScheduler().schedule(pl, new Runnable() {
			public void run() {
				updateServers();
			}
		}, 0, Math.max(Math.round(pl.timeBetweenPlayers), 2), TimeUnit.SECONDS).getId();
		//pl.getLogger().info("Time: "+pl.timeBetweenPlayers);
		
		messagerId = pl.getProxy().getScheduler().schedule(pl, new Runnable() {
			public void run() {
				sendMessages();
			}
		}, 0, pl.getConfig().getInt("message-time"), TimeUnit.SECONDS).getId();
		actionbarId = pl.getProxy().getScheduler().schedule(pl, new Runnable() {
			public void run() {
				sendActionBars();
			}
		}, 0, 2, TimeUnit.SECONDS).getId();
		
		if(pl.config.getInt("reload-servers-interval") > 0) {
			srvRefId = pl.getProxy().getScheduler().schedule(pl, new Runnable() {
				public void run() {
					updateServers();
				}
			}, pl.config.getInt("reload-servers-interval"),  pl.config.getInt("reload-servers-interval"), TimeUnit.SECONDS).getId();
		}
	}
	
	/**
	 * Get the name of the server the player is queued for.
	 * If multiple servers are queued for, it will use the multi-server-queue-pick option in the config
	 * @param p The player
	 * @return The name of the server, the placeholder none message if not queued
	 */
	public String getQueuedName(ProxiedPlayer p) {
		List<Server> queued = findPlayerInQueue(p);
		if(queued.size() <= 0) {
			return msgs.get("placeholders.queued.none");
		}
		Server selected = queued.get(0);
		
		if(pl.config.getString("multi-server-queue-pick").equalsIgnoreCase("last")) {
			selected = queued.get(queued.size()-1);
		}
		
		return selected.getName();
	}
	
	/**
	 * Get a single server the player is queued for. Depends on the multi-server-queue-pick option in the config
	 * @param p The player
	 * @return The server that was chosen that the player is queued for.
	 */
	public Server getSingleServer(ProxiedPlayer p) {
		List<Server> queued = findPlayerInQueue(p);
		if(queued.size() <= 0) {
			return null;
		}
		Server selected = queued.get(0);
		
		if(pl.config.getString("multi-server-queue-pick").equalsIgnoreCase("last")) {
			selected = queued.get(queued.size()-1);
		}
		return selected;
	}
	
	
	
	
	List<Server> servers = new ArrayList<>();
	/**
	 * Checks servers that are in bungeecord and adds any it doesnt
	 * know about.
	 */
	public void reloadServers() {
		Map<String, ServerInfo> svs = ProxyServer.getInstance().getServers();
		for(String name : svs.keySet()) {
			if(findServer(name) != null) continue;
			ServerInfo info = svs.get(name);
			servers.add(new Server(name, info));
		}
	}
	
	/**
	 * Sends actionbar updates to all players in all queues with their
	 * position in the queue and time remaining
	 */
	public void sendActionBars() {
		if(!pl.getConfig().getBoolean("send-actionbar")) return;
		
		for(ProxiedPlayer p : ProxyServer.getInstance().getPlayers()) {
			Server s = this.getSingleServer(p);
			
			if(s == null) continue;
			List<ProxiedPlayer> plys = s.getQueue();
			int pos = plys.indexOf(p)+1;
			if(pos == 0) {
				plys.remove(p);
				continue;
			}
			
			int len = plys.size();
			if(!s.isJoinable(p)) {
				
				String status = "unknown";
				
				
				if(!s.canAccess(p)) {
					status = msgs.get("status.offline.restricted");
				}
				
				if(s.isFull()) {
					status = msgs.get("status.offline.full");
				}
				
				if(s.isPaused()) {
					status = msgs.get("status.offline.paused");
				}
				
				if(!s.isOnline()) {
					status = msgs.get("status.offline.restarting");
				}
				
				if(s.getOfflineTime() > pl.config.getInt("offline-time")) {
					status = msgs.get("status.offline.offline");
				}
				
				
				BungeeUtils.sendCustomData(p, "actionbar", msgs.get("spigot.actionbar.offline")
						.replaceAll("\\{POS\\}", pos+"")
						.replaceAll("\\{LEN\\}", len+"")
						.replaceAll("\\{SERVER\\}", pl.aliases.getAlias(s.getName()))
						.replaceAll("\\{STATUS\\}", status)+";time="+pl.timeBetweenPlayers);
			} else {
				int time = (int) Math.round(pos*pl.timeBetweenPlayers);
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
				BungeeUtils.sendCustomData(p, "actionbar", msgs.get("spigot.actionbar.online")
						.replaceAll("\\{POS\\}", pos+"")
						.replaceAll("\\{LEN\\}", len+"")
						.replaceAll("\\{SERVER\\}", pl.aliases.getAlias(s.getName()))
						.replaceAll("\\{TIME\\}", timeStr)+";time="+pl.timeBetweenPlayers);
			}
		}
		
		/*for(Server s : servers) {
			int ot = s.getOfflineTime();
			List<ProxiedPlayer> plys = s.getQueue();
			Iterator<ProxiedPlayer> it = plys.iterator();
			while(it.hasNext()) {
				ProxiedPlayer ply = it.next();
				int pos = plys.indexOf(ply)+1;
				if(pos == 0) {
					plys.remove(ply);
					continue;
				}
				
				int len = plys.size();
				if(!s.isOnline() || s.isFull() || !s.canAccess(ply)) {
					
					String status = msgs.get("status.offline.restarting");
					
					if(ot > pl.config.getInt("offline-time")) {
						status = msgs.get("status.offline.offline");
					}
					
					if(!s.canAccess(ply)) {
						status = msgs.get("status.offline.restricted");
					}
					
					
					BungeeUtils.sendCustomData(ply, "actionbar", msgs.get("spigot.actionbar.offline")
							.replaceAll("\\{POS\\}", pos+"")
							.replaceAll("\\{LEN\\}", len+"")
							.replaceAll("\\{SERVER\\}", s.getName())
							.replaceAll("\\{STATUS\\}", status)+";time="+pl.timeBetweenPlayers);
				} else {
					int time = pos*pl.timeBetweenPlayers;
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
					BungeeUtils.sendCustomData(ply, "actionbar", msgs.get("spigot.actionbar.online")
							.replaceAll("\\{POS\\}", pos+"")
							.replaceAll("\\{LEN\\}", len+"")
							.replaceAll("\\{SERVER\\}", s.getName())
							.replaceAll("\\{TIME\\}", timeStr)+";time="+pl.timeBetweenPlayers);
				}
			}
		}*/
	}
	
	/**
	 * Sends the message to the player updating them on their position in the queue
	 * along with their time remaining
	 */
	public void sendMessages() {
		for(Server s : servers) {
			int ot = s.getOfflineTime();
			List<ProxiedPlayer> plys = s.getQueue();
			for(ProxiedPlayer ply : plys) {
				int pos = plys.indexOf(ply)+1;
				if(pos == 0) {
					plys.remove(ply);
					continue;
				}
				int len = plys.size();
				if(!s.isJoinable(ply)) {
					
					String status = msgs.get("status.offline.restarting");
					
					if(ot > pl.config.getInt("offline-time")) {
						status = msgs.get("status.offline.offline");
					}
					
					if(s.isFull() && s.isOnline()) {
						status = msgs.get("status.offline.full");
					}
					
					if(!s.canAccess(ply)) {
						status = msgs.get("status.offline.restricted");
					}
					
					if(s.isPaused()) {
						status = msgs.get("status.offline.paused");
					}
					
					ply.sendMessage(Main.formatMessage(
							msgs.get("status.offline.base")
							.replaceAll("\\{STATUS\\}", status)
							.replaceAll("\\{POS\\}", pos+"")
							.replaceAll("\\{LEN\\}", len+"")
							.replaceAll("\\{SERVER\\}", pl.aliases.getAlias(s.getName()))
						));
				} else {
					int time = (int) Math.round(pos*pl.timeBetweenPlayers);
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
					ply.sendMessage(Main.formatMessage(
							msgs.get("status.online.base")
							.replaceAll("\\{POS\\}", pos+"")
							.replaceAll("\\{LEN\\}", len+"")
							.replaceAll("\\{TIME\\}", timeStr)
							.replaceAll("\\{SERVER\\}", pl.aliases.getAlias(s.getName()))
							));
				}
				
			}
		}
	}
	
	/**
	 * Find a server by name
	 * @param name Name of the server
	 * @return The server if it exists (otherwise null)
	 */
	public Server findServer(String name) {
		for(Server server : servers) {
			if(server.getName().equals(name)) {
				return server;
			}
		}
		return null;
	}
	
	/**
	 * Updates info about servers.
	 */
	public void updateServers() {
		Iterator<Server> it = servers.iterator();
		while(it.hasNext()) {
			it.next().update();
		}
	}
	
	/**
	 * Attempts to send the first player in all queues
	 */
	public void sendPlayers() {
		sendPlayers(null);
	}
	/**
	 * Attempts to send the first player in this queue
	 * @param server The server to send the first player in the queue. null for all servers.
	 */
	public void sendPlayers(String server) {
		for(Server s : servers) {
			String name = s.getName();
			if(server != null && !server.equals(name)) continue;
			if(!s.isOnline()) continue;
			if(s.isPaused()) continue;
			if(s.getQueue().size() <= 0) continue;
			
			if(pl.config.getBoolean("send-all-when-back-online") && s.justWentOnline() && s.isOnline()) {
				for(ProxiedPlayer p : s.getQueue()) {
					if(s.isFull() && !p.hasPermission("ajqueue.joinfull")) break;
					p.sendMessage(msgs.getBC("status.sending-now", "SERVER:"+pl.aliases.getAlias(name)));
					p.connect(s.getInfo());
				}
				return;
			}
			
			ProxiedPlayer nextplayer = s.getQueue().get(0);
			
			if(!s.canAccess(nextplayer)) continue;
			
			while(nextplayer.getServer().getInfo().getName().equals(s.getName())) {
				s.getQueue().remove(nextplayer);
				if(s.getQueue().size() <= 0) break;
				nextplayer = s.getQueue().get(0);
			}
			if(s.getQueue().size() <= 0) continue;
			while(!nextplayer.isConnected()) {
				s.getQueue().remove(nextplayer);
				if(s.getQueue().size() <= 0) break;
				nextplayer = s.getQueue().get(0);
			}
			if(s.getQueue().size() <= 0) continue;
			if(s.isFull() && !nextplayer.hasPermission("ajqueue.joinfull")) continue;
			
			nextplayer.sendMessage(Main.formatMessage(msgs.get("status.sending-now").replaceAll("\\{SERVER\\}", pl.aliases.getAlias(name))));
			nextplayer.connect(s.getInfo());
		}
	}
	
	/**
	 * Add a player to the queue for a server
	 * @param p The player
	 * @param s The name of the server
	 */
	public void addToQueue(ProxiedPlayer p, String s) {
		Server server = findServer(s);
		if(server == null) {
			p.sendMessage(msgs.getBC("errors.server-not-exist"));
			return;
		}
		
		if(pl.config.getBoolean("joinfrom-server-permission") && !p.hasPermission("ajqueue.joinfrom."+p.getServer().getInfo().getName())) {
			p.sendMessage(msgs.getBC("errors.deny-joining-from-server"));
			return;
		}
		
		if(server.isPaused() && pl.config.getBoolean("prevent-joining-paused")) {
			p.sendMessage(msgs.getBC("errors.cant-join-paused", "SERVER:"+pl.aliases.getAlias(server.getName())));
			return;
		}
		
		if(p.getServer().getInfo().getName().equals(s)) {
			p.sendMessage(msgs.getBC("errors.already-connected"));
			return;
		}
		
		List<Server> beforeQueues = findPlayerInQueue(p);
		if(beforeQueues.size() > 0) {
			if(beforeQueues.contains(server)) {
				p.sendMessage(msgs.getBC("errors.already-queued"));
				return;
			}
			if(!pl.config.getBoolean("allow-multiple-queues")) {
				p.sendMessage(msgs.getBC("status.left-last-queue"));
				for(Server ser : beforeQueues) {
					ser.getQueue().remove(p);
				}
			}
		}
		
		List<ProxiedPlayer> list = server.getQueue();
		if(list.indexOf(p) != -1) {
			int pos = list.indexOf(p)+1;
			int len = list.size();
			p.sendMessage(Main.formatMessage(
					msgs.get("errors.already-queued")
					.replaceAll("\\{POS\\}", pos+"")
					.replaceAll("\\{LEN\\}", len+"")
					));
			return;
		}
		if(pl.isp) {
			us.ajg0702.queue.Logic.priorityLogic(server.getQueue(), s, p);
		} else {
			if((p.hasPermission("ajqueue.priority") || p.hasPermission("ajqueue.serverpriority."+s)) && list.size() > 0) {
				int i = 0;
				for(ProxiedPlayer ply : list) {
					if(!(ply.hasPermission("ajqueue.priority") || ply.hasPermission("ajqueue.serverpriority."+s))) {
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
		if(list.size() <= 1 && server.isOnline() && server.canAccess(p) && !server.isFull() && !server.isWhitelisted()) {
			sendPlayers(s);
			BaseComponent[] m = msgs.getBC("status.now-in-empty-queue", "POS:"+pos, "LEN:"+len, "SERVER:"+pl.aliases.getAlias(s));
			if(TextComponent.toPlainText(m).length() > 0) {
				p.sendMessage(m);
			}
		} else {
			p.sendMessage(Main.formatMessage(
					msgs.get("status.now-in-queue")
					.replaceAll("\\{POS\\}", pos+"")
					.replaceAll("\\{LEN\\}", len+"")
					.replaceAll("\\{SERVER\\}", pl.aliases.getAlias(s))
					));
		}
		
		BungeeUtils.sendCustomData(p, "position", pos+"");
		BungeeUtils.sendCustomData(p, "positionof", len+"");
		BungeeUtils.sendCustomData(p, "queuename", pl.aliases.getAlias(s));
		BungeeUtils.sendCustomData(p, "inqueue", "true");
	}
	
	/**
	 * Finds which servers the player is queued for
	 * @param p The player to search for
	 * @return The servers the player is queued for.
	 */
	public List<Server> findPlayerInQueue(ProxiedPlayer p) {
		List<Server> srs = new ArrayList<>();
		for(Server s : servers) {
			if(s.getQueue().contains(p)) {
				srs.add(s);
			}
		}
		return srs;
	}
	
	public Server getServer(String name) {
		return findServer(name);
	}
}
