package us.ajg0702.queue.platforms.velocity.players;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.viaversion.viaversion.api.Via;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.sound.SoundStop;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.kyori.adventure.title.Title;
import org.jetbrains.annotations.NotNull;
import us.ajg0702.queue.api.players.AdaptedPlayer;
import us.ajg0702.queue.api.server.AdaptedServer;
import us.ajg0702.queue.common.QueueMain;
import us.ajg0702.queue.common.utils.Debug;
import us.ajg0702.queue.platforms.velocity.server.VelocityServer;

import java.util.List;
import java.util.Objects;
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

    private static Boolean viaAvailable = null;

    private final QueueMain main;

    public VelocityPlayer(Player player) {
        handle = player;
        if(viaAvailable == null) viaAvailable = isClassAvailable("com.viaversion.viaversion.api.Via");
        main = QueueMain.getInstance();
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
        sendMessage(Component.text(message));
    }

    @Override
    public boolean hasPermission(String permission) {
        return handle.hasPermission(permission);
    }

    @Override
    public String getServerName() {
        AdaptedServer currentServer = getCurrentServer();
        if(currentServer == null) return null;
        return currentServer.getName();
    }

    @Override
    public AdaptedServer getCurrentServer() {
        Optional<ServerConnection> serverConnection = handle.getCurrentServer();
        if(!serverConnection.isPresent()) return null;
        ServerConnection connection = serverConnection.get();
        return new VelocityServer(connection.getServer());
    }

    @Override
    public UUID getUniqueId() {
        return handle.getUniqueId();
    }

    @Override
    public void connect(AdaptedServer server) {
        Debug.info("Attempting to send "+getName()+" to "+server.getName());
        handle.createConnectionRequest((RegisteredServer) server.getHandle()).connect().thenAcceptAsync(
                result -> {
                    if(!result.isSuccessful()) {
                        QueueMain main = QueueMain.getInstance();
                        Component reason = result.getReasonComponent().orElse(null);
                        if(reason == null) {
                            switch (result.getStatus()) {
                                case SUCCESS:
                                    reason = main.getMessages().getComponent("velocity-built-in-kick-messages.success");
                                    break;
                                case ALREADY_CONNECTED:
                                    reason = main.getMessages().getComponent("velocity-built-in-kick-messages.already-connected");
                                    break;
                                case CONNECTION_IN_PROGRESS:
                                    reason = main.getMessages().getComponent("velocity-built-in-kick-messages.already-connecting");
                                    break;
                                case CONNECTION_CANCELLED:
                                    reason = main.getMessages().getComponent("velocity-built-in-kick-messages.cancelled");
                                    break;
                                case SERVER_DISCONNECTED:
                                    reason = main.getMessages().getComponent("velocity-built-in-kick-messages.disconnected");
                                    break;
                            }
                        }

                        try {
                            if(main.getConfig().getBoolean("velocity-kick-message")) {
                                handle.sendMessage(
                                        main.getMessages().getComponent(
                                                "velocity-kick-message",
                                                "SERVER:" + server.getName(),
                                                "REASON:" +
                                                        LegacyComponentSerializer.legacyAmpersand()
                                                                .serialize(reason)
                                                                .replaceAll("§", "&")
                                ));
                            }
                        } catch(Exception e) {
                            main.getLogger().warn("Error while sending velocity-kick-message:", e);
                        }
                        main.getEventHandler().onServerKick(
                                this,
                                server,
                                reason,
                                false
                        );
                    }
                }
        );
    }

    @Override
    public int getProtocolVersion() {
        if(viaAvailable) {
            return Via.getAPI().getPlayerVersion(handle.getUniqueId());
        }
        return handle.getProtocolVersion().getProtocol();
    }

    @Override
    public String getName() {
        return handle.getUsername();
    }

    @Override
    public void kick(Component reason) {
        handle.disconnect(reason);
    }

    @Override
    public List<String> getPermissions() {
        throw new IllegalStateException("AdaptedPlayer#getPermissions cannot be used on velocity");
    }

    @Override
    public Player getHandle() {
        return handle;
    }


    private static boolean isClassAvailable(String className) {
        try {
            Class.forName(className);
        } catch(Exception e) {
            return false;
        }
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VelocityPlayer that = (VelocityPlayer) o;
        return handle.equals(that.handle);
    }

    @Override
    public int hashCode() {
        return Objects.hash(handle);
    }
}
