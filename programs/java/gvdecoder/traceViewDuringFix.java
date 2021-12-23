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
import javax.swing.event.*;
import java.awt.event.*;

/**
* The <b>traceView</b> class shows multiple traces in one window. <p>
*It is presently set up
* to show a 16x16 grid of voltage and calcium data as a grid.
* Individual traces can contain timestamp information that can be set by
* the user (usually via a Jython script) as well as arbitrary labels.<p><p>
* <u>Usage:</u> The class is intended to be used with a Jython window open, allowing
* direct access to parameters. The class obtains traces from a Matrix (a 3D data array with accessor functions).
* Re-initalizing the matrix viewed by the traceView window will reset the traces. Similarly, performing filtering
* functions will reset the traces viewed.<p>
* There are two zoom functions, that operate in similar ways. The first allows zooming on <i>groups of traces</i>, effectively
* increasing the size of the trace on the screen. The second allows zooming on a <i>timewindow</i> within a trace, allowing
* a close-up view of details within a given trace.<p>
* Once the window is open and displaying traces, the user can zoom in on specific traces by dragging a
* box (top left to bottom right)
* with the mouse.  To zoom in on a group or single trace, the user must drag on the outside of all trace windows.
* To unzoom the user hits the "unzoom" button at the bottom of the window.
* To zoom in on a time window within a trace (ie if the trace window contains 10000 samples,
* and the user wants to zoom in on the first 1000), the user drags the mouse within a particular
* window - this resets the times for all the tracewindows. To unzoom the user drags in the opposite direction (within
* the individual trace window.)<p>
* The view can be seen as a single window with fixed width or a larger scrollable window with all the traces viewable.
* To use the large scroll window, zoom in on a set of traces, then hit the 'scroll' checkbox.
* <p>
* <u>Useful routines that can be called directly:</u><br>
*  <ul>
*  <li>{@link setAPStarts(int,int,float[])}</li>
*  <li>{@link setAPEnds(int,int,float[])}</li>
*  <li>{@link setLabels(int,int,float[],String[])}</li>
*  <li>{@link setLabels(int,int,float[],float[])}</li>
*  </ul>
*  <p>
* <u>Parameters that can be set directly:</u><br>
*  label_color (default Color.yellow), xy_color (default Color.cyan), xy_back_color (default Color.gray),
*  voltage_color( default Color.white), calcium_color(default Color.gray),  background_color (default Color.black),
*  ruler_color (default Color.red), select_color (default Color.yellow), border_color (default Color.orange),
*  apstart_color(default Color.blue), apend_color (default Color.red)
*
*
*
*/
public class traceView extends JInternalFrame implements InternalFrameListener, HtmlSaver{

int maxx,maxy;
public BufferedImage buffer;
public Graphics2D g2;

String imagefilename;

public int x,y,width,height;

public singleTrace[][] traces;
public singleTrace[][] traces2;

public JPanel jp;
public JPanel all;
public JScrollPane jpSP;
public JViewport prt;
public JScrollBar vsb;
public JScrollBar hsb;

public JythonHelper2 jh=null;

public Matrix m;
public Matrix m2;


public int xdim=500;
public int ydim=500;

public Color selectcol=Color.black;
public Rectangle selectrect=null;
public Rectangle ruler=null;
public boolean setstartend=false;
public boolean usingscrollpane=false;

public boolean rulermode=false;
public boolean cursormode=false;

JCheckBox showSecondary;
JCheckBox showCalcium;
JCheckBox showVoltage;
JCheckBox showIndex;
JCheckBox showRuler;
JCheckBox showCursor;
JCheckBox showScrollbars;

JButton unzoom;
JButton savepic;

public  Color label_color=Color.yellow;
public  Color xy_color=Color.cyan;
public  Color xy_back_color=Color.gray;
public  Color voltage_color=Color.white;
public  Color calcium_color=Color.gray;
public  Color background_color=Color.black;
public  Color ruler_color=Color.red;
public  Color select_color=Color.yellow;
public  Color border_color=Color.orange;
public  Color apstart_color=Color.blue;
public  Color apend_color=Color.red;
public  Color overlay_color=Color.yellow;

public GeneralPath globaloverlay=null;
public Vector GlobalTimes=null;
public Vector CursorTimes=null; //special case.

OptionListener optionListener;

/**
 The constructor takes a Matrix (a class containing a datArray3D plus access functions) and

 the width and height of the window.</p>
 The program must call {@link populate()} prior to displaying to initialize the data.
 */

public String saveHtml(){
	 try{
		 String namestring= AnalysisHelper.getAnalysisHelper().saveImage(getImage());
	     File imagef=new File(namestring);
	     java.net.URL imageurl=imagef.toURI().toURL();
	     return "</pre><table border='1' cellspacing='0'><tr><th colspan='2'><img src='"+imageurl+"'></th></tr><tr><td align='right'>start="+start_x+"</td><td align='right'>end="+end_x+"</td></tr></table><pre>";
	     }catch(Exception e){e.printStackTrace();}
	     return "";
	}


