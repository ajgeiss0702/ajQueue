package us.ajg0702.queue.common;

import com.google.common.collect.ImmutableList;

import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import io.lettuce.core.support.ConnectionPoolSupport;

import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

import us.ajg0702.queue.api.AjQueueAPI;
import us.ajg0702.queue.api.players.AdaptedPlayer;
import us.ajg0702.queue.api.players.QueuePlayer;
import us.ajg0702.queue.api.queueholders.QueueHolder;
import us.ajg0702.queue.api.queues.QueueServer;
import us.ajg0702.queue.api.queues.QueueType;
import us.ajg0702.queue.common.players.QueuePlayerImpl;
import us.ajg0702.queue.common.utils.Debug;
import us.ajg0702.utils.common.Config;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;

/**
 * Redis-backed {@link QueueHolder} implementation.
 *
 * <p>Each queue server gets two Redis lists:
 * {@code ajqueue:queue:<name>:standard} and {@code ajqueue:queue:<name>:express}.
 * Players are stored as pipe-delimited strings (see {@link #serialize} / {@link #deserialize}).
 *
 * <p>The Lettuce client and connection pool are shared across all instances via static state
 * and initialised lazily on the first constructor call.
 */
public class RedisQueueHolder extends QueueHolder {

    private static RedisClient redisClient;
    private static GenericObjectPool<StatefulRedisConnection<String, String>> connectionPool;

    /**
     * Initialises the shared {@link RedisClient} and connection pool from plugin config.
     * No-ops if already initialised. Safe to call from multiple threads.
     */
    private static synchronized void ensureClient() {
        if (redisClient != null && connectionPool != null) return;

        Config config = AjQueueAPI.getInstance().getConfig();
        String host     = config.getString("redis-host");
        int    port     = config.getInt("redis-port");
        String password = config.getString("redis-password");
        int    database = config.getInt("redis-database");

        AjQueueAPI.getInstance().getLogger().info(
                "[redis] Connecting to Redis at " + host + ":" + port + " (database " + database + ")"
        );

        RedisURI.Builder uriBuilder = RedisURI.builder()
                .withHost(host)
                .withPort(port)
                .withDatabase(database)
                .withTimeout(Duration.ofSeconds(5));
        if (password != null && !password.isEmpty()) {
            uriBuilder.withPassword(password.toCharArray());
        }

        redisClient = RedisClient.create(uriBuilder.build());

        GenericObjectPoolConfig<StatefulRedisConnection<String, String>> poolConfig =
                new GenericObjectPoolConfig<>();
        poolConfig.setMaxTotal(10);
        poolConfig.setMaxIdle(5);
        poolConfig.setMinIdle(1);
        // Avoid blocking the caller thread on every borrow with a ping round-trip.
        poolConfig.setTestOnBorrow(false);
        poolConfig.setTestOnReturn(false);
        // Fail fast when the pool is exhausted rather than queuing callers indefinitely.
        poolConfig.setMaxWait(Duration.ofSeconds(5));

        connectionPool = ConnectionPoolSupport.createGenericObjectPool(
                () -> redisClient.connect(),
                poolConfig,
                true   // wrapConnections=true: close() returns the connection to the pool
        );

        AjQueueAPI.getInstance().getLogger().info("[redis] Redis connection pool initialised");
    }

    @Override
    public boolean isPersistent() {
        return true;
    }

    @Override
    public void onShutdown() {
        closeClient();
    }

    /**
     * Shuts down the shared connection pool and Redis client.
     * Safe to call multiple times; subsequent calls are no-ops.
     */
    public static synchronized void closeClient() {
        if (connectionPool != null) { connectionPool.close(); connectionPool = null; }
        if (redisClient != null)    { redisClient.shutdown(); redisClient = null; }
    }

    private final QueueServer queueServer;
    private final String standardKey;
    private final String expressKey;
    private final String lastSentKey;

