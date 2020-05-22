package us.ajg0702.queue.spigot;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;

import us.ajg0702.queue.spigot.utils.VersionSupport;

public class Main extends JavaPlugin implements PluginMessageListener {
	public void onEnable() {
		getServer().getMessenger().registerIncomingPluginChannel(this, "ajqueue:tospigot", this);
		getLogger().info("Spigot side enabled! v"+getDescription().getVersion());
	}

	@Override
	public void onPluginMessageReceived(String channel, Player player, byte[] message) {
		if (!channel.equals("ajqueue:tospigot")) return;
		
		ByteArrayDataInput in = ByteStreams.newDataInput(message);
		
	    String subchannel = in.readUTF();
	    
	    if(subchannel.equals("actionbar")) {
	    	String playername = in.readUTF();
	    	Player p = Bukkit.getPlayer(playername);
	    	if(p == null) return;
	    	
	    	String data = in.readUTF();
	    	final String text = data.split(";time=")[0];
	    	//getLogger().info("recieved actionbar for "+player.getName()+": "+text);
	    	VersionSupport.sendActionBar(p, text);
	    }
	}
}
