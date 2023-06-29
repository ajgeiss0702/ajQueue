package us.ajg0702.queue.spigot.api;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import us.ajg0702.queue.api.spigot.AjQueueSpigotAPI;
import us.ajg0702.queue.api.spigot.MessagedResponse;
import us.ajg0702.queue.spigot.SpigotMain;
import us.ajg0702.queue.spigot.communication.ResponseManager;

import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

public class SpigotAPI extends AjQueueSpigotAPI {
    private final ResponseManager responseManager;
    private final SpigotMain main;

    public SpigotAPI(ResponseManager responseManager, SpigotMain main) {
        this.responseManager = responseManager;
        this.main = main;
    }

    @Override
    public Future<Boolean> isInQueue(UUID player) {
        Player p = Bukkit.getPlayer(player);
        if(p == null) throw new IllegalArgumentException("Player must be online!");

        CompletableFuture<Boolean> future = new CompletableFuture<>();

        responseManager.awaitResponse(player.toString(), "inqueue", response -> {
            future.complete(Boolean.valueOf(response.getResponse()));
        });

        main.sendMessage(p, "inqueue", "");

        return future;
    }

    @Override
    public Future<Boolean> addToQueue(UUID player, String queueName) {
        Player p = Bukkit.getPlayer(player);
        if(p == null) throw new IllegalArgumentException("Player must be online!");

        CompletableFuture<Boolean> future = new CompletableFuture<>();

        responseManager.awaitResponse(player.toString(), "serverqueue", response -> {
            future.complete(Boolean.valueOf(response.getResponse()));
        });

        main.sendMessage(p, "serverqueue", queueName);

        return future;
    }

    @Override
    public void sudoQueue(UUID player, String queueName) {
        Player p = Bukkit.getPlayer(player);
        if(p == null) throw new IllegalArgumentException("Player must be online!");
        main.sendMessage(p, "queue", queueName);
    }

    @Override
    public Future<MessagedResponse<String>> getQueueName(UUID player) {
        Player p = Bukkit.getPlayer(player);
        if(p == null) throw new IllegalArgumentException("Player must be online!");

        CompletableFuture<MessagedResponse<String>> future = new CompletableFuture<>();

        responseManager.awaitResponse(player.toString(), "queuename", response -> {
            future.complete(new MessagedResponse<>(response.getResponse(), response.getNoneMessage()));
        });

        main.sendMessage(p, "queuename", "");

        return future;
    }

    @Override
    public Future<MessagedResponse<Integer>> getPosition(UUID player) {
        Player p = Bukkit.getPlayer(player);
        if(p == null) throw new IllegalArgumentException("Player must be online!");

        CompletableFuture<MessagedResponse<Integer>> future = new CompletableFuture<>();

        responseManager.awaitResponse(player.toString(), "position", response -> {
            String r = response.getResponse();
            Integer i = r == null ? null : Integer.valueOf(r);
            future.complete(new MessagedResponse<>(i, response.getNoneMessage()));
        });

        main.sendMessage(p, "position", "");

        return future;
    }

    @Override
    public Future<MessagedResponse<Integer>> getTotalPositions(UUID player) {
        Player p = Bukkit.getPlayer(player);
        if(p == null) throw new IllegalArgumentException("Player must be online!");

        CompletableFuture<MessagedResponse<Integer>> future = new CompletableFuture<>();

        responseManager.awaitResponse(player.toString(), "positionof", response -> {
            String r = response.getResponse();
            Integer i = r == null ? null : Integer.valueOf(r);
            future.complete(new MessagedResponse<>(i, response.getNoneMessage()));
        });

        main.sendMessage(p, "positionof", "");

        return future;
    }

    @Override
    public Future<Integer> getPlayersInQueue(String queueName) {
        Player p = getSomePlayer();

        CompletableFuture<Integer> future = new CompletableFuture<>();

        responseManager.awaitResponse(queueName, "queuedfor", response -> {
            String responseString = response.getResponse();
            if(responseString.equals("invalid_server")) {
                future.completeExceptionally(new IllegalArgumentException(queueName + " does not exist!"));
                return;
            }
            future.complete(Integer.valueOf(responseString));
        });

        main.sendMessage(p, "queuedfor", queueName);

        return future;
    }

    @Override
    public Future<String> getServerStatusString(String queueName) {
        return getServerStatusString(queueName, null);
    }

    @Override
    public Future<String> getServerStatusString(String queueName, UUID player) {
        Player p = player == null ? getSomePlayer() : Bukkit.getPlayer(player);
        if(p == null) throw new IllegalArgumentException("Player must be online!");

        String channel = player == null ? "status" : "playerstatus";
        String id = player == null ? queueName : player + queueName;

        CompletableFuture<String> future = new CompletableFuture<>();

        responseManager.awaitResponse(id, channel, response -> {
            String responseString = response.getResponse();
            if(responseString.equals("invalid_server")) {
                future.completeExceptionally(new IllegalArgumentException(queueName + " does not exist!"));
                return;
            }
            future.complete(responseString);
        });

        main.sendMessage(p, channel, queueName);

        return future;
    }

    @Override
    public Future<MessagedResponse<String>> getEstimatedTime(UUID player) {
        Player p = Bukkit.getPlayer(player);
        if(p == null) throw new IllegalArgumentException("Player must be online!");

        CompletableFuture<MessagedResponse<String>> future = new CompletableFuture<>();

        responseManager.awaitResponse(player.toString(), "estimated_time", response -> {
            future.complete(new MessagedResponse<>(response.getResponse(), response.getNoneMessage()));
        });

        main.sendMessage(p, "estimated_time", "");

        return future;
    }

    private Player getSomePlayer() {
        Collection<? extends Player> players = Bukkit.getOnlinePlayers();
        if(players.size() == 0) return null;
        return players.iterator().next();
    }
}
