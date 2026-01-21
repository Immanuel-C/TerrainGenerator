package terrain_generator.swing;

import org.joml.Matrix2d;
import org.joml.Vector2d;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Arc2D;
import java.awt.image.BufferedImage;
import java.util.function.Consumer;

public class ColourPicker extends JPanel implements ActionListener, ChangeListener {
    private final Consumer<Color> colourChangedListener;
    private Color currentColour;
    private JTextField hexCodeLabel;
    private LabeledSpinner[] rgbSpinners;
    private JButton testButton;
    private ColourWheel colourWheel;

    private JPanel colourInfoPanel;
    private JPanel rgbSpinnersPanel;
    private boolean stateChangedByProgram;


    public ColourPicker(Color initialColour, int colourWheelRadius, Consumer<Color> colourChangedListener) {
        this.setLayout(new GridLayout(2, 1));

        this.currentColour = initialColour;
        this.colourChangedListener = colourChangedListener;

        this.colourWheel = new ColourWheel(colourWheelRadius, this.currentColour, (newColour) -> {
            this.stateChangedByProgram = true;
            this.currentColour = newColour;
            this.setRgbSpinners(newColour);
            this.currentColourToHex();
            this.stateChangedByProgram = false;
            this.colourChangedListener.accept(this.currentColour);
        });

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
        this.colourWheel.setPreferredSize(new Dimension(this.getWidth() / 2, this.getHeight() / 2));
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

        this.setRgbSpinners(newColour);
    }

    private void setRgbSpinners(Color newColour) {
        this.rgbSpinners[0].getSpinner().setValue(newColour.getRed());
        this.rgbSpinners[1].getSpinner().setValue(newColour.getGreen());
        this.rgbSpinners[2].getSpinner().setValue(newColour.getBlue());
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
        this.colourWheel.setCurrentColour(this.currentColour);
        this.colourChangedListener.accept(this.currentColour);
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        this.setCurrentColour();
        this.currentColourToHex();
        this.testButton.setBackground(this.currentColour);

        if (!this.stateChangedByProgram)
            this.colourWheel.setCurrentColour(this.currentColour);

        this.colourChangedListener.accept(this.currentColour);
    }

    // The Colour Wheel operates in three coordinate systems referred to as "space(s)" in comments.
    // - The JPanel pixel space is the size of the JPanel min dimension (min(width, height)). The origin is the top left pixel of the JPanel.
    // - The image pixel or image space is the size of the unscaled image which has a width and height of the diameter of the circle. The origin of this space is the top left pixel.
    // - The circle space's origin starts at the center of the circle. Any pixel that lies within the radius and center of the circle is a part of the circle. Its basically just the image pixel space with a translated origin.
    // This class was separated from the colour picker as it's easier for me to deal with certain components at a time and figuring out how to connect them
    // rather than creating one giant component. With this class anything I change can basically only deal with its context and not with the entire colour picker.
    // TLDR: I'm breaking the problem down into simpler parts.
    private static class ColourWheel extends JPanel implements MouseListener, MouseMotionListener {
        private final BufferedImage colourWheel;
        private final int radius;
        public Color currentColour;
        // In JPanel pixel space.
        private Vector2d pickerPosition;
        private final Vector2d upVector;

        private final Consumer<Color> colourChangedListener;

