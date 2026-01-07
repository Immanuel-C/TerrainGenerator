package terrain_generator.swing;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;

public class DropDownMenu extends JPanel implements ActionListener, PopupMenuListener {
    JPopupMenu menu;
    JButton trigger;
    String triggerLabel;

    public DropDownMenu(String label) {
        this.triggerLabel = label;
        this.trigger = new JButton(" v " + this.triggerLabel);
        this.trigger.addActionListener(this);
        this.trigger.setBackground(Color.WHITE);
        this.trigger.setBorder(new LineBorder(Color.BLACK));



        this.menu = new JPopupMenu(label);
        this.menu.addPopupMenuListener(this);

        // Must call super.add because this class overrides the add method.
        super.add(this.trigger);
        super.add(this.menu);

    }

    @Override
    public Component add(Component component) {
        return this.menu.add(component);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == this.trigger) {
            this.menu.show(this.trigger, this.trigger.getX(), this.trigger.getY() + this.trigger.getHeight());
        }
    }


    @Override
    public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
        this.trigger.setText(" ^ " + this.triggerLabel);
    }

    @Override
    public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
        this.trigger.setText(" v " + this.triggerLabel);
    }

    @Override
    public void popupMenuCanceled(PopupMenuEvent e) {
        this.trigger.setText(" v " + this.triggerLabel);
    }
}
