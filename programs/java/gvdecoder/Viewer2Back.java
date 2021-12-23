package gvdecoder;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.awt.geom.*;
import java.awt.Graphics2D.*;
import java.util.*;
import java.awt.image.*;
import java.awt.image.renderable.*;
import gvdecoder.utilities.*;
import gvdecoder.prefs.*;
import gvdecoder.trace.*;
import ij.process.*;
import ij.*;
import javax.swing.KeyStroke;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.InputMap;

public class Viewer2 extends JInternalFrame implements KeyListener, ActionListener, ImageViewer, HtmlSaver{
    ViewerParams vp;
    public int ROIMODE=2;
    public int FREEFORMROI=1;
    public int RECTANGLEROI=2;

    public int DRAGMODE=1;
    public int DRAWROI=1;
    public int DRAWRULER=2;
    public int DRAWTRACE=3;
    public int ADDTRACE=4;


    boolean AUTOSCALE=true; //toggle scale for x,y
    public boolean ACCUMULATERULERS=false;


    public JPanel viewPane;

    public String roidirectory="";

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
	public int MaxFrames;
	public int MaxBands;
	public int instance;
	//public static String filename;
	//public static String absolutefilename;
	public String filename;
	public String absolutefilename;
	GView ifd; //contains this
	public NavigatorGraph nav;    //associated with this file
	public NavigatorGraph lastCreatedNavigator=null; //associated with this file
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

    float UserSelectedScale=1.0f; //user selected scale for x,y
    public Color[] roicolors=new Color[10];
    public JSplitPane splitPane; // for control via scripting
    public ExportPanel ex;


    public boolean on_tap_plottrace=false;

    public MatrixSubAction matrixSubAction;

    public boolean CANCEL_LOOP=false;



    public int move_ROI_index=0;





   /** Handle the key typed event from the text field. */



  public void setupactionmap(){
	   KeyStroke stroke = KeyStroke.getKeyStroke("ctrl C");
	  Action action = new MyActionListener("Action Didn't Happen");
	  InputMap inputMap = jp.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);//
	  inputMap.put(stroke, "ctrlc");
	  ActionMap actionMap = jp.getActionMap();
      actionMap.put("cntrlc", action);

  }

   static class MyActionListener extends AbstractAction {
     MyActionListener(String s) {
       super(s);
     }

     public void actionPerformed(ActionEvent e) {
       System.out.println(getValue(Action.NAME));
     }
  }

    public void keyTyped(KeyEvent e) {
	 if (e.getKeyChar()=='n')  {
    	 	move_ROI_index++;
    	 	if (move_ROI_index>=jp.rois.size()){
				move_ROI_index=0;
			}
	   		}

      System.out.println("key typed");
    }

   public void keyPressed(KeyEvent e) {
	     System.out.println("key pressed");
   		// Bottom padel uses LEFT and RIGHT arrow buttons
   		// Top padel users, uses A and D to move the padel.
           int x_offset=0;
           int y_offset=0;
           switch (e.getKeyCode()) {
               case KeyEvent.VK_RIGHT: x_offset=1;  break;
               case KeyEvent.VK_LEFT: x_offset=-1;  break;
               case KeyEvent.VK_UP:   y_offset=-1;  break;
               case KeyEvent.VK_DOWN: y_offset=1;   break;
           }
          if ((jp.rois!=null)&&(jp.rois.size()>0)){

			  ((ROI)jp.rois.get(move_ROI_index)).poly.translate(x_offset,y_offset);
		  }
          repaint();
    }

    /** Handle the key released event from the text field. */
    public void keyReleased(KeyEvent e) {
		if (e.isControlDown()&&(e.getKeyCode() == KeyEvent.VK_C)){
			 toClipboard();
		 }
    }
    public void toClipboard(){
		 gvdecoder.ImageUtils.setClipboard(getImage());
		 AnalysisHelper.getAnalysisHelper().Notify("image exported to clipboard");
	}

	public String saveHtml(){
	try{
     String namestring=AnalysisHelper.getAnalysisHelper().saveImage(getImage());
     File imagef=new File(namestring);
	 java.net.URL imageurl=imagef.toURI().toURL();
	  return "</pre><img src='"+imageurl+"'><pre>";
	 }catch(Exception e){e.printStackTrace();}
     return "";
	}

    aFrame aframe;
    public aFrame getaFrame(){
		aframe=new ViewFrame(this); //new aFrame((JPanel)this.getContentPane(),aFrame.Internal_Frame,getTitle(),ifd);
		this.hide();
		this.dispose();
		return aframe;
	}

    public void cleanup(){

	 ifd.jh.removeViewer(this);
	 ifd.updateWindowList();
	}

	public void notifyHelpers(){
	 System.out.println("INTERNAL FRAME ACTIVATED - ");
	 ifd.jh.setPresentViewer(this);
	 System.out.println("focus granted="+requestFocusInWindow());
	 ifd.jh.setSaveHtml(this,"import image");
	}

 	public void notifyclosed(){
	ifd.jh.unsetSaveHtml(this);
	}
    public String toString(){
		int[] tmp=new int[4];
		im.ReturnXYBandsFrames(tmp,instance);
		return filename+" (x,y,z)=("+tmp[0]+","+tmp[1]+","+tmp[3]+")";
	}




	public void SetupWindow(){

		matrixSubAction=new MatrixSubAction(this,"range");

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

                 public void internalFrameActivated(InternalFrameEvent e){
					 notifyHelpers();
				   }
				  public void internalFrameClosed(InternalFrameEvent e) {
					 notifyclosed();
					 stopAnimation();
					 im.CloseImageFile(instance);
					 if (!ifd.REUSEARRAY){
					  viewArray=null;
					  datArray=null;
				     }
					 normalize=null;
	                 background=null;
					 cleanup();
					 System.out.println("internal frame closed");
				  }
		        });


		jp=new ImagePanel(viewArray,X_dim,Y_dim,viewScale);



		JAIControlPanel cp=new JAIControlPanel(this);
		PreprocessControlPanel pp=new PreprocessControlPanel(this);
		JPanel ppc=new JPanel();
		ppc.setLayout(new BoxLayout(ppc,BoxLayout.Y_AXIS));


		ppc.add(pp);
		ppc.add(Box.createVerticalGlue());




		JPanel MainControlPanel=new JPanel();

		Border lineBorder=BorderFactory.createLineBorder(Color.blue,1);
	    MainControlPanel.setBorder(lineBorder);
	    MainControlPanel.setLayout(new BoxLayout(MainControlPanel, BoxLayout.X_AXIS));


	    MainControlPanel.add(ppc);
	    MainControlPanel.add(cp);
		setViewScale();

		viewPane=new JPanel();
		//ExportPanel ex=new ExportPanel(this);


		//JPanel export=new JPanel();
		//export.setLayout(new BoxLayout(export, BoxLayout.X_AXIS));
		//export.setBorder( BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),"Export",TitledBorder.LEFT,TitledBorder.TOP));
		//export.add(ex);
        //MainControlPanel.add(ex);


		viewPane.setLayout(new BoxLayout(viewPane,BoxLayout.X_AXIS));

		JPanel contentPane = new JPanel();
		contentPane.setLayout(new BoxLayout(contentPane,BoxLayout.Y_AXIS));




		JPanel toolBar = new MediaButtons(this,true,true,true,true,true);

		toolBar.setAlignmentY(Component.TOP_ALIGNMENT);

		//add scroll pane here if appropriate.

	    if (vp.UseScrollPane){
	    JScrollPane scrollPane = new JScrollPane(jp);
		contentPane.add(scrollPane);
		setVisualScale(1.0f);
	    }else contentPane.add(jp);
	    //JPanel botControl=new JPanel();
	    //botControl.setLayout(new BoxLayout(botControl,BoxLayout.X_AXIS));
	    //botControl.add(toolBar);
	    //botControl.setAlignmentY(Component.TOP_ALIGNMENT);
	    //JTextField tf=new JTextField(15);
	    //Dimension d=new Dimension(180,20);
	    //toolBar.setMaximumSize(d);
		//tf.setPreferredSize(d);
		//tf.setMaximumSize(d);

	    //botControl.add(tf);
	    //botControl.add(Box.createHorizontalGlue());
        contentPane.add(toolBar);
	    //contentPane.add(botControl);
        viewPane.add(contentPane);
        //JSplitPane sp=new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,viewPane,export);
        //sp.setOneTouchExpandable(true);
        //sp.setDividerLocation(0.99);
		/*JSplitPane*/
		splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,  MainControlPanel,  viewPane);
		splitPane.setOneTouchExpandable(true);
		splitPane.setDividerLocation(vp.DividerLocation);

	    getContentPane().add(splitPane, BorderLayout.CENTER);
	        //setJMenuBar(createMenuBar());
            popup=new JPopupMenu();

		  ViewMenuListener myViewMenuListener=new ViewMenuListener(this);
		  viListener vilistener = new viListener(this);
		          JMenuItem menuItem = new JMenuItem("rescale");
				  menuItem.addActionListener(myViewMenuListener);
				  popup.add(menuItem);
                  popup.addSeparator();
				  JMenu submenu = new JMenu("mode");
                  JMenu subsubmenu=new JMenu("->matrix");
                  menuItem = new JMenuItem("all frames");
				  menuItem.addActionListener(myViewMenuListener);
				  subsubmenu.add(menuItem);
				  menuItem = new JMenuItem(matrixSubAction);
