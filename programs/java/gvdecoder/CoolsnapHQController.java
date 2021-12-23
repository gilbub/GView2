package gvdecoder;

public class CoolsnapHQController extends ImageDecoderAdapter{

 static {
      System.loadLibrary("CoolsnapHQ");
   }
   native void StartupCamera();
   native void SetupFocus();

   native int GetFocusImage();
   native int GetData(int[] dat, int width, int height);
   native void StoreData(double[] data, int zdim, int ydim, int xdim);
   native void ShutdownFocus();
   native void ShutdownCamera();


  int xdim;
  int ydim;
  Viewer2 vw;
  GView gv;
  int[] buffer;
  public boolean focuson=false;
  public CoolsnapHQController(GView gv){
	  this.gv=gv;
  }
  public int UpdateImageArray(int[] arr,int xdim,int ydim, int instance){
	 //System.out.println("before getdata");
	 GetData(arr,xdim,ydim);
     //System.out.println("after getdata");
     return 1;
     }

  public long store(Matrix ma){
	//ShutdownFocus();
	SetupFocus();
	long ct=System.currentTimeMillis();

	if (buffer==null){ buffer=new int[xdim*ydim];}
	for (int z=0;z<ma.zdim;z++){
	  GetData(buffer,xdim,ydim);
	  for (int y=0;y<ydim;y++){
		  for (int x=0;x<xdim;x++){
			  ma.dat.set(z,y,x,buffer[y*xdim+x]);
		  }
	  }
    }
	long dur=System.currentTimeMillis()-ct;
	ShutdownFocus();
	return dur;
  }

  public int OpenImageFile(String filename){ return 0;}
  public  int CloseImageFile(int instance){return 0;}
  public int FilterOperation(int OperationCode, int startx, int endx, int instance){return 0;}
  public String ReturnSupportedFilters(){return "";}
  public int ReturnFrameNumber(){return 0;}
  public int JumpToFrame(int framenum, int instance){return 0;}
   public  int ReturnXYBandsFrames(int[] dat, int instance){
	   dat[0]=xdim;
	   dat[1]=ydim;
	   dat[2]=1;
	   dat[3]=1;
	   return 1;
	   }
   public void show(GView gv, String title){
   	vw=gv.openImageFile(this,title);
  }

  public void prepCamera(String filename, int xdim, int ydim){
  		if ((vw==null)||(this.xdim!=xdim)||(this.ydim!=ydim)){
  			this.xdim=xdim;
  			this.ydim=ydim;
  			show(gv,"CoolsnapHQ");
		}
     focuson=true;
 }



}