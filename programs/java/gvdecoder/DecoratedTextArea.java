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

public class DecoratedTextArea extends JTextArea implements KeyListener{
//String[] keywords;
//Color[] colors;
ArrayList<KeyWord> list=new ArrayList<KeyWord>();
String word=null;
Color color=null;
public boolean colorise=true;

 public void keyTyped(KeyEvent e) {
        System.out.println("KEY TYPED: ");
    }

    /** Handle the key-pressed event from the text field. */
    public void keyPressed(KeyEvent e) {
        //displayInfo(e, "KEY PRESSED: ");
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
   String[] strs=this.getText().split("\n");
   for (int i=0;i<strs.length;i++){
    g.drawString(strs[i],6,i*20);
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