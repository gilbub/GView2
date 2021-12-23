package gvdecoder;
/*
 * Swing version.
 */

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.prefs.*;
import java.util.regex.*;
import java.awt.image.*;
import java.awt.image.renderable.*;
//import JSci.maths.*;
//import JSci.maths.wavelet.*;
import gvdecoder.trace.*;
import gvdecoder.prefs.*;


/*
 Paints a graph from a file.
 For now, assume the data is a series of ints, not scaled.
 The code should determine the amount of room it has, and
 generate a second x and y array that contains a drawable line.
 It uses drawPolyLine(arrayx, arrayy, length)
 */


public class NavigatorGraph extends JPanel implements KeyListener, FrameListener, HtmlSaver{

   /*constants*/
   static final int SCALE_INVERSE_MINDIST=30; //min distance that the mouse drags backward before rescale is called.

   static final int SCALE_INVERSE_MAXDIST=200; //if scaled past this distance, rescale all points.

    public GView gv;
    public Viewer2 vw;
    public NavigatorGraphParams np;

    public String filename;
    JPopupMenu popup;
    JPopupMenu cursorinfo;
    JPopupMenu cursorpopup;
    JLabel cursorinfolabel;
	JMenu menu, submenu;
	JRadioButtonMenuItem rbMenuItem;
    String messageString="";
	Rectangle[] rect; //used to select a trace
	public Rectangle[] cursors;//used to select a cursor.
	Rectangle NavigatorButton=new Rectangle(0,0,10,10);

	public TraceGroup tg;

	public int length;
	public int cols;
	public int SelectedRoi=-1;
	public gvdecoder.trace.Cursor SelectedCursor=null;
	DetectorTag SelectedTag=null;
    Dimension preferredSize = new Dimension(300,100);
    public int rectWidth = 50;
    int rectHeight = 50;
	public ArrayList rois=null;
	public boolean NAVIGATE=false;
	public boolean SHOWALL=true;
	static Vector FrameListeners;
	public Trace DetectorTagReferenceTrace;

	public static volatile boolean SnapScaleToSelection=false;
	public static volatile double SnapScaleGridStartIncrement=5;
	public static volatile double SnapScaleGridEndIncrement=10;

	public double x_scale=1;  //what the xarr array gets divided by
	public double y_scale=1;  //what the yarr array gets divided by
	public int x_offset=0;    //the first frame shown on the graph
	public int y_offset;   //the y_offset for the graph
	public int x_range=0;     //the length (in frames) of the graph
	public int user_start =0; //user selectable range (sets a frame number) starting point.
	public int user_last =0;
	public Dimension lastRange;
	public boolean flipped=false;
    public Vector CursorVector;
	public Vector TagVector;
	public int frame_pos=0;


	Dimension windowSize;  //keeps track of the window size for resize events.
	Dimension scaleToSize; //similar to windowSize, but used to scale the data

	int maxY=Integer.MIN_VALUE;
	int minY=Integer.MAX_VALUE;

	public Rectangle selectionRect;
	public boolean showSelection;
	public boolean invertSelection; //in case of a zoomout operation
    Font font = new Font("Arial", Font.PLAIN, 10);
    public MyInternalFrame mif;

    public boolean ISBLANK=false;


	public String saveHtml(){
		try{
		     String namestring=AnalysisHelper.getAnalysisHelper().saveImage(getImage());
		     File imagef=new File(namestring);
			 java.net.URL imageurl=imagef.toURI().toURL();
			 String res="</pre>";
			 if ((vw!=null) && (vw.filename!=null))
			     res="<br>"+vw.filename+"\n";
			  res+="<img src='"+imageurl+"'><pre>";
			  return res;
			 }catch(Exception e){e.printStackTrace();}
     return "";

	}


    /** this constructor only exists to facilitate opening a navigator without an associated viewer **/
    public NavigatorGraph(String absolutefilename, GView gv, MyInternalFrame mif, ArrayList rois){
	 this.gv=gv;
	 Viewer2 v2=null;
	 System.out.println("\n\n USING GVIEW CONSTRUCTOR");
	 initializeNavigator(absolutefilename, v2,  mif, rois);

	}

	public NavigatorGraph(String name, GView gv, Trace trace, Trace referenceTrace){

     this.DetectorTagReferenceTrace=referenceTrace;
     this.filename=name;
     this.gv=gv;
     tg=new TraceGroup();
     gv.jh.setTraceGroup(tg);
     tg.AddTrace(trace);
     length=trace.getLength();
     cols=2;
     tg.setXValues(length);
     x_range=length;
     x_offset=0;
     rect = new Rectangle[cols-1];
	 	 for (int i=0;i<rect.length;i++) rect[i]=new Rectangle(0,0,10,10); //initialize with something


     MyInternalFrame frame=new MyInternalFrame(name,this);
     mif=frame;
     rois=new ArrayList();
	 rois.add(new ROI(null,Color.blue));
     frame.setVisible(true); //necessary as of kestrel
	    	        gv.desktop.add(frame);
	    	        try {
	    	            frame.setSelected(true);
	    	        } catch (java.beans.PropertyVetoException e){};
	  setupGUI();

     System.out.println("done creating new navigator");
    }


    public NavigatorGraph(String name, GView gv, Trace trace){


	     this.filename=name;
	     this.gv=gv;
	     tg=new TraceGroup();
	     gv.jh.setTraceGroup(tg);
	     tg.AddTrace(trace);
	     length=trace.getLength();
	     cols=2;
	     tg.setXValues(length);
	     x_range=length;
	     x_offset=0;
	     rect = new Rectangle[cols-1];
		 	 for (int i=0;i<rect.length;i++) rect[i]=new Rectangle(0,0,10,10); //initialize with something


	     MyInternalFrame frame=new MyInternalFrame(name,this);
	     mif=frame;
	     rois=new ArrayList();
		 rois.add(new ROI(null,Color.blue));
	     frame.setVisible(true); //necessary as of kestrel
		    	        gv.desktop.add(frame);
		    	        try {
		    	            frame.setSelected(true);
		    	        } catch (java.beans.PropertyVetoException e){};
		  setupGUI();

	     System.out.println("done creating new navigator");
    }


    public NavigatorGraph(String absolutefilename, Viewer2 vw, MyInternalFrame mif, ArrayList rois) {
	  System.out.println("\n\n USING Viewer2 CONSTRUCTOR");
	  initializeNavigator(absolutefilename, vw,  mif, rois);
	}

	public NavigatorGraph(int[][] roi_t, Viewer2 vw, MyInternalFrame mif, ArrayList rois) {
		  System.out.println("\n\n USING Viewer2 CONSTRUCTOR");
		  initializeNavigator(roi_t, vw,  mif, rois);
	}

    public NavigatorGraph(String name,GView gv, Trace tr, MyInternalFrame mif){
	 np=NavigatorGraphParams.getInstance();
     this.mif=mif;
     filename=name;
     tg=new TraceGroup();
 	 tg.AddTrace(tr);
 	 gv.jh.setTraceGroup(tg);
	 x_range=length;
	 x_offset=0;

 	 tg.setXValues(tr.y_values.length);
 	 rect=new Rectangle[1];
 	 cols=1;
 	 rois=new ArrayList();
 	 rois.add(new ROI(null,Color.blue));
 	 this.gv=gv;

     setupGUI();
 	 MyInternalFrame frame=new MyInternalFrame(name,this);
     frame.setVisible(true); //necessary as of kestrel
	    	        gv.desktop.add(frame);
	    	        try {
	    	            frame.setSelected(true);
	    	        } catch (java.beans.PropertyVetoException e){};




	}



