package terrain_generator.renderer;

import static org.lwjgl.opengl.GL43.*;

public class VertexDescriptorArray {
    int vao;

    public VertexDescriptorArray() {
        this.vao = glGenVertexArrays();
    }

    public void addVertexDescriptor(int stride, VertexAttributeDescriptor[] attributeSizes) {
        this.bind();

        for (int i = 0; i < attributeSizes.length; i++) {
            glVertexAttribPointer(i, attributeSizes[i].size(), GL_FLOAT, false, stride, (long)attributeSizes[i].offset());
            glEnableVertexAttribArray(i);
            System.out.println("Vertex Attribute " + i + ":\nsize: " + attributeSizes[i].size() + " floats\nstride: "
                    + stride + " bytes\noffset: " + attributeSizes[i].offset() + " bytes");
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
