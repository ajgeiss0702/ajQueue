package us.ajg0702.queue.api.premium;

import us.ajg0702.queue.api.AliasManager;
import us.ajg0702.queue.api.players.AdaptedPlayer;
import us.ajg0702.queue.api.premium.Logic;
import us.ajg0702.utils.common.Config;

import java.util.List;

@SuppressWarnings("unused")
public interface LogicGetter {
    Logic constructLogic();
    AliasManager constructAliasManager(Config config);
    List<String> getPermissions(AdaptedPlayer player);
    PermissionGetter getPermissionGetter();
}
