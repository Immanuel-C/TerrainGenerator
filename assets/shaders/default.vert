#version 430 core

uniform mat4 proj, view, model;

layout(location = 0)
in vec3 aPos;

layout(location = 1)
in vec3 aNormal;

layout(location = 0)
out vec3 outNormal;

void main() {
    outNormal = aNormal;
    gl_Position = proj * view * model * vec4(aPos, 1.0);
}