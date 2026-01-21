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

        LabeledSpinner octaveSpinner = new LabeledSpinner("Octaves: ", new SpinnerNumberModel(this.terrainState.getOctaves(), 0, 100, 1));

        octaveSpinner.getSpinner().addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                terrainState.setOctaves((int) octaveSpinner.getSpinner().getValue());
                terrainState.setShouldGenerateTerrain(true);
            }
        });


        LabeledSpinner frequencySlider = new LabeledSpinner("Frequency: ", new SpinnerNumberModel(this.terrainState.getFrequency(), 0.0, 1.0, 0.001));

        frequencySlider.getSpinner().addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                terrainState.setFrequency(((Double) frequencySlider.getSpinner().getValue()).floatValue());
                terrainState.setShouldGenerateTerrain(true);
            }
        });


        LabeledSpinner amplitudeSlider = new LabeledSpinner("Amplitude: ", new SpinnerNumberModel(this.terrainState.getAmplitude(), 1, 50, 1));

        amplitudeSlider.getSpinner().addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                terrainState.setAmplitude(((Double)amplitudeSlider.getSpinner().getValue()).floatValue());
                terrainState.setShouldGenerateTerrain(true);
            }
        });


        LabeledSpinner frequencyMultiplierSpinner = new LabeledSpinner("Frequency Multiplier: ", new SpinnerNumberModel(this.terrainState.getFrequencyMultiplier(), 1.1, 10.0, 1));

        frequencyMultiplierSpinner.getSpinner().addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                terrainState.setFrequencyMultiplier(((Double)frequencyMultiplierSpinner.getSpinner().getValue()).floatValue());
                terrainState.setShouldGenerateTerrain(true);
            }
        });

        LabeledSpinner amplitudeMultiplierSpinner = new LabeledSpinner("Amplitude Multiplier: ", new SpinnerNumberModel(this.terrainState.getAmplitudeMultiplier(), 0.1, 1.0, 0.1));

        amplitudeMultiplierSpinner.getSpinner().addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                terrainState.setAmplitudeMultiplier(((Double)amplitudeMultiplierSpinner.getSpinner().getValue()).floatValue());
                terrainState.setShouldGenerateTerrain(true);
            }
        });


        LabeledSpinner widthSpinner = new LabeledSpinner("Width: ", new SpinnerNumberModel(this.terrainState.getWidth(), 16, 1024, 2));

        widthSpinner.getSpinner().addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                terrainState.setWidth((Integer)widthSpinner.getSpinner().getValue());

                if (terrainState.getWidth() % 2 != 0) {
                    // Has to be done to satisfy the requirements of volatile primitives.
                    // TODO: extract this into a method.
                    AtomicInteger newWidth = new AtomicInteger(terrainState.getWidth() + 1);
                    widthSpinner.getSpinner().setValue(newWidth.get());
                    terrainState.setWidth(newWidth.get());
                }

                terrainState.setShouldGenerateTerrain(true);
            }
        });


        LabeledSpinner lengthSpinner = new LabeledSpinner("Length: ", new SpinnerNumberModel(this.terrainState.getLength(), 16, 1024, 2));

        lengthSpinner.getSpinner().addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                terrainState.setLength((Integer)lengthSpinner.getSpinner().getValue());

                if (terrainState.getLength() % 2 != 0) {
                    // Has to be done to satisfy the requirements of volatile primitives.
                    // TODO: extract this into a method.
                    AtomicInteger newLength = new AtomicInteger(terrainState.getLength() + 1);
                    lengthSpinner.getSpinner().setValue(newLength.get());
                    terrainState.setLength(newLength.get());
                }

                terrainState.setShouldGenerateTerrain(true);
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