//				  menuItem.addActionListener(myViewMenuListener);
				  subsubmenu.add(menuItem);
	              submenu.add(subsubmenu);


                  submenu.addSeparator();
                  ButtonGroup bgroup=new ButtonGroup();
				  JRadioButtonMenuItem rb=new JRadioButtonMenuItem("ROI mode");
				  rb.setActionCommand("ROI");
				  rb.addActionListener(myViewMenuListener);
				  rb.setSelected(true);
				  rb.setMnemonic(KeyEvent.VK_O);
				  bgroup.add(rb);
				  submenu.add(rb);
				  rb=new JRadioButtonMenuItem("Ruler mode");
				  rb.setActionCommand("Ruler");
				  rb.addActionListener(myViewMenuListener);
				  rb.setMnemonic(KeyEvent.VK_U);
				  bgroup.add(rb);
				  submenu.add(rb);
				  rb=new JRadioButtonMenuItem("view trace");
				  rb.setActionCommand("viewTrace");
				  rb.addActionListener(myViewMenuListener);
				  rb.setMnemonic(KeyEvent.VK_T);
				  bgroup.add(rb);
				  submenu.add(rb);
				  rb=new JRadioButtonMenuItem("add trace");
			      rb.setActionCommand("addTrace");
				  rb.addActionListener(myViewMenuListener);
				  rb.setMnemonic(KeyEvent.VK_T);
				  bgroup.add(rb);
				  submenu.add(rb);

                  popup.add(submenu);
				  popup.addSeparator();



				  menuItem = new JMenuItem("open navigator");
				  menuItem.addActionListener(myViewMenuListener);
				  popup.add(menuItem);
				  menuItem = new JMenuItem("toggle background");
				  menuItem.addActionListener(myViewMenuListener);
				  popup.add(menuItem);



				  submenu = new JMenu("export");
				  submenu.setMnemonic(KeyEvent.VK_E);
				      menuItem = new JMenuItem("export control");
				      menuItem.addActionListener(myViewMenuListener);
				      submenu.add(menuItem);

					  menuItem = new JMenuItem("save gview");
					  menuItem.addActionListener(myViewMenuListener);
					  submenu.add(menuItem);

					  menuItem = new JMenuItem("to clipboard");
					  menuItem.addActionListener(myViewMenuListener);
					  submenu.add(menuItem);

					  menuItem = new JMenuItem("2x bin");
					  menuItem.addActionListener(myViewMenuListener);
					  submenu.add(menuItem);

					  menuItem = new JMenuItem("3x bin");
					  menuItem.addActionListener(myViewMenuListener);
					  submenu.add(menuItem);

					  menuItem = new JMenuItem("4x bin");
					  menuItem.addActionListener(myViewMenuListener);
					  submenu.add(menuItem);

					  menuItem = new JMenuItem("5x bin");
					  menuItem.addActionListener(myViewMenuListener);
					  submenu.add(menuItem);

					  menuItem = new JMenuItem("10x bin");
					  menuItem.addActionListener(myViewMenuListener);
					  submenu.add(menuItem);


					  menuItem = new JMenuItem("20x bin");
					  menuItem.addActionListener(myViewMenuListener);
					  submenu.add(menuItem);

					  menuItem = new JMenuItem("ROI out");
					  menuItem.addActionListener(myViewMenuListener);
					  submenu.add(menuItem);



				  popup.add(submenu);

				   submenu = new JMenu("view");
				  	          submenu.setMnemonic(KeyEvent.VK_M);
				  	          ButtonGroup group = new ButtonGroup();
				  	          JRadioButtonMenuItem rbMenuItem = new JRadioButtonMenuItem("1.0x mouse");
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


							  rbMenuItem = new JRadioButtonMenuItem("ROI zoom");
							  group.add(rbMenuItem);
							  rbMenuItem.addActionListener(myViewMenuListener);
							  submenu.add(rbMenuItem);
							  menuItem = new JMenuItem("recenter");
							  group.add(menuItem);
							  menuItem.addActionListener(myViewMenuListener);
							  submenu.add(menuItem);



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

		 submenu=new JMenu("ruler");
         submenu.setMnemonic(KeyEvent.VK_U);
                  menuItem = new JMenuItem("delete last ruler");
				  menuItem.addActionListener(myViewMenuListener);
				  submenu.add(menuItem);
				  menuItem = new JMenuItem("delete all rulers");
				  menuItem.addActionListener(myViewMenuListener);
				  submenu.add(menuItem);
				  JCheckBox cb = new JCheckBox("accumulate rulers");
				  cb.addItemListener(myViewMenuListener);
				  submenu.add(cb);
        popup.add(submenu);


		 submenu=new JMenu("roi");
		 submenu.setMnemonic(KeyEvent.VK_R);

				  menuItem = new JMenuItem("delete roi");
				  menuItem.addActionListener(myViewMenuListener);
				  submenu.add(menuItem);
				  menuItem = new JMenuItem("to Matrix");
				  				  menuItem.addActionListener(myViewMenuListener);
				  				  submenu.add(menuItem);
				  menuItem = new JMenuItem("process rois");
					menuItem.addActionListener(myViewMenuListener);
					submenu.add(menuItem);


				  menuItem = new JMenuItem("scale to roi");
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

                    menuItem=new JMenuItem("matrix replace");
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

	   roidirectory=PropertyHelper.getPropertyHelper().getProperty("ROI dir",".");
       setupactionmap();

	   windowSize=this.getSize();
	   JumpToFrame(0);

	   roicolors[0]=new Color(255,0,0);
	   roicolors[1]=new Color(0,255,0);
	   roicolors[2]=new Color(0,0,255);
	   roicolors[3]=new Color(255,255,0);
	   roicolors[4]=new Color(0,255,255);
	   roicolors[5]=new Color(255,0,255);
	   roicolors[6]=new Color(255,150,0);
	   roicolors[7]=new Color(0,255,150);
	   roicolors[8]=new Color(150,0,255);
	   roicolors[9]=new Color(0,150,255);


       if (vp.AutoLoadLastRois) loadROIs();

       if (vp.AutoProcessLoadedRois) processRois(0,-1);

       //send information to JythonHelper
       ifd.jh.setViewer(this);
       //jump to the first frame and rescale.
       findScaleOffset();
       JumpToFrame(0);

    //   getaFrame();


	}

	public Viewer2(GView ifd, ImageDecoder id){
		super(((ViewerDecoder)id).vw.filename+" processed",
							 true, //resizable
							 true, //closable
							 true, //maximizable
			  true);//iconifiable
	    setFocusable(true);
	    addKeyListener( this );


		vp=ViewerParams.getInstance();
		this.absolutefilename=((ViewerDecoder)id).vw.absolutefilename;
		this.filename=((ViewerDecoder)id).vw.filename;
        this.ifd=ifd;
        this.im=id;
        instance=0;
        setFPS(20);
        FrameListeners=new Vector();
	    int widthtmp=vp.InitialWidth;
		int heighttmp=vp.InitialHeight;
		setSize(new Dimension(widthtmp,heighttmp));
		setLocation(vp.InitialX,vp.InitialY);
        int[] tmpdat=new int[4];
		im.ReturnXYBandsFrames(tmpdat,instance);
		X_dim=tmpdat[0];
		Y_dim=tmpdat[1];
		MaxFrames=tmpdat[3];
		MaxBands=tmpdat[2];
		speinfo=new SPEInfo(X_dim,Y_dim,tmpdat[3],"","tmp.dat");
		datArray=new int[X_dim*Y_dim];
		windowSize=this.getSize();
	 	viewArray=new int[X_dim*Y_dim];
	    SetupWindow();

	}

	public Viewer2(GView ifd, int fps, String filename, String absolutefilename, ImageDecoder id){
		 super(filename,
					 true, //resizable
					 true, //closable
					 true, //maximizable
			  true);//iconifiable
	    setFocusable(true);
	    addKeyListener( this );


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
		MaxFrames=tmpdat[3];
		MaxBands=tmpdat[2];
		speinfo=new SPEInfo(X_dim,Y_dim,tmpdat[3],"","tmp.dat");
		datArray=new int[X_dim*Y_dim];
		windowSize=this.getSize();
		viewArray=new int[X_dim*Y_dim];
		System.out.println("going to setupwindow");
	    SetupWindow();
		System.out.println("out of setupwindow");
	}

    public void setImageAdapter(ImageDecoder im, String filename){
	/**
	 for use in scripts, to view a dataset with no new window open

	**/
	this.filename=filename;
	this.absolutefilename=filename;
	this.im=im;
	this.instance=0; //this is a holdover from .spe .dll

	int[] tmpdat=new int[4];
	im.ReturnXYBandsFrames(tmpdat,instance);
	X_dim=tmpdat[0];
	Y_dim=tmpdat[1];
	MaxFrames=tmpdat[3];
	MaxBands=tmpdat[2];
    speinfo=new SPEInfo(X_dim,Y_dim,tmpdat[3],"","tmp.dat");
	datArray=new int[X_dim*Y_dim];
	windowSize=this.getSize();
	viewArray=new int[X_dim*Y_dim];
	setViewScale();
    jp.createNewImage(viewArray,X_dim,Y_dim,viewScale);
    updateTitle();
	}





   public Viewer2(GView ifd, int fps, String filename, String absolutefilename, String filetype, int frameskip) {
       super(filename,
	                 true, //resizable
	                 true, //closable
	                 true, //maximizable
              true);//iconifiable
 		setFocusable(true);
        addKeyListener( this );

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
       if (frameskip>1){
		   if (im instanceof RedShirtDecoder) ((RedShirtDecoder)im).FRAMESTEP=frameskip;
		   if (im instanceof BigRedShirtDecoder) ((BigRedShirtDecoder)im).FRAMESTEP=frameskip;
	       if (im instanceof OMPRODecoder) ((OMPRODecoder)im).FRAMESTEP=frameskip;

	   }

       instance=im.OpenImageFile(absolutefilename);

	   FrameListeners=new Vector();

	   if (filetype.equals("spe")){
	   speinfo=SPEUtils.getSPEInfo(absolutefilename);
	   System.out.println(speinfo);
	   System.out.println("the nav file is "+speinfo.NavFile);
	   X_dim=speinfo.X_dim;
	   Y_dim=speinfo.Y_dim;
	   int[] tmpdat=new int[4];
	   im.ReturnXYBandsFrames(tmpdat,instance);
	   MaxBands=tmpdat[2];
	   MaxFrames=tmpdat[3];
	   }
	   else{
	   int[] tmpdat=new int[4];
	   System.out.println("debug before returnXYBandsFrames(tmpdat)");
	   im.ReturnXYBandsFrames(tmpdat,instance);
   	   System.out.println("debug after returnXYBandsFrames(tmpdat)");

	   X_dim=tmpdat[0];
	   Y_dim=tmpdat[1];
	   MaxBands=tmpdat[2];
	   MaxFrames=tmpdat[3];
	   speinfo=new SPEInfo(X_dim,Y_dim,tmpdat[3],"","tmp.dat");
	   }
       if (ifd.REUSEARRAY){
	   			   datArray=ifd.getIntArray(X_dim*Y_dim,0);
	   			   viewArray=ifd.getIntArray(X_dim*Y_dim,1);
	   		   }
	   		   else{
	   			   datArray=new int[X_dim*Y_dim];
	   			   viewArray=new int[X_dim*Y_dim];
		 }

	   windowSize=this.getSize();

       SetupWindow();

	}

