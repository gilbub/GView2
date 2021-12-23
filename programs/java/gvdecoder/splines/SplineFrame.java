package gvdecoder.splines;

import java.applet.*;
import java.awt.*;
import java.net.*;
import java.util.*;
import java.io.*;

/**  This applet allows users to experiment with splines.
@author Tim Lambert
  */
/* copied from http://www.cse.unsw.edu.au/~lambert/splines/source.html*/

public class SplinePanel extends JPanel{
  protected ControlCurve curve;
  protected Color bgcolor=java.awt.Color.white;
  protected MouseEvent mouseEvent;
  public CurveCanvas canvas;

  public SplinePanel() {


    /*dynamically load the class for the curve basis*/
     curve=new NatCubic();
     curve.addPoint(0,0);
     curve.addPoint(100,100);
     curve.addPoint(200,200);
     curve.addPoint(255,255);




    setLayout(new BorderLayout(0,0));
    setSize(300,300);
    mouseEvent = new MouseEvent();
    canvas = new CurveCanvas(mouseEvent,curve);
    add("Center",canvas);
    Thread canvasThread = new Thread(canvas);
    canvasThread.start();
    canvasThread.setPriority(Thread.MIN_PRIORITY);
  }

  public boolean handleEvent(Event e) {
    if (e.id==Event.MOUSE_DOWN ||
        e.id==Event.MOUSE_DRAG ||
        e.id==Event.MOUSE_UP) {
      mouseEvent.put(e);
      return true;
    } else {
     return false;
    }
  }


}
