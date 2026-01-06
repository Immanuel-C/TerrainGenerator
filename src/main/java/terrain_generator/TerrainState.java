package terrain_generator;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class TerrainState {
    // Noise Settings
    volatile int octaves = 6, width = 128, length = 128;
    volatile float frequency = 0.01f, amplitude = 10.0f, frequencyMultiplier = 2.0f, amplitudeMultiplier = 0.2f;
    volatile boolean shouldGenerateTerrain;

}
