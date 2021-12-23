//package filters;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import ptolemy.plot.*;

/**
 * Use this modal dialog to let the user choose one string from a long
 * list.  See the main method for an example of using LookupDialog.  The
 * basics:
 * <pre>
    String[] choices = {"A", "long", "array", "of", "strings"};
    LookupDialog.initialize(componentInControllingFrame, choices,
                          "Dialog Title",
                          "A description of the list:");
    String selectedName = LookupDialog.showDialog(locatorComponent,
                                                initialSelection);
 * </pre>
 */
public class LookupDialog extends JDialog {
    private static LookupDialog dialog;
    private static String value = "";
    private static int[] lookup=new int[256];
	static EditablePlot plot;

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
        dialog = new LookupDialog(frame, data, hist
                                title, labelText);
    }

    /**
     * Show the initialized dialog.  The first argument should
     * be null if you want the dialog to come up in the center
     * of the screen.  Otherwise, the argument should be the
     * component on top of which the dialog should appear.
     */
    public static int[] showDialog(Component comp ) {
        if (dialog != null) {
            // dialog.setValue(initialValue);
            dialog.setLocationRelativeTo(comp);
            dialog.setVisible(true);
        } else {
            System.err.println("LookupDialog requires you to call initialize "
                               + "before calling showDialog.");
        }
        return lookup;
    }

    
    private  static void setData(){
	double[][] tmp=plot.getData(0);
	System.out.println("length of first-"+tmp.length+" length of second-"+tmp[1].length);
	for (int i=0;i<lookup.length;i++){
	 lookup[i]=(int)tmp[1][i];
	 if (lookup[i]<0) lookup[i]=0;
	 if (lookup[i]>255) lookup[i]=255;
	 }
	
	}
	
	private static void userSetData(int index){
	switch (index){
	  case 0:   plot.clear(0); 
	            boolean first = true;
		        for (int i = 0; i < lookup.length; i++) {    
		          plot.addPoint(0, (double)i,(double)lookup[i], !first);         
		          first = false
		        }
	          break;
	}
	}
	
	
    private LookupDialog(Frame frame, int[] data, int[] hist, String title,
                       String labelText) {
        super(frame, title, true);

        //buttons
        JButton cancelButton = new JButton("Cancel");
        final JButton setButton = new JButton("Set");
        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                LookupDialog.dialog.setVisible(false);
            }
        });
        setButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                LookupDialog.setData();
                LookupDialog.dialog.setVisible(false);
            }
        });
		
		
        getRootPane().setDefaultButton(setButton);
		
		//copy data to lookup
	 
		for (int i=0;i<data.length;i++) lookup[i]=data[i];
		
		plot = new EditablePlot();
		        
		//plot.addEditListener(this); 
		plot.setEditable(0);
				
		       
		
		        // Create the left plot by calling methods.
			    plot.setSize(350,250);
		        plot.setButtons(true);
				plot.setXLabel("level");
				plot.setYLabel("val");
				 
				
		       
		        boolean first = true;
		        for (int i = 0; i < lookup.length; i++) {
				    
		            plot.addPoint(0, (double)i,(double)lookup[i], !first);
		            plot.addPoint(1, (double)i,(double)hist[i],!first); 
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
        contentPane.add(plot, BorderLayout.CENTER);
        contentPane.add(buttonPane, BorderLayout.SOUTH);

        pack();
    }

    /**
     * This is here so that you can view LookupDialog even if you
     * haven't written the code to include it in a program. 
     */
    public static void main(String[] args) {
        int[] dat = new int[256];
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
        for (int i=0;i<dat.length;i++) dat[i]=i;
		
        JButton button = new JButton("Pick a new name...");
        LookupDialog.initialize(f, dat, "lookup",
                              "lookup");
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int[] tmpdat = LookupDialog.showDialog(null);
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
