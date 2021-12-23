import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.awt.geom.*;
import java.awt.Graphics2D.*;
import java.util.*;
import java.awt.image.*;
import java.awt.image.renderable.*;
import utilities.*;
import prefs.*;
import gvdecoder.trace.*;
import ij.process.*;
import ij.*;

public class Viewer2 extends JInternalFrame implements ActionListener, ImageViewer{
    ViewerParams vp;
    int ROIMODE=2;
    int FREEFORMROI=1;
    int RECTANGLEROI=2;

    int DRAGMODE=1;
    int DRAWROI=1;
    int DRAWRULER=2;

    public int frameNumber = -1;
    javax.swing.Timer timer;
    boolean frozen = true;
    JLabel label;
	public ImagePanel jp;
	SPEInfo speinfo;
	String filetype;
	public ImageDecoder im;
	public int[] viewArray;//the scaled image mod 256 etc.
	public int[] datArray; //actual data
	public double[] normalize=null;
	public double[] background=null;
	public int offset;     //conversion from data to view
	public double scale;   //conversion from data to view (z)
	public double UserScale=1.0; //this is for z scaling
	public int UserOffset=0;
	Dimension windowSize;
	public float viewScale;  //calculated value for x,y scaling
	int view_X_dim; //view X_dim
	int view_Y_dim;
	public int X_dim;      //data X_dim
	public int Y_dim;
	public int instance;
	//public static String filename;
	//public static String absolutefilename;
	public String filename;
	public String absolutefilename;
	GView ifd; //contains this
	NavigatorGraph nav;    //associated with this file
	NavigatorGraph lastCreatedNavigator=null; //associated with this file
	int Navigator_start=0; //the navigator object can set these values to define start and end frames of interest
	int Navigator_end=0;   // "
	JPopupMenu popup;
	JPopupMenu popupROI;
    public int lastMouseX;
	public int lastMouseY;
	public int lastCoordX;
	public int lastCoordY;
	Vector FrameListeners;
	int LastUserSetFrame=0;
    boolean AUTOSCALE=true; //toggle scale for x,y
    float UserSelectedScale=1.0f; //user selected scale for x,y

	public void SetupWindow(){
		addInternalFrameListener(new InternalFrameAdapter() {
		            public void internalFrameIconified(WindowEvent e) {
		                stopAnimation();
		            }
		            public void internalFrameDeiconified(WindowEvent e) {
		                startAnimation();
		            }
		            public void internalFrameClosing(WindowEvent e) {
		                stopAnimation();
		            }

				  public void internalFrameClosed(InternalFrameEvent e) {
					 stopAnimation();
					 im.CloseImageFile(instance);
					 System.out.println("internal frame closed");
				  }
		        });


		jp=new ImagePanel(viewArray,X_dim,Y_dim,viewScale);



		JAIControlPanel cp=new JAIControlPanel(this);
		PreprocessControlPanel pp=new PreprocessControlPanel(this);
		JPanel MainControlPanel=new JPanel();

		Border lineBorder=BorderFactory.createLineBorder(Color.blue,1);
	    MainControlPanel.setBorder(lineBorder);
	    MainControlPanel.setLayout(new BoxLayout(MainControlPanel, BoxLayout.X_AXIS));


	    MainControlPanel.add(pp);
	    MainControlPanel.add(cp);
		setViewScale();
		JPanel contentPane = new JPanel();
		contentPane.setLayout(new BoxLayout(contentPane,BoxLayout.Y_AXIS));





		JPanel toolBar = new MediaButtons(this,true,true,true,true,true);

		toolBar.setAlignmentY(Component.TOP_ALIGNMENT);
		//add scroll pane here if appropriate.

	    if (vp.UseScrollPane){
	    JScrollPane scrollPane = new JScrollPane(jp);
		contentPane.add(scrollPane);
	    }else contentPane.add(jp);
	    contentPane.add(toolBar);



		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
		                                              MainControlPanel,
		                                              contentPane);
		        splitPane.setOneTouchExpandable(true);
		        splitPane.setDividerLocation(vp.DividerLocation);

	   		getContentPane().add(splitPane, BorderLayout.CENTER);
	        //setJMenuBar(createMenuBar());
            popup=new JPopupMenu();
		//System.out.println("3");
		  ViewMenuListener myViewMenuListener=new ViewMenuListener(this);
		  viListener vilistener = new viListener(this);

				  JMenuItem menuItem = new JMenuItem("quicktrace");
				  menuItem.addActionListener(myViewMenuListener);
				  popup.add(menuItem);
				  menuItem = new JMenuItem("create navigator");
				  menuItem.addActionListener(myViewMenuListener);
				  popup.add(menuItem);
				  menuItem = new JMenuItem("toggle background");
				  menuItem.addActionListener(myViewMenuListener);
				  popup.add(menuItem);
				  menuItem = new JMenuItem("toggle ruler|roi");
				  menuItem.addActionListener(myViewMenuListener);
				  popup.add(menuItem);
				  menuItem = new JMenuItem("rescale");
				  menuItem.addActionListener(myViewMenuListener);
				  popup.add(menuItem);

				  JMenu submenu = new JMenu("export");
				  submenu.setMnemonic(KeyEvent.VK_E);
					  menuItem = new JMenuItem("save jpgs");
					  menuItem.addActionListener(myViewMenuListener);
					   submenu.add(menuItem);
					  menuItem = new JMenuItem("save bmps");
					  menuItem.addActionListener(myViewMenuListener);
					  submenu.add(menuItem);
					  menuItem = new JMenuItem("save avi");
					  menuItem.addActionListener(myViewMenuListener);
					  submenu.add(menuItem);
				  popup.add(submenu);

