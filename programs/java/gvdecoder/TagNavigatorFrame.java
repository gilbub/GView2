package gvdecoder;
import javax.swing.*;

import java.awt.event.*;
import java.awt.*;
import java.util.*;

public class TagNavigatorFrame extends JInternalFrame {
    static int openFrameCount = 0;
    static final int xOffset = 10, yOffset = 10;
    TagController tc;

    public TagNavigatorFrame(GView ifd) {
        super("Tag control",
              true, //resizable
              true, //closable
              true, //maximizable
              true);//iconifiable

        //...Create the GUI and put it in the window...

        tc=new TagController(ifd);


		this.getContentPane().add(tc,BorderLayout.CENTER);
        //...Then set the window size or call pack...
        setSize(tc.getPreferredSize());

        //Set the window's location.
        setLocation(xOffset*openFrameCount, yOffset*openFrameCount);
    }
 public TagController getTagController(){
  return tc;
 }

}
