package terrain_generator.swing;

import terrain_generator.RenderSettings;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class RenderSettingsUi extends JPanel {
    final RenderSettings renderSettings;

    public RenderSettingsUi(RenderSettings renderSettings) {
        this.renderSettings = renderSettings;

        LabeledSpinner ambientStrengthSpinner = new LabeledSpinner("Ambient Lighting Strength", new SpinnerNumberModel((double)this.renderSettings.ambientStrength, 0.1, 1.0, 0.1));

        ambientStrengthSpinner.getSpinner().addChangeListener((ignored) -> {
            renderSettings.ambientStrength = ((Double)ambientStrengthSpinner.getSpinner().getValue()).floatValue();
        });

        JCheckBox wireFrameCheckBox = new JCheckBox("Enable Wire Frame");

        wireFrameCheckBox.addActionListener((ignored) -> {
            renderSettings.wireFrame = wireFrameCheckBox.isSelected();
        });

        JCheckBox renderNormalDirections = new JCheckBox("Render Normal Directions");

        renderNormalDirections.addActionListener((ignored) -> {
            renderSettings.renderNormalDirections = renderNormalDirections.isSelected();
        });

        ColourPicker colourPicker = new ColourPicker(this.renderSettings.clearColour, 5_000, (newColour) -> this.renderSettings.clearColour = newColour);

        this.add(ambientStrengthSpinner);
        this.add(wireFrameCheckBox);
        this.add(renderNormalDirections);
        this.add(colourPicker);

    }

}
