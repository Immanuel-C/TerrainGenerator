import org.lwjgl.opengl.GL;
import static org.lwjgl.opengl.GL43.*;
import static org.lwjgl.system.MemoryUtil.memByteBuffer;

import org.lwjgl.opengl.GLDebugMessageCallbackI;
import org.lwjgl.opengl.awt.AWTGLCanvas;
import org.lwjgl.opengl.awt.GLData;

import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class TerrainCanvas extends AWTGLCanvas implements ComponentListener, GLDebugMessageCallbackI {
    private AtomicBoolean running;
    private DeltaTime deltaTime;
    private final Input input;
    private Renderer renderer;

    private final AtomicReference<Double> fps;

    BlockingQueue<ThreadMessage> messageQueue;

    public TerrainCanvas(GLData data, Input input) {
        super(data);

        this.addComponentListener(this);
        this.addKeyListener(input);
        this.addMouseListener(input);
        this.addMouseMotionListener(input);
        this.addMouseWheelListener(input);

        this.running = new AtomicBoolean(true);
        this.deltaTime = new DeltaTime();
        this.messageQueue = new LinkedBlockingQueue<>();
        this.input = input;
        this.fps = new AtomicReference<>(0.0);

    }

    @Override
    public void initGL() {
        GL.createCapabilities();
        this.renderer = new Renderer();

        glDebugMessageCallback(this, 0);
    }

    @Override
    public void paintGL() {
        this.renderer.render();

        ThreadMessage message;

        while ((message = messageQueue.poll()) != null) {
            switch (message) {
                case RESIZED -> {
                    glViewport(0, 0, getWidth(), getHeight());
                    System.out.println("Canvas Size: " + getWidth() + "x" + getHeight());
                }
                default -> {}
            }
        }


        swapBuffers();
    }

    public void run() {
        while (this.isRunning()) {
            this.deltaTime.start();

            try {
                this.render();
            } catch (NullPointerException e) {
                this.renderer.destroy();
                break;
            }

            this.fps.set(1.0 / deltaTime.get());

            this.deltaTime.end();
        }

    }

    public double getFps() {
        return this.fps.get();
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
        messageQueue.add(ThreadMessage.RESIZED);
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

    @Override
    public void invoke(int source, int type, int id, int severity, int length, long messagePointer, long userParam) {
        String msg = memByteBuffer(messagePointer, length).toString();

        System.out.println("glDebugMessage: " + msg);
    }
}
