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
        for(String rawServer : slashServerServers) {
            if(rawServer.contains(":") && main.isPremium()) {
                String[] parts = rawServer.split(":");
                String command = parts[0];
                String server = parts[1];
                SlashServerCommand slashServerCommand = new SlashServerCommand(main, command, server);
                serverCommands.add(slashServerCommand);
                implementation.registerCommand(slashServerCommand);
            } else {
                SlashServerCommand command = new SlashServerCommand(main, rawServer);
                serverCommands.add(command);
                implementation.registerCommand(command);
            }

        }
    }
}
