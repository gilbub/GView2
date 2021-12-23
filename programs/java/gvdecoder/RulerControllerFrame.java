package gvdecoder;
import javax.swing.*;

import java.awt.event.*;
import java.awt.*;
import java.util.*;

public class RulerControllerFrame extends JInternalFrame {
    static int openFrameCount = 0;
    static final int xOffset = 10, yOffset = 10;
    RulerController cc;

    public RulerControllerFrame(GView ifd) {
        super("ruler control",
              true, //resizable
              false, //closable
              true, //maximizable
              true);//iconifiable

        //...Create the GUI and put it in the window...

        cc=new RulerController(ifd);


		this.getContentPane().add(cc,BorderLayout.CENTER);
        //...Then set the window size or call pack...
        setSize(cc.getPreferredSize());

        //Set the window's location.
        setLocation(xOffset*openFrameCount, yOffset*openFrameCount);
    }
 public RulerController getRulerController(){
  return cc;
 }

}
