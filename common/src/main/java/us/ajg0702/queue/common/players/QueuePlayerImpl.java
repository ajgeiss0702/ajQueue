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

    public QueuePlayerImpl(AdaptedPlayer player, QueueServer server, int highestPriority) {
        this.player = player;
        this.server = server;

        this.highestPriority = highestPriority;

        uuid = player.getUniqueId();
    }

    @Override
    public UUID getUniqueId() {
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
        if(!player.isConnected()) return null;
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
}
