package us.ajg0702.queue.logic;

import us.ajg0702.queue.api.AliasManager;
import us.ajg0702.queue.api.premium.Logic;
import us.ajg0702.queue.api.players.AdaptedPlayer;
import us.ajg0702.queue.api.premium.LogicGetter;
import us.ajg0702.queue.api.premium.PermissionGetter;
import us.ajg0702.utils.common.Config;

import java.util.List;

public class LogicGetterImpl implements LogicGetter {

    @Override
    public Logic constructLogic() {
        return new FreeLogic();
    }

    @Override
    public AliasManager constructAliasManager(Config config) {
        return new FreeAliasManager(config);
    }

    @Override
    public List<String> getPermissions(AdaptedPlayer player) {
        return null;
    }

    @Override
    public PermissionGetter getPermissionGetter() {
        return null;
    }
}
