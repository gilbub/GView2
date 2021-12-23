package gvdecoder;
import java.awt.*;
import java.awt.event.*;
import javax.swing.AbstractButton;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JFrame;
import javax.swing.ImageIcon;
import gvdecoder.prefs.*;

public class ButtonDemo extends JPanel
                        implements ActionListener {
    protected JButton b1, b2, b3;
    public volatile int p=10;
    public volatile String l="loo";
    public volatile double myparam=2.33;
    public volatile boolean truefalse=true;

    public ButtonDemo() {

        b1 = new JButton("Disable middle button");
        b1.setVerticalTextPosition(AbstractButton.CENTER);
        b1.setHorizontalTextPosition(AbstractButton.LEFT);
        b1.setMnemonic(KeyEvent.VK_D);
        b1.setActionCommand("disable");

        b2 = new JButton("Middle button");
        b2.setVerticalTextPosition(AbstractButton.BOTTOM);
        b2.setHorizontalTextPosition(AbstractButton.CENTER);
        b2.setMnemonic(KeyEvent.VK_M);

        b3 = new JButton("Enable middle button");
        //Use the default text position of CENTER, RIGHT.
        b3.setMnemonic(KeyEvent.VK_E);
        b3.setActionCommand("enable");
        b3.setEnabled(false);

        //Listen for actions on buttons 1 and 3.
        b1.addActionListener(this);
        b3.addActionListener(this);

        b1.setToolTipText("Click this button to disable the middle button.");
        b2.setToolTipText("This middle button does nothing when you click it.");
        b3.setToolTipText("Click this button to enable the middle button.");

        //Add Components to this container, using the default FlowLayout.
        add(b1);
        add(b2);
        add(b3);
        PrefManager.getInstance().register("button test", this);
         JFrame frame = new JFrame("ButtonDemo");

		        frame.addWindowListener(new WindowAdapter() {
		            public void windowClosing(WindowEvent e) {
		                System.exit(0);
		            }
		        });

		        frame.getContentPane().add(this, BorderLayout.CENTER);
		        frame.pack();
		        frame.setVisible(true);
    }


    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals("disable")) {
            b2.setEnabled(false);
            b1.setEnabled(false);
            b3.setEnabled(true);
        } else {
            b2.setEnabled(true);
            b1.setEnabled(true);
            b3.setEnabled(false);
            System.out.println("the value of p="+p);
            System.out.println("the value of l="+l);
            System.out.println("the value of lbool="+truefalse);
			System.out.println("the value of double="+myparam);
        }

    }

    public static void main(String[] args) {
     ButtonDemo bd=new ButtonDemo();
    }
}

