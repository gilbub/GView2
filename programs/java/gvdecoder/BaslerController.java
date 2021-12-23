package gvdecoder;

import javax.swing.JProgressBar;
import javax.swing.JLabel;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.BufferedOutputStream;

import java.io.IOException;

public class BaslerController implements Runnable{
 static {
  System.loadLibrary("BaslerPylonAdapter");
 }

  public native int openCamera();
  public native int closeCamera();
  public native int startFocus(int exposetime_us);
  public native int stopFocus();
  public native int GetData(int[] datArray, int startx, int starty, int width, int height, int skip);
  public native int saveImages(String path, int number_of_images, int exposetime_us);


  public native int saveToCircularBufferMulti(String path, int numberofframes);
  public native int setActiveCamera(int cameranumber);

  public native int saveToCircularBuffer();
  public native int saveToCircularBufferFilter();
  public native int getCircularBufferStoredFrameNumber();
  public native int getCircularBufferFrame(int framenumber, int[] datArray);

  public native int loadConfigFile(String filename);
  public native int writeStatus(String filename);

  public native int switchCameraOrder(int cam1, int cam2);
  public native void mirror(boolean horizontal, boolean vertical);


  public native double setExposeTime(int exposetime_us);
  public native double setFrameRate(double fps);
  public native void setGainAuto(int mode); //0 auto_off, auto_once, auto_continuous
  public native int setGainValue(double va);
  public native int setTrigger(int mode);//0 free run, 1 trigger frame, 2 trigger burst
  public native int setOffsets(int x_offset, int y_offset);

  public native int setPosition(String serialnumber, int position);

  public native int setParameters(int camnumber, int[] params);
  public native int getParameters(int camnumber, int[] params);

  public native String getSerialNumber();

  private native void testCamera();
  private native void sayHello();


    public String savepath;
    public int numberofframestosave;

    public JProgressBar save_progressbar;
    public JLabel camera_status;
    public JLabel buffer_status;

    public static String save_path;
    public static int save_n_images;
    public static int save_exposetime_us;

    public Viewer2 vw;
    public int xdim=128;
    public int ydim=128;
    public int xoffset=0;
    public int yoffset=0;
    public int numberofcameras=2;
    public int circularbuffersize=1000;

    public BaslerBackingArray ba;
    public BaslerBackingArray bf;
    public boolean focuson=false;
    public boolean playbackcircularbuffer=false;
    public GView gv;
    public long lastViewerUpdateTime;
    public boolean saveToFile=false;
    public boolean saveToCircularBuffer=true;
    public boolean saveToCircularBufferFilter=false;
    public int mode=2;
    public int viewmode=0;
    public boolean status;
    public int bytes_per_pixel;

    boolean save_only_n_frames=false;
    int save_n_frames=0;
    ConfigFileHelper cf;

    boolean cameraconfigured=false;

    public boolean write_bytes_as_shorts=true;


   public boolean setConfigFile(String filename){
	   if (cf!=null) cf.filename=filename;
	   else cf=new ConfigFileHelper(filename);
	   if (cf.read()<=0) {status=false; return status;}
	   readParameters();
	   return true;
   }
   public void  readParameters(){
	   cf.read();
	   xdim=cf.getInt("width");
	   ydim=cf.getInt("height");
	   xoffset=cf.getInt("x_offset");
	   yoffset=cf.getInt("y_offset");
	   numberofcameras=cf.getInt("numberofcameras");
	   mode=cf.getInt("mode");
	   viewmode=cf.getInt("viewmode");
	   circularbuffersize=cf.getInt("framebuffers");
       bytes_per_pixel=cf.getInt("bytes_per_pixel");
   }

  public BaslerController(String filename){
	  if (!setConfigFile(filename)) { System.out.println("Unrecoverable error - unable to open, or empty configfile at "+cf.filename); status=false; }
	  else status =true;
  }




