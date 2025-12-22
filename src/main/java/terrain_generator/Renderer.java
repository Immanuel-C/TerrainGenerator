package terrain_generator;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.ArrayList;

import static org.lwjgl.opengl.GL43.*;

public class Renderer {
    ShaderProgram defaultShader;
    Camera camera;

    Matrix4f model;

    int terrainWidth, terrainLength;

    ArrayList<Float> vertices;
    ArrayList<Integer> indices;

    VertexDescriptorArray vertexDescriptor;
    VertexBuffer vertexBuffer;
    IndexBuffer indexBuffer;

    boolean shouldGenerateTerrainVertices;

    public Renderer(float fov, float canvasWidth, float canvasHeight) {

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


        VertexAttributeDescriptor[] attributeDescriptors = {
                new VertexAttributeDescriptor(4, 0)
        };

        this.vertexDescriptor = new VertexDescriptorArray();
        this.vertexDescriptor.bind();


        //this.generateTerrainData();
        this.shouldGenerateTerrainVertices = false;

        System.out.println("Here");

//        float[] convertedVertices = new float[this.vertices.size()];
//
//        for (int i = 0; i < this.vertices.size(); i++)  {
//            convertedVertices[i] = this.vertices.get(i);
//        }

        float[] convertedVertices = {
                -0.5f, -0.5f, 0.0f, 1.0f,
                -0.5f,  0.5f, 0.0f, 1.0f,
                 0.5f,  0.5f, 0.0f, 1.0f,
                 0.5f, -0.5f, 0.0f, 1.0f,
        };

        this.vertexBuffer = new VertexBuffer(convertedVertices);
        this.vertexBuffer.bind();

        this.vertexDescriptor.addVertexDescriptor(4 * Float.BYTES, attributeDescriptors);


//        int[] convertedIndices = new int[this.indices.size()];
//
//        for (int i = 0; i < this.indices.size(); i++)  {
//            convertedIndices[i] = this.indices.get(i);
//        }

        int[] convertedIndices = {
            0, 1, 2, // BL, TL, TR
            2, 3, 0  // TR, BR, BL
        };



        this.indexBuffer = new IndexBuffer(convertedIndices);

        VertexDescriptorArray.unBind();
        IndexBuffer.unBind();

    }

    public void destroy() {
        this.defaultShader.destroy();
    }

    public void render() {
        final float radius = 5.0f;
        this.camera.position.x = (float) (Math.sin(System.currentTimeMillis() / 1000.0) * radius);
        this.camera.position.z = (float) (Math.cos(System.currentTimeMillis() / 1000.0) * radius);

        this.camera.update();

        glClearColor(0.5f, 0.6f, 0.7f, 1.0f);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);


        this.vertexDescriptor.bind();
        this.vertexBuffer.bind();
        this.indexBuffer.bind();
        this.defaultShader.bind();


        this.camera.uploadViewMatrix(this.defaultShader, "view");
        this.camera.uploadProjectionMatrix(this.defaultShader, "proj");
        this.defaultShader.uploadMatrix4f(this.model, "model");

        glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_INT, 0);

        ShaderProgram.unBind();
        IndexBuffer.unBind();
        VertexBuffer.unBind();
        VertexDescriptorArray.unBind();

    }

    private void generateTerrainData() {
        for (int i = 0; i < this.terrainLength; i++) {
            for (int j = 0; j < this.terrainWidth; j++) {
                this.vertices.add(-this.terrainLength / 2.0f + i); // x
                this.vertices.add(0.0f); // y. The y coord should be calculated in a compute shader.
                this.vertices.add(-this.terrainWidth / 2.0f + j); // z
                this.vertices.add(1.0f); // Padding for SSBO
            }
        }

        for (int i = 0; i < this.terrainLength - 1; i++) {
            for (int j = 0; j < this.terrainWidth; j++) {
                for (int k = 0; k < 2; k++) {
                    this.indices.add(j + this.terrainWidth * (i + k));
                }
            }
        }
    }
}
