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

public class oFrame extends JFrame implements WindowListener, ComponentListener{

  public aFrame aframe;

  public oFrame(aFrame aframe){
   super(aframe.title);
   this.aframe=aframe;
   this.getContentPane().add(aframe.panel,BorderLayout.CENTER);
   this.setSize(aframe.getSize());
   addWindowListener(this);
   addComponentListener(this);
   pack();
   show();
   }



   public void windowClosing(WindowEvent e) {
    aframe.aFrameClosing();
    this.dispose();
   }

   public void windowClosed(WindowEvent e) {
     aframe.aFrameClosed();
   }

   public void windowOpened(WindowEvent e) {
    aframe.aFrameOpened();
   }

   public void windowIconified(WindowEvent e) {
    aframe.aFrameIconified();
   }

   public void windowDeiconified(WindowEvent e) {
    aframe.aFrameDeiconified();

   }

   public void windowActivated(WindowEvent e) {
     aframe.aFrameActivated();
   }

   public void windowDeactivated(WindowEvent e) {
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


}