package gvdecoder;
import javax.swing.JInternalFrame;
//import org.jscroll.JScrollDesktopPane;
import javax.swing.JDesktopPane;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JMenuBar;
import javax.swing.JFrame;
import javax.swing.filechooser.*;
import javax.swing.*;
import javax.swing.event.*;
import java.util.*;
import java.io.*;
import gvdecoder.log.*;
import gvdecoder.trace.*;

import prosilica.jni.*;

import java.awt.event.*;
import java.awt.*;
import gvdecoder.prefs.*;

import java.nio.*;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

//new
public class GView3{                        // extends JFrame {
    public JDesktopPane desktop;

    //public FileDialog fc=new FileDialog(this,"open");

    public JFrame mainWindow; //new

	public FilePicker fc;//=FilePicker.getFilePicker();
	public PropertyHelper ph=PropertyHelper.getPropertyHelper();
	public final PropertyViewer pv=new PropertyViewer(this);
	boolean FileChooserActivated=false;
	public boolean OpenDirectory=false; //FileChooser is too slow, and filedialog has no way to select directories
    public CursorNavigatorFrame ccf;
    ROIControllerFrame roif;
    RulerControllerFrame rulerf;
    public ExportControllerFrame ex; //public here for debug.
    TagNavigatorFrame tcf;
    PrefManagerFrame pmf;
    //final LogManager logger=LogManager.getInstance();
    final PrefManager prefs=PrefManager.getInstance();
    public JythonHelper2 jh;
    Matrix test;
    public JythonViewer jv;
    public ProgressDisplayer pm;
    public JPopupMenu popup;
    //full screen support

    public GraphicsDevice graphicsdevice;
    public DisplayMode originalDM;


    public static  GView3 gv;

    public GView3Server GView3server;

    public JMenu windowList;

    boolean isFullScreen=false;




    public GView3(GraphicsDevice graphicsdevice,boolean fullscreen  ) {

        mainWindow=new JFrame("GView3");  //new

        this.gv=this;
        this.graphicsdevice=graphicsdevice;

        LogManager.getInstance().log( "intitialized LogManager" );
        System.out.println("in this new GView3 prior to init jh");
        jh=new JythonHelper2();
        jh.gv=this;
        pm=new ProgressDisplayer(this);


		//Make the big window be indented 50 pixels from each edge
        //of the screen.



        isFullScreen = graphicsdevice.isFullScreenSupported();
		if (!fullscreen) isFullScreen=false;

        originalDM=graphicsdevice.getDisplayMode();

		setupGUI(isFullScreen);



	    popup=new JPopupMenu();
	    //Make dragging faster:
        //desktop.putClientProperty("JDesktopPane.dragMode", "outline");



        ViewerParams.getInstance();//force instance of ViewerParams in case user uses this before opening a window
        NavigatorGraphParams.getInstance();
        fc=FilePicker.getFilePicker();
     }

public void setupGUI(boolean  fullscreen){


	  JInternalFrame[] frames=null;
			   if (desktop!=null) {
				   frames=desktop.getAllFrames();
			   }

       int inset = 150;
		if (fullscreen) {
              mainWindow.dispose();
              mainWindow=new JFrame("GView3");  //new
              desktop = new JDesktopPane(); //a specialized layered pane
			  		        //createFrame(); //Create first window
			  desktop.setDesktopManager(new gvDesktopManager(this));
	          if (frames!=null){
				  for (int i=0;i<frames.length;i++){
				   desktop.add(frames[i]);
				  }
			  }
	          mainWindow.setContentPane(desktop);
			  mainWindow.setUndecorated(fullscreen);
		      mainWindow.setResizable(true);
			   createMenuBar(fullscreen);
			   gvListener gvlistener = new gvListener(this);
			   mainWindow.addMouseListener(gvlistener);
			   mainWindow.addMouseMotionListener(gvlistener);
			   // Full-screen mode
			   //graphicsdevice.setFullScreenWindow(mainWindow);
			   mainWindow.validate();
			   Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
			    mainWindow.setSize(screenSize);
			   //showAllFrames();
			   isFullScreen=true;
			   mainWindow.setVisible(true);
			    } else
			       {
					if (isFullScreen){
					//hideAllFrames();
					mainWindow.dispose();
              		mainWindow=new JFrame("GView3");  //new
				   }
				   desktop = new JDesktopPane(); //a specialized layered pane
				   								  		        //createFrame(); //Create first window
				   						          if (frames!=null){
				   									  for (int i=0;i<frames.length;i++){
				   									   desktop.add(frames[i]);
				   									  }
				   			       }
				   	desktop.setDesktopManager(new gvDesktopManager(this));
			  		mainWindow.setContentPane(desktop);
					mainWindow.setUndecorated(fullscreen);
					mainWindow.setResizable(!fullscreen);
					mainWindow.setJMenuBar(createMenuBar(fullscreen));
					//gvListener gvlistener = new gvListener(this);
					//mainWindow.addMouseListener(gvlistener);
					//mainWindow.addMouseMotionListener(gvlistener);

					mainWindow.addWindowListener(new WindowAdapter() {  public void windowClosing(WindowEvent e){ System.exit(0);} });
			        mainWindow.pack();
			        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
				    Dimension winSize=new Dimension(screenSize.width - inset*2, screenSize.height-inset*2);
				    mainWindow.setBounds(inset, inset, screenSize.width - inset*2, screenSize.height-inset*2);
				    mainWindow.setSize(winSize);
			        mainWindow.setVisible(true);
	        	    showAllFrames();
	        	    isFullScreen=false;
	         }


}

