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
public class GraphButton extends JButton{
	Vector myListeners; /*when value changes, notify the listeners of this event*/
    Color background;
    Dimension preferredSize = new Dimension(100,100);
    double Value;    //actual value contained in the component
    double maxValue; //user supplied max
    double minValue; //user supplied min
    int displayVal;  //the offset of the bar.
    boolean TOGGLENUMBERDISPLAY=false;
    boolean HIGHLIGHT=false;
    DecimalFormat numberformater;
    Font font=new Font("SansSerif", Font.PLAIN, 10);
    FontMetrics metric;
    String Name;
    Color lowColor;
    Color highColor;
    Color highlightColor;
    double Increment;
    double[] data;
    int arraylength;
    float step;
    AffineTransform at;
    float tracexscale,traceyscale;
    double max,min;
    int userstart,userend;

    int startval,endval; //these used to determine userstart and userend

    public Dimension getPreferredSize(){
		 //return new Dimension(metric.stringWidth(Name),25);
		 return preferredSize;
	 }

	public Dimension getMinimumSize(){
		 //return new Dimension(metric.stringWidth(Name),25);
		 return preferredSize;
	 }

    public void  paintComponent(Graphics g1){
	 Graphics2D g=(Graphics2D) g1.create();
	 super.paintComponent(g);

	 //Insets insets = getInsets();
	 //int currentWidth=getWidth()-insets.left-insets.right;
	 //int currentHeight=getHeight()-insets.top-insets.bottom;

	 g.setColor(lowColor);
	 g.fillRect(0,0,getWidth(),getHeight());
	 if ((HIGHLIGHT)&&(endval>startval)){
	  g.setColor(highlightColor);
	  g.fillRect(startval,0,endval-startval,getHeight());
     }
	 //calculate displayVal
	 //displayVal=(int)(((Value-minValue)/(maxValue-minValue))*getWidth());
     g.setColor(highColor);
     //g.fillRect(displayVal,0,getWidth(),getHeight());
     g.draw(getShape(userstart,userend,this.getBounds()));
     //g.drawString output=Name;
     /*
     if (TOGGLENUMBERDISPLAY){
	   output=numberformater.format(Value);
      }

	  int xloc=(getWidth())/2-metric.stringWidth(output)/2;
	  int yloc=getHeight()-6;

	  g.setColor(Color.white);
	  g.setFont(font);
	  g.drawString(output,xloc,yloc);

      //System.out.println("width = "+getWidth()+" string width = "+metric.stringWidth(output));
 	  */
 	  }

 public Shape getShape(int start, int end, Rectangle bounds){
   	arraylength=(int)(bounds.width*2.0f);
   	if (end-start<bounds.width*2) arraylength=end-start;
   	step=((float)(end-start))/arraylength;

   	GeneralPath gp=new GeneralPath(GeneralPath.WIND_EVEN_ODD,arraylength+1);

   	min=Double.POSITIVE_INFINITY;
   	max=Double.NEGATIVE_INFINITY;
   	double val=0;
   	gp.moveTo(0.0f,(float)data[0]);
   	int count=0;
    for (int i=start;i<end;i++){
		val=data[i];
		if (val>max) max=val;
   		if (val<min) min=val;
	}
   	for (float f=(float)start;f<end;f+=step){
   		val=data[(int)f];
   		gp.lineTo((float)count++,(float)max-(float)val);
   	}
   	at=new AffineTransform();
   	tracexscale=(float)((float)bounds.width/(float)count);
   	traceyscale=(float)((float)bounds.height/((float)(max-min)));
   	at.setToScale(tracexscale ,traceyscale);
   	//at.translate( bounds.x/tracexscale, bounds.y/traceyscale-min);
   	return at.createTransformedShape(gp);
   	//scale this to fit in bounds.
   }



	public void changeVal(int amount){
     /*
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
     */
	}

    public double getValue(){ return Value;}
	public GraphButton(Color backcol,  //background color of control
                       Color tracecol,
                       Color highcol,
	 				   double defaultval, //initial (Default)

	 				   String name ,        //the button name
	 				   double[] dat
	 				   ){
    background=backcol;
    userstart=0;
    userend=dat.length;
    Value=defaultval;
    Name=name;

    lowColor=backcol;
    highColor=tracecol;
    highlightColor=highcol;
    this.data=dat;

    //add mouse behavior
	addMouseListener(new MouseAdapter(){
			 public void mousePressed(MouseEvent e){
		       startval=e.getX();

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

			  if (endval<startval) {userstart=0;userend=data.length;}
				else
				if (endval>getWidth()){
					userstart=(int)(data.length*((double)startval)/(double)getWidth());
					userend=data.length;
					}
				else{
				//calculate value of Value
				//			Value=((double)displayVal/(double)getWidth())*(maxValue-minValue)+minValue;
				   userstart=(int)(data.length*((double)startval)/(double)getWidth());
				   userend=(int)(data.length*((double)endval)/(double)getWidth());
				   }
			 	System.out.println("userstart="+userstart+" userend="+userend+" startval="+startval+" endval="+endval);
			  HIGHLIGHT=false;
			  repaint();
			 }

     });
    addMouseMotionListener(new MouseMotionAdapter(){
			public void mouseDragged(MouseEvent e){
			HIGHLIGHT=true;
			//force displayVal to equal mouseposition
			endval=e.getX();
			//do bounds check
			if (endval<0) {endval=0;}
			else
			if (endval>getWidth()){endval=getWidth();}
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
  //DragControl dc = new DragControl(Color.red,-100,100,60,true,1,"###","framerate");
  double[] data=new double[100];
  for (int i=0;i<100;i++){data[i]=i%21;}
  GraphButton gr=new GraphButton(Color.black,Color.white,Color.magenta,10.0,"test",data);

  f.getContentPane().add(gr,BorderLayout.NORTH);
  //f.getContentPane().add(new GraphButton(Color.lightGray,1000,2000,1200, false, 10.0,"####.#","offset"), BorderLayout.SOUTH);
  f.pack();
  f.setVisible(true);


}

}