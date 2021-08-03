package us.ajg0702.queue.api.commands;

import com.google.common.collect.ImmutableList;
import us.ajg0702.utils.common.Messages;

import java.util.List;

@SuppressWarnings("unused")
public interface IBaseCommand {

    String getName();

    ImmutableList<String> getAliases();

    ImmutableList<ISubCommand> getSubCommands();

    String getPermission();

    boolean showInTabComplete();

    Messages getMessages();

    void addSubCommand(ISubCommand subCommand);

    void execute(ICommandSender sender, String[] args);

    List<String> autoComplete(ICommandSender sender, String[] args);
}
