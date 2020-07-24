package us.ajg0702.queue;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.event.ServerKickEvent;
import net.md_5.bungee.api.event.ServerSwitchEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.event.EventHandler;
import us.ajg0702.queue.commands.LeaveCommand;
import us.ajg0702.queue.commands.ManageCommand;
import us.ajg0702.queue.commands.MoveCommand;
import us.ajg0702.utils.bungee.BungeeConfig;
import us.ajg0702.utils.bungee.BungeeMessages;
import us.ajg0702.utils.bungee.BungeeStats;
import us.ajg0702.utils.bungee.BungeeUtils;

public class Main extends Plugin implements Listener {
	
	static Main plugin = null;
	
	public double timeBetweenPlayers = 5.0;
	
	BungeeStats metrics;
	
	BungeeMessages msgs;
	
	BungeeConfig config;
	
	Manager man;
	
	boolean isp;
	
	MoveCommand moveCommand;
	
	public AliasManager aliases;
	
	@Override
	public void onEnable() {
		plugin = this;
		
		LinkedHashMap<String, String> d = new LinkedHashMap<>();
		
		
		d.put("status.offline.base", "&c{SERVER} is {STATUS}. &7You are in position &f{POS}&7 of &f{LEN}&7.");
		
		d.put("status.offline.offline", "offline");
		d.put("status.offline.restarting", "restarting");
		d.put("status.offline.full", "full");
		d.put("status.offline.restricted", "restricted");
		d.put("status.offline.paused", "paused");
		
		d.put("status.online.base", "&7You are in position &f{POS}&7 of &f{LEN}&7. Estimated time: {TIME}");
		d.put("status.left-last-queue", "&aYou left the last queue you were in.");
		d.put("status.now-in-queue", "&aYou are now queued for {SERVER}! &7You are in position &f{POS}&7 of &f{LEN}&7.\n&7Type &f/leavequeue&7 to leave the queue!");
		d.put("status.now-in-empty-queue", "");
		d.put("status.sending-now", "&aSending you to &f{SERVER} &anow..");
		
		d.put("errors.server-not-exist", "&cThat server does not exist!");
		d.put("errors.already-queued", "&cYou are already queued for that server!");
		d.put("errors.player-only", "&cThis command can only be executed as a player!");
		d.put("errors.already-connected", "&cYou are already connected to this server!");
		d.put("errors.cant-join-paused", "&cYou cannot join the queue for {SERVER} because it is paused.");
		d.put("errors.deny-joining-from-server", "&cYou are not allowed to join queues from this server!");
		
		d.put("commands.leave-queue", "&aYou left the queue for {SERVER}!");
		d.put("commands.reload", "&aConfig and messages reloaded successfully!");
		d.put("commands.joinqueue.usage", "&cUsage: /joinqueue <server>");
		
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
		
		d.put("commands.leave.more-args", "&cPlease specify which queue you want to leave! &7You are in these queues: {QUEUES}");
		d.put("commands.leave.queues-list-format", "&f{NAME}&7, ");
		d.put("commands.leave.not-queued", "&cYou are not queued for that server! &7You are in these queues: {QUEUES}");
		d.put("commands.leave.no-queues", "&cYou are not queued!");
		
		d.put("commands.pause.more-args", "&cUsage: /ajqueue pause <server>");
		d.put("commands.pause.no-server", "&cThat server does not exist!");
		d.put("commands.pause.success", "&aThe queue for &f{SERVER} &ais now {PAUSED}");
		d.put("commands.pause.paused.true", "&epaused");
		d.put("commands.pause.paused.false", "&aun-paused");
		
		msgs = BungeeMessages.getInstance(this, d);
		//msgs = BungeeMessages.getInstance(this);
		
		config = new BungeeConfig(this);
		checkConfig();
		
		aliases = new AliasManager(this);
		
		moveCommand = new MoveCommand(this);
		this.getProxy().getPluginManager().registerCommand(this, moveCommand);
		this.getProxy().getPluginManager().registerCommand(this, new ManageCommand(this));
		this.getProxy().getPluginManager().registerCommand(this, new LeaveCommand(this));
		
		this.getProxy().getPluginManager().registerListener(this, this);
		
		getProxy().registerChannel("ajqueue:tospigot");
		getProxy().registerChannel("ajqueue:tobungee");
		
		timeBetweenPlayers = config.getDouble("wait-time");
		
		isp = Logic.isp;
		
		man = Manager.getInstance(this);
		
		
		metrics = new BungeeStats(this, 7404);
		metrics.addCustomChart(new BungeeStats.SimplePie("premium", () -> isp+""));
		
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
		List<Server> alreadyqueued = man.findPlayerInQueue(p);
		for(Server ser : alreadyqueued) {
			List<ProxiedPlayer> queue = ser.getQueue();
			int pos = queue.indexOf(p);
			if((pos == 0 && p.getServer().getInfo().equals(ser.getInfo())) || config.getBoolean("remove-player-on-server-switch")) {
				queue.remove(p);
			}
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
		if(p.hasPermission("ajqueue.stay-queued-on-leave")) return;
		List<Server> servers = man.findPlayerInQueue(p);
		for(Server server : servers) {
			server.getQueue().remove(p);
		}
	}
	
	@EventHandler
	public void onFailedMove(ServerKickEvent e) {
		ProxiedPlayer p = e.getPlayer();
		List<Server> queuedServers = man.findPlayerInQueue(p);
		for(Server server : queuedServers) { 
			if(!(e.getKickedFrom().equals(server.getInfo()))) continue;
			if(server.getQueue().indexOf(p) != 0) continue;
			List<String> kickreasons = config.getStringList("kick-reasons");
			boolean hasReason = false;
			//getLogger().info(e.getKickReasonComponent());
			for(String reason : kickreasons) {
				for(BaseComponent b : e.getKickReasonComponent()) {
					if(b.toPlainText().toLowerCase().contains(reason)) {
						hasReason = true;
						break;
					}
				}
				if(hasReason) break;
			}
			if(!hasReason) continue;
			server.getQueue().remove(p);
		}
	}
	
	
	@EventHandler
	public void onMessage(PluginMessageEvent e) {
		//getLogger().info("Recieved message of "+e.getTag());
		if(!e.getTag().equals("ajqueue:tobungee")) return;
		e.setCancelled(true);
		DataInputStream in = new DataInputStream(new ByteArrayInputStream(e.getData()));
		try {
			String subchannel = in.readUTF();
			ProxiedPlayer player = (ProxiedPlayer) e.getReceiver();
			
			
			if(subchannel.equals("queue")) {
				String data = in.readUTF();
				String[] args = new String[1];
				args[0] = data;
				moveCommand.execute(player, args);
				//man.addToQueue(player, data);
				
			}
			if(subchannel.equals("queuename")) {
				BungeeUtils.sendCustomData(player, "queuename", aliases.getAlias(man.getQueuedName(player)));
			}
			if(subchannel.equals("position")) {
				Server server = man.getSingleServer(player);
				String pos = msgs.get("placeholders.position.none");
				if(server != null) {
					pos = server.getQueue().indexOf(player)+1+"";
				}
				BungeeUtils.sendCustomData(player, "position", pos);
			}
			if(subchannel.equals("positionof")) {
				Server server = man.getSingleServer(player);
				String pos = msgs.get("placeholders.position.none");
				if(server != null) {
					pos = server.getQueue().size()+"";
				}
				BungeeUtils.sendCustomData(player, "positionof", pos);
			}
			if(subchannel.equals("inqueue")) {
				Server server = man.getSingleServer(player);
				BungeeUtils.sendCustomData(player, "inqueue", (server != null)+"");
			}
			if(subchannel.equals("queuedfor")) {
				String srv = in.readUTF();
				Server server = man.findServer(srv);
				if(server == null) return;
				BungeeUtils.sendCustomData(player, "queuedfor", srv, server.getQueue().size()+"");
			}
			
		} catch (IOException e1) {
			getLogger().warning("An error occured while reading data from spigot side:");
			e1.printStackTrace();
		}
	}
	
}
