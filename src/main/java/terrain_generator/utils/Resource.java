package terrain_generator.utils;


import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public abstract class Resource {
    protected final ResourceType type;
    protected Set<Resource> dependencies;


    protected Resource(Collection<Resource> dependencies, ResourceType type) {
        this.dependencies = ConcurrentHashMap.newKeySet();

        dependencies
                .stream()
                .filter(Objects::nonNull)
                .forEach(this.dependencies::add);

        this.type = type;
    }

    public ResourceType getType() { return this.type; }
    public void destroy() {}

    public void recreate() {}

}

