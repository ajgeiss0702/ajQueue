package us.ajg0702.queue.logic.permissions;

import us.ajg0702.queue.api.players.AdaptedPlayer;
import us.ajg0702.queue.api.premium.PermissionGetter;
import us.ajg0702.queue.api.premium.PermissionHook;
import us.ajg0702.queue.common.QueueMain;
import us.ajg0702.queue.logic.permissions.hooks.AquaCoreHook;
import us.ajg0702.queue.logic.permissions.hooks.BuiltInHook;
import us.ajg0702.queue.logic.permissions.hooks.LuckPermsHook;
import us.ajg0702.queue.logic.permissions.hooks.UltraPermissionsHook;

import java.util.*;

public class PermissionGetterImpl implements PermissionGetter {

    private final List<PermissionHook> hooks;

    private final QueueMain main;
    public PermissionGetterImpl(QueueMain main) {
        hooks = Arrays.asList(
                new BuiltInHook(main),
                new LuckPermsHook(main),
                new UltraPermissionsHook(main),
                new AquaCoreHook(main)
        );
        this.main = main;
    }

    private PermissionHook selected;
    @Override
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
        if(selected == null) {
            throw new IllegalStateException("All hooks are unusable!");
        }
        main.getLogger().info("Using "+selected.getName()+" for permissions.");
        return selected;
    }

    @Override
    public int getMaxOfflineTime(AdaptedPlayer player) {
        return getHighestPermission(player, "ajqueue.stayqueued.");
    }

    @Override
    public int getPriority(AdaptedPlayer player) {
        return getHighestPermission(player, "ajqueue.priority.");
    }

    @Override
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