  /*internal frame events*/
  public void internalFrameClosing(InternalFrameEvent e) {
       if (jh!=null) jh.unsetSaveHtml(this);
      }



    public void internalFrameClosed(InternalFrameEvent e) {
	 if (jh!=null) jh.unsetSaveHtml(this);

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

		   jh.setSaveHtml(this,"import traceview");

	   }
	//displayMessage("Internal frame activated", e);
    }

    public void internalFrameDeactivated(InternalFrameEvent e) {
	//displayMessage("Internal frame deactivated", e);
    }

public traceView(Matrix m, int width, int height){
	super("traceview",
							 true, //resizable
							 true, //closable
							 true, //maximizable
			  true);//iconifiable
this.x=x;
this.y=y;
this.width=width;
this.height=height;
this.m=m;
xdim=width;
ydim=height;
jp=new drawsurface(this);
all=new JPanel();
all.setLayout( new BorderLayout() );
trListener trlistener=new trListener(this);
optionListener=new OptionListener(this);
keyboardListener keylistener=new keyboardListener(this);

jp.addMouseListener(trlistener);
jp.addMouseMotionListener(trlistener);
jp.addKeyListener(keylistener);
SetupWindow();
}



/**
Not functioning yet
**/
public void addSecondaryMatrix(Matrix m){
	this.m2=m;
    populate2();
}

public void paint(Graphics g_old){
	super.paint(g_old);
	//_repaint();

}

public void _repaint(Graphics g){
	//render();
    //System.out.println("called repaint(g)");
}

public void _repaint(){
   // System.out.println("called repaint()");
   	if (buffer!=null){

   	Graphics2D g=(Graphics2D)jp.getGraphics();

    g.drawImage(buffer,0,0,this);
	if (selectrect!=null){
		if (setstartend){
		  g.setColor(Color.orange);
		  g.draw(selectrect);

		}else{

		g.setColor(selectcol);
	    g.draw(selectrect);
	    }

     }

	g.dispose();
   }
}

public BufferedImage getImage(){
 return buffer;
}

public void saveImage(){
	AnalysisHelper.getAnalysisHelper().saveImage(getImage());
}

public void _paintComponent(Graphics g){
	super.paintComponent(g);

	//System.out.println("called paintcomponent(g)");

    repaint();
}

public void _update(){System.out.println("called update()");}

public void _update(Graphics g){System.out.println("called update(g)");}

public int maxxindex=16;
public int maxyindex=16;
public float xscale=1.0f;
public float yscale=1.0f;
public float topx=0.0f;
public float topy=0.0f;
public float botx=500.0f;
public float boty=500.0f;
public float scaledwidth=32.0f;
public float scaledheight=32.0f;
public void zoom(int tx, int ty, int bx, int by, int width, int height){



    topx=maxxindex*((float)tx/(float)xdim);
    topy=maxyindex*((float)ty/(float)ydim);
    botx=maxxindex*((float)bx/(float)xdim);
    boty=maxyindex*((float)by/(float)ydim);
    xscale=(float)xdim/(bx-tx);
    yscale=(float)ydim/(by-ty);
    scaledwidth=width*xscale;
    scaledheight=height*yscale;


if ( g2==null){
			buffer=new BufferedImage(xdim,ydim,BufferedImage.TYPE_INT_RGB);
			g2= buffer.createGraphics();
			System.out.println("created buffer");
	}


	g2.setColor(background_color);
	g2.fillRect(0,0,xdim,ydim);

	for (int i=0;i<16;i++){
		for (int j=0;j<maxyindex;j++){
			rendersingle(g2,j,i,topx,topy,(int)(scaledwidth),(int)(scaledheight));

		}
	}
    Graphics2D g=(Graphics2D)jp.getGraphics();
    g.drawImage(buffer,0,0,this);
	g.dispose();

}

