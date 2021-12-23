package gvdecoder;

import javax.swing.*;
import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;

public class FindField implements KeyListener, FocusListener{
 JythonViewer jv;
 JTextField textfield;
 JPanel panel;
 JLabel info;
 JLabel mode;
 String preamble= " cntrl-N/esc : ";
 boolean functions=false;
 public FindField(JythonViewer jv){
  this.panel=new JPanel();
  this.jv=jv;
  panel.setLayout(new BoxLayout(this.panel, BoxLayout.X_AXIS));
  this.textfield=new JTextField();
  this.textfield.addFocusListener(this);
  mode=new JLabel(" find: ");
  panel.add(mode);
  panel.add(this.textfield);
  panel.add(Box.createHorizontalGlue());
  textfield.setBackground(new Color(0,0,100));
  this.textfield.addKeyListener(this);
  this.info=new JLabel(" cntrl-n=next; esc=exit: ");
  this.info.setPreferredSize(new Dimension(150,40));
  this.info.setMinimumSize(new Dimension(150,40));
  panel.add(info);

  }

  public void setFindMode(){
	  functions=false;
	  mode.setText("  find: ");
	  info.setText(" cntrl-n=next; esc=exit: ");
	  textfield.setBackground(new Color(0,0,100));

}


public void setGotoMode(){
	 functions=true;
	 mode.setText("  goto: ");
	 info.setText(" enter=goto; esc=exit  ");
	 textfield.setBackground(new Color(0,100,0));

}
 public void focusGained(FocusEvent e) {


 }

 public void focusLost(FocusEvent e) {
	    System.out.println("focus lost");
	    if (jv.jythonWordFinder.iw!=null) jv.jythonWordFinder.iw.hide();

 }

 public void keyPressed(KeyEvent e){
		 int c = e.getKeyCode();

	     if (c==KeyEvent.VK_UP) {
			 jv.jythonWordFinder.functionLocationOffset(-1);
			 int loc=jv.jythonWordFinder.getInfoWindowPick();
	         jv.editor.setCaretPosition(loc);
			 e.consume();
			 return;
			 }
	     if (c==KeyEvent.VK_DOWN) {
			 jv.jythonWordFinder.functionLocationOffset(1);
			 int loc=jv.jythonWordFinder.getInfoWindowPick();
	         jv.editor.setCaretPosition(loc);
	         e.consume();
	         return ;}
         if (c==KeyEvent.VK_ESCAPE){
			 textfield.setText("");
			 if (functions) {
				 if (jv.jythonWordFinder.iw!=null) {
					 jv.jythonWordFinder.iw.hide();
					 jv.jythonWordFinder.iw.selected=0;
					 }
				}
			 ((CardLayout)(jv.cardPanel.getLayout())).show(jv.cardPanel,"input");
			 jv.textField.requestFocus();
		 	 e.consume();
		 	 return;
		 }


		 String tmp=textfield.getText();
		 if ( (c == KeyEvent.VK_ENTER)){
			  if (functions) {
			   if (jv.jythonWordFinder.iw!=null){
				  jv.jythonWordFinder.iw.hide();
				  if (jv.jythonWordFinder.functionLocations.size()>0){
	                int loc=jv.jythonWordFinder.getInfoWindowPick();
	                jv.editor.setCaretPosition(loc);
	                jv.editor.requestFocus();
	                jv.jythonWordFinder.iw.selected=0;
	                }
			      }
			    }
			   else jv.jythonWordFinder.find(tmp);
			textfield.setText("");
            ((CardLayout)(jv.cardPanel.getLayout())).show(jv.cardPanel,"input");
            jv.editor.requestFocus();
		 	e.consume();
		 	return;
	    }
	    if (((e.getModifiers() & KeyEvent.CTRL_MASK) != 0) && (c == KeyEvent.VK_N)){
		  if (functions){
			  if (jv.jythonWordFinder.functionLocations.size()>0){
			   jv.jythonWordFinder.iw.nextSelected();
			   int loc=jv.jythonWordFinder.getInfoWindowPick();
	           jv.editor.setCaretPosition(loc);
		       }
	          e.consume();
	          return;

		  }else

		  if (jv.jythonWordFinder.foundLocations.size()>0) {
						int val=jv.jythonWordFinder.currentWordIndex;
                        int loc=jv.jythonWordFinder.getNextLocation();
						jv.editor.setCaretPosition(loc);
						//System.out.println("jumping to position "+loc);
				 		 info.setText(preamble+" hit "+(val+1)+" of "+jv.jythonWordFinder.foundLocations.size());
				 		e.consume();
				 		return;
			}
	    }

	    if (tmp.length()>1){
			if (functions) jv.jythonWordFinder.score(tmp);
			else{
			jv.jythonWordFinder.find(tmp);
			int numfound=jv.jythonWordFinder.foundLocations.size();
	        info.setText(preamble+" found "+numfound);
	        if (numfound>0) jv.editor.setCaretPosition(jv.jythonWordFinder.foundLocations.get(0));

			}
		    }
	   }


	  public void keyTyped(KeyEvent e) {

	  }

	  public void keyReleased(KeyEvent e){
	  }

  }