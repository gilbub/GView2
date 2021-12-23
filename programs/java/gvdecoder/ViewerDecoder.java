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

import java.nio.*;

import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;



/*
Header Information:

The header RedShirt files (from BumRac Choi)


*/






public class ViewerDecoder extends ImageDecoderAdapter {


Viewer2 vi=null;
Viewer2 vw;
int xdim;
int ydim;
int FrameNumber;
int xs,xe,ys,ye,bin;
int width,height;


public ViewerDecoder(Viewer2 vw, int xs, int ys, int xe, int ye, int bin){
	this.vw=vw;
	int m=1;
	System.out.println("ViewerDecoder called, with xs,ys,xe,ye = "+xs+","+ys+","+xe+","+ye+" bin="+bin);

	if ((xe-xs)%bin!=0){
		m=(int)((xe-xs)/bin);
		xe=xs+m*bin;
		if (xe>vw.X_dim){ xe=xs+(m-1)*bin;}
	}
	if ((ye-ys)%bin!=0){
		m=(int)((ye-ys)/bin);
		ye=ys+m*bin;
		if (xe>vw.Y_dim){ ye=ys+(m-1)*bin;}
	}

    xdim=(int)((xe-xs)/bin);
    ydim=(int)((ye-ys)/bin);
	this.xs=xs;
	this.ys=ys;
	this.xe=xe;
	this.ye=ye;
	this.bin=bin;
	width=xdim;
	height=ydim;
	System.out.println("ViewerDecoder initialized, with xs,ys,xe,ye = "+xs+","+ys+","+xe+","+ye+" bin="+bin+ " xdim,ydim="+xdim+","+ydim);

	}


public int FilterOperation(int OperationCode, int startx, int endx, int instance){return 0;}

public int CloseImageFile(int instance){
	this.vw=null;
	this.vi=null;
	return -1;}

//public int FilterOperation(int a, int b, int c, int d){ return 0;}

public int OpenImageFile(String filename){ return 1;
}




public int FilterOperation(int OperationCode){return -1;}
public String ReturnSupportedFilters(){return("no filters supported");}
public int ReturnFrameNumber(){return FrameNumber;}
public int JumpToFrame(int framenum, int instance){
//seek to the right location in the file...
FrameNumber=framenum;
return framenum;
}


public int ReturnXYBandsFrames(int[] arr, int instance){
	//String fileName=imagepath+File.separator+(String)images.get(FrameNumber);

	arr[0]=xdim;
	arr[1]=ydim;
	arr[2]=1;
	arr[3]=vw.MaxFrames;
	return 1;

}

boolean useViewArray=true;
public int UpdateImageArray(int[] arr, int xdim, int ydim, int instance){
   vw.JumpToFrame(FrameNumber);
   int myi=0;
   int vwi=0;
   try{
   for (int y=ys;y<ye;y+=bin){
	   for (int x=xs;x<xe;x+=bin){
		   myi= ((int)((y-ys)/bin))*xdim+((int)((x-xs)/bin));
		   int sum=0;
		   for (int yy=0;yy<bin;yy++){
			   for (int xx=0;xx<bin;xx++){
	              vwi= (y+yy)*vw.X_dim+(x+xx);
	              if (useViewArray) sum+=vw.viewArray[vwi];
	               else sum+=vw.datArray[vwi];
			   }
		   }
		   arr[myi]=sum;
	   }
}
}catch (Exception e){
	System.out.println("ViewerDecoder error: myi="+myi+" vwi="+vwi);
}


   FrameNumber+=1;
   return 1;
}



public int SumROIs(int[][] rois, String outfile, int startframe, int endframe,int instance){




     System.out.println("in Neo SumROIs start="+startframe+" end="+endframe);
     //determine x,y dimensions
     double averagepixel=0.0;
     int[] dims=new int[4];
     ReturnXYBandsFrames(dims,0);
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
     if (vi!=null) {
		if ((vi.viewerfilter!=null)&&(vi.useviewerfilter)){
		System.out.println("using viewer filter");
		vi.viewerfilter.run(vi,tmparray);
	   }
     }

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



}


