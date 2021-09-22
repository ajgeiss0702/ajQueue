package us.ajg0702.queue.commands.commands.manage;

import com.google.common.collect.ImmutableList;
import us.ajg0702.queue.api.commands.ICommandSender;
import us.ajg0702.queue.commands.SubCommand;
import us.ajg0702.queue.common.QueueMain;
import us.ajg0702.utils.common.Messages;
import us.ajg0702.utils.common.Updater;

import java.util.ArrayList;
import java.util.List;

public class Update extends SubCommand {

    final QueueMain main;
    public Update(QueueMain main) {
        this.main = main;
    }

    @Override
    public String getName() {
        return "update";
    }

    @Override
    public ImmutableList<String> getAliases() {
        return ImmutableList.of();
    }

    @Override
    public String getPermission() {
        return "ajqueue.manage.update";
    }

    @Override
    public Messages getMessages() {
        return main.getMessages();
    }

    @Override
    public void execute(ICommandSender sender, String[] args) {
        if(!checkPermission(sender)) return;
        Updater updater = main.getUpdater();
        if(updater.isAlreadyDownloaded()) {
            sender.sendMessage(getMessages().getComponent("updater.already-downloaded"));
            return;
        }
        if(!updater.isUpdateAvailable()) {
            sender.sendMessage(getMessages().getComponent("updater.no-update"));
            return;
        }
        if(updater.downloadUpdate()) {
            sender.sendMessage(getMessages().getComponent("updater.success"));
        } else {
            sender.sendMessage(getMessages().getComponent("updater.fail"));
        }
    }

    @Override
    public List<String> autoComplete(ICommandSender sender, String[] args) {
        return new ArrayList<>();
    }
}
