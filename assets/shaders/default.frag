#version 430 core

layout(location = 0)
out vec4 fragColour;

layout(location = 0)
in vec3 inNormal;

layout(location = 1)
in vec3 inFragPos;

uniform float ambientStrength;
uniform vec3 lightPos, lightColour;

void main() {
    vec3 normal = normalize(inNormal);
    vec3 lightDirection = normalize(lightPos - inFragPos);

    // If the light is behind the fragment (the angle between the light and the normal > pi/2) than the value will be negative.
    // So the we must choose the max of either the dot product or 0.
    vec3 diff = max(dot(normal, lightDirection), 0.0) * lightColour;

    fragColour = vec4(vec3(0.5) * (diff + ambientStrength), 1.0);
}