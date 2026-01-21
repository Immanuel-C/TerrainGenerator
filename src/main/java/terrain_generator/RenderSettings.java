package terrain_generator;

import java.awt.*;

public class RenderSettings {
    private volatile float ambientStrength = 0.1f;
    private volatile boolean wireFrame = false, renderNormalDirections = false;
    private volatile Color clearColour = new Color(0.5f, 0.6f, 0.7f);

    public float getAmbientStrength() {
        return ambientStrength;
    }

    public void setAmbientStrength(float ambientStrength) {
        this.ambientStrength = ambientStrength;
    }

    public boolean isWireFrame() {
        return wireFrame;
    }

    public void setWireFrame(boolean wireFrame) {
        this.wireFrame = wireFrame;
    }

    public boolean isRenderNormalDirections() {
        return renderNormalDirections;
    }

    public void setRenderNormalDirections(boolean renderNormalDirections) {
        this.renderNormalDirections = renderNormalDirections;
    }

    public Color getClearColour() {
        return clearColour;
    }

    public void setClearColour(Color clearColour) {
        this.clearColour = clearColour;
    }
}
