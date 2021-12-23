package gvdecoder;
import java.io.*;

public class NeoController{

	static {
			System.loadLibrary("neo1");
	}

    public native int Record();
	public native int GetData(int[] datArray, int startx, int starty, int width, int height, int skip);
	public native int StartFocus();
	public native int StopFocus();
	public native int LoadParameters(String filename);
	public native int WriteParameters(String filename);
	public native int InitializeCamera(NeoController neo);
	public native int CloseCamera();

    public native int GetCameraParameters(double[] arr); //return status, lasterror, framerate, exposetime, temperature
    public native int SetCameraParameters(double[] arr); //set framerate, exposetime


    public Viewer2 vw;
    public int xdim;
    public int ydim;
    public BackingArray ba;
    public boolean focuson=false;
    NeoGui gui;
    GView gv;

    public NeoController(GView gv){
		 this.gv=gv;
		 gui=new NeoGui(this);

		 InitializeCamera(this);
         gui.showgui();
		}

    public String CameraCallback(String arg){
		//System.out.println("From java: "+arg);
        gui.treegui.botPane.append(arg+"\n");
        return "0";
	}

    public void startfocus(String filename){
		//StartFocus(filename);
		focuson=true;
	}
	public void stopfocus(){
		focuson=false;
		StopFocus();
	}

	public void setParam(double framerate, double exposure){
		double[] tmp=new double[2];
		tmp[0]=framerate;
		tmp[1]=exposure;
		SetCameraParameters(tmp);

	}

    public void prepCamera(String filename, int xdim, int ydim){
		if ((vw==null)||(this.xdim!=xdim)||(this.ydim!=ydim)){
			this.xdim=xdim;
			this.ydim=ydim;
			ba=new BackingArray(this,xdim,ydim);
			ba.show(gv,"Neo Control");
			this.vw=ba.vw;
		}

		LoadParameters(filename);
		File f=new File(filename);
		String camdata=f.getParent()+File.separator+"CameraStatus.txt";
		System.out.println("writing camera data to "+camdata);
		WriteParameters(camdata);
		}

	public void startfocus(String filename,int xdim, int ydim){
		prepCamera(filename,xdim,ydim);
		StartFocus();
		focuson=true;
	}


	public void record(String filename,int xdim, int ydim){
		 prepCamera(filename,xdim,ydim);
		 Record();
	}

	public void exit(){
		focuson=false;
		CloseCamera();
		gui.guiframe.dispose();

	}


    /*
	public NeoController(GView gv){
		this.xdim=2592;
		this.ydim=2160;
		ba=new BackingArray(this,xdim,ydim);
		ba.show(gv,"Neo Control");
        this.vw=ba.vw;
	}
    */
    public NeoController(GView gv,int xdim,int ydim){

        this.xdim=xdim;
        this.ydim=ydim;
        ba=new BackingArray(this,xdim,ydim);
        ba.show(gv,"Neo Control");
        this.vw=ba.vw;

	}

    public void show(GView gv, String title){
		ba.show(gv,title);
	}

    public void updateViewer(){

		GetData(vw.datArray,0,0,xdim,ydim,0);
		vw.jp.ARRAYUPDATED=true;
		vw.jp.forceRepaint();

	}
}

/*
	public static void main(String[] argv)
	{
		String retval = null;
		nativetest nt = new nativetest();
		retval = nt.sayHello("neoconfig2.txt");
		System.out.println("Invocation returned " + retval);
	}
*/

