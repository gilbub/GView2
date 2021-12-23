//package filters;

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
public class ListDialog extends JDialog {
    private static ListDialog dialog;
    private static String value = "";
    private JList list;

    /**
     * Set up the dialog.  The first argument can be null,
     * but it really should be a component in the dialog's
     * controlling frame.
     */
    public static void initialize(Component comp,
                                  String[] possibleValues,
                                  String title,
                                  String labelText) {
        Frame frame = JOptionPane.getFrameForComponent(comp);
        dialog = new ListDialog(frame, possibleValues,
                                title, labelText);
    }

    /**
     * Show the initialized dialog.  The first argument should
     * be null if you want the dialog to come up in the center
     * of the screen.  Otherwise, the argument should be the
     * component on top of which the dialog should appear.
     */
    public static String showDialog(Component comp, String initialValue) {
        if (dialog != null) {
            dialog.setValue(initialValue);
            dialog.setLocationRelativeTo(comp);
            dialog.setVisible(true);
        } else {
            System.err.println("ListDialog requires you to call initialize "
                               + "before calling showDialog.");
        }
        return value;
    }

    private void setValue(String newValue) {
        value = newValue;
        list.setSelectedValue(value, true);
    }

    private ListDialog(Frame frame, Object[] data, String title,
                       String labelText) {
        super(frame, title, true);

        //buttons
        JButton cancelButton = new JButton("Cancel");
        final JButton setButton = new JButton("Set");
        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                ListDialog.dialog.setVisible(false);
            }
        });
        setButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                ListDialog.value = (String)(list.getSelectedValue());
                ListDialog.dialog.setVisible(false);
            }
        });
        getRootPane().setDefaultButton(setButton);

        //main part of the dialog
        list = new JList(data);
        list.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        list.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    setButton.doClick();
                }
            }
        });
        JScrollPane listScroller = new JScrollPane(list);
        listScroller.setPreferredSize(new Dimension(250, 80));
        //XXX: Must do the following, too, or else the scroller thinks
        //XXX: it's taller than it is:
        listScroller.setMinimumSize(new Dimension(250, 80));
        listScroller.setAlignmentX(LEFT_ALIGNMENT);

        //Create a container so that we can add a title around
        //the scroll pane.  Can't add a title directly to the 
        //scroll pane because its background would be white.
        //Lay out the label and scroll pane from top to button.
        JPanel listPane = new JPanel();
        listPane.setLayout(new BoxLayout(listPane, BoxLayout.Y_AXIS));
        JLabel label = new JLabel(labelText);
        label.setLabelFor(list);
        listPane.add(label);
        listPane.add(Box.createRigidArea(new Dimension(0,5)));
        listPane.add(listScroller);
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
        String[] names = {"Arlo", "Cosmo", "Elmo", "Hugo",
                          "Jethro", "Laszlo", "Milo", "Nemo",
                          "Otto", "Ringo", "Rocco", "Rollo"};
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
        ListDialog.initialize(f, names, "Name Chooser",
                              "Baby names ending in O:");
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String selectedName = ListDialog.showDialog(null,
                                                            name.getText());
                name.setText(selectedName);
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
