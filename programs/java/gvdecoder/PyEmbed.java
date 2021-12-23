package gvdecoder;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import org.python.core.*;
import org.python.util.*;

public class PyEmbed extends JFrame {

    PythonInterpreter interp;
    JPanel easel;
    String startFile;
    JTextField textField;

    public PyEmbed(String startup) {
        this.startFile = startup;
        this.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent evt) {
                System.exit(0);
            }});
        this.getContentPane().setLayout(new BorderLayout());
        this.easel = new JPanel();
        this.easel.setPreferredSize(new Dimension(200, 200));
        this.getContentPane().add(this.easel, BorderLayout.CENTER);
        this.textField = new JTextField();
        this.textField.setPreferredSize(new Dimension(200, 20));
        this.getContentPane().add(this.textField, BorderLayout.SOUTH);
        this.textField.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                PyEmbed.this.interp.exec(PyEmbed.this.textField.getText());
                PyEmbed.this.textField.setText("");
            }});
        this.pack();
    }

    public void initInterp() {
        PySystemState.initialize();
        this.interp = new PythonInterpreter();
        this.interp.set("w", this);
        this.interp.set("e", this.easel);
        this.interp.set("g", this.easel.getGraphics());
    }

    public void show() {
        super.show();
        this.initInterp();
        this.interp.execfile(this.startFile);
    }

    public static void main(String[] argv) {
        PyEmbed embed = new PyEmbed("gview_python.py");
        embed.show();
    }

}

