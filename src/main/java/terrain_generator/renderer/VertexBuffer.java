package terrain_generator.renderer;

import static org.lwjgl.opengl.GL45.*;

// A VertexBuffer holds vertex data on the GPU
public class VertexBuffer {
    // Vertex buffer ID that represents the memory on the GPU.
    int vbo;

    public VertexBuffer(float[] vertices) {
        // Generate the buffer and upload data to it.
        this.vbo = glGenBuffers();
        this.uploadData(vertices);
    }

    public void uploadData(float[] vertices) {
        // Bind the buffer so OpenGL knows which buffer we are uploading data to.
        this.bind();
        // Tell OpenGL to upload data to the buffer bound to the GL_ARRAY_BUFFER slot.
        // GL_DYNAMIC_DRAW is a hint that tells OpenGL we will be changing the data in the buffer a lot and
        // using it in drawing operations
        glBufferData(GL_ARRAY_BUFFER, vertices, GL_DYNAMIC_DRAW);
        // Unbind, optional.
        VertexBuffer.unBind();
    }

    // Bind the vertex buffer to the GL_ARRAY_BUFFER slot.
    public void bind() {
        glBindBuffer(GL_ARRAY_BUFFER, this.vbo);
    }

    // The ID of 0 kind of represents null as no buffer will have an ID of 0.
    public static void unBind() {
        glBindBuffer(GL_ARRAY_BUFFER, 0);
    }
}
