package terrain_generator;

public class TerrainState {
    // Noise Settings
    private volatile int octaves = 6;
    private volatile int width = 512;
    private volatile int length = 512;
    private volatile float frequency = 0.01f;
    private volatile float amplitude = 35.0f;
    private volatile float frequencyMultiplier = 2.0f;
    private volatile float amplitudeMultiplier = 0.3f;
    private volatile boolean shouldGenerateTerrain;

    public int getOctaves() {
        return octaves;
    }

    public void setOctaves(int octaves) {
        this.octaves = octaves;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public float getFrequency() {
        return frequency;
    }

    public void setFrequency(float frequency) {
        this.frequency = frequency;
    }

    public float getAmplitude() {
        return amplitude;
    }

    public void setAmplitude(float amplitude) {
        this.amplitude = amplitude;
    }

    public float getFrequencyMultiplier() {
        return frequencyMultiplier;
    }

    public void setFrequencyMultiplier(float frequencyMultiplier) {
        this.frequencyMultiplier = frequencyMultiplier;
    }

    public float getAmplitudeMultiplier() {
        return amplitudeMultiplier;
    }

    public void setAmplitudeMultiplier(float amplitudeMultiplier) {
        this.amplitudeMultiplier = amplitudeMultiplier;
    }

    public boolean isShouldGenerateTerrain() {
        return shouldGenerateTerrain;
    }

    public void setShouldGenerateTerrain(boolean shouldGenerateTerrain) {
        this.shouldGenerateTerrain = shouldGenerateTerrain;
    }
}
