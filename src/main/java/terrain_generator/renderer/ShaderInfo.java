package terrain_generator.renderer;

import terrain_generator.utils.Resource;
import terrain_generator.utils.ResourceType;

import java.lang.ref.WeakReference;
import java.util.Optional;

public final class ShaderInfo extends Resource {
    private final String path;
    // If no other references exist of the String stored in the WeakReference than the garbage collector will free
    // the string inside the WeakReference. Storing the source of a resource is wasteful. For shaders this may not matter much.
    // But for something like images this could waste a significant amount of memory.
    private WeakReference<String> source;

    public ShaderInfo(String path, ResourceType.Shader.Type type) {
        super(new ResourceType.Shader(type));
        this.path = path;
    }

    public String getPath() {
        return path;
    }

    public Optional<String> getSource() {
        return Optional.ofNullable(source.get());
    }

    public void setSource(String source) {
        this.source = new WeakReference<>(source);
    }

}
