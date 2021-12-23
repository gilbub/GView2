package gvdecoder.splines;

import java.applet.*;
import java.awt.*;
import java.net.*;
import java.util.*;
import java.io.*;
import javax.swing.*;
import javax.swing.event.*;
import java.awt.event.*;

/**  This applet allows users to experiment with splines.
@author Tim Lambert
  */
/* copied from http://www.cse.unsw.edu.au/~lambert/splines/source.html*/

public class SplinePanel extends JPanel implements ActionListener {
  protected ControlCurve redcurve,bluecurve,greencurve,selectedcurve;
  protected Color bgcolor=java.awt.Color.white;

  public CurveCanvas canvas;
  public gvdecoder.GView gv;

  public Dimension getPreferredSize(){
	  return new Dimension(300,300);
  }


  public SplinePanel() {
    MyListener myListener = new MyListener(this);
    addMouseListener(myListener);
    addMouseMotionListener(myListener);

    /*dynamically load the class for the curve basis*/
    redcurve=new NatCubic(Color.red);
    redcurve.pts.addPoint(20,276);
    redcurve.pts.addPoint(148,148);
    redcurve.pts.addPoint(276,20);
    selectedcurve=redcurve;

	bluecurve=new NatCubic(Color.blue);
    bluecurve.pts.addPoint(20,276);
    bluecurve.pts.addPoint(148,148);
    bluecurve.pts.addPoint(276,20);

	greencurve=new NatCubic(Color.green);
    greencurve.pts.addPoint(20,276);
    greencurve.pts.addPoint(148,148);
    greencurve.pts.addPoint(276,20);

    setLayout(new BorderLayout(0,0));
    setSize(350,350);

    canvas = new CurveCanvas( redcurve,greencurve,bluecurve);
    add(canvas, BorderLayout.CENTER);

    ButtonGroup group = new ButtonGroup();
     JRadioButton redbutton = new JRadioButton("red");
     redbutton.setActionCommand("red");
     group.add(redbutton);
     redbutton.addActionListener(this);
     JRadioButton greenbutton=new JRadioButton("green");
     greenbutton.setActionCommand("green");
     greenbutton.addActionListener(this);
     group.add(greenbutton);
     JRadioButton bluebutton=new JRadioButton("blue");
     bluebutton.setActionCommand("blue");
     bluebutton.addActionListener(this);
     group.add(bluebutton);
     JPanel buttonpanel=new JPanel();
     buttonpanel.add(redbutton);
     buttonpanel.add(greenbutton);
     buttonpanel.add(bluebutton);
     JButton resetbutton=new JButton("reset");
     resetbutton.setActionCommand("reset");
     resetbutton.addActionListener(this);
     JButton applybutton=new JButton("apply");
     applybutton.setActionCommand("apply");
     applybutton.addActionListener(this);
     buttonpanel.add(resetbutton);
     buttonpanel.add(applybutton);
     add(buttonpanel,BorderLayout.SOUTH);

    //Thread canvasThread = new Thread(canvas);
    //canvasThread.start();
    //canvasThread.setPriority(Thread.MIN_PRIORITY);
  }

  public void resetCurve(){
	  selectedcurve.pts=new Polygon();
	  selectedcurve.pts.addPoint(20,276);
	  selectedcurve.pts.addPoint(148,148);
      selectedcurve.pts.addPoint(276,20);
      canvas.update();
  }

  public void applyCurves(){
	  if (gv!=null){
		     int[][] lt=new int[3][256];
		     lt[0]=((NatCubic)redcurve).get256();
		     lt[1]=((NatCubic)greencurve).get256();
  			 lt[2]=((NatCubic)bluecurve).get256();
	  		 gvdecoder.ColorFilter cf=(gvdecoder.ColorFilter)(gv.jh.presentviewer.jp.myfilt.filters.get(0));
	  		 cf.lt=lt;
             gv.jh.presentviewer.jp.repaint();
	  }

  }

  public void actionPerformed(ActionEvent e) {
	  if (e.getActionCommand().equals("red")) selectedcurve=redcurve;
	  else
	  if (e.getActionCommand().equals("green")) selectedcurve=greencurve;
	  else
	  if (e.getActionCommand().equals("blue")) selectedcurve=bluecurve;
	  else
	  if (e.getActionCommand().equals("reset")) resetCurve();
	  else
	  if (e.getActionCommand().equals("apply")) applyCurves();


}
}

 class MyListener extends MouseInputAdapter {
	SplinePanel sp;
	public MyListener(SplinePanel sp){
		this.sp=sp;
	}

    public void mousePressed(java.awt.event.MouseEvent e) {
        int x = e.getX();
        int y = e.getY();
        if (sp.selectedcurve.selectPoint(x,y) == -1) { /*no point selected add new one*/
			  sp.selectedcurve.addPoint(x,y);
			  sp.canvas.update();
	       }

    }

    public void mouseDragged(java.awt.event.MouseEvent e) {
      sp.selectedcurve.setPoint(e.getX(),e.getY());
	  sp.canvas.update();
    }




}