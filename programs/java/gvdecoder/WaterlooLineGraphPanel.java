package gvdecoder;
//package filters;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.border.Border;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import kcl.waterloo.graphics.GJGraph;
import kcl.waterloo.graphics.GJGraphContainer;
import kcl.waterloo.graphics.plots2D.GJPlotInterface;
import kcl.waterloo.graphics.plots2D.GJLine;
import gvdecoder.TimeSeries;
import gvdecoder.trace.TraceGroup;
import gvdecoder.trace.Trace;
import java.awt.geom.Rectangle2D;
import org.python.core.*;

public class WaterlooLineGraphPanel extends JPanel{

Viewer2 vw;

GJGraphContainer gc;

public Vector datasets;

boolean autoscale=false;
boolean stagger=false;
double staggeramount=1.0;

ScrollControl datarange;

Dimension preferredSize = new Dimension(300,300);

public Dimension getPreferredSize(){
		 return preferredSize;
 }


 public Dimension getMaximumSize(){
		 return preferredSize;
 }



public void add(double[] xs, double[] ys, java.awt.Color color){
	dataset d=new dataset(xs,ys,color);
	datasets.add(d);
	gc.getView().add(d.p);
	gc.getView().autoScale();

}
public void add(TimeSeries xs, TimeSeries ys, java.awt.Color color){
	dataset d=new dataset(xs,ys,color);
	datasets.add(d);
    gc.getView().add(d.p);
	gc.getView().autoScale();
}

public void add(double[] ys){
	double[] xs=new double[ys.length];
	for (int i=0;i<xs.length;i++) xs[i]=i;
	add(xs,ys,java.awt.Color.BLACK);

}

public void add(TimeSeries ys){
   double[] xst=new double[ys.length];
   for (int i=0;i<xst.length;i++) xst[i]=i;
   add(new TimeSeries(xst),ys,java.awt.Color.BLACK);
}

public TimeSeries ys(int i){
	return (TimeSeries)((dataset)datasets.elementAt(i)).ys;
}

public void color(int i, java.awt.Color color){
	 if (color==null) color=java.awt.Color.black;
	((dataset)datasets.elementAt(i)).color=color;
	((dataset)datasets.elementAt(i)).p.setLineColor(color);
}

public void color(java.awt.Color color){
	if (color==null) color=java.awt.Color.black;
	((dataset)datasets.lastElement()).color=color;
	((dataset)datasets.lastElement()).p.setLineColor(color);
}


public void width(int i, float w){
	((dataset)datasets.elementAt(i)).p.setLineStroke(new java.awt.BasicStroke(w));
}



TimeSeries selectedset;
public TimeSeries __getitem__(int z){
	if (z>=datasets.size()) throw Py.IndexError("index out of range");
	selectedset=((dataset)datasets.elementAt(z)).ys;
	return selectedset;
}

public void __setitem__(int z, TimeSeries ts){
	if (z>=datasets.size()) throw Py.IndexError("index out of range");
	dataset d=(dataset)datasets.elementAt(z);
	d.ys=ts;
	d.p.setYData(ts.doubles());
	gc.getView().autoScale();
	selectedset=ts;
}

public void __setitem__(int z, java.awt.Color color){
	if (z>=datasets.size()) throw Py.IndexError("index out of range");
	 if (color==null) color=java.awt.Color.black;
	((dataset)datasets.elementAt(z)).color=color;
	((dataset)datasets.elementAt(z)).p.setLineColor(color);
	repaint();
}

public void __setitem__(int z, double w){
	if (z>=datasets.size()) throw Py.IndexError("index out of range");
	dataset d=(dataset)datasets.elementAt(z);
	d.w=(float)w;
    d.p.setLineStroke(new java.awt.BasicStroke((float)w));
    repaint();
}

public void __setitem__(int z, String str){
	if (z>=datasets.size()) throw Py.IndexError("index out of range");
	dataset d=(dataset)datasets.elementAt(z);
    BasicStroke stroke = new BasicStroke(d.w,BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL);
    //Stroke dotted = new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[] {1,2}, 0);
	if (str=="line")   stroke=new BasicStroke(d.w,BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL);
	if (str=="dash")  {
		float[]tmp=new float[1];
		tmp[0]=d.w*4;
		stroke=new BasicStroke(d.w,BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL,10.0f,tmp,0);
		}
	if (str=="dot"){
		float[]tmp=new float[2];
		tmp[0]=d.w;
		tmp[1]=d.w*4;
		stroke=new BasicStroke(d.w,BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL,10.0f,tmp,0);
	  }
	d.p.setLineStroke(stroke);
	repaint();
}

public void __setitem__(int z, java.awt.BasicStroke s){
	if (z>=datasets.size()) throw Py.IndexError("index out of range");
	((dataset)datasets.elementAt(z)).p.setLineStroke(s);
	repaint();
}


public void reload(){
  for (int j=0;j<datasets.size();j++){
	dataset d=(dataset)datasets.elementAt(j);
    d.p.setXData(d.xs.doubles());
    d.p.setYData(d.ys.doubles());
   }
     gc.getView().autoScale();
}

/* this is a work around because I don't understand the waterloo graphics api*/
/* t=g3.gc.getView().setAxesBounds(554,0,600,1)*/
public void setRange(double lowv, double highv){
  double maxvalue=-1*(Double.MAX_VALUE-1);
  double minvalue=Double.MAX_VALUE;
  for (int j=0;j<datasets.size();j++){
	dataset d=(dataset)datasets.elementAt(j);
	if (d.ys.minvalue<minvalue) minvalue=d.ys.minvalue;
	if (d.ys.maxvalue>maxvalue) maxvalue=d.ys.maxvalue;
  }
  gc.getView().setAxesBounds(new Rectangle.Double(lowv,minvalue,highv-lowv,maxvalue-minvalue));
}
  //old way
/*
  for (int j=0;j<datasets.size();j++){
	dataset d=(dataset)datasets.elementAt(j);
	int lowi=-1;
    int highi=-1;
    for (int x=0;x<d.xs.length;x++){
	 if	((d.xs.arr[x]>=lowv)&&(lowi==-1)) lowi=x;
	 if ((d.xs.arr[x]>=highv)&&(highi==-1)) highi=x;
	}
	if (highi==-1) highi=d.xs.length-1;
	if (lowi==-1) lowi=0;
	if (highi<=lowi) highi=lowi+1;
	if (highi>=d.xs.length) highi=d.xs.length-1;
	//System.out.println("lowv="+lowv+" highv="+highv+" lowi="+lowi+" highi="+highi);
	double[] xt=new double[highi-lowi];
	double[] yt=new double[highi-lowi];
	if (autoscale) d.ys.scale(0,1,lowi,highi);
	for (int i=lowi;i<highi;i++){
		xt[i-lowi]=d.xs.arr[i];
		if (stagger)
		 yt[i-lowi]=d.ys.arr[i]+j*staggeramount;
		else
		 yt[i-lowi]=d.ys.arr[i];
	}
    d.p.setXData(xt);
    d.p.setYData(yt);
  }
   gc.getView().autoScale();
*/



public TimeSeries getXs(int length){
  double[] xs=new double[length];
  for (int i=0;i<length;i++) xs[i]=i;
  TimeSeries xst=new TimeSeries(xs);
  return xst;

}

public void show(){
	System.out.println("in show");
	GView gv=GView.getGView();
	JInternalFrame inf=new JInternalFrame("waterloo graph",true, true, true, true);
	inf.getContentPane().add(this);
	inf.pack();
	inf.setSize(300,300);
	inf.setVisible(true);
	gv.desktop.add(inf);
	gv.updateWindowList();
	 try {
	        inf.setSelected(true);
    } catch (java.beans.PropertyVetoException e) {}
	System.out.println("done show");

}

public static TimeSeries getTimeSeries(double[] arr){ return new TimeSeries(arr);}

public WaterlooLineGraphPanel(double[] arr){
	this(getTimeSeries(arr));
}

public WaterlooLineGraphPanel(TimeSeries ts){
 TimeSeries xs=getXs(ts.doubles().length);
 datasets=new Vector();
 gc = GJGraphContainer.createInstance(GJGraph.createInstance());
 dataset d=new dataset(xs,ts, java.awt.Color.BLACK);
 datasets.add(d);
 gc.getView().add(d.p);
 SetupWindow(xs);
 gc.getView().autoScale();
 this.add(gc, BorderLayout.CENTER);
 show();
}


public WaterlooLineGraphPanel(TimeSeries[] tsarr){
  int length=tsarr[0].doubles().length;
  TimeSeries xst=getXs(length);
  datasets=new Vector(tsarr.length);
  gc = GJGraphContainer.createInstance(GJGraph.createInstance());
  for (int j=0;j<tsarr.length;j++){
        dataset d=new dataset(xst,tsarr[j], java.awt.Color.BLACK);
		datasets.add(d);
		gc.getView().add(d.p);
     }
   	SetupWindow(xst);
    gc.getView().autoScale();
    this.add(gc, BorderLayout.CENTER);
    show();
 }

public WaterlooLineGraphPanel(NavigatorGraph nav){
	int length=nav.tg.raw_x.length;
	int user_start=nav.x_offset;
	int user_end=nav.x_offset+nav.x_range;
	datasets=new Vector(nav.tg.Traces.size());
	double[] xs= new double[length];
	for (int i=0;i<nav.tg.raw_x.length;i++) xs[i]=nav.tg.raw_x[i];
    gc = GJGraphContainer.createInstance(GJGraph.createInstance());
    for (int j=0;j<nav.tg.Traces.size();j++){
		double[] ys=new double[length];
		Trace t=(Trace)nav.tg.Traces.get(j);
		for (int k=0;k<length;k++) ys[k]=t.y_values[k]*-1;
		dataset d=new dataset(xs,ys, ((ROI)nav.rois.get(j)).color);
		datasets.add(d);
		gc.getView().add(d.p);
	}
	SetupWindow(xs);
	datarange.lowValue=user_start;
	datarange.highValue=user_end;

	gc.getView().autoScale();
	this.add(gc, BorderLayout.CENTER);
	show();

}

public void SetupWindow(double[] xs){
	 this.setLayout(new BorderLayout());
	 datarange=new ScrollControl(Color.gray,xs[0],xs[xs.length-1],xs[0],xs[xs.length-1],true,1,"###","range");
     datarange.addScrollListener(new rangeListener(this));
	 this.add(datarange,BorderLayout.SOUTH);
}
public void SetupWindow(TimeSeries xs){
	 SetupWindow(xs.doubles());
}

public WaterlooLineGraphPanel(Viewer2 vw, double[] xs, double [] ys){

 this.vw=vw;

 datasets=new Vector();
 dataset d=new dataset(xs,ys,java.awt.Color.BLACK);
 datasets.add(d);
 SetupWindow(xs);

 gc = GJGraphContainer.createInstance(GJGraph.createInstance());
 gc.getView().add(d.p);
 gc.getView().autoScale();

 this.add(gc, BorderLayout.CENTER);
 show();
}



class rangeListener implements ScrollListener {

WaterlooLineGraphPanel pp;

public rangeListener(WaterlooLineGraphPanel pp){
 this.pp=pp;
}
 public void scrollChange(ScrollControl source) {
    pp.setRange((int)source.lowValue,(int)source.highValue);
	//System.out.println("low="+source.lowValue+" high="+source.highValue);

    }
}

public static void main(String[] args){
 JFrame f=new JFrame("ui test");
 f.addWindowListener(new WindowAdapter(){
	  public void windowClosing(WindowEvent e){
		  System.exit(0);
	  }
  });

  f.getContentPane().setLayout(new BorderLayout());

  f.getContentPane().add(new WaterlooLineGraphPanel(null,null,null), BorderLayout.CENTER);
  f.pack();
  f.setVisible(true);


}


class dataset{
	TimeSeries xs;
	TimeSeries ys;
	java.awt.Color color;
	float w;
	GJPlotInterface p;
	public dataset(TimeSeries xs, TimeSeries ys, java.awt.Color color){
		this.xs=xs;
		this.ys=ys;
		if (color==null) this.color=java.awt.Color.BLACK;
		else
		 this.color=color;
		this.p=GJLine.createInstance();
		p.setLineColor(this.color);
		w=1.0f;
		p.setXData(xs.doubles());
		p.setYData(ys.doubles());
	}
	public dataset(double[] xs, double[] ys, java.awt.Color color){
			this.xs=new TimeSeries(xs);
			this.ys=new TimeSeries(ys);
			if (color==null) this.color=java.awt.Color.BLACK;
			else
			 this.color=color;
			this.p=GJLine.createInstance();
			p.setLineColor(this.color);
			p.setXData(this.xs.doubles());
			p.setYData(this.ys.doubles());
			w=1.0f;
	}

}


}






























