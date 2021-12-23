import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.awt.geom.*;
import java.util.*;
import java.awt.image.*;
import java.awt.image.renderable.*;
import javax.media.jai.*;

public class ImagePanel extends JPanel{

public BufferedImage bi;
WritableRaster wr;
int[] arr;
int X_dim;
int Y_dim;
public ROI presentroi;
Ruler presentruler;
public java.util.ArrayList rois;
float scale;
//static Filter myfilt=new Filter();
Filter myfilt=new Filter();
boolean SHOWFILTERED=true;
public boolean SHOWROIS=true;
boolean SHOWLASTLINE=false;
boolean ARRAYUPDATED=true;
public boolean SAVEJPGS=false;
public boolean SAVEBMPS=false;

int filenum=0;

int lastX, lastY, presentX, presentY;

public void deleteAllROIs(){
 presentroi=null;
 rois=new ArrayList();
 repaint();

}

public void createNewImage(int[] arr, int X_dim, int Y_dim, float viewScale){
 this.arr=arr;
 this.X_dim=X_dim;
 this.Y_dim=Y_dim;
 SetScale(viewScale);
 bi=new BufferedImage(X_dim+1,Y_dim+1,BufferedImage.TYPE_BYTE_GRAY);
 wr=bi.getRaster();

}

public Dimension getPreferredSize() {
	    //return the size of the scaled image
        return new Dimension((int)(X_dim*scale),(int)(Y_dim*scale));
    }


public void setInterpolation(int val){
	if (myfilt!=null){
		myfilt.setInterpolation(val);
	}
}


public void saveAsJPG(BufferedImage bi){
	String filestring="";
	if (filenum<10) filestring="000";
	else
	if (filenum<100) filestring="00";
	else
	if (filenum<1000) filestring="0";

	String filename="movie\\tmp"+filestring+filenum+".jpg";
	RenderedOp op=JAI.create("filestore",bi,filename,"JPEG");
	filenum++;
}


public void saveAsBMP(BufferedImage bi){
	String filestring="";
	if (filenum<10) filestring="000";
	else
	if (filenum<100) filestring="00";
	else
	if (filenum<1000) filestring="0";

	String filename="movie\\tmp"+filestring+filenum+".bmp";
	RenderedOp op=JAI.create("filestore",bi,filename,"BMP");
	filenum++;
}


public void SetScale(float viewScale){
 this.scale=viewScale;
 if (myfilt!=null) {
	  myfilt.scale=viewScale;
      myfilt.original_width=X_dim;
      System.out.println("set scale (debug)");
     }
    //this.setPreferredScrollableViewportSize(getPreferredSize());
    repaint();
}

public void showCursor(int x, int y){
	Graphics g=this.getGraphics();
	g.setColor(Color.yellow);
	g.drawRect((int)(x*scale),(int)(y*scale),(int)(scale),(int)(scale));
	g.dispose();

}

public ImagePanel(int[] arr, int X_dim, int Y_dim, float viewScale){

 createNewImage(arr,X_dim,Y_dim,viewScale);
 //myfilt=new Filter();
 myfilt.Initialize(bi);
 rois=new ArrayList();

}

public void set(int val){

 repaint();
}

public Image returnImage(){
	return (Image)bi;
}

public void forceRepaint(){
Graphics g=this.getGraphics();
paintComponent(g);
g.dispose();
}



public void paintComponent(Graphics g) {

        super.paintComponent(g);  //paint background

		Graphics2D g2=(Graphics2D)g;

		if (ARRAYUPDATED){
		 wr.setPixels(0,0,X_dim,Y_dim,arr);

	    }

		if (SHOWFILTERED){
         //bi=myfilt.go(bi);
		 g2.drawImage(myfilt.go(bi),null,0,0);
	     if (SAVEJPGS) saveAsJPG(myfilt.go(bi));
	     if (SAVEBMPS) saveAsBMP(myfilt.go(bi));
		 }
		else g2.drawImage(bi,null,0,0);
		if (SHOWROIS){
		try{
		g2.setColor(Color.red);
		float dash[] = {10.0f};

		         g2.setStroke(new BasicStroke(3.0f,
		             BasicStroke.CAP_BUTT,
		             BasicStroke.JOIN_ROUND,
		             10.0f, dash, 0.0f));


		if (presentroi!=null) g2.draw(presentroi.returnShape());

		for (int i=0;i<rois.size();i++){
		  ROI roi=(ROI)rois.get(i);
		  g2.setColor(roi.color);
		  g2.setStroke(new BasicStroke(3.0f));
		  g2.draw(roi.returnShape());

	    }
		}catch(IllegalPathStateException pe){System.out.println("illegal path..");}
		}
		if (SHOWLASTLINE){
		g2.setColor(Color.cyan);
		g2.drawLine(lastX,lastY,presentX,presentY);
		System.out.println("drawing last line");
		}
		if ((presentruler!=null)&&(presentruler.isDrawable())) g2.draw(presentruler.returnShape());
      ARRAYUPDATED=false;
	}
}