package terrain_generator;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        try {
            // Sacramento State University coming in clutch https://athena.ecs.csus.edu/~gordonvs/RenderDocJava.html
            System.loadLibrary("renderdoc");
        } catch (UnsatisfiedLinkError e) {
            System.out.println("Renderdoc shared lib cannot be found in system path. Debugging using it is disabled.");
        }

        try {
            TerrainGenerator app = new TerrainGenerator();
        } catch (InterruptedException e) {
            Scanner sc = new Scanner(System.in);
            sc.nextLine();
        }
    }
}