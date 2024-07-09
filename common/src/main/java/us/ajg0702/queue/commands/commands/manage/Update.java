package us.ajg0702.queue.commands.commands.manage;

import com.google.common.collect.ImmutableList;
import us.ajg0702.queue.api.commands.ICommandSender;
import us.ajg0702.queue.commands.SubCommand;
import us.ajg0702.queue.common.QueueMain;
import us.ajg0702.utils.common.Messages;
import us.ajg0702.utils.common.UpdateManager;
import us.ajg0702.utils.common.Updater;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

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
        UpdateManager updater = main.getUpdateManager();
        if(updater == null) {
            sender.sendMessage(
                    getMessages().getComponent("updater.disabled")
            );
            return;
        }
        if(updater.isAlreadyDownloaded()) {
            sender.sendMessage(getMessages().getComponent("updater.already-downloaded"));
            return;
        }
        if(!updater.isUpdateAvailable()) {
            sender.sendMessage(getMessages().getComponent("updater.no-update"));
            return;
        }
        /*if() {
            sender.sendMessage(getMessages().getComponent("updater.success"));
        } else {
            sender.sendMessage(getMessages().getComponent("updater.fail"));
        }*/
        AtomicBoolean done = new AtomicBoolean(false);

        main.getTaskManager().runNow(() -> {
            try {
                UpdateManager.DownloadCompleteStatus result = updater.downloadUpdate();

                done.set(true);

                switch(result) {
                    case SUCCESS:
                        sender.sendMessage(getMessages().getComponent("updater.success"));
                        break;
                    case WARNING_COULD_NOT_DELETE_OLD_JAR:
                        sender.sendMessage(getMessages().getComponent("updater.warnings.could-not-delete-old-jar"));
                        break;
                    case ERROR_NO_UPDATE_AVAILABLE:
                        sender.sendMessage(getMessages().getComponent("updater.no-update"));
                        break;
                    case ERROR_WHILE_CHECKING:
                        sender.sendMessage(getMessages().getComponent("updater.errors.while-checking"));
                        break;
                    case ERROR_ALREADY_DOWNLOADED:
                        sender.sendMessage(getMessages().getComponent("updater.already-downloaded"));
                        break;
                    case ERROR_JAR_NOT_FOUND:
                        sender.sendMessage(getMessages().getComponent("updater.errors.could-not-find-jar"));
                        break;
                    case ERROR_WHILE_DOWNLOADING:
                        sender.sendMessage(getMessages().getComponent("updater.errors.while-downloading"));
                        break;
                    case ERROR_MISSING_UPDATE_TOKEN:
                        sender.sendMessage(getMessages().getComponent("updater.errors.missing-update-token"));
                        break;
                    case ERROR_INVALID_UPDATE_TOKEN:
                        sender.sendMessage(getMessages().getComponent("updater.errors.invalid-update-token"));
                        break;
                    default:
                        sender.sendMessage(getMessages().getComponent("updater.errors.unknown", "ERROR:"+result));
                        break;
                }
            } catch(Exception e) {
                sender.sendMessage(getMessages().getComponent("updater.errors.uncaught"));
                main.getLogger().warn("Uncaught error while updating:", e);
            }

            main.getTaskManager().runLater(() -> {
                if(done.get()) return;

                sender.sendMessage(getMessages().getComponent("updater.slow-feedback"));
            }, 1, TimeUnit.SECONDS);
        });


    }

    @Override
    public List<String> autoComplete(ICommandSender sender, String[] args) {
        return new ArrayList<>();
    }
}
