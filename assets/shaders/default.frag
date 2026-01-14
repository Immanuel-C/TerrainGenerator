#version 430 core

layout(location = 0)
out vec4 fragColour;

layout(location = 0)
in vec3 inNormal;

layout(location = 1)
in vec3 inFragPos;

layout(location = 2)
in vec4 newPos;



uniform float ambientStrength;
uniform vec3 lightPos, lightColour;

void main() {
    vec3 normal = normalize(inNormal);
    vec3 lightDirection = normalize(lightPos - inFragPos);

    // If the light is behind the fragment (the angle between the light and the normal > pi/2) than the value will be negative.
    // So the we must choose the max of either the dot product or 0.
    vec3 diff = max(dot(normal, lightDirection), 0.0) * lightColour;

    float far = 100.0;
    float near = 0.1;

    // depth = z - near / far - near
    // far - near calculates the length of the view frustum on the z axis
    // near = 0.1f, far = 100.0f
    float depth = gl_FragCoord.z;
    // depth is now in clip space

    // Convert to NDC [-1.0, 1,0] from [0.0, 1.0]
    float ndcDepth = depth * 2.0 - 1.0;
    ndcDepth = (2.0 * near * far) / (far + near - ndcDepth * (far - near));
    depth = ndcDepth / far; // Convert back to [0.0, 1.0].

    vec3 objColour = vec3(0.5) * (diff + ambientStrength);
    fragColour = vec4(objColour, 1.0);
    //fragColour = vec4(vec3(depth), 1.0);
}