/** Call this routine to clean up the screen if necissary
**/
public void rerender(){
if ( g2==null){
			buffer=new BufferedImage(xdim,ydim,BufferedImage.TYPE_INT_RGB);
			g2= buffer.createGraphics();
			System.out.println("created buffer");
	}

		g2.setColor(background_color);
		g2.fillRect(0,0,xdim,ydim);

		for (int i=0;i<16;i++){
			for (int j=0;j<maxyindex;j++){
				rendersingle(g2,j,i,topx,topy,(int)(scaledwidth),(int)(scaledheight));

			}
		}
	 //   Graphics2D g=(Graphics2D)jp.getGraphics();
	 //   g.drawImage(buffer,0,0,this);
	 // g.dispose();

	 repaint();

}


public void render(Graphics2D g2){
 		g2.setColor(background_color);
		g2.fillRect(0,0,xdim,ydim);

		for (int i=0;i<16;i++){
			for (int j=0;j<maxyindex;j++){
				rendersingle(g2,j,i,topx,topy,(int)(scaledwidth),(int)(scaledheight));

			}
		}
System.out.println("render(Graphics2D g2) was called.");

}

/** Use this routine to alow screen to real value mapping (its used internally)
*/
public float where_x(int x){
 x+=1;
 if (usingscrollpane){
	 float x_right_edge=(float)hsb.getValue()/(float)xdim;
	 float leftmostindex= x_right_edge*maxxindex;
     float x_left_edge=(float)(hsb.getValue()+prt.getWidth())/(float)xdim;

     float rightmostindex=x_left_edge*maxxindex;
    // return leftmostindex+((float)x/prt.getWidth())*(rightmostindex-leftmostindex);
    return ((float)x/xdim)*maxxindex;

    }

 float xpos=(float)x/(float)xdim;
 return topx+(botx-topx)*xpos;

}


public float where_y(int y){
if (usingscrollpane){
	 float y_right_edge=(float)vsb.getValue()/(float)ydim;
	 float leftmostindex= y_right_edge*maxyindex;
     float y_left_edge=(float)(vsb.getValue()+prt.getHeight())/(float)ydim;
     float rightmostindex=y_left_edge*maxyindex;
     //return leftmostindex+((float)y/prt.getHeight())*(rightmostindex-leftmostindex);
     return ((float)y/ydim)*maxyindex;

 }






 float ypos=(float)y/(float)ydim;
 return topy+(boty-topy)*ypos;

}


public int tx;
public int ty;
public int bx;
public int by;
public boolean showruler=false;

public float x_start_scaled;
public float x_end_scaled;
public float x_distance;
public void setruler(int tx,int ty, int bx, int by){
showruler=true;
this.tx=tx;
this.ty=ty;
this.bx=bx;
this.by=by;
//calculate time
float x_startloc=(where_x(tx)%1);
//System.out.println("x_startloc="+x_startloc);
float x_endloc=where_x(bx)%1;
//System.out.println("x_endloc="+x_endloc);
 x_start_scaled=start_x+(end_x-start_x)*x_startloc;
 x_end_scaled=start_x+(end_x-start_x)*x_endloc;

}

public void rulerrelease(){
	showruler=false;
}

public void cursorrelease(int x, int y){
	//float cx_start_scaled=start_x+(end_x-start_x)*(where_x(x)%1);
	CursorTimes=new Vector();
	CursorTimes.add(new timeStamp(x_end_scaled,0));
	showcursor=false;
	showruler=false;
    rerender();
}



public boolean showcursor=false;


public void setcursor(int ctx, int cty, int cbx, int cby){
  showcursor=true;

}

public double xt=0;
public double yt=0;
public boolean drawcoordinates=false;
public boolean voltage=true;
public boolean calcium=false;
public boolean secondary=false;
public int calciumoffset=16;
public void render(int xs, int ys, int xe, int ye, int width, int height){


	if ( g2==null){

			buffer=new BufferedImage(xdim,ydim,BufferedImage.TYPE_INT_RGB);
			g2= buffer.createGraphics();
			System.out.println("created buffer");
	}




	g2.setColor(background_color);
	g2.fillRect(0,0,xdim,ydim);

    if (traces!=null){
	 for (int i=xs;i<xe;i++){
		 for (int j=ys;j<ye;j++){
		    rendersingle(g2,j,i,(float)xs,(float)ys,width,height);
		 }
	 }

	}


 	Graphics2D g=(Graphics2D)jp.getGraphics();
	g.drawImage(buffer,0,0,this);
	g.dispose();
 }

