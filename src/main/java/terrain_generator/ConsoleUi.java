package terrain_generator;

import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

public class ConsoleUi extends JPanel {
    private TextAreaOutputStream outputStream;
    private JTextArea consoleTextArea;

    public ConsoleUi() {
        this.consoleTextArea = new JTextArea();
        this.outputStream = new TextAreaOutputStream(this.consoleTextArea);

        System.setOut(new PrintStream(outputStream));

        this.add(new JScrollPane(this.consoleTextArea));
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