   public static GView3 getGView3(){
	   return gv;
   }

  public void startserver(){
	    GView3server=new GView3Server(this);
        GView3server.start();
   }

public void notifyAFrames(JInternalFrame f){
	if (f instanceof iFrame) ((iFrame)f).checkUnDock();
}

 protected JMenuBar createMenuBar(boolean fullscreen) {


		popup=new JPopupMenu();
        JMenuBar menuBar = new JMenuBar();

        JMenu menu = new JMenu("File");
        menu.setMnemonic(KeyEvent.VK_F);
        JMenuItem menuItem;

       JMenu submenu=new JMenu("Open data");
       submenu.setMnemonic(KeyEvent.VK_D);
	   menuItem = new JMenuItem("SPE");
	   				        menuItem.setMnemonic(KeyEvent.VK_S);
	   				        menuItem.addActionListener(new ActionListener() {
	   				            public void actionPerformed(ActionEvent e) {
								    OpenDirectory=false;
	   				                openSPE();
	   				            }
	   				        });
	           submenu.add(menuItem);

	   menuItem = new JMenuItem("CWRU");
								menuItem.setMnemonic(KeyEvent.VK_C);
								menuItem.addActionListener(new ActionListener() {
									public void actionPerformed(ActionEvent e) {
										OpenDirectory=false;
										openCWRU();
									}
								});
	   submenu.add(menuItem);

      	menuItem = new JMenuItem("CWRU File Control");
	  	  							menuItem.setMnemonic(KeyEvent.VK_F);
	  	  							menuItem.addActionListener(new ActionListener() {
	  	  								public void actionPerformed(ActionEvent e) {

	  	  									openImageFileControl();
	  	  								}
	  	  							});
			   submenu.add(menuItem);


      menuItem = new JMenuItem("Biorad Pic");
								menuItem.setMnemonic(KeyEvent.VK_P);
								menuItem.addActionListener(new ActionListener() {
									public void actionPerformed(ActionEvent e) {
										OpenDirectory=false;
										openBioRadPic();
									}
								});
	   submenu.add(menuItem);
       JMenu subsubmenu=new JMenu("RedShirt");
	    menuItem = new JMenuItem("memresident 1x");
	   								menuItem.setMnemonic(KeyEvent.VK_R);
	   								menuItem.addActionListener(new ActionListener() {
	   									public void actionPerformed(ActionEvent e) {
	   										OpenDirectory=false;
	   										openRedShirt(1);
	   									}
	   								});
	   	   subsubmenu.add(menuItem);
	     menuItem = new JMenuItem("memresident 10x");
	   								menuItem.setMnemonic(KeyEvent.VK_R);
	   								menuItem.addActionListener(new ActionListener() {
	   									public void actionPerformed(ActionEvent e) {
	   										OpenDirectory=false;
	   										openRedShirt(10);
	   									}
	   								});
	   	   subsubmenu.add(menuItem);
          menuItem = new JMenuItem("memresident 50x");
	   								menuItem.setMnemonic(KeyEvent.VK_R);
	   								menuItem.addActionListener(new ActionListener() {
	   									public void actionPerformed(ActionEvent e) {
	   										OpenDirectory=false;
	   										openRedShirt(50);
	   									}
	   								});
	   	  subsubmenu.add(menuItem);

         menuItem = new JMenuItem("frame by frame 1x");
	   								menuItem.setMnemonic(KeyEvent.VK_R);
	   								menuItem.addActionListener(new ActionListener() {
	   									public void actionPerformed(ActionEvent e) {
	   										OpenDirectory=false;
	   										openBigRedShirt(1);
	   									}
	   								});
	   	   subsubmenu.add(menuItem);
          menuItem = new JMenuItem("frame by frame 10x");
	   								menuItem.setMnemonic(KeyEvent.VK_R);
	   								menuItem.addActionListener(new ActionListener() {
	   									public void actionPerformed(ActionEvent e) {
	   										OpenDirectory=false;
	   										openBigRedShirt(10);
	   									}
	   								});
	   	  subsubmenu.add(menuItem);
          menuItem = new JMenuItem("frame by frame 50x");
	   								menuItem.setMnemonic(KeyEvent.VK_R);
	   								menuItem.addActionListener(new ActionListener() {
	   									public void actionPerformed(ActionEvent e) {
	   										OpenDirectory=false;
	   										openBigRedShirt(50);
	   									}
	   								});
	   	  subsubmenu.add(menuItem);

          submenu.add(subsubmenu);

    menuItem = new JMenuItem("OMPRO");
								menuItem.setMnemonic(KeyEvent.VK_O);
								menuItem.addActionListener(new ActionListener() {
									public void actionPerformed(ActionEvent e) {
										OpenDirectory=false;
										openOMPRO();
									}
								});
	   submenu.add(menuItem);



menuItem = new JMenuItem("Argus");
								menuItem.setMnemonic(KeyEvent.VK_A);
								menuItem.addActionListener(new ActionListener() {
									public void actionPerformed(ActionEvent e) {
										OpenDirectory=false;
										openArgus();
									}
								});
	   submenu.add(menuItem);

menuItem = new JMenuItem("Prosilica");
								menuItem.setMnemonic(KeyEvent.VK_P);
								menuItem.addActionListener(new ActionListener() {
									public void actionPerformed(ActionEvent e) {
										OpenDirectory=false;
										openProsilica();
									}
								});
	   submenu.add(menuItem);




menuItem = new JMenuItem("GView3Format");
								menuItem.setMnemonic(KeyEvent.VK_G);
								menuItem.addActionListener(new ActionListener() {
									public void actionPerformed(ActionEvent e) {
										OpenDirectory=false;
										openGView3Format();
									}
								});
	   submenu.add(menuItem);

menuItem = new JMenuItem("Open 8-bit AVI");
								menuItem.setMnemonic(KeyEvent.VK_A);
								menuItem.addActionListener(new ActionListener() {
									public void actionPerformed(ActionEvent e) {
									    OpenDirectory=false;
										openImageFile("avi");
									}
								});
				   submenu.add(menuItem);

menuItem = new JMenuItem("Open Multipage Tiff");
				   								menuItem.setMnemonic(KeyEvent.VK_M);
				   								menuItem.addActionListener(new ActionListener() {
				   									public void actionPerformed(ActionEvent e) {
				   									    OpenDirectory=false;
				   										openImageFile("mtf");
				   									}
				   								});
				   submenu.add(menuItem);
    menu.add(submenu);


menuItem = new JMenuItem("Open Cascade");
				   								menuItem.setMnemonic(KeyEvent.VK_M);
				   								menuItem.addActionListener(new ActionListener() {
				   									public void actionPerformed(ActionEvent e) {
				   									    OpenDirectory=false;
				   										openImageFile("cas");
				   									}
				   								});
				   submenu.add(menuItem);
menuItem = new JMenuItem("OMPRO series");
								menuItem.setMnemonic(KeyEvent.VK_S);
								menuItem.addActionListener(new ActionListener() {
									public void actionPerformed(ActionEvent e) {
										OpenDirectory=true;
										openOMPROSeries();
									}
								});
	   submenu.add(menuItem);
    menu.add(submenu);
	menu.addSeparator();





	menuItem = new JMenuItem("Open Tif Series");
							menuItem.setMnemonic(KeyEvent.VK_T);
							menuItem.addActionListener(new ActionListener() {
								public void actionPerformed(ActionEvent e) {
								    OpenDirectory=true;
									openImageFile("tif");
								}
							});
			   menu.add(menuItem);

		menuItem = new JMenuItem("Open Gif Series");
							menuItem.setMnemonic(KeyEvent.VK_G);
							menuItem.addActionListener(new ActionListener() {
								public void actionPerformed(ActionEvent e) {
									OpenDirectory=true;
									openImageFile("gif");
								}
							});
			   menu.add(menuItem);

	  menuItem = new JMenuItem("Open Bmp Series");
							menuItem.setMnemonic(KeyEvent.VK_B);
							menuItem.addActionListener(new ActionListener() {
								public void actionPerformed(ActionEvent e) {
									OpenDirectory=true;
									openImageFile("bmp");
								}
							});
			   menu.add(menuItem);

	  menuItem = new JMenuItem("Open Jpg Series");
							menuItem.setMnemonic(KeyEvent.VK_J);
							menuItem.addActionListener(new ActionListener() {
								public void actionPerformed(ActionEvent e) {
									OpenDirectory=true;
									openImageFile("JPG");
								}
							});
			   menu.add(menuItem);
	 menu.addSeparator();
     menuItem = new JMenuItem("Open Series");
							menuItem.setMnemonic(KeyEvent.VK_S);
							menuItem.addActionListener(new ActionListener() {
								public void actionPerformed(ActionEvent e) {
									OpenDirectory=true;
									openImageFile("");
								}
							});
			   menu.add(menuItem);
	 menuItem = new JMenuItem("Open Single");
							menuItem.setMnemonic(KeyEvent.VK_I);
							menuItem.addActionListener(new ActionListener() {
								public void actionPerformed(ActionEvent e) {
									OpenDirectory=false;
									openImageFile("");
								}
							});
			   menu.add(menuItem);

	  // menuBar.add(menu);
       if (fullscreen) popup.add(menu); else menuBar.add(menu);
    menu = new JMenu("Scripts");
    menu.setMnemonic(KeyEvent.VK_S);
    menuItem = new JMenuItem("Jython window");
	  								menuItem.setMnemonic(KeyEvent.VK_J);
	  								menuItem.addActionListener(new ActionListener() {
	  									public void actionPerformed(ActionEvent e) {
	  										openJythonWindow();
	  									}
	  								});
	menu.add(menuItem);
    menu.addSeparator();
    menuItem = new JMenuItem("Run script");
	  								menuItem.setMnemonic(KeyEvent.VK_R);
	  								menuItem.addActionListener(new ActionListener() {
	  									public void actionPerformed(ActionEvent e) {
	  										runJythonScript();
	  									}
	  								});
	menu.add(menuItem);


   menuItem = new JMenuItem("Load script");
	  								menuItem.setMnemonic(KeyEvent.VK_L);
	  								menuItem.addActionListener(new ActionListener() {
	  									public void actionPerformed(ActionEvent e) {
	  										loadJythonScript();
	  									}
	  								});
	menu.add(menuItem);

    //menuBar.add(menu);
    if (fullscreen) popup.add(menu); else menuBar.add(menu);

    menu = new JMenu("Prefs and Controls");
    menu.setMnemonic(KeyEvent.VK_C);
    menuItem = new JMenuItem("Directories");
    menuItem.setToolTipText("Set default output directories");
					        menuItem.setMnemonic(KeyEvent.VK_P);
					        menuItem.addActionListener(new ActionListener() {
					            public void actionPerformed(ActionEvent e) {
					                createProperties();
					            }
					        });
	menu.add(menuItem);
    menu.addSeparator();

       menuItem = new JMenuItem("Open Cursor Control");
	  							menuItem.setMnemonic(KeyEvent.VK_C);
	  							menuItem.addActionListener(new ActionListener() {
	  								public void actionPerformed(ActionEvent e) {

	  									openCursorControl();
	  								}
	  							});
			   menu.add(menuItem);
          menuItem = new JMenuItem("Open Prefs Control");
	  							menuItem.setMnemonic(KeyEvent.VK_F);
	  							menuItem.addActionListener(new ActionListener() {
	  								public void actionPerformed(ActionEvent e) {

	  									openPrefsControl();
	  								}
	  							});
			   menu.add(menuItem);
          menuItem = new JMenuItem("Open ROI Control");
	  							menuItem.setMnemonic(KeyEvent.VK_R);
	  							menuItem.addActionListener(new ActionListener() {
	  								public void actionPerformed(ActionEvent e) {

	  									openROIControl();
	  								}
	  							});
			   menu.add(menuItem);
		menuItem = new JMenuItem("Open ruler Control");
	  							menuItem.setMnemonic(KeyEvent.VK_R);
	  							menuItem.addActionListener(new ActionListener() {
	  								public void actionPerformed(ActionEvent e) {

	  									openRulerControl();
	  								}
	  							});
			   menu.add(menuItem);

		menu.addSeparator();

		submenu=new JMenu("Freemind Server");
        submenu.setToolTipText("For use with a modified version of freemind only");
        menuItem = new JMenuItem("Start Server");
	  							menuItem.setMnemonic(KeyEvent.VK_R);
	  							menuItem.addActionListener(new ActionListener() {
	  								public void actionPerformed(ActionEvent e) {
	  									startServerControl();
	  								}
	  							});
			  submenu.add(menuItem);

		 menuItem = new JMenuItem("Stop Server");
	  							menuItem.setMnemonic(KeyEvent.VK_R);
	  							menuItem.addActionListener(new ActionListener() {
	  								public void actionPerformed(ActionEvent e) {
	  									stopServerControl();
	  								}
	  							});
			   submenu.add(menuItem);
         menu.add(submenu);
         menu.addSeparator();
         submenu=new JMenu("User Interface prefs");
			menuItem = new JMenuItem("Use Native FileDialog");
	  							menuItem.setMnemonic(KeyEvent.VK_R);
	  							menuItem.setToolTipText("Use this dialog (only) if the swing dialog is too slow");
	  							menuItem.addActionListener(new ActionListener() {
	  								public void actionPerformed(ActionEvent e) {
	  									FilePicker.getFilePicker().switchToNativeDialog();
	  								}
	  							});
			submenu.add(menuItem);
         	menuItem = new JMenuItem("Use Swing FileDialog");
		 	  							menuItem.setMnemonic(KeyEvent.VK_S);
		 	  							menuItem.setToolTipText("This dialog allows directory selection");
		 	  							menuItem.addActionListener(new ActionListener() {
		 	  								public void actionPerformed(ActionEvent e) {
		 	  									FilePicker.getFilePicker().switchToSwingDialog();
		 	  								}
		 	  							});
   		     submenu.add(menuItem);
   		    menuItem = new JMenuItem("Toggle FullScreen");
	  							menuItem.setMnemonic(KeyEvent.VK_F);
	  							menuItem.setToolTipText("Fullscreen exclusive use right click to see menus");
	  							menuItem.addActionListener(new ActionListener() {
	  								public void actionPerformed(ActionEvent e) {
	  									toggleFullScreen();
	  								}
	  							});
			submenu.add(menuItem);


         menu.add(submenu);
         menu.addSeparator();
         menuItem = new JMenuItem("New Note");
		 	  							menuItem.setMnemonic(KeyEvent.VK_N);
		 	  							menuItem.setToolTipText("Open editable note viewer");
		 	  							menuItem.addActionListener(new ActionListener() {
		 	  								public void actionPerformed(ActionEvent e) {
		 	  									newHTMLReportEditor();
		 	  								}
	  							});
        menu.add(menuItem);
        menuItem = new JMenuItem("Open Note");
		 	  							menuItem.setMnemonic(KeyEvent.VK_N);
		 	  							menuItem.setToolTipText("Open editable note viewer");
		 	  							menuItem.addActionListener(new ActionListener() {
		 	  								public void actionPerformed(ActionEvent e) {
		 	  									openHTMLReportEditor();
		 	  								}
	  							});
        menu.add(menuItem);

       // menuBar.add(menu);
        if (fullscreen) popup.add(menu); else menuBar.add(menu);
        menu= new JMenu("Windows");
         menu.setMnemonic(KeyEvent.VK_W);

        menuItem = new JMenuItem("Close All");
					  							menuItem.setMnemonic(KeyEvent.VK_C);
					  							menuItem.addActionListener(new ActionListener() {
					  								public void actionPerformed(ActionEvent e) {

					  									closeAllWindows();
					  								}
					  							});
	    menu.add(menuItem);
        menu.addSeparator();

        windowList=new JMenu("window list");
        menu.add(windowList);



        //menuBar.add(menu);
         if (fullscreen) popup.add(menu); else menuBar.add(menu);

        menu = new JMenu("Help");
        menu.setMnemonic(KeyEvent.VK_H);
        menuItem = new JMenuItem("Manual");
			  							menuItem.setMnemonic(KeyEvent.VK_R);
			  							menuItem.addActionListener(new ActionListener() {
			  								public void actionPerformed(ActionEvent e) {

			  									openHelp();
			  								}
			  							});
					   menu.add(menuItem);

         menuItem = new JMenuItem("About");
			  							menuItem.setMnemonic(KeyEvent.VK_R);
			  							menuItem.addActionListener(new ActionListener() {
			  								public void actionPerformed(ActionEvent e) {

			  									openAbout();
			  								}
			  							});
	    menu.add(menuItem);
	    if (fullscreen) popup.add(menu); else menuBar.add(menu);

	    menu=new JMenu("Exit");
	    menuItem=new JMenuItem("quit");
	    menuItem.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
			   exitProgram();
			 }
	     });
	     menu.add(menuItem);

        //menuBar.add(menu);
         if (fullscreen) popup.add(menu); else menuBar.add(menu);

        return menuBar;

      // return null;
    }

