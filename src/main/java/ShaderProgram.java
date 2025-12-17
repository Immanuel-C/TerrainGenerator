import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.lwjgl.opengl.GL43.*;

public class ShaderProgram {
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
            throw new IllegalStateException("Shader program failed to compile:\n\n" + shaderProgramLog);
        }

        for (int i = 0; i < paths.length; i++) {
            glDeleteShader(shaders[i]);
        }
    }

    void bind() {
        glUseProgram(this.shaderProgram);
        //System.out.println("Bound shader");
    }

    void destroy() {
        glDeleteProgram(this.shaderProgram);
    }

    private int createShader(ShaderInfo path) {
        int type = switch (path.getType()) {
            case Compute -> {
                yield GL_COMPUTE_SHADER;
            }
            case Vertex -> {
                yield GL_VERTEX_SHADER;
            }
            case Fragment -> {
                yield GL_FRAGMENT_SHADER;
            }
        };

        int shader = glCreateShader(type);

        String shaderSource;

        try {
             shaderSource = Files.readString(Path.of(path.getPath()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        glShaderSource(shader, shaderSource);
        glCompileShader(shader);

        int success = glGetShaderi(shader, GL_COMPILE_STATUS);
        
        if (success == 0) {
            String shaderLog = glGetShaderInfoLog(shader);
            throw new IllegalStateException(path.getPath() + " failed to compile:\n\n" + shaderLog);
        }

        return shader;
    }
}
