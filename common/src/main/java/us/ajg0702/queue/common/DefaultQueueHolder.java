package us.ajg0702.queue.common;

import com.google.common.collect.ImmutableList;
import us.ajg0702.queue.api.players.QueuePlayer;
import us.ajg0702.queue.api.queueholders.QueueHolder;
import us.ajg0702.queue.api.queues.QueueServer;
import us.ajg0702.queue.api.queues.QueueType;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

public class DefaultQueueHolder extends QueueHolder {

    List<QueuePlayer> queue = new CopyOnWriteArrayList<>();
    List<QueuePlayer> expressQueue = new CopyOnWriteArrayList<>();

    public DefaultQueueHolder(QueueServer queueServer) {
        super(queueServer);
    }

    @Override
    public String getIdentifier() {
        return "default";
    }

    @Override
    public void addPlayer(QueuePlayer player) {
        queue.add(player);
    }

    @Override
    public void addPlayer(QueuePlayer player, int position) {
        if(player.isInStandardQueue()) {
            queue.add(position, player);
        } else {
            expressQueue.add(position, player);
        }
    }

    @Override
    public void removePlayer(QueuePlayer player) {
        if(player.isInStandardQueue()) {
            queue.remove(player);
        } else {
            expressQueue.remove(player);
        }
    }

    @Override
    public QueuePlayer findPlayer(UUID uuid) {
        for(QueuePlayer queuePlayer : queue) {
            if(queuePlayer.getUniqueId().toString().equals(uuid.toString())) {
                return queuePlayer;
            }
        }
        for(QueuePlayer queuePlayer : expressQueue) {
            if(queuePlayer.getUniqueId().toString().equals(uuid.toString())) {
                return queuePlayer;
            }
        }
        return null;
    }

    @Override
    public QueuePlayer findPlayer(String name) {
        for(QueuePlayer queuePlayer : queue) {
            if(queuePlayer.getName().equalsIgnoreCase(name)) {
                return queuePlayer;
            }
        }
        return null;
    }

    @Override
    public int getStandardQueueSize() {
        return queue.size();
    }

    @Override
    public int getExpressQueueSize() {
        return expressQueue.size();
    }

    @Override
    public int getTotalQueueSize() {
        return queue.size() + expressQueue.size();
    }

    @Override
    public int getPosition(QueuePlayer player) {
        return (player.getQueueType() == QueueType.EXPRESS ? expressQueue : queue).indexOf(player) + 1;
    }

    @Override
    public List<QueuePlayer> getAllStandardPlayers() {
        return ImmutableList.copyOf(queue);
    }

    @Override
    public List<QueuePlayer> getAllExpressPlayers() {
        return ImmutableList.copyOf(expressQueue);
    }
}
