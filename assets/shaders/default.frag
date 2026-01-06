#version 430 core

layout(location = 0)
out vec4 fragColour;

layout(location = 0)
in vec3 inNormal;

layout(location = 1)
in float inAmbientStrength;

void main() {
    //fragColour = vec4(inPos.y / 20.0 * 0.5 + 0.5);
    fragColour = vec4(inNormal * 0.5 + 0.5, 1.0) * inAmbientStrength;
}