public void setbeginend(float begin, float end){
	//System.out.println("1) begin="+begin+" end="+end);



	begin=begin-(int)begin;

	end=end-(int)end;

	//System.out.println("2) begin="+begin+" end="+end);



	if (end<begin) {

		start_x=0;
		end_x=m.zdim;
		rerender();
	    return;
	}
	int newstart_x=(int)(start_x+begin*(end_x-start_x));
	end_x=(int)(start_x+end*(end_x-start_x));
	start_x=newstart_x;

	//System.out.println("start_x="+start_x+" end_x="+end_x);
	rerender();
}


public void rendersingle(Graphics2D g2, int j, int i, float xs, float ys, int width, int height){
	g2.setColor(border_color);
	g2.drawRect((int)((i-xs)*width),(int)((j-ys)*height),width,height);
    if ((GlobalTimes!=null)&&(GlobalTimes.size()>0)){
		g2.setColor(overlay_color);
		g2.draw(getTimes(GlobalTimes,j,i,xs,ys,width,height));
	}
	if (cursormode){
		if (CursorTimes!=null){
		g2.setColor(Color.green);
		g2.draw(getTimes(CursorTimes  ,i,j,xs,ys,width,height));
	   }
	 }
    if (voltage) {
	 retranslate(j,i,xs,ys,width,height);
	 g2.setColor(voltage_color);
	 g2.draw(traces[i][j].gp);
	 }

	if (calcium) {
	 retranslate(i,j+calciumoffset,xs,ys+calciumoffset,width,height);
	 g2.setColor(calcium_color);
	 g2.draw(traces[i][j+calciumoffset].gp);
	 }
	if (secondary){
	 retranslate(m2,i,j,xs,ys,width,height);
	 g2.setColor(Color.red);
	 g2.draw(traces2[i][j].gp);
	}

    if (drawcoordinates){
	 g2.setColor(xy_back_color);
	 g2.fillRect((int)((i-xs)*width),(int)((j-ys)*height),20,8);
	 g2.setColor(xy_color);
	 g2.drawString((i+","+j),(i-xs)*width,(j-ys)*height+8);
    }



    if (traces[i][j].APStarts.size()>0){
		//System.out.println("trying to draw timestamp "+i+","+j);
		g2.setColor(apstart_color);
		g2.draw(getTimes(traces[i][j].APStarts, i,j,xs,ys,width,height));

   }

   if (traces[i][j].APEnds.size()>0){
		//System.out.println("trying to draw timestamp "+i+","+j);
		g2.setColor(apend_color);
		g2.draw(getTimes(traces[i][j].APEnds, i,j,xs,ys,width,height));

		}

   if (traces[i][j].labels!=null){
	   g2.setColor(label_color);
	  //(i-xs)*width,(j-ys)*height)
	  float w_scale=(float)width/(end_x-start_x);
	  for (int x=0;x<traces[i][j].labels.size();x++){
		   TraceLabel tl=(TraceLabel)traces[i][j].labels.elementAt(x);
		   if ((tl.time>start_x)&&(tl.time<end_x)){
		    int xloc=(int)((tl.time-start_x)*w_scale);
		    g2.drawString(tl.text,xloc+(i-xs)*width,(j-ys)*height+10);


		   }

	 }
   }
}

/**
* For all traces add timestamps located in <b>arr</b>.<br>
* Timestamps will be displayed as vertical lines in all windows.
* @param arr	array of floats containing the timestamps
*/
public void setGlobalTimes(float[] arr){
	GlobalTimes=new Vector();

	for (int x=0;x<arr.length;x++){

		GlobalTimes.add(new timeStamp(arr[x],0));
	}

}



/**
* For a trace index <b>(i,j)</b> add timestamps located in <b>arr</b>.<br>
* Timestamps will be displayed as vertical lines in the specified tracewindow.
* @param i		index (between 0 and 15)
* @param j      index (between 0 and 15)
* @param arr	array of floats containing the timestamps
*/
public void setAPStarts(int i, int j, float[] arr){
	traces[i][j].APStarts=new Vector();

	for (int x=0;x<arr.length;x++){

		traces[i][j].APStarts.add(new timeStamp(arr[x],0));
	}

}

