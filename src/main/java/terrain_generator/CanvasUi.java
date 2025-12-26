package terrain_generator;

import javax.swing.*;
import java.awt.*;

public class CanvasUi extends JPanel {
    JLabel fpsLabel;
    Timer fpsTimedEvent;

    TerrainCanvas canvas;

    public CanvasUi(TerrainCanvas canvas) {
        this.fpsLabel = new JLabel("FPS: 1");
        this.fpsLabel.setForeground(Color.black);

        this.add(fpsLabel);

        this.canvas = canvas;

        this.fpsTimedEvent = new Timer(500, (e) -> {
            if (this.canvas.isRunning()) {
                this.fpsLabel.setText("FPS: " + Math.round(this.canvas.getFps()));
            }
        });

        this.fpsTimedEvent.setRepeats(true);
        this.fpsTimedEvent.start();
    }

    public void stopRunning() {
        this.fpsTimedEvent.stop();
    }

}
