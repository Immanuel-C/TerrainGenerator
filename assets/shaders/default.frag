#version 430 core

layout(location = 0)
out vec4 fragColour;

layout(location = 0)
in vec3 inPos;

void main() {
    fragColour = vec4(0.5 * inPos + 0.5, 1.0);
}