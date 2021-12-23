package gvdecoder;
import javax.swing.*;

import java.awt.event.*;
import java.awt.*;
import java.util.*;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

public class ExportControllerFrame extends JInternalFrame {
    static int openFrameCount = 0;
    static final int xOffset = 10, yOffset = 10;
    ExportPanel cc;

    public ExportControllerFrame(Viewer2 ifd) {
        super(ifd.filename,
              false, //resizable
              true, //closable
              false, //maximizable
              true);//iconifiable

        //...Create the GUI and put it in the window...

        cc=new ExportPanel(ifd);
        JPanel export=new JPanel();
	    export.setLayout(new BoxLayout(export, BoxLayout.X_AXIS));
		export.setBorder( BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),"Export",TitledBorder.LEFT,TitledBorder.TOP));
		export.add(cc);



		this.getContentPane().add(export,BorderLayout.CENTER);
        //...Then set the window size or call pack...
        setSize(new Dimension(225,367));

        //Set the window's location.
        Point pt=ifd.getLocation();
        setLocation(pt.x+ifd.getWidth()+5, pt.y);
    }
 public ExportPanel getExportController(){
  return cc;
 }

}
