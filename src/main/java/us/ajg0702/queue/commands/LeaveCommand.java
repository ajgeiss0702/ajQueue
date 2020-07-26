package us.ajg0702.queue.commands;

import java.util.List;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import us.ajg0702.queue.Main;
import us.ajg0702.queue.Manager;
import us.ajg0702.queue.QueueServer;
import us.ajg0702.utils.bungee.BungeeMessages;

public class LeaveCommand extends Command {
	Main plugin;
	BungeeMessages msgs;
	public LeaveCommand(Main pl) {
		super("leavequeue", null, "leaveq");
		this.plugin = pl;
		msgs = BungeeMessages.getInstance();
		
	}
	@Override
	public void execute(CommandSender sender, String[] args) {
		if(!(sender instanceof ProxiedPlayer)) {
			sender.sendMessage(msgs.getBC("errors.player-only"));
			return;
		}
		Manager man = Manager.getInstance();
		ProxiedPlayer p = (ProxiedPlayer) sender;
		List<QueueServer> servers = man.findPlayerInQueue(p);
		
		if(servers.size() == 0) {
			p.sendMessage(msgs.getBC("commands.leave.no-queues"));
			return;
		}
		
		if(servers.size() == 1) {
			servers.get(0).getQueue().remove(p);
			p.sendMessage(msgs.getBC("commands.leave-queue", "SERVER:"+plugin.aliases.getAlias(servers.get(0).getName())));
			return;
		}
		
		
		if(args.length <= 0) {
			p.sendMessage(msgs.getBC("commands.leave.more-args", "QUEUES:"+getQueueList(servers)));
			return;
		}
		
		String leaving = args[0];
		QueueServer leavingsrv = man.getServer(leaving);
		if(leavingsrv == null) {
			p.sendMessage(msgs.getBC("commands.leave.not-queued", "QUEUES:"+getQueueList(servers)));
			return;
		}
		if(leavingsrv.getQueue().indexOf(p) == -1) {
			p.sendMessage(msgs.getBC("commands.leave.not-queued", "QUEUES:"+getQueueList(servers)));
			return;
		}
		
		leavingsrv.getQueue().remove(p);
		p.sendMessage(msgs.getBC("commands.leave-queue", "SERVER:"+plugin.aliases.getAlias(leavingsrv.getName())));
		
	}
	
	private String getQueueList(List<QueueServer> servers) {
		String queueList = "";
		for(QueueServer server : servers) {
			queueList += msgs.get("commands.leave.queues-list-format").replaceAll("\\{NAME\\}", server.getName());
		}
		if(queueList.length() > 2) {
			queueList = queueList.substring(0, queueList.length()-2);
		}
		return queueList;
	}
}