/**
* For a trace index <b>(i,j)</b> add timestamps located in <b>arr</b>.<br>
* Timestamps will be displayed as vertical lines in the specified tracewindow.
* @param i		index (between 0 and 15)
* @param j      index (between 0 and 15)
* @param arr	array of floats containing the timestamps
*/
public void setAPEnds(int i, int j, float[] arr){
	traces[i][j].APEnds=new Vector();
	for (int x=0;x<arr.length;x++){

		traces[i][j].APEnds.add(new timeStamp(arr[x],0));
	}

}
/**
* For a trace index <b>(i,j)</b> add <b>labels</b> at location given by <b>times</b>.</br>
* <i>The user must ensure that the arrays holding labels and times are the same length.</i><br>
* The labels will be displayed at the top of the window in a color specified by
* the global parameter <b>label_color</b>.
* @param i		index (between 0 and 15)
* @param j      index (between 0 and 15)
* @param times	array of floats containing the timestamps
* @param labels array of floats that will be converted to strings to be used as labels
* @see #label_color
* @see #setLabels(int,int,float[],String[])
*/


public void setLabels(int i, int j, float[] times, float[] labels){
traces[i][j].labels=new Vector();
	for (int x=0;x<times.length;x++){
		traces[i][j].labels.add(new TraceLabel(times[x],(""+labels[x])));
	}

}



/**
* For a trace index <b>(i,j)</b> add <b>labels</b> at location given by <b>times</b>.</br>
* <i>The user must ensure that the arrays holding labels and times are the same length.</i><br>
* The labels will be displayed at the top of the window in a color specified by
* the global parameter <b>label_color</b>.
* @param i		index (between 0 and 15)
* @param j      index (between 0 and 15)
* @param times	array of floats containing the timestamps
* @param labels array of Strings containing the label names
* @see #label_color
* @see #setLabels(int,int,float[],float[])
*/

public void setLabels(int i, int j, float[] times, String[] labels){
	traces[i][j].labels=new Vector();
	for (int x=0;x<times.length;x++){
		traces[i][j].labels.add(new TraceLabel(times[x],labels[x]));
	}

}



public GeneralPath getTimes(Vector tstamps, int i, int j, float xs, float ys, int width, int height){
	GeneralPath gp=new GeneralPath(GeneralPath.WIND_EVEN_ODD);
	float h_scale=1.0f;
	float w_scale=(float)width/(end_x-start_x);
	for (int ii=0;ii<tstamps.size();ii++){

		timeStamp ts=(timeStamp)tstamps.elementAt(ii);
	    if ((ts.time>start_x)&&(ts.time<end_x)){
	    gp.moveTo(((float)((ts.time-start_x))*w_scale),0);
	    gp.lineTo(((float)((ts.time-start_x))*w_scale),height);
	   // System.out.println("in getTimes: plotting "+ts.time+" "+ts.time*w_scale+" start_x="+start_x+" end_x="+end_x+" width="+width);
	  }
	 }

	  AffineTransform t=new AffineTransform();
	  t.setToTranslation((i-xs)*width,(j-ys)*height);
	 // System.out.println("translated to "+(i-xs)*width+","+(j-ys)*height+" i="+i+" xs="+xs+" j="+j+" ys="+ys+" width="+width+" height="+height);
	  gp.transform(t);
	  return gp;
	}

public  GeneralPath getUntranslatedTimes(Vector tstamps){
    GeneralPath gp=new GeneralPath(GeneralPath.WIND_EVEN_ODD);
	float h_scale=1.0f;
	float w_scale=(float)width/(end_x-start_x);
	for (int ii=0;ii<tstamps.size();ii++){

		timeStamp ts=(timeStamp)tstamps.elementAt(ii);
	    if ((ts.time>start_x)&&(ts.time<end_x)){
	    gp.moveTo(((float)((ts.time-start_x))*w_scale),0);
	    gp.lineTo(((float)((ts.time-start_x))*w_scale),height);
	    //System.out.println("in getTimes: plotting "+ts.time*w_scale+" start_x="+start_x+" end_x="+end_x+" width="+width);
	  }
	 }
	return gp;
}

public void translate(GeneralPath gp, int i, int j, float xs, float ys, int width, int height){
	      AffineTransform t=new AffineTransform();
		  t.setToTranslation((i-xs)*width,(j-ys)*height);
		  gp.transform(t);

}



public int start_x=0;
public int end_x=2000;
//i, j are the indices, xs & ys are the location of the topx and topy of the window in reals.
//((i-xs)*width, (j-ys)*height) are the top corner of the window
//start_x and end_x are set by the zoom function
public void retranslate(int i, int j, float xs, float ys,  int width, int height){
	//for (int i=xs; i<xe; i++){
	//	for (int j=ys;j<ye;j++){
		 singleTrace t=traces[i][j];
		 t.at=new AffineTransform();
		 t.at.setToTranslation((i-xs)*width,(j-ys)*height);
		 //t.at.scale(width, height);
		 t.gp=m.getGeneralPath(i,j,start_x,end_x,width,height);
		 t.gp.transform(t.at);
		 traces[i][j]=t;
	//	}
	//}

}