    public void initializeNavigator(String absolutefilename,
    								Viewer2 vw,
    								MyInternalFrame mif,
    								ArrayList rois){

     np=NavigatorGraphParams.getInstance();
     this.mif=mif;
     this.rois=rois;
	 if (rois==null) NAVIGATE=true;
	 this.vw=vw;
	 if (vw!=null)this.gv=vw.ifd;

	 filename=absolutefilename;

	 tg=new TraceGroup();

	 gvdecoder.trace.getArrayFromFile gaff=new gvdecoder.trace.getArrayFromFile(absolutefilename);
	 CursorVector=gaff.cursors;
	 int[][] td=gaff.returnTransposedArray();
	 length=td[0].length;
	 cols=td.length;
	 tg.setXValues(td[0]);
    /*
    for (int u=2600;u<5000;u++){
		 td[1][u]-=20890;
		 td[2][u]+=30000;
	 }
    */
	 for (int q=1;q<td.length;q++){
	  tg.AddTrace(td[q]);
	  }
     gv.jh.setTraceGroup(tg);
	 x_range=length;
	 x_offset=0;
	 rect = new Rectangle[cols-1];
	 for (int i=0;i<rect.length;i++) rect[i]=new Rectangle(0,0,10,10); //initialize with something

     setupGUI();


    }

    public void initializeNavigator(   int [][] roi_t,
	    								Viewer2 vw,
	    								MyInternalFrame mif,
	    								ArrayList rois){

	     np=NavigatorGraphParams.getInstance();
	     this.mif=mif;
	     this.rois=rois;
		 if (rois==null) NAVIGATE=true;
		 this.vw=vw;
		 if (vw!=null)this.gv=vw.ifd;


		 tg=new TraceGroup();


		 length=roi_t[0].length;
		 cols=roi_t.length;
		 tg.setXValues(roi_t[0]);

		 for (int q=1;q<roi_t.length;q++){
		  tg.AddTrace(roi_t[q]);
		  }
	     gv.jh.setTraceGroup(tg);
		 x_range=length;
		 x_offset=0;
		 rect = new Rectangle[cols-1];
		 for (int i=0;i<rect.length;i++) rect[i]=new Rectangle(0,0,10,10); //initialize with something

	     setupGUI();


	    }

  public void append(int tracenumber, int numberofelements, int [] data){
	  Trace tr=(Trace)tg.Traces.get(tracenumber);
	  tr.replaceValues(numberofelements,data);
	  rescale();
	  repaint();
	  }

 public void silentAppend(int tracenumber, int numberofelements, int [] data){
	  Trace tr=(Trace)tg.Traces.get(tracenumber);
	  tr.replaceValues(numberofelements,data);

 }
   public void replaceTraces(   int [][] roi_t, ArrayList rois){

	     np=NavigatorGraphParams.getInstance();

	     this.rois=rois;
		 if (rois==null) NAVIGATE=true;


		 tg=new TraceGroup();


		 length=roi_t[0].length;
		 cols=roi_t.length;
		 tg.setXValues(roi_t[0]);

		 for (int q=1;q<roi_t.length;q++){
		  tg.AddTrace(roi_t[q]);
		  }
	     gv.jh.setTraceGroup(tg);
		 x_range=length;
		 x_offset=0;
		 rect = new Rectangle[cols-1];
		 for (int i=0;i<rect.length;i++) rect[i]=new Rectangle(0,0,10,10); //initialize with something
        rescale();
        repaint();
	    }







  public void resetNavigator(){
	  cols=tg.Traces.size()+1;
      rect = new Rectangle[cols-1];
	  for (int i=0;i<rect.length;i++) rect[i]=new Rectangle(0,0,10,10); //initialize with something

  }

 public void addTrace(Trace tr){
	 tg.AddTrace(tr);
	 rois.add(new ROI());
	 cols=tg.Traces.size()+1;
	 rect = new Rectangle[cols-1];
	 	 for (int i=0;i<rect.length;i++) rect[i]=new Rectangle(0,0,10,10); //initialize with something


 }

 public void addTrace(Trace tr, ROI roi){
 	 tg.AddTrace(tr);
 	 rois.add(roi);
 	 cols=tg.Traces.size()+1;
 	 rect = new Rectangle[cols-1];
 	 	 for (int i=0;i<rect.length;i++) rect[i]=new Rectangle(0,0,10,10); //initialize with something


 }


