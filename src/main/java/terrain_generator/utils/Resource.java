package terrain_generator.utils;


import java.util.Optional;

public abstract class Resource {
    protected final ResourceType type;

    protected Resource(Resource[] dependencies, ResourceType type) {
        this.type = type;
    }

    public ResourceType getType() { return this.type; }
    public void destroy() {}

}

