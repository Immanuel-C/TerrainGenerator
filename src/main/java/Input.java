import java.awt.event.*;
import java.util.HashSet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReadWriteLock;

public class Input implements KeyListener, MouseListener, MouseMotionListener, MouseWheelListener {
    private HashSet<AtomicInteger> keys, mouseButtons;

    private AtomicInteger modifiers;

    private AtomicInteger mouseX, mouseY, mouseScroll;

    public Input() {
        this.keys = new HashSet<>();
        this.mouseButtons = new HashSet<>();
        this.modifiers = 0;
        this.mouseX = 0;
        this.mouseY = 0;
        this.mouseScroll = 0;
    }

    public boolean isKeyDown(int key, int modifiers) {
        return this.keys.contains(new AtomicInteger(key)) && this.isModifiersDown(modifiers);
    }

    public boolean isKeyUp(int key, int modifiers) {
        return !this.keys.contains(key);
    }

    public boolean isModifiersDown(int modifiers) {
        return (this.modifiers & modifiers) != 0;
    }

    public boolean isMouseButtonDown(int button) {
        return this.mouseButtons.contains(button);
    }

    public boolean isMouseButtonUp(int button) {
        return !this.mouseButtons.contains(button);
    }

    public int getMouseScroll() {
        return this.mouseScroll;
    }

    @Override
    public void keyTyped(KeyEvent keyEvent) {
    }

    @Override
    public synchronized void keyPressed(KeyEvent keyEvent) {
        this.keys.add(keyEvent.getKeyCode());
        this.modifiers = keyEvent.getModifiersEx();
    }

    @Override
    public void keyReleased(KeyEvent keyEvent) {
        this.keys.remove(keyEvent.getKeyCode());
        this.modifiers = keyEvent.getModifiersEx();
    }

    @Override
    public void mouseClicked(MouseEvent mouseEvent) {

    }

    @Override
    public void mousePressed(MouseEvent mouseEvent) {
        this.mouseButtons.add(mouseEvent.getButton());
        this.modifiers = mouseEvent.getModifiersEx();
    }

    @Override
    public void mouseReleased(MouseEvent mouseEvent) {
        this.mouseButtons.remove(mouseEvent.getButton());
        this.modifiers = mouseEvent.getModifiersEx();
    }

    @Override
    public void mouseEntered(MouseEvent mouseEvent) {

    }

    @Override
    public void mouseExited(MouseEvent mouseEvent) {

    }

    @Override
    public void mouseDragged(MouseEvent mouseEvent) {

    }

    @Override
    public void mouseMoved(MouseEvent mouseEvent) {
        this.mouseX = mouseEvent.getX();
        this.mouseY = mouseEvent.getY();
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent mouseWheelEvent) {
        if (mouseWheelEvent.getScrollType() == MouseWheelEvent.WHEEL_UNIT_SCROLL) {
            this.mouseScroll = (int)mouseWheelEvent.getPreciseWheelRotation();
        }
    }
}
