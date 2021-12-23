package gvdecoder;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import gvdecoder.trace.*;
import java.util.*;
import java.awt.geom.GeneralPath;
//import java.awt.geom.GeneralPath;
import java.awt.Graphics2D.*;
import com.sun.image.codec.jpeg.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.geom.AffineTransform;
import java.net.URLConnection;

public class traceWindow extends JInternalFrame {
  public Vector traces;

  public int xdim;
  public int ydim;

  public drawwindow jp;
  public JPanel all;
  public JScrollPane jpSP;
  public JViewport prt;
  public JScrollBar vsb;
  public JScrollBar hsb;
  public int width;
  public int height;
  public AffineTransform fliptransform;
  public boolean flip=false;

  public traceWindow(int width, int height){
  	super("traceview",
  							 true, //resizable
  							 true, //closable
  							 true, //maximizable
  			  true);//iconifiable
  this.xdim=width;
  this.ydim=height;
  jp=new drawwindow(this);
  all=new JPanel();
  all.setLayout( new BorderLayout() );
  fliptransform = AffineTransform.getScaleInstance(1.,-1);
  SetupWindow();
}

  public void SetupWindow(){

     jp.setSize(new Dimension(xdim,ydim));
     jp.setPreferredSize(new Dimension(xdim, ydim));
     jpSP = new JScrollPane();
     prt=jpSP.getViewport();
     prt.add(jp);
     vsb = jpSP.getVerticalScrollBar();
     hsb = jpSP.getHorizontalScrollBar();



     all.add(jpSP,BorderLayout.CENTER);

     this.getContentPane().add(all,BorderLayout.CENTER);
     setSize(new Dimension(200,300));
     setLocation(10,10);
  }

 public void paintComponent(Graphics gold){
   super.paintComponent(gold);
   jp.paintComponent(gold);
}
  public void translateTraces(int start, int end){
	  for (int i=0;i<traces.size();i++){
		  singleTrace t1=(singleTrace)traces.elementAt(i);
		  t1.getGeneralPath(start,end,width,100);
		  if (flip) t1.gp.transform(fliptransform);
		   AffineTransform t=new AffineTransform();
		  	   t.setToTranslation(0,i*50);
		  t1.gp.transform(t);
   }

}


class drawwindow extends JPanel{

traceWindow tr;

  public drawwindow(traceWindow tr){

	 this.tr=tr;
 }

  public void paintComponent(Graphics gold){

  Graphics2D g=(Graphics2D)gold;
  if (tr.traces!=null){
	  for (int i=0;i<tr.traces.size();i++){
    singleTrace t1=(singleTrace)tr.traces.elementAt(i);
    g.setColor(t1.col);
    g.draw(t1.gp);

    }
 }



}

}
}