package terrain_generator.utils;

import terrain_generator.renderer.ShaderInfo;
import terrain_generator.renderer.ShaderProgram;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class AsyncResourceManager {
    private Thread watcherThread;
    private WatchService watcher;
    private Set<Path> pathsToWatch;

    private ConcurrentHashMap<String, Resource> resources;
    // The set is the resources that depend on the key.
    private ConcurrentHashMap<Resource, Set<Resource>> dependentResources;
    private ConcurrentHashMap<Resource, Set<Resource>> resourceDependencies;

    private AtomicBoolean running;
    private ExecutorService ioExecutor;
    private Executor glExecutor;

    public AsyncResourceManager(Executor glExecutor) {
        this.glExecutor = glExecutor;
        this.ioExecutor = Executors.newCachedThreadPool();
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
                Path path = (Path)key.watchable();
                key.pollEvents()
                        .stream()
                        .filter(e -> e.context() != null) // If the context is null than we cannot do anything with it.
                        .map(e -> path.resolve((Path) e.context())) // Convert this stream into a Path stream by getting the absolute path.
                        .filter(this.pathsToWatch::contains) // If we are not watching this file then filter it out.
                        .map(p -> {
                            Resource resourceDependency = this.resources.get(p.toString());

                            // This should never happen if it does the program should crash and I should investigate.
                            if (resourceDependency == null)
                                throw new RuntimeException(p + " is not inside the resources HashMap.");

                            return resourceDependency;
                        }) // Get the resource
                        .filter(this.dependentResources::containsKey) // Check if any resources are dependent on this resource.
                        .forEach(resourceDependency -> { // For each resource dependency and its dependant resources.
                            // Ideally we should never check if something is a child of resource but im running out of time.
                            if (resourceDependency instanceof ShaderInfo shaderInfo) {
                                CompletableFuture
                                    .runAsync(() -> {
                                        // Load the shader file.
                                        this.loadShaderFile(shaderInfo);
                                    }, this.ioExecutor)
                                    .thenRunAsync(() -> {
                                        this.dependentResources
                                            .get(resourceDependency) // Get all the resources that are dependent on this resource
                                            .forEach((dependentResource) -> {
                                                // Then get all the dependencies of that resource.
                                                Set<Resource> dependencies = this.resourceDependencies.get(dependentResource);
                                                // Recreate the resource. We don't have to remove the dependency we changed since we modify the reference.
                                                // Not re-initialize it with a constructor. This is the same reason why I recreate instead of
                                                // calling the constructor. If the client of the resource manager takes a reference of a resource
                                                // it should always point towards the most recent version of that resource.
                                                dependentResource.recreate(dependencies);
                                            });
                                    }, this.glExecutor);
                            }
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
        this.ioExecutor.shutdown();
    }

    private CompletableFuture<ShaderInfo> loadShaderFile(ShaderInfo shaderInfo) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Path absolutePath = Path.of(shaderInfo.getPath()).toAbsolutePath();
                String source = Files.readString(absolutePath);
                Path parent = absolutePath.getParent();

                if (parent == null)
                    throw new IOException(absolutePath + " does not have a parent directory cannot watch this file for hot-reloading.");
                if (!Files.isDirectory(parent))
                    throw new IOException(absolutePath + " does not have a parent directory cannot watch this file for hot-reloading.");


                parent = parent.toAbsolutePath();

                // All the paths that the manager deals with are absolute. While the shader info may have a relative path.
                this.pathsToWatch.add(absolutePath);

                this.resources.putIfAbsent(absolutePath.toString(), shaderInfo);


                if (!this.pathsToWatch.contains(parent)) {
                    System.out.println("Watching parent: " + parent);

                    parent.register(
                            this.watcher,
                            StandardWatchEventKinds.ENTRY_MODIFY,
                            StandardWatchEventKinds.ENTRY_CREATE,
                            StandardWatchEventKinds.ENTRY_DELETE
                    );

                    // Add the parent dir to the set so we dont register it multiple times with the watcher.
                    this.pathsToWatch.add(parent);
                }

                shaderInfo.setSource(source);

                return shaderInfo;
            } catch (IOException e) {
                throw new CompletionException(e);
            }
        });
    }

    // shaderInfos should not be modified after calling this method doing so could cause
    // undefined behavior.
    public void loadShaderProgram(String name, Collection<ShaderInfo> shaderInfos) {
        CompletableFuture
                .supplyAsync(() -> { // Load on dedicated Resource IO threads
                    for (ShaderInfo shaderInfo: shaderInfos) {
                        try {
                            /*
                             .get waits for the IO executor to finish executing the future.
                             loadShaderFile may also be called from the watcher thread so it must also run
                             asynchronously from when it was called.
                            */
                            loadShaderFile(shaderInfo).get();
                        } catch (InterruptedException | ExecutionException e) {
                            throw new CompletionException(e);
                        }
                    }

                    return shaderInfos;
                }, this.ioExecutor)
                // Then create the actual OpenGL object inside the Render thread and when the OpenGL context is current.
                // As OpenGL does not guarantee thread safety so all OpenGL functions must be called on the thread in which
                // the context is current on. Then add it to the resources hash map. It makes the API more simple at the cost of
                // flexibility and performance.
                .thenAcceptAsync(shaderInfosWithSource -> {
                    ShaderProgram shaderProgram = new ShaderProgram(shaderInfosWithSource);
                    this.resources.put(name, shaderProgram);


                    // Add to dependent Resource to see which resources dependents are and add to resource dependencies to
                    // see which resources are the dependency of the shader program.
                    for (ShaderInfo info: shaderInfosWithSource) {
                        this.dependentResources.compute(info, (shaderInfo, dependentResources) -> {
                            if (dependentResources == null)
                                dependentResources = ConcurrentHashMap.newKeySet();

                            dependentResources.add(shaderProgram);
                            // Return the modified dependent set since we modify the reference to the dependent set.
                            // This doesn't really matter but the BiFunction requires a return value.
                            return dependentResources;
                        });

                        this.resourceDependencies.compute(shaderProgram, (program, resourceDependencies) -> {
                            if (resourceDependencies == null)
                                resourceDependencies = ConcurrentHashMap.newKeySet();

                            resourceDependencies.add(info);

                            return resourceDependencies;
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

    public static String getResourcePath(String name) {
        String path = AsyncResourceManager.class
                .getClassLoader()
                .getResource(name)
                .getPath();


        if (path == null)
            throw new RuntimeException("Resource folder cannot be located.");

        return path;
    }

}