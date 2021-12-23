package gvdecoder;
 import java.io.*;
 import java.util.*;
 import ij.process.FloatProcessor;
 import ij.ImagePlus;
 import ij.gui.ImageWindow;
 import ij.gui.StackWindow;
/**
  See: ImageDecoder.java (an interface).
  This is a convenience class if you are too lazy to write an efficient
  SumROIs function for a given decoder.
  Extending classes must override all other methods to use.
 **/
public class ImageJ_8bitAdapter implements ImageDecoder{

   Object[] pixelarray;
   int FrameNumber=0;
   int myxdim=128;
   int myydim=128;
   int mytotalframes=1000;

   public ImageJ_8bitAdapter(int x, int y, int frames, Object[] p){
   myxdim=x;
   myydim=y;
   mytotalframes=frames;
   pixelarray=p;
   }

   public  int UpdateImageArray(int[] arr,int xdim,int ydim, int instance){
	   for (int y=0;y<ydim;y++){
		   for (int x=0;x<xdim;x++){
			 arr[y*xdim+x]=(int)(((byte[])pixelarray[FrameNumber])[y*xdim+x]&0xff);
		   }
	   }
	   FrameNumber+=1;
	 return FrameNumber;
   }
   public  int OpenImageFile(String filename){ return 1;}
   public  int CloseImageFile(int instance){return 1;}
   public int FilterOperation(int OperationCode, int startx, int endx, int instance){return 0;}
   public String ReturnSupportedFilters(){return "";}
   public int ReturnFrameNumber(){return FrameNumber;}
   public int JumpToFrame(int framenum, int instance){ FrameNumber=framenum; return FrameNumber;}
   public int ReturnXYBandsFrames(int[] dat, int instance){
	 dat[0]=myxdim;
	 dat[1]=myydim;
	 dat[2]=1;
	 dat[3]=mytotalframes;
	 return 1;
   }

   public int SumROIs(int[][] rois, String outfile, int startframe, int endframe,int instance){
     System.out.println("in adapter SumROIs start="+startframe+" end="+endframe);
     //determine x,y dimensions
     int[] dims=new int[4];
     ReturnXYBandsFrames(dims, 0);
     int framewidth=dims[0];
     int frameheight=dims[1];
     int numframes=dims[3];
     System.out.println("debug width ="+framewidth+" height ="+frameheight+" num frames ="+numframes);
     //bounds check
     if (endframe<0) endframe=numframes; /*a shortcut for scanning whole record*/
	 if (endframe>numframes) endframe=numframes;
	 if (startframe<0) startframe=0;
     if (startframe>numframes) startframe=0;

     //create an array to hold the sums from the rois.
     int[][] sum=new int[endframe-startframe][rois.length];

     //create an array to read the data into.
     int[] tmparray=new int[framewidth*frameheight];

     for (int k=startframe;k<endframe;k++){//for each frame
     JumpToFrame(k,0); //goto frame
     UpdateImageArray(tmparray,framewidth,frameheight,0); //load image
     for (int i=0;i<rois.length;i++){ //for each roi

       for (int j=0;j<rois[i].length;j++){ //go to each element in the roi
         sum[k-startframe][i]+=tmparray[rois[i][j]];

       }//j
       //System.out.println("sim for frame "+k+" = "+sum[k-startframe][i]);
      }//i
     }//k

    //generate output
    try{
    PrintWriter file=new PrintWriter(new FileWriter(outfile),true);
    System.out.println("printing roi file end "+endframe+" start "+startframe);
    for (int j=startframe;j<endframe;j++){
	 // System.out.println("debug frame="+j);
	 file.print((j)+" ");
	// System.out.print("debug "+(j)+" ");
	 for (int i=0;i<rois.length;i++) {
		 //System.out.println("debug rois "+i+" out of "+rois.length);
		 file.print(sum[(j-startframe)][i]+" ");
		 //System.out.print("debug "+sum[(j-startframe)][i]+" ");
		 }
	 file.print("\n");
	 //System.out.print("\n");
	 //System.out.println("debug frame="+j);
	 }
    file.close();
    System.out.println("debug closed file");
    }catch(IOException e){System.out.println("error opening file for rois...");}
     catch(Exception e){System.out.println("Some other error in sumROis");e.printStackTrace();}
    return 1;
   }

}