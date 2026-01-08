package terrain_generator.utils;

import org.jetbrains.annotations.NotNull;
import terrain_generator.renderer.ShaderInfo;
import terrain_generator.renderer.ShaderProgram;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class ResourceManager {
    private Thread resourceThread;
    private BlockingQueue<Runnable> tasks;
    private ConcurrentHashMap<String, Resource> resources;
    private AtomicBoolean running;

    public ResourceManager() {
        this.resourceThread = new Thread(this::run, "Resource Manager Thread");
        this.tasks = new LinkedBlockingQueue<>();
        this.resources = new ConcurrentHashMap<>();
        this.running = new AtomicBoolean(true);

        this.resourceThread.start();
    }

    public void run() {
        Runnable task;
        while (this.running.get()) {
            try {
                // If there are no tasks this will block
                task = this.tasks.take();
                task.run();
            } catch (InterruptedException e) {
                // Reset interrupt.
                this.resourceThread.interrupt();
            }
        }

        // Finish any unfinished tasks before exiting.
        while ((task = this.tasks.poll()) != null) {
            task.run();
        }
    }

    public void stopRunning() {
        this.running.set(false);
        // Interrupt the thread since BlockingQueue::take blocks the thread must be unblocked through an interrupt.
        this.resourceThread.interrupt();

        try {
            this.resourceThread.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        for (Resource resource: this.resources.values()) {
            resource.destroy();
        }
    }


    public void loadShaderProgram(String name, ShaderInfo[] shaderInfos) {
        this.tasks.add(() -> {
            ShaderProgram shaderProgram = new ShaderProgram(shaderInfos);
            this.resources.put(name, shaderProgram);
        });
    }

    public Resource getResource(String name) {
        return this.resources.get(name);
    }

}
