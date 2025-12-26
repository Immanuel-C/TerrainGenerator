package terrain_generator;



import org.lwjgl.opengl.*;

import static org.lwjgl.opengl.GL43.*;
import org.lwjgl.opengl.awt.AWTGLCanvas;
import org.lwjgl.opengl.awt.GLData;
import org.lwjgl.system.Callback;

import java.awt.*;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.lwjgl.opengl.GL43.glDebugMessageCallback;
import static org.lwjgl.system.MemoryUtil.memByteBuffer;

public class TerrainCanvas extends AWTGLCanvas implements ComponentListener {
    private AtomicBoolean running;
    private DeltaTime deltaTime;
    private final Input input;
    private Renderer renderer;
    private Thread renderThread;

    private final AtomicReference<Double> fps;

    ConcurrentLinkedQueue<TerrainCanvasMessage> messageQueue;

    public TerrainCanvas(GLData data, Input input) {
        super(data);

        this.addComponentListener(this);
        this.addKeyListener(input);
        this.addMouseListener(input);
        this.addMouseMotionListener(input);
        this.addMouseWheelListener(input);

        this.running = new AtomicBoolean(true);
        this.deltaTime = new DeltaTime();
        this.messageQueue = new ConcurrentLinkedQueue<>();
        this.input = input;
        this.fps = new AtomicReference<>(0.0);

        this.renderThread = new Thread(this::run, "Terrain Canvas Render Thread");
        this.renderThread.start();
    }

    @Override
    public void initGL() {
        GL.createCapabilities();
        this.renderer = new Renderer((float) Math.toRadians(90.0), this.getWidth(), this.getHeight());
    }

    @Override
    public void paintGL() {
        this.renderer.render();

        TerrainCanvasMessage message;
        while ((message = messageQueue.poll()) != null) {
            switch (message) {
                case Resized -> glViewport(0, 0, this.getWidth(), this.getHeight());
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
            } catch (Exception e) {}

            this.fps.set(1.0 / deltaTime.get());

            this.deltaTime.end();
        }

        this.disposeCanvas();
    }

    public double getFps() {
        return this.fps.get();
    }

    public boolean isRunning() {
        return this.running.get();
    }

    @Override
    public void disposeCanvas() {
        this.renderer.destroy();
        this.platformCanvas.dispose();
    }

    public void stopRunning() {
        this.running.set(false);

        // Wait on the main thread until the render thread is fully finished.
        try {
            this.renderThread.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void componentResized(ComponentEvent componentEvent) {
        this.messageQueue.add(TerrainCanvasMessage.Resized);
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