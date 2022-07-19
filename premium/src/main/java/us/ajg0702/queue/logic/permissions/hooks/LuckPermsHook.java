package us.ajg0702.queue.logic.permissions.hooks;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.context.Context;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.Node;
import net.luckperms.api.node.NodeType;
import net.luckperms.api.query.QueryOptions;
import us.ajg0702.queue.api.players.AdaptedPlayer;
import us.ajg0702.queue.api.premium.PermissionHook;
import us.ajg0702.queue.common.QueueMain;

import java.util.*;

public class LuckPermsHook implements PermissionHook {

    private final QueueMain main;
    public LuckPermsHook(QueueMain main) {
        this.main = main;
    }

    @Override
    public String getName() {
        return "LuckPerms";
    }

    @Override
    public boolean canUse() {
        return main.getPlatformMethods().hasPlugin("LuckPerms");
    }

    @Override
    public List<String> getPermissions(AdaptedPlayer player) {
        LuckPerms api = LuckPermsProvider.get();

        User user = api.getUserManager().getUser(player.getUniqueId());

        if(user == null) {
            main.getLogger().warn("LuckPerms doesnt seem to have data loaded for "+player.getName()+"! " +
                    "Because of this I can't load priority permissions. Acting like "+player.getName()+" doesnt have any.");
            return Collections.emptyList();
        }

        SortedSet<Node> nodes = user.resolveDistinctInheritedNodes(QueryOptions.nonContextual());

        List<String> perms = new ArrayList<>();

        for (Node node : nodes) {
            if (!node.getType().equals(NodeType.PERMISSION)) continue;
            if (!node.getValue()) continue;
            String permission = node.getKey();


            if(permission.equalsIgnoreCase("ajqueue.contextbypass")) {
                boolean skip = false;
                for(Context context : node.getContexts()) {
                    if(context.getKey().equalsIgnoreCase("server")) {
                        skip = true;
                        perms.add("ajqueue.serverbypass."+context.getValue());
                    }
                }
                if(skip) continue;
            }

            if(permission.toLowerCase(Locale.ROOT).startsWith("ajqueue.contextpriority.")) {
                boolean skip = false;
                int level;
                try {
                    level = Integer.parseInt(permission.substring(0, 24));
                } catch(NumberFormatException e) {
                    main.getLogger().warning("A non-number is in the priority permission "+permission);
                    continue;
                }
                for(Context context : node.getContexts()) {
                    if(context.getKey().equalsIgnoreCase("server")) {
                        skip = true;
                        perms.add("ajqueue.serverpriority."+context.getValue()+"."+level);
                    }
                }
                if(skip) continue;
            }


            perms.add(permission);
        }

        return perms;
    }
}
