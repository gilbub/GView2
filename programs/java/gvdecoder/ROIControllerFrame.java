package gvdecoder;
import javax.swing.*;

import java.awt.event.*;
import java.awt.*;
import java.util.*;

public class ROIControllerFrame extends JInternalFrame {
    static int openFrameCount = 0;
    static final int xOffset = 10, yOffset = 10;
    ROIController cc;

    public ROIControllerFrame(GView ifd) {
        super("roi control",
              true, //resizable
              true, //closable
              true, //maximizable
              true);//iconifiable

        //...Create the GUI and put it in the window...

        cc=new ROIController(ifd);


		this.getContentPane().add(cc,BorderLayout.CENTER);
        //...Then set the window size or call pack...
        setSize(cc.getPreferredSize());

        //Set the window's location.
        setLocation(xOffset*openFrameCount, yOffset*openFrameCount);
    }
 public ROIController getROIController(){
  return cc;
 }

}
