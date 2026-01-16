package terrain_generator.utils;

import terrain_generator.renderer.ShaderInfo;
import terrain_generator.renderer.ShaderProgram;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

public class AsyncResourceManager {
    private Thread watcherThread;
    private WatchService watcher;
    private Set<Path> pathsToWatch;

    private ConcurrentHashMap<String, Resource> resources;
    // The set is the resources that depend on the key.
    private ConcurrentHashMap<Resource, Set<Resource>> dependentResources;
    // The
    private ConcurrentHashMap<Resource, Set<Resource>> resourceDependencies;


    private AtomicBoolean running;
    private ExecutorService futureExecutor;
    private Executor glExecutor;

    public AsyncResourceManager(Executor glExecutor) {
        this.glExecutor = glExecutor;
        this.futureExecutor = Executors.newCachedThreadPool();
        this.watcherThread = new Thread(this::watchFiles, "Resource Manager File Watcher Thread");
        this.resources = new ConcurrentHashMap<>();
        this.dependentResources = new ConcurrentHashMap<>();
        this.resourceDependencies = new ConcurrentHashMap<>();
        this.running = new AtomicBoolean(true);

        this.pathsToWatch = ConcurrentHashMap.newKeySet();

        try {
            this.watcher = FileSystems.getDefault().newWatchService();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


        this.watcherThread.start();
    }

    public void watchFiles() {
        WatchKey key;

        while (this.running.get()) {
            try {
                key = this.watcher.take();
                Path path = ((Path)key.watchable());
                key.pollEvents()
                        .stream()
                        .filter(e -> e.context() != null) // If the context is null than we cannot do anything with it.
                        .map(e -> path.resolve((Path)e.context())) // Convert this stream into a Path stream by getting the absolute path.
                        .filter(this.pathsToWatch::contains) // If we are not watching this file then filter it out.
                        .map(p -> {
                            Resource dependentResource = this.resources.get(p.toString());

                            // This should never happen if it does the program should crash and I should investigate.
                            if (dependentResource == null)
                                throw new RuntimeException(p + " is not inside the resources HashMap.");

                            return dependentResource;
                        }) // Get the resource
                        .filter(this.dependentResources::containsKey) // Check if any resources are dependent on this resource. If there is none then there are resources that depend on this resource.
                        .forEach(dependentResource -> {
                            dependentResources.get(dependentResource).forEach(dependentsDependency -> {
                                dependentsDependency.dependencies.
                            });
                        });
                // Add the key back onto the queue which allows the service to modify it
                // and us to take it again.
                key.reset();
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
        this.watcherThread.interrupt();

        try {
            this.watcherThread.join();
            this.watcher.close();
        } catch (InterruptedException | IOException e) {
            throw new RuntimeException(e);
        }

        glExecutor.execute(() -> {
            for (Resource resource: this.resources.values()) {
                resource.destroy();
            }
        });
        this.futureExecutor.shutdown();
    }

    private CompletableFuture<Pair<ShaderInfo, String>> loadShaderFile(ShaderInfo shaderInfo) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Path shaderPath = Path.of(shaderInfo.getPath());
                String source = Files.readString(shaderPath);
                Path parent = shaderPath.getParent();

                if (parent == null)
                    throw new IOException(shaderPath + " does not have a parent directory cannot watch this file for hot-reloading.");
                if (!Files.isDirectory(parent))
                    throw new IOException(shaderPath + " does not have a parent directory cannot watch this file for hot-reloading.");

                // TODO: Modify maps


                parent = parent.toAbsolutePath();

                System.out.println("Watching parent: " + parent);

                parent.register(
                        this.watcher,
                        StandardWatchEventKinds.ENTRY_MODIFY,
                        StandardWatchEventKinds.ENTRY_CREATE,
                        StandardWatchEventKinds.ENTRY_DELETE
                );

                this.pathsToWatch.add(shaderPath.toAbsolutePath());
                System.out.println("Added shader resource: " + shaderPath.toAbsolutePath());

                return new Pair<>(shaderInfo, source);
            } catch (IOException e) {
                throw new CompletionException(e);
            }
        });
    }

    public void loadShaderProgram(String name, ShaderInfo[] shaderInfos) {

        CompletableFuture
                .supplyAsync(() -> { // Load on dedicated Resource IO threads
                    Pair<ShaderInfo, String>[] infoSourcePairs = new Pair[shaderInfos.length];

                    for (int i = 0; i < shaderInfos.length; i++) {
                        try {
                            infoSourcePairs[i] = loadShaderFile(shaderInfos[i]).get();
                        } catch (InterruptedException | ExecutionException e) {
                            throw new CompletionException(e);
                        }
                    }

                    return infoSourcePairs;
                }, this.futureExecutor)
                // Then create the actual OpenGL object inside the Render thread and when the OpenGL context is current.
                // Then add it to the resources hash map.
                .thenAcceptAsync(infoSourcePair -> {
                    ShaderProgram shaderProgram = new ShaderProgram(infoSourcePair.first(), infoSourcePair.second());
                    this.resources.put(name, shaderProgram);


                    for (ShaderInfo info: infoSourcePair.first()) {
                        String absolutePath = Path.of(info.getPath()).toAbsolutePath().toString();

                        this.resources.put(absolutePath, info);
                        this.dependentResources.computeIfAbsent(info, (shaderInfo) -> {
                            Set<Resource> dependencies = ConcurrentHashMap.newKeySet();
                            dependencies.add(shaderProgram);
                            return dependencies;
                        });

                        this.dependentResources.computeIfPresent(info, (shaderInfo, dependentResources) -> {
                            dependentResources.add(shaderProgram);
                            // Return the modified dependent set since we modify the reference to the dependent set.
                            // This doesn't really matter but the BiFunction requires a return value.
                            return dependentResources;
                        });
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