package terrain_generator.renderer;

import org.joml.Vector3f;

// Vertices can include more than position data and often includes any data
// related to the position to it like the normal.
public record Vertex(Vector3f position, Vector3f normal) {
    // The number of elements in 1 vertex.
    public static final int STRIDE = 6;
}
