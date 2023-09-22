package us.ajg0702.queue.api.premium;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class PermissionHookRegistry {
    private final Map<String, PermissionHook> hooks = new HashMap<>();

    public void register(PermissionHook... permissionHooks) {
        for (PermissionHook hook : permissionHooks) {
            if(hooks.containsKey(hook.getName())) {
                throw new IllegalArgumentException("Hook " + hook.getName() + " is already registered!");
            }
        }
        for (PermissionHook hook : permissionHooks) {
            hooks.put(hook.getName(), hook);
        }
    }

    public Collection<PermissionHook> getRegisteredHooks() {
        return hooks.values();
    }
}