public void retranslate(Matrix m2, int i, int j, float xs, float ys,  int width, int height){
	//for (int i=xs; i<xe; i++){
	//	for (int j=ys;j<ye;j++){
		 singleTrace t=traces2[i][j];
		 t.at=new AffineTransform();
		 t.at.setToTranslation((i-xs)*width,(j-ys)*height);
		 //t.at.scale(width, height);
		 t.gp=m2.getGeneralPath(i,j,start_x,end_x,width,height);
		 t.gp.transform(t.at);
		 traces2[i][j]=t;
	//	}
	//}

}


public int ypixels=16;
public int ypixeloffset=0;
public boolean is_pda=false; //distinguish between ompro and argus filetype
public void populate(){


	if (m.ydim==34) {
		ypixels=34;
		ypixeloffset=1;
	}
	if (m.ydim==32){
		ypixels=32;
		ypixeloffset=0;
	}
	if (m.ydim==17){
		ypixels=17;
		maxyindex=17;
		ypixeloffset=0;
	}
	if (m.ydim==16){
		ypixels=16;
		maxyindex=17;
		ypixeloffset=0;
	}

    if (is_pda)
     maxyindex=16;

	calciumoffset=16+ypixeloffset;

	traces=new singleTrace[ypixels][16];
	for (int x=0;x<16;x++){
		for (int y =0;y<ypixels;y++){
			singleTrace tr=new singleTrace();
			tr.col=Color.white;
			tr.APStarts=new Vector();
			tr.APEnds=new Vector();
			tr.x=x;
			tr.y=y;
			tr.at=new AffineTransform();
			tr.at.setToTranslation(x*width,y*height);
			tr.gp=m.getGeneralPath(y,x,0,end_x,width,height);
			tr.gp.transform(tr.at);
			traces[y][x]=tr;
		}
    }

//traces[10][10].APStarts.add(new timeStamp(2000,1000));
}

public boolean on_tap_plottrace=false;
public void traceToNavigator(int x, int y){
	if (m.vw!=null){
		m.vw.addTrace((int)where_y(y), (int)where_x(x));
	}

}

public void populate2(){
	traces2=new singleTrace[16][16];
	for (int x=0;x<16;x++){
		for (int y=0;y<16;y++){
			singleTrace tr=new singleTrace();
			tr.col=Color.white;
			tr.x=x;
			tr.y=y;
			tr.at=new AffineTransform();
			tr.at.setToTranslation(x*width,y*height);
			tr.gp=m2.getGeneralPath(x,y,0,800,width,height);
			tr.gp.transform(tr.at);
			traces2[x][y]=tr;

		}
    }
}




public void SetupWindow(){

   showCalcium = new JCheckBox("Ca");
   showCalcium.addActionListener(optionListener);
   showCalcium.setSelected(false);

   showVoltage = new JCheckBox("Vm");
   showVoltage.addActionListener(optionListener);
   showVoltage.setSelected(true);

   showIndex = new JCheckBox("x,y");
   showIndex.addActionListener(optionListener);
   showIndex.setSelected(false);

   showSecondary=new JCheckBox("f(x)");
   showSecondary.addActionListener(optionListener);
   showSecondary.setSelected(false);

   showScrollbars = new JCheckBox("scroll");
   showScrollbars.addActionListener(optionListener);
   showScrollbars.setSelected(false);

   showRuler = new JCheckBox("R");
   showRuler.addActionListener(optionListener);
   showRuler.setSelected(false);

   showCursor = new JCheckBox("C");
   showCursor.addActionListener(optionListener);
   showCursor.setSelected(false);


   unzoom = new JButton("unzoom");
   unzoom.addActionListener(optionListener);

   savepic=new JButton("save");
   savepic.addActionListener(optionListener);


   JPanel bottom_controls=new JPanel();
   bottom_controls.add(showVoltage);
   bottom_controls.add(showCalcium);
   bottom_controls.add(showSecondary);
   bottom_controls.add(showIndex);
   JPanel side_controls=new JPanel();
   side_controls.setLayout(new BoxLayout(side_controls, BoxLayout.LINE_AXIS));
   side_controls.add(showRuler);
   side_controls.add(showCursor);
   side_controls.add(showScrollbars);
   side_controls.add(unzoom);
   side_controls.add(savepic);
   jp.setSize(new Dimension(xdim,ydim));
   jp.setPreferredSize(new Dimension(xdim, ydim));
   jpSP = new JScrollPane();
   prt=jpSP.getViewport();
   prt.add(jp);
   vsb = jpSP.getVerticalScrollBar();
   hsb = jpSP.getHorizontalScrollBar();


   JPanel controls=new JPanel();
   controls.setLayout(new BoxLayout(controls,BoxLayout.PAGE_AXIS));
   controls.add(bottom_controls);
   controls.add(side_controls);
   all.add(jpSP,BorderLayout.CENTER);
   all.add(controls,BorderLayout.SOUTH);
   //all.add(side_controls,BorderLayout.WEST);
   this.getContentPane().add(all,BorderLayout.CENTER);
   setSize(new Dimension(xdim+25,ydim+25));
   addInternalFrameListener(this);
   setLocation(300,10);

}

