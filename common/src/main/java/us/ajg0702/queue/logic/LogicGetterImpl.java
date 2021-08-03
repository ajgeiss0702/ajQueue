package us.ajg0702.queue.logic;

import us.ajg0702.queue.api.AliasManager;
import us.ajg0702.queue.api.Logic;
import us.ajg0702.queue.api.players.AdaptedPlayer;
import us.ajg0702.utils.common.Config;

import java.util.List;

public class LogicGetterImpl implements us.ajg0702.queue.api.LogicGetter {

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
}
