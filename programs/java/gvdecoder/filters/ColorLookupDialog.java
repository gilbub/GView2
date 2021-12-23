//package filters;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import ptolemy.plot.*;

/**
 * Use this modal dialog to let the user choose one string from a long
 * list.  See the main method for an example of using ColorLookupDialog.  The
 * basics:
 * <pre>
    String[] choices = {"A", "long", "array", "of", "strings"};
    ColorLookupDialog.initialize(componentInControllingFrame, choices,
                          "Dialog Title",
                          "A description of the list:");
    String selectedName = ColorLookupDialog.showDialog(locatorComponent,
                                                initialSelection);
 * </pre>
 */
public class ColorLookupDialog extends JDialog {
    private static ColorLookupDialog dialog;
    private static String value = "";
    private static int[][] lookup=new int[3][256];
	static EditablePlot p1,p2,p3;


    /**
     * Set up the dialog.  The first argument can be null,
     * but it really should be a component in the dialog's
     * controlling frame.
     */
    public static void initialize(Component comp,
                                  int[] data, int[] hist,
                                  String title,
                                  String labelText) {
        Frame frame = JOptionPane.getFrameForComponent(comp);
        dialog = new ColorLookupDialog(frame, data, hist,
                                title, labelText);
    }

    /**
     * Show the initialized dialog.  The first argument should
     * be null if you want the dialog to come up in the center
     * of the screen.  Otherwise, the argument should be the
     * component on top of which the dialog should appear.
     */
    public static int[][] showDialog(Component comp ) {
        if (dialog != null) {
            // dialog.setValue(initialValue);
            dialog.setLocationRelativeTo(comp);
            dialog.setVisible(true);
        } else {
            System.err.println("ColorLookupDialog requires you to call initialize "
                               + "before calling showDialog.");
        }
        return lookup;
    }


    private  static void setData(){
	double[][] tmp1=p1.getData(0);
	double[][] tmp2=p2.getData(0);
	double[][] tmp3=p3.getData(0);
	for (int i=0;i<lookup.length;i++){
	 lookup[0][i]=(int)tmp1[1][i];
	 if (lookup[0][i]<0) lookup[0][i]=0;
	 if (lookup[0][i]>255) lookup[0][i]=255;
	 }

	}

	private static void userSetData(int index){

	}


    private ColorLookupDialog(Frame frame, int[] data, int[] hist, String title,
                       String labelText) {
        super(frame, title, true);

        //buttons
        JButton cancelButton = new JButton("Cancel");
        final JButton setButton = new JButton("Set");
        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                ColorLookupDialog.dialog.setVisible(false);
            }
        });
        setButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                ColorLookupDialog.setData();
                ColorLookupDialog.dialog.setVisible(false);
            }
        });


        getRootPane().setDefaultButton(setButton);

		//copy data to lookup
        for (int j=0;j<3;j++){
		for (int i=0;i<data.length;i++) lookup[j][i]=data[i];
	    }
		p1 = new EditablePlot();
		p2 = new EditablePlot();
		p3 = new EditablePlot();

		//plot.addEditListener(this);
		p1.setEditable(0);
		p2.setEditable(0);
		p3.setEditable(0);



		        // Create the left plot by calling methods.
			    p1.setSize(350,75);
			    p2.setSize(350,75);
			    p3.setSize(350,75);
		        p1.setButtons(true);
				//p3.setXLabel("level");
				//p3.setYLabel("val");



		        boolean first = true;
		        for (int i = 0; i < lookup.length; i++) {

		            p1.addPoint(0, (double)i,(double)lookup[0][i], !first);
		            p1.addPoint(1, (double)i,(double)hist[i],!first);
		            p2.addPoint(0, (double)i,(double)lookup[0][i], !first);
		            p3.addPoint(0, (double)i,(double)lookup[0][i], !first);
		            first = false;

		        }

		        // Layout the two plots


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
        JPanel jp=new JPanel();
        jp.setLayout(new BoxLayout(jp, BoxLayout.Y_AXIS));
        jp.add(p1);
        jp.add(p2);
        jp.add(p3);
        contentPane.add(jp, BorderLayout.CENTER);
        contentPane.add(buttonPane, BorderLayout.SOUTH);

        pack();
    }

    /**
     * This is here so that you can view ColorLookupDialog even if you
     * haven't written the code to include it in a program.
     */
    public static void main(String[] args) {
        int[] dat = new int[256];
        int[] hst=new int[256];
		 int[] outdat;

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
        for (int i=0;i<dat.length;i++) dat[i]=i*10;

        JButton button = new JButton("Pick a new name...");
        ColorLookupDialog.initialize(f, dat, hst, "lookup", "lookup");
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int[][] tmpdat = ColorLookupDialog.showDialog(null);
                for (int j=0;j<tmpdat.length;j++) System.out.println("="+tmpdat[j]);
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
