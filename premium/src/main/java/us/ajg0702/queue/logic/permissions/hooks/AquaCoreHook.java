package us.ajg0702.queue.logic.permissions.hooks;

import me.activated.core.plugin.AquaCoreAPI;
import us.ajg0702.queue.api.players.AdaptedPlayer;
import us.ajg0702.queue.common.QueueMain;
import us.ajg0702.queue.api.premium.PermissionHook;

import java.util.ArrayList;
import java.util.List;

public class AquaCoreHook implements PermissionHook {

    private final QueueMain main;
    public AquaCoreHook(QueueMain main) {
        this.main = main;
    }

    @Override
    public String getName() {
        return "AquaCore";
    }

    @Override
    public boolean canUse() {
        if(!main.getPlatformMethods().hasPlugin("AquaProxy") ) return false;
        try {
            if(AquaCoreAPI.INSTANCE == null) {
                main.getLogger().warn("AquaCore is installed, but its INSTANCE returned null! Unable to hook into it.");
                return false;
            }
        } catch(NoClassDefFoundError e) {
            main.getLogger().warning("AquaCore seems to be installed, but its api doesnt seem to be!");
            return false;
        }
        return true;
    }

    @Override
    public List<String> getPermissions(AdaptedPlayer player) {
        AquaCoreAPI api = AquaCoreAPI.INSTANCE;

        List<String> permissions = new ArrayList<>();

        api.getPlayerData(player.getUniqueId()).getActiveGrants().forEach(grant -> {
            if(!grant.isActiveSomewhere() || grant.hasExpired()) return;
            permissions.addAll(grant.getRank().getAvailablePermissions());
        });

        return permissions;
    }
}
