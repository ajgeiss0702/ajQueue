package us.ajg0702.queue.logic.permissions.hooks;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.Node;
import net.luckperms.api.node.NodeType;
import net.luckperms.api.query.QueryOptions;
import us.ajg0702.queue.api.players.AdaptedPlayer;
import us.ajg0702.queue.common.QueueMain;
import us.ajg0702.queue.logic.permissions.PermissionHook;

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

        assert user != null;
        SortedSet<Node> nodes = user.resolveDistinctInheritedNodes(QueryOptions.defaultContextualOptions());

        List<String> perms = new ArrayList<>();

        for (Node node : nodes) {
            if (!node.getType().equals(NodeType.PERMISSION)) continue;
            if (!node.getValue()) continue;
            perms.add(node.getKey());
        }

        return perms;
    }
}
