import java.awt.event.*;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class Input implements KeyListener, MouseListener, MouseMotionListener, MouseWheelListener {
    private Set<Integer> keys, mouseButtons;

    // Atomic primitives are used as blocking the EDT will cause the GUI to
    // freeze. Atomic primitives do come at a cost compared to sync objects like locks especially
    // since mutating it or accessing it is slower compared to a regular primitive (2-10x slower).
    // Atomic wrappers are used instead of volatile as there is no possible way this is not thread safe unlike volatiles at the cost
    // of simplicity.
    private AtomicInteger modifiers, mouseX, mouseY, mouseScroll, mouseScrollSum;

    public Input() {
        // Concurrent Sets allow the set to be modified in a non-blocking manner.
        this.keys = ConcurrentHashMap.newKeySet();
        this.mouseButtons = ConcurrentHashMap.newKeySet();

        this.modifiers = new AtomicInteger(0);
        this.mouseX = new AtomicInteger(0);
        this.mouseY = new AtomicInteger(0);
        this.mouseScroll = new AtomicInteger(0);
        this.mouseScrollSum = new AtomicInteger(0);
    }

    public boolean isKeyDown(int key, int modifiers) {
        return this.keys.contains(key);
    }

    public boolean isKeyUp(int key, int modifiers) {
        return !this.keys.contains(key);    }

    public boolean isModifiersDown(int modifiers) {
        return (this.modifiers.get() & modifiers) != 0;
    }

    public boolean isMouseButtonDown(int button) {
        return this.mouseButtons.contains(button);
    }

    public boolean isMouseButtonUp(int button) {
        return !this.mouseButtons.contains(button);
    }

    public int getMouseScroll() {
        return this.mouseScroll.get();
    }

    public int getMouseScrollSum() {
        return this.mouseScrollSum.get();
    }

    @Override
    public void keyTyped(KeyEvent keyEvent) {
    }

    @Override
    public synchronized void keyPressed(KeyEvent keyEvent) {
        this.keys.add(keyEvent.getKeyCode());
        this.modifiers.set(keyEvent.getModifiersEx());
    }

    @Override
    public void keyReleased(KeyEvent keyEvent) {
        this.keys.remove(keyEvent.getKeyCode());
        this.modifiers.set(keyEvent.getModifiersEx());
    }

    @Override
    public void mouseClicked(MouseEvent mouseEvent) {

    }

    @Override
    public void mousePressed(MouseEvent mouseEvent) {
        this.mouseButtons.add(mouseEvent.getButton());
        // this.modifiers = mouseEvent.getModifiersEx();
    }

    @Override
    public void mouseReleased(MouseEvent mouseEvent) {
        this.mouseButtons.remove(mouseEvent.getButton());
        // this.modifiers = mouseEvent.getModifiersEx();
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
        this.mouseX.set(mouseEvent.getX());
        this.mouseY.set(mouseEvent.getY());
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent mouseWheelEvent) {
        // Usually you would add to the current mouse scroll value to represent
        // total mouse scroll in a GUI but in 3D applications knowing the instantaneous
        // mouse scroll is much more important. This is why there are two mouse scroll values.
        this.mouseScroll.set(mouseWheelEvent.getWheelRotation());
        this.mouseScrollSum.getAndAdd(mouseWheelEvent.getWheelRotation());
    }
}
