package gvdecoder;
import java.io.*;
import java.util.*;


import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.awt.geom.*;

import java.awt.image.*;
import java.awt.image.renderable.*;
import javax.media.jai.*;
import javax.media.jai.iterator.*;
import javax.media.jai.widget.*;

import java.awt.color.*;



import gvdecoder.log.*;
import gvdecoder.utilities.*;







public class CascadeDecoder extends ImageDecoderAdapter {
RandomAccessFile Picfile;
int iNumFrames=0;
int xdim=128;
int ydim=128;
int framesize=16384;
public byte[] singleframe;
public int FrameNumber=0;
public String filename="";



public CascadeDecoder(){
 FrameNumber=0;

}

public int FilterOperation(int OperationCode, int startx, int endx, int instance){return 0;}

public int CloseImageFile(int instance){return -1;}

//public int FilterOperation(int a, int b, int c, int d){ return 0;}

public int OpenImageFile(String filename){
 int j=0;
 //open the file...
 this.filename=filename;
 try{

      Picfile=new RandomAccessFile(filename,"r");
      long filesize = Picfile.length();
      iNumFrames=(int)(filesize/framesize);
      singleframe=new byte[framesize];

  }
  catch(IOException e){System.out.println("error reading the file, please check..."); e.printStackTrace();}
  return 1;

  }

public int JumpToFrame(int framenum, int instance){
  FrameNumber=framenum;
  return framenum;

}

public int ReturnXYBandsFrames(int[] arr, int instance){
	//String fileName=imagepath+File.separator+(String)images.get(FrameNumber);

	//int frames=images.size();
	arr[0]=xdim;
	arr[1]=ydim;
	arr[2]=1;
	arr[3]=iNumFrames;
	return 1;
}

public int UpdateImageArray(int[] arr, int xdim, int ydim, int instance){

    int val=0;
    try{
	Picfile.seek(FrameNumber*framesize);
    Picfile.readFully(singleframe);

	 for (int i=0;i<framesize;i++){
		       arr[i]=(int)singleframe[i];
    }
    FrameNumber++;

    }  catch(IOException e){
	System.out.println("error reading into filebuffer in PicDecoder");
	}

return 1;
}



/*
public int SumROIs(int[][] rois, String outfile, int startframe, int endframe,int instance){




     System.out.println("in adapter SumROIs start="+startframe+" end="+endframe);
     //determine x,y dimensions
     double averagepixel=0.0;
     int[] dims=new int[4];
     ReturnXYBandsFrames(dims);
     int framewidth=dims[0];
     int frameheight=dims[1];
     int numframes=dims[3];
     System.out.println("debug width ="+framewidth+" height ="+frameheight+" num frames ="+numframes);
     //bounds check
     if (endframe<0) endframe=numframes;
	 if (endframe>numframes) endframe=numframes;
	 if (startframe<0) startframe=0;
     if (startframe>numframes) startframe=0;

     //create an array to hold the sums from the rois.
     double[][] sum=new double[endframe-startframe][rois.length];

     //create an array to read the data into.
     int[] tmparray=new int[framewidth*frameheight];

     for (int k=startframe;k<endframe;k++){//for each frame
     JumpToFrame(k,0); //goto frame
     UpdateImageArray(tmparray,framewidth,frameheight,0); //load image
      //sum whole frame
      averagepixel=0.0;
      int numpixels=0;
     for (int p=0;p<width*height;p++){
		 if (tmparray[p]>0) {
		  averagepixel+=tmparray[p];
		  numpixels++;
	     }
	  }
	  averagepixel=averagepixel/((double)(numpixels));
      System.out.println("average pixel="+averagepixel);
     for (int i=0;i<rois.length;i++){ //for each roi

       for (int j=0;j<rois[i].length;j++){ //go to each element in the roi
         sum[k-startframe][i]+=(((double)tmparray[rois[i][j]])/((double)rois[i].length));

       }//j
       //System.out.println("sim for frame "+k+" = "+sum[k-startframe][i]);
      }//i
     }//k

    //generate output
    try{
    PrintWriter file=new PrintWriter(new FileWriter(outfile),true);
    System.out.println("printing roi file end "+endframe+" start "+startframe);
    file.println("# rois are average value per pixel minus average value for whole frame");
    for (int j=startframe;j<endframe;j++){
	 // System.out.println("debug frame="+j);
	 file.print((j)+" ");
	// System.out.print("debug "+(j)+" ");
	 for (int i=0;i<rois.length;i++) {
		 //System.out.println("debug rois "+i+" out of "+rois.length);
		 file.print((int)(sum[(j-startframe)][i]-averagepixel)+" "); //get three decimal places
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
*/

public static void main(String[] arg){
//System.out.println("biorad reader test");
//OMPRODecoder pd=new OMPRODecoder();/
//pd.OpenImageFile(arg[0]);
//pd.test();



}

}


