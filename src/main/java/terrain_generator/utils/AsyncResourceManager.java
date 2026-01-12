package terrain_generator.utils;

import terrain_generator.renderer.ShaderInfo;
import terrain_generator.renderer.ShaderProgram;

import java.io.IOException;
import java.nio.file.*;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

public class AsyncResourceManager {
    private Thread watcherThread;
    private WatchService watcher;

    private ConcurrentHashMap<String, Resource> resources;
    private ConcurrentHashMap<Resource, Set<Resource>> dependentResources;

    private AtomicBoolean running;
    private ExecutorService futureExecutor;
    private Executor glExecutor;

    public AsyncResourceManager(String resourceDirectory, Executor glExecutor) {
        this.glExecutor = glExecutor;
        this.futureExecutor = Executors.newCachedThreadPool();
        this.watcherThread = new Thread(this::watchFiles, "Resource Manager File Watcher Thread");
        this.resources = new ConcurrentHashMap<>();
        this.dependentResources = new ConcurrentHashMap<>();
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
        try (Stream<Path> pathStream = Files.walk(resourceDirectoryPath).filter(Files::isDirectory)) {
            for (Path path: (Iterable<Path>) pathStream::iterator) {
                path.register(
                        this.watcher,
                        StandardWatchEventKinds.ENTRY_MODIFY,
                        StandardWatchEventKinds.ENTRY_CREATE,
                        StandardWatchEventKinds.ENTRY_DELETE
                );
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

//        this.watcherThread.start();
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

    // This should be called before the glExecutor is destroyed
    public void stopRunning() {
        this.running.set(false);
        // Interrupt the thread since BlockingQueue::take blocks the thread must be unblocked through an interrupt.
//        this.watcherThread.interrupt();
//
//        try {
//            this.watcherThread.join();
//            this.watcher.close();
//        } catch (InterruptedException | IOException e) {
//            throw new RuntimeException(e);
//        }

        glExecutor.execute(() -> {
            for (Resource resource: this.resources.values()) {
                resource.destroy();
            }
        });
        this.futureExecutor.shutdown();
    }



    public void loadShaderProgram(String name, ShaderInfo[] shaderInfos) {
        CompletableFuture
                .supplyAsync(() -> { // Load on dedicated Resource IO threads
                    String[] sources = new String[shaderInfos.length];
                    for (int i = 0; i < shaderInfos.length; i++) {
                        try {
                            sources[i] = Files.readString(Path.of(shaderInfos[i].getPath()));
                        } catch (IOException e) {
                            throw new CompletionException(e);
                        }
                    }


                    return new Pair<>(shaderInfos, sources);
                }, this.futureExecutor)
                // Then create the actual OpenGL object inside the Render thread and when the OpenGL context is current.
                // Then add it to the resources hash map.
                .thenAcceptAsync(infoSourcePair -> {
                    ShaderProgram shaderProgram = new ShaderProgram(infoSourcePair.first(), infoSourcePair.second());
                    this.resources.put(name, shaderProgram);

                    for (ShaderInfo info: infoSourcePair.first()) {
                        if (this.dependentResources.containsKey(info)) {
                            this.dependentResources.get(info).add(shaderProgram);
                        } else {
                            this.dependentResources.put(info, new HashSet<>(List.of(shaderProgram)));
                        }
                    }
                }, this.glExecutor)
                .exceptionally(e -> { e.printStackTrace(); return null; });

    }

    // If a resource does not exist or is not ready then .get will return null.
    // It is wrapped in an Optional as we want to force the user of the method to
    // check if the return value is invalid.
    public Optional<Resource> getResource(String name) {
        return Optional.ofNullable(this.resources.get(name));
    }

    public boolean isResourceAvailable(String name) {
        return this.resources.containsKey(name);
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