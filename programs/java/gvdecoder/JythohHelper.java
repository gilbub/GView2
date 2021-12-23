package gvdecoder;
import gvdecoder.trace.*;

public class JythonHelper{

int vieweractive;
int tracegroupactive;
public TraceGroup tg=null; //active trace group
public Trace tr=null;	   //active trace
public int[] va;//the scaled image mod 256 etc.
public int[] da; //actual data
public double[] no=null; //normalized
public double[] ba=null; //background
public Viewer2 vw=null;
public Matrix ma;

public JythonHelper(){
	vieweractive=0; //false in jython land
	tracegroupactive=0;
}

public void setTraceGroup(TraceGroup tracegroup){
	this.tg=tracegroup;
	tracegroupactive=1;
}

public void setViewer(Viewer2 viewer){
	this.vw=viewer;
	this.va=viewer.viewArray;
	this.da=viewer.datArray;
	this.no=viewer.normalized;
	this.ba=viewer.background;
	vieweractive=1;
}

public void memtest(){
	    ma=Matrix()
		ma.initialize(gv.jh.vw[index],start,end)
		gv.openImageFile(ma,"matrix")
		//return ma


}

}