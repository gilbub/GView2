package gvdecoder.utilities;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import quicktime.*;
import quicktime.io.*;
import quicktime.qd.*;
import quicktime.std.*;
import quicktime.std.movies.*;
import quicktime.std.movies.media.*;
import quicktime.std.image.*;
import quicktime.util.*;
/**

 */
public class QuickTimeDialog extends JOptionPane implements StdQTConstants{
    public static QuickTimeDialog dialog;
    private static String value = "";
    public static QuickTimeDialogInfo aviinfo;
    private static JTextField start;
    private static JTextField end;

    private static int maxframes=1;
    private static JComboBox codecbox;
    private static JComboBox qualitybox;
    public static String[] codecs = {"Cinepak", "Animation", "H.263", "Sorenson", "Sorenson 3", "MPEG-4"};
	public static int[] codecTypes = {kCinepakCodecType, kAnimationCodecType, kH263CodecType, kSorensonCodecType, 0x53565133, 0x6d703476};

	public static String[] quality= {"Low", "Normal", "High", "Maximum"};
	public static int[] qualityTypes = {codecLowQuality, codecNormalQuality, codecHighQuality, codecMaxQuality};
    public static FileDialog qfc;//=new FileDialog(null,"save as");
    public static String filename="test.mov";

    /**
     * Set up the dialog.  The first argument can be null,
     * but it really should be a component in the dialog's
     * controlling frame.
     */
    public static void initialize(Component comp,
                                  String labelText,int max, FileDialog fc) {
		qfc=fc;
        Frame frame = JOptionPane.getFrameForComponent(comp);
		aviinfo=new QuickTimeDialogInfo();
		maxframes=max;
        dialog = new QuickTimeDialog(frame,  labelText);
    }

    /**
     * Show the initialized dialog.  The first argument should
     * be null if you want the dialog to come up in the center
     * of the screen.  Otherwise, the argument should be the
     * component on top of which the dialog should appear.
     */
    public static QuickTimeDialogInfo showDialog(Component comp, String initialValue) {
        if (dialog != null) {
            dialog.setValue(initialValue);
           // dialog.setLocationRelativeTo(comp);
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
	  aviinfo.name = filename;//(String)(list.getSelectedValue());
	  aviinfo.start = Integer.parseInt(start.getText());
	  aviinfo.end = Integer.parseInt(end.getText());
	  int i=codecbox.getSelectedIndex();
	  if (i>=0) aviinfo.codec=codecTypes[i];
	   else aviinfo.codec=codecTypes[4];
	  int j=qualitybox.getSelectedIndex();
	  if (j>=0) aviinfo.quality=qualityTypes[j];
       else aviinfo.quality=qualityTypes[1];
      if ((aviinfo.start>-1)&&(aviinfo.end>aviinfo.start)&&(aviinfo.end<maxframes)) {valid=true;
         aviinfo.valid=true;
	    }
      return valid;
	}

    public void cancel(){
	 aviinfo.valid=false;
	}

    private QuickTimeDialog(Frame frame,
                       String labelText) {
        //super(frame, labelText, true);
		final JButton filenamebutton=new JButton("filename");
		start=new JTextField("input start frame");
		end=new JTextField("input end frame");
		codecbox = new JComboBox();
		qualitybox=new JComboBox();

		for (int i=0; i< codecs.length; i++) codecbox.addItem( codecs[i] );
        for (int j=0; j< quality.length;j++) qualitybox.addItem(quality[j]);

        //buttons
        JButton cancelButton = new JButton("Cancel");
        final JButton setButton = new JButton("Set");
        filenamebutton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				 qfc.setVisible(true);
				 String file = qfc.getFile();
		         String directory= qfc.getDirectory();
		         filename=directory+file;
		         filenamebutton.setText(file);
			 }
			 });

        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
				cancel();
                QuickTimeDialog.dialog.setVisible(false);
            }
        });
        setButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
				if (setinfo())
                 QuickTimeDialog.dialog.setVisible(false);
            }
        });
        //getRootPane().setDefaultButton(setButton);

        JPanel listPane = new JPanel();
        listPane.setLayout(new BoxLayout(listPane, BoxLayout.Y_AXIS));


        listPane.add(filenamebutton);
        listPane.add(start);
         listPane.add(end);
        listPane.add(codecbox);
        listPane.add(qualitybox);
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
        //Container contentPane = getContentPane();
        this.add(listPane, BorderLayout.CENTER);
        this.add(buttonPane, BorderLayout.SOUTH);

        //pack();
    }

    /**
     * This is here so that you can view ListDialog even if you
     * haven't written the code to include it in a program.
     */

}
