package us.ajg0702.queue.common;

import org.jetbrains.annotations.NotNull;
import us.ajg0702.queue.api.ServerTimeManager;
import us.ajg0702.queue.api.players.AdaptedPlayer;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ServerTimeManagerImpl implements ServerTimeManager {

    final Map<UUID, Long> serverSwitches = new ConcurrentHashMap<>();


    @Override
    public long getLastServerChange(@NotNull AdaptedPlayer player) {
        return serverSwitches.get(player.getUniqueId());
    }

    public void playerChanged(@NotNull AdaptedPlayer player) {
        serverSwitches.put(player.getUniqueId(), System.currentTimeMillis());
    }

    public void removePlayer(@NotNull AdaptedPlayer player) {
        serverSwitches.remove(player.getUniqueId());
    }
}
