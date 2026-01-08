package terrain_generator.swing;

import org.lwjgl.opengl.awt.GLData;
import terrain_generator.RenderSettings;
import terrain_generator.TerrainState;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

public class TerrainGenerator extends JFrame implements WindowListener {
    TerrainCanvas canvas;
    JTabbedPane infoPane;
    CanvasUi canvasUi;
    Input input;

    public TerrainGenerator() throws InterruptedException {
        super("Terrain Generator");

        this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

        this.addWindowListener(this);

        this.setLayout(new BorderLayout());
        this.setPreferredSize(new Dimension(1280, 720));

        TerrainState terrainState = new TerrainState();
        RenderSettings renderSettings = new RenderSettings();

        GLData data = new GLData();
        data.majorVersion = 4;
        data.minorVersion = 3;
        data.doubleBuffer = true;
        data.samples = 4;
        data.swapInterval = 0;
        data.debug = true;
        data.profile = GLData.Profile.CORE;

        this.canvas = new TerrainCanvas(data, input, terrainState, renderSettings);

        this.infoPane = new JTabbedPane();
        this.infoPane.setPreferredSize(new Dimension(300, 720));

        canvasUi = new CanvasUi(canvas, terrainState);
        RenderSettingsUi renderSettingsUi = new RenderSettingsUi(renderSettings);

        this.infoPane.addTab("Terrain Data", this.canvasUi);
        this.infoPane.addTab("Render Settings", renderSettingsUi);

        this.add(this.infoPane, BorderLayout.WEST);
        this.add(canvas, BorderLayout.CENTER);

        this.pack();
        this.setVisible(true);

    }

    @Override
    public void windowOpened(WindowEvent windowEvent) {
    }

    @Override
    public void windowClosing(WindowEvent windowEvent) {
        this.canvasUi.stopRunning();
        this.canvas.stopRunning();
        this.dispose();
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
