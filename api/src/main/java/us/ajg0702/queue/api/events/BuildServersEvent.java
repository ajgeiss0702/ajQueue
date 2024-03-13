package us.ajg0702.queue.api.events;

import org.jetbrains.annotations.Nullable;
import us.ajg0702.queue.api.server.AdaptedServer;

import java.util.*;

/**
 * Called before AjQueue attempts to re-compile its list of QueueServers
 * Use Case: Add/Remove an AdaptedServer from being registered as a QueueServer.
 */
@SuppressWarnings("unused")
public class BuildServersEvent implements Event {
    private final Map<String, AdaptedServer> servers = new HashMap<>();
    private final Map<String, List<AdaptedServer>> groups = new HashMap<>();

    public BuildServersEvent(List<? extends AdaptedServer> servers) {
        // Compile a map of server names to servers
        for(AdaptedServer server : servers) {
            this.servers.put(server.getName(), server);
        }
    }

    /**
     * @see #addServer(AdaptedServer)
     * @see #removeServer(AdaptedServer)
     * @see #removeServer(String)
     * @return an immutable view of the servers that will be registered as QueueServers.
     */
    public List<AdaptedServer> getServers() {
        return Collections.unmodifiableList(new ArrayList<>(servers.values()));
    }

    /**
     * Add a server to be registered as a QueueServer.
     * @param server The server to add
     * @return The previous AdaptedServer with that name, or null if there was no previous server
     */
    public @Nullable AdaptedServer addServer(AdaptedServer server) {
        return servers.put(server.getName(), server);
    }

    /**
     * Remove a server, preventing it from being registered as a QueueServer.
     * @param server The AdaptedServer to remove
     * @return true if the server was removed, false if it was not found
     */
    public boolean removeServer(AdaptedServer server) {
        return servers.remove(server.getName()) != null;
    }

    /**
     * Remove a server, preventing it from being registered as a QueueServer.
     * @param name The name of the server to remove
     * @return true if the server was removed, false if it was not found
     */
    public boolean removeServer(String name) {
        return servers.remove(name) != null;
    }


    /**
     * @see #addGroup(String, List)
     * @see #removeGroup(String)
     * @return an immutable list of the sets of servers that will be registered as group QueueServers.
     */
    public List<List<AdaptedServer>> getGroups() {
        return Collections.unmodifiableList(new ArrayList<>(groups.values()));
    }

    /**
     * Used internally
     */
    public Set<Map.Entry<String, List<AdaptedServer>>> groupEntrySet() {
        return groups.entrySet();
    }

    /**
     * Add a server-group to be registered by the Queue Manager.
     * @param name The name of the server-group
     * @param servers The servers to add to the group
     * @return The previous server list with that name, or null if there was no previous data
     */
    public @Nullable List<AdaptedServer> addGroup(String name, List<AdaptedServer> servers) {
        return groups.put(name, servers);
    }

    /**
     * Remove a server, preventing it from being registered as a QueueServer.
     * @param name The name of the server to remove
     * @return true if the server was removed, false if it was not found
     */
    public boolean removeGroup(String name) {
        return groups.remove(name) != null;
    }

}
