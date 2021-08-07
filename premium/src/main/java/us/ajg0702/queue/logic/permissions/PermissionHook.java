package us.ajg0702.queue.logic.permissions;

import us.ajg0702.queue.api.players.AdaptedPlayer;

import java.util.List;

public interface PermissionHook {
    String getName();
    boolean canUse();
    List<String> getPermissions(AdaptedPlayer player);
}
