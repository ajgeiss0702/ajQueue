package us.ajg0702.queue.spigot.placeholders;

import java.util.regex.Matcher;

public class CachedPlaceholder {
    private final Matcher matcher;
    private final Placeholder placeholder;

    public CachedPlaceholder(Matcher matcher, Placeholder placeholder) {
        this.matcher = matcher;
        this.placeholder = placeholder;
    }

    public Matcher getMatcher() {
        return matcher;
    }

    public Placeholder getPlaceholder() {
        return placeholder;
    }
}
