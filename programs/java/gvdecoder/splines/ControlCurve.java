/** This class represents a curve defined by a sequence of control points */
/* copied from http://www.cse.unsw.edu.au/~lambert/splines/source.html*/

package gvdecoder.splines;

import java.awt.*;

public class ControlCurve {

  public Polygon pts;
  protected int selection = -1;
  public Color linecolor=Color.red;
  public java.util.Vector xs;
  public java.util.Vector ys;

  public ControlCurve() {
    pts = new Polygon();
  }


  static Font f = new Font("Courier",Font.PLAIN,12);

  /** paint this curve into g.*/
  public void paint(Graphics g){
    FontMetrics fm = g.getFontMetrics(f);
    g.setFont(f);
    int h = fm.getAscent()/2;

    for(int i = 0; i < pts.npoints; i++)  {
      String s = Integer.toString(i);
      int w = fm.stringWidth(s)/2;
      g.setColor(linecolor);
      g.drawString(Integer.toString(i),pts.xpoints[i]-w,pts.ypoints[i]+h);
    }
  }

  static final int EPSILON = 36;  /* square of distance for picking */

  /** return index of control point near to (x,y) or -1 if nothing near */
  public int selectPoint(int x, int y) {
    int mind = Integer.MAX_VALUE;
    selection = -1;
    for (int i = 0; i < pts.npoints; i++) {
      int d = sqr(pts.xpoints[i]-x) + sqr(pts.ypoints[i]-y);
      if (d < mind && d < EPSILON) {
	mind = d;
	selection = i;
      }
    }
    return selection;
  }

  // square of an int
  static int sqr(int x) {
    return x*x;
  }

  /** add a control point, return index of new control point */
  public int addPoint(int x, int y) {
	Polygon newpoly=new Polygon();
	int selected=0;
	newpoly.addPoint(pts.xpoints[0],pts.ypoints[0]);
	for (int i=1;i<pts.npoints;i++){
		if ((x>pts.xpoints[i-1])&&(x<=pts.xpoints[i])){
		  newpoly.addPoint(x,y);
		  selected=i;
	  }
	  newpoly.addPoint(pts.xpoints[i],pts.ypoints[i]);
	}
    selection=selected;
    pts=newpoly;
    return selection;

  }

  /** set selected control point */
  public void setPoint(int x, int y) {
    if (selection >= 0) {
      pts.xpoints[selection] = x;
      pts.ypoints[selection] = y;
    }
  }

  /** remove selected control point */
  public void removePoint() {
    if (selection >= 0) {
      pts.npoints--;
      for (int i = selection; i < pts.npoints; i++) {
	pts.xpoints[i] = pts.xpoints[i+1];
	pts.ypoints[i] = pts.ypoints[i+1];
      }
    }
  }

  public String toString() {
    StringBuffer result = new StringBuffer();
    for (int i = 0; i < pts.npoints; i++) {
      result.append(" " + pts.xpoints[i] + " " + pts.ypoints[i]);
    }
    return result.toString();
  }
}
