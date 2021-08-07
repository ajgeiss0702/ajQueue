package us.ajg0702.queue.platforms.velocity.commands;

import com.velocitypowered.api.command.RawCommand;
import us.ajg0702.queue.commands.BaseCommand;
import us.ajg0702.queue.common.QueueMain;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class VelocityCommand implements RawCommand {

    final QueueMain main;
    final BaseCommand command;

    public VelocityCommand(QueueMain main, BaseCommand command) {
        this.main = main;
        this.command = command;
    }

    @Override
    public void execute(Invocation invocation) {
        command.execute(new VelocitySender(invocation.source()), invocation.arguments().split(" "));
    }

    @Override
    public List<String> suggest(final Invocation invocation) {
        List<String> args = new ArrayList<>(Arrays.asList(invocation.arguments().split(" ")));
        if(invocation.arguments().length() > 0 &&invocation.arguments().charAt(invocation.arguments().length()-1) == ' ') {
            args.add(" ");
        }
        return command.autoComplete(new VelocitySender(invocation.source()), args.toArray(new String[0]));
    }

    @Override
    public boolean hasPermission(final Invocation invocation) {
        String permission = command.getPermission();
        if(permission == null) return true;
        return invocation.source().hasPermission(permission);
    }
}
