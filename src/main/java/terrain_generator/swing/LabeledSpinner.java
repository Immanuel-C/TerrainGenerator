package terrain_generator.swing;

import javax.swing.*;

public class LabeledSpinner extends JPanel {
    JLabel label;
    JSpinner spinner;

    public LabeledSpinner(String label, SpinnerModel model) {
        this.label = new JLabel(label);
        this.spinner = new JSpinner(model);

        this.add(this.label);
        this.add(this.spinner);
    }

    public JSpinner getSpinner() {
        return this.spinner;
    }

    public JLabel getLabel() {
        return this.label;
    }
}
