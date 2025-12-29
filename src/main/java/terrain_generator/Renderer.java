package terrain_generator;

import org.joml.Matrix4f;
import org.joml.SimplexNoise;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.HashMap;

import static org.lwjgl.opengl.GL43.*;

public class Renderer {
    ShaderProgram defaultShader;
    Camera camera;

    Matrix4f model;

    int terrainWidth, terrainLength;

    ArrayList<Vertex> vertices;
    ArrayList<Integer> indices;
    HashMap<Integer, Vector3f> normals;

    VertexDescriptorArray vertexDescriptor;
    VertexBuffer vertexBuffer;
    IndexBuffer indexBuffer;

    boolean shouldGenerateTerrainVertices;

    public Renderer(float fov, float canvasWidth, float canvasHeight) {

        glEnable(GL_DEPTH_TEST);


        System.out.println("Here");
        ShaderInfo[] shaderInfos = {
                new ShaderInfo("assets/shaders/default.vert", ShaderType.Vertex),
                new ShaderInfo("assets/shaders/default.frag", ShaderType.Fragment),
        };

        this.defaultShader = new ShaderProgram(shaderInfos);
        this.camera = new Camera(new Vector3f(0.0f, 0.0f, 2.0f), fov, canvasWidth / canvasHeight, 0.1f, 100.0f);

        this.model = new Matrix4f().identity().scale(5);

        this.terrainWidth = 128;
        this.terrainLength = 128;

        this.vertices = new ArrayList<>();
        this.indices = new ArrayList<>();
        this.normals = new HashMap<>();

        VertexAttributeDescriptor[] attributeDescriptors = {
                new VertexAttributeDescriptor(3, 0),
                new VertexAttributeDescriptor(3, 3 * Float.BYTES),
        };

        this.vertexDescriptor = new VertexDescriptorArray();
        this.vertexDescriptor.bind();


        this.generateTerrainData();
        this.shouldGenerateTerrainVertices = false;

        System.out.println("Here");

        float[] convertedVertices = new float[this.vertices.size() * 6];

        for (int i = 0; i < this.vertices.size() * 6; i += 6)  {
            Vertex v = this.vertices.get(i / 6);
            convertedVertices[i] = v.position().x;
            convertedVertices[i + 1] = v.position().y;
            convertedVertices[i + 2] = v.position().z;

            convertedVertices[i + 3] = v.normal().x;
            convertedVertices[i + 4] = v.normal().y;
            convertedVertices[i + 5] = v.normal().z;
        }

//        float[] convertedVertices = {
//                -0.5f, -0.5f, 0.0f, 1.0f,
//                -0.5f,  0.5f, 0.0f, 1.0f,
//                 0.5f,  0.5f, 0.0f, 1.0f,
//                 0.5f, -0.5f, 0.0f, 1.0f,
//        };

        this.vertexBuffer = new VertexBuffer(convertedVertices);
        this.vertexBuffer.bind();

        this.vertexDescriptor.addVertexDescriptor(6 * Float.BYTES, attributeDescriptors);


        int[] convertedIndices = new int[this.indices.size()];

        for (int i = 0; i < this.indices.size(); i++)  {
            convertedIndices[i] = this.indices.get(i);
        }

//        int[] convertedIndices = {
//            0, 1, 2, // BL, TL, TR
//            2, 3, 0  // TR, BR, BL
//        };

        this.indexBuffer = new IndexBuffer(convertedIndices);

        VertexDescriptorArray.unBind();
        IndexBuffer.unBind();

        this.camera.position.y += 10.0f;
        this.camera.position.z += 10.0f;


        this.model.scale(0.01f);

    }

    public void destroy() {
        this.defaultShader.destroy();
    }

    public void render() {
        final float radius = 15.0f;
        this.camera.position.x = (float) (Math.sin(System.currentTimeMillis() / 1000.0) * radius);
        this.camera.position.z = (float) (Math.cos(System.currentTimeMillis() / 1000.0) * radius);


        //glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);

        this.camera.update();


        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        glClearColor(0.5f, 0.6f, 0.7f, 1.0f);


        this.vertexDescriptor.bind();
        this.vertexBuffer.bind();
        this.indexBuffer.bind();
        this.defaultShader.bind();


        this.camera.uploadViewMatrix(this.defaultShader, "view");
        this.camera.uploadProjectionMatrix(this.defaultShader, "proj");
        this.defaultShader.uploadMatrix4f(this.model, "model");

        glDrawElements(GL_TRIANGLES, this.indices.size(), GL_UNSIGNED_INT, 0);

        ShaderProgram.unBind();
        IndexBuffer.unBind();
        VertexBuffer.unBind();
        VertexDescriptorArray.unBind();

    }

    private void generateTerrainData() {
        int width = 256, length = 256;

        // Create a grid of
        for (int x = -width / 2; x < width / 2; x++)  {
            for (int z = -length / 2; z < length / 2; z++) {
                this.vertices.add(
                        new Vertex(
                                new Vector3f(x, this.noise(x, z), z),
                                new Vector3f(0.0f)
                        )
                );
            }
        }

        for (int row = 0; row < width - 1; row++) {
            for (int column = 0; column < length - 1; column++) {
                // Top Left
                int v1 = row * length + column;
                // Bottom Left
                int v2 = v1 + length;
                // Top Right
                int v3 = v1 + 1;
                // Bottom Right
                int v4 = v2 + 1;

                this.indices.add(v3);
                this.indices.add(v1);
                this.indices.add(v2);

                Vertex v1Vertex = this.vertices.get(v1);
                Vertex v2Vertex = this.vertices.get(v2);
                Vertex v3Vertex = this.vertices.get(v3);

                Vector3f edge1 = new Vector3f(v1Vertex.position()).sub(v2Vertex.position());
                Vector3f edge2 = new Vector3f(v3Vertex.position()).sub(v2Vertex.position());

                Vector3f normal = edge1.cross(edge2);

                v1Vertex.normal().add(normal);
                v2Vertex.normal().add(normal);
                v3Vertex.normal().add(normal);

                this.indices.add(v2);
                this.indices.add(v4);
                this.indices.add(v3);

                Vertex v4Vertex = this.vertices.get(v4);

                edge1 = new Vector3f(v2Vertex.position()).sub(v4Vertex.position());
                edge2 = new Vector3f(v3Vertex.position()).sub(v4Vertex.position());

                normal = edge1.cross(edge2);

                v4Vertex.normal().add(normal);
                v2Vertex.normal().add(normal);
                v3Vertex.normal().add(normal);
            }

        }

        for (int i = 0; i < this.vertices.size(); i++) {
            this.vertices.get(i).normal().normalize();
        }

    }

    private float noise(float x, float y) {
        int octaves = 12;
        float lacunarity = 1.4f;   // frequency multiplier
        float persistence = 0.5f;  // amplitude multiplier

        float height = 0.0f;
        float frequency = 0.01f;
        float amplitude = 20.0f;

        for (int i = 0; i < octaves; i++) {
            height += SimplexNoise.noise(x * frequency, y * frequency) * amplitude;
            frequency *= lacunarity;
            amplitude *= persistence;
        }


        return height;
    }

}
