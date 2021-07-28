package us.ajg0702.queue.logic;

import us.ajg0702.queue.api.AliasManager;
import us.ajg0702.queue.api.Logic;
import us.ajg0702.queue.api.LogicGetter;
import us.ajg0702.queue.common.QueueMain;
import us.ajg0702.utils.common.Config;

public class LogicGetterImpl implements LogicGetter {
    @Override
    public Logic constructLogic() {
        return new PremiumLogic(QueueMain.getInstance());
    }

    @Override
    public AliasManager constructAliasManager(Config config) {
        return new PremiumAliasManager(config);
    }
}