				   submenu = new JMenu("view");
				  	          submenu.setMnemonic(KeyEvent.VK_M);
				  	          ButtonGroup group = new ButtonGroup();
				  	          JRadioButtonMenuItem rbMenuItem = new JRadioButtonMenuItem("2.0x");
							  group.add(rbMenuItem);
							  rbMenuItem.addActionListener(myViewMenuListener);
				  			  submenu.add(rbMenuItem);
				  	          rbMenuItem = new JRadioButtonMenuItem("1.0x");
							  rbMenuItem.setSelected(true);
							  group.add(rbMenuItem);
							  rbMenuItem.addActionListener(myViewMenuListener);
							  submenu.add(rbMenuItem);
							  rbMenuItem = new JRadioButtonMenuItem("0.5x");
							  group.add(rbMenuItem);
							  rbMenuItem.addActionListener(myViewMenuListener);
							  submenu.add(rbMenuItem);
							  rbMenuItem = new JRadioButtonMenuItem("0.25x");
							  group.add(rbMenuItem);
							  rbMenuItem.addActionListener(myViewMenuListener);
							  submenu.add(rbMenuItem);
							  rbMenuItem = new JRadioButtonMenuItem("auto");
							  group.add(rbMenuItem);
							  rbMenuItem.addActionListener(myViewMenuListener);
							  submenu.add(rbMenuItem);

		 popup.add(submenu);

		 submenu = new JMenu("interpolate");
				  	          submenu.setMnemonic(KeyEvent.VK_I);
				  	          ButtonGroup group2 = new ButtonGroup();

				  	          rbMenuItem = new JRadioButtonMenuItem("nearest");
							  rbMenuItem.setSelected(true);
							  group2.add(rbMenuItem);
							  rbMenuItem.addActionListener(myViewMenuListener);
							  submenu.add(rbMenuItem);
							  rbMenuItem = new JRadioButtonMenuItem("bilinear");
							  group2.add(rbMenuItem);
							  rbMenuItem.addActionListener(myViewMenuListener);
							  submenu.add(rbMenuItem);
							  rbMenuItem = new JRadioButtonMenuItem("bicubic");
							  group2.add(rbMenuItem);
							  rbMenuItem.addActionListener(myViewMenuListener);
							  submenu.add(rbMenuItem);
							  rbMenuItem = new JRadioButtonMenuItem("bicubic2");
							  group2.add(rbMenuItem);
							  rbMenuItem.addActionListener(myViewMenuListener);
							  submenu.add(rbMenuItem);

		 popup.add(submenu);

		 submenu=new JMenu("roi");
		 submenu.setMnemonic(KeyEvent.VK_R);

				  menuItem = new JMenuItem("delete roi");
				  menuItem.addActionListener(myViewMenuListener);
				  submenu.add(menuItem);
				  menuItem = new JMenuItem("process rois");
					menuItem.addActionListener(myViewMenuListener);
					submenu.add(menuItem);
				  menuItem = new JMenuItem("color rois");
					menuItem.addActionListener(myViewMenuListener);
					submenu.add(menuItem);
				  menuItem = new JMenuItem("toggle rect/poly");
				  	menuItem.addActionListener(myViewMenuListener);
					submenu.add(menuItem);

				  menuItem=new JMenuItem("unit roi");
					menuItem.addActionListener(myViewMenuListener);
					submenu.add(menuItem);
					menuItem=new JMenuItem("fast unit roi");
										menuItem.addActionListener(myViewMenuListener);
					submenu.add(menuItem);

					menuItem=new JMenuItem("delete all");
					menuItem.addActionListener(myViewMenuListener);
					submenu.add(menuItem);

					menuItem=new JMenuItem("save rois");
					menuItem.addActionListener(myViewMenuListener);
					submenu.add(menuItem);


					menuItem=new JMenuItem("load rois");
					menuItem.addActionListener(myViewMenuListener);
					submenu.add(menuItem);
		    popup.add(submenu);

				  menuItem = new JMenuItem("toggle filter");
				  menuItem.addActionListener(myViewMenuListener);
				  popup.add(menuItem);
				  menuItem = new JMenuItem("next filter");
				  menuItem.addActionListener(myViewMenuListener);
				  popup.add(menuItem);


                  menuItem=new JMenuItem("save image");
				  menuItem.addActionListener(myViewMenuListener);
                  popup.add(menuItem);

                  popupROI=new JPopupMenu();
                  menuItem=new JMenuItem("close roi");
                  menuItem.addActionListener(myViewMenuListener);
                  popupROI.add(menuItem);
                  menuItem=new JMenuItem("delete last");
                  menuItem.addActionListener(myViewMenuListener);
                  popupROI.add(menuItem);


				  jp.addMouseListener(vilistener);
			      jp.addMouseMotionListener(vilistener);

	   windowSize=this.getSize();
	   JumpToFrame(0);

       if (vp.AutoLoadLastRois) loadROIs();

       if (vp.AutoProcessLoadedRois) processRois(0,-1);

