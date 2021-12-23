package gvdecoder;
import javax.swing.*;

import java.awt.event.*;
import java.awt.*;
import java.util.*;
import gvdecoder.trace.*;



public class GeneralDialog extends JInternalFrame {

  public Object[] result;
  public boolean valid;
  //public JComboBox[] boxes;
  public GView gv;
  public static int j;
  public GeneralDialogChoices[] choices;
 public GeneralDialog(String windowname, String[] header, GeneralDialogChoices[] choices, GView gv) {
        super(windowname,
              false, //resizable
              true, //closable
              false, //maximizable
              false);//iconifiable
      this.choices=choices;
      this.gv=gv;
     
      int maxlength=0;
    
      //boxes=new JComboBox[choices.length];
      valid=false;
      JPanel panel=new JPanel();
     
      panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));

      if (header!=null){
		  for (int i=0;i<header.length;i++){
			  JLabel label=new JLabel(header[i]);
			  //label.setMinimumSize(d);
			  panel.add(label);
		  }
	  }
	  if (choices!=null){
		  for (j=0;j<choices.length;j++){
			  JPanel inner=new JPanel();
			  inner.setLayout(new BoxLayout(inner, BoxLayout.LINE_AXIS));
			  /*file dialog requests*/
			  if (choices[j].showFileDialog){
			    JButton filenamebutton=new JButton(choices[j].choices[0]);
			    
				inner.add(filenamebutton);
				choices[j].textfield=new JTextField();
				choices[j].textfield.setMinimumSize(d);
				choices[j].textfield.setText(choices[j].choices[0]);
			    inner.add(choices[j].textfield);
                inner.add(Box.createHorizontalGlue());
				filenamebutton.addActionListener(new ActionListener(){
				 public void actionPerformed(ActionEvent e){
			            String text=((JButton)e.getSource()).getText();
			            ShowFiledialog(text);
			           }
			  });
			  }else
			  /* textfield entry */
			  if (choices[j].choices.length==1){
			  JLabel l=new JLabel(choices[j].text);
			  
			  inner.add(new JLabel(choices[j].text));

			  choices[j].textfield=new JTextField();
			  choices[j].textfield.setMinimumSize(d);
			  choices[j].textfield.setText(choices[j].choices[0]);
		      inner.add(choices[j].textfield);
		      }
			   else
			  /* combo box*/
			  {
			  inner.add(new JLabel(choices[j].text));
			  JLabel l=new JLabel(choices[j].text);
			  
			  choices[j].combobox=new JComboBox();
			  choices[j].combobox.setMinimumSize(d);
			  for (int k=0; k< choices[j].choices.length; k++) choices[j].combobox.addItem( choices[j].choices[k]);
			  inner.add(choices[j].combobox);
		      }
			  if (choices[j].showFileDialog) {

		  }
         panel.add(inner);
	  }

	  }
	  
	  this.getContentPane().add(panel,BorderLayout.CENTER);
      this.pack();
      this.setVisible(true); //necessary as of kestrel
	  gv.desktop.add(this);
	  try {this.setSelected(true);} catch (java.beans.PropertyVetoException e){};
    }

public void ShowFiledialog(String text){
   gv.fc.setVisible(true);
   String file = gv.fc.getFile();
   String directory= gv.fc.getDirectory();
   String filename=directory+file;
   for (int i=0;i<choices.length;i++){
	   if (text.equals(choices[i].choices[0])){
         choices[i].textfield.setText(file);
         choices[i].chosenfile=filename;
        }
      }
}
public static void main(String[] args){
    /* test the class without invoking gview */
}
}