public void setExpanded(boolean val){
	//System.out.println("called setExpanded "+val);



	if (!val){
		setSmall();

	}
	if (val){

		setLarge();
	}

}

public void setSmall(){
	xdim= width;
	ydim= height;
    jp.setPreferredSize(new Dimension(xdim, ydim));
	g2=null;
	zoom(0,0,xdim,ydim,(int)((float)xdim/(float)maxxindex),(int)((float)ydim/(float)maxyindex));
    usingscrollpane=false;
    hsb.setVisible(false);
    vsb.setVisible(false);
    repaint();
}


public void unzoom(){
	zoom(0,0,xdim,ydim,(int)(((float)xdim/(float)maxxindex)),(int)(((float)ydim/(float)maxyindex)) );
}

public void setLarge(){
	xdim= (int)(scaledwidth*maxxindex);
	ydim= (int)(scaledheight*maxyindex);
	jp.setPreferredSize(new Dimension(xdim, ydim));
    g2=null;
    zoom(0,0,xdim,ydim,(int)scaledwidth,(int)scaledheight);
    usingscrollpane=true;
    hsb.setVisible(true);
    vsb.setVisible(true);
    repaint();
}
/*
    public void keyTyped(KeyEvent e) {
	//displayInfo(e, "KEY TYPED: ");
    }

    public void keyPressed(KeyEvent e) {
	//displayInfo(e, "KEY PRESSED: ");
    }

    public void keyReleased(KeyEvent e) {
	System.out.println("KEY RELEASED: ");

    if (e.isControlDown()&&(e.getKeyCode() == KeyEvent.VK_C))
     saveImage();

    }
*/
}

class TraceLabel{
	public String text;
	public double time;
	public int xpos;
	public int ypos;
	public TraceLabel(double time, String text){this.time=time; this.text=text;}
}


class timeStamp{
	public double time;
	public double xloc;
	public timeStamp(double time, double xloc){this.time=time; this.xloc=xloc;}

}


/*
class singleTrace{

	public Color col;
	public int x;
	public int y;
	public AffineTransform at;
	public GeneralPath gp;
	public Vector APStarts;
	public Vector APEnds;
	public Vector labels;


}

*/
class OptionListener implements ActionListener {
	traceView vi;


	public OptionListener(traceView vi){

		this.vi=vi;
	}
	public void actionPerformed(ActionEvent e) {
	    JComponent c = (JComponent) e.getSource();
	    if (c == vi.showCalcium) {vi.calcium=(((JCheckBox)c).isSelected());} else
	    if (c == vi.showVoltage) {vi.voltage=(((JCheckBox)c).isSelected());} else
	    if (c == vi.showSecondary) {if (vi.m2!=null) vi.secondary=(((JCheckBox)c).isSelected());} else
	    if (c == vi.showIndex) {vi.drawcoordinates=(((JCheckBox)c).isSelected());}
	    if (c == vi.showScrollbars){ vi.setExpanded( (((JCheckBox)c).isSelected()));}
	    if (c == vi.showRuler){vi.rulermode=(((JCheckBox)c).isSelected());}else
	    if (c == vi.showCursor){vi.cursormode=(((JCheckBox)c).isSelected());}else
	    if (c == vi.unzoom) {vi.zoom(0,0,vi.xdim,vi.ydim,(int)(((float)vi.xdim/(float)vi.maxxindex)),(int)(((float)vi.ydim/(float)vi.maxyindex)));	}
	    if (c == vi.savepic){vi.saveImage();}
	    vi.rerender();

	}
}


class keyboardListener extends KeyAdapter{
    traceView tr;
    public keyboardListener(traceView tr){
		this.tr=tr;
	}

