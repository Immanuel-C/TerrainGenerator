package terrain_generator.utils;

import org.jetbrains.annotations.Nullable;
import terrain_generator.renderer.ShaderInfo;
import terrain_generator.renderer.ShaderProgram;
import terrain_generator.renderer.ShaderSource;

import java.io.IOException;
import java.nio.file.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

public class AsyncResourceManager {
    private Thread watcherThread;
    private WatchService watcher;

    private ConcurrentLinkedDeque<CompletableFuture<Resource>> resourceFutures;
    private ConcurrentHashMap<String, Resource> resources;

    private AtomicBoolean running;
    private Executor futureExecutor, glExecutor;

    public AsyncResourceManager(String resourceDirectory, Executor glExecutor) {
        this.glExecutor = glExecutor;
        this.futureExecutor = Executors.newSingleThreadExecutor();
        this.watcherThread = new Thread(this::watchFiles, "Resource Manager File Watcher Thread");
        this.resources = new ConcurrentHashMap<>();
        this.resourceFutures = new ConcurrentLinkedDeque<>();
        this.running = new AtomicBoolean(true);
        try {
            this.watcher = FileSystems.getDefault().newWatchService();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Path resourceDirectoryPath = Path.of(resourceDirectory);

        if (!Files.isDirectory(resourceDirectoryPath)) {
            throw new RuntimeException(resourceDirectoryPath + " is not a directory");
        }

        // Recursively go into the resource directory find every folder and add a watcher
        // for each folder.
        try (Stream<Path> pathStream = Files.walk(resourceDirectoryPath)) {
            pathStream.forEach(path -> {
                if (Files.isDirectory(path)) {
                    try {
                        path.register(
                                this.watcher,
                                StandardWatchEventKinds.ENTRY_MODIFY,
                                StandardWatchEventKinds.ENTRY_CREATE,
                                StandardWatchEventKinds.ENTRY_DELETE
                        );
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                } else if (Files.isRegularFile(path)) {
                    switch (AsyncResourceManager.resourceType(path)) {
                        case VertexShader, FragmentShader, ComputeShader -> {
                            this.loadFile(path);
                        }
                        case Texture -> {
                            System.err.println("TODO: Add texture loading support.");
                        }
                        case Unknown -> {
                            System.out.println("Unknown Resource Type: " + path);
                        }
                    }
                }
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


    }

    public void watchFiles() {
        WatchKey key;

        while (this.running.get()) {
            try {
                key = this.watcher.take();
                key.pollEvents().forEach(event -> {
                    if (event.kind().name().equals(StandardWatchEventKinds.ENTRY_MODIFY.name())) {

                    }
                });
            } catch (InterruptedException e) {
                this.watcherThread.interrupt();
                break;
            }
        }
    }

    public void stopRunning() {
        this.running.set(false);
        // Interrupt the thread since BlockingQueue::take blocks the thread must be unblocked through an interrupt.
        this.watcherThread.interrupt();

        try {
            this.watcherThread.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        try {
            this.watcher.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        for (Resource resource: this.resources.values()) {
            resource.destroy();
        }
    }



    public void loadShaderProgram(String name, ShaderInfo[] shaderInfos) {
        CompletableFuture
                .supplyAsync(() -> { // Load on a dedicated Resource IO thread
                    ShaderSource[] sources = new ShaderSource[shaderInfos.length];

                    try {
                        for (int i = 0; i < shaderInfos.length; i++)
                            sources[i] = new ShaderSource(shaderInfos[i].type(), Path.of(shaderInfos[i].path()));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }

                    return sources;
                }, this.futureExecutor)
                // Then create the actual OpenGL object inside the Render thread.
                .thenApplyAsync(ShaderProgram::new, this.glExecutor)
                // Then put the resources into the resources hash map that can then be retrieved by the renderer.
                .thenAcceptAsync(shaderProgram -> this.resources.put(name, shaderProgram), this.futureExecutor)
                // If any stage fails throw a new Runtime Exception.
                .exceptionally(e -> {
                    throw new RuntimeException(e);
                });

    }

    public @Nullable Resource getResource(String name) {
        return this.resources.get(name);
    }


    static private ResourceType resourceType(Path path) {
        String fileName = String.valueOf(path.getFileName());
        int index = fileName.lastIndexOf('.');

        // No file extension
        if (index == -1)
            return ResourceType.Unknown;

        String fileExtension = fileName.substring(index);

        switch (fileExtension) {
            case ".vert" -> {
                return ResourceType.VertexShader;
            }
            case ".frag" -> {
                return ResourceType.FragmentShader;
            }
            case ".comp" -> {
                return ResourceType.ComputeShader;
            }
            default -> {
                return ResourceType.Unknown;
            }
        }
    }

}