  public void setupGUI(){
	 if (mif!=null){
	 np=NavigatorGraphParams.getInstance();
	 mif.setSize(new Dimension(np.InitialWidth,np.InitialHeight));
	 mif.setLocation(np.InitialX,np.InitialY);
     }
	 windowSize=this.getSize();
     setScaleToSize(windowSize);
	 selectionRect=new Rectangle(0,0,10,10);//not used till later, initialized now for convenience.
     setFocusable(true);
     requestFocus();
     addKeyListener(this);
    cursorinfo=new JPopupMenu();
    cursorinfolabel=new JLabel("null");
    cursorinfo.add(cursorinfolabel);
    cursorpopup=new JPopupMenu();
    popup=new JPopupMenu();
	menuListener myMenuListener=new menuListener(this);
	sgListener myListener = new sgListener(this);


    JMenuItem menuItem;
     menuItem=new JMenuItem("edit note");
     menuItem.addActionListener(myMenuListener);
     cursorpopup.add(menuItem);
     menuItem=new JMenuItem("delete note");
     menuItem.addActionListener(myMenuListener);
     cursorpopup.add(menuItem);
     menuItem=new JMenuItem("show all notes");
	 menuItem.addActionListener(myMenuListener);
	 cursorpopup.add(menuItem);
     menuItem=new JMenuItem("hide all notes");
	 menuItem.addActionListener(myMenuListener);
	 cursorpopup.add(menuItem);


      menuItem = new JMenuItem("jump to");
	  menuItem.addActionListener(myMenuListener);
	  popup.add(menuItem);
	  rbMenuItem = new JRadioButtonMenuItem("show all");
	  rbMenuItem.setSelected(true);
	  rbMenuItem.addActionListener(myMenuListener);
	  rbMenuItem = new JRadioButtonMenuItem("scale y");
	  rbMenuItem.setSelected(!tg.holdYscale);
	  rbMenuItem.addActionListener(myMenuListener);
	  popup.add(rbMenuItem);
	  menuItem = new JMenuItem("add cursor");
	  menuItem.addActionListener(myMenuListener);
	  popup.add(menuItem);

		menuItem = new JMenuItem("remove cursor");
	  menuItem.addActionListener(myMenuListener);
	  popup.add(menuItem);


	  menuItem = new JMenuItem("link viewer");
	  menuItem.addActionListener(myMenuListener);
	  popup.add(menuItem);



      menuItem = new JMenuItem("process rois");
	  menuItem.addActionListener(myMenuListener);
	  popup.add(menuItem);

	  menuItem = new JMenuItem("clear selected");
	  menuItem.addActionListener(myMenuListener);
	  popup.add(menuItem);


      menuItem = new JMenuItem("clear traces");
      menuItem.addActionListener(myMenuListener);
      popup.add(menuItem);

      menuItem = new JMenuItem("save Navigator");
	  menuItem.addActionListener(myMenuListener);
	  popup.add(menuItem);


	  popup.addSeparator();
	  	  submenu=new JMenu("processing");

	  	  			 menuItem = new JMenuItem("add tag");

	  	  			 menuItem.addActionListener(myMenuListener);
	  	  			 submenu.add(menuItem);

	  	  			 menuItem = new JMenuItem("move tag");
					 menuItem.addActionListener(myMenuListener);
	  	  			 submenu.add(menuItem);

	  	  			menuItem = new JMenuItem("run detector");

	  	  			menuItem.addActionListener(myMenuListener);
	  	  			submenu.add(menuItem);

	  	  			menuItem = new JMenuItem("recalculate");

	  	  			menuItem.addActionListener(myMenuListener);
	  	  			submenu.add(menuItem);
	  		 popup.add(submenu);





	  popup.add(menuItem);
	  popup.addSeparator();
	  submenu=new JMenu("settings");



	  			submenu.setMnemonic(KeyEvent.VK_S);
	  			menuItem = new JMenuItem("load");
	  			menuItem.setMnemonic(KeyEvent.VK_L);
	  			menuItem.addActionListener(myMenuListener);
	  			submenu.add(menuItem);

	  			menuItem = new JMenuItem("save");
	  			menuItem.setMnemonic(KeyEvent.VK_S);
	  			menuItem.addActionListener(myMenuListener);
	  			submenu.add(menuItem);

	  			menuItem = new JMenuItem("load next");
	  			menuItem.setMnemonic(KeyEvent.VK_N);
	  			menuItem.addActionListener(myMenuListener);
	  			submenu.add(menuItem);

	  			menuItem = new JMenuItem("set flip");
				menuItem.setMnemonic(KeyEvent.VK_N);
				menuItem.addActionListener(myMenuListener);
	  			submenu.add(menuItem);


	  		 popup.add(submenu);


      popup.addSeparator();
      submenu=new JMenu("export");
       menuItem = new JMenuItem("save image");
	   				menuItem.addActionListener(myMenuListener);
	   	            submenu.add(menuItem);

	   	             menuItem = new JMenuItem("ptolemy");
	   				 menuItem.addActionListener(myMenuListener);

	   				 menuItem = new JMenuItem("print");
	   	  	         menuItem.addActionListener(myMenuListener);
 				 submenu.add(menuItem);


				 menuItem = new JMenuItem("range->Matrix");
				 menuItem.addActionListener(myMenuListener);
 				 submenu.add(menuItem);

 				 menuItem = new JMenuItem("to clipboard");
				 				 menuItem.addActionListener(myMenuListener);
 				 submenu.add(menuItem);

 		popup.add(submenu);


	  popup.addSeparator();
	          submenu = new JMenu("mode");
	          submenu.setMnemonic(KeyEvent.VK_M);
	          ButtonGroup group = new ButtonGroup();
	          rbMenuItem = new JRadioButtonMenuItem("navigate");
			          if (NAVIGATE) rbMenuItem.setSelected(true);
			          rbMenuItem.setMnemonic(KeyEvent.VK_N);
			          rbMenuItem.setAccelerator(KeyStroke.getKeyStroke(
					          KeyEvent.VK_N, ActionEvent.ALT_MASK));

			          group.add(rbMenuItem);
					  rbMenuItem.addActionListener(myMenuListener);
			          submenu.add(rbMenuItem);

			          rbMenuItem = new JRadioButtonMenuItem("scale");
					  if (!NAVIGATE) rbMenuItem.setSelected(true);
			          rbMenuItem.setMnemonic(KeyEvent.VK_S);
			          			          rbMenuItem.setAccelerator(KeyStroke.getKeyStroke(
					          KeyEvent.VK_S, ActionEvent.ALT_MASK));
			          group.add(rbMenuItem);
                      rbMenuItem.addActionListener(myMenuListener);
					  submenu.add(rbMenuItem);

		 popup.add(submenu);
		 popup.addSeparator();
	          submenu = new JMenu("select");
	          submenu.setMnemonic(KeyEvent.VK_S);
	          ButtonGroup group2 = new ButtonGroup();
	          for (int k=1;k<cols;k++){
			  String col="#666666";
			  if (rois!=null){
			   Color tmp=((ROI)rois.get(k-1)).color;
			   String red=Integer.toHexString(tmp.getRed());
			   String green=Integer.toHexString(tmp.getGreen());
			   String blue=Integer.toHexString(tmp.getBlue());
			   col="#"+red+green+blue;
			  }
			  String roitag="<html><font color="+col+">ROI "+k+"</font></html>";
			  rbMenuItem = new JRadioButtonMenuItem(roitag);
			  rbMenuItem.addActionListener(myMenuListener);
			  if (k==1) rbMenuItem.setSelected(true);
			  group2.add(rbMenuItem);
			  submenu.add(rbMenuItem);
			  }

        popup.add(submenu);

		     submenu=new JMenu("scale ROI");
			 submenu.setMnemonic(KeyEvent.VK_R);
			 menuItem = new JMenuItem("all");
			 menuItem.setMnemonic(KeyEvent.VK_A);
			 menuItem.addActionListener(myMenuListener);
			 submenu.add(menuItem);

			menuItem = new JMenuItem("selected");
			menuItem.setMnemonic(KeyEvent.VK_S);
			menuItem.addActionListener(myMenuListener);
			submenu.add(menuItem);

			menuItem = new JMenuItem("recenter");
			menuItem.setMnemonic(KeyEvent.VK_R);
			menuItem.addActionListener(myMenuListener);
			submenu.add(menuItem);

			menuItem = new JMenuItem("absolute");
			menuItem.setMnemonic(KeyEvent.VK_B);
			menuItem.addActionListener(myMenuListener);
			submenu.add(menuItem);

			menuItem = new JMenuItem("flip");
			menuItem.setMnemonic(KeyEvent.VK_F);
			menuItem.addActionListener(myMenuListener);
			submenu.add(menuItem);

		 popup.add(submenu);



      addMouseListener(myListener);
      addMouseMotionListener(myListener);

  }

 /*framelistener stuff*/
 public void SetFrame(int framenum){
  frame_pos=framenum;
  //System.out.println("notified that frame is = "+frame_pos);
  repaint();
 }


/** Handle the key typed event from the text field. */
    public void keyTyped(KeyEvent e) {
	if (e.getKeyChar()=='n')  {
		//System.out.println("N KEY");
		if (NAVIGATE) NAVIGATE=false;
		else NAVIGATE=true;
		repaint(); ;
       }
    else if (Character.isDigit(e.getKeyChar())) addCursorType(Character.getNumericValue(e.getKeyChar()));
    repaint();


    }

   public void keyPressed(KeyEvent e) {
   		// Bottom padel uses LEFT and RIGHT arrow buttons
   		// Top padel users, uses A and D to move the padel.
           switch (e.getKeyCode()) {
               case KeyEvent.VK_RIGHT: x_offset=x_offset+x_range;  break;
               case KeyEvent.VK_LEFT: x_offset=x_offset-x_range;  break;

           }
           if (x_offset<0) x_offset=0;
           if (x_offset+x_range>=length) x_offset=0;
           scale(x_offset);
           repaint();
    }

    /** Handle the key released event from the text field. */
    public void keyReleased(KeyEvent e) {

		if (e.isControlDown()&&(e.getKeyCode() == KeyEvent.VK_C)){
			 gvdecoder.ImageUtils.setClipboard(getImage());
		     AnalysisHelper.getAnalysisHelper().Notify("navigator exported to clipboard");
		 }
    }


public void setScaleToSize(Dimension d){
		if (scaleToSize==null){
		  scaleToSize=new Dimension();
		 }

		 scaleToSize.setSize(d.getWidth(), d.getHeight()-10);

	}



