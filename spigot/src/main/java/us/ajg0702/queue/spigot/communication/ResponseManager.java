package us.ajg0702.queue.spigot.communication;

import us.ajg0702.queue.api.communication.ComResponse;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;


public class ResponseManager {
    private final Map<ResponseKey, Consumer<ComResponse>> responseMap = new ConcurrentHashMap<>();

    public synchronized void awaitResponse(String id, String from, Consumer<ComResponse> callback) {
        ResponseKey key = new ResponseKey(id, from);
        responseMap.merge(key, callback, (a, b) -> r -> {
            b.accept(r);
            a.accept(r);
        });
    }

    public void executeResponse(ComResponse response) {
        ResponseKey key = new ResponseKey(response.getIdentifier(), response.getFrom());
        Consumer<ComResponse> callback = responseMap.get(key);
        if(callback == null) return;
        responseMap.remove(key);
        callback.accept(response);
    }
}
