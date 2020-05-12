package us.ajg0702.queue.utils;

import java.util.Collection;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class BungeeUtils {
	public static void sendCustomData(ProxiedPlayer player, String channel, String data1) {
	    Collection<ProxiedPlayer> networkPlayers = ProxyServer.getInstance().getPlayers();
	    // perform a check to see if globally are no players
	    if ( networkPlayers == null || networkPlayers.isEmpty()) return;

	    ByteArrayDataOutput out = ByteStreams.newDataOutput();
	    out.writeUTF( channel );
	    out.writeUTF( data1 );
	 
	    // we send the data to the server
	    // using ServerInfo the packet is being queued if there are no players in the server
	    // using only the server to send data the packet will be lost if no players are in it
	    player.getServer().getInfo().sendData( "ajqueue:tospigot", out.toByteArray() );
	}
}
