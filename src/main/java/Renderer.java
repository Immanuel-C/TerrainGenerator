import org.joml.Matrix4f;
import org.joml.Vector3f;

import static org.lwjgl.opengl.GL43.*;

public class Renderer {

    ShaderProgram defaultShader;
    Camera camera;

    Matrix4f model;

    public Renderer(float fov, float canvasWidth, float canvasHeight) {
        ShaderInfo[] shaderInfos = {
                new ShaderInfo("assets/shaders/default.vert", ShaderType.Vertex),
                new ShaderInfo("assets/shaders/default.frag", ShaderType.Fragment),
        };

        this.defaultShader = new ShaderProgram(shaderInfos);
        this.camera = new Camera(new Vector3f(0.0f, 0.0f, 2.0f), (float) (2 * Math.PI / 3), canvasWidth / canvasHeight, 0.1f, 100.0f);

        this.model = new Matrix4f().identity().scale(5);

        int dummy = glGenVertexArrays();

        glBindVertexArray(dummy);
    }

    public void destroy() {
        this.defaultShader.destroy();
    }

    public void render() {
        final float radius = 5.0f;
        this.camera.position.x = (float) (Math.sin(System.currentTimeMillis() / 1000.0) * radius);
        //this.camera.position.z = (float) (Math.cos(System.currentTimeMillis() / 1000.0) * radius);

        this.camera.update();

        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        glClearColor(0.5f, 0.6f, 0.7f, 1.0f);

        this.defaultShader.bind();

        this.camera.uploadViewMatrix(this.defaultShader, "view");
        this.camera.uploadProjectionMatrix(this.defaultShader, "proj");
        this.defaultShader.uploadMatrix4f(this.model, "model");

        glDrawArrays(GL_TRIANGLES, 0, 3);
        ShaderProgram.unBind();
    }

}
