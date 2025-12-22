#version 430 core

uniform mat4 proj, view, model;

layout(location = 0)
in vec4 aPos;

layout(location = 0)
out vec3 outPos;

void main() {
    outPos = aPos.xyz;
    gl_Position = proj * view * model * aPos;
}