public Viewer2(GView ifd, int fps, String filename, String absolutefilename, String filetype) {
    super(filename,
		                 true, //resizable
		                 true, //closable
		                 true, //maximizable
	              true);//iconifiable
	         setFocusable(true);
	        addKeyListener( this );

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
		   int[] tmpdat=new int[4];
		   im.ReturnXYBandsFrames(tmpdat,instance);
		   MaxBands=tmpdat[2];
		   MaxFrames=tmpdat[3];
		   }
		   else{
		   int[] tmpdat=new int[4];
		   System.out.println("debug before returnXYBandsFrames(tmpdat)");
		   im.ReturnXYBandsFrames(tmpdat,instance);
	   	   System.out.println("debug after returnXYBandsFrames(tmpdat)");

		   X_dim=tmpdat[0];
		   Y_dim=tmpdat[1];
		   MaxBands=tmpdat[2];
		   MaxFrames=tmpdat[3];
		   speinfo=new SPEInfo(X_dim,Y_dim,tmpdat[3],"","tmp.dat");
		   }
           if (ifd.REUSEARRAY){
			   datArray=ifd.getIntArray(X_dim*Y_dim,0);
			   viewArray=ifd.getIntArray(X_dim*Y_dim,1);
		   }
		   else{
			   datArray=new int[X_dim*Y_dim];
			   viewArray=new int[X_dim*Y_dim];
		   }
           windowSize=this.getSize();

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


/* In-place transformation to matrix object- this window now holds an im that is a Matrix object*/
public void toMatrix(){
        toMatrix(0,MaxFrames);
}

public ImageDecoder olddecoder=null;
public void toMatrix(int start, int end){
	ma=new Matrix();
	ma.initialize(im,start,end);
	MaxFrames=ma.zdim;
	olddecoder=im;
	im=ma;
}