    public Dimension getPreferredSize() {
        return preferredSize;
    }

  public void setZoom(int i){
  	//assume compression of 5 not implementied
	}


public void replaceLastTrace(Trace newtrace){
	tg.RemoveTrace(tg.Traces.size()-1);
	tg.AddTrace(newtrace);
	rescale();
	repaint();
}


public void replaceLastTrace(Matrix ma,int startframe, int x, int y){
  np=NavigatorGraphParams.getInstance();
  if (tg.Traces.size()>0){
	if (np.UseUnitROI) ((Trace)tg.Traces.get(tg.Traces.size()-1)).replaceTrace(ma,startframe,x,y);
	else ((Trace)tg.Traces.get(tg.Traces.size()-1)).replaceTrace(ma,startframe,x,y,np.ROI_width);
	rescale();
	repaint();
 }
}

public void clearTraces(){

	for (int i=0;i<cols;i++){
		tg.RemoveTrace(tg.Traces.size()-1);
	}
	cols=0;
	vw.jp.deleteAllROIs();
	rois=vw.jp.rois;
	repaint();
}

public void clearSelected(){
	 tg.RemoveTrace(SelectedRoi-1);
	 rois.remove(SelectedRoi);
	 cols=cols-1;
	 repaint();
}


/** Saves the traces as a .nav file, in the Navigator dir directory for quick opening...*/
public void saveNavigator(){
  	//clean up filename:
    String navname=vw.getNavigatorFileName();//vw.absolutefilename.replace(':','_').replace('\\','_').replace('//','_').replace('.','_');
    String newfilename=vw.ifd.pv.getStringProperty("Navigator dir",".")+File.separator+navname+".nav";
     try{
	    PrintWriter file=new PrintWriter(new FileWriter(newfilename),true);
	    if (CursorVector!=null){
			for (int v=0;v<CursorVector.size();v++){
				gvdecoder.trace.Cursor c=(gvdecoder.trace.Cursor)CursorVector.elementAt(v);
				file.println("&cursor="+c.toString());
			}
		}
	    for (int j=0;j<((Trace)tg.Traces.get(0)).raw_y.length;j++){
		 file.print((j)+" ");
		 for (int i=0;i<tg.Traces.size();i++) {
			 file.print( ((Trace)tg.Traces.get(i)).raw_y[j]+" ");
		 }
		 file.print("\n");
		 }
	    file.close();
	    System.out.println("debug closed file");
	    }catch(IOException e){System.out.println("error opening file for rois...");}



}

public void scale(int offset){
   int startx=tg.raw_x[offset];
   System.out.println("in scale : startx="+startx);
   System.out.println("scale: x_offset="+x_offset+" x_range="+x_range);
  tg.scaleXY(x_offset,x_offset+x_range,0,(int)scaleToSize.getWidth(),0,(int)scaleToSize.getHeight());
  x_scale=1/tg.getXScale();

  //tg.scaleXY(startx,startx+x_range,0,(int)scaleToSize.getWidth(),0,(int)scaleToSize.getHeight());

}


  public void rescale(){
        setScaleToSize(windowSize);
        int windowHeight=(int)scaleToSize.getHeight();
		int windowWidth=(int)scaleToSize.getWidth();
		x_scale=(double)(length-1)/(double)windowWidth;
		y_scale=(double)(maxY-minY)/((double)windowHeight);
		y_offset=-1*minY;

	 lastRange=new Dimension(x_offset,x_range);

	 x_offset=0;

	 x_range=length;
	  tg.scaleXY(x_offset,x_offset+x_range,0,(int)scaleToSize.getWidth(),0,(int)scaleToSize.getHeight());

	}

	public void rescaleToSelection(){
	   if (invertSelection){

	    if (selectionRect.width<-SCALE_INVERSE_MAXDIST) rescale();
	    else
	    if ((selectionRect.width>=-SCALE_INVERSE_MAXDIST)&&(selectionRect.width<-SCALE_INVERSE_MINDIST)){
		 int oldx_range=x_range;
		 int oldx_offset=x_offset;
		 double stretch=(-1*((double)selectionRect.width+SCALE_INVERSE_MINDIST)/SCALE_INVERSE_MINDIST);
		 if (stretch<1) stretch=1.1;
		 x_range=(int)(x_range*stretch);

		 x_offset=x_offset-(int)( (double)(x_range-oldx_range)/2);
		 if (x_offset<0) x_offset=0;
		 if (x_offset+x_range>=tg.raw_x.length) x_range=tg.raw_x.length-1-x_offset;

		  scale(x_offset);

	    }
		else
		if (selectionRect.width>-SCALE_INVERSE_MINDIST) {};
		}
	   else
	   {
		 //where in the xrange was the mouse clicked...
	    x_offset=x_offset+(int)(selectionRect.x*x_scale);
        //if (SnapScaleToSelection)
		//whats the new x_scale...
		x_scale=x_scale / (scaleToSize.getWidth()/selectionRect.width);
		x_range=(int)(scaleToSize.getWidth()*x_scale);
    	scale(x_offset);
	   }
	  if (vw!=null){vw.setNavigatorStartEnd(x_offset,x_offset+x_range);}
	  repaint();
	}

	public void setPoint(int x, int y){
	// uses x only for now...
	user_last = user_start;
	user_start =x_offset+(int)(x*x_scale);
	System.out.println("last="+user_last+" new="+user_start+" difference="+(user_start-user_last)+" CWRU="+(double)(user_start-user_last)*0.667 );
	repaint();
	}


	//try to save the image
	public void saveImage(){
        AnalysisHelper.getAnalysisHelper().saveImage(getImage());

		//ImageUtils.WriteImage("analysis/trace.bmp",getImage(),"bmp");
	}

	public void toClipboard(){
		 gvdecoder.ImageUtils.setClipboard(getImage());
		 AnalysisHelper.getAnalysisHelper().Notify("navigator image exported to clipboard");

	}


    public BufferedImage getImage(){
		BufferedImage image = new BufferedImage(
		   (int)windowSize.getWidth(),(int)windowSize.getHeight(),BufferedImage.TYPE_BYTE_INDEXED);

		Graphics2D myG = (Graphics2D)image.getGraphics();

		paintComponent(myG);
		return image;

	}

    //save and load are shortcuts to speed up repetative work, mostly related to cwru format where all files are
    //similar. It saves scales, offsets, and cursors to a file, and reads them back
    public void savesettings(){
	try{
	RandomAccessFile outfile=new RandomAccessFile("navigatorsettings.dat","rw");
	outfile.writeInt((int)windowSize.getWidth());
	outfile.writeInt((int)windowSize.getHeight());
    outfile.writeDouble(x_scale);
    outfile.writeDouble(y_scale);
    outfile.writeInt(x_offset);
    outfile.writeInt(y_offset);
    outfile.writeInt(x_range);
    outfile.writeInt(user_start);
    outfile.writeInt(user_last);
    outfile.writeInt(selectionRect.x);
    outfile.writeInt(selectionRect.y);
    outfile.writeInt(selectionRect.width);
    outfile.writeInt(selectionRect.height);
    outfile.writeInt((int)scaleToSize.getWidth());
    outfile.writeInt((int)scaleToSize.getHeight());

    //scaling the traces

    outfile.writeInt(tg.Traces.size());
    for (int k=0;k<tg.Traces.size();k++){
	 Trace trace=(Trace)tg.Traces.get(k);
	 outfile.writeDouble(trace.y_scale);
	 outfile.writeDouble(trace.y_offset);
	 outfile.writeInt(trace.start_xrange);
	 outfile.writeInt(trace.end_xrange);
	}

	if (CursorVector==null) outfile.writeInt(0);
	else{
		outfile.writeInt(CursorVector.size());
		for (int i=0;i<CursorVector.size();i++){
		gvdecoder.trace.Cursor tmp=(gvdecoder.trace.Cursor)CursorVector.elementAt(i);
		outfile.writeInt(tmp.position.intValue());
	}
	}


    }catch(IOException e){e.printStackTrace();}
    }


