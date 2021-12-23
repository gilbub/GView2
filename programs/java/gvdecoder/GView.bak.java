import javax.swing.JInternalFrame;
import javax.swing.JDesktopPane;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JMenuBar;
import javax.swing.JFrame;
import javax.swing.filechooser.*;
import javax.swing.*;
import java.util.*;
import java.io.*;
import log.*;

import java.awt.event.*;
import java.awt.*;
import prefs.*;

public class GView extends JFrame {
    JDesktopPane desktop;
	//final JFileChooser fc = new JFileChooser(".");
    FileDialog fc=new FileDialog(this,"open");
	final PropertyViewer pv=new PropertyViewer(this);
	boolean FileChooserActivated=false;
	boolean OpenDirectory=false; //FileChooser is too slow, and filedialog has no way to select directories
    CursorNavigatorFrame ccf;
    ROIControllerFrame roif;
    TagNavigatorFrame tcf;
    PrefManagerFrame pmf;
    final LogManager logger=LogManager.getInstance();
    final PrefManager prefs=PrefManager.getInstance();
    public JythonHelper jh;

    public GView() {

        super("GView");
        LogManager.getInstance().log( "intitialized LogManager" );
        jh=new JythonHelper();
        jh.gv=this;
		//Make the big window be indented 50 pixels from each edge
        //of the screen.
        int inset = 150;
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setBounds(inset, inset,
                  screenSize.width - inset*2,
                  screenSize.height-inset*2);

        //Quit this app when the big window closes.
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });


        //Set up the GUI.
        desktop = new JDesktopPane(); //a specialized layered pane
        //createFrame(); //Create first window


        setContentPane(desktop);
        setJMenuBar(createMenuBar());

        //Make dragging faster:
        desktop.putClientProperty("JDesktopPane.dragMode", "outline");
        ViewerParams.getInstance();//force instance of ViewerParams in case user uses this before opening a window
        NavigatorGraphParams.getInstance();

     }

    protected JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        JMenu menu = new JMenu("Document");
        menu.setMnemonic(KeyEvent.VK_D);
        JMenuItem menuItem = new JMenuItem("New Navigator");
        menuItem.setMnemonic(KeyEvent.VK_N);
        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                createNavigator();
            }
        });
        menu.add(menuItem);
		menuItem = new JMenuItem("New Animator");
		        menuItem.setMnemonic(KeyEvent.VK_N);
		        menuItem.addActionListener(new ActionListener() {
		            public void actionPerformed(ActionEvent e) {
		                createViewer();
		            }
		        });
        menu.add(menuItem);
		menuItem = new JMenuItem("properties");
				        menuItem.setMnemonic(KeyEvent.VK_P);
				        menuItem.addActionListener(new ActionListener() {
				            public void actionPerformed(ActionEvent e) {
				                createProperties();
				            }
				        });
        menu.add(menuItem);

	   menuItem = new JMenuItem("Open SPE");
	   				        menuItem.setMnemonic(KeyEvent.VK_N);
	   				        menuItem.addActionListener(new ActionListener() {
	   				            public void actionPerformed(ActionEvent e) {
								    OpenDirectory=false;
	   				                openSPE();
	   				            }
	   				        });
	           menu.add(menuItem);

	   menuItem = new JMenuItem("Open CWRU");
								menuItem.setMnemonic(KeyEvent.VK_C);
								menuItem.addActionListener(new ActionListener() {
									public void actionPerformed(ActionEvent e) {
										OpenDirectory=false;
										openCWRU();
									}
								});
	   menu.add(menuItem);



      menuItem = new JMenuItem("Open biorad pic");
								menuItem.setMnemonic(KeyEvent.VK_P);
								menuItem.addActionListener(new ActionListener() {
									public void actionPerformed(ActionEvent e) {
										OpenDirectory=false;
										openBioRadPic();
									}
								});
	   menu.add(menuItem);

	   menuItem = new JMenuItem("Open RedShirt");
	   								menuItem.setMnemonic(KeyEvent.VK_R);
	   								menuItem.addActionListener(new ActionListener() {
	   									public void actionPerformed(ActionEvent e) {
	   										OpenDirectory=false;
	   										openRedShirt();
	   									}
	   								});
	   	   menu.add(menuItem);



menuItem = new JMenuItem("Open OMPRO");
								menuItem.setMnemonic(KeyEvent.VK_O);
								menuItem.addActionListener(new ActionListener() {
									public void actionPerformed(ActionEvent e) {
										OpenDirectory=false;
										openOMPRO();
									}
								});
	   menu.add(menuItem);
