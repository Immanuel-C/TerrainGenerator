package terrain_generator;

import static org.lwjgl.opengl.GL43.*;

public class IndexBuffer {
    int ibo;

    public IndexBuffer(int[] indices) {
        this.ibo = glGenBuffers();

        this.bind();

        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indices, GL_STATIC_DRAW);
    }

    public void bind() {
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, this.ibo);
    }

    public static void unBind() {
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
    }
}
