package terrain_generator.renderer;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;
import terrain_generator.utils.Resource;

import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.lwjgl.opengl.GL43.*;

public class ShaderProgram implements Resource {
    int shaderProgram;

    public ShaderProgram(ShaderInfo[] paths) {
        this.shaderProgram = glCreateProgram();

        int[] shaders = new int[paths.length];

        for (int i = 0; i < paths.length; i++) {
            shaders[i] = this.createShader(paths[i]);
            glAttachShader(this.shaderProgram, shaders[i]);
        }

        glLinkProgram(this.shaderProgram);

        int success = glGetProgrami(this.shaderProgram, GL_LINK_STATUS);

        if (success == 0) {
            String shaderProgramLog = glGetProgramInfoLog(this.shaderProgram);
            throw new RuntimeException("Shader program failed to compile:\n\n" + shaderProgramLog);
        }

        for (int i = 0; i < paths.length; i++) {
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

    private int createShader(ShaderInfo path) {
        int type = switch (path.type()) {
            case Compute -> GL_COMPUTE_SHADER;
            case Vertex -> GL_VERTEX_SHADER;
            case Fragment -> GL_FRAGMENT_SHADER;
        };

        int shader = glCreateShader(type);

        String shaderSource;

        try {
             shaderSource = Files.readString(Path.of(path.path()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        glShaderSource(shader, shaderSource);
        glCompileShader(shader);

        int success = glGetShaderi(shader, GL_COMPILE_STATUS);
        
        if (success == 0) {
            String shaderLog = glGetShaderInfoLog(shader);
            throw new RuntimeException(path.path() + " failed to compile:\n\n" + shaderLog);
        }

        return shader;
    }
}
