package us.ajg0702.queue.logic;

import us.ajg0702.queue.api.AliasManager;
import us.ajg0702.utils.common.Config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PremiumAliasManager implements AliasManager {

    private final Config config;

    protected PremiumAliasManager(Config config) {
        this.config = config;
    }

    @Override
    public String getAlias(String server) {
        List<String> aliasesRaw = config.getStringList("server-aliases");
        for(String aliasRaw : aliasesRaw) {
            List<String> parts = new ArrayList<>(Arrays.asList(aliasRaw.split(":")));
            String realName = parts.remove(0);
            if(!realName.equalsIgnoreCase(server)) continue;
            return String.join(":", parts);
        }
        return server;
    }

    @Override
    public String getServer(String alias) {
        List<String> aliasesRaw = config.getStringList("server-aliases");
        for(String aliasRaw : aliasesRaw) {
            List<String> parts = new ArrayList<>(Arrays.asList(aliasRaw.split(":")));
            String realName = parts.remove(0);
            String tAlias = String.join(":", parts);
            if(!tAlias.equalsIgnoreCase(alias)) continue;
            return realName;
        }
        return alias;
    }
}
