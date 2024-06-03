package us.ajg0702.queue.common;

import us.ajg0702.queue.api.PlatformMethods;
import us.ajg0702.queue.api.ProtocolNameManager;
import us.ajg0702.utils.common.Config;
import us.ajg0702.utils.common.Messages;

import java.util.HashMap;
import java.util.List;

public class ProtocolNameManagerImpl implements ProtocolNameManager {

    private final Messages messages;
    private final PlatformMethods platformMethods;
    public ProtocolNameManagerImpl(Messages messages, PlatformMethods platformMethods) {
        this.messages = messages;
        this.platformMethods = platformMethods;
    }

    @Override
    public String getProtocolName(int protocol) {
        String setName = messages.getRawString("protocol-names." + protocol);

        if(setName == null) return platformMethods.getProtocolName(protocol);

        return setName;
    }
}
