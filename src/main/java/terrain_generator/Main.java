package terrain_generator;

import terrain_generator.swing.TerrainGenerator;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        // Sacramento State University coming in clutch https://athena.ecs.csus.edu/~gordonvs/RenderDocJava.html.
        // Renderdoc is an application used to debug Graphics API's like OpenGL.
        // Renderdoc has trouble seeing this as an OpenGL application since it's embedded inside
        // Swing. But if the library loaded at runtime Renderdoc can be forced to see out application as a graphical one.
        // This only works on Windows since WGL is not strict unlike GLX which will not tolerate late injections by
        // Renderdoc. On linux the library must be preloaded before it starts.
        if (System.getProperty("os.name").toLowerCase().startsWith("win")) {
            try {
                System.loadLibrary("renderdoc");
            } catch (UnsatisfiedLinkError e) {
                System.err.println("Renderdoc shared lib cannot be found in system path. Debugging using it is disabled.");
            }
        }

        try {
            new TerrainGenerator();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}