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

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

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
        return "ajqueue.list";
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

            Component msg = getMessages().getComponent("list.format",
                    "SERVER:"+server.getName()
            );
            Component playerList = Component.empty();
            List<QueuePlayer> players = server.getQueue();
            boolean none = true;
            for(QueuePlayer p : players) {
                playerList = playerList.append(getMessages().getComponent("list.playerlist",
                        "NAME:" + p.getName()
                ));
                none = false;
            }
            if(none) {
                playerList = playerList.append(getMessages().getComponent("list.none"));
                playerList = playerList.append(Component.text(", "));
            }
            Component finalPlayerList = playerList;
            msg = msg.replaceText(b -> b.match(Pattern.compile("\\{LIST}")).replacement(finalPlayerList));
            char[] commaCountString = PlainTextComponentSerializer.plainText().serialize(msg).toCharArray();
            int commas = 0;
            for(Character fChar : commaCountString) {
                if(fChar == ',') commas++;
            }

            int finalCommas = commas;
            msg = msg.replaceText(b -> b.match(",(?!.*,)").replacement("").condition((r, c, re) -> {
                if(c == finalCommas) {
                    return PatternReplacementResult.REPLACE;
                }
                return PatternReplacementResult.CONTINUE;
            }));
            total += players.size();
            msg = msg.replaceText(b -> b.match(Pattern.compile("\\{COUNT}")).replacement(players.size()+""));
            sender.sendMessage(msg);
        }
        sender.sendMessage(getMessages().getComponent("list.total", "TOTAL:"+total));
    }

    @Override
    public java.util.List<String> autoComplete(ICommandSender sender, String[] args) {
        return new ArrayList<>();
    }
}
