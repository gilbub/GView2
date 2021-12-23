package gvdecoder;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import gvdecoder.trace.*;
import java.util.*;
import java.awt.geom.GeneralPath;
//import java.awt.geom.GeneralPath;
import java.awt.Graphics2D.*;
import com.sun.image.codec.jpeg.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.geom.AffineTransform;
import java.net.URLConnection;
import javax.swing.event.*;
import java.awt.event.*;
import javax.swing.AbstractAction;

public class TraceViewControl extends JPanel{

public TraceView2 tr;


public TraceViewControl(int xdim, int ydim){
 tr=new TraceView2(xdim,ydim);
 SetupWindow();

}

public void toggleMove(){
  tr.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.MOVE_CURSOR));
  tr.mode=tr.MOVE_FIELD_MODE;
 }


public void toggleZoom(){
  tr.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.CROSSHAIR_CURSOR));
  tr.mode=tr.ZOOM_MODE;
 }

public void unzoom(){
 tr.zoom(0,0,tr.xdim,tr.ydim);
 tr.render();
// tr.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.CROSSHAIR_CURSOR));
// tr.mode=tr.ZOOM_MODE;
}

public void toggleShiftTrace(){
  tr.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
  tr.mode=tr.SHIFT_TRACE_MODE;


}

public void SetupWindow(){

 Icon move=new ImageIcon("gvdecoder/images/Move.png");
 Icon demag=new ImageIcon("gvdecoder/images/MagnifyMinus.png");
 Icon mag=new ImageIcon("gvdecoder/images/Magnify.png");
 Icon hand=new ImageIcon("gvdecoder/images/FingerUp.png");

 Action unzoomaction=new AbstractAction("",demag){
   public void actionPerformed(ActionEvent e){
     unzoom();
   }
  };


 Action moveaction=new AbstractAction("",move){
   public void actionPerformed(ActionEvent e){
    toggleMove();
   }
 };

 Action zoomaction=new AbstractAction("",mag){
   public void actionPerformed(ActionEvent e){
    toggleZoom();
   }
 };

 Action shifttraceaction=new AbstractAction("",hand){
   public void actionPerformed(ActionEvent e){
    toggleShiftTrace();
   }
 };

 ButtonGroup group=new ButtonGroup();
 JToggleButton movebutton=new JToggleButton(moveaction);
 //movebutton.addActionListener(this);
 JToggleButton zoombutton=new JToggleButton(zoomaction);
 //zoombutton.addActionListener(this);
 JToggleButton shifttracebutton=new JToggleButton(shifttraceaction);
 //shifttracebutton.addActionListener(this);
 group.add(movebutton);
 group.add(zoombutton);
 group.add(shifttracebutton);


 JPanel bottombar=new JPanel(new GridLayout(1,0));
 bottombar.add(movebutton);
 bottombar.add(zoombutton);
 bottombar.add(shifttracebutton);
 JButton unzoombutton=new JButton(unzoomaction);
 //unzoom.addActionListener(this)
 bottombar.add(unzoombutton);
 this.setLayout(new BorderLayout());
 this.add(tr,BorderLayout.CENTER);
 this.add(bottombar,BorderLayout.SOUTH);


}
public Dimension getPreferredSize(){
	return new Dimension(tr.xdim,tr.ydim+25);
}

}