public void toggleFullScreen(){
	if (isFullScreen){
		  try{
			     if (originalDM!=null)
			      graphicsdevice.setDisplayMode(originalDM);
	     }catch(Exception e){;}
	     setupGUI(false);
	     isFullScreen=false;
	 }else{
		 setupGUI(true);
		 isFullScreen=true;

	 }




}

   public void createViewer(){

   }
   public void createProperties(){

	  pv.setVisible(true);
	  desktop.add(pv);
	   try{
		pv.setSelected(true);
		}catch(java.beans.PropertyVetoException e){}
   }

   public void openSPE(){
      openImageFile("spe");
	  }
   public void openCWRU(){
      openImageFile("log");
      }
   public void openBioRadPic(){
	  openImageFile("pic");
      }

   public void openOMPRO(){
	   openImageFile("ompro");
   }

   public void openArgus(){
	   openImageFile("pda");
   }

   public void openProsilica(){
     openImageFile("pro");
   }

   public void openCascade(){
	   openImageFile("cas");
   }

   public void openGView3Format(){
   	   openImageFile("gv");
   }

   public void openRedShirt(int k){
	   openImageFile("red",k);
   }


   public void openBigRedShirt(int k){
	   openImageFile("bred",k);
   }


   public JInternalFrame[] getWindows(){
	   return desktop.getAllFrames();
   }

   public void hideAllFrames(){
	 JInternalFrame[] frames=desktop.getAllFrames();
	 if (frames==null) return;
	 try{
	 for (int i=0;i<frames.length;i++){
			 frames[i].setIcon(true);
	   }
   }catch(Exception e){;}
	   mainWindow.repaint();
   }

 public void showAllFrames(){
	   if (desktop==null) return;
	   JInternalFrame[] frames=desktop.getAllFrames();
	   if (frames==null) return;
	   try{
	   for (int i=0;i<frames.length;i++){
			 frames[i].setIcon(false);
	   }
      }catch(Exception e){;}
	   mainWindow.repaint();
   }



   public void closeAllWindows(){
	   JInternalFrame[] frames=getWindows();
	   for (int i=0;i<frames.length;i++){
		   //don't automatically close the jython window.
		   if (!(frames[i].getContentPane().getComponent(0) instanceof JythonViewer)){

			 frames[i].dispose();
		   }

	   }
   }

   public void openTagControl(){
	   System.out.println("adding tag control");
	  if (tcf==null){
	  tcf=new TagNavigatorFrame(this);
	  tcf.setVisible(true);
	   desktop.add(tcf);
	   try{
		tcf.setSelected(true);
		}catch(java.beans.PropertyVetoException e){}
     } else tcf.toFront();
     if (tcf==null) System.out.println("tag control failed");
   }
   public void openCursorControl(){
	  if (ccf==null){
	  ccf=new CursorNavigatorFrame(this);
	  ccf.setVisible(true);
	   desktop.add(ccf);
	   try{
		ccf.setSelected(true);
		}catch(java.beans.PropertyVetoException e){}
     } else{
		 try{
				 if (ccf.isIcon())	ccf.setIcon(false);
		         ccf.toFront();
			}catch(java.beans.PropertyVetoException e){}
	    }
    }


    public void openPrefsControl(){
	 if (pmf==null){
	  pmf=new  PrefManagerFrame(this);
	  pmf.setVisible(true);
	  desktop.add(pmf);
	  try{
 		  pmf.setSelected(true);
	  	 }catch(java.beans.PropertyVetoException e){}
	    }
	    else {
			    try{
				 if (pmf.isIcon())	pmf.setIcon(false);
		         pmf.toFront();
			}catch(java.beans.PropertyVetoException e){}
	    }
    }

   public HTMLReportEditor html=null;
   public void newHTMLReportEditor(){
	  html=new HTMLReportEditor(this,true);
	 // html.setVisible(true);
	 // desktop.add(html);

   }
   public void openHTMLReportEditor(){
	  html=new HTMLReportEditor(this,false);
	 // html.setVisible(true);
	 // desktop.add(html);

   }

    public void openRulerControl(){
		  if (rulerf==null){
				  rulerf=new RulerControllerFrame(this);
				  rulerf.setVisible(true);
				   desktop.add(rulerf);
				   try{
					rulerf.setSelected(true);
					}catch(java.beans.PropertyVetoException e){}
			     }
		        else {
				try{
								 if (rulerf.isIcon())	rulerf.setIcon(false);
						         rulerf.toFront();
					}catch(java.beans.PropertyVetoException e){}
			    }

	}

	public ExportPanel openExportControl(Viewer3 vw){

					  //ExportControllerFrame
					  ex=new ExportControllerFrame(vw);
					  ex.setVisible(true);

					  desktop.add(ex);
					   try{
						ex.setSelected(true);
						}catch(java.beans.PropertyVetoException e){}
            return ex.getExportController();

	}

    public void openROIControl(){
		  if (roif==null){
		  roif=new ROIControllerFrame(this);
		  roif.setVisible(true);
		   desktop.add(roif);
		   try{
			roif.setSelected(true);
			}catch(java.beans.PropertyVetoException e){}
	     }
        else {
		try{
						 if (roif.isIcon())	roif.setIcon(false);
				         roif.toFront();
			}catch(java.beans.PropertyVetoException e){}
	    }
    }




   public void openImageFileControl(){

          try{
   		  ImageFileNavigatorFrame ifnf=new ImageFileNavigatorFrame(fc.openFile(ph.getProperty("DataFiles dir",".")),this);
	      ifnf.setVisible(true);
   		  desktop.add(ifnf);
   		  ifnf.setSelected(true);
   			}catch(java.beans.PropertyVetoException e){
   			}catch(Exception e){return;}
   	     }


   public void openHelp(){
 			JOptionPane.showMessageDialog(
				   		    this.mainWindow,
				   		    "<html>Image Data General Viewer (GView3) is a free program<br>for viewing large collections of scientific image data.<br><br>A users guide: <u>GView3_Manual.html</u> is located in <br>the main GView3 directory.<br>Please contact Gil Bub <i>gil.bub@physiol.ox.ac.uk</i> if it is missing.</html>"
					);

   }

   public void openAbout(){
	   JOptionPane.showMessageDialog(
	   		    this.mainWindow,
	   		    "<html><center><h2>GView3</h2>Image Data <u>G</u>eneral <u>View</u>er <br> ver. 0.8<br><br>Copyright(C) 1998-2006, Gil Bub<br>uses: Jython, ImageJ, Gnuplot GPL code<br>This program is free software (GPL) <br> <i>gil.bub@physiol.ox.ac.uk</i></center></html>"
		);

   }

   public void exitProgram(){
	     try{
	     if (originalDM!=null)
	      graphicsdevice.setDisplayMode(originalDM);
	     }catch(Exception e){;}
	   	 System.exit(0);

   }

