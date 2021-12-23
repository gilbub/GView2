package gvdecoder;
import javax.swing.*;
import java.awt.*;
import ptolemy.plot.*;
import javax.swing.event.*;
import java.util.Vector;
import java.awt.event.*;
import java.io.*;

public class quickPlot extends JInternalFrame implements InternalFrameListener, ComponentListener, HtmlSaver{


public static JythonHelper2 jh=null;
public EditablePlot p;
public double start;
public double end;
public double inc;
public int dataset=1;
public JScrollPane scrollarea;
public JPanel grapharea;
public static int default_xdim=300;
public static int default_ydim=200;
public boolean default_setbuttons=true;
public boolean ismultiplot=false;

public String saveHtml(){
	//return AnalysisHelper.getAnalysisHelper().saveImage(p.getImage());
	 try{
	 String namestring= AnalysisHelper.getAnalysisHelper().saveImage(p.getImage());
     File imagef=new File(namestring);
     java.net.URL imageurl=imagef.toURI().toURL();
     return "</pre><img src='"+imageurl+"'><pre>";
     }catch(Exception e){e.printStackTrace();}
     return "";

}

/*component listener routines*/
 public void componentHidden(ComponentEvent e){;}
 public void componentMoved(ComponentEvent e){;}
 public void componentResized(ComponentEvent e){
      Dimension tmp=this.getSize();
      p.setSize(tmp.width-20,tmp.height-55);

	}
 public void componentShown(ComponentEvent e){;}


  /*internal frame events*/
  public void internalFrameClosing(InternalFrameEvent e) {

      System.out.println("Internal Frame closing");
      if (jh!=null){
         System.out.println("Sending close signal to jh");
		 jh.findPlot(p.getTitle());
		 jh.plots.remove(this);
		 jh.unsetSaveHtml(this);
	  }

	//displayMessage("Internal frame closing", e);
    }

    public void internalFrameClosed(InternalFrameEvent e) {
	 System.out.println("Internal Frame closed");

	 if (jh!=null){

	         System.out.println("Sending close signal to jh");
			 jh.findPlot(p.getTitle());
			 jh.plots.remove(this);
			 jh.unsetSaveHtml(this);
	  }
	//displayMessage("Internal frame closed", e);
	//listenedToWindow = null;
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

       if (jh!=null){

		   System.out.println("Setting selected plot");
		   jh.pl=this;
		    jh.setSaveHtml(this,"import graph");

	   }
	//displayMessage("Internal frame activated", e);
    }

