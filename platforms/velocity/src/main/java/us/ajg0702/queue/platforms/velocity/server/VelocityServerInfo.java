package us.ajg0702.queue.platforms.velocity.server;

import com.velocitypowered.api.proxy.server.ServerInfo;
import us.ajg0702.queue.api.server.AdaptedServerInfo;

public class VelocityServerInfo implements AdaptedServerInfo {

    private final ServerInfo handle;

    public VelocityServerInfo(ServerInfo handle) {
        this.handle = handle;
    }

    @Override
    public String getName() {
        return handle.getName();
    }

    @Override
    public ServerInfo getHandle() {
        return handle;
    }
}
