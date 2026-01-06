package terrain_generator;

import static org.lwjgl.opengl.GL43.*;

public class IndexBuffer {
    int ibo;

    public IndexBuffer(VertexDescriptorArray vertexDescriptorArray, int[] indices) {
        this.ibo = glGenBuffers();
        vertexDescriptorArray.bind();
        this.bind();
        // Must unbind the descriptor array before the index buffer.
        // This connects the index buffer and the vertex array.
        VertexDescriptorArray.unBind();
        IndexBuffer.unBind();

        this.uploadData(indices);
    }

    void uploadData(int[] indices) {
        this.bind();

        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indices, GL_DYNAMIC_DRAW);

        IndexBuffer.unBind();
    }

    public void bind() {
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, this.ibo);
    }

    public static void unBind() {
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
    }
}