       //send information to JythonHelper
       ifd.jh.setViewer(this);


	}

	public Viewer2(GView ifd, int fps, String filename, String absolutefilename, ImageDecoder id){
		 super(filename,
					 true, //resizable
					 true, //closable
					 true, //maximizable
			  true);//iconifiable
		vp=ViewerParams.getInstance();
		this.absolutefilename=absolutefilename;
		this.filename=filename;
        this.ifd=ifd;
        this.im=id;
        instance=0;
        setFPS(fps);
        FrameListeners=new Vector();

        int widthtmp=vp.InitialWidth;
		int heighttmp=vp.InitialHeight;
	    setSize(new Dimension(widthtmp,heighttmp));
	    setLocation(vp.InitialX,vp.InitialY);

		int[] tmpdat=new int[4];
		im.ReturnXYBandsFrames(tmpdat,instance);
		X_dim=tmpdat[0];
		Y_dim=tmpdat[1];
		speinfo=new SPEInfo(X_dim,Y_dim,tmpdat[3],"","tmp.dat");
		datArray=new int[X_dim*Y_dim];
		windowSize=this.getSize();
		viewArray=new int[X_dim*Y_dim];
		System.out.println("going to setupwindow");
	    SetupWindow();
		System.out.println("out of setupwindow");
	}

    public Viewer2(GView ifd, int fps, String filename, String absolutefilename, String filetype) {
       super(filename,
	                 true, //resizable
	                 true, //closable
	                 true, //maximizable
              true);//iconifiable
        vp=ViewerParams.getInstance();
		this.absolutefilename=absolutefilename;
		this.filename=filename;
        this.ifd=ifd;
        this.filetype=filetype;
        System.out.println("debug:"+filetype);
        setFPS(fps);


        int widthtmp=vp.InitialWidth;
        int heighttmp=vp.InitialHeight;
		setSize(new Dimension(widthtmp,heighttmp));

		        //Set the window's location.
        setLocation(vp.InitialX,vp.InitialY);

	   im=ImageDecoderFactory.getDecoder(filetype);
       instance=im.OpenImageFile(absolutefilename);

	   FrameListeners=new Vector();

	   if (filetype.equals("spe")){
	   speinfo=SPEUtils.getSPEInfo(absolutefilename);
	   System.out.println(speinfo);
	   System.out.println("the nav file is "+speinfo.NavFile);
	   X_dim=speinfo.X_dim;
	   Y_dim=speinfo.Y_dim;
	   }
	   else{
	   int[] tmpdat=new int[4];
	   System.out.println("debug before returnXYBandsFrames(tmpdat)");
	   im.ReturnXYBandsFrames(tmpdat,instance);
   	   System.out.println("debug after returnXYBandsFrames(tmpdat)");

	   X_dim=tmpdat[0];
	   Y_dim=tmpdat[1];
	   speinfo=new SPEInfo(X_dim,Y_dim,tmpdat[3],"","tmp.dat");
	   }

	   datArray=new int[X_dim*Y_dim];
	   windowSize=this.getSize();
	   viewArray=new int[X_dim*Y_dim];
       SetupWindow();

	}



	 public void addFrameListener(FrameListener obj){
	  System.out.println("added a frame listener");
	  FrameListeners.add(obj);
	 }

	 public void removeFrameListener(FrameListener obj){
	  System.out.println("removed a frame listener");
	  FrameListeners.remove(obj);
	 }

	 public void NotifyFrameListeners(int framenum){

	  for (int i=0;i<FrameListeners.size();i++){

	   ((FrameListener)FrameListeners.elementAt(i)).SetFrame(framenum);
	  }

	 }


	protected JMenuBar createMenuBar() {
	        JMenuBar menuBar = new JMenuBar();
	        JMenu menu = new JMenu("file");

	        menu.setMnemonic(KeyEvent.VK_N);
	        JMenuItem menuItem = new JMenuItem("New Navigator");
	        menuItem.setMnemonic(KeyEvent.VK_N);
	        menuItem.addActionListener(new ActionListener() {
	            public void actionPerformed(ActionEvent e) {
	                createNavigator();
	            }
        });

	menu.add(menuItem);
	menuBar.add(menu);
	return menuBar;
	}

    /** ImageViewer Interface **/
    public void Start(){
	 frozen=false;
	 startAnimation();
	}
	public void Stop(){
	 frozen=true;
	 stopAnimation();
	}
	public void Rewind(){
	 JumpToFrame(LastUserSetFrame);
	}

	public void AdvanceOneFrame(){
	 frozen=true;
	 stopAnimation();
	 AdvanceSingleFrame();

	}

	public void BackOneFrame(){
	 frozen=true;
	 stopAnimation();
	 BackSingleFrame();

	}



	public void setFPS(int fps){
	 int delay = (fps > 0) ? (1000 / fps) : 100;
	 if (timer==null) timer = new javax.swing.Timer(delay, this);
	 else timer.setDelay(delay);
	 timer.setInitialDelay(0);
	 timer.setCoalesce(true);
	}

	public void updateTitle(){

	lastCoordX=(int)((float)lastMouseX/viewScale);
	lastCoordY=(int)((float)lastMouseY/viewScale);
	int val=datArray[lastCoordY*X_dim+lastCoordX];
	String title=filename+" ("+lastCoordX+","+lastCoordY+")="+val;
	setTitle(title);
	}

	public void processRois(int startrange, int endrange){
		System.out.println("in processRois");
		String resultfile=filename+".roi";
		 try{
		    int[][] roi=new int[jp.rois.size()][];
		 	for (int i=0;i<jp.rois.size();i++){
		 	  ((ROI)jp.rois.get(i)).findAllPixels(X_dim,Y_dim);
			   roi[i]=((ROI)jp.rois.get(i)).arrs;
		 	  }

		 	if (vp.AutoSaveChartAsNavigator)
		 	  resultfile=FileNameManager.getInstance().FindNavFileName(filename,"");
		 	im.SumROIs(roi,resultfile,startrange,endrange,instance);
			}
		catch(Exception e){System.out.println("couldn't find roi"); e.printStackTrace();}
		 MyInternalFrame frame = new MyInternalFrame(resultfile, resultfile,this, jp.rois);
				    addFrameListener(frame.getNavigator());
				    lastCreatedNavigator=frame.getNavigator();
					frame.setVisible(true); //necessary as of kestrel
			        ifd.desktop.add(frame);
			        try {
			            frame.setSelected(true);
	        } catch (java.beans.PropertyVetoException e) {}
		System.out.println("exit processRois");
		}


   public int[] returnRawPixel(int x, int y, int startframe, int endframe){

	   int[] result = new int[endframe-startframe];
	   int resetToFrame=frameNumber;
	   int i=0;
	   try{
	   for ( i=startframe;i<endframe;i++){
		   im.JumpToFrame(i, instance);
           im.UpdateImageArray(datArray,speinfo.X_dim,speinfo.Y_dim,instance);
		   int val=datArray[y*speinfo.X_dim+x];
		   result[i]=val;
	   }
       }catch (Exception e){ System.out.println("i="+i+" x="+x+" y="+y+" xdim="+speinfo.X_dim);e.printStackTrace();}
	   im.JumpToFrame(resetToFrame,instance);
	   return result;
   }

  public void drawOnNavigator(int x, int y){
  int xloc=(int)(x/viewScale);
  int yloc=(int)(y/viewScale);
  if (nav!=null){
	  nav.tg.RemoveTrace(0);
	  nav.tg.AddTrace(0,returnRawPixel(xloc,yloc,0,2800));
	  nav.scale(nav.x_offset);
      nav.repaint();

   }

  }
	 protected void createNavigator() {
		   System.out.println("debug: in create navigator, know filetype is "+filetype+" and this reads "+filetype.equals("log"));

		   String absnavfilename="";
		   if (filetype.equals("log")) {
		   			   absnavfilename=FileNameManager.getInstance().FindNavFileName(absolutefilename,"log");
		   			   System.out.println("debug: in createNavigator, filtype detected as log");
		     }
			else if (filetype.equals("ompro")){
				 absnavfilename="navfiles"+File.separator+filename+".nav";
				  System.out.println("debug: in createNavigator, think type=ompro, nav name="+absnavfilename);
			}

		   else if (speinfo!=null) {
			   absnavfilename=ifd.pv.getStringProperty("navdir",".")+File.separator+speinfo.NavFile;
		       System.out.println("debug: in createNavigator, think type=spe");
		     }

		    else
		    {
		   absnavfilename=ifd.pv.getStringProperty("navdir",".")+File.separator+"tmp.nav";
	        System.out.println("debug: in createNavigator, filetype default detected");
	        }
		   try{
		    File tmp=new File(absnavfilename);
			if (!tmp.exists()){
			 //create?
			 //public native int SumROIs(int[][] rois, String outfile, int startframe, int endframe,int instance);
	         System.out.println("trying to create... "+absnavfilename);
			 int[][] roi=new int[1][4];
			 roi[0][0]=(int)((X_dim/2)*Y_dim+(X_dim/2));
			 roi[0][1]=(int)((X_dim/2)*Y_dim+(X_dim/2))+1;
			 roi[0][2]=(int)(((X_dim/2)+1)*Y_dim+(X_dim/2));
			 roi[0][3]=(int)(((X_dim/2)+1)*Y_dim+(X_dim/2))+1;
			 im.SumROIs(roi,absnavfilename,0,-1,instance);
			 }
			}catch(Exception e){e.printStackTrace();}
	        MyInternalFrame frame;
	        if (filetype.equals("log")) openNavigatorWindow(absnavfilename,absnavfilename);
		    else if (filetype.equals("ompro")) openNavigatorWindow(absnavfilename, absnavfilename);
		    else if (speinfo!=null)  openNavigatorWindow(absnavfilename,speinfo.NavFile);
		    else openNavigatorWindow(absnavfilename,"tmp.nav");

          }

    public void openNavigatorWindow(String absnavfilename, String filename){
            MyInternalFrame frame;
            if (filetype.equals("log")) {
				jp.rois.add(new ROI(this,Color.blue));
				jp.rois.add(new ROI(this,Color.green));
				jp.rois.add(new ROI(this,Color.red));
				jp.rois.add(new ROI(this,Color.magenta));
				jp.rois.add(new ROI(this,Color.yellow));

			    frame=new MyInternalFrame(absnavfilename,filename,this,jp.rois);
			    jp.rois=null;
			    jp.rois=new ArrayList();
			    }
			else frame=new MyInternalFrame(absnavfilename,filename,this,null);
		    nav=frame.getNavigator();
			addFrameListener(nav);
			frame.setVisible(true); //necessary as of kestrel
	        ifd.desktop.add(frame);
	        try {
	            frame.setSelected(true);
	        } catch (java.beans.PropertyVetoException e){};

	}

    public void findScaleOffset(){
	    int max=Integer.MIN_VALUE;
	 	int min=Integer.MAX_VALUE;
	 	{
	 	for (int i=0;i<datArray.length;i++){
	 	 if (datArray[i]<min) min=datArray[i];
	 	 if (datArray[i]>max) max=datArray[i];
	 	 }
	    }
	    if (normalize!=null){
			offset=min;
			max=Integer.MIN_VALUE;
			min=Integer.MAX_VALUE;
	 	 for (int i=0;i<datArray.length;i++){
		 int val=(int)(((double)datArray[i]-background[i])*normalize[i]);
	 	 if (val<min) min=val;
	 	 if (val>max) max=val;
	 	 }
	    }
	     scale=((double)255)/((double)(max-min));
		 scale=scale*0.8;
	     offset=min;
	     System.out.println("in findScaleOffset... max = "+max+" min = "+min+ " scale ="+scale+" offset ="+offset);

	}
    public void ToggleBackground(){
	if (jp.myfilt.SUPERIMPOSE) jp.myfilt.SUPERIMPOSE=false;
	 else jp.myfilt.SUPERIMPOSE=true;

	}

    public void rescale(){
	int val=0;
	int off=offset+UserOffset;
	double sc=scale*UserScale;

	if (normalize==null){
	for (int i=0;i<datArray.length;i++){

	   val=(int)(((double)datArray[i]-off)*sc);
	   if (val>255)val=255;
	   if (val<0) val=0;
	    viewArray[i]=val;

	 }
     }
     else
     {
		 for (int i=0;i<datArray.length;i++){

		 	   val=(int)((((double)(datArray[i]-background[i])*normalize[i]))*sc);
		 	   if (val>255)val=255;
		 	   if (val<0) val=0;
		 	   viewArray[i]=val;

	 }

     }
    }

	public void toggleFilter(){
	 if (jp.SHOWFILTERED){jp.SHOWFILTERED=false; viewScale=1.0f; jp.scale=1.0f;}
	  else{
	    jp.SHOWFILTERED=true;
	    setViewScale();
	  }
	 }
	 public void nextFilter(){
	  jp.myfilt.ConvolveNum++;
	  if (jp.myfilt.ConvolveNum>2)
	   jp.myfilt.ConvolveNum=0;
	}

    public void setVisualScale(float val){
	if (val>0){ UserSelectedScale=val; AUTOSCALE=false;}
	else AUTOSCALE=true;
	setViewScale();
	repaint();
	}

	public void saveImage(){
			BufferedImage image = new BufferedImage(
			   (int)windowSize.getWidth(),(int)windowSize.getHeight(),BufferedImage.TYPE_BYTE_INDEXED);

			Graphics2D myG = (Graphics2D)image.getGraphics();

			jp.paintComponent(myG);

			ImageUtils.WriteImage("savedimage.bmp",image,"bmp");


		}

   public void saveROIs(){
	  try{
	    FileOutputStream out = new FileOutputStream("lastROIs.roi");
		ObjectOutputStream s = new ObjectOutputStream(out);
		s.writeObject(jp.rois);

		s.flush();
	}catch(IOException e){e.printStackTrace();}


   }

   public void loadROIs(){
	 try{
	   FileInputStream in = new FileInputStream("lastROIs.roi");
	   ObjectInputStream s = new ObjectInputStream(in);

	   jp.rois = (java.util.ArrayList)s.readObject();

    }
    catch(ClassNotFoundException e){e.printStackTrace();}
    catch(IOException e){e.printStackTrace();}
      for (int i=0;i<jp.rois.size();i++){
		  ROI roi=(ROI)jp.rois.get(i);
		  roi.vi=this;
     }
   }

	public void setViewScale(){
		if (AUTOSCALE){
		//maintain aspect ratio,
		viewScale=(float)((Math.min(windowSize.getHeight(),windowSize.getWidth()))/(Math.max(Y_dim,X_dim)));
		if (Y_dim*viewScale>windowSize.getHeight()) viewScale=(float)windowSize.getHeight()/(float)Y_dim;
		if (X_dim*viewScale>windowSize.getWidth()) viewScale=(float)windowSize.getWidth()/(float)X_dim;
		viewScale=viewScale*1.1f;
		//if (viewScale<1) viewScale=1;
		view_X_dim=(int)(X_dim*viewScale);
		view_Y_dim=(int)(Y_dim*viewScale);
	   }
	   else viewScale=UserSelectedScale;
		if (jp!=null){if (jp.SHOWFILTERED) jp.SetScale(viewScale);}
        System.out.println("set scale");
	}

	public void setInterpolation(int val){
		if (jp!=null) jp.setInterpolation(val);
	}

    //Can be invoked by any thread (since timer is thread-safe).
    public void startAnimation() {
        if (frozen) {
            //Do nothing.  The user has requested that we
            //stop changing the image.
        } else {
            //Start animating!
            if (!timer.isRunning()) {
                timer.start();
            }
        }
    }

    //Can be invoked by any thread (since timer is thread-safe).
    public void stopAnimation() {
        //Stop the animating thread.
        if (timer.isRunning()) {
            timer.stop();
        }
    }


    /*****************************************
     ImageJ addons
    ******************************************/

	public ColorProcessor getColorProcessor(){
		BufferedImage img=jp.myfilt.go(jp.bi);//myfilt.go(bi),null,0,0)jp.bi;

		int width = img.getWidth(null);
		int height = img.getHeight(null);

		BufferedImage dest= new BufferedImage(width,height,BufferedImage.TYPE_INT_ARGB);
		ColorConvertOp op=new ColorConvertOp(null);
		op.filter(img,dest);
		ColorProcessor cp=new ColorProcessor(dest);
		return cp;
	}



	public ImagePlus getImagePlus(int startframe, int endframe, int increment){
		 JumpToFrame(startframe);
	 ColorProcessor cp=getColorProcessor();
	 ImageStack stack=new ImageStack(jp.myfilt.go(jp.bi).getWidth(null),jp.myfilt.go(jp.bi).getHeight(null));
	 stack.addSlice("tmp",cp);
	 im.JumpToFrame(startframe,instance);
	 for (int i=startframe+1; i<endframe;i+=increment){
		AdvanceSingleFrame();
		cp=getColorProcessor();
		stack.addSlice("tmp"+i,cp);
		//System.out.println("new frame="+i);
	 }
	 ImagePlus ip=new ImagePlus(filename+".ip",stack);
	 return ip;

	}

	public void writeAVI(){
		AVIDialog.initialize(null,"avi range", speinfo.NumberOfFrames);
		AVIDialogInfo avd=AVIDialog.showDialog(null, "");
		AVI_Writer avw=new AVI_Writer();
		try{
		ImagePlus ipl=getImagePlus(avd.start,avd.end,1);
		//ipl.show();
		avw.writeImage(ipl);
		}catch(Exception e){e.printStackTrace();}

	}

