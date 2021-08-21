package us.ajg0702.queue.platforms.velocity.players;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.sound.SoundStop;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.kyori.adventure.title.Title;
import org.jetbrains.annotations.NotNull;
import us.ajg0702.queue.api.players.AdaptedPlayer;
import us.ajg0702.queue.api.server.AdaptedServer;
import us.ajg0702.queue.common.QueueMain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class VelocityPlayer implements AdaptedPlayer, Audience {
    @Override
    public void showTitle(@NotNull Title title) {
        handle.showTitle(title);
    }

    @Override
    public void clearTitle() {
        handle.clearTitle();
    }

    @Override
    public void resetTitle() {
        handle.resetTitle();
    }

    @Override
    public void showBossBar(@NotNull BossBar bar) {
        handle.showBossBar(bar);
    }

    @Override
    public void hideBossBar(@NotNull BossBar bar) {
        handle.hideBossBar(bar);
    }

    @Override
    public void playSound(@NotNull Sound sound) {
        handle.playSound(sound);
    }

    @Override
    public void playSound(@NotNull Sound sound, double x, double y, double z) {
        handle.playSound(sound, x, y, z);
    }

    @Override
    public void stopSound(@NotNull Sound sound) {
        handle.stopSound(sound);
    }

    @Override
    public void playSound(@NotNull Sound sound, Sound.@NotNull Emitter emitter) {
        handle.playSound(sound, emitter);
    }

    @Override
    public void stopSound(@NotNull SoundStop stop) {
        handle.stopSound(stop);
    }

    final Player handle;

    public VelocityPlayer(Player player) {
        handle = player;
    }

    @Override
    public boolean isConnected() {
        return handle.isActive();
    }

    @Override
    public void sendMessage(@NotNull Component message) {
        if(PlainTextComponentSerializer.plainText().serialize(message).isEmpty()) return;
        handle.sendMessage(message);
    }

    @Override
    public void sendActionBar(@NotNull Component message) {
        handle.sendActionBar(message);
    }

    @Override
    public void sendMessage(String message) {
        handle.sendMessage(Component.text().content(message));
    }

    @Override
    public boolean hasPermission(String permission) {
        return handle.hasPermission(permission);
    }

    @Override
    public String getServerName() {
        Optional<ServerConnection> serverConnection = handle.getCurrentServer();
        if(!serverConnection.isPresent()) return "none";
        ServerConnection connection = serverConnection.get();
        return connection.getServerInfo().getName();
    }

    @Override
    public UUID getUniqueId() {
        return handle.getUniqueId();
    }

    @Override
    public void connect(AdaptedServer server) {
        handle.createConnectionRequest((RegisteredServer) server.getHandle()).connect().thenAcceptAsync(
                result -> {
                    if(!result.isSuccessful()) {
                        QueueMain.getInstance().getEventHandler().onServerKick(
                                this,
                                server,
                                result.getReasonComponent().orElseGet(() -> Component.text("Connection failed")),
                                false
                        );
                    }
                }
        );
    }

    @Override
    public String getName() {
        return handle.getUsername();
    }

    @Override
    public List<String> getPermissions() {
        throw new IllegalStateException("AdaptedPlayer#getPermissions cannot be used on velocity");
    }

    @Override
    public Player getHandle() {
        return handle;
    }
}
