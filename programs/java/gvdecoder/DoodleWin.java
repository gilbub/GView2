import javax.swing.*;

import java.awt.event.*;
import java.awt.*;
import java.util.*;


public class DoodleWin extends JInternalFrame {
     static final int xOffset = 30, yOffset = 30;
    DoodlePanel sg;
    Doodle doodle;

 public DoodleWin(String absolutefilename, Doodle doodle){
         super(windowname,
              true, //resizable
              true, //closable
              true, //maximizable
              true);//iconifiable

        //...Create the GUI and put it in the window...

        sg=new DoodlePanel(absolutefilename,this);
 		this.doodle=doodle;

        System.out.println("using the new constructor");
		this.getContentPane().add(sg,BorderLayout.CENTER);
        //...Then set the window size or call pack...
        setSize(sg.getPreferredSize());

        //Set the window's location.
        setLocation(xOffset*openFrameCount, yOffset*openFrameCount);
    }

  public MyInternalFrame(Doodle doodle){
	  super(        name,
	                true, //resizable
	                true, //closable
	                true, //maximizable
	                true);//iconifiable
	 sg=new DoodlePanel(this);
     this.doodle=doodle;
     this.getContentPane().add(sg,BorderLayout.CENTER);
	         //...Then set the window size or call pack...
	  setSize(sg.getPreferredSize());
	         //Set the window's location.
      setLocation(xOffset*openFrameCount, yOffset*openFrameCount);

  }



 public DoodlePanel getDoodlePanel(){
  return sg;
 }

 public Doodle getMainApp(){
	 return doodle;
 }

}
