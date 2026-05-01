package us.ajg0702.queue.common;
import com.google.common.collect.ImmutableList;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import us.ajg0702.queue.api.AjQueueAPI;
import us.ajg0702.queue.api.players.AdaptedPlayer;
import us.ajg0702.queue.api.players.QueuePlayer;
import us.ajg0702.queue.api.queueholders.QueueHolder;
import us.ajg0702.queue.api.queues.QueueServer;
import us.ajg0702.queue.api.queues.QueueType;
import us.ajg0702.queue.common.players.QueuePlayerImpl;
import us.ajg0702.utils.common.Config;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;
/**
 * A QueueHolder that stores queued players in Redis, enabling cross-proxy queue syncing.
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
    private static JedisPool pool;
    private final String standardKey;
    private final String expressKey;
    private final QueueServer queueServer;
    public RedisQueueHolder(QueueServer queueServer) {
        super(queueServer);
        this.queueServer = queueServer;
        String safeName = queueServer.getName().replace(":", "_");
        this.standardKey = "ajqueue:queue:" + safeName + ":standard";
        this.expressKey  = "ajqueue:queue:" + safeName + ":express";
        ensurePool();
    }
    private static synchronized void ensurePool() {
        if (pool != null && !pool.isClosed()) return;
        Config config = AjQueueAPI.getInstance().getConfig();
        String host     = config.getString("redis.host");
        int    port     = config.getInt("redis.port");
        String password = config.getString("redis.password");
        int    database = config.getInt("redis.database");
        JedisPoolConfig poolCfg = new JedisPoolConfig();
        poolCfg.setMaxTotal(10);
        poolCfg.setMaxIdle(5);
        poolCfg.setMinIdle(1);
        poolCfg.setTestOnBorrow(true);
        if (password == null || password.isEmpty()) {
            pool = new JedisPool(poolCfg, host, port, 2000, null, database);
        } else {
            pool = new JedisPool(poolCfg, host, port, 2000, password, database);
        }
    }
    // Serialization: uuid|name|queueType|priority|maxOfflineTime
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
    @Override
    public String getIdentifier() { return "redis"; }
    @Override
    public void addPlayer(QueuePlayer player) {
        String key = player.isInStandardQueue() ? standardKey : expressKey;
        try (Jedis jedis = pool.getResource()) { jedis.rpush(key, serialize(player)); }
    }
    @Override
    public void addPlayer(QueuePlayer player, int position) {
        String key = player.isInStandardQueue() ? standardKey : expressKey;
        String serialized = serialize(player);
        try (Jedis jedis = pool.getResource()) {
            List<String> current = jedis.lrange(key, 0, -1);
            int insertAt = Math.max(0, Math.min(position - 1, current.size()));
            current.add(insertAt, serialized);
            rebuildList(jedis, key, current);
        }
    }
    @Override
    public void removePlayer(QueuePlayer player) {
        String key = player.isInStandardQueue() ? standardKey : expressKey;
        try (Jedis jedis = pool.getResource()) {
            // Remove all entries with this UUID from the list
            removeByUuid(jedis, key, player.getUniqueId());
        }
    }
    @Override
    public QueuePlayer findPlayer(UUID uuid) {
        try (Jedis jedis = pool.getResource()) {
            QueuePlayer p = findInList(jedis, standardKey, uuid);
            if (p != null) return p;
            return findInList(jedis, expressKey, uuid);
        }
    }
    @Override
    public QueuePlayer findPlayer(String name) {
        try (Jedis jedis = pool.getResource()) {
            QueuePlayer p = findInListByName(jedis, standardKey, name);
            if (p != null) return p;
            return findInListByName(jedis, expressKey, name);
        }
    }
    @Override
    public int getStandardQueueSize() {
        try (Jedis jedis = pool.getResource()) { return Math.toIntExact(jedis.llen(standardKey)); }
    }
    @Override
    public int getExpressQueueSize() {
        try (Jedis jedis = pool.getResource()) { return Math.toIntExact(jedis.llen(expressKey)); }
    }
    @Override
    public int getTotalQueueSize() {
        try (Jedis jedis = pool.getResource()) { return Math.toIntExact(jedis.llen(standardKey) + jedis.llen(expressKey)); }
    }
    @Override
    public int getTotalOnlineQueueSize() {
        Predicate<QueuePlayer> online = p -> p.getPlayer() != null && p.getPlayer().isConnected();
        try (Jedis jedis = pool.getResource()) {
            long count = jedis.lrange(standardKey, 0, -1).stream()
                    .map(this::deserialize).filter(p -> p != null && online.test(p)).count();
            count += jedis.lrange(expressKey, 0, -1).stream()
                    .map(this::deserialize).filter(p -> p != null && online.test(p)).count();
            return Math.toIntExact(count);
        }
    }
    @Override
    public int getPosition(QueuePlayer player) {
        String key = player.isInStandardQueue() ? standardKey : expressKey;
        try (Jedis jedis = pool.getResource()) {
            List<String> list = jedis.lrange(key, 0, -1);
            for (int i = 0; i < list.size(); i++) {
                QueuePlayer p = deserialize(list.get(i));
                if (p != null && p.getUniqueId().equals(player.getUniqueId())) return i + 1;
            }
            return -1;
        }
    }
    @Override
    public List<QueuePlayer> getAllStandardPlayers() {
        try (Jedis jedis = pool.getResource()) { return deserializeList(jedis.lrange(standardKey, 0, -1)); }
    }
    @Override
    public List<QueuePlayer> getAllExpressPlayers() {
        try (Jedis jedis = pool.getResource()) { return deserializeList(jedis.lrange(expressKey, 0, -1)); }
    }
    private List<QueuePlayer> deserializeList(List<String> raw) {
        List<QueuePlayer> result = new ArrayList<>(raw.size());
        for (String s : raw) {
            QueuePlayer p = deserialize(s);
            if (p != null) result.add(p);
        }
        return ImmutableList.copyOf(result);
    }
    private QueuePlayer findInList(Jedis jedis, String key, UUID uuid) {
        for (String raw : jedis.lrange(key, 0, -1)) {
            QueuePlayer p = deserialize(raw);
            if (p != null && p.getUniqueId().equals(uuid)) return p;
        }
        return null;
    }
    private QueuePlayer findInListByName(Jedis jedis, String key, String name) {
        for (String raw : jedis.lrange(key, 0, -1)) {
            QueuePlayer p = deserialize(raw);
            if (p != null && p.getName().equalsIgnoreCase(name)) return p;
        }
        return null;
    }
    private void removeByUuid(Jedis jedis, String key, UUID uuid) {
        List<String> list = jedis.lrange(key, 0, -1);
        List<String> toRemove = new ArrayList<>();
        for (String raw : list) {
            QueuePlayer p = deserialize(raw);
            if (p != null && p.getUniqueId().equals(uuid)) toRemove.add(raw);
        }
        for (String raw : toRemove) jedis.lrem(key, 0, raw);
    }
    private void rebuildList(Jedis jedis, String key, List<String> items) {
        jedis.del(key);
        if (!items.isEmpty()) jedis.rpush(key, items.toArray(new String[0]));
    }
    public static synchronized void closePool() {
        if (pool != null && !pool.isClosed()) { pool.close(); pool = null; }
    }
}
