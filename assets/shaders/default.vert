#version 430 core

uniform mat4 proj, view, model;

layout(location = 0)
in vec3 aPos;

layout(location = 1)
in vec3 aNormal;

layout(location = 0)
out vec3 outNormal;

layout(location = 1)
out vec3 outFragPos;

void main() {
    outNormal = mat3(transpose(inverse(model))) * aNormal;
    // Lighting calculations are done in world space.
    outFragPos = vec3(model * vec4(aPos, 1.0));
    gl_Position = proj * view * model * vec4(aPos, 1.0);
}