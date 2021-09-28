package us.ajg0702.queue.api;

import us.ajg0702.queue.api.commands.IBaseCommand;

public interface Implementation {
    void registerCommand(IBaseCommand command);
    void unregisterCommand(String name);
    default void unregisterCommand(IBaseCommand command) {
        unregisterCommand(command.getName());
    }
}
