import org.lwjgl.opengl.GL43;

import java.util.ArrayList;

public class Shader {
    public Shader(ShaderPath[] paths) {
        int[] shaders = new int[paths.length];

        for (int i = 0; i < paths.length; i++) {
            shaders[i] = this.createShader(paths[i]);
        }
    }

    private int createShader(ShaderPath path) {
        return 0;
    }
}
