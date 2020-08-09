package us.ajg0702.queue.commands;

import java.util.ArrayList;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;
import us.ajg0702.queue.Main;
import us.ajg0702.queue.Manager;
import us.ajg0702.utils.bungee.BungeeMessages;

public class MoveCommand extends Command implements TabExecutor {
	Main plugin;
	BungeeMessages msgs;
	public MoveCommand(Main pl) {
		super("move", null, "queue", "server", "joinqueue", "joinq");
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
		
		if(args.length > 0) {
			if(plugin.getConfig().getBoolean("require-permission") && !p.hasPermission("ajqueue.queue."+args[0])) {
				sender.sendMessage(msgs.getBC("noperm"));
				return;
			}
			Manager.getInstance().addToQueue(p, args[0]);
		} else if(args.length == 0) {
			sender.sendMessage(msgs.getBC("commands.joinqueue.usage"));
			/*Server server = Manager.getInstance().findPlayerInQueue((ProxiedPlayer) sender);
			if(server != null) {
				server.getQueue().remove(p);
				p.sendMessage(msgs.getBC("commands.leave-queue"));
			}*/
		}
    }

	@Override
	public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
		if(!plugin.getConfig().getBoolean("tab-complete-queues")) {
			return new ArrayList<>();
		}
		if(args.length == 1) {
			return Manager.getInstance().getServerNames();
		}
		return null;
	}
}