/***********************************************
************************************************/
	public void JumpToFrame(int framenum){
	im.JumpToFrame(framenum, instance);
	frameNumber=framenum;
	LastUserSetFrame=framenum;
	im.UpdateImageArray(datArray,speinfo.X_dim,speinfo.Y_dim,instance);
	rescale();
	//notify listeners...
	NotifyFrameListeners(framenum);
	repaint();
	jp.ARRAYUPDATED=true;
	jp.repaint();
	}

	public void AdvanceSingleFrame(){
	if (frameNumber<speinfo.NumberOfFrames){
	im.JumpToFrame(frameNumber++, instance);
	im.UpdateImageArray(datArray,speinfo.X_dim,speinfo.Y_dim,instance);
	jp.ARRAYUPDATED=true;
	rescale();
	//notify listeners...
	NotifyFrameListeners(frameNumber);
	repaint();
	jp.forceRepaint();
	}
	}
	public void BackSingleFrame(){
	if (frameNumber>0){
	im.JumpToFrame(frameNumber--, instance);
	im.UpdateImageArray(datArray,speinfo.X_dim,speinfo.Y_dim,instance);
	jp.ARRAYUPDATED=true;
	rescale();
	//notify listeners...
	NotifyFrameListeners(frameNumber);
	repaint();
	}
	}

	public void JavaNormalize(){

	int oldframe=frameNumber;
	int startrange=frameNumber;
	int endrange=frameNumber+100;
	if (endrange>=speinfo.NumberOfFrames) endrange=speinfo.NumberOfFrames-1;

	//determine a range to normalize over
	if (Navigator_end>Navigator_start){startrange=Navigator_start; endrange=Navigator_end;}
	if (normalize==null){
	System.out.println("starting normalization calculation");
		normalize=new double[X_dim*Y_dim];
		background=new double[X_dim*Y_dim];
		double[] minval=new double[X_dim*Y_dim];
		double[] maxval=new double[X_dim*Y_dim];
	for (int p=0;p<X_dim*Y_dim;p++){
		minval[p]=Double.MAX_VALUE;
		maxval[p]=Double.MIN_VALUE;
	}
	for (int i=startrange; i<endrange;i++){
	im.JumpToFrame(i,instance);
	im.UpdateImageArray(datArray,speinfo.X_dim,speinfo.Y_dim, instance);
	jp.ARRAYUPDATED=true;

	for (int  j=0;j<X_dim*Y_dim;j++){
		if (minval[j]>datArray[j]) minval[j]=datArray[j];
		if (maxval[j]<datArray[j]) maxval[j]=datArray[j];
	}
    }
    double maxnorm=Double.MIN_VALUE;
	for (int k=0;k<normalize.length;k++){
	 normalize[k]=maxval[k]-minval[k];
	 if (maxnorm<normalize[k])maxnorm=normalize[k];
	}
    background=minval; // keep this.
    for (int l=0;l<normalize.length;l++){
	 normalize[l]=maxnorm/normalize[l];
	}



     im.JumpToFrame(oldframe,instance);
     System.out.println("ending normalization calculation");

    }
     else {normalize=null; background=null;System.out.println("reset normalize");}
	}

	public void deleteRoi( ){
	try{
	for (int i=0;i<jp.rois.size();i++){
	 if (((ROI)jp.rois.get(i)).poly.contains(lastMouseX/viewScale, lastMouseY/viewScale)){

	  if (lastCreatedNavigator!=null) lastCreatedNavigator.tg.Traces.remove(i);
	  jp.rois.remove(i);
	  if (lastCreatedNavigator!=null) lastCreatedNavigator.cols-=1;
	  break;
	  }
	}
	}catch(Exception e){e.printStackTrace(); System.out.println("couldn't find roi");}
	jp.repaint();
	}

    public void toggleROIMode(){
	if (ROIMODE==FREEFORMROI) ROIMODE=RECTANGLEROI;
	else ROIMODE=FREEFORMROI;
   }

    public void toggleRulerRoi(){
	  if (DRAGMODE==DRAWROI) DRAGMODE=DRAWRULER;
	  else DRAGMODE=DRAWROI;
	}


    public void toggleSaveJPGSeries(){
		if (jp.SAVEJPGS) jp.SAVEJPGS=false;
		else jp.SAVEJPGS=true;
	}

	 public void toggleSaveBMPSeries(){
			if (jp.SAVEBMPS) jp.SAVEBMPS=false;
			else jp.SAVEBMPS=true;
	}
	public void changeRoiColor( ){
		try{
		for (int i=0;i<jp.rois.size();i++){
		 if (((ROI)jp.rois.get(i)).poly.contains(lastMouseX/viewScale, lastMouseY/viewScale)){

		  Color newColor = JColorChooser.showDialog(
		                       this,
		                       "Choose ROI Color",
		                        ((ROI)jp.rois.get(i)).color);
		  if (newColor!=null) ((ROI)jp.rois.get(i)).color=newColor;


		  break;
		  }
		}
		}catch(Exception e){System.out.println("couldn't find roi");}
		jp.repaint();
	}

	public void fastUnitRoi(){
		jp.deleteAllROIs();
		createUnitRoi();
		processRois(0,-1);
	}

	public void createUnitRoi(){
		int iX,iY;
		Color brightColor=new Color((int)(Math.random()*155)+100,(int)(Math.random()*155)+100,(int)(Math.random()*155)+100);

		iX=(int)(lastMouseX/viewScale);
		iY=(int)(lastMouseY/viewScale);
		jp.presentroi=new ROI(this,brightColor);
		jp.presentroi.poly.addPoint(iX,iY);
		jp.presentroi.poly.addPoint(iX+1,iY);
		jp.presentroi.poly.addPoint(iX+1,iY+1);
		jp.presentroi.poly.addPoint(iX,iY+1);
		jp.rois.add(jp.presentroi);

		if (im instanceof OMPRODecoder){
		System.out.println("OMPRO detected, try to auto create a complimentary unit roi");
		int [] inf=new int[4];
		int yoffset=0;
		im.ReturnXYBandsFrames(inf,instance);
		if (inf[2]==2){
			if (inf[1]==32) yoffset=16;
			if (inf[1]==34) yoffset=17;
			System.out.println("using a "+yoffset+" offset");
		jp.presentroi=new ROI(this,brightColor.darker());
		jp.presentroi.poly.addPoint(iX,iY+yoffset);
		jp.presentroi.poly.addPoint(iX+1,iY+yoffset);
		jp.presentroi.poly.addPoint(iX+1,iY+1+yoffset);
		jp.presentroi.poly.addPoint(iX,iY+1+yoffset);
		jp.rois.add(jp.presentroi);
		}
		 else System.out.println("OMPRO has only one channel");
		}

	}

	public void quickTrace(){
		int iX,iY;
	    iX=(int)(lastMouseX/viewScale);
		iY=(int)(lastMouseY/viewScale);
		//
		if (im instanceof Matrix){
		  Matrix ma=(Matrix)im;
		  Trace tr= ma.getTrace(iX,iY);
		  lastCreatedNavigator.replaceLastTrace(tr);
		}

	}

	 public void paintComponent(Graphics g) {
        super.paintComponent(g);  //paint background
		//lazy way to detect a resize. should redo CHANGE
		  Dimension tmp=jp.getSize();
				if (!tmp.equals(windowSize)){

					windowSize=tmp;
					setViewScale();

			}


		}

    public void actionPerformed(ActionEvent e) {
        //Advance the animation frame.

		frameNumber++;
		im.UpdateImageArray(datArray,X_dim,Y_dim,instance);
		jp.ARRAYUPDATED=true;
        rescale();
		jp.set(frameNumber);
		if (nav!=null) nav.setFrame(frameNumber);
		NotifyFrameListeners(frameNumber);
		//System.out.print(frameNumber+" ");
	  }

	public void MouseOverPixel(int x, int y){
	 System.out.println("x,y: "+x+","+y+" pixel x,y: "+(int)x/viewScale+","+(int)y/viewScale);


	}

	}




