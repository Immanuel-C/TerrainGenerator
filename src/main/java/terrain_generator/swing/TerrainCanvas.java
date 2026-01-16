package terrain_generator.swing;



import org.lwjgl.opengl.*;

import org.lwjgl.opengl.awt.AWTGLCanvas;
import org.lwjgl.opengl.awt.GLData;
import terrain_generator.*;
import terrain_generator.renderer.Renderer;
import terrain_generator.utils.AsyncResourceManager;
import terrain_generator.utils.DeltaTime;

import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class TerrainCanvas extends AWTGLCanvas implements ComponentListener, Executor {
    private AtomicBoolean running;
    private DeltaTime deltaTime;
    private final Input input;
    private Renderer renderer;
    private Thread renderThread;
    private TerrainState terrainState;
    private RenderSettings renderSettings;

    private final AtomicReference<Double> fps;

    // These are tasks that must be run inside the OpenGL context.
    ConcurrentLinkedQueue<Runnable> glTasks;
    AsyncResourceManager resourceManager;


    public TerrainCanvas(GLData data, Input input, TerrainState terrainState, RenderSettings renderSettings) {
        super(data);

        this.addComponentListener(this);
        this.addKeyListener(input);
        this.addMouseListener(input);
        this.addMouseMotionListener(input);
        this.addMouseWheelListener(input);

        this.running = new AtomicBoolean(true);
        this.deltaTime = new DeltaTime();
        this.glTasks = new ConcurrentLinkedQueue<>();
        this.resourceManager = new AsyncResourceManager(this);
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
        this.renderer = new Renderer(this.resourceManager, this.terrainState, renderSettings, (float) Math.toRadians(90.0), this.getWidth(), this.getHeight());
    }

    @Override
    public void paintGL() {
        this.renderer.render();
        runGLTasks();
        swapBuffers();
    }

    private void runGLTasks() {
        Runnable glTask;
        while ((glTask = glTasks.poll()) != null) {
            glTask.run();
        }
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

        this.resourceManager.stopRunning();

        try {
            this.executeInContext(() -> {
                // Finish glTasks
                runGLTasks();

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
    public boolean isDisplayable() {
        // Blocking on the EDT is considered bad practice but Swing does not provide a
        // listener to check if the component is displayable.
        synchronized (this) {
            return super.isDisplayable();
        }
    }


    @Override
    // Suppresses IntelliJ's warning of not using the @NotNull annotation. This annotation is JetBrains specific making it non-portable.
    public void execute(@SuppressWarnings("all") Runnable runnable) {
        this.glTasks.add(runnable);
    }


    @Override
    public void componentResized(ComponentEvent componentEvent) {
        this.glTasks.add(() -> this.renderer.resizeViewport(0, 0, this.getWidth(), this.getHeight()));
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
    public void disposeCanvas() {}

}