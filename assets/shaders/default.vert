#version 430 core


void main() {
    vec3 vertices[3] = vec3[](
        vec3( 0.5,  0.5, 0.0), // Red
        vec3( 0.5, -0.5, 0.0), // Green
        vec3(-0.5, -0.5, 0.0)  // Blue
    );

    gl_Position = vec4(vertices[gl_VertexID], 1.0);
}