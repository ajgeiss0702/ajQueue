package us.ajg0702.queue.api.events;

import us.ajg0702.queue.api.players.AdaptedPlayer;

/**
 * Event that is called when priorities are calculated.
 * You can provide dynamic priorities for your players based on your own criteria
 * instead of just permissions<br>
 * <br>
 * Just listen to this event, and use the addPriority method to add a priority.
 * If it is higher than a player's other priorities, it will be used.<br>
 * <br>
 * If running ajQueue (not ajQueuePlus), then any number bigger than 0 will count as priority
 */
public class PriorityCalculationEvent implements Event {
    private final AdaptedPlayer player;
    private int highestPriority;

    public PriorityCalculationEvent(AdaptedPlayer player, int highestPriority) {
        this.player = player;
        this.highestPriority = highestPriority;
    }

    /**
     * Gets the current highest priority
     * @return The highest priority number
     */
    public int getHighestPriority() {
        return highestPriority;
    }

    /**
     * Adds a priority. Does nothing if the priority is lower than the player already has
     * @param priority the priority to add
     */
    public void addPriority(int priority) {
        if(priority < highestPriority) return; // do nothing if this isnt going to change the highest priority
        highestPriority = priority;
    }
}