class ViewMenuListener implements ActionListener{
 Viewer2 vi;

 public ViewMenuListener(Viewer2 vi){
  this.vi=vi;
  }

 //gets popup events
 public void actionPerformed(ActionEvent e) {
        JMenuItem source = (JMenuItem)(e.getSource());
        String s = "Action event detected."+ "    Event source: " + source.getText();
        System.out.println(s);
		if (source.getText().equals("create navigator")){

		 vi.createNavigator();
		}
	if (source.getText().equals("toggle ruler|roi")){vi.toggleRulerRoi();}
	else if (source.getText().equals("quicktrace")){vi.quickTrace();}
	else if (source.getText().equals("save jpgs")){vi.toggleSaveJPGSeries();}
	else if (source.getText().equals("save bmps")){vi.toggleSaveBMPSeries();}
	else if (source.getText().equals("save avi")){vi.writeAVI();}

	else if (source.getText().equals("toggle background")){ vi.ToggleBackground();}
    else if (source.getText().equals("rescale")){ vi.findScaleOffset();}
	else if (source.getText().equals("2.0x")) {vi.setVisualScale(2.0f);}
	else if (source.getText().equals("1.0x")) {vi.setVisualScale(1.0f);}
	else if (source.getText().equals("0.5x")) {vi.setVisualScale(0.5f);}
	else if (source.getText().equals("0.25x")) {vi.setVisualScale(0.25f);}
	else if (source.getText().equals("auto")) {vi.setVisualScale(0f);}
	else if (source.getText().equals("nearest")) {vi.setInterpolation(0);}
	else if (source.getText().equals("bilinear")) {vi.setInterpolation(1);}
	else if (source.getText().equals("bicubic")) {vi.setInterpolation(2);}
	else if (source.getText().equals("bicubic2")) {vi.setInterpolation(3);}
	else if (source.getText().equals("delete roi")){ vi.deleteRoi();}
	else if (source.getText().equals("delete all")){ vi.jp.deleteAllROIs();}
	else if (source.getText().equals("toggle filter")){ vi.toggleFilter();}
	else if (source.getText().equals("next filter")){ vi.nextFilter();}
	else if (source.getText().equals("process rois")){ vi.processRois(0,-1);}
	else if (source.getText().equals("color rois")){ vi.changeRoiColor();}
	else if (source.getText().equals("toggle rect/poly")){ vi.toggleROIMode();}
	else if (source.getText().equals("close roi")){ vi.jp.rois.add(vi.jp.presentroi); vi.jp.presentroi=null; System.out.println("closed roi");}
	else if (source.getText().equals("delete pt")){ /*vi.deleteRoiPt();*/}
	else if (source.getText().equals("unit roi")){vi.createUnitRoi();}
	else if (source.getText().equals("fast unit roi")){vi.fastUnitRoi();}
	else if (source.getText().equals("save image")){vi.saveImage();}
	else if (source.getText().equals("save rois")){vi.saveROIs();}
	else if (source.getText().equals("load rois")){vi.loadROIs();}

	   }

}


