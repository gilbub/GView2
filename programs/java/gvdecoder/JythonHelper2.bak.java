import gvdecoder.trace.*;
import ij.process.*;
import java.awt.*;
import java.awt.image.*;
import java.awt.image.renderable.*;
import ij.*;

/* things it should be able to do
   grab a stack of raw or normalized data from a viewer, change some parameters, make a movie
   from same stack, make a contour plot
   rescale and place the traces in a navigator, or put them in another chart.
   use jsci classes on the image or the traces
   figure out intersection points, mark them and store in a file
*/

public class JythonHelper{

int vieweractive;
int tracegroupactive;
public TraceGroup tg=null; //active trace group
public Trace tr=null;	   //active trace
public int[] va;//the scaled image mod 256 etc.
public int[] da; //actual data
public double[] no=null; //normalized
public double[] ba=null; //background
public Viewer2[] vw=null;
public GView gv=null;
public int width;
public int height;
public quickPlot pl=null;
public int index;

public JythonHelper(){
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

public void quickPlot(double[] dat, double start, double end, int logscale){
     pl=new quickPlot(dat,start,end,logscale);
    		    pl.setVisible(true); //necessary as of kestrel
    	        gv.desktop.add(pl);
    	        try {
    	            pl.setSelected(true);
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




public void setViewer(Viewer2 viewer){
	if (vw==null) {
		vw=new Viewer2[20];
		index=0;
	} else
	index++;
	this.vw[index]=viewer;
	this.va=viewer.viewArray;
	this.da=viewer.datArray;
	this.no=viewer.normalize;
	this.ba=viewer.background;
	vieweractive=1;
	if (index>=vw.length) {
		index=0;
		System.out.println("WARNING! overwriting vw[0] in memory");
	}

}

}