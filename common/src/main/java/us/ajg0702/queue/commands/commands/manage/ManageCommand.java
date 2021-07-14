package us.ajg0702.queue.commands.commands.manage;

import com.google.common.collect.ImmutableList;
import net.kyori.adventure.text.Component;
import us.ajg0702.queue.api.commands.ICommandSender;
import us.ajg0702.queue.api.commands.ISubCommand;
import us.ajg0702.queue.commands.BaseCommand;
import us.ajg0702.queue.common.QueueMain;
import us.ajg0702.utils.common.Messages;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class ManageCommand extends BaseCommand {

    final QueueMain main;

    public ManageCommand(QueueMain main) {
        this.main = main;

        addSubCommand(new Reload(main));
        addSubCommand(new Tasks(main));
        addSubCommand(new Version(main));
        addSubCommand(new Pause(main));
        addSubCommand(new ISP(main));
        addSubCommand(new QueueList(main));
        addSubCommand(new Send(main));
    }


    @Override
    public String getName() {
        return "ajqueue";
    }

    @Override
    public ImmutableList<String> getAliases() {
        return ImmutableList.of("ajq");
    }

    final List<ISubCommand> subCommands = new ArrayList<>();

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
        if(args.length > 0) {
            for(ISubCommand subCommand : subCommands) {
                if(args[0].equalsIgnoreCase(subCommand.getName()) || subCommand.getAliases().contains(args[0].toLowerCase(Locale.ROOT))) {
                    subCommand.execute(sender, Arrays.copyOfRange(args, 1, args.length));
                    return;
                }
            }
        }
        sender.sendMessage(Component.text("/ajQueue <reload|list|send|pause>"));
    }

    @Override
    public List<String> autoComplete(ICommandSender sender, String[] args) {
        if(args.length > 1) {
            for(ISubCommand subCommand : subCommands) {
                if(args[0].equalsIgnoreCase(subCommand.getName()) || subCommand.getAliases().contains(args[0].toLowerCase(Locale.ROOT))) {
                    return subCommand.autoComplete(sender, Arrays.copyOfRange(args, 1, args.length));
                }
            }
            return new ArrayList<>();
        }
        List<String> commands = new ArrayList<>();
        for(ISubCommand subCommand : subCommands) {
            commands.add(subCommand.getName());
            commands.addAll(subCommand.getAliases());
        }
        return commands;
    }
}