    public void loadsettings(){
	try{
	RandomAccessFile infile=new RandomAccessFile("navigatorsettings.dat","r");
	windowSize.setSize(infile.readInt()+10,infile.readInt()+25);

	mif.setSize(windowSize);
    //now ensure its the same to avoid a rescale call...
    windowSize=this.getSize();
    x_scale=infile.readDouble();
    y_scale=infile.readDouble();
    x_offset=infile.readInt();
    y_offset=infile.readInt();
    x_range=infile.readInt();
    user_start=infile.readInt();
    user_last=infile.readInt();
    selectionRect.x=infile.readInt();
    selectionRect.y=infile.readInt();
    selectionRect.width=infile.readInt();
    selectionRect.height=infile.readInt();
    scaleToSize.setSize(infile.readInt(), infile.readInt());
    //scaling the traces


    int numberoftraces=infile.readInt();
    if (tg.Traces.size()<numberoftraces){
     numberoftraces=tg.Traces.size();
     System.out.println("number of traces changed to "+numberoftraces);
     }
	for (int k=0;k<numberoftraces;k++){
		 Trace trace=(Trace)tg.Traces.get(k);
		 trace.y_scale=infile.readDouble();
		 trace.y_offset=infile.readDouble();
		 trace.start_xrange=infile.readInt();
		 trace.end_xrange=infile.readInt();
	}
	int numberofcursors=infile.readInt();
	for (int i=0;i<numberofcursors;i++){
	user_start=infile.readInt();
	addCursor();
	}

	//NAVIGATE=true;
	invertSelection=false;
	//rescaleToSelection();
   // scale(x_offset);
   tg.reCalculateAllTransformations();
   tg.scaleX(x_offset,x_offset+x_range,0,(int)scaleToSize.getWidth());
   tg.reCalculateAllYValues();
    }catch(IOException e){e.printStackTrace();}
    System.out.println("out of load");

   }





   public void loadnext(){
   //loads the next navigator graph in a series (must be there...)
   //this is a shortcut that is usefull for CWRU type data.
   //what is the new file name?

   int firstdot=filename.indexOf(".");

   System.out.println(filename.substring(firstdot-3,firstdot));
   int newfilenumber=Integer.parseInt(filename.substring(firstdot-3,firstdot))+1;
   String newfilename=""+newfilenumber;
   System.out.println(newfilename+" "+newfilename.length());
   if (newfilename.length()==1) newfilename="00"+newfilename;
   if (newfilename.length()==2) newfilename="0"+newfilename;

   String wholefilename=filename.substring(0,firstdot-3)+newfilename+filename.substring(firstdot);
   System.out.println("wholefile="+wholefilename);
   //if (vw!=null)
   gv.openNavigatorWindow(wholefilename,newfilename);


   }


   public void startDetector(){
	   //send a trace to a detector window
	   Trace trace=tg.getTrace(SelectedRoi-1);
	   trace.name=filename+" t="+(SelectedRoi-1);
	   Trace newtrace=trace.subTrace(x_offset,x_offset+x_range);
       NavigatorGraph nav=new NavigatorGraph(filename+" t="+(SelectedRoi-1),gv,newtrace,trace);
   }


   public void runDetector(){
	  // (gv.tcf.getTagController()).runDetection(0);
      DetectorManager dm= DetectorManager.getInstance();
      dm.runDetection(tg);

   }

   public void CursorIsSelected(boolean selected,int x, int y){
	   if (selected){
		   if ( (SelectedCursor.note!=null)&&(!SelectedCursor.note.equals("null"))){
			cursorinfolabel.setText(SelectedCursor.note);
		    cursorinfo.show(this,x,y+10);
		}

	   }else{
		  // cursorinfo.hide();
      }
   }

	public void addCursor(){
	// uses last user cursor.
	 addCursor(tg.raw_x[user_start]);

    }

    public void addCursorType(int cursortype){
		//CursorController cc=null;
			if (CursorVector==null){
				CursorVector=new Vector();
			    //register it with the CursorController

			   // gv.openCursorControl(); //opens and initializes only if not open
			   // cc=gv.ccf.getCursorController();
			   // cc.CursorList.add(CursorVector);
			  }

			// if (cc==null) {gv.openCursorControl();cc=gv.ccf.getCursorController();}
			gvdecoder.trace.Cursor cursor = new gvdecoder.trace.Cursor(filename,CursorVector.size(),(double)tg.raw_x[0],tg.raw_x[user_start],cursortype);
	        CursorVector.add(cursor);
			//cc.resetColumnCount();
	       repaint();
	}

    public void addCursor(int position){
	 	//CursorController cc=null;
			if (CursorVector==null){
				CursorVector=new Vector();
			    //register it with the CursorController

		//	    gv.openCursorControl(); //opens and initializes only if not open
		//	    cc=gv.ccf.getCursorController();
		//	    cc.CursorList.add(CursorVector);
			  }

		//	 if (cc==null) cc=gv.ccf.getCursorController();

			gvdecoder.trace.Cursor cursor = new gvdecoder.trace.Cursor(filename,CursorVector.size(),(double)tg.raw_x[0],0.0,0.0,position,Color.green,true);
			CursorVector.add(cursor);
		//	cc.resetColumnCount();
	repaint();

	}


	public void delCursor(int num){



	}

    public void editNote(){
		 if ((CursorVector!=null)&&(SelectedCursor!=null)){
			 String tmp=null;
			 if ((SelectedCursor.note!=null)&&(!SelectedCursor.note.equals("null")))
			 tmp=SelectedCursor.note;
			 SelectedCursor.note=(String)JOptionPane.showInputDialog(this,null,"Add note",JOptionPane.PLAIN_MESSAGE,null,null,tmp);
		 }
	}

	public void deleteNote(){
	if ((CursorVector!=null)&&(SelectedCursor!=null)){
			 SelectedCursor.note=null;
		 }
   }

	public boolean SHOWALLNOTES=false;
	public void showAllNotes(){
	  SHOWALLNOTES=true;
	}

	public void hideAllNotes(){
	 SHOWALLNOTES=false;
	}



	public void addTag(){
	 DetectorManager dm= DetectorManager.getInstance();
	 int selectedval=0;
	 Trace tr=null;
	 if (tg.Traces.size()==1) tr=tg.getTrace(0);
	       else
      if (SelectedRoi>0) {tr=tg.getTrace(SelectedRoi-1); selectedval=SelectedRoi-1;}
	  dm.addTag(tr,selectedval,tg.raw_x[user_start],tg.raw_x[0]);

	}
	public void moveTag(){
		 DetectorManager dm= DetectorManager.getInstance();
		 int selectedval=0;
		 Trace tr=null;
		 if (tg.Traces.size()==1) tr=tg.getTrace(0);
		       else
	      if (SelectedRoi>0) {tr=tg.getTrace(SelectedRoi-1); selectedval=SelectedRoi-1;}
		  dm.resetPosition(tr, tg.raw_x[user_start]);

	}