    /**
     * Creates a holder backed by Redis for the given queue server.
     * The Redis client is initialised on first use if it has not been already.
     *
     * @param queueServer the server whose queue this holder manages
     */
    public RedisQueueHolder(QueueServer queueServer) {
        super(queueServer);
        this.queueServer = queueServer;
        String safeName = queueServer.getName().replace(":", "_");
        this.standardKey = "ajqueue:queue:" + safeName + ":standard";
        this.expressKey  = "ajqueue:queue:" + safeName + ":express";
        this.lastSentKey = "ajqueue:queue:" + safeName + ":lastSent";
        ensureClient();
        Debug.info("[redis] RedisQueueHolder created for " + queueServer.getName()
                + " (standardKey=" + standardKey + ", expressKey=" + expressKey + ")");
    }

    /** Callback executed inside a borrowed Redis connection. */
    @FunctionalInterface
    private interface RedisAction<T> {
        T run(RedisCommands<String, String> commands) throws Exception;
    }

    /**
     * Borrows a connection from the pool, runs {@code action}, then returns the connection.
     *
     * @param opName label included in the exception message if the operation fails
     * @param action the Redis work to perform
     * @return whatever {@code action} returns
     * @throws RuntimeException wrapping any exception thrown by the action or pool
     */
    private <T> T withRedis(String opName, RedisAction<T> action) {
        try (StatefulRedisConnection<String, String> conn = connectionPool.borrowObject()) {
            return action.run(conn.sync());
        } catch (Exception e) {
            AjQueueAPI.getInstance().getLogger().warning(
                    "[redis] Operation failed [" + opName + "]: " + e.getMessage()
            );
            throw new RuntimeException("Redis operation failed [" + opName + "]", e);
        }
    }

    /** Field separator used in the pipe-delimited Redis value. */
    private static final String SEP = "|";

    /**
     * Encodes a {@link QueuePlayer} as a pipe-delimited string.
     * Format: {@code uuid|name|queueType|priority|maxOfflineTime|leaveTime}
     */
    private String serialize(QueuePlayer player) {
        long leaveTime = (player instanceof QueuePlayerImpl)
                ? ((QueuePlayerImpl) player).getLeaveTime()
                : 0L;
        return player.getUniqueId().toString()
                + SEP + player.getName()
                + SEP + player.getQueueType().name()
                + SEP + player.getPriority()
                + SEP + player.getMaxOfflineTime()
                + SEP + leaveTime;
    }

    /**
     * Reconstructs a {@link QueuePlayer} from a serialized Redis value.
     * Returns {@code null} if the value is malformed or the UUID is invalid.
     * If the player is currently online on this proxy, their {@link AdaptedPlayer} is attached.
     */
    private QueuePlayer deserialize(String raw) {
        if (raw == null) return null;
        String[] parts = raw.split("\\|", 6);
        if (parts.length < 5) {
            Debug.info("[redis] deserialize: malformed entry (only " + parts.length + " fields): " + raw);
            return null;
        }

        UUID uuid;
        try { uuid = UUID.fromString(parts[0]); } catch (IllegalArgumentException e) {
            Debug.info("[redis] deserialize: invalid UUID in entry: " + raw);
            return null;
        }

        String name = parts[1];

        QueueType queueType;
        try { queueType = QueueType.valueOf(parts[2]); } catch (IllegalArgumentException e) {
            Debug.info("[redis] deserialize: unknown QueueType '" + parts[2] + "' for " + uuid + ", defaulting to STANDARD");
            queueType = QueueType.STANDARD;
        }

        int priority, maxOfflineTime;
        try {
            priority       = Integer.parseInt(parts[3]);
            maxOfflineTime = Integer.parseInt(parts[4]);
        } catch (NumberFormatException e) {
            Debug.info("[redis] deserialize: bad priority/maxOfflineTime for " + uuid);
            priority = 0; maxOfflineTime = 0;
        }

        long leaveTime = 0;
        if (parts.length >= 6) {
            try { leaveTime = Long.parseLong(parts[5]); } catch (NumberFormatException ignored) {}
        }

        QueuePlayerImpl qp = new QueuePlayerImpl(uuid, name, queueServer, priority, maxOfflineTime, queueType);
        if (leaveTime > 0) qp.restoreLeaveTime(leaveTime);

        AdaptedPlayer online = AjQueueAPI.getInstance().getPlatformMethods().getPlayer(uuid);
        if (online != null) {
            qp.setPlayer(online);
            Debug.info("[redis] deserialize: " + name + " (" + uuid + ") is online on this proxy");
        } else {
            Debug.info("[redis] deserialize: " + name + " (" + uuid + ") is NOT on this proxy"
                    + (leaveTime > 0 ? " (leaveTime=" + leaveTime + ")" : " (online on another proxy)"));
        }
        return qp;
    }