public void startServerControl(){
	System.out.println("in open server");

	if (GView3server==null)
	    GView3server=new GView3Server(this);


    if (!GView3server.serverstarted)
        GView3server.start();

    if (jv==null) openJythonWindow();

    System.out.println("started server");
    JOptionPane.showMessageDialog(
		   		    this.mainWindow,
		   		    " server started ="+GView3server.serverstarted
		);
}

public void stopServerControl(){
	if (GView3server!=null){
	GView3server.closeserver();
	JOptionPane.showMessageDialog(
			   		    this.mainWindow,
			   		    " server started ="+GView3server.serverstarted
		);
	}
}

   //

   public void createNavigator() {

	   }


    public NavigatorGraph createNavigator(String name, Trace tr){
		   MyInternalFrame frame=new MyInternalFrame(name,this,tr);
           return frame.sg;

	}

    public void openNavigatorWindow(String absnavfilename, String filename){
   		   ArrayList ar=new ArrayList();
		    ar.add(new ROI(null,Color.blue));
			ar.add(new ROI(null,Color.green));
			ar.add(new ROI(null,Color.red));
			ar.add(new ROI(null,Color.magenta));
			ar.add(new ROI(null,Color.yellow));

   		   MyInternalFrame frame=new MyInternalFrame(absnavfilename,filename,this,ar);
   		    frame.setVisible(true); //necessary as of kestrel
   	        desktop.add(frame);
   	        try {
   	            frame.setSelected(true);
   	        } catch (java.beans.PropertyVetoException e){};


       }

  /* public MyInternalFrame jythonwindow;
   public void openJythonWindow(){
	     if (jv==null){
	     jv=new JythonViewer("GView3_python_internal.py",this,jh);
	     if (jythonwindow==null){
	      jythonwindow=new MyInternalFrame("jython", jv);
	      jythonwindow.setVisible(true); //necessary as of kestrel
	      desktop.add(jythonwindow);
	      }
	     try {
			 jythonwindow.setSelected(true);
	      } catch (java.beans.PropertyVetoException e){System.out.println(" cant open jython window ");e.printStackTrace();};

    }
   }
*/
public aFrame jythonwindow;
public void openJythonWindow(){
		 if (jv==null){
		   jv=new JythonViewer("GView3_python_internal.py",this,jh);
	     if (jythonwindow==null){
			 jythonwindow=new aFrame(jv,aFrame.Internal_Frame,"Jython",gv);
		 }
	 }
 }




   public MyInternalFrame prosilicawindow;
    public void openProsilicaWindow(ProsilicaImagePanel propanel){
   	      prosilicawindow=new MyInternalFrame("prosilica", propanel);
   	      prosilicawindow.setVisible(true); //necessary as of kestrel
   	      desktop.add(prosilicawindow);

   	     try {
   			 prosilicawindow.setSelected(true);
   	      } catch (java.beans.PropertyVetoException e){System.out.println(" cant open prosilica window ");e.printStackTrace();};


   }


    public void runJythonScript(){
       openJythonWindow();
       try{
         jv.interp.execfile(fc.openFile(ph.getProperty("Script dir",".")));
        }catch(Exception e){
          return;
	  }
   }
   /** called from GView3 main**/
   public void loadJythonScript(String script){
      	openJythonWindow();
          try{
          jv.opentextfile(script);
          jv.editor_runAll();
         }catch(Exception e){

   	  }
      }

   public void loadJythonScript(){
         	openJythonWindow();
             try{
             jv.editor_load();
             jv.editor_runAll();
            }catch(Exception e){

      	  }
      }

	//
   public static void writeImageFile(ImageDecoder in, String out){
   	int [] specs=new int[4];
   	in.ReturnXYBandsFrames(specs,0);

   	int in_x=specs[0];
   	int in_y=specs[1];
   	int bands=specs[2];
   	int frames=specs[3];

   	try{
   			 RandomAccessFile outfile=new RandomAccessFile(out,"rw");

   			 double[] tmp=new double[in_x*in_y];
   			 byte[] bytes=new byte[in_x*in_y*8];
   	 		 int[] arr=new int[in_x*in_y];

   			 FileChannel fc=outfile.getChannel();
   			 MappedByteBuffer buffer = fc.map (FileChannel.MapMode.READ_WRITE, 0, frames*in_x*in_y*8+4*4);
   		     buffer.putInt(1); //version
   			 buffer.putInt(frames);
   			 buffer.putInt(in_x);
   		     buffer.putInt(in_y);
   		     for (int z=0;z<frames;z++){
   				in.JumpToFrame(z,0);
   				in.UpdateImageArray(arr,in_x,in_y,0);
   				 for (int y=0;y<in_y;y++){
   					 for (int x=0;x<in_x;x++){
   						 buffer.putShort((short)arr[y*in_x+x]);

   					 }
   				 }

   	}

   	outfile.close();
   }catch(Exception e){e.printStackTrace();}
   }


   public void saveImageFile(ImageDecoder id){
           fc.openFile(fc.UserDir);
		   if (fc.approved) writeImageFile(id, fc.getAbsolutePath());

   }

   public void openOMPROSeries(){
	 String filepath=fc.openFile(fc.DataDir);
     if (!fc.approved) return;
	 ImageDecoder id=new MultiDatafileDecoder("ompro");
	 id.OpenImageFile(fc.getDirectory());
	 String tmp=fc.getAbsolutePath().replace('\\','_').replace('.','_').replace(':','_').replace('/','_');
	 Viewer3 Viewer3=new Viewer3(this,50,tmp,tmp,id);
	  Viewer3.setVisible(true);
	  desktop.add(Viewer3);
	  try{
	   Viewer3.setSelected(true);
	   }catch(java.beans.PropertyVetoException e){}
	  //Viewer3.getaFrame();
	  updateWindowList();

	 //(GView3 ifd, int fps, String filename, String absolutefilename, ImageDecoder id)
   }

   public void openImageFile(String filetype){
    openImageFile(filetype,1);
   }

   public void openImageFile(String filetype, int frameskip){

     filetype=filetype;

     String filepath=fc.openFile(fc.DataDir);
     if (!fc.approved) return;
     if (filetype==""){filetype=fc.getFileType();}
      System.out.println("got file= "+fc.getName()+" directory= "+fc.getDirectory()+" path="+fc.getAbsolutePath()+" filetype="+filetype);

	  Viewer3 Viewer3=new Viewer3(this,
	                              ph.getIntProperty("framerate",20),
	                              (OpenDirectory?fc.getDirectory():fc.getName()),
	                              (OpenDirectory?fc.getDirectory():fc.getAbsolutePath()),
	                              filetype,frameskip);

	Viewer3.setVisible(true);
	desktop.add(Viewer3);
		try{
	Viewer3.setSelected(true);
    }catch(java.beans.PropertyVetoException e){}
    //Viewer3.getaFrame();
	  updateWindowList();
	}






    public Viewer3 openImageFile(ImageDecoder id, String name){
		Viewer3 Viewer3=new Viewer3(this,15,name,name,id);
	    Viewer3.setVisible(true);
		desktop.add(Viewer3);
		try{
	    Viewer3.setSelected(true);
   	    }catch(java.beans.PropertyVetoException e){}
   	   // Viewer3.getaFrame();
   		updateWindowList();
   		return Viewer3;
	}

	public void openImageFile(String filename,String filetype){
		File file=new File(filename);
		Viewer3 Viewer3=new Viewer3(this,pv.getIntProperty("framerate","5"),file.getName(),file.getAbsolutePath(),filetype);
		   					   Viewer3.setVisible(true);
	   	   					   desktop.add(Viewer3);
		   					   try{
		   					  Viewer3.setSelected(true);
   	     }catch(java.beans.PropertyVetoException e){}
   	     //Viewer3.getaFrame();
         updateWindowList();

	}