   public void prepCamera(String filename, int xdim, int ydim, int numberofcameras, int viewmode){
	        if (!status) return;
			if ((vw==null)||
			    (this.xdim!=xdim)||(this.ydim!=ydim)||
			    (this.ba.viewmode!=viewmode)||
			    (this.ba.arr.length!=xdim*ydim*numberofcameras)||(this.numberofcameras!=numberofcameras)){
				this.xdim=xdim;
				this.ydim=ydim;
				this.numberofcameras=numberofcameras;
				if (viewmode==0) ba=new BaslerBackingArray(this,xdim,ydim*numberofcameras,viewmode);
                else ba=new BaslerBackingArray(this,xdim*numberofcameras,ydim,viewmode);
				ba.show(gv,filename);
				this.vw=ba.vw;
				lastViewerUpdateTime=System.currentTimeMillis();
             }
             if (save_progressbar!=null) save_progressbar.setMaximum(circularbuffersize);
		 }
   public void prepCamera(){
     readParameters();
     prepCamera("basler", xdim, ydim, numberofcameras, viewmode);

   }

   public void startfocus(int exposetime_us){
   		openCamera();
   		startFocus(exposetime_us);
   		prepCamera("test",xdim,ydim,numberofcameras,0);
   		focuson=true;
   	}


   public void stopfocus(){
	   stopFocus();
	   closeCamera();
	   focuson=false;
   }

   public int testval=1;

   public boolean cancel_save=false;
   public boolean cb_buffer_full=false;

   //camera alerts baslercontroller that a frame is ready.
   //If the user cancels image acquisition (cancel_save==true), return 2
   //If its been over 100ms since the last screen update, then return 1 (which then causes the dll to push a frame
   //else return 0 (which tells the dll to do nothing).
   public int frameReady(int cb_frame){
	 if (cancel_save) {
		 cancel_save=false;
		 return 2;
	 }
	 if ((save_only_n_frames)&&(cb_frame>=save_n_frames)){playbackcircularbuffer=true; return 2;}

	 long t=System.currentTimeMillis();
     if (t-lastViewerUpdateTime>100){
		 if (cb_frame!=-1) {
		 		 if (save_progressbar!=null) save_progressbar.setValue(cb_frame);
		 		 // System.out.println("circular buffer frame= "+cb_frame);
	 }
		 lastViewerUpdateTime=t;
		 return 1;
	 }
	 //System.out.println("frame ready, rejecting");
	 return 0;
   }

   public void pushFrame(byte[] singleframe){
      System.out.println("pushed frame");
      if (saveToCircularBuffer){
	   if (bytes_per_pixel==1)for (int i=0;i<xdim*ydim*numberofcameras;i++) ba.arr[i]=(int)(singleframe[i]&0xFF);
       else
       if (bytes_per_pixel==2) for (int i=0;i<xdim*ydim*numberofcameras;i++) ba.arr[i]= BytesToInt(singleframe[i*2],singleframe[i*2+1]);// (int)(((singleframe[(i*2)+1]&0xFF)<<8)&singleframe[(i*2)]); //((b & 0xff) << 8) & a
       ba.vw.JumpToFrame(0);
      }
      }

   public int BytesToInt(byte a, byte b){
	 return  b<<8 &0xFF00 | a&0xFF;

   }

  public void setSave(String path, int numberofframes){
	  save_path=path;
	  save_n_images=numberofframes;
  }

  public void run(){
	  //setConfigFile("D:\\data\\balsertest\\config.txt");


	  if (!cameraconfigured) {
		  prepCamera(); //this routine reads the config file and sets up the backing array
		  loadConfigFile(cf.filename); //camera setup via dll.
		  cameraconfigured=true;
	  }
	  switch(mode){
		  case 0: //saveToFile
		    saveImages(save_path, save_n_images, save_exposetime_us);
	        break;
	      case 1://saveToCircularBuffer
	        saveToCircularBuffer();
	        break;
	      case 2://saveToCircularBufferMulti
	        saveToCircularBufferMulti(save_path,save_n_images);
	        break;
		}
		/*
		 if (saveToFile) saveImages(save_path, save_n_images, save_exposetime_us);
		 else
		 if (saveToCircularBuffer) {
			 loadConfigFile("D:\\data\\balsertest\\config.txt");
			 saveToCircularBuffer();
		 }
		 else
		 if (saveToCircularBufferFilter){
			 loadConfigFile("D:\\data\\balsertest\\config.txt");
			// saveToCircularBufferFilter();
		 }
         */
	 }


