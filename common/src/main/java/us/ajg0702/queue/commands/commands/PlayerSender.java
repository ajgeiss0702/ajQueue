package us.ajg0702.queue.commands.commands;

import net.kyori.adventure.audience.MessageType;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.identity.Identified;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.inventory.Book;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.sound.SoundStop;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.title.Title;
import net.kyori.adventure.title.TitlePart;
import org.jetbrains.annotations.NotNull;
import us.ajg0702.queue.api.commands.ICommandSender;
import us.ajg0702.queue.api.players.AdaptedPlayer;

import java.util.UUID;

public class PlayerSender implements ICommandSender {

    final AdaptedPlayer handle;

    public PlayerSender(AdaptedPlayer handle) {
        this.handle = handle;
    }

    @Override
    public boolean hasPermission(String permission) {
        return handle.hasPermission(permission);
    }

    @Override
    public boolean isPlayer() {
        return true;
    }

    @Override
    public UUID getUniqueId() throws IllegalStateException {
        return handle.getUniqueId();
    }

    @Override
    public AdaptedPlayer getHandle() {
        return handle;
    }

    @Override
    public void sendMessage(@NotNull ComponentLike message) {
        handle.sendMessage(message);
    }

    @Override
    public void sendMessage(@NotNull Identified source, @NotNull ComponentLike message) {
        handle.sendMessage(source, message);
    }

    @Override
    public void sendMessage(@NotNull Identity source, @NotNull ComponentLike message) {
        handle.sendMessage(source, message);
    }

    @Override
    public void sendMessage(@NotNull Component message) {
        handle.sendMessage(message);
    }

    @Override
    public void sendMessage(@NotNull Identified source, @NotNull Component message) {
        handle.sendMessage(source, message);
    }

    @Override
    public void sendMessage(@NotNull Identity source, @NotNull Component message) {
        handle.sendMessage(source, message);
    }

    @Override
    public void sendMessage(@NotNull ComponentLike message, @NotNull MessageType type) {
        handle.sendMessage(message, type);
    }

    @Override
    public void sendMessage(@NotNull Identified source, @NotNull ComponentLike message, @NotNull MessageType type) {
        handle.sendMessage(source, message, type);
    }

    @Override
    public void sendMessage(@NotNull Identity source, @NotNull ComponentLike message, @NotNull MessageType type) {
        handle.sendMessage(source, message, type);
    }

    @Override
    public void sendMessage(@NotNull Component message, @NotNull MessageType type) {
        handle.sendMessage(message, type);
    }

    @Override
    public void sendMessage(@NotNull Identified source, @NotNull Component message, @NotNull MessageType type) {
        handle.sendMessage(source, message, type);
    }

    @Override
    public void sendMessage(@NotNull Identity source, @NotNull Component message, @NotNull MessageType type) {
        handle.sendMessage(source, message, type);
    }

    @Override
    public void sendActionBar(@NotNull ComponentLike message) {
        handle.sendActionBar(message);
    }

    @Override
    public void sendActionBar(@NotNull Component message) {
        handle.sendActionBar(message);
    }

    @Override
    public void sendPlayerListHeader(@NotNull ComponentLike header) {
        handle.sendPlayerListHeader(header);
    }

    @Override
    public void sendPlayerListHeader(@NotNull Component header) {
        handle.sendPlayerListHeader(header);
    }

    @Override
    public void sendPlayerListFooter(@NotNull ComponentLike footer) {
        handle.sendPlayerListFooter(footer);
    }

    @Override
    public void sendPlayerListFooter(@NotNull Component footer) {
        handle.sendPlayerListFooter(footer);
    }

    @Override
    public void sendPlayerListHeaderAndFooter(@NotNull ComponentLike header, @NotNull ComponentLike footer) {
        handle.sendPlayerListHeaderAndFooter(header, footer);
    }

    @Override
    public void sendPlayerListHeaderAndFooter(@NotNull Component header, @NotNull Component footer) {
        handle.sendPlayerListHeaderAndFooter(header, footer);
    }

    @Override
    public void showTitle(@NotNull Title title) {
        handle.showTitle(title);
    }

    @Override
    public <T> void sendTitlePart(@NotNull TitlePart<T> part, @NotNull T value) {
        handle.sendTitlePart(part, value);
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

    @Override
    public void openBook(Book.@NotNull Builder book) {
        handle.openBook(book);
    }

    @Override
    public void openBook(@NotNull Book book) {
        handle.openBook(book);
    }
}
