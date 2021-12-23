package gvdecoder;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.text.*;

/*
general purpose control, similar to dial. Mouse down on left decreases, mouse down
on right increases value.  Two color button displays value when mouseover, otherwise
displays name. The size of the color gives a visual cue to the value.
bar shifts to inde


*/
public class DragControl extends JButton{
	Vector myListeners; /*when value changes, notify the listeners of this event*/
    Color background;
    Dimension preferredSize = new Dimension(75,20);
    double Value;    //actual value contained in the component
    double maxValue; //user supplied max
    double minValue; //user supplied min
    int displayVal;  //the offset of the bar.
    boolean TOGGLENUMBERDISPLAY=false;
    DecimalFormat numberformater;
    Font font=new Font("SansSerif", Font.PLAIN, 10);
    FontMetrics metric;
    String Name;
    Color lowColor;
    Color highColor;
    double Increment;

    public Dimension getPreferredSize(){
		 //return new Dimension(metric.stringWidth(Name),25);
		 return preferredSize;
	 }

	public Dimension getMinimumSize(){
		 //return new Dimension(metric.stringWidth(Name),25);
		 return preferredSize;
	 }

    public void  paintComponent(Graphics g){
	 super.paintComponent(g);
	 //Insets insets = getInsets();
	 //int currentWidth=getWidth()-insets.left-insets.right;
	 //int currentHeight=getHeight()-insets.top-insets.bottom;

	 g.setColor(lowColor);
	 g.fillRect(0,0,getWidth(),getHeight());
	 //calculate displayVal
	 displayVal=(int)(((Value-minValue)/(maxValue-minValue))*getWidth());
     g.setColor(highColor);
     g.fillRect(displayVal,0,getWidth(),getHeight());
     String output=Name;
     if (TOGGLENUMBERDISPLAY){
	   output=numberformater.format(Value);
      }

	  int xloc=(getWidth())/2-metric.stringWidth(output)/2;
	  int yloc=getHeight()-6;

	  g.setColor(Color.white);
	  g.setFont(font);
	  g.drawString(output,xloc,yloc);

      //System.out.println("width = "+getWidth()+" string width = "+metric.stringWidth(output));
 	  }




	public void changeVal(int amount){
     double scale=1;
     if (Increment==0){
     double range=maxValue-minValue;
	 scale=range/getWidth();
     }
     else scale=Increment;
	 Value+=scale*amount;
	 displayVal+=amount;
	 System.out.println("value changed = "+Value+" display = "+displayVal);
	 //fireEventChanged();
	repaint();

	}

    public double getValue(){ return Value;}
	public DragControl(Color col,  //background color of control
	 				   double min, //minimum value
	 				   double max, //max value
	 				   double defaultval, //initial (Default)
	 				   boolean returnInt, //casts to integer (not implemented)
	 				   double increment,  //defined increment (if 0 then calculated)
	 				   String decimalformat,//###.## gives 2 decimal places etc
	 				   String name          //the button name
	 				   ){
    background=col;
    maxValue=max;
    minValue=min;
    Value=defaultval;
    Name=name;
    Increment=increment;
    //generate a DecimalFormater
    numberformater=new DecimalFormat(decimalformat);
    //calculate position of bar.
    displayVal=(int)((Value/((double)(max-min)))*getPreferredSize().width);
	//generate border display.
	Border etchedBorder=BorderFactory.createEtchedBorder();
	setBorder(etchedBorder);
	//determine Font metrics
    metric=getFontMetrics(font);
    //set both colors
    lowColor=col;
    highColor=col.darker();

    //add mouse behavior
	addMouseListener(new MouseAdapter(){
			 public void mousePressed(MouseEvent e){
			 if (e.getX()<displayVal) changeVal(-1);
			 if (e.getX()>displayVal) changeVal(+1);
			  }
			 public void mouseEntered(MouseEvent e){
			 TOGGLENUMBERDISPLAY=true;
			  repaint();
			 }
			 public void mouseExited(MouseEvent e){
			 TOGGLENUMBERDISPLAY=false;
			   repaint();
			 }
    });
    addMouseMotionListener(new MouseMotionAdapter(){
			public void mouseDragged(MouseEvent e){
			//force displayVal to equal mouseposition
			displayVal=e.getX();
			//do bounds check
			if (displayVal<0) displayVal=0;
			if (displayVal>getWidth()) displayVal=getWidth();
			//calculate value of Value
			Value=((double)displayVal/(double)getWidth())*(maxValue-minValue)+minValue;

			repaint();
			}
	});




	}

public static void main(String[] args){
 JFrame f=new JFrame("ui test");
 f.addWindowListener(new WindowAdapter(){
	  public void windowClosing(WindowEvent e){
		  System.exit(0);
	  }
  });

  f.getContentPane().setLayout(new BorderLayout());
  DragControl dc = new DragControl(Color.red,-100,100,60,true,1,"###","framerate");
  f.getContentPane().add(dc,BorderLayout.NORTH);
  f.getContentPane().add(new DragControl(Color.lightGray,1000,2000,1200, false, 10.0,"####.#","offset"), BorderLayout.SOUTH);
  f.pack();
  f.setVisible(true);


}

}