menuItem = new JMenuItem("Open GviewFormat");
								menuItem.setMnemonic(KeyEvent.VK_O);
								menuItem.addActionListener(new ActionListener() {
									public void actionPerformed(ActionEvent e) {
										OpenDirectory=false;
										openGViewFormat();
									}
								});
	   menu.add(menuItem);
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

	    menuItem = new JMenuItem("Open File Control");
	  							menuItem.setMnemonic(KeyEvent.VK_M);
	  							menuItem.addActionListener(new ActionListener() {
	  								public void actionPerformed(ActionEvent e) {

	  									openImageFileControl();
	  								}
	  							});
			   menu.add(menuItem);
       menuItem = new JMenuItem("Open Cursor Control");
	  							menuItem.setMnemonic(KeyEvent.VK_U);
	  							menuItem.addActionListener(new ActionListener() {
	  								public void actionPerformed(ActionEvent e) {

	  									openCursorControl();
	  								}
	  							});
			   menu.add(menuItem);
menuItem = new JMenuItem("Open Prefs Control");
	  							menuItem.setMnemonic(KeyEvent.VK_P);
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


	   menuBar.add(menu);

        return menuBar;
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

   public void openRedShirt(){
	   openImageFile("red");
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
   		  fc.setVisible(true);

		  String filename = fc.getFile();
		  String directory= fc.getDirectory();
		  File file = new File(directory+filename);
		  String absfilename=file.getAbsolutePath();


   		  ImageFileNavigatorFrame ifnf=new ImageFileNavigatorFrame(absfilename,this);
   		  ifnf.setVisible(true);
   		   desktop.add(ifnf);
   		   try{
   			ifnf.setSelected(true);
   			}catch(java.beans.PropertyVetoException e){}
   	     }




   //
   public void createNavigator() {

   		   fc.setVisible(true);

		   String filename = fc.getFile();
		   String directory= fc.getDirectory();
		   File file = new File(directory+filename);
		   String absnavfilename=file.getAbsolutePath();
		openNavigatorWindow(absnavfilename,filename);
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


	//


   public void openImageFile(String filetype){
        //filechooser

   	 //if (!FileChooserActivated){
   	 // fc.setCurrentDirectory(new File(pv.getStringProperty("spedir",".")));
     //    FileChooserActivated=true;
   	 // }
   	 //   fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
     //   int returnVal = fc.showOpenDialog(this);
     //String filetype;
     filetype=filetype;
     fc.setVisible(true);

                   //if (returnVal == JFileChooser.APPROVE_OPTION){
                       //File file = fc.getSelectedFile();
                       String filename = fc.getFile();
					   String directory = fc.getDirectory();
					   if (filetype==""){
					   		 //determine the filetype, assuming an image was entered
					   		 int period=filename.indexOf(".");
					   		 filetype=(filename.substring(period)).trim();
					   		 System.out.println("file type guessed to be "+filetype);

						 }
					   System.out.println("got file = "+filename+" directory = "+directory);
					   File file=null;
					   if (filename!=null){
					   if (!OpenDirectory) file = new File(directory+filename);
					    else file=new File(directory);
					   System.out.println("Opening: " + file.getName()+" path: "+file.getAbsolutePath()+" parent: "+file.getParent());
					   Viewer2 viewer2=new Viewer2(this,pv.getIntProperty("framerate","5"),file.getName(),file.getAbsolutePath(),filetype);
   					   viewer2.setVisible(true);
   					   desktop.add(viewer2);
   					   try{
   					    viewer2.setSelected(true);
   						}catch(java.beans.PropertyVetoException e){}
   					} else {System.out.println("Open command cancelled by user.");}
                   }



    public void openImageFile(ImageDecoder id, String name){
		Viewer2 viewer2=new Viewer2(this,15,name,name,id);
		System.out.println("out of viewer2 initialization");
	    viewer2.setVisible(true);
		   					   desktop.add(viewer2);
		   					   try{
		   					    viewer2.setSelected(true);
   		}catch(java.beans.PropertyVetoException e){}
	}

	public void openImageFile(String filename,String filetype){
		File file=new File(filename);
		Viewer2 viewer2=new Viewer2(this,pv.getIntProperty("framerate","5"),file.getName(),file.getAbsolutePath(),filetype);
		   					   viewer2.setVisible(true);
		   					   desktop.add(viewer2);
		   					   try{
		   					  viewer2.setSelected(true);
   	     }catch(java.beans.PropertyVetoException e){}


	}


    public static void main(String[] args) {
        GView frame = new GView();
        frame.setVisible(true);
    }
}
