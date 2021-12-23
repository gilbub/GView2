package gvdecoder.utilities;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Use this modal dialog to let the user choose one string from a long
 * list.  See the main method for an example of using ListDialog.  The
 * basics:
 * <pre>
    String[] choices = {"A", "long", "array", "of", "strings"};
    ListDialog.initialize(componentInControllingFrame, choices,
                          "Dialog Title",
                          "A description of the list:");
    String selectedName = ListDialog.showDialog(locatorComponent,
                                                initialSelection);
 * </pre>
 */
public class AVIDialog extends JDialog {
    public static AVIDialog dialog;
    private static String value = "";
    public static AVIDialogInfo aviinfo;
    private static JTextField start;
    private static JTextField end;
    private static JTextField filename;
    private static int maxframes=1;


    /**
     * Set up the dialog.  The first argument can be null,
     * but it really should be a component in the dialog's
     * controlling frame.
     */
    public static void initialize(Component comp,
                                  String labelText,int max) {
        Frame frame = JOptionPane.getFrameForComponent(comp);
		aviinfo=new AVIDialogInfo();
		maxframes=max;
        dialog = new AVIDialog(frame,  labelText);
    }

    /**
     * Show the initialized dialog.  The first argument should
     * be null if you want the dialog to come up in the center
     * of the screen.  Otherwise, the argument should be the
     * component on top of which the dialog should appear.
     */
    public static AVIDialogInfo showDialog(Component comp, String initialValue) {
        if (dialog != null) {
            dialog.setValue(initialValue);
            dialog.setLocationRelativeTo(comp);
            dialog.setVisible(true);
        } else {
            System.err.println("ListDialog requires you to call initialize "
                               + "before calling showDialog.");
        }
        return aviinfo;
    }

    private void setValue(String newValue) {
        value = newValue;
        //list.setSelectedValue(value, true);
    }
    public boolean setinfo(){
	  boolean valid=false;
	  aviinfo.name = filename.getText();//(String)(list.getSelectedValue());
	  aviinfo.start = Integer.parseInt(start.getText());
	  aviinfo.end = Integer.parseInt(end.getText());
      if ((aviinfo.start>-1)&&(aviinfo.end>aviinfo.start)&&(aviinfo.end<maxframes)) {valid=true;
         aviinfo.valid=true;
	    }
      return valid;
	}

    private AVIDialog(Frame frame,
                       String labelText) {
        super(frame, labelText, true);
		filename=new JTextField("filename");
		start=new JTextField("input start frame");
		end=new JTextField("input end frame");
        //buttons
        JButton cancelButton = new JButton("Cancel");
        final JButton setButton = new JButton("Set");
        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                AVIDialog.dialog.setVisible(false);
            }
        });
        setButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
				if (setinfo())
                 AVIDialog.dialog.setVisible(false);
            }
        });
        getRootPane().setDefaultButton(setButton);

        JPanel listPane = new JPanel();
        listPane.setLayout(new BoxLayout(listPane, BoxLayout.Y_AXIS));


        //listPane.add(filename);
        listPane.add(start);
         listPane.add(end);

        listPane.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

        //Lay out the buttons from left to right.
        JPanel buttonPane = new JPanel();
        buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.X_AXIS));
        buttonPane.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
        buttonPane.add(Box.createHorizontalGlue());
        buttonPane.add(cancelButton);
        buttonPane.add(Box.createRigidArea(new Dimension(10, 0)));
        buttonPane.add(setButton);

        //Put everything together, using the content pane's BorderLayout.
        Container contentPane = getContentPane();
        contentPane.add(listPane, BorderLayout.CENTER);
        contentPane.add(buttonPane, BorderLayout.SOUTH);

        pack();
    }

    /**
     * This is here so that you can view ListDialog even if you
     * haven't written the code to include it in a program.
     */
    public static void main(String[] args) {

        JFrame f = new JFrame("Name That Baby");
        f.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                 System.exit(0);
            }
        });

        JLabel intro = new JLabel("The chosen name:");

        final JLabel name = new JLabel("Cosmo");
        intro.setLabelFor(name);
        name.setForeground(Color.black);

        JButton button = new JButton("Pick a new name...");
        AVIDialog.initialize(f,"avi range", 100);
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
               AVIDialogInfo avd=AVIDialog.showDialog(null, "");

               System.out.println("name="+avd.name+" s="+avd.start+" e="+avd.end);

            }
        });

        JPanel contentPane = new JPanel();
        f.setContentPane(contentPane);
        contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));
        contentPane.setBorder(BorderFactory.createEmptyBorder(20,20,20,20));

        contentPane.add(intro);
        contentPane.add(name);
        contentPane.add(Box.createRigidArea(new Dimension(0,10)));
        contentPane.add(button);
        intro.setAlignmentX(JComponent.CENTER_ALIGNMENT);
        name.setAlignmentX(JComponent.CENTER_ALIGNMENT);
        button.setAlignmentX(JComponent.CENTER_ALIGNMENT);

        f.pack();
        f.setVisible(true);
    }
}
