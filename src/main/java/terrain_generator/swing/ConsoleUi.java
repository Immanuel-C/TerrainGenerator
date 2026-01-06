package terrain_generator.swing;

import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

public class ConsoleUi extends JPanel implements ComponentListener {
    private TextAreaOutputStream outputStream;
    private JTextArea consoleTextArea;

    public ConsoleUi() {
        this.consoleTextArea = new JTextArea();
        this.consoleTextArea.setEditable(false);
        this.consoleTextArea.setLineWrap(true);

        this.consoleTextArea.setSize(this.getSize());

        this.outputStream = new TextAreaOutputStream(this.consoleTextArea);

        System.setOut(new PrintStream(outputStream));

        this.add(consoleTextArea);
    }

    @Override
    public void componentResized(ComponentEvent e) {
        this.consoleTextArea.setSize(this.getSize());
    }

    @Override
    public void componentMoved(ComponentEvent e) {

    }

    @Override
    public void componentShown(ComponentEvent e) {

    }

    @Override
    public void componentHidden(ComponentEvent e) {

    }

    static class TextAreaOutputStream extends OutputStream {
        JTextArea textArea;

        public TextAreaOutputStream(JTextArea textArea) {
            this.textArea = textArea;
        }

        @Override
        public void write(int b) throws IOException {
            this.textArea.append(String.valueOf(b));
        }

        @Override
        public void write(@NotNull byte[] b, int off, int len) throws IOException {
            this.textArea.append(new String(b, off, len));
        }
    }
}
