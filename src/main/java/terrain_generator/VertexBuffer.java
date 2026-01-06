package terrain_generator;

import static org.lwjgl.opengl.GL43.*;

public class VertexBuffer {

    int vbo;

    public VertexBuffer(float[] vertices) {
        this.vbo = glGenBuffers();

        this.uploadData(vertices);
    }

    public void uploadData(float[] vertices) {
        this.bind();

        glBufferData(GL_ARRAY_BUFFER, vertices, GL_DYNAMIC_DRAW);

        VertexBuffer.unBind();
    }

    public void bind() {
        glBindBuffer(GL_ARRAY_BUFFER, this.vbo);
    }

    public static void unBind() {
        glBindBuffer(GL_ARRAY_BUFFER, 0);
    }
}
