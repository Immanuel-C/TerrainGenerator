#version 430 core

layout(location = 0)
out vec4 fragColour;

layout(location = 0)
in vec3 inNormal;

layout(location = 1)
in vec3 inFragPos;

uniform float ambientStrength;
uniform vec3 lightPos, lightColour;

vec4 calculateFragDepth(float far, float near) {
    float depth = gl_FragCoord.z;

    // Convert to NDC [-1.0, 1,0] from [0.0, 1.0]
    float ndcDepth = depth * 2.0 - 1.0;
    // far - near: calculates the length of the view frustum on the z axis
    ndcDepth = (2.0 * near * far) / (far + near - ndcDepth * (far - near));
    depth = ndcDepth / far; // Convert back to [0.0, 1.0].

    return vec4(vec3(depth), 1.0);
}

vec4 calculateFragColourWithLight() {
    vec3 normal = normalize(inNormal);
    vec3 lightDirection = normalize(lightPos - inFragPos);

    // If the light is behind the fragment (the angle between the light and the normal > pi/2) than the value will be negative.
    // So the we must choose the max of either the dot product or 0.
    vec3 diff = max(dot(normal, lightDirection), 0.0) * lightColour;

    return vec4(vec3(0.5) * (diff + ambientStrength), 1.0);
}

void main() {
    float far = 100.0, near = 0.1;
    fragColour = calculateFragColourWithLight();
    //fragColour = calculateFragDepth(far, near);
}