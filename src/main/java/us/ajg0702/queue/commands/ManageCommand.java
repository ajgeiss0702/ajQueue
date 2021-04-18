package us.ajg0702.queue.commands;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;
import us.ajg0702.queue.Main;
import us.ajg0702.queue.Manager;
import us.ajg0702.queue.QueueServer;
import us.ajg0702.utils.bungee.BungeeMessages;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ManageCommand extends Command implements TabExecutor {
	
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
				if(!sender.hasPermission("ajqueue.list")) {
					sender.sendMessage(msgs.getBC("noperm"));
					return;
				}
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
			if(args[0].equalsIgnoreCase("send")) {
				if(!sender.hasPermission("ajqueue.send")) {
					sender.sendMessage(msgs.getBC("noperm"));
					return;
				}
				sender.sendMessage(Main.formatMessage("/ajQueue send <player> <server>"));
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
					sender.sendMessage(msgs.getBC("errors.server-not-exist", "SERVER:"+args[1]));
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
			if(args[0].equalsIgnoreCase("send")) {
				if(!sender.hasPermission("ajqueue.send")) {
					sender.sendMessage(msgs.getBC("noperm"));
					return;
				}
				sender.sendMessage(Main.formatMessage("/ajQueue send <player> <server>"));
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
					sender.sendMessage(msgs.getBC("commands.pause.no-server", "SERVER:"+args[1]));
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
			if(args[0].equalsIgnoreCase("send")) {
				if(!sender.hasPermission("ajqueue.send")) {
					sender.sendMessage(msgs.getBC("noperm"));
					return;
				}

				if(Manager.getInstance().getServer(args[2]) == null) {
					sender.sendMessage(msgs.getBC("errors.server-not-exist", "SERVER:"+args[2]));
					return;
				}
				
				List<String> playerNames = getNameList(true);
				if(playerNames.contains(args[1].toLowerCase())) {
					
					ProxiedPlayer ply = pl.getProxy().getPlayer(args[1]);
					Manager.getInstance().addToQueue(ply, args[2]);
					sender.sendMessage(Main.formatMessage(
							msgs.get("send")
							.replaceAll("\\{PLAYER\\}", ply.getDisplayName())
							.replaceAll("\\{SERVER\\}", args[2]))
						);
					return;
				} else if(pl.getProxy().getServers().keySet().contains(args[1])) {

					ServerInfo from = pl.getProxy().getServerInfo(args[1]);
					if(from == null) {
						sender.sendMessage(msgs.getBC("errors.server-not-exist", "SERVER:"+args[1]));
						return;
					}
					List<ProxiedPlayer> players = new ArrayList<>(from.getPlayers());
					for(ProxiedPlayer ply : players) {
						Manager.getInstance().addToQueue(ply, args[2]);
					}

					sender.sendMessage(msgs.getBC("send", "PLAYER:"+args[1], "SERVER:"+args[2]));
					return;

				} else {
					sender.sendMessage(msgs.getBC("commands.send.player-not-found"));
					return;
				}
			}
		}
		
		sender.sendMessage(Main.formatMessage("/ajqueue <reload|list|send|pause>"));
	}
	
	private List<String> getNameList(boolean lowercase) {
		List<String> playerNames = new ArrayList<>();
		for(ProxiedPlayer ply : pl.getProxy().getPlayers()) {
			if(ply == null || !ply.isConnected()) continue;
			if(lowercase) {
				playerNames.add(ply.getName().toLowerCase());
			} else {
				playerNames.add(ply.getName());
			}
		}
		return playerNames;
	}
	
	@Override
	public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
		if(args.length == 1) {
			return Arrays.asList("reload", "list", "send", "pause");
		}
		if(args.length == 2) {
			if(args[0].equalsIgnoreCase("send")) {
				List<String> options = new ArrayList<>(pl.getProxy().getServers().keySet());
				options.addAll(getNameList(false));
				return options;
			}
			if(args[0].equalsIgnoreCase("pause")) {
				return Manager.getInstance().getServerNames();
			}
		}
		if(args.length == 3) {
			if(args[0].equalsIgnoreCase("send")) {
				return Manager.getInstance().getServerNames();
			}
			if(args[0].equalsIgnoreCase("pause")) {
				return Arrays.asList("on", "off", "true", "false");
			}
		}
		return new ArrayList<>();
	}
}