class viListener extends MouseInputAdapter {
	Viewer2 vi;
	int x;
	int y;
	boolean dragged=false;
	boolean shifton=false;

	public viListener(Viewer2 vi){
		   this.vi=vi;
		}

    public void mouseMoved(MouseEvent e){
	 if ((shifton)&&(vi.jp.presentroi!=null)){
	 vi.jp.presentX=e.getX();
	 vi.jp.presentY=e.getY();
	 vi.jp.SHOWLASTLINE=true;
	 System.out.println("should show line...");
	 vi.repaint();

	 }
	}
	public void mousePressed(MouseEvent e) {
	 if((e.getModifiers() & InputEvent.BUTTON1_MASK)
	     == InputEvent.BUTTON1_MASK){
		 if (vi.DRAGMODE==vi.DRAWROI){
		  x = e.getX();
		  y = e.getY();
         if ((!shifton)||(vi.jp.presentroi==null))
		 vi.jp.presentroi=new ROI(vi,new Color((int)(Math.random()*255),(int)(Math.random()*255),(int)(Math.random()*255)));
         vi.jp.presentroi.firstX=(int)((float)x/vi.viewScale);
		 vi.jp.presentroi.firstY=(int)((float)y/vi.viewScale);

		 vi.lastMouseX=x;
		 vi.lastMouseY=y;
		  if (e.isAltDown()){
			  System.out.println("alt - graphing");
			  vi.drawOnNavigator(e.getX(),e.getY());
		  }else
          if (e.isShiftDown()){
			 System.out.println("shift detected on click");
			 vi.jp.presentroi.poly.addPoint((int)((float)e.getX()/vi.viewScale),(int)((float)e.getY()/vi.viewScale));
			 dragged=false;
             System.out.println("present poly has "+vi.jp.presentroi.poly.npoints);
             shifton=true;
			 vi.jp.SHOWLASTLINE=true;
			 vi.jp.lastX=vi.lastMouseX;
			 vi.jp.lastY=vi.lastMouseY;
			 vi.jp.presentX=vi.lastMouseX;
			 vi.jp.presentY=vi.lastMouseY;
             vi.repaint();
		  } else
		   shifton=false;
		  }

	      else{
		  //dragmode=drawruler
			 x=e.getX();
			 y=e.getY();
			 vi.lastMouseX=x;
			 vi.lastMouseY=y;

			 vi.jp.presentruler=new Ruler(vi,Color.red);
			 vi.jp.presentruler.firstX=(int)((float)x/vi.viewScale);
			 vi.jp.presentruler.firstY=(int)((float)y/vi.viewScale);
			 System.out.println("Created new ruler, firstX="+ vi.jp.presentruler.firstX);
		  }

	     }
	     maybeShowPopup(e);
		}

