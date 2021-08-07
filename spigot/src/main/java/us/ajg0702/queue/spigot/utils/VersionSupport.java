package us.ajg0702.queue.spigot.utils;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class VersionSupport {
	
	public static String getVersion() {
		return Bukkit.getVersion().split("\\(MC: ")[1].split("\\)")[0];
	}
	public static int getMinorVersion() {
		return Integer.parseInt(getVersion().split("\\.")[1]);
	}

	/**
	 * Send the player an actionbar message
	 * @param ply The {@link org.bukkit.entity.Player Player} to send the actionbar to
	 * @param message The message to send in the actionbar.
	 */
	public static void sendActionBar(Player ply, String message) {
		// Use spigot version if available, otherwise use packets.
		if(getMinorVersion() >= 11) {
			ply.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(message));
		} else if(getMinorVersion() >= 8) {
			ActionBar.send(ply, message);
		}
	}

}
