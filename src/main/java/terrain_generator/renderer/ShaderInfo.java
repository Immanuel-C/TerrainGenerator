package terrain_generator.renderer;

import terrain_generator.utils.Resource;
import terrain_generator.utils.ResourceType;

import java.util.Optional;

// ShaderInfo describes the type of shader (vertex/fragment/compute (unused)) .
// The path to the shader file and the source code of the shader.
public final class ShaderInfo extends Resource {
    private final String path;
    // The source shouldn't be stored at all since a Resources data could be very large.
    // But since im running out of time and couldn't find a way to do this in time
    // im storing it. Using a SoftReference<String> (A reference to an object that hints to the
    // garbage collector that it can delete the object if the program needs more memory) or a
    // WeakReference<String> (which tells the garbage collector that it can delete it if
    // it is only weakly reachable. This means if there are no other strong, soft or phantom references
    // it can delete it. A strong reference would just be a String object and phantom references
    // are used to tell when an object is deleted by the garbage collector. This is useful to delete native
    // objects whenever the GC decides to drop it like an OpenGL object but is unused since I only recently learned about it).
    private String source;

    // The ShaderInfo constructor only takes in a shader type and a path. The source is to be set by the resource manager.
    public ShaderInfo(String path, ResourceType.ShaderInfo.Type type) {
        // Set the Resource type which is a Shader Info which its shader type is provided in the constructor.
        super(new ResourceType.ShaderInfo(type));
        this.path = path;
    }

    public String getPath() {
        return path;
    }

    public void setSource(String source) {
        this.source = source;
    }

    // Optional is used to force the caller of this method to check if the source is in an invalid state.
    // ofNullable checks if source is null if it is then the value is not present if source is not null then the source is present.
    public Optional<String> getSource() {
        return Optional.ofNullable(this.source);
    }
}
