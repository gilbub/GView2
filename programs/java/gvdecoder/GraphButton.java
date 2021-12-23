package gvdecoder;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.text.*;
import java.awt.image.*;
import java.io.*;
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
public class GraphButton extends JButton implements HtmlSaver{
	Vector myListeners; /*when value changes, notify the listeners of this event*/

    Dimension preferredSize = new Dimension(150,50);
    public Rectangle bounds=new Rectangle(0,0,150,50);
    boolean TOGGLENUMBERDISPLAY=false;

    DecimalFormat numberformater;
    Font font=new Font("SansSerif", Font.PLAIN, 10);
    FontMetrics metric;
    String Name;
    double[] data;
    boolean ALLOWZOOM=false;
    Rectangle xydrawerbounds;


    int mouseStartval,mouseEndval;   // obtained from mouse clicks, relative to present view, in pixels
    int userstart,userend; // integer user start and end, scaled to x of data
    XYDrawer xydlast;
    XYDrawer xyd;

    public String saveHtml(){
			try{
			     String namestring=AnalysisHelper.getAnalysisHelper().saveImage(getImage());
			     File imagef=new File(namestring);
				 java.net.URL imageurl=imagef.toURI().toURL();

				 return "</pre><br><img src='"+imageurl+"'><br><pre>";
				 }catch(Exception e){e.printStackTrace();}
	     return "";

	}

		public void saveImage(){
	        AnalysisHelper.getAnalysisHelper().saveImage(getImage());

			//ImageUtils.WriteImage("analysis/trace.bmp",getImage(),"bmp");
		}

	    public BufferedImage getImage(){
			BufferedImage image = new BufferedImage(
			   (int)getWidth(),(int)getHeight(),BufferedImage.TYPE_BYTE_INDEXED);

			Graphics2D myG = (Graphics2D)image.getGraphics();

			paintComponent(myG);
			myG.dispose();
			return image;

		}




    public Dimension getPreferredSize(){

		 return preferredSize;
	 }

	public Dimension getMinimumSize(){

		 return preferredSize;
	 }

	public Dimension getMaximumSize(){
		return preferredSize;
	}


   public void setPreferredSize(Dimension d){
	   this.preferredSize=d;
	   this.bounds=new Rectangle(0,0,(int)d.getWidth(),(int)d.getHeight());
	   xydlast.RECALCSHAPE=true;
	   xyd.RECALCSHAPE=true;
   }

   public void setPreferredSize(int w, int h){
	   this.bounds=new Rectangle(0,0,w,h);
	   this.preferredSize=new Dimension(w,h);
	   xydlast.RECALCSHAPE=true;
	   xyd.RECALCSHAPE=true;
   }

   public void setData(double[] d){
	   xydlast=xyd;
	   xydlast.traceColor=Color.cyan;
	   xydlast.FILLBACKGROUND=false;
	   this.data=d;
	   xyd=new XYDrawer(data);
	   xyd.getShape(0,d.length,getXYDrawerBounds(bounds));
	   xyd.RECALCSHAPE=false;
	   userstart=0;
	   userend=data.length;
   }

   public String getTextString(){
	   if (xyd.HIGHLIGHT) return "dx="+(userend-userstart)+" y1="+data[userstart]+" y2="+data[userend];
	   else
	   return "l="+data.length+", ("+userstart+" < x < "+userend+"), ("+xyd.min+" < y < "+xyd.max+")";
   }

   public int getTextStringWidth(){
	   return metric.stringWidth(getTextString());
   }

