import javax.swing.*;

public class CanvasUiTasks extends SwingWorker<Void, Void> {
    JLabel fpsLabel;
    TerrainCanvas canvas;

    long lastTimeFpsDisplayed;

    public CanvasUiTasks(TerrainCanvas canvas, JLabel fpsLabel) {
        this.fpsLabel = fpsLabel;
        this.canvas = canvas;
        this.lastTimeFpsDisplayed = System.currentTimeMillis();
    }

    @Override
    protected Void doInBackground() {
        while (this.canvas.isRunning()) {
            if (System.currentTimeMillis() - this.lastTimeFpsDisplayed > 1000) {
                this.fpsLabel.setText("FPS: " + Math.round(this.canvas.getFps()));
                this.lastTimeFpsDisplayed = System.currentTimeMillis();
            }
        }

        return null;
    }
}
