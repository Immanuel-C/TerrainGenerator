package terrain_generator.renderer;

import terrain_generator.utils.Resource;
import terrain_generator.utils.ResourceType;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ShaderSource implements Resource {
    private String source;
    private ResourceType type;

    public ShaderSource(ResourceType type, Path shaderPath) throws IOException {
        this.source = Files.readString(shaderPath);

        switch (type) {
            case VertexShader, FragmentShader, ComputeShader -> {
                this.type = type;
            }
            default -> throw new IllegalArgumentException("Non Shader Resource Type: " + type);
        }
    }

    @Override
    public void destroy() {}

    public ResourceType getType() {
        return type;
    }

    public String getSource() {
        return source;
    }
}
