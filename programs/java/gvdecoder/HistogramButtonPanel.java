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
public class HistogramButtonPanel extends JPanel{
	Vector myListeners; /*when value changes, notify the listeners of this event*/

    Dimension preferredSize = new Dimension(90,30);
    Font font=new Font("SansSerif", Font.PLAIN, 10);

    boolean TOGGLENUMBERDISPLAY=false;


    DecimalFormat numberformater;

    FontMetrics metric;
    String Name;
    double[] data;
    final Viewer2 vw;
    Rectangle BarGraphDrawerBounds;

    int mouseStartval,mouseEndval;   // obtained from mouse clicks, relative to present view, in pixels
    int userstart,userend; // integer user start and end, scaled to x of data
    BarGraphDrawer xyd;

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

    public void forceRepaint(){
     SwingUtilities.invokeLater(new Runnable(){
       public void run(){
		 genHistogram();
       	 repaint();
       	}

     });
    }

   public int updateperiod=100;
   javax.swing.Timer timer;
   public void startUpdate(){
   if (timer==null){
   timer = new javax.swing.Timer(updateperiod, new ActionListener() {
	    public void actionPerformed(ActionEvent evt) {
	      genHistogram();
	      repaint();
	    }
       });
     timer.start();
    }
    else timer.restart();

   }
   public void stopUpdate(){
	   timer.stop();
   }
   public void setUpdateDelay(int n){
	   if (timer!=null) timer.setDelay(n);
	   updateperiod=n;
   }


    public void  paintComponent(Graphics g1){
	 Graphics2D g=(Graphics2D) g1.create();
	 super.paintComponent(g);
	 xyd.RECALCSHAPE=true;
     xyd.paint(g,getBarGraphDrawerBounds(this.getBounds()));
     g.setFont(font);
	 g.setColor(Color.BLACK);
	 g.drawString(getTextString(),10,BarGraphDrawerBounds.height+20);

 	  }

    public Rectangle getBarGraphDrawerBounds(Rectangle bounds){
	  BarGraphDrawerBounds= new Rectangle(1,5,bounds.width-5,bounds.height-30);
	  return BarGraphDrawerBounds;
   }




    public void genHistogram(){
		vw.dataHistogram();
		scaleHistogram(0,64);
	}

	public void scaleHistogram(int s, int e){
		if ((s==0)&&(e==data.length)){
			vw.vwlut=null;
			xyd.HIGHLIGHT=false;
			vw.scale=((double)255)/((double)(vw.histdatamax-vw.histdatamin));
		    vw.offset=(int)vw.histdatamin;
		    }
		else{
		  int bins=vw.histdata.length;
		  double binsize=(vw.histdatamax-vw.histdatamin)/bins;
		  double min=vw.histdatamin+binsize*s;
		  double max=vw.histdatamin+binsize*e;
          vw.scale=((double)255)/((double)(max-min));
		  vw.offset=(int)min;
		}
		//vw.rescaleHistogram(s,e);
	    repaint();
	    vw.rescale();
	    vw.jp.ARRAYUPDATED=true;
	    vw.jp.repaint();

	}

   public String getTextString(){
	   return "min="+vw.histdatamin+" max="+vw.histdatamax;
   }

	public HistogramButtonPanel( Viewer2 vw, double[] dat){
    //super("t");
    userstart=0;
    userend=dat.length;
    this.vw=vw;
    this.data=dat;
    xyd=new BarGraphDrawer(data);

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
				   userstart=(int)(data.length*((double)mouseStartval)/(double)getWidth());
				   userend=(int)(data.length*((double)mouseEndval)/(double)getWidth());
				   }
			 	System.out.println("userstart="+userstart+" userend="+userend+" mouseStartval="+mouseStartval+" mouseEndval="+mouseEndval);

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