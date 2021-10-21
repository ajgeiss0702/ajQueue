package us.ajg0702.queue.spigot;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class Commands implements CommandExecutor {
	
	final SpigotMain pl;
	public Commands(SpigotMain pl) {
		this.pl = pl;
	}

	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
		if(!pl.hasProxy() && pl.config.getBoolean("check-proxy-response")) {
			sender.sendMessage(color("&cajQueue must also be installed on the proxy!&7 If it has been installed on the proxy, make sure it loaded correctly and try relogging."));
			return true;
		}
		Player player = null;
		if(sender instanceof Player) {
			player = (Player) sender;
		}
		if(command.getName().equals("leavequeue")) {
			if(player == null) return true;
			StringBuilder arg = new StringBuilder();
			for(String a : args) {
				arg.append(" ");
				arg.append(a);
			}
			pl.sendMessage(player, "leavequeue", arg.toString());
			return true;
		}
		if(args.length < 1) return false;
		
		String srvname = args[0];
		
		if(args.length > 1) {
			pl.getLogger().info("Sending "+args[0]+" to queue");
			if(!sender.hasPermission("ajqueue.send")) {
				sender.sendMessage(color("&cYou do not have permission to do this!"));
				return true;
			}
			Player tply = Bukkit.getPlayer(args[0]);
			if(tply == null) {
				sender.sendMessage(color("&cCannot find that player!"));
				return true;
			}
			player = tply;
			srvname = args[1];
		}
		if(pl.config.getBoolean("send-queue-commands-in-batches")) {
			pl.queuebatch.put(player, srvname);
		} else {
			assert player != null;
			pl.sendMessage(player, "queue", srvname);
		}
		
		return true;
	}
	
	public String color(String txt) {
		return ChatColor.translateAlternateColorCodes('&', txt);
	}

}
