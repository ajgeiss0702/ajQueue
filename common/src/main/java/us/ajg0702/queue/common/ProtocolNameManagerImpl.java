package us.ajg0702.queue.common;

import us.ajg0702.queue.api.PlatformMethods;
import us.ajg0702.queue.api.ProtocolNameManager;
import us.ajg0702.utils.common.Config;

import java.util.HashMap;
import java.util.List;

public class ProtocolNameManagerImpl implements ProtocolNameManager {

    private final Config config;
    private final PlatformMethods platformMethods;
    public ProtocolNameManagerImpl(Config config, PlatformMethods platformMethods) {
        this.config = config;
        this.platformMethods = platformMethods;
    }

    @Override
    public String getProtocolName(int protocol) {
        return getProtocolNames().getOrDefault(protocol, platformMethods.getProtocolName(protocol));
    }

    @Override
    public HashMap<Integer, String> getProtocolNames() {
        List<String> raw = config.getStringList("protocol-names");
        HashMap<Integer, String> result = new HashMap<>();
        for(String protocolRaw : raw) {
            String[] parts = protocolRaw.split(":");
            if(parts.length < 2) continue;
            String versionRaw = parts[0];
            String versionString = parts[1];
            int version;
            try {
                version = Integer.parseInt(versionRaw);
            } catch(NumberFormatException e) {
                continue;
            }

            result.put(version, versionString);
        }
        return result;
    }
}
