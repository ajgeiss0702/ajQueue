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
import us.ajg0702.utils.common.Config;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.logging.Logger;
/**
 * A QueueHolder that stores queued players in Redis using Lettuce, enabling cross-proxy queue syncing.
 *
 * Configure via config.yml:
 *   queue-holder: redis
 *   redis:
 *     host: localhost
 *     port: 6379
 *     password: ""
 *     database: 0
 */
public class RedisQueueHolder extends QueueHolder {
    private static final Logger log = Logger.getLogger("ajQueue/RedisQueueHolder");
    private static RedisClient redisClient;
    private static GenericObjectPool<StatefulRedisConnection<String, String>> connectionPool;
    private final String standardKey;
    private final String expressKey;
    private final QueueServer queueServer;
    public RedisQueueHolder(QueueServer queueServer) {
        super(queueServer);
        log.info("[RedisQueueHolder] Constructor called for server: " + queueServer.getName());
        this.queueServer = queueServer;
        String safeName = queueServer.getName().replace(":", "_");
        this.standardKey = "ajqueue:queue:" + safeName + ":standard";
        this.expressKey  = "ajqueue:queue:" + safeName + ":express";
        log.info("[RedisQueueHolder] Keys set — standard=" + standardKey + " express=" + expressKey);
        ensureClient();
        log.info("[RedisQueueHolder] Ready for server: " + queueServer.getName());
    }
    private static synchronized void ensureClient() {
        log.info("[RedisQueueHolder] ensureClient() called");
        if (redisClient != null && connectionPool != null) {
            log.info("[RedisQueueHolder] Client already initialized, skipping.");
            return;
        }
        log.info("[RedisQueueHolder] Reading Redis config...");
        Config config = AjQueueAPI.getInstance().getConfig();
        String host     = config.getString("redis.host");
        int    port     = config.getInt("redis.port");
        String password = config.getString("redis.password");
        int    database = config.getInt("redis.database");
        log.info("[RedisQueueHolder] Connecting to Redis at " + host + ":" + port + " db=" + database);
        RedisURI.Builder uriBuilder = RedisURI.builder()
                .withHost(host)
                .withPort(port)
                .withDatabase(database)
                .withTimeout(Duration.ofSeconds(5));
        if (password != null && !password.isEmpty()) {
            uriBuilder.withPassword(password.toCharArray());
        }
        log.info("[RedisQueueHolder] Creating RedisClient...");
        redisClient = RedisClient.create(uriBuilder.build());
        GenericObjectPoolConfig<StatefulRedisConnection<String, String>> poolConfig =
                new GenericObjectPoolConfig<>();
        poolConfig.setMaxTotal(10);
        poolConfig.setMaxIdle(5);
        poolConfig.setMinIdle(1);
        // Never ping on borrow — avoids blocking the caller thread every time
        poolConfig.setTestOnBorrow(false);
        poolConfig.setTestOnReturn(false);
        // Fail fast if pool is exhausted rather than blocking forever
        poolConfig.setMaxWait(Duration.ofSeconds(5));
        log.info("[RedisQueueHolder] Creating connection pool...");
        connectionPool = ConnectionPoolSupport.createGenericObjectPool(
                () -> redisClient.connect(),
                poolConfig,
                true   // wrapConnections=true: close() returns to pool instead of destroying
        );
        log.info("[RedisQueueHolder] Redis connection pool ready (" + host + ":" + port + ")");
    }
    // -----------------------------------------------------------------------
    // Redis execution — runs on the dedicated redis thread, never the main thread
    // -----------------------------------------------------------------------
    @FunctionalInterface
    private interface RedisAction<T> {
        T run(RedisCommands<String, String> commands) throws Exception;
    }
    private <T> T withRedis(String opName, RedisAction<T> action) {
        log.fine("[RedisQueueHolder] start op: " + opName + " (thread=" + Thread.currentThread().getName() + ")");
        long t0 = System.currentTimeMillis();
        try (StatefulRedisConnection<String, String> conn = connectionPool.borrowObject()) {
            log.fine("[RedisQueueHolder] connection borrowed for: " + opName);
            T result = action.run(conn.sync());
            log.fine("[RedisQueueHolder] op done: " + opName + " (" + (System.currentTimeMillis() - t0) + "ms)");
            return result;
        } catch (Exception e) {
            log.warning("[RedisQueueHolder] op FAILED: " + opName + " (" + (System.currentTimeMillis() - t0) + "ms) — " + e);
            throw new RuntimeException("Redis operation failed [" + opName + "]", e);
        }
    }
    // -----------------------------------------------------------------------
    // Serialization: uuid|name|queueType|priority|maxOfflineTime
    // -----------------------------------------------------------------------
    private static final String SEP = "|";
    private String serialize(QueuePlayer player) {
        return player.getUniqueId().toString()
                + SEP + player.getName()
                + SEP + player.getQueueType().name()
                + SEP + player.getPriority()
                + SEP + player.getMaxOfflineTime();
    }
    private QueuePlayer deserialize(String raw) {
        if (raw == null) return null;
        String[] parts = raw.split("\\|", 5);
        if (parts.length < 5) return null;
        UUID uuid;
        try { uuid = UUID.fromString(parts[0]); } catch (IllegalArgumentException e) { return null; }
        String name = parts[1];
        QueueType queueType;
        try { queueType = QueueType.valueOf(parts[2]); } catch (IllegalArgumentException e) { queueType = QueueType.STANDARD; }
        int priority, maxOfflineTime;
        try {
            priority       = Integer.parseInt(parts[3]);
            maxOfflineTime = Integer.parseInt(parts[4]);
        } catch (NumberFormatException e) { priority = 0; maxOfflineTime = 0; }
        QueuePlayerImpl qp = new QueuePlayerImpl(uuid, name, queueServer, priority, maxOfflineTime, queueType);
        AdaptedPlayer online = AjQueueAPI.getInstance().getPlatformMethods().getPlayer(uuid);
        if (online != null) qp.setPlayer(online);
        return qp;
    }
    // -----------------------------------------------------------------------
    // QueueHolder implementation
    // -----------------------------------------------------------------------
    @Override public String getIdentifier() { return "redis"; }
    @Override
    public void addPlayer(QueuePlayer player) {
        String key = player.isInStandardQueue() ? standardKey : expressKey;
        withRedis("addPlayer:" + player.getName(), cmd -> cmd.rpush(key, serialize(player)));
    }
    @Override
    public void addPlayer(QueuePlayer player, int position) {
        String key = player.isInStandardQueue() ? standardKey : expressKey;
        withRedis("addPlayer[pos=" + position + "]:" + player.getName(), cmd -> {
            List<String> current = new ArrayList<>(cmd.lrange(key, 0, -1));
            int insertAt = Math.max(0, Math.min(position - 1, current.size()));
            current.add(insertAt, serialize(player));
            rebuildList(cmd, key, current);
            return null;
        });
    }
    @Override
    public void removePlayer(QueuePlayer player) {
        String key = player.isInStandardQueue() ? standardKey : expressKey;
        withRedis("removePlayer:" + player.getName(),
                cmd -> { removeByUuid(cmd, key, player.getUniqueId()); return null; });
    }
    @Override
    public QueuePlayer findPlayer(UUID uuid) {
        return withRedis("findPlayer:uuid:" + uuid, cmd -> {
            QueuePlayer p = findInList(cmd, standardKey, uuid);
            if (p != null) return p;
            return findInList(cmd, expressKey, uuid);
        });
    }
    @Override
    public QueuePlayer findPlayer(String name) {
        return withRedis("findPlayer:name:" + name, cmd -> {
            QueuePlayer p = findInListByName(cmd, standardKey, name);
            if (p != null) return p;
            return findInListByName(cmd, expressKey, name);
        });
    }
    @Override
    public int getStandardQueueSize() {
        return withRedis("getStandardQueueSize:" + standardKey,
                cmd -> Math.toIntExact(cmd.llen(standardKey)));
    }
    @Override
    public int getExpressQueueSize() {
        return withRedis("getExpressQueueSize:" + expressKey,
                cmd -> Math.toIntExact(cmd.llen(expressKey)));
    }
    @Override
    public int getTotalQueueSize() {
        return withRedis("getTotalQueueSize",
                cmd -> Math.toIntExact(cmd.llen(standardKey) + cmd.llen(expressKey)));
    }
    @Override
    public int getTotalOnlineQueueSize() {
        Predicate<QueuePlayer> online = p -> p.getPlayer() != null && p.getPlayer().isConnected();
        return withRedis("getTotalOnlineQueueSize", cmd -> {
            long count = cmd.lrange(standardKey, 0, -1).stream()
                    .map(this::deserialize).filter(p -> p != null && online.test(p)).count();
            count += cmd.lrange(expressKey, 0, -1).stream()
                    .map(this::deserialize).filter(p -> p != null && online.test(p)).count();
            return Math.toIntExact(count);
        });
    }
    @Override
    public int getPosition(QueuePlayer player) {
        String key = player.isInStandardQueue() ? standardKey : expressKey;
        return withRedis("getPosition:" + player.getName(), cmd -> {
            List<String> list = cmd.lrange(key, 0, -1);
            for (int i = 0; i < list.size(); i++) {
                QueuePlayer p = deserialize(list.get(i));
                if (p != null && p.getUniqueId().equals(player.getUniqueId())) return i + 1;
            }
            return -1;
        });
    }
    @Override
    public List<QueuePlayer> getAllStandardPlayers() {
        return withRedis("getAllStandardPlayers",
                cmd -> deserializeList(cmd.lrange(standardKey, 0, -1)));
    }
    @Override
    public List<QueuePlayer> getAllExpressPlayers() {
        return withRedis("getAllExpressPlayers",
                cmd -> deserializeList(cmd.lrange(expressKey, 0, -1)));
    }
    // -----------------------------------------------------------------------
    // Private helpers
    // -----------------------------------------------------------------------
    private List<QueuePlayer> deserializeList(List<String> raw) {
        List<QueuePlayer> result = new ArrayList<>(raw.size());
        for (int i = 0; i < raw.size(); i++) {
            QueuePlayer p = deserialize(raw.get(i));
            if (p != null) {
                // Set lastPosition to the known 1-based position so that positionChange()
                // comparisons are accurate without requiring another Redis round-trip.
                ((QueuePlayerImpl) p).lastPosition = i + 1;
                result.add(p);
            }
        }
        return ImmutableList.copyOf(result);
    }
    private QueuePlayer findInList(RedisCommands<String, String> cmd, String key, UUID uuid) {
        for (String raw : cmd.lrange(key, 0, -1)) {
            QueuePlayer p = deserialize(raw);
            if (p != null && p.getUniqueId().equals(uuid)) return p;
        }
        return null;
    }
    private QueuePlayer findInListByName(RedisCommands<String, String> cmd, String key, String name) {
        for (String raw : cmd.lrange(key, 0, -1)) {
            QueuePlayer p = deserialize(raw);
            if (p != null && p.getName().equalsIgnoreCase(name)) return p;
        }
        return null;
    }
    private void removeByUuid(RedisCommands<String, String> cmd, String key, UUID uuid) {
        for (String raw : cmd.lrange(key, 0, -1)) {
            QueuePlayer p = deserialize(raw);
            if (p != null && p.getUniqueId().equals(uuid)) {
                cmd.lrem(key, 0, raw);
            }
        }
    }
    private void rebuildList(RedisCommands<String, String> cmd, String key, List<String> items) {
        cmd.del(key);
        if (!items.isEmpty()) cmd.rpush(key, items.toArray(new String[0]));
    }
    /**
     * Closes the Lettuce client and connection pool. Called on plugin shutdown.
     */
    public static synchronized void closeClient() {
        log.info("[RedisQueueHolder] Shutting down...");
        if (connectionPool != null) { connectionPool.close(); connectionPool = null; }
        if (redisClient != null) { redisClient.shutdown(); redisClient = null; }
        log.info("[RedisQueueHolder] Shutdown complete.");
    }
}
