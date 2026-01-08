package terrain_generator.swing;



import org.lwjgl.opengl.*;

import org.lwjgl.opengl.awt.AWTGLCanvas;
import org.lwjgl.opengl.awt.GLData;
import terrain_generator.*;
import terrain_generator.renderer.Renderer;
import terrain_generator.utils.DeltaTime;

import java.awt.*;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.lwjgl.system.MemoryUtil.memByteBuffer;

public class TerrainCanvas extends AWTGLCanvas implements ComponentListener {
    private AtomicBoolean running;
    private DeltaTime deltaTime;
    private final Input input;
    private Renderer renderer;
    private Thread renderThread;
    private TerrainState terrainState;
    private RenderSettings renderSettings;

    private final AtomicReference<Double> fps;

    ConcurrentLinkedQueue<TerrainCanvasMessage> messageQueue;

    public TerrainCanvas(GLData data, Input input, TerrainState terrainState, RenderSettings renderSettings) {
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



        this.terrainState = terrainState;
        this.renderSettings = renderSettings;

        this.renderThread = new Thread(this::run, "Terrain Canvas Render Thread");
        this.renderThread.start();
    }

    @Override
    public void initGL() {
        GL.createCapabilities();
        this.renderer = new Renderer(this.terrainState, renderSettings, (float) Math.toRadians(90.0), this.getWidth(), this.getHeight());
    }

    @Override
    public void paintGL() {
        this.renderer.render();

        TerrainCanvasMessage message;
        while ((message = messageQueue.poll()) != null) {
            switch (message) {
                case Resized -> this.renderer.resizeViewport(0, 0, this.getWidth(), this.getHeight());
                default -> {}
            }
        }

        swapBuffers();
    }

    public void run() {
        while (this.isRunning()) {
            // OpenGL needs the component to be visible and ready to use before it can start rendering.
            // The layer between OpenGL and Swing locks the drawing surface so Swing cannot touch it but
            // this operation fails if the canvas is not valid since the drawing surface hasn't been created yet.
            if (!this.isDisplayable()) {
                // Tell the OS that it can leave this thread safely and do other tasks
                // since we cannot do anything on this thread.
                Thread.yield();
                continue;
            }

            this.deltaTime.start();

            try {
                this.render();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            this.deltaTime.end();
            this.fps.set(1.0 / deltaTime.get());
        }

        try {
            this.executeInContext(() -> {
                this.renderer.destroy();
                return null;
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
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

        // Wait on the main thread until the render thread is fully finished.
        // Destroying the canvas while the render thread is running can cause the renderer to not
        // be destroyed before the program exits.
        try {
            this.renderThread.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public void disposeCanvas() {}

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


    @Override
    public boolean isDisplayable() {
        // Blocking on the EDT is considered bad practice but Swing does not provide a
        // listener to check if the component is valid.
        synchronized (this) {
            return super.isDisplayable();
        }
    }
}