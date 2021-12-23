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
import kcl.waterloo.marker.GJMarker;
import kcl.waterloo.graphics.GJGraphContainer;
import kcl.waterloo.graphics.plots2D.GJPlotInterface;
import kcl.waterloo.graphics.plots2D.GJLine;
import kcl.waterloo.graphics.plots2D.GJScatter;
import kcl.waterloo.graphics.plots2D.GJErrorBar;
import kcl.waterloo.graphics.plots2D.GJBar;
import kcl.waterloo.export.ExportFactory;
import gvdecoder.TimeSeries;
import gvdecoder.trace.TraceGroup;
import gvdecoder.trace.Trace;
import java.awt.geom.Rectangle2D;
import org.python.core.*;
import java.io.Serializable;
import java.io.IOException;
import java.awt.image.BufferedImage;

public class Plot extends JPanel implements Serializable{

public static final int RESCALE_Y_TO_XRANGE = 0;
public static final int RESCALE_Y_TO_FULLRANGE = 1;
public static final int USE_USER_YRANGE = 2;

public int yscale_behavior=RESCALE_Y_TO_XRANGE;

public double user_minyvalue=-1;
public double user_maxyvalue=1;
public double user_minxvalue=0;
public double user_maxxvalue=1;

public JInternalFrame inf;


GJGraphContainer gc;

public Vector datasets;

boolean autoyscale=true;
boolean stagger=false;
double staggeramount=1.0;
public double pointsize=5;

ScrollControl datarange;

Dimension preferredSize = new Dimension(300,300);


public dataset __getitem__(int z){
	if (z>=datasets.size()) throw Py.IndexError("index out of range");
	dataset d=(dataset)datasets.elementAt(z);
	return d;
}

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

public void add(double[] xs, double[] ys){
   add(xs,ys,java.awt.Color.BLACK);
}

public void add(TimeSeries ys){
   double[] xst=new double[ys.length];
   for (int i=0;i<xst.length;i++) xst[i]=i;
   add(new TimeSeries(xst),ys,java.awt.Color.BLACK);
}

public void remove(int i){
	 dataset d=(dataset)datasets.elementAt(i);
	 if (d.p!=null) gc.getView().remove(d.p);
	 if (d.s!=null) gc.getView().remove(d.s);
	 if (d.b!=null) gc.getView().remove(d.b);
	 datasets.removeElementAt(i);
}
public void remove(){
	int i=datasets.size();
	if (i>0) remove(i-1);
}

public String help(){
	String s="Plot.java uses the Waterloo Scientific Graphics oackage (waterloo.sourceforge.net)\n"
	        +"constructor:                     p=gvdecoder.Plot(xs,ys) or gvdecoder.Plot(ys) where ys is a gvdecoder.TimeSeries or double array.\n"
	        +"adding plots:                    p.add(xs,ys), p.add(ys), p.add(xs,ys,Color.blue).\n"
	        +"removing a plot:                 p.remove(i): where i is the index of the plot to go, or p.remove() removes the last one added.\n"
	        +"accessing a plot:                p[0] returns a 'dataset' object which can be accessed by get/set methods.\n"
	        +"lines(edges) or points(markers): p[0].marks = 1 or 0 (add or remove markers; p[0].edges =1 or 0 (add or remove lines).\n"
	        +"mark style:                      p[0].markstyle = 'squares','circles','dots','diamonds',or 'triangles'.\n"
	        +"mark size:                       p[0].marksize = 4.0.\n"
	        +"edge style:                      p[0].edgestyle = 'solid','dash', or 'dot'.\n"
	        +"edge width:                      p[0].edgewidth = 3.5\n"
	        +"edge and marker color, fill:     p[0].color = java.awt.Color.red; p[0]=(255,0,0),  p[0].markfill = 1 or 0.\n"
	        +"error bars:                      p[0].addErrorbars(doubles[]) or p[0].e= ts (TimeSeries); p[0].errorbars= 1 or 0 add/remove bars.\n"
	        +"accessing data (via TimeSeries): p[0].y *= 100, p[0].y.scale(), p[0].y += [x*0.8 for x in range(100)], p[0].x*=0.324, p[0].e*=2.\n"
	        +"xlabel, ylabel:                  p.xlabel='t(ms)'; p.fontsize=5 (also... p.gc.setTitleText('title')) \n"
	        +"yrange:                          p.yrange(low,high) sets the yrange to low,high; p.yrange() sets it to auto \n"
	        +"xrange:                          p.xrange(low,high) sets the xrange to low,high; p.xrange() sets it to auto \n"
	        +"resizewindow:                    p.resizewindow(10,10,500,400) resizes the window (x,y,width,height)\n"
	        +"saving:						   p.copy() clipboard; p.save() opens dialog (svg,pdf,tif,bmp,gif,png,jpg); p.save('name.svg') saves svg.\n"
	        +"double click on the plot to open a Waterloo Graphics editing window (titles, export, etc)\n";
	 return s;
}




public void yrange(){
	yscale_behavior=RESCALE_Y_TO_XRANGE;
    setRange(user_minxvalue,user_maxxvalue);
   }

public void yrange(double minv, double maxv){
	yscale_behavior=USE_USER_YRANGE;
	user_minyvalue=minv;
	user_maxyvalue=maxv;
    setRange(user_minxvalue,user_maxxvalue);
}

public void xrange(double lowv, double highv){
  setRange(lowv,highv);
}

public void xrange(){
  double maxxvalue=Double.NEGATIVE_INFINITY;
  double minxvalue=Double.POSITIVE_INFINITY;
  for (int j=0;j<datasets.size();j++){
	  dataset d=(dataset)datasets.elementAt(j);
	  d.xs.findRange();
	  if (d.xs.maxvalue>maxxvalue) maxxvalue=d.xs.maxvalue;
	  if (d.xs.minvalue<minxvalue) minxvalue=d.xs.minvalue;
    }
 setRange(minxvalue,maxxvalue);
}


/* this is a work around because I don't understand the waterloo graphics api*/
public void setRange(double lowv, double highv){

 int j;
 double maxyvalue=Double.NEGATIVE_INFINITY;
 double minyvalue=Double.POSITIVE_INFINITY;

 user_minxvalue=lowv; //keep these for later
 user_maxxvalue=highv;

 switch(yscale_behavior) {
            case RESCALE_Y_TO_XRANGE:
                   for (j=0;j<datasets.size();j++){
	                 dataset d=(dataset)datasets.elementAt(j);
                     d.xs.findLowHighValues(lowv,highv); //timeseries now has minvalueindex and maxvalueindex set
                     d.ys.findRange(d.xs.minvalueindex,d.xs.maxvalueindex);
                 	 if (d.ys.minvalue<minyvalue) minyvalue=d.ys.minvalue;
	                 if (d.ys.maxvalue>maxyvalue) maxyvalue=d.ys.maxvalue;
				     }
                    break;
            case RESCALE_Y_TO_FULLRANGE:
                  for (j=0;j<datasets.size();j++){
				 	dataset d=(dataset)datasets.elementAt(j);
				 	if (d.ys.minvalue<minyvalue) minyvalue=d.ys.minvalue;
				 	if (d.ys.maxvalue>maxyvalue) maxyvalue=d.ys.maxvalue;
				   }
                   break;
            case USE_USER_YRANGE:
                 minyvalue=user_minyvalue;
                 maxyvalue=user_maxyvalue;
                 break;
		 }

  double height=(maxyvalue-minyvalue);
  double padding=0;
  if (yscale_behavior!=USE_USER_YRANGE) padding=height*0.05;;
  gc.getView().setAxesBounds(new Rectangle.Double(lowv,minyvalue-padding,highv-lowv,(height+2*padding)));
}

/*
public void setRange(double lowv, double highv){
  double maxvalue=-1*(Double.MAX_VALUE-1);
  double minvalue=Double.MAX_VALUE;

  for (int j=0;j<datasets.size();j++){
	dataset d=(dataset)datasets.elementAt(j);
	if (d.ys.minvalue<minvalue) minvalue=d.ys.minvalue;
	if (d.ys.maxvalue>maxvalue) maxvalue=d.ys.maxvalue;
  }
  double height=(maxvalue-minvalue);
  double padding=height*0.05;
  //System.out.println("height ="+height+" padding ="+padding);
  gc.getView().setAxesBounds(new Rectangle.Double(lowv,minvalue-padding,highv-lowv,(height+2*padding)));
}
*/


public TimeSeries getXs(int length){
  double[] xs=new double[length];
  for (int i=0;i<length;i++) xs[i]=i;
  TimeSeries xst=new TimeSeries(xs);
  return xst;
}

public void show(){

	GView gv=GView.getGView();
	inf=new JInternalFrame("waterloo graph",true, true, true, true);
	inf.getContentPane().add(this);
	inf.pack();
	inf.setSize(300,300);
	inf.setVisible(true);
	gv.desktop.add(inf);
	gv.updateWindowList();
	 try {
	        inf.setSelected(true);
    } catch (java.beans.PropertyVetoException e) {}

}

public void resizewindow(int x, int y, int w, int h){
	inf.reshape(x,y,w,h);
}


public void scatter(dataset d){
	if (d.s==null) d.s=GJScatter.createInstance();
	d.s.setLineColor(d._color);
	d.s.setXData(d.xs.doubles());
    d.s.setYData(d.ys.doubles());
    d.s.setEdgeColor(d._color);
    gc.getView().add(d.s);
}

public void lines(dataset d){
	if (d.p==null) d.p=GJLine.createInstance();
	d.p.setLineColor(d._color);
	d.p.setXData(d.xs.doubles());
    d.p.setYData(d.ys.doubles());
    gc.getView().add(d.p);
}


public static TimeSeries getTimeSeries(double[] arr){ return new TimeSeries(arr);}

public Plot(double[] arr){
	this(getTimeSeries(arr));
}



String _xlabel=null;
String _ylabel=null;
int _fontsize=4;

public int getFontsize(){return _fontsize;}
public void setFontsize(int v){_fontsize=v; setXlabel(_xlabel); setYlabel(_ylabel);}

public String getXlabel(){return _xlabel;}
public void setXlabel(String str){
	_xlabel=str;
	if (str==null) gc.getView().setXLabel("");
	String tmp="<html><font size="+_fontsize+">"+str+"</font></html>";
	gc.getView().setXLabel(tmp);
}

public String getYlabel(){return _ylabel;}
public void setYlabel(String str){
	_ylabel=str;
	if (str==null) gc.getView().setYLabel("");//for some reason null doesn't work
	String tmp="<html><font size="+_fontsize+">"+str+"</font></html>";
	gc.getView().setYLabel(tmp);
}



public BufferedImage getImage(){
	 BufferedImage image = new BufferedImage(gc.getWidth(), gc.getHeight(),
	 BufferedImage.TYPE_INT_RGB);
	 Graphics2D g2 = image.createGraphics();
	 gc.paint(g2);
     g2.dispose();
	return image;
}

public String copy(){
	toClipboard();
	return "copied to clipboard";
}

public void toClipboard(){
		 gvdecoder.ImageUtils.setClipboard(getImage());
		 AnalysisHelper.getAnalysisHelper().Notify("image exported to clipboard");
}

public String save(){
	String str=GView.getGView().fc.chooseFile(gvdecoder.PropertyHelper.getPropertyHelper().getProperty("TempImages dir"));
	if (str!=null) return save(str);
	return "not saved";
}

public String save(String path){
	if (path.endsWith(".svg")||(path.endsWith(".SVG"))) return savesvg(path);
	else if (path.endsWith(".pdf")||(path.endsWith(".PDF"))) return savepdf(path);
	else if (path.endsWith(".BMP")||path.endsWith(".bmp")){
	  gvdecoder.ImageUtils.WriteImage(path,getImage(),"bmp");
	  return "saved bmp file";
	}
	else if (path.endsWith(".tiff")||path.endsWith(".tif")|| path.endsWith(".TIF")|| path.endsWith(".TIFF")){
		  gvdecoder.ImageUtils.WriteImage(path,getImage(),"tiff");
		  return "saved tiff file";
		}
	else if (path.endsWith(".jpeg")||path.endsWith(".jpg")||path.endsWith(".JPG")||path.endsWith(".JPEG")){
			  gvdecoder.ImageUtils.WriteImage(path,getImage(),"jpeg");
			  return "saved jpeg file";
		}
	else if (path.endsWith(".png")||path.endsWith(".PNG")){
			  gvdecoder.ImageUtils.WriteImage(path,getImage(),"png");
			  return "saved png file";
		}
	else return "Not saved - please add 'svg','pdf','tif','jpg','bmp' extensions to the filename.";
}



public String savesvg(String path){
 try{
	ExportFactory.saveAsSVG(gc,path);
}catch(IOException e){e.printStackTrace(); return "Failed, see stdout"; }
 return "done saveing svg";
}

/*
public String saveeps(String path){
 try{
	ExportFactory.saveAsEPS(gc,path);
}catch(IOException e){e.printStackTrace(); return "Failed, see stdout"; }
 return "done saving eps";
}
*/
public String savepdf(String path){
 try{
	ExportFactory.saveAsPDF(gc,path);
}catch(IOException e){e.printStackTrace(); return "Failed, see stdout"; }
 return "done saving pdf";
}

public Plot(TimeSeries ts){
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


public Plot(TimeSeries[] tsarr){
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


public Plot(){
	gc = GJGraphContainer.createInstance(GJGraph.createInstance());
	this.setLayout(new BorderLayout());
	gc.getView().autoScale();
	this.add(gc, BorderLayout.CENTER);
	show();
}



public Plot(NavigatorGraph nav){
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

public Plot(double[] xs, double [] ys){

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

Plot pp;

public rangeListener(Plot pp){
 this.pp=pp;
}
 public void scrollChange(ScrollControl source) {
    pp.setRange(source.lowValue,source.highValue);
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
  f.getContentPane().add(new Plot(null,null), BorderLayout.CENTER);
  f.pack();
  f.setVisible(true);
}


class dataset{
	TimeSeries xs;
	TimeSeries ys;
	TimeSeries es;
	java.awt.Color _color;
	float w=1.0f;
	//double marksize=5;
	String _pointstyle="circles";
	String _linestyle="solid";
	double _pointsize=5;
	boolean _lines=true;
	boolean _points=false;
	boolean _fill=true;
	boolean _errorbars=false;
	boolean _bars=false;
	GJPlotInterface p; //primary
	GJPlotInterface s; //secondary (scatter plot)
	GJPlotInterface b; //error bars
	private boolean _reset=false;
	public dataset(){this.p=GJLine.createInstance();}//never used


	public void   setEdgewidth(double w){ this.w=(float)w; _setlinestyle(); repaint();}
	public double getEdgewidth(){ return(double)w;}
	public void   setMarksize(double s){this._pointsize=s; _setpointstyle(); repaint();}
	public double  getMarksize(){return _pointsize;}
    public TimeSeries getX(){reload(); return xs;}
    public void       setX(TimeSeries newx){xs=newx; if (p!=null) p.setXData(xs.doubles()); if (s!=null) s.setXData(xs.doubles()); if (b!=null) b.setXData(xs.doubles()); gc.getView().autoScale();}
    public TimeSeries getY(){reload(); return ys;}
    public void       setY(TimeSeries newy){ys=newy; if (p!=null) p.setYData(ys.doubles()); if (s!=null) s.setYData(ys.doubles()); if (b!=null) b.setYData(ys.doubles()); gc.getView().autoScale();}

    public TimeSeries getE(){reload(); return es;}
    public void       setE(TimeSeries newe){es=newe; _errorbars=true; resetplot();  gc.getView().autoScale();}

    public void    setEdges(boolean v){_lines=v; resetplot();} //lines didn't work with introspection
    public boolean getEdges(){return _lines;}
    public void    setMarks(boolean v){_points=v; resetplot();}
    public boolean getMarks(){return _points;}
    public void    setErrorbars(boolean v){_errorbars=v;resetplot();}
    public boolean getErrorbars(){return _errorbars;}
    public String    getEdgestyle(){return _linestyle;}
    public void      setEdgestyle(String str){_linestyle=str; _setlinestyle(); repaint();}
    public java.awt.Color getColor(){return _color;}
    public void setColor(java.awt.Color col){_color=col; if (p!=null) p.setLineColor(_color); if (s!=null) {s.setEdgeColor(_color); _setpointstyle();} if (b!=null) b.setLineColor(_color); repaint();}
    public String getMarkstyle(){return _pointstyle;}
    public void setMarkstyle(String ps){_pointstyle=ps; _setpointstyle(); repaint();}
    public boolean getMarkfill(){return _fill;}
    public void setMarkfill(boolean v){_fill=v;  _setpointstyle(); repaint();}
    public void setPlotstyle(String str){_plotstyle=str; changePlotStyle();}
    public String getPlotstyle(){return _plotstyle;}

    public void addErrorbars(double [] vs){setE(new TimeSeries(vs));}

    private void reload(){
		SwingUtilities.invokeLater(new Runnable(){
		 public void run(){
		 if (p!=null) {p.setXData(xs.doubles());p.setYData(ys.doubles());}
		 if (s!=null) {s.setXData(xs.doubles());s.setYData(ys.doubles());}
		 if (b!=null) {b.setXData(xs.doubles());b.setYData(ys.doubles());
		               if (es!=null){
						 b.setExtraData1(es.doubles());
						 b.setExtraData3(es.doubles());
						 }
					   }
		 gc.getView().autoScale();
	 }
	});
  }

  /*

    public void _errorbars(double[] es){
		if (es==null) removeErrorBars();
		else addErrorBars(new TimeSeries(es));
	}

	public void _errorbars(TimeSeries es){
		if (es==null) removeErrorBars();
		else addErrorBars(es);
	}

    public void _errorbars(double v){
		if (v==0) removeErrorBars();
		else{
		 TimeSeries es=new TimeSeries(xs.length);
		 for (int i=0;i<xs.length;i++) es.arr[i]=v;
	     addErrorBars(es);
	    }
	}

    public void addErrorBars(TimeSeries es){
	   	if (e==null) e=GJErrorBar.createInstance();
	   	e.setXData(xs.doubles());
	   	e.setYData(ys.doubles());
	   	e.setExtraData1(es.doubles());
	   	e.setExtraData3(es.doubles());
	   	e.setLineColor(_color);
	   	gc.getView().add(e);
	    gc.getView().autoScale();
	}

	public void removeErrorBars(){
		if (e!=null) gc.getView().remove(e);
	    gc.getView().autoScale();

	}
*/

    private void resetplot(){
     if (p!=null) gc.getView().remove(p);
	 if (s!=null) gc.getView().remove(s);
	 if (b!=null) gc.getView().remove(b);
	 if (_lines)  _setlines();
	 if (_errorbars)_seterrorbars();
	 if (_points) _setpoints();
	 if (_bars) _setbars();
	 repaint();
	}
    private void _setpoints(){
		if (s==null) s=GJScatter.createInstance();
		s.setLineColor(_color);
		s.setXData(xs.doubles());
	    s.setYData(ys.doubles());
	    s.setEdgeColor(_color);
	    _setpointstyle();
	    gc.getView().add(s);
	}

	private void _setlines(){
		if (p==null) p=GJLine.createInstance();
		p.setLineColor(_color);
		p.setXData(xs.doubles());
	    p.setYData(ys.doubles());
	    _setlinestyle();
	    gc.getView().add(p);
	}

	private void _setbars(){
			if (p==null) p=GJBar.createInstance();
			p.setLineColor(_color);
			p.setXData(xs.doubles());
		    p.setYData(ys.doubles());
		    _setlinestyle();
		    gc.getView().add(p);
	}



    private void _seterrorbars(){
		if (b==null) b=GJErrorBar.createInstance();
	    b.setXData(xs.doubles());
	    b.setYData(ys.doubles());
	    if (es!=null){
	     b.setExtraData1(es.doubles());
	     b.setExtraData3(es.doubles());
	     }
	   	b.setLineColor(_color);
	    gc.getView().add(b);
	}


    private void _setpointstyle(){
		GJMarker mark=null;
		if (_pointstyle=="squares") mark=GJMarker.Square(_pointsize);
		if (_pointstyle=="dots") mark=GJMarker.Dot(_pointsize);
		if (_pointstyle=="circles") mark=GJMarker.Circle(_pointsize);
		if (_pointstyle=="diamonds") mark=GJMarker.Diamond(_pointsize);
		if (_pointstyle=="triangles") mark=GJMarker.Triangle(_pointsize);
		if ((mark!=null)&&(s!=null)) s.setMarker(mark);
		if (s!=null){
			if (_fill) s.setFill(_color);
			else s.setFill(java.awt.Color.white);
		 }
	   }

    public String _plotstyle="line";
    public void changePlotStyle(){
       if ((_plotstyle=="line")||(_plotstyle=="lines")){
		gc.getView().remove(p);
		p=null;
		_lines=true; _bars=false;
		_setlines();
	   }
	   if ((_plotstyle=="bar")||(_plotstyle=="bars")){
		gc.getView().remove(p);
		p=null;
		_lines=false; _bars=true;
        _setbars();
	   }
	}


    private void _setlinestyle(){
		 BasicStroke stroke = null;
		 if (_linestyle=="solid")   stroke=new BasicStroke(w,BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL);
		 if (_linestyle=="dash")  {
				float[]tmp=new float[1];
				tmp[0]=w*4;
				stroke=new BasicStroke(w,BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL,5.0f,tmp,0);
				}
		 if (_linestyle=="dot"){
				float[]tmp=new float[2];
				tmp[0]=w;
				tmp[1]=w*4;
				stroke=new BasicStroke(w,BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL,5.0f,tmp,0);
	     }

		if (p!=null) p.setLineStroke(stroke);
	}


	public dataset(TimeSeries xs, TimeSeries ys, java.awt.Color color){
		this.xs=xs;
		this.ys=ys;
		if (color==null) this._color=java.awt.Color.BLACK;
		else
		 this._color=color;
		this.p=GJLine.createInstance();
		p.setLineColor(this._color);
		w=1.0f;
		p.setXData(xs.doubles());
		p.setYData(ys.doubles());
	}
	public dataset(double[] xs, double[] ys, java.awt.Color color){
			this.xs=new TimeSeries(xs);
			this.ys=new TimeSeries(ys);
			if (color==null) this._color=java.awt.Color.BLACK;
			else
			 this._color=color;
			this.p=GJLine.createInstance();
			p.setLineColor(this._color);
			p.setXData(this.xs.doubles());
			p.setYData(this.ys.doubles());
			w=1.0f;
	}

}


}


/*not used
public void pointstyle(dataset d, String mtype, double size){
	GJMarker mark=null;
	if (mtype=="squares") mark=GJMarker.Square(size);
	if (mtype=="dots") mark=GJMarker.Dot(size);
	if (mtype=="circles") mark=GJMarker.Circle(size);
	if (mtype=="diamonds") mark=GJMarker.Diamond(size);
	if (mtype=="triangles") mark=GJMarker.Triangle(size);
	if (mark!=null) d.s.setMarker(mark);
}
*/
/*
TimeSeries selectedset;

public TimeSeries __getitem__(int z){
	if (z>=datasets.size()) throw Py.IndexError("index out of range");
	selectedset=((dataset)datasets.elementAt(z)).ys;
	return selectedset;
}
*/
/*not used
public void __setitem__(int z, TimeSeries ts){
	if (z>=datasets.size()) throw Py.IndexError("index out of range");
	dataset d=(dataset)datasets.elementAt(z);
	d.ys=ts;
	if (d.p!=null) d.p.setYData(ts.doubles());
	if (d.s!=null) d.s.setYData(ts.doubles());
	gc.getView().autoScale();
	selectedset=ts;
}


public void __setitem__(int z, java.awt.Color color){
	if (z>=datasets.size()) throw Py.IndexError("index out of range");
	 dataset d=(dataset)datasets.elementAt(z);
	 if (color==null) color=java.awt.Color.black;
	 d._color=color;
	 if (d.p!=null) {d.p.setLineColor(color);}
	 if (d.s!=null) {d.s.setEdgeColor(color);}
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
    BasicStroke stroke = null;
	if (str=="solidline")   stroke=new BasicStroke(d.w,BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL);
	if (str=="dashedline")  {
		float[]tmp=new float[1];
		tmp[0]=d.w*4;
		stroke=new BasicStroke(d.w,BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL,10.0f,tmp,0);
		}
	if (str=="dottedline"){
		float[]tmp=new float[2];
		tmp[0]=d.w;
		tmp[1]=d.w*4;
		stroke=new BasicStroke(d.w,BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL,10.0f,tmp,0);
	  }
    if (str=="points"){
	 if (d.p!=null) gc.getView().remove(d.p);
	 if (d.s!=null) gc.getView().remove(d.s);
	 scatter(d);
	 }
	if (str=="lines"){
	 if (d.s!=null) gc.getView().remove(d.s);
	 if (d.p!=null) gc.getView().remove(d.p);
	 lines(d);
	}
	if (str=="linespoints"){
	 if (d.s!=null) gc.getView().remove(d.s);
	 if (d.p!=null) gc.getView().remove(d.p);
	 lines(d);
	 scatter(d);
	}else
	if ((str=="squares")||(str=="circles")||(str=="triangles")||(str=="diamonds")||(str=="dots")){
		d._pointstyle=str;
		pointstyle(d,str,pointsize);
	}
	if (stroke!=null) d.p.setLineStroke(stroke);
	repaint();
}



public void __setitem__(int z, java.awt.BasicStroke s){
	if (z>=datasets.size()) throw Py.IndexError("index out of range");
	((dataset)datasets.elementAt(z)).p.setLineStroke(s);
	repaint();
}

public String help(){
	return "commands (interface for waterloo.sorceforge.net):\n 'p=gvdecoder.Plot(set1)', 'p.add(set2)', 'p.add(xs,ys)','p.add(xs,ys,java.awt.Color.blue)'\n set type of line: p[0]='solidline','dashedline', or 'dottedline'\n set points or lines: p[0]='points','lines', or 'linespoints'\n set line width p[0]=2, p[1]=5.\n set point style: p[0]='squares','circles','triangles','diamonds',or 'dots'\n pointsize: 'p.setpointsize(5)' before setting point style.\n setting color: 'p[1]=java.awt.Color.red'\n scaling: 'p[0]=p[0]*10', 'p[1]=p[1]+30'.\n double click to edit or export.";
}


public void setpointsize(double v){
	pointsize=v;
	for (int j=0;j<datasets.size();j++){
	 dataset d=(dataset)datasets.elementAt(j);
	 pointstyle(d,d._pointstyle,pointsize);

   }
  repaint();
}

*/



/*
public void reload(){
  for (int j=0;j<datasets.size();j++){
	dataset d=(dataset)datasets.elementAt(j);
	if (d.p!=null){
     d.p.setXData(d.xs.doubles());
     d.p.setYData(d.ys.doubles());

   }
   if (d.s!=null){
	 d.s.setXData(d.xs.doubles());
     d.s.setYData(d.ys.doubles());
	}
   }
     gc.getView().autoScale();
}
*/


























