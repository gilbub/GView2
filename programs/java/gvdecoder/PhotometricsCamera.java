package gvdecoder;

public class PhotometricsCamera extends Matrix{

 public MultiPVCAMController controller;
 public int cameranumber=-1;
 public int[] savebuffer;
 long duration;
 boolean focuson;
 boolean camerasetup;
 String status = "";
 Matrix data;
 public String configfile; //temp storage spot for config file
 public int RelativeFrequency=1;
 public int currentFrame=0;
 public int stopFrame=0;
 public int currentBuffer=0;
 public int CameraCircularBufferSize=1;
 public boolean mirror_h=false;
 public int startTime=0;
 public long[] frameTimes;


 public String toString(){
	 return "PhotometricsCamera "+cameranumber+" "+status;
 }

 public void fliphorizontal(int[]  arr, int xdim, int ydim){
	 for (int y=0;y<ydim;y++){
	  for (int x=0;x<xdim/2;x++){
		  int l_index=y*xdim+x;
		  int r_index=y*xdim+xdim-x-1;
		  int tmp=arr[r_index];
		  arr[r_index]=arr[l_index];
		  arr[l_index]=tmp;
	  }

  }
}

 public PhotometricsCamera(MultiPVCAMController controller, int cameranumber, int zdim, int ydim, int xdim, int fy, int fx){
	 super(1,fy,fx);
	 data=new Matrix(zdim,ydim,xdim);
	 frameTimes=new long[zdim];
	 this.controller=controller;
	 this.cameranumber=cameranumber;
	 focuson=false;
	 camerasetup=false;
	 savebuffer=new int[xdim*ydim];
	 status=" initialized";
 }

 public void reShape(int z, int y, int x){
	 create(z,y,x);
	 savebuffer=new int[x*y];
 }



 public void SetupFocus(String filename){
	 //filename should contain a config file with overwrite=true
	 if (controller.StartCamera(cameranumber,filename)>-1){
	  focuson=true;
	  status="camera "+cameranumber+" in focus mode";
    }
	 else{
	 status="Error: camera "+cameranumber+" can't be set into focus mode";
     focuson=false;
   }
 }

  public void StopFocus(){
	 controller.StopCamera(cameranumber);
	 focuson=false;
	 status="camera "+cameranumber+" not in focus mode";
  }


 public int UpdateImageArray(int[] arr,int xdim,int ydim, int instance){
	 if (!focuson){ return super.UpdateImageArray(arr,xdim,ydim,instance);}
	 else
	 // this method doesn't store information in the matrix object, but just dumps to screen. The camera should be set up in focus mode
	 // by first calling controller.SetupSequence(int camnum, String filename) with filename containing 'overwrite=true'
	 if (focuson) {
		 controller.GetData(cameranumber,arr,xdim,ydim);
		 if (mirror_h) fliphorizontal(arr,xdim,ydim);
	     return 1;
	 }
     //System.out.println("after getdata");
     return -1;
     }

  public long store(String configfile){
	if (focuson) StopFocus();
	startTime=controller.StartCamera(cameranumber,configfile);
    long ct=System.currentTimeMillis();

	if (savebuffer==null){ savebuffer=new int[data.xdim*data.ydim];}
	for (int z=0;z<data.zdim;z++){

	  frameTimes[z]=controller.GetData(cameranumber,savebuffer,data.xdim,data.ydim);

      if (mirror_h) fliphorizontal(savebuffer,data.xdim,data.ydim);
	  for (int y=0;y<data.ydim;y++){
		  for (int x=0;x<data.xdim;x++){
			  data.dat.set(z,y,x,savebuffer[y*data.xdim+x]);
		  }
	  }
    }
	duration=System.currentTimeMillis()-ct;
	controller.StopCamera(cameranumber);
	return duration;
  }


