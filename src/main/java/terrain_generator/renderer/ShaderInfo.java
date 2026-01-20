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
    // im storing it. Using a SoftReference<String> (A reference to an object that hints to the
    // garbage collector that it can delete the object if the program needs more memory) or a
    // WeakReference<String> (which tells the garbage collector that it can delete it if
    // it is only weakly reachable. This means if there are no other strong, soft or phantom references
    // it can delete it. A strong reference would just be a String object and phantom references
    // are used to tell when an object is deleted by the garbage collector. This is useful to delete native
    // objects whenever the GC decides to drop it like an OpenGL object but is unused since I only recently learned about it).
    private String source;

    public ShaderInfo(String path, ResourceType.Shader.Type type) {
        super(new ResourceType.Shader(type));
        this.path = path;
    }

    public String getPath() {
        return path;
    }



    public void setSource(String source) {
        this.source = source;
    }

    public Optional<String> getSource() {
        return Optional.ofNullable(this.source);
    }
}
