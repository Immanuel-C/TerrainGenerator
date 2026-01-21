package terrain_generator.renderer;

import static org.lwjgl.opengl.GL45.*;

// An Index buffer describes how the vertices of an object should be connected.
// It basically says connect v1, v2 and v3 together (for triangles)
// until all the points in the vertex buffer are connected.
// OpenGL calls it an element buffer or EBO but ever other graphics API including
// OpenGL's successor Vulkan, calls it an index buffer.
public class IndexBuffer {
    // ID that represents the object on the GPU.
    int ibo;

    public IndexBuffer(VertexDescriptorArray vertexDescriptorArray, int[] indices) {
        // Generate and bind.
        this.ibo = glGenBuffers();
        // The vertex descriptor array stores a reference to the index buffer
        // so the vertex descriptor array must be bound before binding the index buffer.
        vertexDescriptorArray.bind();
        this.bind();
        // Must unbind the descriptor array before the index buffer.
        // This connects the index buffer and the vertex array.
        VertexDescriptorArray.unBind();
        IndexBuffer.unBind();

        // Then upload data to the index buffer
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
