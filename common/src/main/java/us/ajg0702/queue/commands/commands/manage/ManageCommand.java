package us.ajg0702.queue.commands.commands.manage;

import com.google.common.collect.ImmutableList;
import us.ajg0702.queue.api.commands.ICommandSender;
import us.ajg0702.queue.api.commands.ISubCommand;
import us.ajg0702.queue.commands.BaseCommand;
import us.ajg0702.queue.commands.SubCommand;
import us.ajg0702.queue.common.QueueMain;
import us.ajg0702.utils.common.Messages;

import java.util.ArrayList;
import java.util.List;

public class ManageCommand extends BaseCommand {

    QueueMain main;

    public ManageCommand(QueueMain main) {
        this.main = main;


    }


    @Override
    public String getName() {
        return "ajqueue";
    }

    @Override
    public ImmutableList<String> getAliases() {
        return ImmutableList.of("ajq");
    }

    List<ISubCommand> subCommands = new ArrayList<>();

    @Override
    public ImmutableList<ISubCommand> getSubCommands() {
        return ImmutableList.copyOf(subCommands);
    }

    @Override
    public String getPermission() {
        return "ajqueue.manage";
    }

    @Override
    public Messages getMessages() {
        return main.getMessages();
    }

    @Override
    public void addSubCommand(ISubCommand subCommand) {
        subCommands.add(subCommand);
    }

    @Override
    public void execute(ICommandSender sender, String[] args) {

    }

    @Override
    public List<String> autoComplete(ICommandSender sender, String[] args) {
        return null;
    }
}
