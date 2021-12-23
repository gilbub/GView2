import javax.swing.JInternalFrame;
import javax.swing.JDesktopPane;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JMenuBar;
import javax.swing.JFrame;
import javax.swing.filechooser.*;
import javax.swing.*;
import java.io.*;

import java.awt.event.*;
import java.awt.*;

public class InternalFrameDemo extends JFrame {
    JDesktopPane desktop;
	final JFileChooser fc = new JFileChooser("N:/Sharepoint/Share1/shrierlab/Shared Documents/gils/");
    final PropertyViewer pv=new PropertyViewer(this);

    public InternalFrameDemo() {
        super("InternalFrameDemo");
          
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
	   				                openSPE();
	   				            }
	   				        });
	           menu.add(menuItem);
       
	   
	   menuBar.add(menu);

        return menuBar;
    }

    protected void createNavigator() {
        
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
     int returnVal = fc.showOpenDialog(this);
   
                   if (returnVal == JFileChooser.APPROVE_OPTION) {
                       File file = fc.getSelectedFile();
                       //this is where a real application would open the file.
					   System.out.println("Opening: " + file.getName()+" path: "+file.getAbsolutePath()+" parent: "+file.getParent());
                       Viewer2 viewer2=new Viewer2(this,20,file.getName(),file.getAbsolutePath());
					   viewer2.setVisible(true);
					   desktop.add(viewer2);
					   try{
					    viewer2.setSelected(true);
						}catch(java.beans.PropertyVetoException e){}
					} else {
                       System.out.println("Open command cancelled by user.");
                   }
            }
   
   
   
   
   

    public static void main(String[] args) {
        InternalFrameDemo frame = new InternalFrameDemo();
        frame.setVisible(true);
    }
}
