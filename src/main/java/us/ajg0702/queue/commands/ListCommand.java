package us.ajg0702.queue.commands;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import us.ajg0702.queue.Main;
import us.ajg0702.queue.Manager;
import us.ajg0702.queue.QueueServer;
import us.ajg0702.utils.bungee.BungeeMessages;

public class ListCommand extends Command {
	Main pl;
	BungeeMessages msgs;

	public ListCommand(Main pl) {
		super("listqueues", null, "listq");
		this.pl = pl;
		msgs = BungeeMessages.getInstance();
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		if(!sender.hasPermission("ajqueue.listqueues")) {
			sender.sendMessage(msgs.getBC("noperm"));
			return;
		}
		
		ProxiedPlayer spp = null;
		if(sender instanceof ProxiedPlayer) {
			spp = (ProxiedPlayer) sender;
		}
		
		String m = msgs.get("commands.listqueues.header");
		for(QueueServer s : Manager.getInstance().getServers()) {
			String color = "&a";
			if(!s.isOnline()) {
				color = "&c";
			} else if(!s.isJoinable(spp)) {
				color = "&e";
			}
			m += "\n"+msgs.get("commands.listqueues.format")
					.replaceAll("\\{COLOR\\}", msgs.color(color))
					.replaceAll("\\{NAME\\}", s.getName())
					.replaceAll("\\{COUNT\\}", s.getQueue().size()+"");
		}
		
		sender.sendMessage(TextComponent.fromLegacyText(m));
		
	}
}
