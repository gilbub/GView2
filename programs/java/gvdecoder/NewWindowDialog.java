package gvdecoder;
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
  //  private static int[] lookup=new int[4][256]; //grey,red,green,blue
	private static LookupColorInfo look=new LookupColorInfo();
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
        dialog = new LookupDialog(frame, data, hist,title, labelText);
    }

    /**
     * Show the initialized dialog.  The first argument should
     * be null if you want the dialog to come up in the center
     * of the screen.  Otherwise, the argument should be the
     * component on top of which the dialog should appear.
     */
    public static LookupColorInfo showDialog(Component comp ) {
        if (dialog != null) {
            // dialog.setValue(initialValue);
            dialog.setLocationRelativeTo(comp);
            dialog.setVisible(true);
        } else {
            System.err.println("LookupDialog requires you to call initialize "
                               + "before calling showDialog.");
        }
        return look;
    }


    private  static void setData(){
	double[][] tmp=plot.getData(0);
	System.out.println("length of first-"+tmp.length+" length of second-"+tmp[1].length);
	for (int i=0;i<look.lookup.length;i++){
	 look.lookup[i]=(int)tmp[1][i];
	 if (look.lookup[i]<0) look.lookup[i]=0;
	 if (look.lookup[i]>255) look.lookup[i]=255;
	 }

	}

	private static void userSetData(int index){
	switch (index){
	  case 0:   plot.clear(0);
	            boolean first = true;
		        for (int i = 0; i < look.lookup.length; i++) {
		          plot.addPoint(0, (double)i,(double)look.lookup[i], !first);
		          first = false;
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
        DragControl redSet=new DragControl(Color.red,0,255,1,true,1,"###","red offset");
        redSet.addActionListener(new ActionListener() {
		            public void actionPerformed(ActionEvent e) {
		                LookupDialog.look.redOffset=(int)((DragControl)e.getSource()).getValue();

		            }
        });

       DragControl blueSet=new DragControl(Color.blue,0,255,0,true,1,"###","blue offset");
	           blueSet.addActionListener(new ActionListener() {
	   		            public void actionPerformed(ActionEvent e) {
	   		                LookupDialog.look.blueOffset=(int)((DragControl)e.getSource()).getValue();

	   		            }
        });

       DragControl greenSet=new DragControl(Color.green,0,255,0,true,1,"###","green offset");
	           greenSet.addActionListener(new ActionListener() {
	   		            public void actionPerformed(ActionEvent e) {
	   		                LookupDialog.look.greenOffset=(int)((DragControl)e.getSource()).getValue();

	   		            }
        });

        getRootPane().setDefaultButton(setButton);

		//copy data to lookup

		for (int i=0;i<data.length;i++) look.lookup[i]=data[i];

		plot = new EditablePlot();

		//plot.addEditListener(this);
		plot.setEditable(0);



		        // Create the left plot by calling methods.
			    plot.setSize(350,250);
		        plot.setButtons(true);
			   plot.setXLabel("level");
				plot.setYLabel("val");



		        boolean first = true;
		        for (int i = 0; i < look.lookup.length; i++) {

		            plot.addPoint(0, (double)i,(double)look.lookup[i], !first);
		            plot.addPoint(1, (double)i,(double)hist[i],!first);
		            first = false;

		        }

		        // Layout the two plots


        //Lay out the buttons from left to right.
        JPanel buttonPane = new JPanel();
        buttonPane.setLayout(new GridLayout(1,0));
       // buttonPane.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
       // buttonPane.add(Box.createHorizontalGlue());
        buttonPane.add(redSet);
		buttonPane.add(greenSet);
        buttonPane.add(blueSet);
        buttonPane.add(cancelButton);
       // buttonPane.add(Box.createRigidArea(new Dimension(10, 0)));
        buttonPane.add(setButton);


        //Put everything together, using the content pane's BorderLayout.
        Container contentPane = getContentPane();
        contentPane.add(plot, BorderLayout.CENTER);
        contentPane.add(buttonPane, BorderLayout.SOUTH);

        pack();
    }



}
