package terrain_generator.swing;

import org.joml.Vector2i;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.io.File;
import java.io.IOException;
import java.util.HexFormat;

public class ColourPicker extends JPanel implements ActionListener, ChangeListener {
    private Color currentColour;
    private JTextField hexCodeLabel;
    private LabeledSpinner[] rgbSpinners;
    private JButton testButton;
    private ColourWheel colourWheel;

    private JPanel colourInfoPanel;
    private JPanel rgbSpinnersPanel;


    public ColourPicker(Color initialColour, int colourWheelRadius) {
        this.setLayout(new BorderLayout());

        this.currentColour = initialColour;

        this.colourWheel = new ColourWheel(colourWheelRadius, this.currentColour);
        this.add(this.colourWheel, BorderLayout.WEST);

        this.colourInfoPanel = new JPanel();
        this.colourInfoPanel.setLayout(new BoxLayout(this.colourInfoPanel, BoxLayout.Y_AXIS));

        this.testButton = new JButton();
        this.testButton.setBackground(this.currentColour);
        this.colourInfoPanel.add(this.testButton);
        this.hexCodeLabel = new JTextField(7);
        this.hexCodeLabel.setMaximumSize(this.hexCodeLabel.getPreferredSize());
        this.hexCodeLabel.addActionListener(this);
        this.colourInfoPanel.add(this.hexCodeLabel);

        this.rgbSpinners = new LabeledSpinner[3];
        this.rgbSpinners[0] = new LabeledSpinner("r: ", new SpinnerNumberModel(this.currentColour.getRed(), 0, 255, 1));
        this.rgbSpinners[1] = new LabeledSpinner("g: ", new SpinnerNumberModel(this.currentColour.getGreen(), 0, 255, 1));
        this.rgbSpinners[2] = new LabeledSpinner("b: ", new SpinnerNumberModel(this.currentColour.getBlue(), 0, 255, 1));

        this.rgbSpinnersPanel = new JPanel();
        this.rgbSpinnersPanel.setLayout(new BoxLayout(this.rgbSpinnersPanel, BoxLayout.X_AXIS));

        for (LabeledSpinner colourSpinner: this.rgbSpinners) {
            colourSpinner.getSpinner().addChangeListener(this);
            this.rgbSpinnersPanel.add(colourSpinner);
        }

        this.colourInfoPanel.add(this.rgbSpinnersPanel);

        this.currentColourToHex();
        this.currentHexCodeToColour();

        this.add(this.colourInfoPanel, BorderLayout.EAST);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        this.colourWheel.setMinimumSize(new Dimension(this.getWidth() / 2, this.getHeight() / 2));
    }

    private void currentColourToHex() {
        String newLabel =
            "#"
            + ColourPicker.toHex(this.currentColour.getRed())
            + ColourPicker.toHex(this.currentColour.getGreen())
            + ColourPicker.toHex(this.currentColour.getBlue());
        this.hexCodeLabel.setText(
                newLabel
        );
    }

    private void currentHexCodeToColour() {
        // Remove '#'
        String rawHex = this.hexCodeLabel.getText().substring(1);

        if (rawHex.length() != 6)
            return;

         Color newColour;

         try {
             newColour = new Color(
                Integer.parseInt(rawHex.substring(0, 2), 16),
                Integer.parseInt(rawHex.substring(2, 4), 16),
                Integer.parseInt(rawHex.substring(4, 6), 16)
            );
         } catch (NumberFormatException ignored) {
             return;
         }

        this.currentColour = newColour;

        this.setRgbSpinners();
    }

    private void setRgbSpinners() {
        this.rgbSpinners[0].getSpinner().setValue(this.currentColour.getRed());
        this.rgbSpinners[1].getSpinner().setValue(this.currentColour.getGreen());
        this.rgbSpinners[2].getSpinner().setValue(this.currentColour.getBlue());
    }

    private void setCurrentColour() {
        this.currentColour = new Color(
            (Integer)this.rgbSpinners[0].getSpinner().getValue(),
            (Integer)this.rgbSpinners[1].getSpinner().getValue(),
            (Integer)this.rgbSpinners[2].getSpinner().getValue()
        );
    }

    static private String toHex(int num) {
        String hex = Integer.toHexString(num);
        if (hex.length() == 1) {
            return "0" + hex;
        }
        return hex;
    }


    @Override
    public void actionPerformed(ActionEvent e) {
        this.currentHexCodeToColour();
        this.testButton.setBackground(this.currentColour);
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        this.setCurrentColour();
        this.currentColourToHex();
        this.testButton.setBackground(this.currentColour);
    }

    private class ColourWheel extends JPanel implements MouseListener {
        private BufferedImage colourWheel;
        private final int radius;

        public ColourWheel(int radius, Color initialColour) {
            this.radius = radius;

            int diameter = this.radius * 2;
            this.colourWheel = new BufferedImage(diameter, diameter, BufferedImage.TYPE_INT_RGB);

            double[] samples = new double[4];

            for (int x = 0; x < diameter; x++) {
                for (int y = 0; y < diameter; y++) {
                    double xDistanceToPoint = x - this.radius, yDistanceToPoint = y - this.radius;
                    // BR
                    samples[0] = Math.sqrt(Math.pow(xDistanceToPoint + 0.5, 2) + Math.pow(yDistanceToPoint + 0.5, 2));
                    // BL
                    samples[1] = Math.sqrt(Math.pow(xDistanceToPoint - 0.5, 2) + Math.pow(yDistanceToPoint + 0.5, 2));
                    // TR
                    samples[2] = Math.sqrt(Math.pow(xDistanceToPoint + 0.5, 2) + Math.pow(yDistanceToPoint - 0.5, 2));
                    // TL
                    samples[3] = Math.sqrt(Math.pow(xDistanceToPoint - 0.5, 2) + Math.pow(yDistanceToPoint - 0.5, 2));

                    int numSamplesInCircle = 0;

                    for (double sample: samples)
                        if (sample < this.radius)
                            numSamplesInCircle++;

                    int color = (int)Math.round((double) numSamplesInCircle / samples.length * 255.0);

                    this.colourWheel.setRGB(x, y, new Color(color, color, color).getRGB());
                }
            }

        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            int size = Math.min(this.getWidth(), this.getHeight());
            g.drawImage(this.colourWheel, 0, 0, size, size, this);
        }

        @Override
        public void mouseClicked(MouseEvent e) {

        }

        @Override
        public void mousePressed(MouseEvent e) {

        }

        @Override
        public void mouseReleased(MouseEvent e) {

        }

        @Override
        public void mouseEntered(MouseEvent e) {

        }

        @Override
        public void mouseExited(MouseEvent e) {

        }
    }
}
