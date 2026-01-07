package terrain_generator.renderer;

import org.joml.Matrix4f;
import org.joml.SimplexNoise;
import org.joml.Vector3f;
import terrain_generator.utils.Camera;
import terrain_generator.RenderSettings;
import terrain_generator.TerrainState;

import java.util.ArrayList;

import static org.lwjgl.opengl.GL43.*;

public class Renderer {
    ShaderProgram defaultShader;
    Camera camera;

    Matrix4f model;


    ArrayList<Vertex> vertices;
    ArrayList<Integer> indices;
    Renderable terrain;

    ArrayList<Vertex> normalDebugLinesVertices;
    Renderable normalDebugLines;


    TerrainState terrainState;
    RenderSettings renderSettings;

    public Renderer(TerrainState terrainState, RenderSettings renderSettings, float fov, float canvasWidth, float canvasHeight) {

        glEnable(GL_DEPTH_TEST);
        glEnable(GL_CULL_FACE);
        glCullFace(GL_BACK);


        System.out.println("Here");
        ShaderInfo[] shaderInfos = {
                new ShaderInfo("assets/shaders/default.vert", ShaderType.Vertex),
                new ShaderInfo("assets/shaders/default.frag", ShaderType.Fragment),
        };

        this.defaultShader = new ShaderProgram(shaderInfos);
        this.camera = new Camera(new Vector3f(0.0f, 0.0f, 2.0f), fov, canvasWidth / canvasHeight, 0.1f, 100.0f);

        this.model = new Matrix4f().identity().scale(5);


        this.vertices = new ArrayList<>();
        this.indices = new ArrayList<>();
        this.normalDebugLinesVertices = new ArrayList<>();

        VertexAttributeDescriptor[] attributeDescriptors = {
                new VertexAttributeDescriptor(3, 0),
                new VertexAttributeDescriptor(3, 3 * Float.BYTES),
        };

        this.terrainState = terrainState;
        this.renderSettings = renderSettings;
        this.generateTerrainData();

        this.terrain = new Renderable(this.vertices, this.indices, 6 * Float.BYTES, attributeDescriptors);

        ArrayList<Integer> dummyIndices = new ArrayList<>();
        dummyIndices.add(0);
        this.normalDebugLines = new Renderable(this.normalDebugLinesVertices, dummyIndices, 6 * Float.BYTES, attributeDescriptors);

        this.camera.position.y += 5.0f;
        this.camera.position.z += 5.0f;


        this.model.scale(0.01f);
    }

    // TODO: Destroy all opengl objects.
    public void destroy() {
        this.defaultShader.destroy();
    }

    public void resizeViewport(int x, int y, int width, int height) {
        glViewport(x, y, width, height);
    }

    public void render() {
        final float radius = 15.0f;
        //this.camera.position.x = (float) (Math.sin(System.currentTimeMillis() / 1000.0) * radius);
        //this.camera.position.z = (float) (Math.cos(System.currentTimeMillis() / 1000.0) * radius);


        if (this.renderSettings.wireFrame)
            glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
        else
            glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);


        if (this.terrainState.shouldGenerateTerrain) {
            this.terrainState.shouldGenerateTerrain = false;
            this.generateTerrainData();
            this.terrain.uploadData(this.vertices, this.indices);
            ArrayList<Integer> dummyIndices = new ArrayList<>();
            dummyIndices.add(0);
            this.normalDebugLines.uploadData(this.normalDebugLinesVertices, dummyIndices);
        }


        this.camera.update();

        glClearColor(0.5f, 0.6f, 0.7f, 1.0f);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        this.terrain.bind();
        this.defaultShader.bind();


        this.camera.uploadViewMatrix(this.defaultShader, "view");
        this.camera.uploadProjectionMatrix(this.defaultShader, "proj");
        this.defaultShader.uploadMatrix4f(this.model, "model");
        this.defaultShader.uploadFloat(this.renderSettings.ambientStrength, "ambientStrength");

        glDrawElements(GL_TRIANGLES, this.indices.size(), GL_UNSIGNED_INT, 0);

        ShaderProgram.unBind();
        IndexBuffer.unBind();
        VertexBuffer.unBind();
        VertexDescriptorArray.unBind();

        this.normalDebugLines.bind();
        this.defaultShader.bind();

        glDrawArrays(GL_LINES, 0, this.normalDebugLinesVertices.size());

    }

    private void generateTerrainData() {
        this.vertices.clear();
        this.indices.clear();
        this.normalDebugLinesVertices.clear();

        // Create a grid of
        for (int x = -this.terrainState.width / 2; x < this.terrainState.width / 2; x++)  {
            for (int z = -this.terrainState.length / 2; z < this.terrainState.length / 2; z++) {
                this.vertices.add(
                        new Vertex(
                                new Vector3f(x, this.noise(x, z), z),
                                new Vector3f(0.0f)
                        )
                );
            }
        }

        // Compute 2 triangles from the terrain vertices
        for (int row = 0; row < this.terrainState.width - 1; row++) {
            for (int column = 0; column < this.terrainState.length - 1; column++) {
                // Top Left
                int v1 = row * this.terrainState.length + column;
                // Bottom Left
                int v2 = v1 + this.terrainState.length;
                // Top Right
                int v3 = v1 + 1;
                // Bottom Right
                int v4 = v2 + 1;

                // First Triangle

                this.indices.add(v2);
                this.indices.add(v1);
                this.indices.add(v3);


                Vertex v1Vertex = this.vertices.get(v1);
                Vertex v2Vertex = this.vertices.get(v2);
                Vertex v3Vertex = this.vertices.get(v3);

                Vector3f edge1 = new Vector3f(v1Vertex.position()).sub(v2Vertex.position());
                Vector3f edge2 = new Vector3f(v3Vertex.position()).sub(v2Vertex.position());

                Vector3f normal = edge1.cross(edge2);

                v1Vertex.normal().add(normal);
                v2Vertex.normal().add(normal);
                v3Vertex.normal().add(normal);

                // 2nd Triangle

                this.indices.add(v3);
                this.indices.add(v4);
                this.indices.add(v2);

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
            Vertex v = this.vertices.get(i);
            v.normal().normalize();

            this.normalDebugLinesVertices.add(new Vertex(new Vector3f(v.position()), new Vector3f(0.5f)));
            this.normalDebugLinesVertices.add(new Vertex(new Vector3f(v.position()).add(v.normal()), new Vector3f(0.5f)));

        }

    }

    private float noise(float x, float y) {
        float height = 0.0f;
        float frequency = this.terrainState.frequency;
        float amplitude = this.terrainState.amplitude;

        for (int i = 0; i < this.terrainState.octaves; i++) {
            height += amplitude * SimplexNoise.noise(x * frequency, y * frequency) + amplitude / 2;
            frequency *= this.terrainState.frequencyMultiplier;
            amplitude *= this.terrainState.amplitudeMultiplier;
        }


        return height;
    }

}
