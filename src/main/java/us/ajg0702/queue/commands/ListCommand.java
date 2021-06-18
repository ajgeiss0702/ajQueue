package us.ajg0702.queue.commands;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import us.ajg0702.queue.Main;
import us.ajg0702.queue.Manager;
import us.ajg0702.queue.QueueServer;
import us.ajg0702.utils.bungee.BungeeMessages;

import java.util.ArrayList;
import java.util.Arrays;

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
		
		ArrayList<BaseComponent> m = new ArrayList<>(Arrays.asList(msgs.getBC("commands.listqueues.header")));
		for(QueueServer s : Manager.getInstance().getServers()) {
			String color = "&a";
			if(!s.isOnline()) {
				color = "&c";
			} else if(!s.isJoinable(spp)) {
				color = "&e";
			}

			m.addAll(Arrays.asList(TextComponent.fromLegacyText("\n")));
			m.addAll(Arrays.asList(msgs.getBC("commands.listqueues.format",
					"COLOR:" + msgs.color(color),
					"NAME:" + s.getName(),
					"COUNT:" + s.getQueue().size(),
					"STATUS:" + s.getStatusString(spp)
			)));
		}
		
		sender.sendMessage(m.toArray(new BaseComponent[m.size()-1]));
		
	}
}
