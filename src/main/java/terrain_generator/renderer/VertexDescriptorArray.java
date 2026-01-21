package terrain_generator.renderer;

import java.util.Collection;

import static org.lwjgl.opengl.GL45.*;


// OpenGL calls it a VertexAttributeObject, but I believe a more fitting name is
// the VertexDescriptorArray. The vertex descriptor array defines how the vertex buffer
// should be interpreted by the vertex shader. It's also important to note that a
// vertex in OpenGL does not only store position data but can also store data related to that
// point these are called attributes.
public class VertexDescriptorArray {
    // ID of buffer on GPU.
    int vao;

    // Generate the vertex array.
    public VertexDescriptorArray() {
        this.vao = glGenVertexArrays();
    }

    // The stride is how long one vertex inside the vertex buffer. V = {Attrib1, Attrib2, ...AttribN}, VertexBuffer = {V1, V2, ...VN}.
    // stride is equal to the number of elements in V.
    public void addVertexDescriptor(int stride, Collection<VertexAttributeDescriptor> attributeSizes) {
        this.bind();

        int i = 0;
        for (VertexAttributeDescriptor attributeDescriptor : attributeSizes) {
            // Loop through each descriptor the index of the descriptor is the current iteration of the loop.
            // i represents the index inside OpenGL which is used for other OpenGL functions but is also the location
            // inside the vertex shader where this attribute will appear.
            // GL_FLOAT represents the data type of the data the attribute holds.
            // The stride of the entire vertex (This shouldn't be included in this function but OpenGL does).
            // Any other parameters are explained in the VertexAttributeDescriptor class.
            glVertexAttribPointer(i, attributeDescriptor.size(), GL_FLOAT, attributeDescriptor.normalize(), stride, attributeDescriptor.offset());
            // Tell OpenGL to enable this attribute for use in vertex shaders.
            glEnableVertexAttribArray(i);
            // Print some debug info.
            System.out.println("Vertex Attribute " + i + ":\nsize: " + attributeDescriptor.size() + " floats\nstride: "
                    + stride + " bytes\noffset: " + attributeDescriptor.offset() + " bytes");

            i++;
        }

        VertexDescriptorArray.unBind();
    }

    public void bind() {
        glBindVertexArray(this.vao);
    }

    public static void unBind() {
        glBindVertexArray(0);
    }

}
