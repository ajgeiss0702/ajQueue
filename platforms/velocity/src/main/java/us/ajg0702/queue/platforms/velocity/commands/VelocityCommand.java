package us.ajg0702.queue.platforms.velocity.commands;

import com.velocitypowered.api.command.RawCommand;
import us.ajg0702.queue.commands.BaseCommand;
import us.ajg0702.queue.common.QueueMain;

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
        return command.autoComplete(new VelocitySender(invocation.source()), invocation.arguments().split(" "));
    }

    @Override
    public boolean hasPermission(final Invocation invocation) {
        return command.checkPermission(new VelocitySender(invocation.source()));
    }
}
