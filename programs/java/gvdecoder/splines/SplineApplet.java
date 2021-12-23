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

public class SplineFrame extends aFrame {
  protected ControlCurve curve;
  protected Color bgcolor;
  protected MouseEvent mouseEvent;

  public void init() {

    if (bgcolor != null){
      setBackground(Color.white);
      }

    /*dynamically load the class for the curve basis*/
    try {
      String  curveName = "ControlCurve";
      curve = (ControlCurve) Class.forName(curveName).newInstance();
    } catch (ClassNotFoundException e) {
      System.err.println("Class not found: "+e.getMessage());
    } catch (InstantiationException e) {
      System.err.println("Couldn't Instance "+e.getMessage());
    } catch (IllegalAccessException e) {
      System.err.println("Couldn't Instance "+e.getMessage());
    }
     curve.addPoint(0,0);
     curve.addPoint(100,100);
     curve.addPoint(200,200);
     curve.addPoint(255,255);




    setLayout(new BorderLayout(0,0));
    mouseEvent = new MouseEvent();
    CurveCanvas canvas = new CurveCanvas(mouseEvent,curve);
    getContentPane().add("Center",canvas);
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
