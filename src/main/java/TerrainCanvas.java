import org.lwjgl.opengl.GL;
import static org.lwjgl.opengl.GL43.*;
import org.lwjgl.opengl.awt.AWTGLCanvas;
import org.lwjgl.opengl.awt.GLData;

import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class TerrainCanvas extends AWTGLCanvas implements ComponentListener {
    private AtomicBoolean running;
    private DeltaTime deltaTime;

    BlockingQueue<CanvasThreadMessage> messageQueue;

    public TerrainCanvas(GLData data) {
        super(data);

        this.addComponentListener(this);

        this.running = new AtomicBoolean(true);
        this.deltaTime = new DeltaTime();
        this.messageQueue = new LinkedBlockingQueue<>();
    }

    @Override
    public void initGL() {
        GL.createCapabilities();
    }

    @Override
    public void paintGL() {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        glClearColor(0.5f, 0.6f, 0.7f, 1.0f);
    }

    public void run() {
        while (this.isRunning()) {
            this.deltaTime.start();

            this.render();
            swapBuffers();

            CanvasThreadMessage message;

            while ((message = messageQueue.poll()) != null) {
                switch (message) {
                    case RESIZED:
                        glViewport(0, 0, getWidth(), getHeight());
                        System.out.println("Canvas Size: " + getWidth() + "x" + getHeight());
                        break;
                    default:
                        break;
                }
            }



            double fps = 1.0 / deltaTime.get();
            //System.out.println(fps + " Thread: " + Thread.currentThread().getName());

            this.deltaTime.end();
        }
    }

    public boolean isRunning() {
        return this.running.get();
    }

    public void stopRunning() {
        this.running.set(false);
        GL.setCapabilities(null);
        super.disposeCanvas();
    }

    @Override
    public void componentResized(ComponentEvent componentEvent) {
        messageQueue.add(CanvasThreadMessage.RESIZED);
    }

    @Override
    public void componentMoved(ComponentEvent componentEvent) {

    }

    @Override
    public void componentShown(ComponentEvent componentEvent) {

    }

    @Override
    public void componentHidden(ComponentEvent componentEvent) {

    }
}
