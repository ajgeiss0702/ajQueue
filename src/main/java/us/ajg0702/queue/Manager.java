package us.ajg0702.queue;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import us.ajg0702.queue.utils.BungeeMessages;
import us.ajg0702.queue.utils.BungeeUtils;

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
		reloadServers();
		reloadIntervals();
	}
	
	/*
	 * Returns all servers
	 */
	public List<Server> getServers() {
		return servers;
	}
	
	
	
	int sendId = -1;
	int updateId = -1;
	int messagerId = -1;
	int actionbarId = -1;
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
		
		sendId = pl.getProxy().getScheduler().schedule(pl, new Runnable() {
			public void run() {
				sendPlayers();
			}
		}, 2, pl.timeBetweenPlayers, TimeUnit.SECONDS).getId();
		
		updateId = pl.getProxy().getScheduler().schedule(pl, new Runnable() {
			public void run() {
				updateServers();
			}
		}, 0, Math.max(pl.timeBetweenPlayers, 2), TimeUnit.SECONDS).getId();
		
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
	}
	
	/**
	 * Get the name of the server the player is queued for
	 * @param p The player
	 * @return The name of the server, the placeholder none message if not queued
	 */
	public String getQueuedName(ProxiedPlayer p) {
		Server queued = findPlayerInQueue(p);
		if(queued == null) {
			return msgs.get("placeholders.queued.none");
		}
		return queued.getName();
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
		for(Server s : servers) {
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
				if(!s.isOnline() || s.isFull()) {
					String or = msgs.get("status.offline.restarting");
					if(ot > pl.config.getInt("offline-time")) {
						or = msgs.get("status.offline.offline");
					}
					BungeeUtils.sendCustomData(ply, "actionbar", msgs.get("spigot.actionbar.offline")
							.replaceAll("\\{POS\\}", pos+"")
							.replaceAll("\\{LEN\\}", len+"")
							.replaceAll("\\{SERVER\\}", s.getName())
							.replaceAll("\\{STATUS\\}", or)+";time="+pl.timeBetweenPlayers);
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
		}
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
				if(!s.isOnline() || s.isFull()) {
					String or = msgs.get("status.offline.restarting");
					if(ot > pl.config.getInt("offline-time")) {
						or = msgs.get("status.offline.offline");
					}
					if(s.isFull() && s.isOnline()) {
						or = msgs.get("status.offline.full");
					}
					ply.sendMessage(Main.formatMessage(
							msgs.get("status.offline.base")
							.replaceAll("\\{STATUS\\}", or)
							.replaceAll("\\{POS\\}", pos+"")
							.replaceAll("\\{LEN\\}", len+"")
							.replaceAll("\\{SERVER\\}", s.getName())
						));
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
					ply.sendMessage(Main.formatMessage(
							msgs.get("status.online.base")
							.replaceAll("\\{POS\\}", pos+"")
							.replaceAll("\\{LEN\\}", len+"")
							.replaceAll("\\{TIME\\}", timeStr)
							.replaceAll("\\{SERVER\\}", s.getName())
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
			if(s.getQueue().size() <= 0) continue;
			
			ProxiedPlayer nextplayer = s.getQueue().get(0);
			
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
			
			nextplayer.sendMessage(Main.formatMessage(msgs.get("status.sending-now").replaceAll("\\{SERVER\\}", name)));
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
		
		if(p.getServer().getInfo().getName().equals(s)) {
			p.sendMessage(msgs.getBC("errors.already-connected"));
			return;
		}
		
		Server beforeQueue = findPlayerInQueue(p);
		if(beforeQueue != null) {
			if(beforeQueue.equals(server)) {
				p.sendMessage(msgs.getBC("errors.already-queued"));
				return;
			}
			p.sendMessage(msgs.getBC("status.left-last-queue"));
			beforeQueue.getQueue().remove(p);
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
			us.ajg0702.queue.Logic.priorityLogic(list, s, p);
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
		if(list.size() <= 1) {
			p.sendMessage(Main.formatMessage(
					msgs.get("status.now-in-empty-queue")
					.replaceAll("\\{POS\\}", pos+"")
					.replaceAll("\\{LEN\\}", len+"")
					.replaceAll("\\{SERVER\\}", s)
					));
		} else {
			p.sendMessage(Main.formatMessage(
					msgs.get("status.now-in-queue")
					.replaceAll("\\{POS\\}", pos+"")
					.replaceAll("\\{LEN\\}", len+"")
					.replaceAll("\\{SERVER\\}", s)
					));
		}
		
		BungeeUtils.sendCustomData(p, "position", pos+"");
		BungeeUtils.sendCustomData(p, "positionof", len+"");
		BungeeUtils.sendCustomData(p, "queuename", s);
		BungeeUtils.sendCustomData(p, "inqueue", "true");
		
		if(list.size() <= 1) {
			sendPlayers(s);
		}
	}
	
	/**
	 * Finds which server the player is queued for
	 * @param p The player to search for
	 * @return The server the player is queued for. Null if not in a queue
	 */
	public Server findPlayerInQueue(ProxiedPlayer p) {
		for(Server s : servers) {
			if(s.getQueue().contains(p)) return s;
		}
		return null;
	}
}
