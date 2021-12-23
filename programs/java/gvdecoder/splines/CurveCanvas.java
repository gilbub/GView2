/**
 * Canvas for painting controlled curves into
 */
 /* copied from http://www.cse.unsw.edu.au/~lambert/splines/source.html*/
package gvdecoder.splines;
import java.applet.*;
import java.awt.*;
import javax.swing.*;

public class CurveCanvas extends JPanel {

  Image offscreen;
  ControlCurve redcurve,bluecurve,greencurve;
 // MouseEvent mouseEvent;

  public CurveCanvas( ControlCurve redcurve, ControlCurve greencurve, ControlCurve bluecurve){

    this.redcurve = redcurve;
    this.bluecurve=bluecurve;
    this.greencurve=greencurve;
  }

  public void paint(Graphics g) {
    if (offscreen == null) {
      offscreen = createImage(300, 300);
    }
    Graphics og = offscreen.getGraphics();
    og.clearRect(0,0,300,300);
    redcurve.paint(og);
    bluecurve.paint(og);
    greencurve.paint(og);
    og.setColor(Color.black);
    og.drawLine(20,20,20,276);
    og.drawLine(20,276,276,276);
    og.drawLine(148,270,148,276);
    g.drawImage(offscreen,0,0,null);
  }

  public void update(Graphics g) {
    paint(g);
  }

  public void update() {
    update(getGraphics());
  }
/*
  public void run() {
    for (;;) {
      Event e = mouseEvent.get();
      if (e.id == Event.MOUSE_DOWN) {
	if (curve.selectPoint(e.x,e.y) == -1) {
	  curve.addPoint(e.x,e.y);
	  update();
	}
      } else if (e.id == Event.MOUSE_DRAG) {
	curve.setPoint(e.x,e.y);
	  update();
      } else if (e.id == Event.MOUSE_UP) {
	if(e.shiftDown()) {
	  curve.removePoint(); //Shift Click removes control points
	  update();
	}
      }
    }
  }
*/

}

