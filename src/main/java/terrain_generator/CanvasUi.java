package terrain_generator;

import javax.swing.*;
import java.awt.*;

public class CanvasUi extends JPanel {
    JLabel fpsLabel;
    Timer fpsTimedEvent;

    TerrainCanvas canvas;

    long lastTimeFpsDisplayed;

    public CanvasUi(TerrainCanvas canvas) {
        this.fpsLabel = new JLabel("FPS: 1");
        this.fpsLabel.setForeground(Color.black);

        this.add(fpsLabel);

        this.canvas = canvas;
        this.lastTimeFpsDisplayed = System.currentTimeMillis();

        this.fpsTimedEvent = new Timer(500, (e) -> {
            if (this.canvas.isRunning() && System.currentTimeMillis() - this.lastTimeFpsDisplayed > 1000) {
                this.fpsLabel.setText("FPS: " + Math.round(this.canvas.getFps()));
                this.lastTimeFpsDisplayed = System.currentTimeMillis();
            }
        });

        this.fpsTimedEvent.setRepeats(true);
        this.fpsTimedEvent.start();
    }

    public void stopRunning() {
        this.fpsTimedEvent.stop();
    }

}
