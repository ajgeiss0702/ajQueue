package us.ajg0702.queue.spigot;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import us.ajg0702.queue.api.spigot.AjQueueSpigotAPI;

public class Commands implements CommandExecutor {
	
	final SpigotMain pl;
	public Commands(SpigotMain pl) {
		this.pl = pl;
	}

	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
		if(!pl.hasProxy() && pl.getAConfig().getBoolean("check-proxy-response")) {
			if(sender instanceof Player) pl.sendMessage((Player) sender, "ack", "");
			sender.sendMessage(
					color(
							"&c" +
								(sender.hasPermission("ajqueue.manage") ? "ajQueue" : "The queue plugin") +
								" must also be installed on the proxy!&7 If it has been installed on the proxy, make sure it loaded correctly and try again."
					)
			);
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


		// Queue command


		if(args.length < 1) return false;

		String serverName = args[0];

		boolean sudo = true;

		// /queue <player> <server>
		if(args.length > 1) {
			sudo = false;
			if(!sender.hasPermission("ajqueue.send")) {
				sender.sendMessage(color("&cYou do not have permission to do this!"));
				return true;
			}
			pl.getLogger().info("Sending "+args[0]+" to queue '" + args[1] + "'");
			Player playerToSend = Bukkit.getPlayer(args[0]);
			if(playerToSend == null) {
				sender.sendMessage(color("&cCannot find that player!"));
				return true;
			}
			player = playerToSend;
			serverName = args[1];
		}

		if(player == null) {
			sender.sendMessage("I need to know what player to send!");
			return true;
		}

		if(sudo) {
			if(pl.getAConfig().getBoolean("send-queue-commands-in-batches")) {
				pl.queuebatch.put(player, serverName);
			} else {
				AjQueueSpigotAPI.getInstance().sudoQueue(player.getUniqueId(), serverName);
			}
		} else {
			AjQueueSpigotAPI.getInstance().addToQueue(player.getUniqueId(), serverName);
		}
		
		return true;
	}
	
	public String color(String txt) {
		return ChatColor.translateAlternateColorCodes('&', txt);
	}

}
