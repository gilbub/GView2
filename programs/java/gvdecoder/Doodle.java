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
import gvdecoder.prefs.*;

public class Doodle extends JFrame {
    JDesktopPane desktop;
	FileDialog fc=new FileDialog(this,"open");


    public Doodle() {

        super("Doodle");
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

     }

    protected JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        JMenu menu = new JMenu("File");
        menu.setMnemonic(KeyEvent.VK_D);
        JMenuItem menuItem = new JMenuItem("New");
        menuItem.setMnemonic(KeyEvent.VK_N);
        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                createDoodle();
            }
        });
        menu.add(menuItem);
		menuItem = new JMenuItem("Open");
		        menuItem.setMnemonic(KeyEvent.VK_O);
		        menuItem.addActionListener(new ActionListener() {
		            public void actionPerformed(ActionEvent e) {
		                openDoodle();
		            }
		        });
        menu.add(menuItem);
		menuItem = new JMenuItem("Save");
				        menuItem.setMnemonic(KeyEvent.VK_S);
				        menuItem.addActionListener(new ActionListener() {
				            public void actionPerformed(ActionEvent e) {
				                saveDoodle();
				            }
				        });
        menu.add(menuItem);




	   menuBar.add(menu);

       return menuBar;
    }




   public void openDoodle(){

   		  fc.setVisible(true);

		  String filename = fc.getFile();
		  String directory= fc.getDirectory();
		  File file = new File(directory+filename);
		  String absfilename=file.getAbsolutePath();


   		  DoodleWin dw=new DoodleWin(absfilename,this);
   		  dw.setVisible(true);
   		   desktop.add(ifnf);
   		   try{
   			dw.setSelected(true);
   			}catch(java.beans.PropertyVetoException e){}
   	     }

 	public void newDoodle(){
	 DoodleWin dw=new DoodleWin(this);
	 dw.setVisible(true);
	 desktop.add(dw);
	   try{
	    dw.setSelected(true);
   		}catch(java.beans.PropertyVetoException e){}
  	}


  	public void saveDoodle(){
	  fc.setVisible(true);

	  String filename = fc.getFile();
	  String directory= fc.getDirectory();
	  File file = new File(directory+filename);
	  String absfilename=file.getAbsolutePath();

	  //find the selected doodle
	  internalFrame[] ifs=desktop.getAllFrames()
      for (int i=0;i<ifs.size();i++){
		  if (ifs[i].isSelected()){
			  ifs.saveFile(absfilename);
		  }
		}

  	}




    public static void main(String[] args) {
        Doodle frame = new Doodle();
        frame.setVisible(true);
    }
}
