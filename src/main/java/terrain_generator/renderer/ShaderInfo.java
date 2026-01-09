package terrain_generator.renderer;

import terrain_generator.utils.Resource;
import terrain_generator.utils.ResourceType;

public final class ShaderInfo extends Resource {
    private final String path;

    public ShaderInfo(String path, ResourceType type) {
        super(null, type);
        this.path = path;
    }

    public String getPath() {
        return path;
    }

}
