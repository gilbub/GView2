package gvdecoder;
import javax.swing.*;

import java.awt.event.*;
import java.awt.*;
import java.util.*;
import gvdecoder.trace.*;
import prosilica.jni.*;
import javax.swing.event.*;


public class MyInternalFrame extends JInternalFrame implements InternalFrameListener, HtmlSaver{
    static int openFrameCount = 0;
    static final int xOffset = 30, yOffset = 30;
    public NavigatorGraph sg;


 public String saveHtml(){
 	 if (sg!=null) return sg.saveHtml();
     else return null;
 	}


   /*internal frame events*/
   public void internalFrameClosing(InternalFrameEvent e) {

       }



     public void internalFrameClosed(InternalFrameEvent e) {

 	   if (sg!=null){
		   sg.gv.jh.unsetSaveHtml(sg);
	   }
 	  }

     public void internalFrameOpened(InternalFrameEvent e) {
 	//displayMessage("Internal frame opened", e);
     }

     public void internalFrameIconified(InternalFrameEvent e) {
 	//displayMessage("Internal frame iconified", e);
     }

     public void internalFrameDeiconified(InternalFrameEvent e) {
 	//displayMessage("Internal frame deiconified", e);
     }

     public void internalFrameActivated(InternalFrameEvent e) {
 	System.out.println("internal frame activated");

        if (sg!=null){

 		   sg.gv.jh.setSaveHtml(sg,"import navigator");

 	   }
 	//displayMessage("Internal frame activated", e);
     }

     public void internalFrameDeactivated(InternalFrameEvent e) {
 	//displayMessage("Internal frame deactivated", e);
     }


 public MyInternalFrame(String absolutefilename, String windowname, GView gv,  ArrayList rois) {
        super(windowname,
              true, //resizable
              true, //closable
              true, //maximizable
              true);//iconifiable

        //...Create the GUI and put it in the window...
		NavigatorGraphParams np=NavigatorGraphParams.getInstance();
		this.setSize(new Dimension(np.InitialWidth,np.InitialHeight));
	    this.setLocation(np.InitialX,np.InitialY);
        sg=new NavigatorGraph(absolutefilename,gv,this,rois);

        System.out.println("using the new constructor");
		this.getContentPane().add(sg,BorderLayout.CENTER);
		addInternalFrameListener(this);
        //...Then set the window size or call pack...
        //setSize(sg.getPreferredSize());

        //Set the window's location.
        //setLocation(xOffset*openFrameCount, yOffset*openFrameCount);
    }



  public MyInternalFrame(String name, NavigatorGraph sg){
	  super(        name,
	                true, //resizable
	                true, //closable
	                true, //maximizable
	                true);//iconifiable
     this.getContentPane().add(sg,BorderLayout.CENTER);
	         //...Then set the window size or call pack...
	  NavigatorGraphParams np=NavigatorGraphParams.getInstance();
	  this.setSize(new Dimension(np.InitialWidth,np.InitialHeight));
	  this.setLocation(np.InitialX,np.InitialY);
      addInternalFrameListener(this);
	  //setSize(sg.getPreferredSize());
	         //Set the window's location.
      //setLocation(xOffset*openFrameCount, yOffset*openFrameCount);

  }

  public MyInternalFrame(String name,GView gv, Trace tr){
  sg=new NavigatorGraph(name,gv,tr,this);
  this.getContentPane().add(sg,BorderLayout.CENTER);
          //...Then set the window size or call pack...
         // setSize(sg.getPreferredSize());

          //Set the window's location.
         // setLocation(xOffset*openFrameCount, yOffset*openFrameCount);

        NavigatorGraphParams np=NavigatorGraphParams.getInstance();
		this.setSize(new Dimension(np.InitialWidth,np.InitialHeight));
	    this.setLocation(np.InitialX,np.InitialY);
	     addInternalFrameListener(this);

  }


    public MyInternalFrame(String absolutefilename, String windowname, Viewer2 vw,  ArrayList rois) {
        super(windowname,
              true, //resizable
              true, //closable
              true, //maximizable
              true);//iconifiable

        //...Create the GUI and put it in the window...

        sg=new NavigatorGraph(absolutefilename,vw,this,rois);


		this.getContentPane().add(sg,BorderLayout.CENTER);
        //...Then set the window size or call pack...
        //setSize(sg.getPreferredSize());

        //Set the window's location.
        //setLocation(xOffset*openFrameCount, yOffset*openFrameCount);
    	NavigatorGraphParams np=NavigatorGraphParams.getInstance();
		this.setSize(new Dimension(np.InitialWidth,np.InitialHeight));
	    this.setLocation(np.InitialX,np.InitialY);
        addInternalFrameListener(this);

    }


    public MyInternalFrame(int[][] roi_t, String windowname, Viewer2 vw,  ArrayList rois) {
	        super(windowname,
	              true, //resizable
	              true, //closable
	              true, //maximizable
	              true);//iconifiable

	        //...Create the GUI and put it in the window...

	        sg=new NavigatorGraph(roi_t,vw,this,rois);


			this.getContentPane().add(sg,BorderLayout.CENTER);
	        //...Then set the window size or call pack...
	        //setSize(sg.getPreferredSize());

	        //Set the window's location.
	        //setLocation(xOffset*openFrameCount, yOffset*openFrameCount);
	    	NavigatorGraphParams np=NavigatorGraphParams.getInstance();
			this.setSize(new Dimension(np.InitialWidth,np.InitialHeight));
		    this.setLocation(np.InitialX,np.InitialY);
	        addInternalFrameListener(this);

    }

    public MyInternalFrame(String windowname, JythonViewer pyview){

		 super(windowname,
		              true, //resizable
		              false, //closable
		              true, //maximizable
              true);//iconifiable

            this.getContentPane().add(pyview,BorderLayout.CENTER);
            setSize(pyview.getPreferredSize());
            setLocation(10,10);
	}


	public MyInternalFrame(String windowname,ProsilicaImagePanel propanel){

		 super(windowname,
				              true, //resizable
				              true, //closable
				              true, //maximizable
              true);//iconifiable
		 this.getContentPane().add(propanel,BorderLayout.CENTER);
		 setSize(1000,1000);
		 setLocation(140,13);
	}

 public NavigatorGraph getNavigator(){
  return sg;
 }

}
