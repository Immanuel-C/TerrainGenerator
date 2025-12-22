package terrain_generator;

import org.lwjgl.opengl.awt.GLData;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

public class TerrainGenerator implements WindowListener {
    Thread renderThread;
    TerrainCanvas canvas;
    JTabbedPane infoPane;
    CanvasUi canvasUi;
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
        data.samples = 8;
        data.swapInterval = 0;
        data.debug = true;

        this.canvas = new TerrainCanvas(data, input);

        this.infoPane = new JTabbedPane();
        this.infoPane.setPreferredSize(new Dimension(300, 720));

        canvasUi = new CanvasUi(canvas);
        RendererDebugUi rendererDebugUi = new RendererDebugUi();

        this.infoPane.addTab("Terrain Data", canvasUi);
        this.infoPane.addTab("Renderer Debug Data", rendererDebugUi);

        frame.add(this.infoPane, BorderLayout.WEST);
        frame.add(canvas, BorderLayout.CENTER);

        frame.pack();
        frame.setVisible(true);

        this.renderThread = new Thread(canvas::run, "Terrain Canvas Render Thread");
        this.renderThread.start();
    }

    @Override
    public void windowOpened(WindowEvent windowEvent) {

    }

    @Override
    public void windowClosing(WindowEvent windowEvent) {
        this.canvasUi.stopRunning();
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
