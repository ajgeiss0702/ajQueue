package us.ajg0702.queue.commands.commands.manage.debug;

import com.google.common.collect.ImmutableList;
import net.kyori.adventure.text.Component;
import us.ajg0702.queue.api.commands.ICommandSender;
import us.ajg0702.queue.api.queues.QueueServer;
import us.ajg0702.queue.commands.SubCommand;
import us.ajg0702.queue.common.QueueMain;
import us.ajg0702.utils.common.Messages;

import java.util.ArrayList;
import java.util.List;

public class Whitelist extends SubCommand {
    final QueueMain main;
    public Whitelist(QueueMain main) {
        this.main = main;
    }

    @Override
    public String getName() {
        return "whitelist";
    }

    @Override
    public ImmutableList<String> getAliases() {
        return ImmutableList.of();
    }

    @Override
    public String getPermission() {
        return null;
    }

    @Override
    public boolean showInTabComplete() {
        return false;
    }

    @Override
    public Messages getMessages() {
        return main.getMessages();
    }

    @Override
    public void execute(ICommandSender sender, String[] args) {
        if(!checkPermission(sender)) return;
        if(args.length < 1) {
            sender.sendMessage(main.getMessages().toComponent("<red>Not enough args!"));
            return;
        }
        QueueServer server = main.getQueueManager().findServer(args[0]);
        if(server == null) {
            sender.sendMessage(main.getMessages().toComponent("<red>Server not found"));
            return;
        }
        sender.sendMessage(main.getMessages().toComponent("<green>Yours: "+ main.getPlatformMethods().senderToPlayer(sender).getUniqueId().toString()));
        server.getWhitelistedPlayers().forEach(uuid -> sender.sendMessage(main.getMessages().toComponent("<yellow>"+uuid)));
    }

    @Override
    public List<String> autoComplete(ICommandSender sender, String[] args) {
        return new ArrayList<>();
    }
}
