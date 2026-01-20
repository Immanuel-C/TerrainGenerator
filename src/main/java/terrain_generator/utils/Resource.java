package terrain_generator.utils;


import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public abstract class Resource {
    protected final ResourceType type;

    protected Resource(ResourceType type) { this.type = type; }
    public ResourceType getType() { return this.type; }

    // If a resource does not need to be manually destroyed than this method
    // does not need to be overridden.
    public void destroy() {}
    // If a resource does not need hot reloading from disk than this method
    // does not need to be overridden.
    public void recreate(Collection<Resource> dependencies) {}

}

