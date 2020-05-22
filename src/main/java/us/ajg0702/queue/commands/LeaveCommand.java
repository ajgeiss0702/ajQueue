package us.ajg0702.queue.commands;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import us.ajg0702.queue.Main;
import us.ajg0702.queue.Manager;
import us.ajg0702.queue.Server;
import us.ajg0702.queue.utils.BungeeMessages;

public class LeaveCommand extends Command {
	Main plugin;
	BungeeMessages msgs;
	public LeaveCommand(Main pl) {
		super("leavequeue");
		this.plugin = pl;
		msgs = BungeeMessages.getInstance();
		
	}
	@Override
	public void execute(CommandSender sender, String[] args) {
		if(!(sender instanceof ProxiedPlayer)) {
			sender.sendMessage(msgs.getBC("errors.player-only"));
			return;
		}
		ProxiedPlayer p = (ProxiedPlayer) sender;
		Server server = Manager.getInstance().findPlayerInQueue((ProxiedPlayer) sender);
		if(server != null) {
			server.getQueue().remove(p);
			p.sendMessage(msgs.getBC("commands.leave-queue"));
		}
	}
}