    public void removeCursor(){
	 //removes the selected cursor
	 if ((CursorVector!=null)&&(SelectedCursor!=null)){
	 	CursorVector.removeElement(SelectedCursor);
	 	SelectedCursor=null;
	 	for (int i=0;i<CursorVector.size();i++){
			//renumber
			((gvdecoder.trace.Cursor)CursorVector.elementAt(i)).number=new Integer(i);
	    }
	 }
	repaint();

	}





	public void setFrame(int x){
	frame_pos =x;
	repaint();
	}

	public void resetSelection(int start_x, int start_y, int end_x, int end_y){
	 //if rectangle is inverted....
	 if (start_x>end_x){/*int tmp=start_x; start_x=end_x; end_x=tmp; */invertSelection=true;}
	 else{
	 invertSelection=false;
     }
	 if (start_y>end_y){int tmp=start_y; start_y=end_y; end_y=tmp;};
	 selectionRect.x=start_x;
	 selectionRect.y=start_y;
	 selectionRect.width=end_x-start_x;
	 selectionRect.height=end_y-start_y;
     System.out.println("reset selection");
	 showSelection=true;
	}

    public void forceRepaint(){
		Graphics g=this.getGraphics();
		paintComponent(g);
		g.dispose();
	}

	public Color debugcolor;
	public int   debugroinum;
    public void paintComponent(Graphics gOld) {
        super.paintComponent(gOld);  //paint background
		Graphics2D g=(Graphics2D)gOld;

		int RelativeStart= tg.raw_x[x_offset];
		int RelativeEnd=RelativeStart+x_range;
		int UserFrameNumber=0;

		if (user_start>=0)UserFrameNumber=tg.raw_x[user_start];

		int UserPosition=(int)((user_start -x_offset)/x_scale);


		//lazy way to detect a resize. should redo CHANGE
		Dimension tmp=this.getSize();
		if (!tmp.equals(windowSize)){
			windowSize=tmp;
			System.out.println("rescale called from paintComponent");
			rescale();
			}
		//clear the screen.
		g.setColor(Color.lightGray);
		g.fillRect(0,0,(int)windowSize.getWidth(),(int)windowSize.getHeight());

		//draw the location of the last selected range
		if (lastRange != null){
		  g.setColor(Color.cyan);
		  g.drawRect((int)(lastRange.getWidth()/x_scale),0,(int)(lastRange.getHeight()/x_scale),(int)windowSize.getHeight());
		}

        //draw each trace.
        if ((SHOWALL)&&(tg.Traces.size()>0)){
		for (int j=0;j<cols-1;j++) {
		 if ((j+1==SelectedRoi)&&(!NAVIGATE)) g.setColor(Color.white);

		 else if ((rois!=null)&&(rois.size()>j) && (!NAVIGATE)) {
			 debugcolor=((ROI)rois.get(j)).color;
			 debugroinum=j;
			 g.setColor(((ROI)rois.get(j)).color);
		  }
		 else g.setColor(Color.darkGray);

	     g.drawPolyline(tg.x_values,tg.getYs(j),length);
	    }
	   } else
	   {
		 if ((SelectedRoi>0)&&(tg.Traces.size()>0)){
			g.setColor(((ROI)rois.get(SelectedRoi-1)).color);
			g.drawPolyline(tg.x_values,tg.getYs(SelectedRoi-1),length);
	     }
	   }


		//draw a line at the current frame position
		if ((frame_pos>RelativeStart)&&(frame_pos<RelativeEnd))	{
			 	 g.setColor(Color.yellow);
				 g.drawLine((int)((frame_pos - RelativeStart)/x_scale),0, (int)((frame_pos -RelativeStart)/x_scale),(int)(windowSize.getHeight()-10));
		 }

		//decorate
		g.setColor(Color.blue);
		g.setFont(font);
		String left_x = "<"+RelativeStart;
	 	String right_x = ""+RelativeEnd+">";
		FontMetrics metrics = g.getFontMetrics();
		int width = metrics.stringWidth(left_x);
		int height = metrics.getHeight();
		int GenericWidth=metrics.stringWidth("88");//this is silly

		//draw the beginning frame number at the left bottom part of the screen
        g.drawString( left_x, 1, (int)(windowSize.getHeight())-1 );

		//draw the end frame number at the bottom right.
		width = metrics.stringWidth(right_x);
		g.drawString( right_x, (int)(windowSize.getWidth()-width),(int)(windowSize.getHeight())-1);



 		//this section draws little red boxes and labels on the left of each trace.
		  for (int j=1;j<cols;j++){
		   g.setColor(Color.red);
		   rect[j-1].setLocation(1,tg.getTrace(j-1).getZero());
		   g.fill(rect[j-1]);
		   g.setColor(Color.white);
		   g.drawString(""+j,1,tg.getTrace(j-1).getZero()+height/2+3);
		   }

         //draw the navigator/scale toggle button at the top left.
          g.setColor(Color.blue);
          g.fill(NavigatorButton);
          g.setColor(Color.white);
          String tmpstr;
          if (NAVIGATE)tmpstr="s"; else tmpstr="N";
          g.drawString(tmpstr,1,7);

        //draw the red cursor, and a number at the bottom indicating the frame number.

	 	if ((user_start >x_offset)&&(user_start <x_offset+x_range))	{
		 String frame=""+UserFrameNumber;
	 	 g.setColor(Color.red);
	 	 g.drawLine(UserPosition,0, UserPosition,(int)(windowSize.getHeight()-10));
		 width=metrics.stringWidth(frame);
		 g.setColor(Color.lightGray);
		 g.fillRect((int)(UserPosition-width/2),(int)(windowSize.getHeight())-height,width,height);
		 g.setColor(Color.white);
		 g.drawString(frame,(int)(UserPosition-width/2),(int)(windowSize.getHeight())-1);
		}

		//draw each user cursor
        if (CursorVector!=null){
		int minfront=Integer.MAX_VALUE;
	    int minback=Integer.MAX_VALUE;
		for (int j=0;j<CursorVector.size();j++){
	     gvdecoder.trace.Cursor cursor=(gvdecoder.trace.Cursor)(CursorVector.elementAt(j));
	     cursor.paintCursor(g,RelativeStart,x_range,x_scale,windowSize,GenericWidth,height,SHOWALLNOTES);

	     if (SelectedCursor!=null){

				 if (cursor!=SelectedCursor){
					 int dif=SelectedCursor.position-cursor.position;
					 if (dif>0){
						  if (dif<minback){
							  minback=dif;
						     }
					 }else{
						 dif=-1*dif;
						 if (dif<minfront){
							  minfront=dif;
					         }
				     }//else
			 }//if
		    }//selected

		  }//j
         g.setColor(Color.black);
	     if (minfront!=Integer.MAX_VALUE){

			 String mf=minfront+">";
			 g.drawString(mf,(int)(SelectedCursor.scaledposition+10),8);
			 }
		 if (minback!=Integer.MAX_VALUE){
			 String mb="<"+minback;
			 int ws=metrics.stringWidth(mb);
			 g.drawString(mb,(int)(SelectedCursor.scaledposition-(ws+6)),8);
			 }
        }//!null



        if (SelectedRoi>0){}
         //DetectorManager.getInstance().paint(g,tg.getTrace(SelectedRoi-1),RelativeStart,x_range,x_scale,windowSize,GenericWidth,height);
		 //DetectorManager.getInstance().paint(g,tg.getTrace(SelectedRoi-1),x_offset,x_range,x_scale,windowSize,GenericWidth,height);

		//Paint a rectangle at user's dragged selection.

        if (showSelection){
           // System.out.println("in show selection");
        if (!invertSelection){
		    g.setColor(Color.yellow);
            g.drawRect(selectionRect.x,selectionRect.y,selectionRect.width,selectionRect.height);
            g.setColor(Color.black);
	        }else
	        {
		    if (selectionRect.width<-SCALE_INVERSE_MAXDIST) g.setColor(Color.white);
		    else g.setColor(Color.blue);
		    g.drawLine(selectionRect.x,selectionRect.y,selectionRect.x+selectionRect.width,selectionRect.y);
            g.setColor(Color.red);
            //System.out.println("x="+selectionRect.x+" width="+selectionRect.width);
            int midpt=(int)((selectionRect.x+selectionRect.x+selectionRect.width)/2);
            g.drawLine(midpt+SCALE_INVERSE_MINDIST/2,selectionRect.y,midpt-SCALE_INVERSE_MINDIST/2,selectionRect.y);
            g.setColor(Color.black);
		  }
	   }
	  else {
		//  System.out.println("showSelection is false");
	  }
	 }





}



