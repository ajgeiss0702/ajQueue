package us.ajg0702.queue.logic;

import us.ajg0702.queue.api.AliasManager;
import us.ajg0702.utils.common.Config;

public class FreeAliasManager implements AliasManager {
    Config config;
    public FreeAliasManager(Config config) {
        this.config = config;
    }

    @Override
    public String getAlias(String server) {
        return server;
    }

    @Override
    public String getServer(String alias) {
        return alias;
    }
}
