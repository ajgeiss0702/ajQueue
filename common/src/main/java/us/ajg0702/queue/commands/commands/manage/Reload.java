package us.ajg0702.queue.commands.commands.manage;

import com.google.common.collect.ImmutableList;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.spongepowered.configurate.ConfigurateException;
import us.ajg0702.queue.api.commands.ICommandSender;
import us.ajg0702.queue.commands.SubCommand;
import us.ajg0702.queue.common.QueueMain;
import us.ajg0702.utils.common.Messages;
import us.ajg0702.utils.common.UpdateManager;

import java.util.Collections;
import java.util.List;

public class Reload extends SubCommand {

    final QueueMain main;
    public Reload(QueueMain main) {
        this.main = main;
    }

    @Override
    public String getName() {
        return "reload";
    }

    @Override
    public ImmutableList<String> getAliases() {
        return ImmutableList.of();
    }

    @Override
    public String getPermission() {
        return "ajqueue.manage.reload";
    }

    @Override
    public Messages getMessages() {
        return main.getMessages();
    }

    @Override
    public void execute(ICommandSender sender, String[] args) {
        if(!checkPermission(sender)) return;
        main.getMessages().reload();
        try {
            main.getConfig().reload();
            main.getUpdaterConfig().reload();
        } catch (ConfigurateException e) {
            sender.sendMessage(Component.text("An error occurred while reloading. Check the console").color(NamedTextColor.RED));
            e.printStackTrace();
            return;
        }
        main.setTimeBetweenPlayers();
        main.getTaskManager().rescheduleTasks();
        main.getQueueManager().reloadServers();
        main.getMessages().reload();
        main.getSlashServerManager().reload();

        UpdateManager updateManager = main.getUpdateManager();
        if(updateManager != null) updateManager.setUpdateToken(main.getUpdaterConfig().getString("updater-token"));

        sender.sendMessage(getMessages().getComponent("commands.reload"));
    }

    @Override
    public List<String> autoComplete(ICommandSender sender, String[] args) {
        return Collections.emptyList();
    }
}
