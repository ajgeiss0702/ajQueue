package us.ajg0702.queue.common.players;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import us.ajg0702.queue.api.AjQueueAPI;
import us.ajg0702.queue.api.events.PreConnectEvent;
import us.ajg0702.queue.api.players.AdaptedPlayer;
import us.ajg0702.queue.api.players.QueuePlayer;
import us.ajg0702.queue.api.queues.QueueServer;
import us.ajg0702.queue.api.queues.QueueType;
import us.ajg0702.queue.api.server.AdaptedServer;
import us.ajg0702.queue.api.util.ExpressRatio;
import us.ajg0702.queue.common.QueueMain;
import us.ajg0702.queue.common.queues.QueueServerImpl;
import us.ajg0702.utils.common.Messages;

import java.util.UUID;

public class QueuePlayerImpl implements QueuePlayer {


    private AdaptedPlayer player;
    private final QueueServer server;
    private final QueueType queueType;

    private final int highestPriority;

    private final UUID uuid;
    private final String name;

    private final int maxOfflineTime;

    private final AdaptedServer initialServer;

    public int lastPosition;

    public QueuePlayerImpl(UUID uuid, String name, QueueServer server, int highestPriority, int maxOfflineTime, QueueType queueType) {
        this(null, name, uuid, server, highestPriority, maxOfflineTime, queueType);
    }

    public QueuePlayerImpl(AdaptedPlayer player, QueueServer server, int highestPriority, int maxOfflineTime, QueueType queueType) {
        this(player, player.getName(), player.getUniqueId(), server, highestPriority, maxOfflineTime, queueType);
    }

    private QueuePlayerImpl(@Nullable AdaptedPlayer player, String name, @NotNull UUID uuid, QueueServer server, int highestPriority, int maxOfflineTime, QueueType queueType) {
        this.player = player;
        this.server = server;
        this.queueType = queueType;

        this.highestPriority = highestPriority;

        this.uuid = uuid;
        this.name = name;

        this.maxOfflineTime = maxOfflineTime;

        initialServer = player != null ? player.getCurrentServer() : null;

        lastPosition = getPosition();
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
    public QueueType getQueueType() {
        return queueType;
    }

    @Override
    public int getAbsolutePosition() {
        int otherQueueSize = isInStandardQueue() ?
                server.getQueueHolder().getExpressQueueSize() :
                server.getQueueHolder().getStandardQueueSize();

        // Convert 1-based position to 0-based index for calculation
        int playerIndex = getPosition() - 1;

        // dont bother with other calculations if the other queue is empty
        if(otherQueueSize == 0) return playerIndex + 1;

        ExpressRatio ratio = ((QueueMain) AjQueueAPI.getInstance()).getExpressRatio();

        // the batch size of the queue that the player is in
        int playerQueueBatchSize = isInStandardQueue() ? ratio.getStandardCount() : ratio.getExpressCount();
        int otherBatchSize = isInStandardQueue() ? ratio.getExpressCount() : ratio.getStandardCount();

        // 1. Calculate how many FULL cycles (batches) have passed before the current batch
        int completeCycles = playerIndex / playerQueueBatchSize;

        // 2. Calculate how many players from the OTHER queue were taken in those complete cycles
        int aheadInOtherQueue = completeCycles * otherBatchSize;

        // 3. Handle the current partial cycle
        // If the other queue has priority (goes first in the batch), we must add
        //  their batch size for the current round because they will cut in front of us.
        if (server.getLastQueueSend() != queueType && server.getSendCount() < otherBatchSize) {
            aheadInOtherQueue += otherBatchSize - server.getSendCount();
        }

        // 4. Cap the count: We cannot count more players than actually exist in the other queue
        if (aheadInOtherQueue > otherQueueSize) {
            aheadInOtherQueue = otherQueueSize;
        }

        // 5. Final Calculation:
        // (Players ahead in player's queue) + (Players ahead from other queue) + (Player)
        return playerIndex + aheadInOtherQueue + 1;
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

    @Override
    public int getMaxOfflineTime() {
        return maxOfflineTime;
    }

    @Override
    public AdaptedServer getInitialServer() {
        return initialServer;
    }

    @Override
    public int getETA() {
        return Math.round((float) (server.getAverageSendTime() * getAbsolutePosition()));
    }


    private long leaveTime = 0;
    public void setLeaveTime(long leaveTime) {
        this.leaveTime = leaveTime;
    }

    @Override
    public void connect(@NotNull AdaptedServer server) {
        AdaptedPlayer player = getPlayer();
        if (player == null) {
            throw new IllegalArgumentException("Player must be online!");
        }

        PreConnectEvent preConnectEvent = new PreConnectEvent(server, this);
        QueueMain.getInstance().call(preConnectEvent);

        // Event declares that the addon/developer handle notifying the player of this cancellation
        if (preConnectEvent.isCancelled()) { return; }

        // Fetch an addon-supplied handle if available, or use the existing server handle (default behavior)
        player.connect(preConnectEvent.getTargetServer());
    }
}