/* actions controlled here... */

class menuListener implements ActionListener{
 NavigatorGraph sg;

 public menuListener(NavigatorGraph sg){
  this.sg=sg;
  }

 //gets popup events
 public void actionPerformed(ActionEvent e) {
        JMenuItem source = (JMenuItem)(e.getSource());
        String s = "Action event detected."+ "    Event source: " + source.getText();
        System.out.println(s);
		if (source.getText().equals("set flip")){
		  if (sg.tg.tp.FlipData) sg.tg.tp.FlipData=false; else sg.tg.tp.FlipData=true;

		}

		if (source.getText().equals("flip")){
		 for (int i=0;i<sg.tg.Traces.size();i++){
		     Trace tmp=(Trace)sg.tg.Traces.get(i);
		     tmp.flipdata();
		 }

		 sg.repaint(0,0,(int)sg.windowSize.getWidth(),(int) sg.windowSize.getHeight());
		 sg.rescale();
		}
		if (source.getText().equals("jump to")){
		if (sg.vw!=null)
		  sg.vw.JumpToFrame(sg.user_start);
		}
		if (source.getText().equals("add cursor")){
		sg.addCursor();
	    }

        if (source.getText().equals("edit note")){
			sg.editNote();
		}
		if (source.getText().equals("delete note")){
		  sg.deleteNote();
		}
		if (source.getText().equals("show all notes")){
		  sg.SHOWALLNOTES=true;
		}
		if (source.getText().equals("hide all notes")){
		  sg.SHOWALLNOTES=false;
		}

	    if (source.getText().equals("show all")){
				if (sg.SHOWALL) sg.SHOWALL=false;
				else sg.SHOWALL=true;
				sg.repaint();
	    }

	    if (source.getText().equals("scale y")){
			if (sg.tg.holdYscale) sg.tg.holdYscale=false;
				else sg.tg.holdYscale=true;
				sg.scale(sg.x_offset);
			    sg.repaint();
	    }

        if (source.getText().equals("link viewer")){
			sg.vw=sg.gv.jh.presentviewer;
			sg.vw.nav=sg;
			sg.mif.setTitle("linked: "+sg.vw.filename);
	    }

	    if (source.getText().equals("remove cursor")){
				sg.removeCursor();
	    }
	    if (source.getText().equals("add tag")){
				sg.addTag();
	    }

	    if (source.getText().equals("move tag")){
						sg.moveTag();
	    }
	    if (source.getText().equals("start detector")){
			sg.startDetector();
	    }
	    if (source.getText().equals("run detector")){
					sg.runDetector();
	    }
		if (source.getText().equals("navigate")){
		 sg.NAVIGATE=true; sg.repaint();
		}
		if (source.getText().equals("scale")){
				 sg.NAVIGATE=false; sg.repaint();
		}
		if (source.getText().equals("recalculate")){
         sg.tg.reCalculateAllTransformations();
         sg.scale(sg.x_offset);
		 sg.repaint();
		}
		if (source.getText().equals("save")){
		         sg.savesettings();
		}
		if (source.getText().equals("load")){
		         sg.loadsettings();
		}
		if (source.getText().equals("save image")){
		 		sg.saveImage();
		}
		if (source.getText().equals("range->Matrix")){
				 		sg.vw.rangeToMatrix();
		}
		if (source.getText().equals("to clipboard")){
						 		sg.toClipboard();
		}
		if (source.getText().equals("load next")){
				         sg.loadnext();
		}
		if (source.getText().equals("selected")){
		 if (sg.rois!=null){
		  ((ROI)sg.rois.get(sg.SelectedRoi-1)).scale=1.0;
		  ((ROI)sg.rois.get(sg.SelectedRoi-1)).offset=0;
		 }
		 sg.scale(sg.x_offset);
		 sg.repaint();
		 }
		 if (source.getText().equals("recenter")){
                sg.tg.tp.Absolute=false;
                sg.scale(sg.x_offset);
		        sg.repaint();
			 }
		  if (source.getText().equals("absolute")){

              sg.tg.tp.Absolute=true;
			  sg.scale(sg.x_offset);
		      sg.repaint();
		  }

		if (source.getText().equals("ptolemy")){
		 //PtolemyPlot pp=new PtolemyPlot(sg);
		}
		if (source.getText().equals("print")){
		 PrintUtilities.printComponent(sg);

		}
		if (source.getText().equals("process rois")){
		System.out.println("trying to process "+sg.lastRange.getWidth()+" "+((int)sg.lastRange.getWidth()+sg.lastRange.getHeight()));
		if (sg.vw!=null){
		 int _st=0;
		 int _en=1;
		 if (sg.x_offset==0){
		  _st=(int)sg.lastRange.getWidth();
		  _en=(int)sg.lastRange.getWidth()+(int)sg.lastRange.getHeight();
	     }else{
		  _st=sg.x_offset;
		  _en=sg.x_offset+sg.x_range;
		 }
	      sg.vw.processRois(_st,_en);
	      if (sg.ISBLANK){
			  int[] data=sg.vw.roi_t[1];
			  Trace tr=sg.tg.getTrace(0);
			  int trlength=tr.y_values.length;
			  double[] newtracedata=new double[trlength];
			  for (int i=0;i<_st;i++){
				  newtracedata[i]=data[0];
			  }
			  for (int j=_st;j<_en;j++){
				  newtracedata[j]=data[j-_st];
			  }
			  for (int k=_en;k<trlength;k++){
			    newtracedata[k]=data[_en-_st-1];
			  }

			 Trace newtrace=new Trace(newtracedata);
			 sg.addTrace(newtrace,(ROI)sg.vw.jp.rois.get(0));
			 sg.vw.nav=sg;

		  }
	      }//if
		}
		if (source.getText().equals("clear traces")){
			sg.clearTraces();
		}

        if (source.getText().equals("clear selected")){
			sg.clearSelected();
		}

		if (source.getText().equals("save Navigator")){
					sg.saveNavigator();
		}
		if (source.getText().equals("all")){
		 sg.tg.tp.Absolute=false;
		 if (sg.rois!=null){
		  for (int i=0;i<sg.rois.size();i++){
		  ((ROI)sg.rois.get(i)).scale=1.0;
		  ((ROI)sg.rois.get(i)).offset=0;

		  System.out.println("Rescaled "+i);
		  }
		 }
		 sg.scale(sg.x_offset);
		 sg.repaint();
		}
		if (source.getText().indexOf("ROI")>0){
		  System.out.println("found roi...");
		  if (sg.rois!=null){
		  for (int i=0;i<sg.rois.size();i++) {
		   String tmp="ROI "+(i+1);
		   System.out.println("looking for "+tmp);
		   if (source.getText().indexOf(tmp)>0){ sg.SelectedRoi=i+1;  sg.repaint(); break;}

		  }
		 }
		}
   }

}


