package gvdecoder;

import javax.swing.*;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.Shape;
import javax.swing.text.Document;
import javax.swing.text.BadLocationException;
import java.util.ArrayList;
import java.awt.event.*;

public class ColoredTextArea extends JTextArea implements KeyListener{
//String[] keywords;
//Color[] colors;
ArrayList<KeyWord> list=new ArrayList<KeyWord>();
String word=null;
Color color=null;
public int stagger=0;
public boolean colorise=true;
JythonViewer jv;


 public ColoredTextArea(JythonViewer vw){
	 this.jv=vw;
	 this.addKeyListener(this);

}

 public void keyTyped(KeyEvent e) {
        System.out.println("KEY TYPED: ");
    }

    /** Handle the key-pressed event from the text field. */
    public void keyPressed(KeyEvent e) {
		 int c = e.getKeyCode();
         if (((e.getModifiers() & KeyEvent.CTRL_MASK) != 0) && (c == KeyEvent.VK_F)){

			 jv.FindMode();
			 e.consume();
		  }

		 if (((e.getModifiers() & KeyEvent.CTRL_MASK) != 0) && (c == KeyEvent.VK_G)){

			 jv.GotoMode();
			 e.consume();
		  }
	}


    /** Handle the key-released event from the text field. */
    public void keyReleased(KeyEvent e) {
        //displayInfo(e, "KEY RELEASED: ");
    }

public void add(String word, Color c){
      	list.add(new KeyWord(word,c));
}
public void add(String word, int r, int g, int b){
	list.add(new KeyWord(word,new Color(r,g,b)));
}
public void clear(){
  list.clear();
}

public void paintComponent(java.awt.Graphics g){
  super.paintComponent(g);
   if (list.isEmpty()) return;
 	String content = null;
	    try {
	      Document d = this.getDocument();
	      content = d.getText(0, d.getLength()).toLowerCase();
	    } catch (BadLocationException e) {
	      // Cannot happen
	      return;
	    }
  for (KeyWord keyword:list){
	word=keyword.word;
	color=keyword.color;

    Rectangle position=null;
    int lastIndex = 0;
    int wordSize = word.length();
	while ((lastIndex = content.indexOf(word, lastIndex)) != -1) {
	      int endIndex = lastIndex + wordSize;
	      try {
	        position=this.modelToView(lastIndex);

	      } catch (BadLocationException e) {
	        // Nothing to do
           }
    g.setColor(color);
    FontMetrics fm = this.getFontMetrics(this.getFont());
    int baseline = position.y + position.height-fm.getDescent()+stagger;
    if (position!=null)
	 g.drawString(word,position.x,baseline);
     lastIndex=endIndex;
   }
 }
}
}


class KeyWord{
	String word;
	Color color;
	public KeyWord(String w, Color c){
		this.word=w;
		this.color=c;
	}
}