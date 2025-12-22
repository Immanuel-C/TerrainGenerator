package terrain_generator;

public class DeltaTime {
    private double deltaTime;
    private long start;

    public DeltaTime() {
        this.deltaTime = 1;
    }

    public double get() {
        return this.deltaTime;
    }

    public void start() {
        this.start = System.nanoTime();
    }

    public void end() {
        this.deltaTime = (double)(System.nanoTime() - this.start) / 1e+9;
    }
}
