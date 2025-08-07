package us.ajg0702.queue.commands.commands.manage;

import com.google.common.collect.ImmutableList;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.PatternReplacementResult;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import us.ajg0702.queue.api.commands.ICommandSender;
import us.ajg0702.queue.api.players.QueuePlayer;
import us.ajg0702.queue.api.queues.QueueServer;
import us.ajg0702.queue.commands.SubCommand;
import us.ajg0702.queue.common.QueueMain;
import us.ajg0702.utils.common.Messages;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class QueueList extends SubCommand {

    final QueueMain main;
    public QueueList(QueueMain main) {
        this.main = main;
    }

    @Override
    public String getName() {
        return "list";
    }

    @Override
    public ImmutableList<String> getAliases() {
        return ImmutableList.of();
    }

    @Override
    public String getPermission() {
        return "ajqueue.manage.list";
    }

    @Override
    public Messages getMessages() {
        return main.getMessages();
    }

    @Override
    public void execute(ICommandSender sender, String[] args) {
        if(!checkPermission(sender)) return;
        int total = 0;
        for(QueueServer server : main.getQueueManager().getServers()) {

            int standardSize = server.getQueueHolder().getStandardQueueSize();
            int expressSize = server.getQueueHolder().getExpressQueueSize();
            int totalSize = standardSize + expressSize;
            String msg = getMessages().getRawString("list." + (totalSize != Math.max(standardSize, expressSize) ? "both-format" : "single-format"),
                    "SERVER:"+server.getName(),
                    "STANDARD_COUNT:" + standardSize,
                    "EXPRESS_COUNT:" + expressSize,
                    "TOTAL_COUNT:" + totalSize,
                    "COUNT:" + totalSize
            );
            StringBuilder playerList = new StringBuilder();
            List<QueuePlayer> players = server.getQueueHolder().getAllPlayers();
            boolean none = true;
            for(QueuePlayer p : players) {
                playerList.append(getMessages().getRawString("list.playerlist-format",
                        "EXPRESS_COLOR:" + (p.isInExpressQueue() ? "&6" : ""),
                        "NAME:" + p.getName()
                ));
                none = false;
            }
            if(none) {
                playerList.append(getMessages().getRawString("list.none"));
                playerList.append(", ");
            }
            msg = msg.replaceAll("\\{LIST}", playerList.toString());

            msg = Arrays.stream(msg.split("\n"))
                    .map(String::trim)
                    .map(c -> c.endsWith(",") ? c.substring(0, c.length() - 1) : c) // removes comma from the end of the line
                    .collect(Collectors.joining("\n"));
            total += players.size();
            sender.sendMessage(main.getMessages().toComponent(msg));
        }
        sender.sendMessage(getMessages().getComponent("list.total", "TOTAL:"+total));
    }

    @Override
    public java.util.List<String> autoComplete(ICommandSender sender, String[] args) {
        return Collections.emptyList();
    }
}
