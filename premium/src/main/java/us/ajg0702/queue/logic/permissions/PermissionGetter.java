package us.ajg0702.queue.logic.permissions;

import us.ajg0702.queue.api.players.AdaptedPlayer;
import us.ajg0702.queue.common.QueueMain;
import us.ajg0702.queue.logic.permissions.hooks.BuiltIn;
import us.ajg0702.queue.logic.permissions.hooks.LuckPermsHook;

import java.util.*;

public class PermissionGetter {

    private final List<PermissionHook> hooks;

    private QueueMain main;
    public PermissionGetter(QueueMain main) {
        hooks = Arrays.asList(
                new BuiltIn(main),
                new LuckPermsHook(main)
        );
    }

    private PermissionHook selected;
    public PermissionHook getSelected() {
        if(selected != null) return selected;
        if(hooks == null) {
            throw new IllegalStateException("Hooks are not initialized yet!");
        }
        for(PermissionHook hook : hooks) {
            if(hook.canUse()) {
                selected = hook;
            }
        }
        return selected;
    }

    public int getMaxOfflineTime(AdaptedPlayer player) {
        return getHighestPermission(player, "ajqueue.stayqueued.");
    }

    public int getPriority(AdaptedPlayer player) {
        return getHighestPermission(player, "ajqueue.priority.");
    }

    public int getServerPriotity(String server, AdaptedPlayer player) {
        return getHighestPermission(player, "ajqueue.serverpriority."+server+".");
    }

    private int getHighestPermission(AdaptedPlayer player, String prefix) {
        if(getSelected() == null) {
            return -1;
        }
        List<String> perms = getSelected().getPermissions(player);
        Iterator<String> it = perms.iterator();
        String highestPerm = prefix+"0";
        while(it.hasNext()) {
            String perm = it.next();
            if(!perm.startsWith(prefix)) continue;
            if(highestPerm.isEmpty()) {
                highestPerm = perm;
                continue;
            }
            int level = Integer.parseInt(perm.substring(prefix.length()));
            int highestlevel = Integer.parseInt(highestPerm.substring(prefix.length()));
            if(level > highestlevel) {
                highestPerm = perm;
            }
        }
        return Integer.parseInt(highestPerm.substring(prefix.length()));
    }
}
