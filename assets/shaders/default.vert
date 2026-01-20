#version 430 core

uniform mat4 projMat, viewMat, modelMat;
uniform mat3 normalMat;

layout(location = 0)
in vec3 aPos;

layout(location = 1)
in vec3 aNormal;

layout(location = 0)
out vec3 outNormal;

layout(location = 1)
out vec3 outFragPos;

layout(location = 2)
out vec4 newPos;

void main() {
    // Since the 4 column of the model matrix has the transformations
    // this must be removed since normals meause the direction of the plane.
    // The plane must also be perpendicular
    outNormal = normalMat * aNormal;
    // Lighting calculations are done in world space.
    outFragPos = vec3(modelMat * vec4(aPos, 1.0));
    newPos = viewMat * modelMat * vec4(aPos, 1.0);
    gl_Position = projMat * viewMat * modelMat * vec4(aPos, 1.0);
}
