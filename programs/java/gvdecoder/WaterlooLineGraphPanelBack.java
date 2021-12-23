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


public class WaterlooLineGraphPanel extends JPanel{

Viewer2 vw;

GJGraphContainer gc;

public Vector datasets;

Dimension preferredSize = new Dimension(300,300);

public Dimension getPreferredSize(){
		 return preferredSize;
 }


 public Dimension getMaximumSize(){
		 return preferredSize;
 }

public void add(double[] xs, double[] ys, double offset, double scale, java.awt.Color color){
	dataset d=new dataset(xs,ys,offset,scale,color);
	datasets.add(d);
	gc.getView().add(d.p);
	gc.getView().autoScale();
}

/* this is a work around because I don't understand the waterloo graphics api*/
public void setRange(double lowv, double highv){
  for (int j=0;j<datasets.size();j++){
	dataset d=(dataset)datasets.elementAt(j);
	int lowi=-1;
    int highi=-1;
    for (int x=0;x<d.xs.length;x++){
	 if	((d.xs[x]>=lowv)&&(lowi==-1)) lowi=x;
	 if ((d.xs[x]>=highv)&&(highi==-1)) highi=x;
	}
	if (highi==-1) highi=d.xs.length-1;
	if (lowi==-1) lowi=0;
	if (highi<=lowi) highi=lowi+1;
	if (highi>=d.xs.length) highi=d.xs.length-1;
	//System.out.println("lowv="+lowv+" highv="+highv+" lowi="+lowi+" highi="+highi);
	double[] xt=new double[highi-lowi];
	double[] yt=new double[highi-lowi];
	for (int i=lowi;i<highi;i++){
		xt[i-lowi]=d.xs[i];
		yt[i-lowi]=d.ys[i]*d.scale+d.offset;
	}
    d.p.setXData(xt);
    d.p.setYData(yt);
  }
   gc.getView().autoScale();
}

public WaterlooLineGraphPanel(Viewer2 vw, double[] xs, double [] ys){

 this.vw=vw;

 datasets=new Vector();
 dataset d=new dataset(xs,ys,0,1,java.awt.Color.BLACK);
 datasets.add(d);

 this.setLayout(new BorderLayout());

 ScrollControl datarange=new ScrollControl(Color.gray,xs[0],xs[xs.length-1],xs[0],xs[xs.length-1],true,1,"###","range");
 datarange.addScrollListener(new rangeListener(this));
 this.add(datarange,BorderLayout.SOUTH);

 gc = GJGraphContainer.createInstance(GJGraph.createInstance());
 gc.getView().add(d.p);
 gc.getView().autoScale();

 this.add(gc, BorderLayout.CENTER);

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
	double[] xs;
	double[] ys;
	double offset;
	double scale;
	java.awt.Color color;
	GJPlotInterface p;
	public dataset(double[] xs, double[] ys, double offset, double scale, java.awt.Color color){
		this.xs=xs;
		this.ys=ys;
		this.offset=offset;
		this.scale=scale;
		this.color=color;
		this.p=GJLine.createInstance();
		p.setXData(xs);
		p.setYData(ys);

	}


}




}






