  public void storeContinuous(String configfile, int updateperiod){
	  if (focuson) StopFocus();
	  startTime = controller.StartCamera(cameranumber,configfile);
	  if (savebuffer==null){ savebuffer=new int[data.xdim*data.ydim];}
	  boolean keepimaging=true;
	  int z=0;
	  if ((data.vw!=null)&&(data.vw.jp.rois!=null)) data.setupProcessRois(data.zdim);
	  while(keepimaging){
	  controller.GetData(cameranumber,savebuffer,data.xdim,data.ydim);
      if (mirror_h) fliphorizontal(savebuffer,data.xdim,data.ydim);
	    for (int y=0;y<data.ydim;y++){
		  for (int x=0;x<data.xdim;x++){
			  data.dat.set(z,y,x,savebuffer[y*data.xdim+x]);
		  }
	     }

          if (z%updateperiod==0){
			 if (data.vw!=null){
			    data.vw.JumpToFrame(z);
			    if (data.vw.jp.rois!=null){
					if (z>0) data.processRois(z,z-1);
					else data.processRois(0,data.zdim);
					data.showRois(false);
			      }
			    }
		  }
        z++;
		if (z>=data.zdim) z=0;
		if (Thread.interrupted()){
			status="camera "+cameranumber+" continuous save mode interrupted";
			System.out.println(status);
			keepimaging=false;
	      }

   }
   System.out.println("before "+cameranumber+" StopCamera");
   controller.StopCamera(cameranumber);
   System.out.println("before "+cameranumber+" data.reorder");
   data.reorder(z-1);

  }
 /*This method must be called from PhotometricsCameraSet's run method, with numberOfBufferFrames and configfile set*/
 public int getFrame(){
	  savebuffer[0]=-1;
	  frameTimes[currentFrame]=controller.GetData(cameranumber,savebuffer,data.xdim,data.ydim);
      if ((mirror_h)&&(savebuffer[0]!=-1)) fliphorizontal(savebuffer,data.xdim,data.ydim);
	 	    for (int y=0;y<data.ydim;y++){
	 		  for (int x=0;x<data.xdim;x++){
	 			  data.dat.set(currentFrame,y,x,savebuffer[y*data.xdim+x]);
	 		  }
	 	  }

	   currentFrame+=1;
	   if (currentFrame>=data.zdim) {currentFrame=0; currentBuffer+=1;}
    if (savebuffer[0]==-1) return -1;    return 1;
}

public void updateDisplay(){
	 if ((data.vw!=null)&&(data.vw.jp.rois!=null)) data.setupProcessRois(data.zdim);
	 if (data.vw!=null){
	 			    if (currentFrame>0)
	 			     data.vw.JumpToFrame(currentFrame-1);
	 			     else
	 			     data.vw.JumpToFrame(data.zdim-1);
	 			    if (data.vw.jp.rois!=null){
	 					if (currentFrame>0) data.processRois(currentFrame,currentFrame-1);
	 					else data.processRois(0,data.zdim);
	 					data.showRois(false);
	 			      }
	 }
}

 public void storeContinuousChunk(String configfile,  int zdim){
	  if (focuson) StopFocus();
	  controller.StartCamera(cameranumber,configfile);
	  savebuffer=new int[zdim*data.xdim*data.ydim];
	  boolean keepimaging=true;
	  int z=0;
	  int numberofframes=0;
	  if ((data.vw!=null)&&(data.vw.jp.rois!=null)) data.setupProcessRois(data.zdim);
	  while(keepimaging){
	   numberofframes=controller.GetDataChunk(cameranumber,savebuffer,zdim,data.xdim,data.ydim);
	   if (numberofframes!=zdim) System.out.println("java warning: number of frames available = "+numberofframes);
       for (int zi=z;zi<z+numberofframes;zi++){
	    for (int y=0;y<data.ydim;y++){
		  for (int x=0;x<data.xdim;x++){
			  data.dat.set(zi%data.zdim,y,x,savebuffer[(zi-z)*(data.xdim*data.ydim)+y*data.xdim+x]);
		  }
	     }
	   }
	     z+=numberofframes;
		if (z>=data.zdim) z=z%data.zdim;
        if (data.vw!=null){
			    data.vw.JumpToFrame(z);
			    if (data.vw.jp.rois!=null){
					if (z>0) data.processRois(z,z-1);
					else data.processRois(0,data.zdim);
					data.showRois(false);
			      }
			    }
	   try {
		   Thread.sleep(20);
		   }catch (InterruptedException e){
            status="camera "+cameranumber+" continuous save mode interrupted (sleep)";
			System.out.println(status);
	         keepimaging=false;
	       }
		if (Thread.interrupted()){
			status="camera "+cameranumber+" continuous save mode interrupted";
			System.out.println(status);
			keepimaging=false;
	      }

	  }
   System.out.println("before "+cameranumber+" StopCamera");
   controller.StopCamera(cameranumber);
   System.out.println("before "+cameranumber+" data.reorder");
   data.reorder(z-1);

  }




  public int OpenImageFile(String filename){if (!focuson) return super.OpenImageFile(filename);  else return 0;}
  public  int CloseImageFile(int instance){if (!focuson) return super.CloseImageFile(instance); else return 0;}
  public int FilterOperation(int OperationCode, int startx, int endx, int instance){return 0;}
  public String ReturnSupportedFilters(){return "";}
  public int ReturnFrameNumber(){if (!focuson) return super.ReturnFrameNumber(); else return 0;}
  public int JumpToFrame(int framenum, int instance){if (!focuson) return super.JumpToFrame(framenum,instance); else return 0;}
   public  int ReturnXYBandsFrames(int[] dat, int instance){
	   if (!focuson) {return super.ReturnXYBandsFrames(dat,instance);}
	   else
	   {
	   dat[0]=xdim;
	   dat[1]=ydim;
	   dat[2]=1;
	   dat[3]=1;
	   return 1;
	   }
   }
   public void show(GView gv, String title){
   	vw=gv.openImageFile(this,title+" focus "+cameranumber);
  }



 }