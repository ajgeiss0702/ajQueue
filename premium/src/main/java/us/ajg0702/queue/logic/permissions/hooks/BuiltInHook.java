package us.ajg0702.queue.logic.permissions.hooks;

import us.ajg0702.queue.api.players.AdaptedPlayer;
import us.ajg0702.queue.common.QueueMain;
import us.ajg0702.queue.api.premium.PermissionHook;

import java.util.ArrayList;
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
        if(main.getPlatformMethods().getImplementationName().equals("velocity")) {
            return new ArrayList<>();
        }
        return player.getPermissions();
    }
}
