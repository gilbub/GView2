package gvdecoder;


import javax.swing.JWindow;
import java.awt.Point;
import java.awt.Color;
import java.awt.Graphics;
import javax.swing.JLabel;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import java.awt.Font;
import javax.swing.SwingConstants;

public class InfoWindow{
JWindow window;
JythonViewer jv;
String[] lines;
int xl,yl, w, h;
int selected=0;
JLabel[] labels;

public InfoWindow(JythonViewer jv, String[] lines, int selected){
 this.jv=jv;
 this.lines=lines;
 labels=new JLabel[lines.length];
 this.selected=selected;
 window=new JWindow();
 JPanel panel=new JPanel();
 Font selectedFont=new Font("SansSerif",Font.BOLD,16);
 panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
 for (int i=0;i<lines.length;i++){
	 labels[i]=new JLabel(lines[i],SwingConstants.LEFT);
	 labels[i].setFont(selectedFont);
	 if (i==selected) {labels[i].setForeground(Color.WHITE);  }
	 else {labels[i].setForeground(Color.BLACK); }
	 panel.add(labels[i]); }
 window.add(panel);
 window.validate();
 relocate();
 window.setVisible(true);
 }

 public void relocate(){
  Point p=jv.findField.textfield.getLocationOnScreen();
  xl=p.x+5;
  w=jv.findField.textfield.getWidth()-10;
  h=lines.length*20+5;
  yl=p.y-h-10;
  window.setBounds(xl,yl,w,h);}

 public void hide(){
  window.setVisible(false);
  }

 public void show(){
   window.setVisible(true);
 }

 public void kill(){
  window.setVisible(false);
  window.dispose();}



 public void setSelected(int i){
  if ((i<0)||(i>=lines.length)) return;
  for (int j=0;j<lines.length;j++)labels[j].setForeground(Color.BLACK);
  selected=i;
  labels[selected].setForeground(Color.WHITE);

 }

 public void nextSelected(){
	 if (selected==lines.length-1) setSelected(0);
	 else setSelected(selected+1);
}

 public void setText(int i, String txt){
	 labels[i].setText(txt);
	 window.validate();
   }
}