    public void  paintComponent(Graphics g1){
	 //Graphics2D g=(Graphics2D) g1.create();
	 Graphics2D g=(Graphics2D) g1;
	 super.paintComponent(g);
	 Rectangle tmp=this.getBounds();
	 if (tmp.width<0) tmp=bounds;
	 xydrawerbounds=getXYDrawerBounds(tmp);

	 g.setFont(font);
	 g.setColor(Color.CYAN);
	 g.drawString(getTextString(),10,xydrawerbounds.height+20);

     if ((xydlast!=null)&&(xydlast.data.length==xyd.data.length)) {
		xydlast.FILLBACKGROUND=true;
		xydlast.paint(g,xydrawerbounds);
		xyd.FILLBACKGROUND=false;
		xyd.paint(g,xydrawerbounds);
	 }
	 else
	{
	 xyd.FILLBACKGROUND=true;
     xyd.paint(g,xydrawerbounds);
    }
   }

   public Rectangle getXYDrawerBounds(Rectangle bounds){
	  xydrawerbounds= new Rectangle(5,5,bounds.width-5,bounds.height-30);
	  return xydrawerbounds;
   }




	public GraphButton( double[] dat){
    super("t");
    userstart=0;
    userend=dat.length;

    this.data=dat;
    xyd=new XYDrawer(data);
    metric=getFontMetrics(font);

    //add mouse behavior
	addMouseListener(new MouseAdapter(){
			 public void mousePressed(MouseEvent e){
		       mouseStartval=e.getX();
		       if (mouseStartval<xydrawerbounds.x) mouseStartval=xydrawerbounds.x;
               xyd.RECALCSHAPE=true;
               if (xydlast!=null) xydlast.RECALCSHAPE=true;
               repaint();
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
					userstart=(int)(data.length*((double)mouseStartval-xydrawerbounds.x)/(double)xydrawerbounds.width);
					userend=data.length;
					}
				else{
				//calculate value of Value
				//			Value=((double)displayVal/(double)getWidth())*(maxValue-minValue)+minValue;
				   userstart=(int)(data.length*((double)mouseStartval-xydrawerbounds.x)/(double)xydrawerbounds.width);
				   userend=(int)(data.length*((double)mouseEndval-xydrawerbounds.x)/(double)xydrawerbounds.width);
				   }
			 	System.out.println("userstart="+userstart+" userend="+userend+" mouseStartval="+mouseStartval+" mouseEndval="+mouseEndval);
			  xyd.HIGHLIGHT=false;
			  xyd.setRange(userstart,userend);
			  if (xydlast!=null) xydlast.setRange(userstart,userend);
			  xyd.RECALCSHAPE=true;
			  if (xydlast!=null) xydlast.RECALCSHAPE=true;


			  repaint();
			 }

     });
    addMouseMotionListener(new MouseMotionAdapter(){
			public void mouseDragged(MouseEvent e){
            float w=(float)getWidth();
			//force displayVal to equal mouseposition
			mouseEndval=e.getX();
			//do bounds check
			if (mouseEndval<0) {mouseEndval=0;}
			else
			if (mouseEndval>getWidth()){mouseEndval=getWidth();}
            userstart=(int)(data.length*((double)mouseStartval-xydrawerbounds.x)/(double)xydrawerbounds.width);
		    userend=(int)(data.length*((double)mouseEndval-xydrawerbounds.x)/(double)xydrawerbounds.width);
			xyd.setHighlightRange(mouseStartval,mouseEndval);
            //xyd.RECALCSHAPE=true;
            //if (xydlast!=null) xydlast.RECALCSHAPE=true;
			repaint();
			}
	});

    addKeyListener(new KeyListener(){
		 public void keyPressed(KeyEvent e) {}


        public void keyReleased(KeyEvent e){

			if (e.isControlDown()&&(e.getKeyCode() == KeyEvent.VK_C)){
		     gvdecoder.ImageUtils.setClipboard(getImage());
		     AnalysisHelper.getAnalysisHelper().Notify("graph exported to clipboard");
		    }
		 }

        public void keyTyped(KeyEvent e){}

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
  GraphButton gr=new GraphButton(data);

  f.getContentPane().add(gr,BorderLayout.NORTH);
  //f.getContentPane().add(new GraphButton(Color.lightGray,1000,2000,1200, false, 10.0,"####.#","offset"), BorderLayout.SOUTH);
  f.pack();
  f.setVisible(true);


}

}