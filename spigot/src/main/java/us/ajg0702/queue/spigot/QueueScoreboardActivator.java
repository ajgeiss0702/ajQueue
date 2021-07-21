package us.ajg0702.queue.spigot;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class QueueScoreboardActivator extends Event {
	private static final HandlerList HANDLERS = new HandlerList();

    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
    
    Player ply;
    
    public QueueScoreboardActivator(Player p) {
    	this.ply = p;
    }
    
    public Player getPlayer() {
    	return ply.getPlayer();
    }

}
