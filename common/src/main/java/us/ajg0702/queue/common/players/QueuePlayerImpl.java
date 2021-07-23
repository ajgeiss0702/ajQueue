package us.ajg0702.queue.common.players;

import org.jetbrains.annotations.Nullable;
import us.ajg0702.queue.api.players.AdaptedPlayer;
import us.ajg0702.queue.api.players.QueuePlayer;
import us.ajg0702.queue.api.queues.QueueServer;

import java.util.UUID;

public class QueuePlayerImpl implements QueuePlayer {


    private AdaptedPlayer player;
    private final QueueServer server;

    private final int highestPriority;

    private final UUID uuid;
    private final String name;

    public QueuePlayerImpl(AdaptedPlayer player, QueueServer server, int highestPriority) {
        this.player = player;
        this.server = server;

        this.highestPriority = highestPriority;

        uuid = player.getUniqueId();
        name = player.getName();
    }

    @Override
    public UUID getUniqueId() {
        if(uuid == null) throw new IllegalStateException("Why is my UUID null??");
        return uuid;
    }

    @Override
    public QueueServer getQueueServer() {
        return server;
    }

    @Override
    public int getPosition() {
        return getQueueServer().getQueue().indexOf(this)+1;
    }

    @Nullable
    @Override
    public AdaptedPlayer getPlayer() {
        if(player != null && !player.isConnected()) player = null;
        return player;
    }

    @Override
    public void setPlayer(AdaptedPlayer player) {
        if(player != null && !player.getUniqueId().equals(getUniqueId())) {
            throw new IllegalArgumentException("UUIDs do not match");
        }
        this.player = player;
    }

    @Override
    public int getPriority() {
        return highestPriority;
    }

    @Override
    public boolean hasPriority() {
        return highestPriority > 0;
    }

    @Override
    public String getName() {
        return name;
    }



    @Override
    public long getTimeSinceOnline() {
        if(player != null && player.isConnected()) {
            return 0;
        }
        return System.currentTimeMillis()-leaveTime;
    }


    private long leaveTime = 0;
    public void setLeaveTime(long leaveTime) {
        this.leaveTime = leaveTime;
    }
}
