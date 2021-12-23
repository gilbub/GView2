public class gvdecoder.JythonHelper{

int vieweractive;
int tracegroupactive;
public TraceGroup tg=null; //active trace group
public Trace tr=null;	   //active trace
public int[] va;//the scaled image mod 256 etc.
public int[] da; //actual data
public double[] no=null; //normalized
public double[] ba=null; //background
public Viewer3 vw=null;


public JythonHelper(){
	vieweractive=0; //false in jython land
	tracegroupactive=0;
}

public void setTraceGroup(TraceGroup tracegroup){
	this.tg=tracegroup;
	tracegroupactive=1;
}

public void setViewer(Viewer3 viewer){
	this.vw=viewer;
	this.va=viewer.viewArray;
	this.da=viewer.datArray;
	this.no=viewer.normalized;
	this.ba=viewer.background;
	vieweractive=1;
}

}