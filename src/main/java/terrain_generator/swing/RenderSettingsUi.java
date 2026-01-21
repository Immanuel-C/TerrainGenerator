package terrain_generator.swing;

import terrain_generator.RenderSettings;

import javax.swing.*;

public class RenderSettingsUi extends JPanel {
    final RenderSettings renderSettings;

    public RenderSettingsUi(RenderSettings renderSettings) {
        this.renderSettings = renderSettings;

        LabeledSpinner ambientStrengthSpinner = new LabeledSpinner("Ambient Lighting Strength", new SpinnerNumberModel((double) this.renderSettings.getAmbientStrength(), 0.1, 1.0, 0.1));

        ambientStrengthSpinner.getSpinner().addChangeListener((ignored) -> {
            renderSettings.setAmbientStrength(((Double)ambientStrengthSpinner.getSpinner().getValue()).floatValue());
        });

        JCheckBox wireFrameCheckBox = new JCheckBox("Enable Wire Frame");

        wireFrameCheckBox.addActionListener((ignored) -> {
            renderSettings.setWireFrame(wireFrameCheckBox.isSelected());
        });

        JCheckBox renderNormalDirections = new JCheckBox("Render Normal Directions");

        renderNormalDirections.addActionListener((ignored) -> {
            renderSettings.setRenderNormalDirections(renderNormalDirections.isSelected());
        });

        ColourPicker colourPicker = new ColourPicker(this.renderSettings.getClearColour(), 5_000, (newColour) -> this.renderSettings.setClearColour(newColour));

        this.add(ambientStrengthSpinner);
        this.add(wireFrameCheckBox);
        this.add(renderNormalDirections);
        this.add(colourPicker);

    }

}
