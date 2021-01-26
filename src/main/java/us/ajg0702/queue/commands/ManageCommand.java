package us.ajg0702.queue.commands;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import us.ajg0702.queue.Main;
import us.ajg0702.queue.Manager;
import us.ajg0702.queue.QueueServer;
import us.ajg0702.utils.bungee.BungeeMessages;

import java.util.ArrayList;
import java.util.List;

public class ManageCommand extends Command {
	
	Main pl;
	BungeeMessages msgs;

	public ManageCommand(Main pl) {
		super("ajqueue", null, "ajq");
		this.pl = pl;
		msgs = BungeeMessages.getInstance();
	}



	@Override
	public void execute(CommandSender sender, String[] args) {
		if(args.length == 1) {
			if(args[0].equalsIgnoreCase("reload")) {
				if(!sender.hasPermission("ajqueue.reload")) {
					sender.sendMessage(msgs.getBC("noperm"));
					return;
				}
				msgs.reload();
				pl.getConfig().reload();
				pl.timeBetweenPlayers = pl.getConfig().getDouble("wait-time");
				Manager.getInstance().reloadIntervals();
				Manager.getInstance().reloadServers();
				pl.checkConfig();
				sender.sendMessage(msgs.getBC("commands.reload"));
				return;
				
			}
			if(args[0].equalsIgnoreCase("list")) {
				int total = 0;
				for(QueueServer server : Manager.getInstance().getServers()) {
					
					String msg = msgs.get("list.format").replaceAll("\\{SERVER\\}", server.getName());
					String playerlist = "";
					List<ProxiedPlayer> players = server.getQueue();
					if(msg.contains("{LIST}")) {
						for(ProxiedPlayer p : players) {
							playerlist += msgs.get("list.playerlist").replaceAll("\\{NAME\\}", p.getDisplayName());
						}
						if(playerlist.equalsIgnoreCase("")) {
							playerlist = msgs.get("list.none")+", ";
						}
						playerlist = playerlist.substring(0, playerlist.length()-2);
						msg = msg.replaceAll("\\{LIST\\}", playerlist);
					}
					total += players.size();
					msg = msg.replaceAll("\\{COUNT\\}", players.size()+"");
					sender.sendMessage(Main.formatMessage(msg));
				}
				sender.sendMessage(Main.formatMessage(msgs.get("list.total").replaceAll("\\{TOTAL\\}", total+"")));
				return;
			}
			if(args[0].equalsIgnoreCase("p")) {
				sender.sendMessage(Main.formatMessage(pl.isp()+""));
				return;
			}
			if(args[0].equalsIgnoreCase("statusdebug")) {
				QueueServer s = Manager.getInstance().getSingleServer((ProxiedPlayer) sender);
				if(s == null) return;
				sender.sendMessage(Main.formatMessage(s.getJoinableDebug((ProxiedPlayer) sender)));
			}
			if(args[0].equalsIgnoreCase("player")) {
				sender.sendMessage(Main.formatMessage("/ajQueue <player> <server>"));
				return;
			}
			if(args[0].equalsIgnoreCase("version")) {
				sender.sendMessage(Main.formatMessage(pl.getDescription().getVersion()));
				return;
			}
			if(args[0].equalsIgnoreCase("pause")) {
				if(!sender.hasPermission("ajqueue.pause")) {
					sender.sendMessage(msgs.getBC("noperm"));
					return;
				}
				sender.sendMessage(msgs.getBC("commands.pause.more-args"));
				return;
			}
		}
		if(args.length == 2) {
			if(args[0].equalsIgnoreCase("pause")) {
				if(!sender.hasPermission("ajqueue.pause")) {
					sender.sendMessage(msgs.getBC("noperm"));
					return;
				}
				if(!Manager.getInstance().getServerNames().contains(args[1])) {
					sender.sendMessage(msgs.getBC("errors.server-not-exist"));
					return;
				}
				QueueServer srv = Manager.getInstance().findServer(args[1]);
				if(srv == null) {
					sender.sendMessage(msgs.getBC("commands.pause.no-server", "SERVER:"+args[1]));
					return;
				}
				srv.setPaused(!srv.isPaused());
				sender.sendMessage(msgs.getBC("commands.pause.success",
						"SERVER:"+srv.getName(),
						"PAUSED:"+msgs.get("commands.pause.paused."+srv.isPaused())
						));
				return;
			}
			
			
			
			if(!sender.hasPermission("ajqueue.send")) {
				sender.sendMessage(msgs.getBC("noperm"));
				return;
			}
			
			List<String> playerNames = getNameList();
			if(playerNames.contains(args[0].toLowerCase())) {
				
				ProxiedPlayer ply = pl.getProxy().getPlayer(args[0]);
				Manager.getInstance().addToQueue(ply, args[1]);
				sender.sendMessage(Main.formatMessage(
						msgs.get("send")
						.replaceAll("\\{PLAYER\\}", ply.getDisplayName())
						.replaceAll("\\{SERVER\\}", args[1]))
					);
				return;
			}
		}
		if(args.length == 3) {
			if(args[0].equalsIgnoreCase("pause")) {
				if(!sender.hasPermission("ajqueue.pause")) {
					sender.sendMessage(msgs.getBC("noperm"));
					return;
				}
				if(!Manager.getInstance().getServerNames().contains(args[1])) {
					sender.sendMessage(msgs.getBC(""));
					return;
				}
				QueueServer srv = Manager.getInstance().findServer(args[1]);
				if(srv == null) {
					sender.sendMessage(msgs.getBC("commands.pause.no-server", "SERVER:"+args[1]));
					return;
				}
				srv.setPaused(args[2].equalsIgnoreCase("on") || args[2].equalsIgnoreCase("true"));
				sender.sendMessage(msgs.getBC("commands.pause.success",
						"SERVER:"+srv.getName(),
						"PAUSED:"+msgs.get("commands.pause.paused."+srv.isPaused())
						));
				return;
			}
		}
		
		sender.sendMessage(Main.formatMessage("/ajqueue <reload|list|player|pause>"));
	}
	
	private List<String> getNameList() {
		List<String> playerNames = new ArrayList<>();
		for(ProxiedPlayer ply : pl.getProxy().getPlayers()) {
			if(ply == null || !ply.isConnected()) continue;
			playerNames.add(ply.getName().toLowerCase());
		}
		return playerNames;
	}
}
