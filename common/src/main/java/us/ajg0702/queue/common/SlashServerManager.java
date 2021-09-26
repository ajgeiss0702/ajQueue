package us.ajg0702.queue.common;

import us.ajg0702.queue.api.Implementation;
import us.ajg0702.queue.commands.commands.SlashServer.SlashServerCommand;

import java.util.ArrayList;
import java.util.List;

public class SlashServerManager {

    private final List<SlashServerCommand> serverCommands = new ArrayList<>();

    private final QueueMain main;
    private final Implementation implementation;
    public SlashServerManager(QueueMain main) {
        this.main = main;
        this.implementation = this.main.getImplementation();
        reload();
    }
    public void reload() {
        serverCommands.forEach(implementation::unregisterCommand);
        serverCommands.clear();

        List<String> slashServerServers = main.getConfig().getStringList("slash-servers");
        for(String server : slashServerServers) {
            SlashServerCommand command = new SlashServerCommand(main, server);
            serverCommands.add(command);
            implementation.registerCommand(command);
        }
    }
}