//invokeLater?
public void updateWindowList(){
	JInternalFrame[] frames=desktop.getAllFrames();
	windowList.removeAll();
	for (int i=0;i<frames.length;i++){
	  JMenuItem mi=new JMenuItem(frames[i].getTitle());
	  windowList.add(mi);
	  mi.addActionListener(new ActionListener(){
	  			public void actionPerformed(ActionEvent e){
	  			  windowSelect(((JMenuItem)e.getSource()).getText());
	  			}
			});
	    }
  }

public void windowSelect(String title){
	JInternalFrame[] frames=desktop.getAllFrames();
	for (int i=0;i<frames.length;i++){
	 if (frames[i].getTitle().equals(title)){
	   try{
	   frames[i].setIcon(false);
	   frames[i].setSelected(true);
	   frames[i].toFront();
       }catch(Exception e){;}
	 }
  }
}
    public static void main(String[] args) {
		boolean fullframerequested=false;

        GraphicsEnvironment env = GraphicsEnvironment. getLocalGraphicsEnvironment();
        GraphicsDevice[] devices = env.getScreenDevices();



		GView3 frame = new GView3(devices[0],fullframerequested);

		frame.mainWindow.setVisible(true);
        if (args.length>0)
          frame.loadJythonScript(args[0]);
    }
}

class gvListener extends MouseInputAdapter {
	GView3 gv;
	public gvListener(GView3 gv){
		   this.gv=gv;
		}

	public void mousePressed(MouseEvent e) {
	     maybeShowPopup(e);
		}

  public void mouseReleased(MouseEvent e) {
  System.out.println("released mouse");
   maybeShowPopup(e);
     }


 private boolean maybeShowPopup(MouseEvent e) {
              boolean showedpopup=false;
			  System.out.println("checking popup");
			  if((e.getModifiers() & InputEvent.BUTTON3_MASK)== InputEvent.BUTTON3_MASK){
              showedpopup=true;
			  gv.popup.show(e.getComponent(),  e.getX(), e.getY());
			  }

			return showedpopup;
			}

    }

class gvDesktopManager extends DefaultDesktopManager{
	GView3 gv;
	public gvDesktopManager(GView3 gv){
		this.gv=gv;
	}
	public void endDraggingFrame(JComponent f){
		 if (f instanceof JInternalFrame){

		   gv.notifyAFrames((JInternalFrame)f);

		 }
		 super.endDraggingFrame(f);
	}
	}

