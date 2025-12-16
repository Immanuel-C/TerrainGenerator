import org.lwjgl.opengl.awt.GLData;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.event.WindowStateListener;
import java.util.concurrent.Semaphore;

public class TerrainGenerator extends JFrame implements WindowListener {
    Thread renderThread;
    TerrainCanvas canvas;
    Input input;


    public TerrainGenerator() throws InterruptedException {
        JFrame frame = new JFrame("Terrain Generator");

        frame.addWindowListener(this);

        input = new Input();

        frame.addKeyListener(input);
        frame.addMouseListener(input);
        frame.addMouseWheelListener(input);
        frame.addMouseMotionListener(input);

        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setLayout(new BorderLayout());
        frame.setPreferredSize(new Dimension(1280, 720));
        GLData data = new GLData();
        data.majorVersion = 4;
        data.minorVersion = 3;
        data.doubleBuffer = true;
        data.swapInterval = 0;

        canvas = new TerrainCanvas(data, input);
        frame.add(canvas, BorderLayout.CENTER);

        JPanel panel = new JPanel();

        panel.setBackground(Color.BLACK);
        panel.setMinimumSize(new Dimension(200, 720));
        panel.setPreferredSize(new Dimension(200, 720));

        JLabel fpsLabel = new JLabel();

        panel.add(fpsLabel);

        CanvasUiTasks canvasTasks = new CanvasUiTasks(canvas, fpsLabel);
        canvasTasks.execute();

        frame.add(panel, BorderLayout.EAST);

        frame.pack();
        frame.setVisible(true);
        frame.transferFocus();

        renderThread = new Thread(canvas::run, "Terrain Canvas Render Thread");
        renderThread.start();


    }



    @Override
    public void windowOpened(WindowEvent windowEvent) {

    }

    @Override
    public void windowClosing(WindowEvent windowEvent) {
        this.canvas.stopRunning();
    }

    @Override
    public void windowClosed(WindowEvent windowEvent) {

    }

    @Override
    public void windowIconified(WindowEvent windowEvent) {

    }

    @Override
    public void windowDeiconified(WindowEvent windowEvent) {

    }

    @Override
    public void windowActivated(WindowEvent windowEvent) {

    }

    @Override
    public void windowDeactivated(WindowEvent windowEvent) {

    }
}