class sgListener extends MouseInputAdapter {
	NavigatorGraph sg;
	int x;
	int y;
	boolean dragged=false;
	boolean foundselected=false;
	boolean foundselectedcursor=false;
	boolean foundselectedtag=false;
	boolean foundselectedmaxrect=false;
	boolean foundselectedminrect=false;
	boolean CHANGESCALE=false;
	boolean blockselecetedtracescale=false;

	public sgListener(NavigatorGraph sg){
		   this.sg=sg;
		}

	public void mousePressed(MouseEvent e) {
	 //check for hit in selected rect...
	 foundselected=false;
	 blockselecetedtracescale=false;
	 for (int i=0;i<sg.rect.length;i++){
	  if(sg.rect[i].contains(e.getX(), e.getY())) {
	   sg.SelectedRoi=i+1;
	   System.out.println("Selected "+i);
	   foundselected=true;
	   foundselectedtag=false;
	   x=e.getX();
	   y=e.getY();
	   sg.repaint();
	   break;
	  }
	 }

	if(sg.CursorVector!=null){
	for (int j=0;j<sg.CursorVector.size();j++){
	 gvdecoder.trace.Cursor tmp=(gvdecoder.trace.Cursor)sg.CursorVector.elementAt(j);
	 if (tmp.SelectRect.contains(e.getX(), e.getY())){
		sg.SelectedCursor=tmp;

		foundselectedcursor=true;
		foundselectedtag=false;
		foundselectedmaxrect=false;
		foundselectedminrect=false;
		foundselected=false;//try to avoid rescaling a trace now.
		sg.SelectedRoi=0;//again, ensure it can't scale a trace.
		blockselecetedtracescale=true;
		x=e.getX();
		y=e.getY();
		//if((e.getModifiers() & InputEvent.BUTTON3_MASK)== InputEvent.BUTTON3_MASK){
		//  System.out.println("CursorPopup should be shown");
	    //  sg.cursorpopup.show(e.getComponent(), x, y);
	    //  return;
	    //}else{
		sg.CursorIsSelected(true,x,y);
	    //}
		sg.repaint();
		break;
    	}
    }
   }
   if (sg.SelectedRoi>0){
      blockselecetedtracescale = DetectorManager.getInstance().findSelectedTag(sg.tg,sg.tg.getTrace(sg.SelectedRoi-1),e.getX(),e.getY());
    }
   if (sg.NavigatorButton.contains(e.getX(),e.getY())){
   	 if (sg.NAVIGATE) sg.NAVIGATE=false; else sg.NAVIGATE=true;
   	 System.out.println("toggled navigator state");
   	 sg.repaint();
	}
   if (!((foundselected)||(foundselectedcursor)||(foundselectedtag))){
	 if((e.getModifiers() & InputEvent.BUTTON1_MASK)== InputEvent.BUTTON1_MASK){
		  x = e.getX();
		  y = e.getY();
		  sg.resetSelection(x,y,x+1,y+1);
		  //System.out.println("pressed");
		  sg.repaint();
		  }
	    }
	    maybeShowPopup(e);
	}

	  public void mouseDragged(MouseEvent e) {
		if (sg.NAVIGATE){
		if (!foundselected){
		 if((e.getModifiers() & InputEvent.BUTTON1_MASK)== InputEvent.BUTTON1_MASK){
			sg.resetSelection(x,y,e.getX(),e.getY());
			sg.repaint();
			dragged=true;
		 }
		 }
		}
		else{
		 if ((sg.rois!=null)&&(sg.SelectedRoi>0)&&(!blockselecetedtracescale)){
		  ROI tmp=(ROI)sg.rois.get(sg.SelectedRoi-1);
	      //determine if the user is scaling in y direction or changing offset.
          if ((CHANGESCALE) || (Math.abs(x-e.getX())>Math.abs(y-e.getY())+1)){

			  CHANGESCALE=true;
			  Trace trace=sg.tg.getTrace(sg.SelectedRoi-1);
			  trace.adjustScale(x,e.getX());
		  }else{//change offset
			  tmp.offset=tmp.offset+y-e.getY();
			  Trace trace=sg.tg.getTrace(sg.SelectedRoi-1);
			  trace.shiftOffset(y,e.getY());
	      }
		  y=e.getY();
		  x=e.getX();
         sg.repaint();
		 }
		else
		if (foundselectedcursor){
		 sg.SelectedCursor.position=new Integer(sg.tg.raw_x[sg.x_offset]+(int)(e.getX()*sg.x_scale));
	     if (sg.SelectedCursor.trace!=null){
	       sg.SelectedCursor.y_value=new Double(sg.SelectedCursor.trace.raw_y[sg.SelectedCursor.position.intValue()]);
	       sg.SelectedCursor.slope_value=new Double(  (sg.SelectedCursor.trace.raw_y[sg.SelectedCursor.position.intValue()+1]-sg.SelectedCursor.trace.raw_y[sg.SelectedCursor.position.intValue()]) );
	       sg.gv.ccf.cc.info=new String[4];
	       sg.gv.ccf.cc.info[0]="";
	       sg.gv.ccf.cc.info[1]=("pos "+sg.SelectedCursor.position.toString());
	       sg.gv.ccf.cc.info[2]=("val "+sg.SelectedCursor.y_value.toString());
	       sg.gv.ccf.cc.info[3]=("m   "+sg.SelectedCursor.slope_value.toString());
	       sg.gv.ccf.cc.CursorData.repaint();
	       }
		 sg.repaint();
		}
		else
		if (sg.SelectedRoi>0){
		  DetectorManager.getInstance().dragSelected( e.getX(), e.getY(), sg.tg.raw_x[sg.x_offset], sg.x_scale);
		  sg.repaint();
	     }
		}
	  }

  public void mouseReleased(MouseEvent e) {
	CHANGESCALE=false;
	//foundselectedcursor=false;
	foundselectedtag=false;
	foundselectedmaxrect=false;
	foundselectedminrect=false;
	blockselecetedtracescale=false;
	sg.CursorIsSelected(false,0,0);
    if (!foundselected){
		if((e.getModifiers() & InputEvent.BUTTON1_MASK)== InputEvent.BUTTON1_MASK){
			if (dragged){
			sg.showSelection=false;
			sg.rescaleToSelection();
			}
			else{
			sg.setPoint(x,y);
			}
			dragged=false;
		}
		maybeShowPopup(e);
		foundselectedcursor=false; //this needs to be called after maybeshowpopup
     }
    sg.requestFocus();
  }

 private void maybeShowPopup(MouseEvent e) {
			  //System.out.println("checking popup");

			  if((e.getModifiers() & InputEvent.BUTTON3_MASK)== InputEvent.BUTTON3_MASK){
				 if (foundselectedcursor)
				  sg.cursorpopup.show(e.getComponent(),e.getX(),e.getY());
				  else
				 sg.popup.show(e.getComponent(), e.getX(), e.getY());
			  }
		     }

    }