    @Override
    public String getIdentifier() { return "redis"; }

    @Override
    public long getSharedLastSendTimestamp() {
        return withRedis("getSharedLastSendTimestamp", cmd -> {
            String val = cmd.get(lastSentKey);
            if (val == null) return 0L;
            try { return Long.parseLong(val); } catch (NumberFormatException e) { return 0L; }
        });
    }

    @Override
    public void recordSharedSend(long timestamp) {
        Debug.info("[redis] recordSharedSend: " + queueServer.getName() + " lastSent=" + timestamp);
        withRedis("recordSharedSend", cmd -> {
            cmd.set(lastSentKey, String.valueOf(timestamp));
            return null;
        });
    }

    @Override
    public void addPlayer(QueuePlayer player) {
        String key = player.isInStandardQueue() ? standardKey : expressKey;
        String serialized = serialize(player);
        Debug.info("[redis] addPlayer: " + player.getName() + " (" + player.getUniqueId()
                + ") -> key=" + key + " value=" + serialized);
        withRedis("addPlayer:" + player.getUniqueId(), cmd -> cmd.rpush(key, serialized));
        Debug.info("[redis] addPlayer: done for " + player.getName());
    }

    /**
     * Inserts {@code player} at the given 1-based {@code position} in the queue.
     * The position is clamped to {@code [1, size+1]}, so passing a value larger than
     * the current size is equivalent to appending.
     */
    @Override
    public void addPlayer(QueuePlayer player, int position) {
        String key = player.isInStandardQueue() ? standardKey : expressKey;
        String serialized = serialize(player);
        Debug.info("[redis] addPlayer[pos=" + position + "]: " + player.getName()
                + " (" + player.getUniqueId() + ") -> key=" + key);
        withRedis("addPlayer[pos=" + position + "]:" + player.getUniqueId(), cmd -> {
            List<String> current = new ArrayList<>(cmd.lrange(key, 0, -1));
            int insertAt = Math.max(0, Math.min(position - 1, current.size()));
            current.add(insertAt, serialized);
            rebuildList(cmd, key, current);
            return null;
        });
        Debug.info("[redis] addPlayer[pos=" + position + "]: done for " + player.getName());
    }

    @Override
    public void removePlayer(QueuePlayer player) {
        String key = player.isInStandardQueue() ? standardKey : expressKey;
        Debug.info("[redis] removePlayer: " + player.getName() + " (" + player.getUniqueId()
                + ") from key=" + key);
        withRedis("removePlayer:" + player.getUniqueId(),
                cmd -> { removeByUuid(cmd, key, player.getUniqueId()); return null; });
        Debug.info("[redis] removePlayer: done for " + player.getName());
    }

