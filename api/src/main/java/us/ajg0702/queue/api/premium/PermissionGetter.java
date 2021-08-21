package us.ajg0702.queue.api.premium;

import us.ajg0702.queue.api.players.AdaptedPlayer;
import us.ajg0702.queue.api.premium.PermissionHook;

public interface PermissionGetter {
    PermissionHook getSelected();

    int getMaxOfflineTime(AdaptedPlayer player);

    int getPriority(AdaptedPlayer player);

    int getServerPriotity(String server, AdaptedPlayer player);
}