	public void keyReleased(KeyEvent e){
		System.out.println("key released");
		if (e.isControlDown()&&(e.getKeyCode() == KeyEvent.VK_C)){
         tr.saveImage();
	     System.out.println("saved image");
	     }
	}
}

class trListener extends MouseInputAdapter {
	traceView vi;
	int x;
	int y;
	int tx=0;

	int ty=0;
	int bx=0;
	int by=0;
	boolean dragged=false;
	boolean shifton=false;

	public trListener(traceView vi){
		   this.vi=vi;
		}

    public void mouseMoved(MouseEvent e){
	 e.getX();
	 e.getY();
	// vi.where(e.getX(), e.getY());
	 vi.repaint();
	 }


	public void mousePressed(MouseEvent e) {
	 	  tx = e.getX();
		  ty = e.getY();
         // System.out.println("x="+x+"+y="+y);


		}

	  public void mouseDragged(MouseEvent e) {
		 bx=e.getX();
		 by=e.getY();

		 if (Math.abs(vi.where_x(bx)-vi.where_x(tx))<1.0f)
		  vi.setstartend=true;
		  else vi.setstartend=false;
          if ((vi.cursormode)||(vi.rulermode)){
			  vi.setruler(tx,ty,bx,by);
              if (vi.cursormode) vi.setcursor(tx,ty,bx,by);

		  }else{


		 if ((bx<tx)||(by<ty)){
		  //dragging right to left
		  vi.selectcol=Color.red;
		  vi.selectrect=new Rectangle(bx,by,(tx-bx),(ty-by));

		 }else{
		 //dragging left to right
		 vi.selectcol=Color.green;
		 vi.selectrect=new Rectangle(tx,ty,(bx-tx),(by-ty));
	      }
	  }
		 vi.repaint();
	}


  public void mouseReleased(MouseEvent e) {
	 if (vi.on_tap_plottrace){
		bx=e.getX();
		by=e.getY();
		if ((Math.abs(tx-bx)<3)&&(Math.abs(ty-by)<3)){
			vi.traceToNavigator(tx,ty);
			return;
		}
	 }
     if (vi.cursormode){
		vi.cursorrelease(e.getX(),e.getY());
	 }else
     if (vi.rulermode){
	    vi.rulerrelease();

	 }else{

    // System.out.println("mouse released with ("+tx+","+bx+")");
	// System.out.println("retranslated=  ("+vi.where_x(tx)+", "+vi.where_x(bx)+")");

     vi.selectrect=null;
     float winwidth=(((float)vi.xdim/(float)vi.maxxindex)); //31
     float winheight=(((float)vi.ydim/(float)vi.maxyindex)); //31
     float x=(bx-tx)/winwidth;
     float y=(by-ty)/winheight;
     if (Math.abs(vi.where_x(bx)-vi.where_x(tx))<1.0f){
		//System.out.println("going to setbeginend with "+vi.where_x(tx)+" "+vi.where_x(bx));
		vi.setbeginend(vi.where_x(tx), vi.where_x(bx));
	    return;
	 }
     if ((bx<tx)||(by<ty)){
      tx=0;ty=0;bx=vi.xdim;by=vi.ydim;
      }

     vi.zoom(tx,ty,bx,by,(int)winwidth,(int)winheight);
  }
  vi.jp.requestFocus();

  }
}


class drawsurface extends JPanel{
	traceView tr;

	public drawsurface(traceView tr){

		this.tr=tr;

	}


	public void paint(Graphics g_old){
		if (tr.buffer!=null){
		Graphics2D g=(Graphics2D)g_old;
		g.drawImage(tr.buffer,0,0,this);
			if (tr.selectrect!=null){
				if (tr.setstartend){
				  g.setColor(Color.yellow);
				  g.draw(tr.selectrect);

				}else{

				g.setColor(tr.selectcol);
			    g.draw(tr.selectrect);
			    }
     }
     if (tr.showcursor){
	  g.setColor(Color.green);
	  g.drawLine(tr.bx,0,tr.bx,tr.height);
	  g.drawLine(tr.tx,0,tr.tx,tr.height);
	  g.drawString("t="+tr.x_end_scaled,tr.bx,tr.by+15);

	 }
     if (tr.showruler){
		 g.setColor(Color.red);
		 g.drawLine(tr.tx,tr.ty,tr.bx,tr.by);
		 //g.drawString(""+tr.x_start_scaled,tr.tx,tr.ty);
		 g.drawString("d="+(tr.x_end_scaled-tr.x_start_scaled),tr.bx,tr.by);

	 }


	}
}
}

