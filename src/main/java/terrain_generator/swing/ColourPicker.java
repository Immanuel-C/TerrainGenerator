package terrain_generator.swing;

import org.joml.Vector2d;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;

public class ColourPicker extends JPanel implements ActionListener, ChangeListener {
    private Color currentColour;
    private JTextField hexCodeLabel;
    private LabeledSpinner[] rgbSpinners;
    private JButton testButton;
    private ColourWheel colourWheel;

    private JPanel colourInfoPanel;
    private JPanel rgbSpinnersPanel;


    public ColourPicker(Color initialColour, int colourWheelRadius) {
        this.setLayout(new GridLayout(2, 1));

        this.currentColour = initialColour;

        this.colourWheel = new ColourWheel(colourWheelRadius, this.currentColour);
        this.add(this.colourWheel);

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

        this.add(this.colourInfoPanel);
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

    private static class ColourWheel extends JPanel implements MouseListener, MouseMotionListener {
        private final BufferedImage colourWheel;
        private final int radius;
        // in image space;
        private Vector2d pickerPosition;


        // The higher the radius the higher quality the image
        // but it uses more memory and takes longer to generate.
        public ColourWheel(int radius, Color initialColour) {
            this.radius = radius;

            int diameter = this.radius * 2;
            this.colourWheel = new BufferedImage(diameter, diameter, BufferedImage.TYPE_INT_ARGB);

            // Up is a 0 rad
            Vector2d up = new Vector2d(0.0, -1.0);

            for (int x = 0; x < diameter; x++) {
                for (int y = 0; y < diameter; y++) {
                    Vector2d point = this.imageSpaceToCircleSpace(new Vector2d(x, y));

                    if (!this.isPointInsideCircle(point)) {
                        this.colourWheel.setRGB(x, y, new Color(0, 0, 0, 0).getRGB());
                        continue;
                    }

                    // Hue is always measured in degrees.
                    // Get the angle between the up dir and the point.
                    // [0 deg, 360 deg]
                    float hue = (float) (Math.toDegrees(point.angle(up)) / 360.0f);

                    if (hue < 0)
                        hue += 360;


                    // [0%, 100%] or [0.0, 1.0]
                    // Get the magnitude of the vector and divide it by the max value it can be
                    // to get it into the range of 0.0, 1.0.
                    float saturation = (float) (point.length() / this.radius);

                    // Lightness is always 0.5, RGB does not contain lightness information.
                    // [0%, 100%] or [0.0, 1.0]
                    float lightness = 1.f;


                    int argb = Color.getHSBColor(hue, saturation, lightness).getRGB();
                    this.colourWheel.setRGB(x, y, argb);
                }
            }

            this.addMouseListener(this);
            this.addMouseMotionListener(this);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            int size = this.getScaledSize();
            g.drawImage(this.colourWheel, 0, 0, size, size, this);
            if (this.pickerPosition != null)
                g.drawRect((int) this.pickerPosition.x, (int) this.pickerPosition.y, 1, 1);
        }

        // This point must be in the image pixel space.
        // The origin of this point must be the center of the circle as well.
        private boolean isPointInsideCircle(Vector2d point) {
            return point.lengthSquared() < Math.pow(this.radius, 2);
        }

        private Vector2d imageSpaceToCircleSpace(Vector2d point) {
             return point.sub(new Vector2d(this.radius));
        }

        private Vector2d scaledImageSpaceToImageSpace(Vector2d point) {
            return point.mul(2.0 * this.radius / this.getScaledSize());
        }

        private int getScaledSize() {
            return Math.min(this.getWidth(), this.getHeight());
        }

        @Override
        public void mouseClicked(MouseEvent e) {
    }

        @Override
        public void mousePressed(MouseEvent e) {
            Vector2d point = new Vector2d(e.getX(), e.getY());
            this.scaledImageSpaceToImageSpace(point);
            Vector2d copy = this.imageSpaceToCircleSpace(new Vector2d(point));

            if (this.isPointInsideCircle(copy)) {
                this.pickerPosition = point;
            }

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


        @Override
        public void mouseDragged(MouseEvent e) {
            Vector2d point = new Vector2d(e.getX(), e.getY());
            this.scaledImageSpaceToImageSpace(point);
            this.imageSpaceToCircleSpace(point);

            System.out.println("Point: " + point + " is in the colour wheel? " + isPointInsideCircle(point));
        }

        @Override
        public void mouseMoved(MouseEvent e) {

        }
    }
}
