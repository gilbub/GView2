package gvdecoder;
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
public Ruler presentruler;
public java.util.ArrayList rois;
public java.util.ArrayList rulers;
public float scale;
//static Filter myfilt=new Filter();
public Filter myfilt=new Filter();
boolean SHOWFILTERED=true;
public boolean SHOWROIS=true;
public boolean SHOWRULERS=true;
boolean SHOWLASTLINE=false;
boolean ARRAYUPDATED=true;
public boolean SAVEJPGS=false;
public boolean SAVEBMPS=false;
public BasicStroke stroke;
public BasicStroke dashed;

public Decoration[] decorations=null;

int filenum=0;

int lastX, lastY, presentX, presentY;

public void deleteAllROIs(){
 presentroi=null;
 rois=new ArrayList();
 repaint();
}

public void deleteAllRulers(){
 presentruler=null;
 rulers=new ArrayList();
 repaint();
}

public void createNewImage(int[] arr, int X_dim, int Y_dim, float viewScale){
 this.arr=arr;
 this.X_dim=X_dim;
 this.Y_dim=Y_dim;
 SetScale(viewScale);
 bi=new BufferedImage(X_dim,Y_dim,BufferedImage.TYPE_BYTE_GRAY);
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
 rulers=new ArrayList();
 stroke=new BasicStroke(2.0f);


 float dash[] = {10.0f};
 dashed=new BasicStroke(3.0f,BasicStroke.CAP_BUTT,BasicStroke.JOIN_ROUND, 10.0f, dash, 0.0f);


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

public int offsetx=0;
public int offsety=0;

public void paintComponent(Graphics g) {

        super.paintComponent(g);  //paint background

		Graphics2D g2=(Graphics2D)g;

		if (ARRAYUPDATED){
		 wr.setPixels(0,0,X_dim,Y_dim,arr);

	    }

		if (SHOWFILTERED){
           //BufferedImage bitemp= myfilt.go(bi);
		   g2.drawImage(myfilt.go(bi),null,offsetx,offsety);
		   //bitemp=null;
		  // myfilt.draw(bi);
	     if (SAVEJPGS) saveAsJPG(myfilt.go(bi));
	     if (SAVEBMPS) saveAsBMP(myfilt.go(bi));
		 }
		else g2.drawImage(bi,null,offsetx,offsety);
		if (SHOWROIS){
		try{
		g2.setColor(Color.red);
		g2.setStroke(dashed);
		if (presentroi!=null) g2.draw(presentroi.returnShape());
		for (int i=0;i<rois.size();i++){
		  ROI roi=(ROI)rois.get(i);
		  g2.setColor(roi.color);
		  g2.setStroke(stroke);
		  g2.draw(roi.returnShape());
	    }
		}catch(IllegalPathStateException pe){System.out.println("illegal path..");}
		}
		if (SHOWRULERS){
		try{
		g2.setColor(Color.yellow);
		g2.setStroke(dashed);
		if ((presentruler!=null)&&(presentruler.isDrawable())) g2.draw(presentruler.returnShape());
		if (rulers!=null){
		for (int i=0;i<rulers.size();i++){
		  Ruler ruler=(Ruler)rulers.get(i);
		  g2.setColor(ruler.color);
		  g2.setStroke(stroke);
		  if (ruler.isDrawable())
		   g2.draw(ruler.returnShape());
	    }
	    }
		}catch(IllegalPathStateException pe){System.out.println("illegal path..");}



		}
		if (SHOWLASTLINE){
		g2.setColor(Color.cyan);
		g2.drawLine(lastX,lastY,presentX,presentY);
		System.out.println("drawing last line");
		}
		/*
		if ((presentruler!=null)&&(presentruler.isDrawable())) {
			g2.setStroke(stroke);
			g2.draw(presentruler.returnShape());

		    if (presentruler.gp!=null) g2.draw(presentruler.returnTics()); //g2.draw(presentruler.gp);
		    if (presentruler.dataString!=null) g2.drawString(presentruler.dataString,10,10);
		    }
       */
	   if (decorations!=null){
		   drawOverlay(g2,decorations);
	   }
      ARRAYUPDATED=false;
	  g2.dispose();
	}


  //this is a utility function designed to be called from jython
  public void drawOverlay(Graphics2D g, Decoration[] decorations){
	 g.setStroke(stroke);
	 Font oldfont=g.getFont();
	 for (int i=0;i<decorations.length;i++){
		 Decoration dec=decorations[i];
		 int sx=(int)((float)dec.xloc*scale)+offsetx;
		 int yx=(int)((float)dec.yloc*scale)+offsety;

		 if (dec.str!=null){
			 g.setColor(dec.color);
			 if (dec.fontsize!=0) g.setFont(new Font(dec.fontname,Font.PLAIN, dec.fontsize));
			 g.drawString(dec.str,sx,yx);
		 }
		 if (dec.ruler!=null){
		   g.setColor(dec.color);
		   g.draw(dec.ruler.returnShape());
		 }
		 if (dec.shape!=null){
			 GeneralPath gp=new GeneralPath(dec.shape);
			 AffineTransform at=new AffineTransform();
			 at.translate(sx,yx);
             Shape shape =gp.createTransformedShape(at);

			 g.setColor(dec.color);
			 if (dec.fill)
			   g.fill(shape);
			  else
			   g.draw(shape);
		 }


	 }
    g.setFont(oldfont);
   }



}