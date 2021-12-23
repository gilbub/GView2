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
public class ScrollControl extends JButton{
	Vector myListeners; /*when value changes, notify the listeners of this event*/
    Color background;
    Dimension preferredSize = new Dimension(75,20);
    double midValue;    //midpoint of scroll area
    double lowValue;    //low point of scroll area
    double highValue;   //high point of scroll area
    double maxValue; //user supplied max
    double minValue; //user supplied min
    int midDisplay;  //the midpoint of the bar in pixels
    int lowDisplay;  //the lowpoint of the bar in pixels
    int highDisplay; //the highpoint of the bar in pixels
    int displayVal;
    int dragStart;
    double lowStart;
    double highStart;
    boolean TOGGLENUMBERDISPLAY=false;
    DecimalFormat numberformater;
    Font font=new Font("SansSerif", Font.PLAIN, 10);
    FontMetrics metric;
    String Name;
    Color lowColor;
    Color highColor;
    double Increment;
    boolean shiftLow=false;
    boolean shiftHigh=false;
    boolean shiftBoth=false;
    public ScrollControl thiscontrol;
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
	 highDisplay=(int)(((highValue-minValue)/(maxValue-minValue))*getWidth());
	 lowDisplay =(int)(((lowValue-minValue)/(maxValue-minValue))*getWidth());
     g.setColor(highColor);
     g.fillRect(lowDisplay,0,highDisplay-lowDisplay,getHeight());
     String output=Name;
     midValue=(lowValue+highValue)/2.0;
     if (TOGGLENUMBERDISPLAY){
	   output=numberformater.format(midValue);
      }

	  int xloc=(getWidth())/2-metric.stringWidth(output)/2;
	  int yloc=getHeight()-6;

	  g.setColor(Color.white);
	  g.setFont(font);
	  g.drawString(output,xloc,yloc);

       }





    public boolean closeTo(int value, int target){
		return  Math.abs(target-value)<6;
	}

	public double getValue(int value){
	  return ((double)value/(double)getWidth())*(maxValue-minValue)+minValue;
	}


  public void addScrollListener(ScrollListener obj){
	  if (myListeners==null) myListeners=new Vector();
	  myListeners.add(obj);


}
	public ScrollControl(Color col,  //background color of control
	 				   double min, //minimum value
	 				   double max, //max value
	 				   double defaultLow, //initial (Default)
	 				   double defaultHigh,
	 				   boolean returnInt, //casts to integer (not implemented)
	 				   double increment,  //defined increment (if 0 then calculated)
	 				   String decimalformat,//###.## gives 2 decimal places etc
	 				   String name          //the button name
	 				   ){
	thiscontrol=this;
    background=col;
    maxValue=max;
    minValue=min;
    lowValue=defaultLow;
    highValue=defaultHigh;
    Name=name;
    Increment=increment;
    //generate a DecimalFormater
    numberformater=new DecimalFormat(decimalformat);
    //calculate position of bar.
    //displayVal=(int)((Value/((double)(max-min)))*getPreferredSize().width);
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
			  displayVal=e.getX();
			  dragStart=displayVal;//used later
			  lowStart=lowValue;
			  highStart=highValue;
			  shiftLow=false;
			  shiftHigh=false;
			  shiftBoth=false;
			  if (closeTo(displayVal,lowDisplay)) shiftLow=true;
			  else if (closeTo(displayVal,highDisplay)) shiftHigh=true;
			  else if ((displayVal>lowDisplay)&&(displayVal<highDisplay)) shiftBoth=true;
			 //if (e.getX()<displayVal) changeVal(-1);
			 //if (e.getX()>displayVal) changeVal(+1);
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

			if (shiftLow) {
				lowValue=getValue(displayVal);
			    if (lowValue>highValue-10) lowValue=highValue-10;
			    }
			else if (shiftHigh) {
				highValue=getValue(displayVal);
				if (highValue<lowValue+10) highValue=lowValue+10;
			}
			else if (shiftBoth){
				double tmp=getValue(displayVal-dragStart)-minValue;
				lowValue=lowStart+tmp;
				highValue=highStart+tmp;
			   		}
            if (myListeners!=null){

            for (int i=0;i<myListeners.size();i++){
				ScrollListener tmp=(ScrollListener)myListeners.get(i);
				tmp.scrollChange(thiscontrol);

			}
		    }
			//calculate value of Value
			//Value=((double)displayVal/(double)getWidth())*(maxValue-minValue)+minValue;

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
  ScrollControl dc = new ScrollControl(Color.red,-100,100,20,60,true,1,"###","framerate");
  f.getContentPane().add(dc,BorderLayout.NORTH);
  f.getContentPane().add(new ScrollControl(Color.lightGray,1000,2000,1200,1800, false, 10.0,"####.#","offset"), BorderLayout.SOUTH);
  f.pack();
  f.setVisible(true);


}

}