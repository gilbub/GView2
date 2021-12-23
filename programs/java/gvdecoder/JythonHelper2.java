package gvdecoder;
import gvdecoder.trace.*;
import ij.process.*;
import java.awt.*;
import java.awt.image.*;
import java.awt.image.renderable.*;
import ij.*;
import java.util.*;
import JSci.maths.LinearMath.*;

/* things it should be able to do
   grab a stack of raw or normalized data from a viewer, change some parameters, make a movie
   from same stack, make a contour plot
   rescale and place the traces in a navigator, or put them in another chart.
   use jsci classes on the image or the traces
   figure out intersection points, mark them and store in a file
*/

public class JythonHelper2 implements FrameListener{

int vieweractive;
int tracegroupactive;
public TraceGroup tg=null; //active trace group
public Trace tr=null;	   //active trace
public int[] va;//the scaled image mod 256 etc.
public int[] da; //actual data
public double[] no=null; //normalized
public double[] ba=null; //background
public Viewer2[] vw=null;
public static GView gv=null;
public int width;
public int height;
public quickPlot pl;
public HtmlSaver sv;
public String sv_description;
public int index;
public Matrix ma;
public int StopAtFrame=-1;


public boolean plot_connected=true;
public String plot_marker="points";
public String plot_title="";
public String plot_xlabel="";
public String plot_ylabel="";



public void setSaveHtml(HtmlSaver sv, String description){
	this.sv_description=description;
	this.sv=sv;
	if (gv.html!=null){
	 gv.html.setSaveDescription(description);
     gv.html.insertimg.setEnabled(true);
 }
}

public void unsetSaveHtml(HtmlSaver sv){
	if (this.sv==sv){
	  this.sv=null;
	  if (gv.html!=null){
		gv.html.setSaveDescription("click a window");
        gv.html.insertimg.setEnabled(false);
      }
	}

}

public void memtest(){
	    ma=new Matrix();
	    System.out.println("calling index="+index);
		ma.initialize(vw[index],300,500);
		//gv.openImageFile(ma,"matrix");

//		ma.processByTrace("m",5);
		//return ma


}
public void memtest2(){
ma.processByTrace("a",5);
Runtime r=Runtime.getRuntime();
r.gc();
}


public static GView getGView(){
   return gv;
}

public JythonHelper2(){
	vieweractive=0; //false in jython land
	tracegroupactive=0;
}

public void setTraceGroup(TraceGroup tracegroup){
	this.tg=tracegroup;
	tracegroupactive=1;
}


public int getX(){
	return (int)( ((double)vw[index].lastMouseX)/((double)vw[index].viewScale));
}

public int getY(){
	return (int)( ((double)vw[index].lastMouseY)/((double)vw[index].viewScale));
}


public ColorProcessor getColorProcessor(){
	BufferedImage img=vw[index].jp.bi;

	width = img.getWidth(null);
	height = img.getHeight(null);

	BufferedImage dest= new BufferedImage(width,height,BufferedImage.TYPE_INT_ARGB);
	ColorConvertOp op=new ColorConvertOp(null);
	op.filter(img,dest);
	ColorProcessor cp=new ColorProcessor(dest);
	return cp;
}


public void JumpToFrame(int framenum){

		vw[index].im.JumpToFrame(framenum, vw[index].instance);
		vw[index].frameNumber=framenum;
		vw[index].LastUserSetFrame=framenum;
		vw[index].im.UpdateImageArray(vw[index].datArray,vw[index].speinfo.X_dim,vw[index].speinfo.Y_dim,vw[index].instance);
		vw[index].rescale();
		//notify listeners...
		vw[index].NotifyFrameListeners(framenum);

		vw[index].jp.ARRAYUPDATED=true;
		//jp.repaint();
		Graphics g=vw[index].jp.getGraphics();
		vw[index].jp.paintComponent(g);
	}


public ImagePlus getImagePlus(int startframe, int endframe, int increment){

 JumpToFrame(startframe);
 ColorProcessor cp=getColorProcessor();
 ImageStack stack=new ImageStack(width,height);
 stack.addSlice("tmp",cp);
 for (int i=startframe+1; i<endframe;i+=increment){
	JumpToFrame(i);
	cp=getColorProcessor();
	stack.addSlice("tmp",cp);
 }
 ImagePlus ip=new ImagePlus(vw[index].filename+".ip",stack);
 return ip;

}


public int[][] findAllCells(ROI roi){
	int xdim=vw[index].X_dim;
	int ydim=vw[index].Y_dim;
	int numpixels=roi.findAllPixels(xdim, ydim);
	int [][] res=new int [numpixels][2];
	for (int i=0;i<res.length;i++){
		int p=roi.arrs[i];
		int y=(int)((float)p/(float)xdim);
		int x=p-y*xdim;
		res[i][0]=x;
		res[i][1]=y;
	}
	return res;

}



public double[] doublearray(int dim){
	 return new double[dim];
 }


public double[][] fit(double[] xs, double[] ys, int degree){
	double[][] in_array=new double[2][ys.length];
	double[][] out_array=new double[2][ys.length];
	for (int i=0;i<ys.length;i++){
		in_array[0][i]=xs[i];
		in_array[1][i]=ys[i];
	}
	JSci.maths.polynomials.RealPolynomial dv=JSci.maths.LinearMath.leastSquaresFit(degree,in_array);
	for (int x=0;x<ys.length;x++){

	    out_array[0][x]=xs[x];
	    out_array[1][x]=dv.map(xs[x]);
	}
 return out_array;
}






public double[] IntToDoubles(int[] arr){
	double[] new_arr=new double[arr.length];
	for (int i=0;i<new_arr.length;i++){
		new_arr[i]=(double)arr[i];
	}
	 return new_arr;
	}


public void savePlot(String plotname, String plottype){
	BufferedImage img=pl.p.getImage();
	if (img!=null){
		ImageUtils.WriteImage(plotname,img,plottype);
	}

}

public void newPlot(double[]dat,double start, double end, int log){
	if (pl==null) {
		quickPlot(dat,start,end,log);

 }else{
	pl.newPlot(dat,start,end,log);


 }
}

public void quickPlot(int[]dat, double start, double end, int log){
	quickPlot(IntToDoubles(dat), start, end,log);
}

public void quickPlot(double[]xs, double[]ys){
      quickPlot(xs,ys,false,false);
}


public void quickPlot(double[]xs, double[]ys, double[] es){
	pl=new quickPlot(xs,ys,es);

	pl.setTitle("plot="+plotnum++);
    if (plots==null) plots=new Vector();
    plots.add(pl);
	pl.setVisible(true); //necessary as of kestrel
	        gv.desktop.add(pl);
	        try {
	           pl.setSelected(true);
	   	        } catch (java.beans.PropertyVetoException e){};

}

public int plotnum;
public Vector plots=null;
public void quickPlot(double[] xs, double[] ys, boolean logx, boolean logy){
	pl=new quickPlot(xs,ys,logx,logy);
	pl.setTitle("plot="+plotnum++);
	if (plots==null) plots=new Vector();
	plots.add(pl);
	    		    pl.setVisible(true); //necessary as of kestrel
	    	        gv.desktop.add(pl);
	    	        try {
	    	            pl.setSelected(true);
	   	        } catch (java.beans.PropertyVetoException e){};


}

public void quickPlot(double[] dat, double start, double end, int logscale){
     pl=new quickPlot(dat,start,end,logscale);
     pl.setTitle("plot="+plotnum++);
	 if (plots==null) plots=new Vector();
	 plots.add(pl);

     pl.setVisible(true); //necessary as of kestrel
    	        gv.desktop.add(pl);
    	        try {
    	            pl.setSelected(true);
   	        } catch (java.beans.PropertyVetoException e){};


}

public void quickPlot(boolean showbuttons, int xdim, int ydim){
     pl=new quickPlot(showbuttons,xdim,ydim);
     pl.setTitle("plot="+plotnum++);
	 if (plots==null) plots=new Vector();
	 plots.add(pl);

     pl.setVisible(true); //necessary as of kestrel
    	        gv.desktop.add(pl);
    	        try {
    	            pl.setSelected(true);
   	        } catch (java.beans.PropertyVetoException e){};


}

public void findPlot(String title){
 for (int i=0;i<plots.size();i++){
	quickPlot ptemp=(quickPlot)plots.elementAt(i);
	if ((ptemp.p.getTitle()).equalsIgnoreCase(title)) {
		pl=ptemp;
    try {  pl.setSelected(true);
	} catch (java.beans.PropertyVetoException e){};
    break;
	}

  }
}

public String[] listPlots(){
 String[] str=new String[plots.size()];
 for (int i=0;i<plots.size();i++){
	quickPlot ptemp=(quickPlot)plots.elementAt(i);
    str[i]=ptemp.p.getTitle();
  }
 return str;
}

public void quickGnuplot(String filename){
	quickGnuplot qc=new quickGnuplot(filename);
    qc.setVisible(true); //necessary as of kestrel
	gv.desktop.add(qc);
    try {
	  qc.setSelected(true);
	 } catch (java.beans.PropertyVetoException e){};

}

public void print(Object o){
	System.out.println("jython: "+ o.toString());
}

public void print(String s){
	System.out.println("jython: "+s);
}

public void print(double d){
	System.out.println("jython: "+d);
}

public void print(float f){
	System.out.println("jython: "+f);
}

public void print(int i){
	System.out.println("jython: "+i);
}

public void print(int x, int y){
	System.out.println("jython: "+x+" "+y);

}

public void print(double[] d){
	System.out.println("jython: "+JSci.maths.ArrayMath.toString(d));
}

public void print(double[][] d){
	System.out.println("jython: "+JSci.maths.ArrayMath.toString(d));

}

public void print(int[] d){
	System.out.println("jython: "+JSci.maths.ArrayMath.toString(d));
}

public void print(int[][] d){
	System.out.println("jython: "+JSci.maths.ArrayMath.toString(d));

}

public void SetFrame(int framenum){
   if ((StopAtFrame>0)&&(framenum>=StopAtFrame)){
	AllStop();
    StopAtFrame=-1;
   }
}



public void AllJump(int n){
for (int i=0;i<vw.length;i++){
 if (vw[i]!=null){
	vw[i].JumpToFrame(n);
	 }
	}
}


public void AllPlay(){
for (int i=0;i<vw.length;i++){
 if (vw[i]!=null){
	vw[i].Start();
	 }
	}
}


public void AllStop(){
for (int i=0;i<vw.length;i++){
 if (vw[i]!=null){
	vw[i].Stop();
	 }
	}
}


public JythonTable presenttable;
public void setPresentTable(JythonTable table){
	presenttable=table;
}

public void closePresentTable(JythonTable table){
	if (table==presenttable)
	 presenttable=null;
}


public int presentviewerindex;
public Viewer2 presentviewer;

public void setPresentViewer(Viewer2 viewer){
	presentviewer=viewer;
	for (int p=0;p<vw.length;p++){

		if (vw[p]!=null){
		 System.out.println("JythonHelper:: p="+p+" "+vw[p].getTitle());
		}

	}

	if (vw!=null){
		int j=-1;
		for (int i=0;i<vw.length;i++){
			if (vw[i]!=null){
			String tmp1=vw[i].getTitle();
			String tmp2=viewer.getTitle();
			System.out.println("JythonHelper:: comparing "+tmp1+" "+tmp2);
			if (vw[i]==viewer){
				j=i;
				presentviewerindex=j;
				presentviewer=viewer;

				System.out.println("JythonHelper:: last comparison a match");
				break;

			}
		  }
		  System.out.println("JythonHelper::found viewer at index="+j);
		}
     }
}

public void removeViewer(Viewer2 viewer){
	System.out.println("JythonHelper:: try to remove viewer from memory, index="+index+" Viewer has title="+viewer.getTitle());

	for (int p=0;p<vw.length;p++){

		if (vw[p]!=null){
		 System.out.println("JythonHelper:: p="+p+" "+vw[p].getTitle());
		}

	}




	if (vw!=null){
		int j=-1;
		for (int i=0;i<vw.length;i++){
			if (vw[i]!=null){
			String tmp1=vw[i].getTitle();
			String tmp2=viewer.getTitle();
			System.out.println("JythonHelper:: comparing "+tmp1+" "+tmp2);
			if (vw[i]==viewer){
				j=i;
				System.out.println("JythonHelper:: last comparison a match");
				break;

			}
		  }
		  System.out.println("JythonHelper::found viewer at index="+j);
		}
		if (j>=0){
			vw[j]=null;
			for (int k=j;k<vw.length-1;k++){
		     vw[k]=vw[k+1];
		  }
		  index--;
		  System.out.println("JythonHelper::index now set to ="+index);
		}else{System.out.println("JythonHelper:: viewer not found, potential mem leak error!!!");}



	}


}

public void setViewer(Viewer2 viewer){
	if (vw==null) {
		vw=new Viewer2[100];
		index=0;
	} else
	index++;
	if (index>=vw.length) {
			index=0;
			System.out.println("WARNING! overwriting vw[0] in memory");
	}
	this.vw[index]=viewer;
	this.va=viewer.viewArray;
	this.da=viewer.datArray;
	this.no=viewer.normalize;
	this.ba=viewer.background;
	vieweractive=1;


}

}