	  public void mouseDragged(MouseEvent e) {
		 if((e.getModifiers() & InputEvent.BUTTON1_MASK)
		 	== InputEvent.BUTTON1_MASK){
			if (vi.DRAGMODE==vi.DRAWROI){
		    if (!e.isShiftDown()){
		    vi.MouseOverPixel(e.getX(),e.getY());
			//vi.resetSelection(x,y,e.getX(),e.getY());
			//vi.repaint();
			if (vi.ROIMODE==vi.FREEFORMROI)
			 vi.jp.presentroi.poly.addPoint((int)((float)e.getX()/vi.viewScale),(int)((float)e.getY()/vi.viewScale));
			if (vi.ROIMODE==vi.RECTANGLEROI){

			 vi.jp.presentroi.poly=new Polygon();
			 vi.jp.presentroi.poly.addPoint(vi.jp.presentroi.firstX,vi.jp.presentroi.firstY);
			 vi.jp.presentroi.poly.addPoint(vi.jp.presentroi.firstX, (int)((float)e.getY()/vi.viewScale));
			 vi.jp.presentroi.poly.addPoint((int)((float)e.getX()/vi.viewScale),(int)((float)e.getY()/vi.viewScale));
			 vi.jp.presentroi.poly.addPoint((int)((float)e.getX()/vi.viewScale), vi.jp.presentroi.firstY);
             System.out.println("firstX="+vi.jp.presentroi.firstX);
			}


			dragged=true;
			vi.lastMouseX=e.getX();
			vi.lastMouseY=e.getY();
		    vi.repaint();
		 }
	    }//drawroi
	    else{
		//drawruler mode
		 x=e.getX();
	     y=e.getY();
	     vi.jp.presentruler.lastX=(int)((float)x/vi.viewScale);
		 vi.jp.presentruler.lastY=(int)((float)y/vi.viewScale);
		 vi.repaint();
		  }

		}
	   }