    /**
     * Persists the player's current state (including {@code leaveTime}) back into Redis
     * so that offline-queue tracking survives server restarts or cross-node reads.
     */
    @Override
    public void onPlayerOffline(QueuePlayer player) {
        String key = player.isInStandardQueue() ? standardKey : expressKey;
        String uuidPrefix = player.getUniqueId().toString() + SEP;
        String serialized = serialize(player);
        long leaveTime = (player instanceof QueuePlayerImpl) ? ((QueuePlayerImpl) player).getLeaveTime() : 0L;
        Debug.info("[redis] onPlayerOffline: " + player.getName() + " (" + player.getUniqueId()
                + ") leaveTime=" + leaveTime + " key=" + key);
        withRedis("onPlayerOffline:" + player.getUniqueId(), cmd -> {
            List<String> list = cmd.lrange(key, 0, -1);
            for (int i = 0; i < list.size(); i++) {
                if (list.get(i).startsWith(uuidPrefix)) {
                    cmd.lset(key, i, serialized);
                    Debug.info("[redis] onPlayerOffline: updated entry at index " + i + " for " + player.getName());
                    return null;
                }
            }
            Debug.info("[redis] onPlayerOffline: player " + player.getName() + " not found in Redis key=" + key);
            return null;
        });
    }

    @Override
    public QueuePlayer findPlayer(UUID uuid) {
        Debug.info("[redis] findPlayer(uuid): " + uuid + " in " + queueServer.getName());
        return withRedis("findPlayer:uuid:" + uuid, cmd -> {
            QueuePlayer p = findInList(cmd, standardKey, uuid);
            if (p != null) {
                Debug.info("[redis] findPlayer(uuid): found " + p.getName() + " in standard queue");
                return p;
            }
            p = findInList(cmd, expressKey, uuid);
            if (p != null) {
                Debug.info("[redis] findPlayer(uuid): found " + p.getName() + " in express queue");
            } else {
                Debug.info("[redis] findPlayer(uuid): " + uuid + " NOT found in " + queueServer.getName());
            }
            return p;
        });
    }

    @Override
    public QueuePlayer findPlayer(String name) {
        Debug.info("[redis] findPlayer(name): " + name + " in " + queueServer.getName());
        return withRedis("findPlayer:name:" + name, cmd -> {
            QueuePlayer p = findInListByName(cmd, standardKey, name);
            if (p != null) {
                Debug.info("[redis] findPlayer(name): found " + name + " in standard queue");
                return p;
            }
            p = findInListByName(cmd, expressKey, name);
            if (p != null) {
                Debug.info("[redis] findPlayer(name): found " + name + " in express queue");
            } else {
                Debug.info("[redis] findPlayer(name): " + name + " NOT found in " + queueServer.getName());
            }
            return p;
        });
    }

    /**
     * Returns the 1-based position of {@code player} in their queue, or {@code -1} if not found.
     *
     * <p>Uses a raw-string prefix scan instead of full deserialization to avoid re-entering
     * {@link #withRedis} (which would deadlock by trying to borrow a second connection from
     * the same thread).
     */
    @Override
    public int getPosition(QueuePlayer player) {
        String key = player.isInStandardQueue() ? standardKey : expressKey;
        String uuidPrefix = player.getUniqueId().toString() + SEP;
        return withRedis("getPosition:" + player.getUniqueId(), cmd -> {
            List<String> list = cmd.lrange(key, 0, -1);
            for (int i = 0; i < list.size(); i++) {
                if (list.get(i).startsWith(uuidPrefix)) {
                    Debug.info("[redis] getPosition: " + player.getName() + " is at position " + (i + 1)
                            + " in key=" + key);
                    return i + 1;
                }
            }
            Debug.info("[redis] getPosition: " + player.getName() + " NOT found in key=" + key);
            return -1;
        });
    }

    @Override
    public int getStandardQueueSize() {
        return withRedis("getStandardQueueSize",
                cmd -> Math.toIntExact(cmd.llen(standardKey)));
    }

    @Override
    public int getExpressQueueSize() {
        return withRedis("getExpressQueueSize",
                cmd -> Math.toIntExact(cmd.llen(expressKey)));
    }

    @Override
    public int getTotalQueueSize() {
        return withRedis("getTotalQueueSize",
                cmd -> Math.toIntExact(cmd.llen(standardKey) + cmd.llen(expressKey)));
    }

