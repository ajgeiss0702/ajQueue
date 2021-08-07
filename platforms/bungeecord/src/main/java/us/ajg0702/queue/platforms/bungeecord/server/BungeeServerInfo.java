package us.ajg0702.queue.platforms.bungeecord.server;

import net.md_5.bungee.api.config.ServerInfo;
import us.ajg0702.queue.api.server.AdaptedServerInfo;

public class BungeeServerInfo implements AdaptedServerInfo {

    final ServerInfo handle;
    public BungeeServerInfo(ServerInfo handle) {
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
