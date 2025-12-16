import org.lwjgl.opengl.awt.GLData;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.event.WindowStateListener;

public class TerrainGenerator extends JFrame implements WindowListener {
    Thread renderThread;
    TerrainCanvas canvas;


    public TerrainGenerator() throws InterruptedException {
        JFrame frame = new JFrame("Terrain Generator");

        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setLayout(new BorderLayout());
        frame.setPreferredSize(new Dimension(1280, 720));
        GLData data = new GLData();
        data.majorVersion = 4;
        data.minorVersion = 3;
        data.doubleBuffer = true;
        data.swapInterval = null;

        canvas = new TerrainCanvas(data);
        frame.add(canvas, BorderLayout.CENTER);

        JPanel panel = new JPanel();

        panel.setBackground(Color.BLACK);
        panel.setMinimumSize(new Dimension(200, 720));
        panel.setPreferredSize(new Dimension(200, 720));

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