   public void getSingleCameraFrame(Matrix ma, int cameranumber){
    int i=cameranumber*xdim*ydim;
    for (int y=0;y<ma.ydim;y++){
		for (int x=0;x<ma.xdim;x++){
			ma.dat.set(0,y,x,ba.arr[i+y*ma.xdim+x]);
		}
	}
   }

 public void circbuffer(){
	 saveToCircularBuffer=true;
	 saveToFile=false;
	 Thread t = new Thread(this);
	 t.start();
 }




 public void saveCircBuffer(String fullpath){
     int frames=getCircularBufferStoredFrameNumber();
     saveCircBuffer(fullpath,0,frames);
	 /*
	 int [] specs=new int[4];
   	 ba.ReturnXYBandsFrames(specs,0);
   	 int _xdim=specs[0];
	 int _ydim=specs[1];
	 int _bands=specs[2];
	 int _frames=specs[3];
	 int[] _tmp=new int[_xdim*_ydim];

	 try {
	       DataOutputStream file = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(fullpath)));
	       file.writeInt(1);
	       file.writeInt(_frames);
	       file.writeInt(_ydim);
	       file.writeInt(_xdim);


	       for (int i = 0; i < _frames; i++){
			   ba.JumpToFrame(i,0);
			   ba.UpdateImageArray(_tmp,_xdim,_ydim,0);

			   for (int j=0;j<_xdim*_ydim;j++){
				   file.writeShort((short)_tmp[j]);
			   }
		   }
	       file.close();
	     } catch (IOException e) {
	       System.out.println("Error - " + e.toString());
    }

    System.out.println("done saving...");

  */
 }


 //don't use this.
 public void saveCircBuffer(String fullpath, int startframe, int endframe){
	 //int numframes=getCircularBufferStoredFrameNumber();
	 int [] specs=new int[4];
	 ba.ReturnXYBandsFrames(specs,0);
	 int _xdim=specs[0];
     int _ydim=specs[1];
     int _bands=specs[2];
     int _frames=specs[3];
	 int[] _tmp=new int[_xdim*_ydim];

     try {
		       DataOutputStream file = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(fullpath)));
		       if ((write_bytes_as_shorts)||(bytes_per_pixel==2)) file.writeInt(1);
		       else file.writeInt(2);
		       file.writeInt(endframe-startframe);
		       file.writeInt(_ydim);
		       file.writeInt(_xdim);


		       for (int i = startframe; i < endframe; i++){
				   ba.JumpToFrame(i,0);
				   ba.UpdateImageArray(_tmp,_xdim,_ydim,0);
                   if ((write_bytes_as_shorts)||(bytes_per_pixel==2)){
				   for (int j=0;j<_xdim*_ydim;j++){
				   		 file.writeShort((short)_tmp[j]);
				   }
				   }else{
				    for (int j=0;j<_xdim*_ydim;j++){
				   		 file.writeByte((byte)_tmp[j]);
				   }
			   }
			   }
		       file.close();
		     } catch (IOException e) {
		       System.out.println("Error - " + e.toString());
	    }
  System.out.println("done saving...");

 }




 public void save(String mypath, int n_images, int exp_us){
	 saveToCircularBuffer=false;
	 saveToFile=true;
	 save_path=mypath;
	 save_n_images=n_images;
	 save_exposetime_us=exp_us;
	 Thread t = new Thread(this);
	 t.start();
  }

   public static void main(String[] args) {
        new BaslerController("d:\\data\\balsertest\\config.txt").testCamera();  // invoke the native method
     }
}