        // The higher the radius the higher quality the image
        // but it uses more memory and takes longer to generate.
        public ColourWheel(int radius, Color initialColour, Consumer<Color> colourChangedListener) {
            this.currentColour = initialColour;
            this.radius = radius;
            // Up is at 0 rad/0 degrees
            // When creating the image the y-axis is flipped.
            // Since the origin of the image space is the top left pixel we are currently in
            // the bottom right pixel is (diameter, diameter). When moving the origin to the center
            // the bottom right pixel is now (radius, radius) so the top left pixel must now be (-radius, -radius).
            this.upVector = new Vector2d(0, -1);

            int diameter = this.radius * 2;
            this.colourWheel = new BufferedImage(diameter, diameter, BufferedImage.TYPE_INT_ARGB);

            for (int x = 0; x < diameter; x++) {
                for (int y = 0; y < diameter; y++) {
                    Vector2d point = this.imagePixelSpaceToCircleSpace(new Vector2d(x, y));

                    if (!this.isPointInsideCircle(point)) {
                        this.colourWheel.setRGB(x, y, new Color(0, 0, 0, 0).getRGB());
                        continue;
                    }

                    // Hue is always measured in degrees.
                    // Get the angle between the up dir and the point.
                    // Divide by PI since that is the period of the method (The max angle between the two angle is +/- PI).
                    // Convert to the range of [-1, 1] to [0, 1] which the HSBtoRGB method expects.
                    // [0, 1]
                    float hue = (float)((point.angle(this.upVector) / Math.PI) + 1.0f) / 2.0f;


                    // [0%, 100%] or [0.0, 1.0]
                    // Get the magnitude of the vector and divide it by the max value it can be
                    // to get it into the range of 0.0, 1.0.
                    float saturation = (float) (point.length() / this.radius);

                    // Lightness is always 0.5, RGB does not contain lightness information.
                    // [0%, 100%] or [0.0, 1.0]
                    float lightness = 1.f;


                    int argb = Color.HSBtoRGB(hue, saturation, lightness);

                    this.colourWheel.setRGB(x, y, argb);
                }
            }


            this.addMouseListener(this);
            this.addMouseMotionListener(this);
            this.colourChangedListener = colourChangedListener;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            int size = this.getScaledSize();
            g.drawImage(this.colourWheel, 0, 0, size, size, this);

            if (this.pickerPosition == null) {
                this.pickerPosition = new Vector2d();
                this.setCurrentColour(this.currentColour);
            }

            Vector2d arcSize = new Vector2d(10, 10);
            Arc2D pickerCircle = new Arc2D.Double(
                    this.pickerPosition.x - arcSize.x / 2.0,
                    this.pickerPosition.y - arcSize.y / 2.0,
                    arcSize.x,
                    arcSize.y,
                    0,
                    360,
                    Arc2D.OPEN
            );

            Graphics2D g2d = (Graphics2D)g;
            g2d.setColor(this.currentColour);
            g2d.fill(pickerCircle);
            g2d.setColor(Color.BLACK);
            g2d.draw(pickerCircle);
        }

        // The origin of this point must be the center of the circle as well.
        private boolean isPointInsideCircle(Vector2d point) {
            return point.lengthSquared() < Math.pow(this.radius, 2);
        }

        private Vector2d imagePixelSpaceToCircleSpace(Vector2d point) {
             return point.sub(new Vector2d(this.radius));
        }

        private Vector2d jPanelPixelSpaceToImagePixelSpace(Vector2d point) {
            return point.mul(2.0 * this.radius / this.getScaledSize());
        }

        private int getScaledSize() {
            return Math.min(this.getWidth(), this.getHeight());
        }

        private void updatePickerPosition(Vector2d point) {
            // Picker position must be in scaled image space. But the isPointerInsideCircle method checks in circle space.
            // A copy is made to check.
            Vector2d copy1 = this.jPanelPixelSpaceToImagePixelSpace(new Vector2d(point));
            Vector2d copy2 = this.imagePixelSpaceToCircleSpace(new Vector2d(copy1));

            if (this.isPointInsideCircle(copy2)) {
                this.pickerPosition = point;
                // Repaint to draw the new picker position
                this.repaint();
                this.currentColour = new Color(this.colourWheel.getRGB((int) copy1.x, (int) copy1.y));
                this.colourChangedListener.accept(this.currentColour);
            }
        }

        // newColor must be in RGB format.
        private void setCurrentColour(Color newColour) {
            float[] hsb = Color.RGBtoHSB(newColour.getRed(), newColour.getGreen(), newColour.getBlue(), new float[3]);

            // To get the point back we must do the inverse of this equation.
            // float hue = (float)(point.angle(up) / Math.PI);
            double angle = -(2 * hsb[0] - 1) * Math.PI;
            double magnitude = hsb[1] * this.radius;
            // rotate the direction of the up vector angle radians using a rotation matrix.
            // This results in the direction of the point from the center of the circle.
            Matrix2d transformationMatrix = new Matrix2d()
                    .scale(magnitude)
                    .rotate(angle);
            Vector2d positionImageSpace = new Vector2d(this.upVector).mul(transformationMatrix);
            positionImageSpace.add(new Vector2d(this.radius));

            this.currentColour = new Color(this.colourWheel.getRGB((int) positionImageSpace.x, (int) positionImageSpace.y));

            this.pickerPosition.set(positionImageSpace.mul(((double) this.getScaledSize() / (this.radius * 2))));
            this.repaint();
        }

        @Override
        public void mouseClicked(MouseEvent e) {
    }

        @Override
        public void mousePressed(MouseEvent e) {
            Vector2d point = new Vector2d(e.getX(), e.getY());
            this.updatePickerPosition(point);
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
            this.updatePickerPosition(point);
        }

        @Override
        public void mouseMoved(MouseEvent e) {

        }
    }
}
