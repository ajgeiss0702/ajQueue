package us.ajg0702.queue.platforms.bungeecord.players;

import com.viaversion.viaversion.api.Via;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.sound.SoundStop;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.serializer.bungeecord.BungeeComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.kyori.adventure.title.Title;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import org.jetbrains.annotations.NotNull;
import us.ajg0702.queue.api.players.AdaptedPlayer;
import us.ajg0702.queue.api.server.AdaptedServer;
import us.ajg0702.queue.common.utils.Debug;
import us.ajg0702.queue.platforms.bungeecord.BungeeQueue;
import us.ajg0702.queue.platforms.bungeecord.server.BungeeServer;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class BungeePlayer implements AdaptedPlayer, Audience {
    @Override
    public void sendActionBar(@NotNull ComponentLike message) {
        getAudience().sendActionBar(message);
    }

    @Override
    public void showTitle(@NotNull Title title) {
        getAudience().showTitle(title);
    }

    @Override
    public void clearTitle() {
        getAudience().clearTitle();
    }

    @Override
    public void resetTitle() {
        getAudience().resetTitle();
    }

    @Override
    public void showBossBar(@NotNull BossBar bar) {
        getAudience().showBossBar(bar);
    }

    @Override
    public void hideBossBar(@NotNull BossBar bar) {
        getAudience().hideBossBar(bar);
    }

    @Override
    public void playSound(@NotNull Sound sound) {
        getAudience().playSound(sound);
    }

    @Override
    public void playSound(@NotNull Sound sound, double x, double y, double z) {
        getAudience().playSound(sound, x, y, z);
    }

    @Override
    public void stopSound(@NotNull Sound sound) {
        getAudience().stopSound(sound);
    }

    @Override
    public void playSound(@NotNull Sound sound, Sound.@NotNull Emitter emitter) {
        getAudience().playSound(sound, emitter);
    }

    @Override
    public void stopSound(@NotNull SoundStop stop) {
        getAudience().stopSound(stop);
    }

    final ProxiedPlayer handle;

    private final boolean viaAvailable;

    public BungeePlayer(ProxiedPlayer player) {
        handle = player;
        viaAvailable = isClassAvailable("com.viaversion.viaversion.api.Via");
    }

    @Override
    public boolean isConnected() {
        return handle.isConnected();
    }

    @Override
    public void sendMessage(@NotNull Component message) {
        if(PlainTextComponentSerializer.plainText().serialize(message).isEmpty()) return;
        getAudience().sendMessage(message);
    }

    @Override
    public void sendActionBar(@NotNull Component message) {
        if(PlainTextComponentSerializer.plainText().serialize(message).isEmpty()) return;
        getAudience().sendActionBar(message);
    }

    @Override
    public void sendMessage(String message) {
        if(message.isEmpty()) return;
        getAudience().sendMessage(Component.text(message));
    }

    @Override
    public boolean hasPermission(String permission) {
        return handle.hasPermission(permission);
    }

    @Override
    public String getServerName() {
        return handle.getServer().getInfo().getName();
    }

    @Override
    public UUID getUniqueId() {
        return handle.getUniqueId();
    }

    @Override
    public void connect(AdaptedServer server) {
        Debug.info("Attempting to send "+getName()+" to "+server.getName());
        handle.connect(((BungeeServer) server).getHandle());
    }

    @Override
    public int getProtocolVersion() {
        if(viaAvailable) {
            return Via.getAPI().getPlayerVersion(handle.getUniqueId());
        }
        return handle.getPendingConnection().getVersion();
    }

    @Override
    public String getName() {
        return handle.getName();
    }

    @Override
    public void kick(Component reason) {
        handle.disconnect(BungeeComponentSerializer.get().serialize(reason));
    }

    @Override
    public List<String> getPermissions() {
        return new ArrayList<>(handle.getPermissions());
    }

    @Override
    public ProxiedPlayer getHandle() {
        return handle;
    }

    private Audience getAudience() {
        return BungeeQueue.adventure().player(handle);
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
        BungeePlayer that = (BungeePlayer) o;
        return handle.equals(that.handle);
    }

    @Override
    public int hashCode() {
        return Objects.hash(handle);
    }
}
