package terrain_generator.swing;

import terrain_generator.TerrainState;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.util.concurrent.atomic.AtomicInteger;

public class CanvasUi extends JPanel {
    JLabel fpsLabel;
    Timer fpsTimedEvent;

    TerrainCanvas canvas;
    TerrainState terrainState;
    DropDownMenu terrainSettingsDropDown;


    public CanvasUi(TerrainCanvas canvas, TerrainState terrainState) {
        this.fpsLabel = new JLabel("FPS: 1");
        this.fpsLabel.setForeground(Color.black);

        this.terrainState = terrainState;
        this.terrainSettingsDropDown = new DropDownMenu("Terrain Settings");

        this.add(fpsLabel);

        LabeledSpinner octaveSpinner = new LabeledSpinner("Octaves: ", new SpinnerNumberModel(this.terrainState.octaves, 0, 100, 1));

        octaveSpinner.getSpinner().addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                terrainState.octaves = (int) octaveSpinner.getSpinner().getValue();
                terrainState.shouldGenerateTerrain = true;
            }
        });


        LabeledSpinner frequencySlider = new LabeledSpinner("Frequency: ", new SpinnerNumberModel(this.terrainState.frequency, 0.0, 1.0, 0.001));

        frequencySlider.getSpinner().addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                terrainState.frequency = ((Double) frequencySlider.getSpinner().getValue()).floatValue();
                terrainState.shouldGenerateTerrain = true;
            }
        });


        LabeledSpinner amplitudeSlider = new LabeledSpinner("Amplitude: ", new SpinnerNumberModel(this.terrainState.amplitude, 1, 50, 1));

        amplitudeSlider.getSpinner().addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                terrainState.amplitude = ((Double)amplitudeSlider.getSpinner().getValue()).floatValue();
                terrainState.shouldGenerateTerrain = true;
            }
        });


        LabeledSpinner frequencyMultiplierSpinner = new LabeledSpinner("Frequency Multiplier: ", new SpinnerNumberModel(this.terrainState.frequencyMultiplier, 1.1, 10.0, 1));

        frequencyMultiplierSpinner.getSpinner().addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                terrainState.frequencyMultiplier = ((Double)frequencyMultiplierSpinner.getSpinner().getValue()).floatValue();
                terrainState.shouldGenerateTerrain = true;
            }
        });

        LabeledSpinner amplitudeMultiplierSpinner = new LabeledSpinner("Amplitude Multiplier: ", new SpinnerNumberModel(this.terrainState.amplitudeMultiplier, 0.1, 1.0, 0.1));

        amplitudeMultiplierSpinner.getSpinner().addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                terrainState.amplitudeMultiplier = ((Double)amplitudeMultiplierSpinner.getSpinner().getValue()).floatValue();
                terrainState.shouldGenerateTerrain = true;
            }
        });


        LabeledSpinner widthSpinner = new LabeledSpinner("Width: ", new SpinnerNumberModel(this.terrainState.width, 16, 1024, 2));

        widthSpinner.getSpinner().addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                terrainState.width = (Integer)widthSpinner.getSpinner().getValue();

                if (terrainState.width % 2 != 0) {
                    // Has to be done to satisfy the requirements of volatile primitives.
                    // TODO: extract this into a method.
                    AtomicInteger newWidth = new AtomicInteger(terrainState.width + 1);
                    widthSpinner.getSpinner().setValue(newWidth.get());
                    terrainState.width = newWidth.get();
                }

                terrainState.shouldGenerateTerrain = true;
            }
        });


        LabeledSpinner lengthSpinner = new LabeledSpinner("Length: ", new SpinnerNumberModel(this.terrainState.length, 16, 1024, 2));

        lengthSpinner.getSpinner().addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                terrainState.length = (Integer)lengthSpinner.getSpinner().getValue();

                if (terrainState.length % 2 != 0) {
                    // Has to be done to satisfy the requirements of volatile primitives.
                    // TODO: extract this into a method.
                    AtomicInteger newLength = new AtomicInteger(terrainState.length + 1);
                    lengthSpinner.getSpinner().setValue(newLength.get());
                    terrainState.length = newLength.get();
                }

                terrainState.shouldGenerateTerrain = true;
            }
        });


        this.terrainSettingsDropDown.add(octaveSpinner);
        this.terrainSettingsDropDown.add(frequencySlider);
        this.terrainSettingsDropDown.add(amplitudeSlider);
        this.terrainSettingsDropDown.add(frequencyMultiplierSpinner);
        this.terrainSettingsDropDown.add(amplitudeMultiplierSpinner);
        this.terrainSettingsDropDown.add(widthSpinner);
        this.terrainSettingsDropDown.add(lengthSpinner);
        this.add(this.terrainSettingsDropDown);

        this.canvas = canvas;

        this.fpsTimedEvent = new Timer(1000, (e) -> {
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
