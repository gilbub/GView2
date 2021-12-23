package gvdecoder;

class BackingArray  extends ImageDecoderAdapter{

 int[] arr;
 int xdim;
 int ydim;
 Viewer2 vw;
 NeoController neo;



 public BackingArray(NeoController neo, int xdim, int ydim){
  this.arr=new int[xdim*ydim];
  this.xdim=xdim;
  this.ydim=ydim;
  this.neo=neo;
 }

 public BackingArray(int arrsize){
  this.arr=new int[arrsize];
  }

  public BackingArray(int[] intarray){
   this.arr=intarray;
   }

  public void show(GView gv, String title){
   	vw=gv.openImageFile(this,title);

  }

 public boolean swap=true;

 public int UpdateImageArray(int[] arr,int xdim,int ydim, int instance){

    long t=System.currentTimeMillis();
     neo.GetData(arr,0,0,xdim,ydim,0);
     System.out.println("array update"+(System.currentTimeMillis()-t));

   return 1;
   }

   public int OpenImageFile(String filename){ return 0;}
     public  int CloseImageFile(int instance){return 0;}
     public int FilterOperation(int OperationCode, int startx, int endx, int instance){return 0;}
     public String ReturnSupportedFilters(){return "";}
     public int ReturnFrameNumber(){return 0;}
     public int JumpToFrame(int framenum, int instance){
		 //neo.GetData(arr,0,0,xdim,ydim,0);
		 return 0;
		 }
   public  int ReturnXYBandsFrames(int[] dat, int instance){
	   dat[0]=xdim;
	   dat[1]=ydim;
	   dat[2]=1;
	   dat[3]=1;
	   return 1;
	   }


 }