    @Override
    public int getTotalOnlineQueueSize() {
        Predicate<QueuePlayer> isOnline = p -> p.getPlayer() != null && p.getPlayer().isConnected();
        return withRedis("getTotalOnlineQueueSize", cmd -> {
            long count = cmd.lrange(standardKey, 0, -1).stream()
                    .map(this::deserialize).filter(p -> p != null && isOnline.test(p)).count();
            count += cmd.lrange(expressKey, 0, -1).stream()
                    .map(this::deserialize).filter(p -> p != null && isOnline.test(p)).count();
            Debug.info("[redis] getTotalOnlineQueueSize for " + queueServer.getName() + ": " + count
                    + " (total in Redis: " + (cmd.llen(standardKey) + cmd.llen(expressKey)) + ")");
            return Math.toIntExact(count);
        });
    }

    @Override
    public List<QueuePlayer> getAllStandardPlayers() {
        return withRedis("getAllStandardPlayers", cmd -> {
            List<String> raw = cmd.lrange(standardKey, 0, -1);
            Debug.info("[redis] getAllStandardPlayers for " + queueServer.getName()
                    + ": " + raw.size() + " entries in Redis");
            return deserializeList(raw);
        });
    }

    @Override
    public List<QueuePlayer> getAllExpressPlayers() {
        return withRedis("getAllExpressPlayers", cmd -> {
            List<String> raw = cmd.lrange(expressKey, 0, -1);
            Debug.info("[redis] getAllExpressPlayers for " + queueServer.getName()
                    + ": " + raw.size() + " entries in Redis");
            return deserializeList(raw);
        });
    }

    /**
     * Deserializes a raw Redis list into an immutable {@link QueuePlayer} list.
     * Each player's {@code lastPosition} is set to their index so that
     * {@code positionChange()} comparisons work without an extra Redis round-trip.
     */
    private List<QueuePlayer> deserializeList(List<String> raw) {
        List<QueuePlayer> result = new ArrayList<>(raw.size());
        for (int i = 0; i < raw.size(); i++) {
            QueuePlayer p = deserialize(raw.get(i));
            if (p != null) {
                ((QueuePlayerImpl) p).lastPosition = i + 1;
                result.add(p);
            }
        }
        return ImmutableList.copyOf(result);
    }

    /** Scans {@code key} and returns the first entry whose UUID matches, or {@code null}. */
    private QueuePlayer findInList(RedisCommands<String, String> cmd, String key, UUID uuid) {
        for (String raw : cmd.lrange(key, 0, -1)) {
            QueuePlayer p = deserialize(raw);
            if (p != null && p.getUniqueId().equals(uuid)) return p;
        }
        return null;
    }

    /** Scans {@code key} and returns the first entry whose name matches (case-insensitive), or {@code null}. */
    private QueuePlayer findInListByName(RedisCommands<String, String> cmd, String key, String name) {
        for (String raw : cmd.lrange(key, 0, -1)) {
            QueuePlayer p = deserialize(raw);
            if (p != null && p.getName().equalsIgnoreCase(name)) return p;
        }
        return null;
    }

    /** Removes all entries in {@code key} whose UUID matches. */
    private void removeByUuid(RedisCommands<String, String> cmd, String key, UUID uuid) {
        boolean removed = false;
        for (String raw : cmd.lrange(key, 0, -1)) {
            QueuePlayer p = deserialize(raw);
            if (p != null && p.getUniqueId().equals(uuid)) {
                cmd.lrem(key, 0, raw);
                removed = true;
            }
        }
        if (!removed) {
            Debug.info("[redis] removeByUuid: " + uuid + " not found in key=" + key);
        }
    }

    /**
     * Replaces the Redis list at {@code key} with {@code items} atomically via DEL + RPUSH.
     * Used when an in-place index operation (e.g. positional insert) isn't possible.
     */
    private void rebuildList(RedisCommands<String, String> cmd, String key, List<String> items) {
        cmd.del(key);
        if (!items.isEmpty()) cmd.rpush(key, items.toArray(new String[0]));
    }
}
