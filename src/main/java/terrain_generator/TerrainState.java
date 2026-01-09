package terrain_generator;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class TerrainState {
    // Noise Settings
    volatile public int octaves = 6, width = 512, length = 512;
    volatile public float frequency = 0.01f, amplitude = 35.0f, frequencyMultiplier = 2.0f, amplitudeMultiplier = 0.3f;
    volatile public boolean shouldGenerateTerrain;

}
