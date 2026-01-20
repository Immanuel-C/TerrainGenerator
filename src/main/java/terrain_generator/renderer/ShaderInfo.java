package terrain_generator.renderer;

import terrain_generator.utils.Resource;
import terrain_generator.utils.ResourceType;

import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.util.Optional;

public final class ShaderInfo extends Resource {
    private final String path;
    // The source shouldn't be stored at all since a Resources data could be very large.
    // But since im running out of time and couldn't find a way to do this in time
    // im storing it.
    private String source;

    public ShaderInfo(String path, ResourceType.Shader.Type type) {
        super(new ResourceType.Shader(type));
        this.path = path;
    }

    public String getPath() {
        return path;
    }
}
