package terrain_generator.renderer;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;
import terrain_generator.utils.Resource;
import terrain_generator.utils.ResourceType;

import java.lang.ref.Reference;
import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL43.*;

public class ShaderProgram extends Resource {
    private int shaderProgram;

    // The source code of each shader isn't stored in the ShaderInfo class because
    // Java would never free the sources since there is something referencing the
    // source. Since a Resources data can be large storing it in memory would be inefficient.
    // This isn't really a problem with Shaders since it's just text but if it were a texture
    // storing it in memory would be ridiculous since it's so large. So to keep consistency
    // no Resource that does not need its data stored in memory should not do so.
    public ShaderProgram(ShaderInfo[] infos, String[] sources) {
        super(infos, ResourceType.ShaderProgram);
        this.createShaderProgram(infos, sources);
    }

    // This method allows hot reloading to work.
    public void createShaderProgram(ShaderInfo[] infos, String[] sources) {
        if (infos.length != sources.length)
            throw new RuntimeException("Shader program sources length != infos length.");

        this.shaderProgram = glCreateProgram();

        int[] shaders = new int[sources.length];

        for (int i = 0; i < sources.length; i++) {
            shaders[i] = this.createShader(infos[i], sources[i]);
            glAttachShader(this.shaderProgram, shaders[i]);
        }

        glLinkProgram(this.shaderProgram);

        int success = glGetProgrami(this.shaderProgram, GL_LINK_STATUS);

        if (success == 0) {
            String shaderProgramLog = glGetProgramInfoLog(this.shaderProgram);
            throw new RuntimeException("Shader program failed to compile:\n\n" + shaderProgramLog);
        }

        for (int i = 0; i < sources.length; i++) {
            glDeleteShader(shaders[i]);
        }
    }

    void bind() {
        glUseProgram(this.shaderProgram);
    }

    static void unBind() {
        glUseProgram(0);
    }

    @Override
    public void destroy() {
        glDeleteProgram(this.shaderProgram);
    }

    public int get() {
        return this.shaderProgram;
    }

    public void uploadMatrix4f(Matrix4f matrix, String uniformName) {
        // This is garbage collected so no need for to free.
        FloatBuffer uniformBuffer = BufferUtils.createFloatBuffer(4*4);
        uniformBuffer = matrix.get(uniformBuffer);

        // Transpose the matrix from row major to column major. Since JOML is already in column major this is not needed.
        glUniformMatrix4fv(this.getUniformLocation(uniformName), false, uniformBuffer);
    }

    public void uploadFloat(float val, String uniformName) {
        glUniform1f(this.getUniformLocation(uniformName), val);
    }

    public void uploadVec3(Vector3f val, String uniformName) {
        glUniform3f(this.getUniformLocation(uniformName), val.x, val.y, val.z);
    }

    private int getUniformLocation(String uniformName) {
        int location = glGetUniformLocation(this.shaderProgram, uniformName);

        if (location == -1)
            throw new RuntimeException("Uniform name provided " + uniformName + " is not found in program " + this.shaderProgram + "\n");

        return location;
    }

    private int createShader(ShaderInfo info, String source) {
        int type = switch (info.getType()) {
            case ComputeShader -> GL_COMPUTE_SHADER;
            case VertexShader -> GL_VERTEX_SHADER;
            case FragmentShader -> GL_FRAGMENT_SHADER;
            default -> throw new RuntimeException("Invalid Shader Type: " + info.getType());
        };

        int shader = glCreateShader(type);


        glShaderSource(shader, source);
        glCompileShader(shader);

        int success = glGetShaderi(shader, GL_COMPILE_STATUS);
        
        if (success == 0) {
            String shaderLog = glGetShaderInfoLog(shader);
            throw new RuntimeException(info.getPath() + " failed to compile:\n\n" + shaderLog);
        }

        return shader;
    }
}
