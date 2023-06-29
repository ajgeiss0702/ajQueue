package us.ajg0702.queue.logic.permissions.hooks;

import us.ajg0702.queue.api.players.AdaptedPlayer;
import us.ajg0702.queue.common.QueueMain;
import us.ajg0702.queue.api.premium.PermissionHook;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class BuiltInHook implements PermissionHook {

    private final QueueMain main;
    public BuiltInHook(QueueMain main) {
        this.main = main;
    }

    @Override
    public String getName() {
        return "Built-In";
    }

    @Override
    public boolean canUse() {
        return true;
    }

    @Override
    public List<String> getPermissions(AdaptedPlayer player) {
        if(main.getConfig().getBoolean("plus-level-fallback")) {
            List<String> hasPermissions = new ArrayList<>();
            for (String fallbackPermission : fallbackPermissions) {
                if(player.hasPermission(fallbackPermission)) {
                    hasPermissions.add(fallbackPermission);
                }
            }
            if(!main.getPlatformMethods().getImplementationName().equals("velocity")) {
                hasPermissions.addAll(player.getPermissions());
            }
            return hasPermissions;
        }


        if(main.getPlatformMethods().getImplementationName().equals("velocity")) {
            return Collections.emptyList();
        }

        return player.getPermissions();
    }

    private final List<String> fallbackPermissions = Arrays.asList(
            "ajqueue.priority.1",
            "ajqueue.priority.2",
            "ajqueue.priority.3",
            "ajqueue.priority.4",
            "ajqueue.priority.5",
            "ajqueue.priority.6",
            "ajqueue.priority.7",
            "ajqueue.priority.8",
            "ajqueue.priority.9",
            "ajqueue.priority.10",
            "ajqueue.stayqueued.15",
            "ajqueue.stayqueued.30",
            "ajqueue.stayqueued.60",
            "ajqueue.stayqueued.120"
    );
}
