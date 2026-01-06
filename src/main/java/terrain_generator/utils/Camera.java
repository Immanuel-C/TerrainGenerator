package terrain_generator.utils;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import terrain_generator.renderer.ShaderProgram;

public class Camera {
    Matrix4f view, projection;

    public Vector3f position;

    float fov, aspect, zNear, zFar;

    public Camera(Vector3f position, float fov, float aspect, float zNear, float zFar) {
        this.position = position;

        this.fov = fov;
        this.aspect = aspect;
        this.zNear = zNear;
        this.zFar = zFar;

        this.update();
    }

    public void update() {
        this.projection = new Matrix4f().setPerspective(this.fov, this.aspect, this.zNear, this.zFar);
        this.view = new Matrix4f().lookAt(this.position, new Vector3f(0, 0, 0), new Vector3f(0, 1, 0));
    }

    public void uploadViewMatrix(ShaderProgram program, String uniformName) {
        program.uploadMatrix4f(this.view, uniformName);
    }

    public void uploadProjectionMatrix(ShaderProgram program, String uniformName) {
        program.uploadMatrix4f(this.projection, uniformName);
    }
}
