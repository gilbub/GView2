
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

    public PrefManagerFrame(GView ifd) {
        super("pref control",
              true, //resizable
              false, //closable
              true, //maximizable
              true);//iconifiable

        //...Create the GUI and put it in the window...

        cc=PrefManager.getInstance();
        cc.setupGUI();

		this.getContentPane().add(cc,BorderLayout.CENTER);
        //...Then set the window size or call pack...
        setSize(cc.getPreferredSize());

        //Set the window's location.
        setLocation(xOffset*openFrameCount, yOffset*openFrameCount);
    }


}
