package gvdecoder;

class BaslerBackingArray  extends ImageDecoderAdapter{

 int[] arr;
 int xdim;
 int ydim;
 Viewer2 vw;
 BaslerController bas;
 int viewmode=1;




 public BaslerBackingArray(BaslerController bas, int xdim, int ydim, int viewmode){
  this.arr=new int[xdim*ydim];
  this.xdim=xdim;
  this.ydim=ydim;
  this.bas=bas;
  this.viewmode=viewmode;
 }

 public BaslerBackingArray(int arrsize){
  this.arr=new int[arrsize];
  }

  public BaslerBackingArray(int[] intarray){
   this.arr=intarray;
   }

  public void show(GView gv, String title){
   	vw=gv.openImageFile(this,title);

  }

 public boolean swap=true;

 public int circularbufferframe=0;

 public int UpdateImageArray(int[] v_arr,int xdim,int ydim, int instance){
	/*
	if (bas.focuson){
    long t=System.currentTimeMillis();
     bas.GetData(v_arr,0,0,xdim,ydim,0);
     System.out.println("array update"+(System.currentTimeMillis()-t));
    }
    else*/
    if (bas.playbackcircularbuffer){
      bas.getCircularBufferFrame(circularbufferframe, arr);
	  circularbufferframe+=1;
	  if (circularbufferframe>=bas.getCircularBufferStoredFrameNumber()) circularbufferframe=0;
	}
	switch(viewmode){
		case 0://vertical
	        for (int i=0;i<xdim*ydim;i++){v_arr[i]=arr[i];}
	        break;
	    case 1://horizontal
	      int i_index,o_index,i,y,x;
	      for (i=0;i<bas.numberofcameras;i++){
	        for (y=0;y<bas.ydim;y++){
				for (x=0;x<bas.xdim;x++){
					i_index=i*bas.xdim*bas.ydim+y*bas.xdim+x;
					o_index= (bas.xdim*bas.numberofcameras)*y+(i*bas.xdim)+x;
					v_arr[o_index]=arr[i_index];
				}
			}
		}
	  }

   return 1;
   }

   public int OpenImageFile(String filename){ return 0;}
     public  int CloseImageFile(int instance){return 0;}
     public int FilterOperation(int OperationCode, int startx, int endx, int instance){return 0;}
     public String ReturnSupportedFilters(){return "";}
     public int ReturnFrameNumber(){return 0;}
     public int JumpToFrame(int framenum, int instance){
		 //neo.GetData(arr,0,0,xdim,ydim,0);
		 circularbufferframe=framenum;
		 //if (circularbufferframe>=bas.getCircularBufferStoredFrameNumber()) circularbufferframe=0;
		 return 0;
		 }
   public  int ReturnXYBandsFrames(int[] dat, int instance){
	   dat[0]=xdim;
	   dat[1]=ydim;
	   dat[2]=1;
	   if (bas.playbackcircularbuffer) dat[3]=bas.getCircularBufferStoredFrameNumber();
	   else
	   dat[3]=1;
	   return 1;
	   }


 }


