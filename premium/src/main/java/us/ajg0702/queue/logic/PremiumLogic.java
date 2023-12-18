package us.ajg0702.queue.logic;

import com.google.common.collect.ImmutableList;
import us.ajg0702.queue.api.AjQueueAPI;
import us.ajg0702.queue.api.events.PriorityCalculationEvent;
import us.ajg0702.queue.api.premium.Logic;
import us.ajg0702.queue.api.players.AdaptedPlayer;
import us.ajg0702.queue.api.players.QueuePlayer;
import us.ajg0702.queue.api.queues.QueueServer;
import us.ajg0702.queue.api.server.AdaptedServer;
import us.ajg0702.queue.api.util.QueueLogger;
import us.ajg0702.queue.common.QueueMain;
import us.ajg0702.queue.common.players.QueuePlayerImpl;
import us.ajg0702.queue.api.premium.PermissionGetter;
import us.ajg0702.queue.logic.permissions.PermissionGetterImpl;

public class PremiumLogic implements Logic {

    public PermissionGetter getPermissionGetter() {
        return permissionGetter;
    }

    @Override
    public int getHighestPriority(QueueServer queueServer, AdaptedServer server, AdaptedPlayer player) {
        int normalPriority = permissionGetter.getPriority(player);
        int serverPriority = permissionGetter.getServerPriotity(queueServer.getName(), player);
        int unJoinablePriority = Logic.getUnJoinablePriorities(queueServer, server, player);

        int highest = Math.max(normalPriority, Math.max(serverPriority, unJoinablePriority));

        PriorityCalculationEvent event = new PriorityCalculationEvent(player, highest);

        QueueMain.getInstance().call(event);

        return event.getHighestPriority();
    }

    private final PermissionGetter permissionGetter;
    public PremiumLogic(QueueMain main) {
        permissionGetter = new PermissionGetterImpl(main);
    }

    @Override
    public boolean isPremium() {
        return true;
    }

    @Override
    public QueuePlayer priorityLogic(QueueServer queueServer, AdaptedPlayer player, AdaptedServer server) {
        int maxOfflineTime = permissionGetter.getMaxOfflineTime(player);

        QueueMain main = QueueMain.getInstance();

        QueueLogger logger = main.getLogger();
        boolean debug = main.getConfig().getBoolean("priority-queue-debug");

        if(hasAnyBypass(player, queueServer.getName())) {
            if(debug) {
                logger.info("[priority] "+player.getName()+" bypass");
            }
            QueuePlayer queuePlayer = new QueuePlayerImpl(player, queueServer, Integer.MAX_VALUE, maxOfflineTime);
            queueServer.addPlayer(queuePlayer, 0);
            main.getQueueManager().sendPlayers(queueServer);
            return queuePlayer;
        }

        int priority = permissionGetter.getPriority(player);
        int serverPriority = permissionGetter.getServerPriotity(queueServer.getName(), player);

        if(debug) {
            logger.info("[priority] Using "+permissionGetter.getSelected().getName()+" for permissions");
        }

        int highestPriority = Math.max(priority, serverPriority);
        highestPriority = Math.max(highestPriority, Logic.getUnJoinablePriorities(queueServer, server, player));

        QueuePlayer queuePlayer = new QueuePlayerImpl(player, queueServer, highestPriority, maxOfflineTime);

        if(debug) {
            logger.info("[priority] "+player.getName()+" highestPriority: "+highestPriority);
            logger.info("[priority] "+player.getName()+"   priority: "+priority);
            logger.info("[priority] "+player.getName()+"   serverPriority: "+serverPriority);
        }

        if(highestPriority <= 0) {
            if(debug) {
                logger.info("[priority] "+player.getName()+"  No priority" );
            }
            queueServer.addPlayer(queuePlayer);
            return queuePlayer;
        }

        ImmutableList<QueuePlayer> list = queueServer.getQueue();

        for(int i = 0; i < list.size(); i++) {
            QueuePlayer pl = list.get(i);
            if (pl.getPriority() < highestPriority) {
                if (debug) {
                    logger.info("[priority] " + player.getName() + "  Adding to: " + i);
                }
                queueServer.addPlayer(queuePlayer, i);
                return queuePlayer;
            }
        }


        if(debug) {
            logger.info("[priority] "+player.getName()+"  Cant go infront of anyone" );
        }
        queueServer.addPlayer(queuePlayer);
        return queuePlayer;
    }

    @Override
    public boolean playerDisconnectedTooLong(QueuePlayer player) {
        return player.getTimeSinceOnline() > player.getMaxOfflineTime()*1000L;
    }
    @Override
    public boolean hasAnyBypass(AdaptedPlayer player, String server) {
        return player.hasPermission("ajqueue.bypass") ||
                player.hasPermission("ajqueue.serverbypass."+ server) ||
                player.hasPermission("ajqueue.joinfullandbypassserver."+ server) ||
                player.hasPermission("ajqueue.joinfullandbypass") ||
                permissionGetter.hasContextBypass(player, server) ||
                (QueueMain.getInstance().isPremium() && permissionGetter.hasUniqueFullBypass(player, server));
    }

}
