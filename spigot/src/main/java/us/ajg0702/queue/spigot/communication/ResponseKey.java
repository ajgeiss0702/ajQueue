package us.ajg0702.queue.spigot.communication;

import java.util.Objects;

public class ResponseKey {
    private final String id;
    private final String from;

    public ResponseKey(String id, String from) {
        this.id = id;
        this.from = from;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ResponseKey that = (ResponseKey) o;
        return id.equals(that.id) && from.equals(that.from);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, from);
    }

    @Override
    public String toString() {
        return "ResponseKey{" +
                "id='" + id + '\'' +
                ", from='" + from + '\'' +
                '}';
    }
}
