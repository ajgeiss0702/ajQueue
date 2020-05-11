package us.ajg0702.queue;

import java.util.ArrayList;
import java.util.List;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import us.ajg0702.queue.utils.BungeeMessages;

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
				List<String> svs = pl.getConfig().getStringList("queue-servers");
				for(String s : svs) {
					if(!s.contains(":")) {
						pl.getLogger().warning("The queue-servers section in the config has been set up incorrectly! Please read the comment above the setting and make sure you have a queue server and a destination server separated by a colon (:)");
						break;
					}
				}
				sender.sendMessage(msgs.getBC("commands.reload"));
				return;
				
			}
			if(args[0].equalsIgnoreCase("list")) {
				int total = 0;
				for(String server : pl.queues.keySet()) {
					
					String msg = msgs.get("list.format").replaceAll("\\{SERVER\\}", server);
					String playerlist = "";
					List<ProxiedPlayer> players = pl.queues.get(server);
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
				sender.sendMessage(Main.formatMessage(pl.isp+""));
				return;
			}
			if(args[0].equalsIgnoreCase("player")) {
				sender.sendMessage(Main.formatMessage("/ajQueue <player> <server>"));
			}
		}
		if(args.length == 2) {
			
			
			List<String> playerNames = new ArrayList<>();
			for(ProxiedPlayer ply : pl.getProxy().getPlayers()) {
				if(ply == null || !ply.isConnected()) continue;
				playerNames.add(ply.getName().toLowerCase());
			}
			if(playerNames.contains(args[0].toLowerCase())) {
				if(!sender.hasPermission("ajqueue.send")) {
					sender.sendMessage(msgs.getBC("noperm"));
					return;
				}
				ProxiedPlayer ply = pl.getProxy().getPlayer(args[0]);
				pl.addToQueue(ply, args[1]);
				sender.sendMessage(Main.formatMessage(
						msgs.get("send")
						.replaceAll("\\{PLAYER\\}", ply.getDisplayName())
						.replaceAll("\\{SERVER\\}", args[1]))
					);
				return;
			}
		}
		
		sender.sendMessage(Main.formatMessage("/ajqueue <reload|list|player>"));
	}}
