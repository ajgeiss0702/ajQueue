package us.ajg0702.queue.logic;

import us.ajg0702.queue.api.AliasManager;
import us.ajg0702.queue.api.premium.Logic;
import us.ajg0702.queue.api.premium.LogicGetter;
import us.ajg0702.queue.api.players.AdaptedPlayer;
import us.ajg0702.queue.api.premium.PermissionGetter;
import us.ajg0702.queue.common.QueueMain;
import us.ajg0702.utils.common.Config;

import java.util.List;

public class LogicGetterImpl implements LogicGetter {
    PremiumLogic logic;

    @Override
    public Logic constructLogic() {
        if(logic == null) {
            logic = new PremiumLogic(QueueMain.getInstance());
        }
        return logic;
    }

    @Override
    public AliasManager constructAliasManager(Config config) {
        return new PremiumAliasManager(config);
    }

    @Override
    public List<String> getPermissions(AdaptedPlayer player) {
        if(logic == null) return null;
        return logic.getPermissionGetter().getSelected().getPermissions(player);
    }

    @Override
    public PermissionGetter getPermissionGetter() {
        return logic.getPermissionGetter();
    }
}
