package terrain_generator.renderer;

import java.util.Collection;
import java.util.List;

// A Renderable object
public class Renderable {
    VertexDescriptorArray vertexDescriptorArray;
    VertexBuffer vertexBuffer;
    IndexBuffer indexBuffer;
    float[] vertices;
    int[] indices;

    public Renderable(Collection<Vertex> vertices, Collection<Integer> indices, int stride, Collection<VertexAttributeDescriptor> attributeDescriptors) {
        this.convertToPrimitives(vertices, indices);

        this.vertexDescriptorArray = new VertexDescriptorArray();
        this.vertexDescriptorArray.bind();

        this.vertexBuffer = new VertexBuffer(this.vertices);
        this.vertexBuffer.bind();

        this.vertexDescriptorArray.addVertexDescriptor(stride, attributeDescriptors);

        this.indexBuffer = new IndexBuffer(this.vertexDescriptorArray, this.indices);
    }

    public void uploadData(Collection<Vertex> vertices, Collection<Integer> indices) {
        this.convertToPrimitives(vertices, indices);
        this.vertexBuffer.uploadData(this.vertices);
        this.indexBuffer.uploadData(this.indices);
    }

    public void bind() {
        this.vertexDescriptorArray.bind();
        this.vertexBuffer.bind();
        this.indexBuffer.bind();
    }

    private void convertToPrimitives(Collection<Vertex> vertices, Collection<Integer> indices) {
        this.vertices = new float[vertices.size() * Vertex.STRIDE];

        List<Vertex> verticesList = vertices.stream().toList();
        List<Integer> indicesList = indices.stream().toList();

        for (int i = 0; i < vertices.size() * Vertex.STRIDE; i += Vertex.STRIDE)  {
            Vertex v = verticesList.get(i / Vertex.STRIDE);
            this.vertices[i] = v.position().x;
            this.vertices[i + 1] = v.position().y;
            this.vertices[i + 2] = v.position().z;

            this.vertices[i + 3] = v.normal().x;
            this.vertices[i + 4] = v.normal().y;
            this.vertices[i + 5] = v.normal().z;
        }

        this.indices = new int[indices.size()];

        for (int i = 0; i < indices.size(); i++)
            this.indices[i] = indicesList.get(i);

    }

}
