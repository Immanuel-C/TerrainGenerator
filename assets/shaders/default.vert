#version 430 core


uniform mat4 proj, view, model;

void main() {
    vec3 vertices[3] = vec3[](
        vec3( 0.5,  0.5, 0.0),
        vec3( 0.5, -0.5, 0.0),
        vec3(-0.5, -0.5, 0.0)
    );

    gl_Position = proj * view * model * vec4(vertices[gl_VertexID], 1.0);
}