  public void mouseReleased(MouseEvent e) {

		if((e.getModifiers() & InputEvent.BUTTON1_MASK)
			== InputEvent.BUTTON1_MASK){
			if (vi.DRAGMODE==vi.DRAWROI){
			if (!e.isShiftDown()){
				shifton=false;
			if (dragged){
			//vi.showSelection=false;
			//vi.rescaleToSelection();
			if (vi.jp.presentroi.poly.npoints>3){
			  vi.jp.rois.add(vi.jp.presentroi);
			  }else {vi.jp.presentroi=null; System.out.println("deleted presentroi.. ");}
			}
			else
			{
			//vi.setPoint(x,y);
			vi.jp.presentroi=null;
			}
			dragged=false;
			vi.lastMouseX=e.getX();
			vi.lastMouseY=e.getY();
		 }

        vi.jp.SHOWLASTLINE=false;
		maybeShowPopup(e);
      }
      else{
	   //dragmode=drawruler
      vi.jp.presentruler.updateResults();
		  }
      }
      vi.updateTitle();

     }


 private void maybeShowPopup(MouseEvent e) {

			  System.out.println("checking popup");
			  if((e.getModifiers() & InputEvent.BUTTON3_MASK)== InputEvent.BUTTON3_MASK){

			  vi.lastMouseX=e.getX();
			  vi.lastMouseY=e.getY();
			    if (e.isShiftDown()){vi.popupROI.show(e.getComponent(),e.getX(), e.getY());}
			     else
				  vi.popup.show(e.getComponent(),
							 e.getX(), e.getY());
			  }
			}

    }