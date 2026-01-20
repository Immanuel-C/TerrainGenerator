package terrain_generator.utils;

// Sealed interfaces are used to mimic Rust style enums.
// These enums can be used as a regular enum or can be used alongside
// some data.
public sealed interface ResourceType {
    // The shader record takes in an enum of type ResourceType.Shader.Type.
    record Shader(Type type) implements ResourceType {
        public enum Type {
            Vertex,
            Fragment,
            Compute,
        }
    }

    // All other records are just used as regular enums for now.
    record ShaderProgram() implements ResourceType {}
    record Texture() implements ResourceType {}
    record Unknown() implements ResourceType {}
}
