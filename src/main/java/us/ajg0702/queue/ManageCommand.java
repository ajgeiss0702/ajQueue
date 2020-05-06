package us.ajg0702.queue;

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
							playerlist = msgs.color("&7None, ");
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
		}
		
		sender.sendMessage(Main.formatMessage("/ajqueue <reload|list>"));
	}}
