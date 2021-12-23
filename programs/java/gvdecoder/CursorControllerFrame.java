package gvdecoder;
import javax.swing.*;

import java.awt.event.*;
import java.awt.*;
import java.util.*;

public class CursorNavigatorFrame extends JInternalFrame {
    static int openFrameCount = 0;
    static final int xOffset = 10, yOffset = 10;
    CursorController cc;

    public CursorNavigatorFrame(GView ifd) {
        super("cursor control",
              true, //resizable
              true, //closable
              true, //maximizable
              true);//iconifiable

        //...Create the GUI and put it in the window...

        cc=new CursorController(ifd);


		this.getContentPane().add(cc,BorderLayout.CENTER);
        //...Then set the window size or call pack...
        setSize(cc.getPreferredSize());

        //Set the window's location.
        setLocation(xOffset*openFrameCount, yOffset*openFrameCount);
    }
 public CursorController getCursorController(){
  return cc;
 }

}
