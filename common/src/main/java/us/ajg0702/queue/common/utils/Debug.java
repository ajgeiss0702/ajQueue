package us.ajg0702.queue.common.utils;

import us.ajg0702.queue.api.AjQueueAPI;

public class Debug {
    public static void info(String message) {
        AjQueueAPI api = AjQueueAPI.getInstance();
        if(!api.getConfig().getBoolean("debug")) return;
        api.getLogger().info("[debug] "+message);
    }
}
