package terrain_generator;

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

        ambientStrengthSpinner.getSpinner().addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                renderSettings.ambientStrength = ((Double)ambientStrengthSpinner.getSpinner().getValue()).floatValue();
            }
        });

        JCheckBox wireFrameCheckBox = new JCheckBox("Enable Wire Frame");

        wireFrameCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                renderSettings.wireFrame = wireFrameCheckBox.isSelected();
            }
        });

        this.add(ambientStrengthSpinner);
        this.add(wireFrameCheckBox);
    }

}
