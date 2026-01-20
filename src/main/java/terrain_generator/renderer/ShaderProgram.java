package terrain_generator.renderer;

import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL46;
import terrain_generator.UniformNotFoundException;
import terrain_generator.utils.Resource;
import terrain_generator.utils.ResourceType;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;

import static org.lwjgl.opengl.GL45.*;

public class ShaderProgram extends Resource {
    private int shaderProgram;

    // The source code of each shader isn't stored in the ShaderInfo class because
    // Java would never free the sources since there is something referencing the
    // source. Since a Resources data can be large, storing it in memory would be inefficient.
    // This isn't really a problem with Shaders since it's just text but if it were a texture
    // storing it in memory would be ridiculous since it could be very large. So to keep consistency
    // no Resource that does not need its data stored in memory should not do so.
    public ShaderProgram(Collection<ShaderInfo> shaderInfos) {
        super(new ResourceType.ShaderProgram());
        this.create(shaderInfos);
    }

    @Override
    public void recreate(Collection<Resource> dependencies) {
        // Destroy the original shader program. This is fine to do since this method will never be called async but will always be
        // called on the render thread meaning that it will never be destroyed will it is being used by the GPU's driver.
        this.destroy();
        this.create(dependencies
                .stream()
                .filter(dependency -> dependency.getType().getClass() == ResourceType.Shader.class)
                .map(dependency -> (ShaderInfo)dependency)
                .collect(Collectors.toList())
        );
    }

    private void create(Collection<ShaderInfo> shaderInfos) {
        this.shaderProgram = glCreateProgram();

        ArrayList<Integer> shaders = new ArrayList<>(shaderInfos.size());
        shaderInfos
                .stream()
                .distinct()
                .map(this::createShader)
                .forEach(shader -> {
                    shaders.add(shader);
                    glAttachShader(this.shaderProgram, shader);
                });

        glLinkProgram(this.shaderProgram);

        int success = glGetProgrami(this.shaderProgram, GL_LINK_STATUS);

        // C does not have booleans int's are used as booleans instead. 0 == false, i != 0 is true.
        // This is useful with pointers as you could do if (!p) {...} to check if the pointer is null.
        if (success == 0) {
            String shaderProgramLog = glGetProgramInfoLog(this.shaderProgram);
            throw new RuntimeException("Shader program failed to compile:\n\n" + shaderProgramLog);
        }

        shaders.forEach(GL46::glDeleteShader);
    }

    public void bind() {
        glUseProgram(this.shaderProgram);
    }

    public void unBind() {
        glUseProgram(0);
    }

    @Override
    public void destroy() {
        glDeleteProgram(this.shaderProgram);
    }

    public int get() {
        return this.shaderProgram;
    }

    public void uploadMatrix4f(Matrix4f matrix, String uniformName)  throws UniformNotFoundException {
        // This is garbage collected so no need for to free.
        FloatBuffer uniformBuffer = BufferUtils.createFloatBuffer(4*4);
        uniformBuffer = matrix.get(uniformBuffer);

        // Transpose the matrix from row major to column major. Since JOML is already in column major this is not needed.
        glUniformMatrix4fv(this.getUniformLocation(uniformName), false, uniformBuffer);
    }

    public void uploadMatrix3f(Matrix3f matrix, String uniformName)  throws UniformNotFoundException {
        // This is garbage collected so no need for to free.
        FloatBuffer uniformBuffer = BufferUtils.createFloatBuffer(3*3);
        uniformBuffer = matrix.get(uniformBuffer);

        // Transpose the matrix from row major to column major. Since JOML is already in column major this is not needed.
        glUniformMatrix3fv(this.getUniformLocation(uniformName), false, uniformBuffer);
    }

    public void uploadFloat(float val, String uniformName)  throws UniformNotFoundException {
        glUniform1f(this.getUniformLocation(uniformName), val);
    }

    public void uploadVec3(Vector3f val, String uniformName) throws UniformNotFoundException {
        glUniform3f(this.getUniformLocation(uniformName), val.x, val.y, val.z);
    }

    private int getUniformLocation(String uniformName) throws UniformNotFoundException {
        int location = glGetUniformLocation(this.shaderProgram, uniformName);

        if (location == -1)
            throw new UniformNotFoundException("Uniform name provided " + uniformName + " is not found in program " + this.shaderProgram + "\n");

        return location;
    }


    private int createShader(ShaderInfo shaderInfo) {
        int type = switch (shaderInfo.getType()) {
            case ResourceType.Shader shaderType ->
                switch (shaderType.type()) {
                    case Compute -> GL_COMPUTE_SHADER;
                    case Vertex -> GL_VERTEX_SHADER;
                    case Fragment -> GL_FRAGMENT_SHADER;
                };
            default -> throw new RuntimeException("Invalid Shader Type: " + shaderInfo.getType());
        };

        int shader = glCreateShader(type);

        String source = shaderInfo
                .getSource()
                .orElseThrow(() -> new IllegalStateException("A shader source must be present when creating a shader"));

        glShaderSource(shader, source);
        glCompileShader(shader);

        int success = glGetShaderi(shader, GL_COMPILE_STATUS);
        
        if (success == 0) {
            String shaderLog = glGetShaderInfoLog(shader);
            throw new RuntimeException(shaderInfo.getPath() + " failed to compile:\n\n" + shaderLog);
        }

        return shader;
    }

}