/* Opens a separate window with the matrix object */
public void rangeToMatrix(){
		if (nav!=null){
		   ma=new Matrix();//MaMatrix(nav.x_offset,nav.x_range,0,Y_dim,0,X_dim);
		   ma.initialize(im,nav.x_offset,(nav.x_offset+nav.x_range),0,Y_dim,0,X_dim);
		   ifd.openImageFile(ma,"segment");
	       ifd.jh.vw[ifd.jh.index].ma=ma;
	}
	}


    public void openExportWindow(){
		//ExportPanel ep=new ExportPanel(this);
		//ep.setSize(new Dimension(215,340));
		//viewPane.add(ep);
		//this.pack();
		ex=ifd.openExportControl(this);
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


    public void setNavigatorStartEnd(int start, int end){
		Navigator_start=start;
		Navigator_end=end;
		if (ex!=null){
			ex.startRange.setText(""+Navigator_start);
			ex.endRange.setText(""+Navigator_end);
		}
		if (matrixSubAction!=null){
			matrixSubAction.setRange(Navigator_start,Navigator_end);
		}

	}


	public void setFPS(int fps){
	 int delay = (fps > 0) ? (1000 / fps) : 100;
	 if (timer==null) timer = new javax.swing.Timer(delay, this);
	 else timer.setDelay(delay);
	 timer.setInitialDelay(0);
	 timer.setCoalesce(true);
	}

public double realpixelscale=1.0;

    public int getvalue(int X, int Y){
	int val=0;

	if (Y*X_dim+X<(X_dim*Y_dim-1)){
	try{val=datArray[Y*X_dim+X];}catch(Exception e){;}
    }
    return val;
	}

	public void updateTitle(){

	lastCoordX=(int)((float)lastMouseX/viewScale-(jp.offsetx/viewScale));
	lastCoordY=(int)((float)lastMouseY/viewScale-(jp.offsety/viewScale));
	if (lastCoordY*X_dim+lastCoordX<(X_dim*Y_dim-1)){
	int val=0;

	try{val=datArray[lastCoordY*X_dim+lastCoordX];}catch(Exception e){;}
	String title="";

	if ((jp.presentruler!=null)&&(jp.presentruler.distance!=0)&&(DRAGMODE==DRAWRULER))
	 title=filename+" "+jp.presentruler.distance+" "+jp.presentruler.distance*realpixelscale;
	 else title=filename+" ("+frameNumber+","+lastCoordY+","+lastCoordX+")="+val;
	if (aframe==null) setTitle(title);
	else aframe.setTitle(title);
    }
	}

    public boolean NAVSUBRANGE=false;
	public void processRange(){
	// range is taken from last navigator
	if (nav==null) processRois(0,-1);
	int start=nav.x_offset;
	int end=start+nav.x_range;
	quickRois(start,end);
    if (NAVSUBRANGE==false){
      showRois(true);
      NAVSUBRANGE=true;
    }
    else
     showRois(false);
    nav.x_offset=start;
	}
	public void newtest(){
	 System.out.println("going crazy");
	}

    public boolean USEQUICKROIS=true;
    public boolean OPENNEWNAVIGATOR=true;
    public boolean SAVEROIDATA=true;
	public void processRois(int startrange, int endrange){
		if (endrange==-1) NAVSUBRANGE=false;
		if (USEQUICKROIS) {
			quickRois(startrange,endrange);
			showRois(OPENNEWNAVIGATOR);
			if (SAVEROIDATA) saveRois(null);
			return;
			}
		System.out.println("in processRois");
		//first check for strange name with / or \ or :
		String f1=filename.replaceAll("\\\\","_");
		String f2=f1.replaceAll(":","_");
		String f3=f2.replaceAll("/","_");
		String resultfile=roidirectory+File.separator+f3+".roi";
		 try{
		    int[][] roi=new int[jp.rois.size()][];
		 	for (int i=0;i<jp.rois.size();i++){
		 	  ((ROI)jp.rois.get(i)).findAllPixels(X_dim,Y_dim);
			   roi[i]=((ROI)jp.rois.get(i)).arrs;
		 	  }

		 	if (vp.AutoSaveChartAsNavigator)
		 	resultfile=roidirectory+File.separator+FileNameManager.getInstance().FindNavFileName(filename,"");

		 	im.SumROIs(roi,resultfile,startrange,endrange,instance);
			}
		catch(Exception e){System.out.println("couldn't find roi"); e.printStackTrace();}
		 MyInternalFrame frame = new MyInternalFrame(resultfile, resultfile,this, jp.rois);
				    nav=frame.getNavigator();
				    addFrameListener(frame.getNavigator());
				    lastCreatedNavigator=frame.getNavigator();
					frame.setVisible(true); //necessary as of kestrel
			        ifd.desktop.add(frame);

			        try {
			            frame.setSelected(true);
	        } catch (java.beans.PropertyVetoException e) {}
		System.out.println("exit processRois");
		}

  public int[][] roi_t;
  public int[][] quickRois(int startframe,int endframe){

	  int[] dims=new int[4];
	  im.ReturnXYBandsFrames(dims, 0);
	  int numframes=dims[3];
	  if (endframe<0) endframe=numframes; /*a shortcut for scanning whole record*/
	  if (endframe>numframes) endframe=numframes;
	  if (startframe<0) startframe=0;
      if (startframe>numframes) startframe=0;

	  int[][] roi=new int[jp.rois.size()][];
	  roi_t=new int[jp.rois.size()+1][endframe-startframe];
	  for (int i=0;i<jp.rois.size();i++){
	  		      ((ROI)jp.rois.get(i)).findAllPixels(X_dim,Y_dim);
	  			   roi[i]=((ROI)jp.rois.get(i)).arrs;
		 	  }

	  for (int k=startframe;k<endframe;k++){//for each frame
	       roi_t[0][k-startframe]=k;
	       im.JumpToFrame(k,0); //goto frame
	       im.UpdateImageArray(datArray,speinfo.X_dim,speinfo.Y_dim,instance); //load image
	       userFilter();
	       for (int i=0;i<roi.length;i++){ //for each roi

	         for (int j=0;j<roi[i].length;j++){ //go to each element in the roi
	           roi_t[i+1][k-startframe]+=datArray[roi[i][j]];

	         }//j
	         //System.out.println("sim for frame "+k+" = "+sum[k-startframe][i]);
	        }//i
	       }//k

   return roi_t;

  }

  public double[][] getRoiData(){
	  double[][] res=new double[roi_t.length-1][roi_t[0].length];
	  for (int i=1;i<roi_t.length;i++){
		for (int j=0;j<roi_t[0].length;j++){
		 res[i-1][j]=roi_t[i][j];
	     }
	  }
	 return res;
  }

  public  void setRoiData(double[][] dat){

	  int [][] res=new int[dat.length+1][dat[0].length];
	  for (int j=0;j<dat[0].length;j++){
	  		 res[0][j]=j;
	  	     }

	  for (int i=0;i<dat.length;i++){
		for ( int j=0;j<dat[0].length;j++){
		 res[i+1][j]=(int)dat[i][j];
	     }

	  }
	roi_t=res;
  }


  public void showRois(boolean newnavigator){
  if ((newnavigator)||(nav==null)){
   MyInternalFrame frame = new MyInternalFrame(roi_t, filename,this, jp.rois);
  					    nav=frame.getNavigator();
  					    addFrameListener(frame.getNavigator());
  					    lastCreatedNavigator=frame.getNavigator();
  						frame.setVisible(true); //necessary as of kestrel
  				        ifd.desktop.add(frame);
  				        try {
  				            frame.setSelected(true);
  	        } catch (java.beans.PropertyVetoException e) {}
    }
   else{

	   nav.replaceTraces(roi_t,jp.rois);
   }

}

 public void saveRois(String newfilename){
	 String outfile=null;
	 if (newfilename==null){
		 		//first check for strange name with / or \ or :
		 		String f1=filename.replaceAll("\\\\","_");
		 		String f2=f1.replaceAll(":","_");
		 		String f3=f2.replaceAll("/","_");
		        outfile=roidirectory+File.separator+f3+".roi";
	 }
	 else{
		 outfile=newfilename;
	 }

	 try{
	     PrintWriter file=new PrintWriter(new FileWriter(outfile),true);
	      for (int j=0;j<roi_t[0].length;j++){
	 	 for (int i=0;i<roi_t.length;i++) {
	 		 //System.out.println("debug rois "+i+" out of "+rois.length);
	 		 file.print(roi_t[i][j]+" ");
	 		 //System.out.print("debug "+sum[(j-startframe)][i]+" ");
	 		 }
	 	 file.print("\n");
	 	 //System.out.print("\n");
	 	 //System.out.println("debug frame="+j);
	 	 }
	     file.close();

	     }catch(IOException e){System.out.println("error opening file for rois...");}
     catch(Exception e){System.out.println("Some other error in sumROis");e.printStackTrace();}

 }


 public int _lastGetPixelFrameNumber=-1;
 public int getPixel(int z, int y, int x){
	  if (z!=_lastGetPixelFrameNumber){
		 _lastGetPixelFrameNumber=z;
		 JumpToFrame(z);
	  }
	  return(datArray[y*X_dim+x]);
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


  /** returns a suggested filename for data saved in ifd.pv.getStringProperty("Navigator dir",".").
      This name is the complete path name replacing strange characters.
   */
  public String getNavigatorFileName(){
   return absolutefilename.replace(':','_').replace('\\','_').replace('/','_').replace('.','_');
  }

  public void openNavigator(){
   String navfilename=getNavigatorFileName();
   String navfilepath=ifd.pv.getStringProperty("Navigator dir",".")+File.separator+navfilename+".nav";
   File file=new File(navfilepath);
   if (!file.exists()){
	    javax.swing.JOptionPane.showInternalMessageDialog(this,"The navigator file doesn't exist.\n"+
	                                                            "Please save the navigator graph\n"+
	                                                            "(from an open Navigator window) first");
	 return;

   }
   MyInternalFrame frame = new MyInternalFrame(navfilepath,(filename+" navigator"),this, null);
   				    nav=frame.getNavigator();
   				    addFrameListener(frame.getNavigator());
   				    lastCreatedNavigator=frame.getNavigator();
   					frame.setVisible(true); //necessary as of kestrel
   			        ifd.desktop.add(frame);

   			        try {
   			            frame.setSelected(true);
	        } catch (java.beans.PropertyVetoException e) {}

  }


	 protected void createNavigator() {
		   System.out.println("debug: in create navigator, know filetype is "+filetype+" and this reads "+filetype.equals("log"));

		   String absnavfilename="";
		   if (filetype.equals("log")) {
		   			   absnavfilename=FileNameManager.getInstance().FindNavFileName(absolutefilename,"log");
		   			   System.out.println("debug: in createNavigator, filtype detected as log");
		     }
			else if (filetype.equals("ompro")){
				 absnavfilename=ifd.pv.getStringProperty("Navigator dir",".")+File.separator+filename+".nav";
				  System.out.println("debug: in createNavigator, think type=ompro, nav name="+absnavfilename);
			}
			else if (filetype.equals("pda")){
			 absnavfilename=ifd.pv.getStringProperty("Navigator dir",".")+File.separator+filename+".nav";
				  System.out.println("debug: in createNavigator, think type=argus pda, nav name="+absnavfilename);
			}

		   else if (speinfo!=null) {
			   absnavfilename=ifd.pv.getStringProperty("Navigator dir",".")+File.separator+speinfo.NavFile;
		       System.out.println("debug: in createNavigator, think type=spe");
		     }

		    else
		    {
		   absnavfilename=ifd.pv.getStringProperty("Navigator dir",".")+File.separator+"tmp.nav";
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


public void ToggleBackground(){
	if (jp.myfilt.SUPERIMPOSE) jp.myfilt.SUPERIMPOSE=false;
	 else jp.myfilt.SUPERIMPOSE=true;

	}
public void ToggleAccumulateRulers(boolean accumulate){
	ACCUMULATERULERS=accumulate;
	if (!ACCUMULATERULERS) jp.deleteAllRulers();

}
public void ToggleOnTapAddTrace(boolean add){
   on_tap_plottrace=add;
}

public void findScaleOffset(){
	    int max=Integer.MIN_VALUE;
	 	int min=Integer.MAX_VALUE;
	 	int val;
	 	if (normalize==null){
	 	for (int i=0;i<datArray.length;i++){
	 	 val=datArray[i];
	 	 if (background!=null) val=val-(int)background[i];
	 	 if (val<min) min=val;
	 	 if (val>max) max=val;
	 	 }
	    }
	    else
	    {//find values for normalized data
			offset=min;
			max=Integer.MIN_VALUE;
			min=Integer.MAX_VALUE;
	 	 for (int i=0;i<datArray.length;i++){
		 val=(int)(((double)datArray[i]-background[i])*normalize[i]);
	 	 if (val<min) min=val;
	 	 if (val>max) max=val;
	 	 }
	    }
	     scale=((double)255)/((double)(max-min));
		 scale=scale*0.9;
	     offset=min;
	     System.out.println("in findScaleOffset... max = "+max+" min = "+min+ " scale ="+scale+" offset ="+offset);

	}

public int[] vwlut;
public void rescaleHistogram(int minv, int maxv){
	   int i;
	   double v;
       if (vwlut==null){
		   vwlut=new int[256];
       }
	   double hsc=255.0/(maxv-minv);
	   for (i=0;i<256;i++){
		 v=(i-minv)*hsc;
		 if (v<0) v=0;
		 if (v>255) v=255;
		 vwlut[i]=(int)v;
	   }
	   rescale();
       jp.repaint();
       repaint();
	}




   public void findScaleOffsetRoi(){
	 ROI roi=null;
	 try{
		 for (int i=0;i<jp.rois.size();i++){
	  	  if (((ROI)jp.rois.get(i)).poly.contains(lastMouseX/viewScale, lastMouseY/viewScale)){
	 	   roi=(ROI)jp.rois.get(i);
		   roi.setPixels(); //just in case its not done
		   break;
	     }
       }
     findScaleOffsetRoi(roi);
	}catch (Exception e){e.printStackTrace();}
	}

	public void findScaleOffsetRoi(ROI roi){
		  int max=Integer.MIN_VALUE;
		  	 	int min=Integer.MAX_VALUE;
		  	 	int index,val;
		  	 	if (normalize==null){
		  	 	for (int i=0;i<roi.arrs.length;i++){
				 index=roi.arrs[i];
				 val=datArray[index];
				 if (background!=null) val=val-(int)background[index];
		  	 	 if (val<min) min=val;
		  	 	 if (val>max) max=val;
		  	 	 }
		  	    }
		  	    else
		  	    {
		  			offset=min;
		  			max=Integer.MIN_VALUE;
		  			min=Integer.MAX_VALUE;
		  	 	 for (int i=0;i<roi.arrs.length;i++){
				 index=roi.arrs[i];
		  		 val=(int)(((double)datArray[index]-background[index])*normalize[index]);
		  	 	 if (val<min) min=val;
		  	 	 if (val>max) max=val;
		  	 	 }
		  	    }
		  	     scale=((double)255)/((double)(max-min));
		  		 scale=scale*0.9;
		  	     offset=min;
		  	     System.out.println("in findScaleOffset... max = "+max+" min = "+min+ " scale ="+scale+" offset ="+offset);

		}


    public void rescale(){

	double dval=0;
	int i=0;
	int off=offset;
	double sc=scale;

	if (normalize==null){
	 if (background==null){
	 //show raw
	 for (i=0;i<datArray.length;i++){

	   dval=(((double)datArray[i]-off)*sc);
	   dval=dval*UserScale+UserOffset;
	   if (dval>255)dval=255;
	   if (dval<0) dval=0;
	   viewArray[i]=(int)dval;

	      }
 	   }
 	 else
 	 {//background subtract
	 for (i=0;i<datArray.length;i++){
  	  dval=(((double)datArray[i]-background[i]-off)*sc);
	  dval=dval*UserScale+UserOffset;
	  if (dval>255)dval=255;
      if (dval<0) dval=0;
      viewArray[i]=(int)dval;
	  }
     }
     }
     else
     {//normalize
	  for (i=0;i<datArray.length;i++){
	    dval=((((double)(datArray[i]-background[i])*normalize[i]))*sc);
	    dval=dval*UserScale+UserOffset;
		if (dval>255)dval=255;
		if (dval<0) dval=0;
		viewArray[i]=(int)dval;
	   }
	   //System.out.println("normalizing");
     }

     if (vwlut!=null){
		for (i=0;i<viewArray.length;i++){
		  viewArray[i]=vwlut[viewArray[i]];
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

    public boolean moveImageMode=false;
    public java.awt.Cursor defaultcursor=new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR);
    public java.awt.Cursor handcursor=new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR);
    public void toggleMoveImage(){
		if (moveImageMode){
			jp.setCursor(defaultcursor);
			setCursor(defaultcursor);
			moveImageMode=false;
		}else{
			jp.setCursor(handcursor);
            setCursor(handcursor);

			moveImageMode=true;
		}

	}

	public void recenterImage(){
		jp.offsetx=0;
		jp.offsety=0;
	}


//try to save the image
	public void saveImage(){
        AnalysisHelper.getAnalysisHelper().saveImage(getImage());

		//ImageUtils.WriteImage("analysis/trace.bmp",getImage(),"bmp");
	}

    public BufferedImage getImage(){
		BufferedImage image = new BufferedImage(
		   (int)windowSize.getWidth(),(int)windowSize.getHeight(),BufferedImage.TYPE_BYTE_INDEXED);

		Graphics2D myG = (Graphics2D)image.getGraphics();

		jp.paintComponent(myG);
		return image;

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
		viewScale=viewScale*1.01f;
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

/*
   public void writeMOV(){
	   	    QuickTimeDialog.initialize(null,"mov range", speinfo.NumberOfFrames,ifd.fc);
	   		QuickTimeDialogInfo avd=QuickTimeDialog.showDialog(null, "");
	   		if (avd.valid){
	   		QuickTimeWriter avw=new QuickTimeWriter();

		    try{
	   		ImagePlus ipl=getImagePlus(avd.start,avd.end,1);
	   		//ipl.show();
	   		avw.codecType=avd.codec;
	   		avw.codecQuality=avd.quality;
	   		avw.run(avd.name,ipl);
	   		}catch(Exception e){e.printStackTrace();}
	       }
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
*/
/***********************************************
************************************************/

   public ViewerFilter viewerfilter=null;
   public boolean useviewerfilter=false;
   public void userFilter(){
	if ((viewerfilter!=null)&&(useviewerfilter)){
		viewerfilter.run(this,datArray);
	}

   }

   public void JavaCustomFilter(){
	 if  (useviewerfilter) useviewerfilter=false;
	  else {
		  useviewerfilter=true;
	      normalize=null;
		  background=null;
	  }


   }

/*
   public int[] _backdata;
   public boolean SubtractBackground=false;
   public void subtractBackground(){
	  int i=0;
	  if (SubtractBackground){
	  if (_backdata==null){

		  im.UpdateImageArray(datArray,X_dim,Y_dim,instance);
		  _backdata=new int[X_dim*Y_dim];
		  for (i=0;i<X_dim*Y_dim;i++){
			  _backdata[i]=datArray[i];
		  }
	  }
	  for (i=0;i<X_dim*Y_dim;i++){
		 datArray[i]=datArray[i]-_backdata[i];
	  }
    }
   }

   public void toggleSubtractBackground(){
	 _backdata=null;
	 if (SubtractBackground) SubtractBackground=false;
	 else
	 SubtractBackground=true;

   }
*/
	public void writeGView(){

			ifd.saveImageFile(im);
		}

    /** SilentJumpToFrame is called from other classes that want to generate
        images without updating the display - usually for export**/

    public void SilentJumpToFrame(int i,boolean showextras){
		im.JumpToFrame(i, instance);
		im.UpdateImageArray(datArray, speinfo.X_dim, speinfo.Y_dim, instance);
		//subtractBackground();
		userFilter();
		rescale();
		jp.ARRAYUPDATED=true;
		jp.wr.setPixels(0,0,jp.X_dim,jp.Y_dim,jp.arr);
        jp.filtered=jp.myfilt.go(jp.bi);
        if (showextras){

			java.awt.color.ColorSpace cs=java.awt.color.ColorSpace.getInstance(java.awt.color.ColorSpace.CS_sRGB);
			java.awt.image.ColorConvertOp op=new java.awt.image.ColorConvertOp(cs,null);

		    if (op==null) System.out.println("op=null");
		    BufferedImage tmp=op.filter(jp.filtered,null);
		    jp.filtered=tmp;
			Graphics2D g= (Graphics2D)jp.filtered.getGraphics();
			jp.showExtras(g);
			g.dispose();

		}
	}

	public void JumpToFrame(int framenum){
	im.JumpToFrame(framenum, instance);
	frameNumber=framenum;
	LastUserSetFrame=framenum;
	im.UpdateImageArray(datArray,speinfo.X_dim,speinfo.Y_dim,instance);
	//subtractBackground();
	userFilter();
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
	//subtractBackground();
	userFilter();
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
	//subtractBackground();
	userFilter();
	jp.ARRAYUPDATED=true;
	rescale();
	//notify listeners...
	NotifyFrameListeners(frameNumber);
	repaint();
	}
	}

	public void JavaRaw(){
		useviewerfilter=false;
		normalize=null;
		background=null;
		singleframebackground=false;
		im.UpdateImageArray(datArray,speinfo.X_dim,speinfo.Y_dim, instance);
	    jp.ARRAYUPDATED=true;
	    findScaleOffset();
	    repaint();

	}


    public double[] histdata;
    public double histdatamin;
    public double histdatamax;

    public void dataHistogram(){
		int v,j;
		double dv,drange;
		histdatamax=Integer.MIN_VALUE;
	 	histdatamin=Integer.MAX_VALUE;
	 	if (histdata==null)
	     histdata=new double[256];
	    for (int i=0;i<256;i++) histdata[i]=0;

	    for (int i=0;i<datArray.length;i++){

		  v=datArray[i];
		  if (v>histdatamax) histdatamax=v;
		  if (v<histdatamin) histdatamin=v;

		}
	   drange=histdatamax-histdatamin;
	   if (drange<1){
		   System.out.println("histogram range too small, returning");
		   return;
	   }
	   for (int i=0;i<datArray.length;i++){

		   dv=(double)datArray[i];
		   j=(int)((dv-histdatamin)/drange * 255);
		   histdata[j]=histdata[j]+1;
	   }
	}

   public void viewHistogram(){
		int v,j;
		double dv,drange;
		vwlut=null;
		rescale();
		histdatamax=Integer.MIN_VALUE;
	 	histdatamin=Integer.MAX_VALUE;
	 	if (histdata==null)
	     histdata=new double[256];
	    for (int i=0;i<256;i++) histdata[i]=0;

	    for (int i=0;i<viewArray.length;i++){
		  v=viewArray[i];
		  if (v>histdatamax) histdatamax=v;
		  if (v<histdatamin) histdatamin=v;

		}
	   drange=histdatamax-histdatamin;
	   if (drange<1){
		   System.out.println("histogram range too small, returning");
		   return;
	   }
	   for (int i=0;i<datArray.length;i++){

		   dv=(double)viewArray[i];
		   j=(int)((dv-histdatamin)/drange * 255);
		   histdata[j]=histdata[j]+1;
	   }
	}


    public boolean singleframebackground=false;

	public void JavaBackground(){
	 normalize=null;
     useviewerfilter=false;
	 background=new double[X_dim*Y_dim];
	 singleframebackground=true;
	 im.UpdateImageArray(datArray,speinfo.X_dim,speinfo.Y_dim, instance);
	 jp.ARRAYUPDATED=true;
	 for (int i=0;i<background.length;i++){
		 background[i]=(double)datArray[i];
	 }
     findScaleOffset();
	 repaint();

	}



	public void JavaNormalize(){
     useviewerfilter=false;
	int oldframe=frameNumber;
	int startrange=frameNumber;
	int endrange=frameNumber+100;
	if (endrange>=speinfo.NumberOfFrames) endrange=speinfo.NumberOfFrames-1;

	//determine a range to normalize over
	if (Navigator_end>Navigator_start){startrange=Navigator_start; endrange=Navigator_end;}
	{
	System.out.println("starting normalization calculation");
		normalize=new double[X_dim*Y_dim];
		if (background==null){
		 background=new double[X_dim*Y_dim];
	     singleframebackground=false;
	     }
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

     if (!singleframebackground) background=minval; // keep this.


    for (int l=0;l<normalize.length;l++){
	 normalize[l]=maxnorm/normalize[l];
	}

     im.JumpToFrame(oldframe,instance);


     System.out.println("ending normalization calculation");

    }
     im.UpdateImageArray(datArray,speinfo.X_dim,speinfo.Y_dim, instance);
	 jp.ARRAYUPDATED=true;
	 findScaleOffset();
	 repaint();

	}

   public void OneXAtXY(){
	    double x=(double)lastMouseX;
	    double y=(double)lastMouseY;
	    jp.offsetx=-1*(int)(x*1.0/viewScale-jp.getWidth()/2.0);
	    jp.offsety=-1*(int)(y*1.0/viewScale-jp.getHeight()/2.0);
        setVisualScale(1.0f);
   }


   public void roiout(){
	   System.out.println("roiout");
	   double x=(double)lastMouseX;
	   double y=(double)lastMouseY;
	   for (int i=0;i<jp.rois.size();i++){
	   	   ROI tmp=(ROI)jp.rois.get(i);
	   	   if (tmp.poly.contains(x/viewScale, y/viewScale)){
				 Rectangle r=tmp.poly.getBounds();
				 if ((r.x+r.width<X_dim)&&(r.y+r.height<Y_dim)){
	                System.out.println("opening roi subimage with ("+r.x+","+r.y+","+r.x+r.width+","+r.y+r.height+".");
	                ifd.openImageFile(this, r.x, r.y, r.x+r.width, r.y+r.height, 1);
				    }
			     else System.out.println("Can't open ROI, as part of it is out of bounds");
			     return;
			 }
		 }
   }

   public void binout(int bin){
	   int xd=X_dim;
	   int yd=Y_dim;
	   System.out.println("calling binout "+bin+".");

	   if (( ((int)(xd/bin))*bin+bin>X_dim)||(((int)(yd/bin))*bin+bin>Y_dim)){
		   System.out.println("xd="+xd+",yd="+yd);
		   yd=yd-bin; xd=xd-bin;
		   System.out.println("xd="+xd+",yd="+yd);
		   }


	   ifd.openImageFile(this, 0, 0, xd,yd, bin);

	   System.out.println("called binout "+bin+".");
   }

   public void zoomToRoi(){
	   System.out.println("in zoomtoroi");
       double x=(double)lastMouseX;
       double y=(double)lastMouseY;
	   for (int i=0;i<jp.rois.size();i++){
	   		ROI tmp=(ROI)jp.rois.get(i);
	         if (tmp.poly.contains(x/viewScale, y/viewScale)){
				 Rectangle r=tmp.poly.getBounds();
				 int maxd=r.width;
				 if (r.height>maxd) maxd=r.height;
				 float newscale=viewScale*(X_dim/maxd);
				 System.out.println("rescaled view");
				 jp.offsetx=-1*(int)(r.x*newscale);
				 jp.offsety=-1*(int)(r.y*newscale);
				 //viewScale

				 setVisualScale(newscale);

			 }
		 }


   }

   public int lastMovedX=-1;
   public int lastMovedY=-1;
   public ROI roimoved=null;
   public void moveROI(int x, int y){
	 try{
	//System.out.println("in moveROI");
	for (int i=0;i<jp.rois.size();i++){
		ROI tmp=(ROI)jp.rois.get(i);
	    if (tmp.poly.contains(x/viewScale, y/viewScale)){
		 if (roimoved!=tmp) {lastMovedX=x; lastMovedY=y; roimoved=tmp;}

		   {
			int shiftx=(int)((x-lastMovedX)/viewScale);
			int shifty=(int)((y-lastMovedY)/viewScale);
			tmp.poly.translate(shiftx,shifty);
			if (Math.abs(shiftx)>0) lastMovedX=x;
			if (Math.abs(shifty)>0) lastMovedY=y;

            tmp.numPixels=-1;
            tmp.sumPixels=-1;
            System.out.println("roi="+i+" shiftx="+shiftx+" shifty="+shifty+" lastMovedX="+lastMovedX);
          //  if ((Math.abs(x-lastMovedX)<>5) || (Math.abs(y-lastMovedY)>5)) roimoved=null;

		 }

	    break;
	  }

      }
	}catch(Exception e){e.printStackTrace(); System.out.println("couldn't find roi");}
	jp.repaint();
	}


	public void deleteRoi(){
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

    //creates a  new window containing a single frame and the contents of this roi
    public Matrix ma;
    public void roiToMatrix(){
	  try{
	  	for (int i=0;i<jp.rois.size();i++){
	  	 if (((ROI)jp.rois.get(i)).poly.contains(lastMouseX/viewScale, lastMouseY/viewScale)){
	           Rectangle rect=((ROI)jp.rois.get(i)).poly.getBounds();
	           ma=new Matrix(1,rect.height,rect.width);
	           for (int y=0;y<rect.height;y++){
				   for (int x=0;x<rect.width;x++){
					   ma.dat.set(x,y,(double)datArray[(rect.y+y)*X_dim+(x+rect.x)]);

				 }
			 }

	     	  ifd.openImageFile(ma,"roi ("+rect.x+","+rect.y+","+rect.width+","+rect.height+")");
	     	  ifd.jh.vw[ifd.jh.index].ma=ma;
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

    public void toggleRulerRoiTrace(String mode){
	  on_tap_plottrace=false;
	  if (mode.equals("Ruler")) DRAGMODE=DRAWRULER;
	  else
	  if (mode.equals("ROI")) DRAGMODE=DRAWROI;
	  else
	  if (mode.equals("viewTrace")) DRAGMODE=DRAWTRACE;

	  else
	  if (mode.equals("addTrace")) {DRAGMODE=ADDTRACE; on_tap_plottrace=true;}
      }

    public void toggleSaveJPGSeries(){
		if (jp.SAVEJPGS) jp.SAVEJPGS=false;
		else jp.SAVEJPGS=true;
	}

	 public void toggleSaveBMPSeries(){
			if (jp.SAVEBMPS) jp.SAVEBMPS=false;
			else jp.SAVEBMPS=true;
	}

    /*
	public void toggleAddTrace(){
		if (on_tap_plottrace) on_tap_plottrace=false;
		else on_tap_plottrace=true;
	}
   */
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

	public void quickReplaceROITraces(){
      /*
    	 nav=jh.presentviewer.lastCreatedNavigator
		 tmptr=nav.tg.Traces.get(roinum)
		 rmproi=jh.presentviewer.jp.rois[roinum]
		 rmproi.numPixels=-1
		 rmproi.findAllPixels(ma.xdim,ma.ydim)
         tmptr.replaceTrace2(ma,0,rmproi)
	  */
      if (im instanceof Matrix){
		this.ma=(Matrix)im;
	  for (int t=0;t<lastCreatedNavigator.tg.Traces.size();t++){

		  Trace tr=(Trace)lastCreatedNavigator.tg.Traces.get(t);
		  ROI roi=(ROI)jp.rois.get(t);
		  roi.numPixels=-1;
		  roi.findAllPixels(ma.xdim,ma.ydim);
		  tr.replaceTrace(ma,0,roi);

	  }
      }
	}


	public void quickTrace(){
		int iX,iY;
		if (lastCreatedNavigator!=null){
		if (lastCreatedNavigator.cols==0){
			addTrace();
			return;
		}
	    iX=(int)(lastMouseX/viewScale- jp.offsetx);
		iY=(int)(lastMouseY/viewScale- jp.offsety);
		//
		if (im instanceof Matrix){
		  lastCreatedNavigator.replaceLastTrace((Matrix)im,0,iX,iY);
		}
       }
	}
	/*
	public void glanceTrace(){
		int iX,iY;
				if (lastCreatedNavigator!=null){
				if (lastCreatedNavigator.cols==0){
					addTrace();
					return;
				}
			    iX=(int)(lastMouseX/viewScale);
				iY=(int)(lastMouseY/viewScale);
				//
				if (im instanceof Matrix){
				  Matrix ma=(Matrix)im;
				  //Trace tr= ma.getTraceXY(iX,iY);
				  lastCreatedNavigator.replaceLastTrace(tr);
				}
		       }
	}

	*/





 	public void addTrace(){
       int iX,iY;
	    iX=(int)(lastMouseX/viewScale);
		iY=(int)(lastMouseY/viewScale);
		//
		addTrace(iX,iY);


	}



 /*called from outside...*/

 public Color getRoiColor(){
  if (jp.rois.size()>9) return new Color((int)(Math.random()*155)+100,(int)(Math.random()*155)+100,(int)(Math.random()*155)+100);
  return roicolors[jp.rois.size()];
 }

 public Color getRulerColor(){
   if (jp.rulers.size()>9) return new Color((int)(Math.random()*155)+100,(int)(Math.random()*155)+100,(int)(Math.random()*155)+100);
   return roicolors[jp.rulers.size()];
  }


 public void addTrace(int iX, int iY){

			//
			if (im instanceof Matrix){
			  Color brightColor=getRoiColor();
			  jp.presentroi=new ROI(this,brightColor);
			  jp.presentroi.poly.addPoint(iX,iY);
			  jp.presentroi.poly.addPoint(iX+1,iY);
			  jp.presentroi.poly.addPoint(iX+1,iY+1);
			  jp.presentroi.poly.addPoint(iX,iY+1);
		      jp.rois.add(jp.presentroi);

			  Matrix ma=(Matrix)im;
			  Trace tr= ma.getTraceXY(iX,iY);
			  lastCreatedNavigator.addTrace(tr,jp.presentroi);

			  lastCreatedNavigator.rescale();
			  lastCreatedNavigator.repaint();


			}

	}

	 public void paintComponent(Graphics g) {
        super.paintComponent(g);  //paint background
		//lazy way to detect a resize. should redo CHANGE
		  Dimension tmp=jp.getSize();
				if (!tmp.equals(windowSize)){

					windowSize=tmp;
					setViewScale();
					/*
					this helps avoid the run away resize. Definitely FIX.
					*/
                    try{
						Thread.sleep(500);
					}catch(Exception e){};
			}


		}

    public void actionPerformed(ActionEvent e) {
        //Advance the animation frame.

		frameNumber++;
		im.UpdateImageArray(datArray,X_dim,Y_dim,instance);
		//subtractBackground();
		userFilter();
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

class MatrixSubAction extends AbstractAction{
	Viewer2 vw;
	int startrange;
	int endrange;

	public MatrixSubAction(Viewer2 vw, String txt){
	super(txt);
	this.vw=vw;
	}

	public void setRange(int start, int end){
		startrange=start;
		endrange=end;
		putValue(NAME,("from "+startrange+" to "+endrange));
	}


	public void actionPerformed(ActionEvent e){
     //  System.out.println("matrixsubaction "+startrange+" "+endrange);
       vw.toMatrix(startrange,endrange);
	}
}


class ViewMenuListener implements ActionListener, ItemListener{
 Viewer2 vi;

 public ViewMenuListener(Viewer2 vi){
  this.vi=vi;
  }

public void itemStateChanged(ItemEvent e){
	JCheckBox source = (JCheckBox)(e.getSource());
	String sourcetext=source.getText();
    if (sourcetext.equals("accumulate rulers")){
		vi.ToggleAccumulateRulers(e.getStateChange()==ItemEvent.SELECTED);
	}
}

 //gets popup events
 public void actionPerformed(ActionEvent e) {
        JMenuItem source = (JMenuItem)(e.getSource());
        String sourcetext=source.getText();
        String s = "Action event detected."+ "    Event source: " + source.getText();
        System.out.println(s);
		if (sourcetext.equals("open navigator")){

		 vi.openNavigator();
		}

	if (sourcetext.equals("Ruler mode")){vi.toggleRulerRoiTrace(e.getActionCommand());}
	else if (sourcetext.equals("ROI mode")){vi.toggleRulerRoiTrace(e.getActionCommand());}
	else if (sourcetext.equals("view trace")){vi.toggleRulerRoiTrace(e.getActionCommand());}
    else if (sourcetext.equals("add trace")){vi.toggleRulerRoiTrace(e.getActionCommand());}
    else if (sourcetext.equals("all frames")){vi.toMatrix();}

	//else if (sourcetext.equals("addtrace")){vi.toggleAddTrace();}
	else if (sourcetext.equals("export control")){vi.openExportWindow();}

	else if (sourcetext.equals("save gview")){vi.writeGView();}
	else if (sourcetext.equals("to clipboard")){vi.toClipboard();}
	else if (sourcetext.equals("delete last ruler")){vi.jp.deleteLastRuler();}
	else if (sourcetext.equals("delete all rulers")){vi.jp.deleteAllRulers();}

	else if (sourcetext.equals("toggle background")){ vi.ToggleBackground();}
    else if (sourcetext.equals("rescale")){ vi.findScaleOffset();}
	else if (sourcetext.equals("1.0x mouse")) {vi.OneXAtXY();}
	else if (sourcetext.equals("1.0x")) {vi.jp.offsetx=0; vi.jp.offsety=0;vi.setVisualScale(1.0f);}
	else if (sourcetext.equals("0.5x")) {vi.jp.offsetx=0; vi.jp.offsety=0;vi.setVisualScale(0.5f);}
	else if (sourcetext.equals("0.25x")) {vi.jp.offsetx=0; vi.jp.offsety=0;vi.setVisualScale(0.25f);}
	else if (sourcetext.equals("auto")) {vi.jp.offsetx=0; vi.jp.offsety=0;vi.setVisualScale(0f);}
	else if (sourcetext.equals("ROI zoom")) {vi.zoomToRoi();}
	else if (sourcetext.equals("toggle move")) {vi.toggleMoveImage();}
	else if (sourcetext.equals("recenter")){vi.recenterImage();}
	else if (sourcetext.equals("nearest")) {vi.setInterpolation(0);}
	else if (sourcetext.equals("bilinear")) {vi.setInterpolation(1);}
	else if (sourcetext.equals("bicubic")) {vi.setInterpolation(2);}
	else if (sourcetext.equals("bicubic2")) {vi.setInterpolation(3);}
	else if (sourcetext.equals("delete roi")){ vi.deleteRoi();}
	else if (sourcetext.equals("to Matrix")){vi.roiToMatrix();}
	else if (sourcetext.equals("delete all")){ vi.jp.deleteAllROIs();}
	else if (sourcetext.equals("matrix replace")){vi.quickReplaceROITraces();}
	else if (sourcetext.equals("toggle filter")){ vi.toggleFilter();}
	else if (sourcetext.equals("next filter")){ vi.nextFilter();}
	else if (sourcetext.equals("process rois")){ vi.processRois(0,-1);}
	else if (sourcetext.equals("process range")){vi.processRange();}
	else if (sourcetext.equals("scale to roi")){ vi.findScaleOffsetRoi();}
	else if (sourcetext.equals("color rois")){ vi.changeRoiColor();}
	else if (sourcetext.equals("toggle rect/poly")){ vi.toggleROIMode();}
	else if (sourcetext.equals("close roi")){ vi.jp.rois.add(vi.jp.presentroi); vi.jp.presentroi=null; System.out.println("closed roi");}
	else if (sourcetext.equals("delete pt")){ /*vi.deleteRoiPt();*/}
	else if (sourcetext.equals("unit roi")){vi.createUnitRoi();}
	else if (sourcetext.equals("fast unit roi")){vi.fastUnitRoi();}
	else if (sourcetext.equals("save image")){vi.saveImage();}
	else if (sourcetext.equals("save rois")){vi.saveROIs();}
	else if (sourcetext.equals("load rois")){vi.loadROIs();}
	else if (sourcetext.equals("2x bin")){vi.binout(2);}
    else if (sourcetext.equals("3x bin")){vi.binout(3);}
    else if (sourcetext.equals("4x bin")){vi.binout(4);}
    else if (sourcetext.equals("5x bin")){vi.binout(5);}
    else if (sourcetext.equals("10x bin")){vi.binout(10);}
    else if (sourcetext.equals("20x bin")){vi.binout(20);}
    else if (sourcetext.equals("ROI out")){vi.roiout();}

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
	 if (vi.on_tap_plottrace){
		 maybeShowPopup(e);
		 return;
	     }
	 if((e.getModifiers() & InputEvent.BUTTON1_MASK)
	     == InputEvent.BUTTON1_MASK){
		 if (vi.DRAGMODE==vi.DRAWROI){
		  x = e.getX();
		  y = e.getY();
         if ((!shifton)||(vi.jp.presentroi==null))
		 vi.jp.presentroi=new ROI(vi,vi.getRoiColor());
         vi.jp.presentroi.firstX=(int)((float)x/vi.viewScale+vi.jp.offsetx);
		 vi.jp.presentroi.firstY=(int)((float)y/vi.viewScale+vi.jp.offsety);

		 vi.lastMouseX=x;
		 vi.lastMouseY=y;
		  if (e.isAltDown()){
			  System.out.println("alt - graphing");
			  vi.drawOnNavigator(e.getX(),e.getY());
		  }else
          if (e.isShiftDown()){
			 System.out.println("shift detected on click");
			 vi.jp.presentroi.poly.addPoint((int)((float)e.getX()/vi.viewScale+vi.jp.offsetx),(int)((float)e.getY()/vi.viewScale+vi.jp.offsety));
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

			 vi.jp.presentruler=new Ruler(vi,vi.getRulerColor());
			 vi.jp.presentruler.firstX=(int)((float)x/vi.viewScale+vi.jp.offsetx);
			 vi.jp.presentruler.firstY=(int)((float)y/vi.viewScale+vi.jp.offsety);
			 System.out.println("Created new ruler, firstX="+ vi.jp.presentruler.firstX);
		  }

	     }
	     maybeShowPopup(e);
		}

	  public void mouseDragged(MouseEvent e) {
		 if (vi.on_tap_plottrace) return;

		 if((e.getModifiers() & InputEvent.BUTTON1_MASK)
		 	== InputEvent.BUTTON1_MASK){
			if (vi.DRAGMODE==vi.DRAWROI){
			if (e.isControlDown()){
              vi.moveROI(e.getX(),e.getY());
			}else
		    if (!e.isShiftDown()){
		    vi.MouseOverPixel(e.getX(),e.getY());
			//vi.resetSelection(x,y,e.getX(),e.getY());
			//vi.repaint();
			if (vi.ROIMODE==vi.FREEFORMROI)
			 vi.jp.presentroi.poly.addPoint((int)((float)e.getX()/vi.viewScale+vi.jp.offsetx),(int)((float)e.getY()/vi.viewScale+vi.jp.offsety));
			if (vi.ROIMODE==vi.RECTANGLEROI){

			 vi.jp.presentroi.poly=new Polygon();
			 vi.jp.presentroi.poly.addPoint(vi.jp.presentroi.firstX,vi.jp.presentroi.firstY);
			 vi.jp.presentroi.poly.addPoint(vi.jp.presentroi.firstX, (int)((float)e.getY()/vi.viewScale+vi.jp.offsety));
			 vi.jp.presentroi.poly.addPoint((int)((float)e.getX()/vi.viewScale+vi.jp.offsetx),(int)((float)e.getY()/vi.viewScale+vi.jp.offsety));
			 vi.jp.presentroi.poly.addPoint((int)((float)e.getX()/vi.viewScale+vi.jp.offsetx), vi.jp.presentroi.firstY);
             System.out.println("firstX="+vi.jp.presentroi.firstX);
			}


			dragged=true;
			vi.lastMouseX=e.getX();
			vi.lastMouseY=e.getY();
			vi.jp.repaint();
		    vi.repaint();
		 }
	    }//drawroi
	    else
	    if (vi.DRAGMODE==vi.DRAWRULER){
		//drawruler mode
		 x=e.getX();
	     y=e.getY();
	     vi.jp.presentruler.lastX=(int)((float)x/vi.viewScale+vi.jp.offsetx);
		 vi.jp.presentruler.lastY=(int)((float)y/vi.viewScale+vi.jp.offsety);
		 vi.jp.repaint();
		 vi.repaint();
		  }

		 else
		 if (vi.DRAGMODE==vi.DRAWTRACE){
			vi.lastMouseX=e.getX();
			vi.lastMouseY=e.getY();
			vi.quickTrace();
		 }

		}
	   }


  public void mouseReleased(MouseEvent e) {
	   //if ((vi.roimoved!=null) && (vi.nav!=null) && (vi.nav.x_offset>0) && (vi.nav.x_range<500)) vi.processRange();

	   vi.roimoved=null;
	   System.out.println("mouse released");
        if (vi.on_tap_plottrace){
			if (!maybeShowPopup(e)){
			vi.lastMouseX=e.getX();
			vi.lastMouseY=e.getY();
			vi.addTrace();
			return;
		  }
		}
		if((e.getModifiers() & InputEvent.BUTTON1_MASK)
			== InputEvent.BUTTON1_MASK){
			if (vi.DRAGMODE==vi.DRAWROI){
			if (!e.isShiftDown()){
				shifton=false;
			if (dragged){
			//vi.showSelection=false;
			//vi.rescaleToSelection();
			if (!e.isControlDown()){
			if (vi.jp.presentroi.poly.npoints>3){
			  vi.jp.rois.add(vi.jp.presentroi);
			  }else {vi.jp.presentroi=null; System.out.println("deleted presentroi.. ");}
		     }else{
				 vi.lastMovedX=-1;
				 vi.lastMovedY=-1;

			 }
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
      else
      if (vi.DRAGMODE==vi.DRAWRULER){
	   //dragmode=drawruler
        vi.jp.presentruler.updateResults();
        if (vi.ACCUMULATERULERS) vi.jp.rulers.add(vi.jp.presentruler);
		  }
	  else
	  if (vi.DRAGMODE==vi.DRAWTRACE){


       }
      }
      vi.updateTitle();

     }


 private boolean maybeShowPopup(MouseEvent e) {
              boolean showedpopup=false;
			  System.out.println("checking popup");
			  if((e.getModifiers() & InputEvent.BUTTON3_MASK)== InputEvent.BUTTON3_MASK){
              showedpopup=true;
			  vi.lastMouseX=e.getX();
			  vi.lastMouseY=e.getY();
			    if (e.isShiftDown()){vi.popupROI.show(e.getComponent(),e.getX(), e.getY());}
			     else
				  vi.popup.show(e.getComponent(),
							 e.getX(), e.getY());
			  }

			return showedpopup;
			}

    }

class ViewFrame extends aFrame{
Viewer2 vw;

	public ViewFrame(Viewer2 vw){
		super((JPanel)vw.getContentPane(),aFrame.Internal_Frame,vw.getTitle(),vw.ifd);
		//aframe=new aFrame((JPanel)this.getContentPane(),aFrame.Internal_Frame,getTitle(),ifd);
      this.vw=vw;
      }

    public void aFrameResized(){
	  System.out.println("Viewer2 resize via aframe");
	  vw.windowSize=vw.jp.getSize();
	  vw.setViewScale();
	}
	public void aFrameActivated(){
		if (vw!=null)
		 vw.notifyHelpers();
	}
}