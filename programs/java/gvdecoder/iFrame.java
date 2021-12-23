package gvdecoder;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.awt.geom.GeneralPath;
import com.sun.image.codec.jpeg.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.net.URLConnection;

public class iFrame extends JInternalFrame implements InternalFrameListener, ComponentListener{

  public aFrame aframe;


  public iFrame(aFrame aframe){
   super(aframe.title, true,true,true,true);
   this.aframe=aframe;
  // this.setSize(aframe.getSize());
   this.getContentPane().add(aframe.panel,BorderLayout.CENTER);
   this.pack();
   aframe.gv.desktop.add(this);
   addInternalFrameListener(this);
   addComponentListener(this);
   show();
   }

 public iFrame(aFrame aframe,boolean packme){
   super(aframe.title, true,true,true,true);
   this.aframe=aframe;
  // this.setSize(aframe.getSize());
 //  this.getContentPane().add(aframe.panel,BorderLayout.CENTER);
 //  this.pack();
 //  aframe.gv.desktop.add(this);
 //  addInternalFrameListener(this);
 //  addComponentListener(this);
 //  show();
   }

   public void display(){
	   this.invalidate();
//	   this.getContentPane().add(aframe.panel,BorderLayout.CENTER);
	   this.pack();
	   this.setVisible(true);
	   aframe.gv.desktop.add(this);
	   addInternalFrameListener(this);
       addComponentListener(this);
	   show();
	}


   public void checkUnDock(){
	   aframe.checkUnDock();
   }

   public void internalFrameClosing(InternalFrameEvent e) {
    aframe.aFrameClosing();
   }

   public void internalFrameClosed(InternalFrameEvent e) {
     aframe.aFrameClosed();
   }

   public void internalFrameOpened(InternalFrameEvent e) {
    aframe.aFrameOpened();
   }

   public void internalFrameIconified(InternalFrameEvent e) {
    aframe.aFrameIconified();
   }

   public void internalFrameDeiconified(InternalFrameEvent e) {
    aframe.aFrameDeiconified();

   }

   public void internalFrameActivated(InternalFrameEvent e) {
     aframe.aFrameActivated();
   }

   public void internalFrameDeactivated(InternalFrameEvent e) {
     aframe.aFrameDeactivated();
   }


   public void componentHidden(ComponentEvent e){
	  aframe.aFrameHidden();

	}
	public void componentMoved(ComponentEvent e){
	  aframe.aFrameMoved();
	}
	public void componentResized(ComponentEvent e){
	  aframe.aFrameResized();
	}

	public void componentShown(ComponentEvent e){
	  aframe.aFrameShown();
	}

	public void mouseReleased(MouseEvent e){
		System.out.println("mouse released");
	}

}