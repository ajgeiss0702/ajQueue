package us.ajg0702.queue.common.utils;

import us.ajg0702.queue.api.AjQueueAPI;

public class Debugger {
    public static void debug(String message) {
        AjQueueAPI api = AjQueueAPI.getInstance();
        if(!api.getConfig().getBoolean("debug")) return;
        api.getLogger().info("[debug] "+message);
    }
}
