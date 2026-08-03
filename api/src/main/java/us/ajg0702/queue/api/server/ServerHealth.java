package us.ajg0702.queue.api.server;

/**
 * The health of an online server.
 * Healthy = acceptable mspt/tps (if available) and ping latency
 * Little Slow = Something was a little off (slightly high mspt/latency or slightly low tps)
 * Unhealthy = Something is majorly wrong (very high mspt/latency or very low tps)
 */
public enum ServerHealth {
    HEALTHY,
    LITTLE_SLOW,
    UNHEALTHY
}
