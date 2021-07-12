package us.ajg0702.queue.api;

@SuppressWarnings("unused")
public interface AliasManager {
    /**
     * Gets an alias from the server/group's name
     * @param server The original name of the server
     * @return The set alias (set in the config)
     */
    String getAlias(String server);

    /**
     * Gets the name of the server/group from an alias
     * @param alias The alias
     * @return The name of the server/group
     */
    String getServer(String alias);
}
