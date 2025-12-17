import static org.lwjgl.opengl.GL43.*;

public class Renderer {

    ShaderProgram sp;

    public Renderer() {
        ShaderInfo[] shaderInfos = {
                new ShaderInfo("assets/shaders/default.vert", ShaderType.Vertex),
                new ShaderInfo("assets/shaders/default.frag", ShaderType.Fragment),
        };

        this.sp = new ShaderProgram(shaderInfos);

        int dummy = glGenVertexArrays();

        glBindVertexArray(dummy);
    }

    public void destroy() {
        this.sp.destroy();
    }

    public void render() {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        glClearColor(0.5f, 0.6f, 0.7f, 1.0f);

        this.sp.bind();
        glDrawArrays(GL_TRIANGLES, 0, 3);
    }

}
