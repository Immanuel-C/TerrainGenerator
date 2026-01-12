package terrain_generator.renderer;

import static org.lwjgl.opengl.GL45.*;

public class Framebuffer {
    int fbo;

    public Framebuffer(int width, int height, boolean depthStencilSampling) {
        this.fbo = glGenFramebuffers();
        this.bind();
    }

    public void resizeAttachments(int width, int height) {


    }


    public void bind() {
        glBindFramebuffer(GL_FRAMEBUFFER, this.fbo);
    }

    public static void unBind() {
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
    }

    public void destroy() {
        glDeleteFramebuffers(this.fbo);
    }

}
