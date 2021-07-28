package us.ajg0702.queue.logic;

import us.ajg0702.queue.api.AliasManager;
import us.ajg0702.utils.common.Config;

import java.util.List;

public class PremiumAliasManager implements AliasManager {

    private final Config config;

    protected PremiumAliasManager(Config config) {
        this.config = config;
    }

    @Override
    public String getAlias(String server) {
        List<String> aliasesraw = config.getStringList("server-aliases");
        for(String aliasraw : aliasesraw) {
            String realname = aliasraw.split(":")[0];
            if(!realname.equalsIgnoreCase(server)) continue;
            return aliasraw.split(":")[1];
        }
        return server;
    }

    @Override
    public String getServer(String alias) {
        List<String> aliasesraw = config.getStringList("server-aliases");
        for(String aliasraw : aliasesraw) {
            String salias = aliasraw.split(":")[1];
            if(!alias.equalsIgnoreCase(salias)) continue;
            return aliasraw.split(":")[0];
        }
        return alias;
    }
}
