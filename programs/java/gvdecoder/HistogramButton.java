package gvdecoder;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.text.*;
import java.awt.geom.GeneralPath;
//import java.awt.geom.GeneralPath;
import java.awt.Graphics2D.*;
import java.awt.geom.AffineTransform;

/*
general purpose control, similar to dial. Mouse down on left decreases, mouse down
on right increases value.  Two color button displays value when mouseover, otherwise
displays name. The size of the color gives a visual cue to the value.
bar shifts to inde


*/
public class HistogramButton extends JButton{
	Vector myListeners; /*when value changes, notify the listeners of this event*/

    Dimension preferredSize = new Dimension(90,30);

    boolean TOGGLENUMBERDISPLAY=false;

    DecimalFormat numberformater;
    Font font=new Font("SansSerif", Font.PLAIN, 10);
    FontMetrics metric;
    String Name;
    double[] data;
    final Viewer2 vw;


    int mouseStartval,mouseEndval;   // obtained from mouse clicks, relative to present view, in pixels
    int userstart,userend; // integer user start and end, scaled to x of data
    XYDrawer xyd;

    public Dimension getPreferredSize(){
		 //return new Dimension(metric.stringWidth(Name),25);
		 return preferredSize;
	 }

	public Dimension getMinimumSize(){
		 //return new Dimension(metric.stringWidth(Name),25);
		 return preferredSize;
	 }

	public Dimension getMaximumSize(){
		return preferredSize;
	}



    public void  paintComponent(Graphics g1){
	 Graphics2D g=(Graphics2D) g1.create();
	 super.paintComponent(g);
	 xyd.RECALCSHAPE=true;
     xyd.paint(g,this.getBounds());
 	  }





    public void genHistogram(){
		vw.viewHistogram();
	}

	public void scaleHistogram(int s, int e){
		if ((s==0)&&(e==data.length)){vw.vwlut=null;xyd.HIGHLIGHT=false;}
		else
		vw.rescaleHistogram(s,e);
	    repaint();

	}


	public HistogramButton( Viewer2 vw, double[] dat){
    super("t");
    userstart=0;
    userend=dat.length;
    this.vw=vw;
    this.data=dat;
    xyd=new XYDrawer(data);

    //add mouse behavior
	addMouseListener(new MouseAdapter(){
			 public void mousePressed(MouseEvent e){
		       mouseStartval=e.getX();
		       genHistogram();

			  }
			 public void mouseEntered(MouseEvent e){
			 TOGGLENUMBERDISPLAY=true;
			  repaint();
			 }
			 public void mouseExited(MouseEvent e){
			 TOGGLENUMBERDISPLAY=false;
			   repaint();
			 }

			 public void mouseReleased(MouseEvent e){

			  if (mouseEndval<mouseStartval) {userstart=0;userend=data.length;}
				else
				if (mouseEndval>getWidth()){
					userstart=(int)(data.length*((double)mouseStartval)/(double)getWidth());
					userend=data.length;
					}
				else{
				//calculate value of Value
				//			Value=((double)displayVal/(double)getWidth())*(maxValue-minValue)+minValue;
				   userstart=(int)(data.length*((double)mouseStartval)/(double)getWidth());
				   userend=(int)(data.length*((double)mouseEndval)/(double)getWidth());
				   }
			 	System.out.println("userstart="+userstart+" userend="+userend+" mouseStartval="+mouseStartval+" mouseEndval="+mouseEndval);
			  //xyd.HIGHLIGHT=false;
			  //xyd.setRange(userstart,userend);


			   scaleHistogram(userstart,userend);

			 }

     });
    addMouseMotionListener(new MouseMotionAdapter(){
			public void mouseDragged(MouseEvent e){

			//force displayVal to equal mouseposition
			mouseEndval=e.getX();
			//do bounds check
			if (mouseEndval<0) {mouseEndval=0;}
			else
			if (mouseEndval>getWidth()){mouseEndval=getWidth();}
			xyd.setHighlightRange(mouseStartval,mouseEndval);
			repaint();
			}
	});




	}



}