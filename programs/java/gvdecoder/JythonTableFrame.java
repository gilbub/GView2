package gvdecoder;
import javax.swing.*;
import org.python.core.*;
import java.awt.event.*;
import java.awt.*;
import java.util.*;
import javax.swing.event.*;

public class JythonTableFrame extends JInternalFrame {
    static int openFrameCount = 0;
    static final int xOffset = 10, yOffset = 10;
    public JythonTable cc;
    GView ifd;

public void SetupWindow(){
	cc.jtf=this;
	this.getContentPane().add(cc,BorderLayout.CENTER);
	          //...Then set the window size or call pack...
	setSize(cc.table.getPreferredScrollableViewportSize());

	          //Set the window's location.
    setLocation(xOffset*openFrameCount, yOffset*openFrameCount);

    addInternalFrameListener(new InternalFrameAdapter() {
		            public void internalFrameIconified(WindowEvent e) {
		            }
		            public void internalFrameDeiconified(WindowEvent e) {
		            }
		            public void internalFrameClosing(WindowEvent e) {
		            }

                    public void internalFrameActivated(InternalFrameEvent e){
					 notifyActivated();
				    }

				    public void internalFrameClosed(InternalFrameEvent e) {
				     notifyClosed();

				    }
		        });
     }
  public JythonTableFrame(GView ifd, Viewer2 vw) {
          super("jython table - Viewer",
                true, //resizable
                true, //closable
                true, //maximizable
                true);//iconifiable

          //...Create the GUI and put it in the window...
		  this.ifd=ifd;
          cc=new JythonTable(ifd,vw);

          SetupWindow();

    }

  public JythonTableFrame(GView ifd, PyList list){
		super("jython table - list",
		              true, //resizable
		              true, //closable
		              true, //maximizable
		              true);//iconifiable

		        //...Create the GUI and put it in the window...
				this.ifd=ifd;
		        cc=new JythonTable(ifd,list);
                SetupWindow();
	}


    public JythonTableFrame(GView ifd, PyDictionary dict) {
        super("jython table - dictionary",
              true, //resizable
              true, //closable
              true, //maximizable
              true);//iconifiable

        //...Create the GUI and put it in the window...
		this.ifd=ifd;
        cc=new JythonTable(ifd,dict);
		SetupWindow();

    }

    public JythonTableFrame(GView ifd, Matrix ma) {
	        super("jython table - Matrix",
	              true, //resizable
	              true, //closable
	              true, //maximizable
	              true);//iconifiable

	        //...Create the GUI and put it in the window...
			this.ifd=ifd;
	        cc=new JythonTable(ifd,ma);
			SetupWindow();
    }


    public JythonTableFrame(GView ifd, double[][] dat) {
	        super("jython table - doubles",
	              true, //resizable
	              true, //closable
	              true, //maximizable
	              true);//iconifiable

	        //...Create the GUI and put it in the window...
			this.ifd=ifd;
	        cc=new JythonTable(ifd,dat);
			SetupWindow();
    }

   public JythonTableFrame(GView ifd, TimeSeries[] dat) {
  	        super("jython table - time series",
  	              true, //resizable
  	              true, //closable
  	              true, //maximizable
  	              true);//iconifiable

  	        //...Create the GUI and put it in the window...
  			this.ifd=ifd;
  	        cc=new JythonTable(ifd,dat);
  			SetupWindow();
      }


public void notifyActivated(){
	 System.out.println("TABLE ACTIVATED");
	 ifd.jh.setPresentTable(cc);
	 ifd.jh.setSaveHtml(cc,"import table");
	}

public void notifyClosed(){
	 System.out.println("TABLE CLOSED");
	ifd.jh.closePresentTable(cc);
	ifd.jh.unsetSaveHtml(cc);
}

 public JythonTable getJythonTable(){
  return cc;
 }

}
