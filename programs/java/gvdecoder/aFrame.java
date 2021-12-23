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

public class aFrame extends JPanel{
	public iFrame iframe;
	public oFrame oframe;
	public int frametype=-1;

	public JPanel panel;
	public String title;

	public static final int Internal_Frame=0;
	public static final int External_Frame=1;

	public GView gv;
	public boolean selected=false;

	public aFrame(JPanel panel, int frametype, String title, GView gv){
		this.frametype=frametype;
		this.panel=panel;
		this.title=title;
		this.gv=gv;

		if (frametype==Internal_Frame){
				iframe=new iFrame(this);

		}else
		if (frametype==External_Frame){
				oframe=new oFrame(this);
		}


     }

    public aFrame(String title){
		gv=GView.getGView();
		panel=new JPanel();
		frametype=Internal_Frame;
		this.title=title;
		iframe=new iFrame(this,false);
	}

	public void display(){
		if (frametype==Internal_Frame){
			iframe.display();
			panel=(JPanel)iframe.getContentPane();
		}
	}

    public Container getContentPane(){
	if (frametype==Internal_Frame){
		return iframe.getContentPane();
	}else
	if (frametype==External_Frame){
		return oframe.getContentPane();
	}
    return null;
	}

    public void setTitle(String title){
		this.title=title;
		if (frametype==Internal_Frame){
		  iframe.setTitle(title);
		}else
		if (frametype==External_Frame){
		 oframe.setTitle(title);
		}
	}

    public void setSelected(boolean selected){
		this.selected=selected;
		if (frametype==Internal_Frame){
		  try{
		  iframe.setSelected(selected);
	      }catch(java.beans.PropertyVetoException e){
			  e.printStackTrace();
		  }
		}


	}

	public void show(){
		if (frametype==Internal_Frame){
			iframe.display();
		}else
		if (frametype==External_Frame){

		}

	}

	public void unDock(){
		iframe.dispose();
		oframe=new oFrame(this);
		frametype=External_Frame;
	}

	public void Dock(){
		oframe.dispose();
		iframe=new iFrame(this);
		frametype=Internal_Frame;
	}


    public void checkUnDock(){
		getLocation();
		Dimension d=gv.mainWindow.getSize();
		if ((xpos<0)||(ypos<0)||(xpos>d.width)||(ypos>d.height)){
					unDock();
		}
	}

	public void iFrameMoved(){
     /*
       Dimension d=gv.mainWindow.getSize();
		System.out.println("iFrame x,y="+xpos+","+ypos);
		if ((xpos<0)||(ypos<0)||(xpos>d.width)||(ypos>d.height)){
			unDock();
		}
	 */
	}

	public void oFrameMoved(){
		System.out.println("oFrame x,y="+xpos+","+ypos);
		if (gv.mainWindow.getBounds().contains(location)){
			Dock();
		}

	}

    public Dimension getPreferredSize(){
		return panel.getPreferredSize();
	}

	public Dimension getSize(){
		if (frametype==Internal_Frame){
				return iframe.getSize();

		}else
		if (frametype==External_Frame){
				return oframe.getSize();
		}
		else return null;
	}

	public void setSize(Dimension d){
		panel.setSize(d);
	}

	public void setSize(int x, int y){
		panel.setSize(new Dimension(x,y));
	}

	public Point location;
	public int xpos=-1;
	public int ypos=-1;
	public Point getLocation(){
		  switch (frametype) {
					  case Internal_Frame:
					  					  location=iframe.getLocation();
					  					  xpos=location.x;
					  					  ypos=location.y;
					  					  break;
					  case External_Frame:
					                      location=oframe.getLocation();
					  					  xpos=location.x;
										  ypos=location.y;
					  					  break;
		  }
		  return location;
	}

	public void aFrameClosing( ) {
		System.out.println(" aframe closing");

    }

	public void aFrameClosed( ) {
		System.out.println(" aframe closed");

	}

	public void aFrameOpened( ) {
		System.out.println(" aframe opened");


	}

	public void aFrameIconified( ) {
		System.out.println(" aframe iconified");

	}

	public void aFrameDeiconified( ) {
             System.out.println(" aframe deiconified");

	}

	public void aFrameActivated( ) {
		System.out.println(" aframe activated");
	}

	public void aFrameDeactivated( ) {
		System.out.println(" aframe deactivated");


    }

    public  void aFrameHidden(){
		System.out.println(" aframe hidden");

		}
	public  void aFrameMoved(){
		getLocation();
		  switch (frametype) {
			  case Internal_Frame:
			                      iFrameMoved();
			  					  break;
			  case External_Frame:
			                      oFrameMoved();
			  					  break;
		  }
	}
	public void aFrameResized(){
		System.out.println(" aframe resized");

	}
	public void aFrameShown(){
		System.out.println(" aframe shown");

		}



	}


