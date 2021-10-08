package us.ajg0702.queue.commands;

import com.google.common.collect.ImmutableList;
import net.kyori.adventure.text.Component;
import us.ajg0702.queue.api.commands.IBaseCommand;
import us.ajg0702.queue.api.commands.ICommandSender;
import us.ajg0702.queue.api.commands.ISubCommand;
import us.ajg0702.utils.common.Messages;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class BaseCommand implements IBaseCommand {
    @Override
    public String getName() {
        return null;
    }

    @Override
    public ImmutableList<String> getAliases() {
        return null;
    }

    @SuppressWarnings("unused")
    @Override
    public ImmutableList<ISubCommand> getSubCommands() {
        return null;
    }

    @Override
    public String getPermission() {
        return null;
    }

    @Override
    public boolean showInTabComplete() {
        return true;
    }

    @Override
    public Messages getMessages() {
        return null;
    }

    @SuppressWarnings("unused")
    @Override
    public void addSubCommand(ISubCommand subCommand) {

    }

    @Override
    public void execute(ICommandSender sender, String[] args) {
        sender.sendMessage(Component.text("Unimplemented command"));
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean checkPermission(ICommandSender sender) {
        if(getPermission() != null && !sender.hasPermission(getPermission())) {
            sender.sendMessage(getMessages().getComponent("noperm"));
            return false;
        }
        return true;
    }

    @Override
    public List<String> autoComplete(ICommandSender sender, String[] args) {
        return null;
    }

    public List<String> filterCompletion(List<String> in, String current) {
        List<String> out = new ArrayList<>(in);
        out.removeIf(t -> !t.toLowerCase(Locale.ROOT).contains(current.toLowerCase(Locale.ROOT)));
        return out;
    }
}
