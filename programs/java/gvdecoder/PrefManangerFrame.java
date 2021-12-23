package gvdecoder;
import javax.swing.*;

import java.awt.event.*;
import java.awt.*;
import java.util.*;
import gvdecoder.prefs.*;

public class PrefManagerFrame extends JInternalFrame {
    static int openFrameCount = 0;
    static final int xOffset = 1, yOffset = 1;
    PrefManager cc;

    public CursorNavigatorFrame(GView ifd) {
        super("cursor control",
              true, //resizable
              true, //closable
              true, //maximizable
              true);//iconifiable

        //...Create the GUI and put it in the window...

        cc=PrefMananger.getInstance();


		this.getContentPane().add(cc,BorderLayout.CENTER);
        //...Then set the window size or call pack...
        setSize(cc.getPreferredSize());

        //Set the window's location.
        setLocation(xOffset*openFrameCount, yOffset*openFrameCount);
    }


}
