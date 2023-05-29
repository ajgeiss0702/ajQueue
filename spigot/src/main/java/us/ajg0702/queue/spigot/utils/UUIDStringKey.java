package us.ajg0702.queue.spigot.utils;

import java.util.Objects;
import java.util.UUID;

public class UUIDStringKey {

    private final UUID uuid;
    private final String string;

    public UUIDStringKey(UUID uuid, String string) {
        this.uuid = uuid;
        this.string = string;
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getString() {
        return string;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UUIDStringKey)) return false;
        UUIDStringKey that = (UUIDStringKey) o;
        return uuid.equals(that.uuid) && string.equals(that.string);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid, string);
    }
}