    public void internalFrameDeactivated(InternalFrameEvent e) {
	//displayMessage("Internal frame deactivated", e);
    }

public void newPlot(double[] res, double start, double end, int logscale){
   p.clear(true);
   if (logscale!=0) p.setYLog(true);
   this.start=start;
   this.end=end;
   inc=(end-start)/res.length;
   dataset=1;
   addPlot(res);
   p.repaint();
}

public quickPlot(double[] res, double start, double end, int logscale){
	super("quick",
							 true, //resizable
							 true, //closable
							 true, //maximizable
			  true);//iconifiable
p=new EditablePlot();
p.setSize(default_xdim,default_ydim);
p.setButtons(default_setbuttons);
if (logscale!=0) p.setYLog(true);
this.start=start;
this.end=end;
inc=(end-start)/res.length;
init(res);
SetupWindow();
}

public quickPlot(boolean setbuttons, int default_xdim, int default_ydim){
  super("quick",
							 true, //resizable
							 true, //closable
							 true, //maximizable
			  true);//iconifiable
 //doesnt do anything but set defaults - for creating a nice multiplot
  this.default_setbuttons=setbuttons;
  this.default_xdim=default_xdim;
  this.default_ydim=default_ydim;
  setupEmptyWindow();

}


public quickPlot(double[] xs, double[] ys, boolean logscalex, boolean logscaley){
	super("quick",
								 true, //resizable
								 true, //closable
								 true, //maximizable
				  true);//iconifiable
	p=new EditablePlot();
	p.setSize(default_xdim,default_ydim);
	p.setButtons(default_setbuttons);
    p.setXLog(logscalex);
    p.setYLog(logscaley);
    addPlot(xs,ys);
    SetupWindow();

}


public quickPlot(double[]xs, double[] ys, double[] es){
	super("quick",
									 true, //resizable
									 true, //closable
									 true, //maximizable
					  true);//iconifiable
	p=new EditablePlot();
	p.setSize(default_xdim,default_ydim);
	p.setButtons(default_setbuttons);
    addPlot(xs,ys,es);
    SetupWindow();


}

public quickPlot(float[] res, double start, double end){
	super("quick",
						 true, //resizable
						 true, //closable
						 true, //maximizable
			  true);//iconifiable
p=new EditablePlot();
p.setSize(default_xdim,default_ydim);

p.setButtons(default_setbuttons);
this.start=start;
this.end=end;
inc=(end-start)/res.length;
init(res);
SetupWindow();
}


public void init(float[] res){
    formatPlot();
	boolean connected=true;
	if (jh!=null) connected=jh.plot_connected;


    boolean first=true;
	for (int i = 0; i < res.length; i++) {
	  p.addPoint(0, start+(double)i*inc,(double)res[i], !first);
	  if (connected) first = false;
	}

}

public void init(double[] res){
    formatPlot();
	boolean connected=true;
	if (jh!=null) connected=jh.plot_connected;


    boolean first=true;
	for (int i = 0; i < res.length; i++) {
	  p.addPoint(0, start+(double)i*inc,(double)res[i], !first);
	  if (connected) first = false;
	}
}

public int addPlot(double[] xs, double[] ys){
  return addPlot(p,xs,ys);
 /*formatPlot();
   boolean connected=true;
   if (jh!=null) connected=jh.plot_connected;
	boolean first=true;
	for (int i=0;i<xs.length;i++){
		p.addPoint(dataset,xs[i],ys[i],!first);
		if (connected) first=false;
	}
  dataset++;
  p.repaint();
  return dataset-1;
*/
}

public int addPlot(EditablePlot p_u, double[] xs, double[] ys){
   formatPlot(p_u);
   boolean connected=true;
   if (jh!=null) connected=jh.plot_connected;
	boolean first=true;
	for (int i=0;i<xs.length;i++){
		p_u.addPoint(dataset,xs[i],ys[i],!first);
		if (connected) first=false;
	}
  dataset++;
  p_u.repaint();
  return dataset-1;
}

public int addPlot(EditablePlot p_u, double[] xs, double[] ys, boolean logx, boolean logy){
   formatPlot(p_u);
   p_u.setYLog(logy);
   p_u.setXLog(logx);
   boolean connected=true;
   if (jh!=null) connected=jh.plot_connected;
	boolean first=true;
	for (int i=0;i<xs.length;i++){
		p_u.addPoint(dataset,xs[i],ys[i],!first);
		if (connected) first=false;
	}
  dataset++;
  p_u.repaint();
  return dataset-1;
}


public int addPlot(double[] xs, double[] ys, double[] err){
 formatPlot();

 boolean connected=true;
 if (jh!=null) connected=jh.plot_connected;
 boolean first=true;
	for (int i=0;i<xs.length;i++){
		p.addPointWithErrorBars(dataset,xs[i],ys[i],ys[i]-err[i],ys[i]+err[i],!first);
		if (connected) first=false;
	}
  dataset++;
  p.repaint();
  return dataset-1;

}



public void addPlot(double[] res){
	addPlot(p,res);
	/*
	formatPlot();
	boolean connected=true;
    if (jh!=null) connected=jh.plot_connected;
	boolean first=true;
		for (int i = 0; i < res.length; i++) {
		  p.addPoint(dataset, (double)i,(double)res[i], !first);
		  if (connected) first = false;
	}
	dataset++;
	p.repaint();
  */
}

public void addPlot(EditablePlot p_u, double[] res){
	formatPlot(p_u);
	boolean connected=true;
    if (jh!=null) connected=jh.plot_connected;
	boolean first=true;
		for (int i = 0; i < res.length; i++) {
		  p_u.addPoint(dataset, (double)i,(double)res[i], !first);
		  if (connected) first = false;
	}
	dataset++;
	p_u.repaint();
}


public void addPlot(float[] res){
	addPlot(p,res);
	/*formatPlot();
    boolean connected=true;
	if (jh!=null) connected=jh.plot_connected;

	boolean first=true;
		for (int i = 0; i < res.length; i++) {
		  p.addPoint(dataset, start+(double)i,(double)res[i], !first);
		  if (connected) first = false;
	}
	dataset++;
	p.repaint();
	*/
}


public void addPlot(EditablePlot p_u, float[] res){
	formatPlot(p_u);
    boolean connected=true;
	if (jh!=null) connected=jh.plot_connected;

	boolean first=true;
		for (int i = 0; i < res.length; i++) {
		  p_u.addPoint(dataset, start+(double)i,(double)res[i], !first);
		  if (connected) first = false;
	}
	dataset++;
	p_u.repaint();
}



public void newPlot(double[] res){
	 dataset=0;
	 p.clear(false);
	 addPlot(res);
}

public void newPlot(EditablePlot p_u, double[] res){
	 dataset=0;
	 p_u.clear(false);
	 addPlot(p_u, res);
}


public void newPlot( double[] xs, double[] ys){
	dataset=0;
	p.clear(true);
	addPlot(xs,ys);
}


public void newPlot(EditablePlot p_u, double[] xs, double[] ys){
	dataset=0;
	p_u.clear(true);
	addPlot(p_u,xs,ys);
}


public void newPlot(EditablePlot p_u, double[] xs, double[] ys, boolean logx, boolean logy){
	dataset=0;
	p_u.clear(true);
	addPlot(p_u,xs,ys,logx,logy);
}


public void newPlot(double[] xs, double[] ys, double[] err){
	dataset=0;
	p.clear(true);
	addPlot(xs,ys,err);
}


public void formatPlot(){
  if (jh!=null){
     p.setMarksStyle(jh.plot_marker);
	 p.setConnected(jh.plot_connected);
	 p.setTitle(jh.plot_title);
	 p.setXLabel(jh.plot_xlabel);
	 p.setYLabel(jh.plot_ylabel);
    }

}

public void formatPlot(EditablePlot pu){
return;
/*  if (jh!=null){
     pu.setMarksStyle(jh.plot_marker);
	 pu.setConnected(jh.plot_connected);
	 if ((pu.getTitle()==null)||(pu.getTitle()==""))pu.setTitle(jh.plot_title);
	 pu.setXLabel(jh.plot_xlabel);
	 pu.setYLabel(jh.plot_ylabel);
    }
*/
}

public void SetupWindow(){
 grapharea=new JPanel();
 grapharea.setLayout(new BoxLayout(grapharea, BoxLayout.Y_AXIS));


 JPanel jp=new JPanel();
 jp.setPreferredSize(new Dimension(default_xdim+10,default_ydim+10));
 jp.add(p);
 grapharea.add(jp);
 scrollarea=new JScrollPane(grapharea);
 this.getContentPane().add(scrollarea,BorderLayout.CENTER);
 setSize(new Dimension(default_xdim+50,default_ydim+60));
 setLocation(10,10);
 addInternalFrameListener(this);
 addComponentListener(this);
}


public void setupEmptyWindow(){
 grapharea=new JPanel();
 grapharea.setPreferredSize(new Dimension(default_xdim+10,default_ydim+10));
 grapharea.setLayout(new BoxLayout(grapharea, BoxLayout.Y_AXIS));
 scrollarea=new JScrollPane(grapharea);
 this.getContentPane().add(scrollarea,BorderLayout.CENTER);
 setSize(new Dimension(default_xdim+10,default_ydim+10));
 setLocation(10,10);
 addInternalFrameListener(this);

}

public Vector subplots;
public int presentlength=0;
public void addPlotWindow(double[] xs,double[] ys,boolean logscalex, boolean logscaley){
		if (subplots==null){
			subplots=new Vector();
			if (p!=null)
			 subplots.add(p);
		}
		p=new EditablePlot();
		p.setSize(default_xdim,default_ydim);
		subplots.add(p);
		presentlength+=default_ydim+10;
		p.setButtons(default_setbuttons);
	    p.setXLog(logscalex);
	    p.setYLog(logscaley);
        addPlot(xs,ys);
        JPanel jp=new JPanel();
        jp.setPreferredSize(new Dimension(default_xdim+10,default_ydim));
        jp.add(p);
        jp.setBorder(BorderFactory.createLineBorder(Color.black));

        grapharea.add(jp);
        grapharea.setPreferredSize(new Dimension(default_xdim+10,presentlength));
        repaint();



}




}