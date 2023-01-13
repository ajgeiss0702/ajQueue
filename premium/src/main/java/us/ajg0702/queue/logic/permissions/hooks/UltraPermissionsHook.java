package us.ajg0702.queue.logic.permissions.hooks;

import me.TechsCode.UltraPermissions.UltraPermissions;
import me.TechsCode.UltraPermissions.UltraPermissionsAPI;
import me.TechsCode.UltraPermissions.storage.objects.User;
import us.ajg0702.queue.api.players.AdaptedPlayer;
import us.ajg0702.queue.common.QueueMain;
import us.ajg0702.queue.api.premium.PermissionHook;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UltraPermissionsHook implements PermissionHook {

    private final QueueMain main;
    public UltraPermissionsHook(QueueMain main) {
        this.main = main;
    }

    @Override
    public String getName() {
        return "UltraPermissions";
    }

    @Override
    public boolean canUse() {
        if(!main.getPlatformMethods().hasPlugin("UltraPermissions") ) return false;
        if(UltraPermissions.getAPI() == null) {
            main.getLogger().warn("UltraPermissions is installed, but its getApi() method returned null! Unable to hook into it.");
            return false;
        }
        return true;
    }

    @Override
    public List<String> getPermissions(AdaptedPlayer player) {
        UltraPermissionsAPI ultraPermissionsAPI = UltraPermissions.getAPI();

        Optional<User> userOptional =  ultraPermissionsAPI
                .getUsers()
                .uuid(player.getUniqueId());
        if(userOptional.isEmpty()) return new ArrayList<>();
        User user = userOptional.get();

        List<String> permissions = new ArrayList<>();
        user.getPermissions().bungee().forEach(permission -> permissions.add(permission.getName()));
        return permissions;
    }
}
