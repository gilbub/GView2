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






public class RedShirt128Decoder extends ImageDecoderAdapter {
RandomAccessFile Picfile;
int FrameNumber=0;
String filename;

Viewer2 vi=null;





public static String navfilename;

int width;
int height;
int numframes;
int pixelsize;
int dec=0;

long header_size=5120;

public int xdim;
public int ydim;
public int zdim;
public int iNumFrames;
public int remainder;
public int[] internal;
public byte[] buffer;

public String filetype;

public ArrayList images;

public String imagepath="";

public int FRAMESTEP=1;



public RedShirt128Decoder(){
	FrameNumber=0;
	this.filetype="dat";
    images=new ArrayList();
}


public int FilterOperation(int OperationCode, int startx, int endx, int instance){return 0;}

public int CloseImageFile(int instance){
	images=null;
	internal=null;
	buffer=null;
	return -1;
	}

//public int FilterOperation(int a, int b, int c, int d){ return 0;}

public int OpenImageFile(String filename){
//asumes filename is a directory that contains a series of images.
//gets the series in alphabetical order, puts each in a list


 File dir=new File(filename);
 System.out.println("the file information is "+dir+" filetype="+filetype);
 String[] tmp=null;
 if (!dir.exists()) {System.out.println("directory not found"); return 0;}
 if (!dir.isDirectory()){
	 System.out.println("the file isn't a directory");
     images.add(dir.getName());
     imagepath=dir.getParent();
     }
 else{
 if (dir.isDirectory()){
   imagepath=filename;
   tmp=dir.list();
  }
  Arrays.sort(tmp);
 for (int i=0;i<tmp.length;i++){
  if (tmp[i].endsWith(filetype) || tmp[i].endsWith(filetype.toUpperCase())) images.add(tmp[i]);
  }
}
  xdim=128;
  ydim=128;
  zdim=images.size()*100;
  buffer=new byte[(xdim*(ydim+1)*100)*2];
  width=xdim;height=ydim;
  System.out.println("Setting xdim="+xdim+" ydim="+ydim+" zdim="+zdim);
 return 1;
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
    int frames=images.size()*100;
	arr[0]=xdim;
	arr[1]=ydim;
	arr[2]=1;
	arr[3]=frames;
	return 1;

}


public int UpdateImageArray(int[] arr, int xdim, int ydim, int instance){
   String fileName=imagepath+(String)images.get((int)(FrameNumber/100));
   System.out.println("trying to open "+fileName);
   try{
   File f=new File(fileName);
   DataInputStream in = new DataInputStream(new FileInputStream(f));
   in.readFully(buffer);
   in.close();
   }catch(IOException e){e.printStackTrace();}
   int j=0;

   for (int y=0;y<128;y++){
	 for (int x=0;x<128;x+=4) arr[y*128+(int)x/4]=getvalue(buffer,FrameNumber,y,x);
	 for (int x=1;x<128;x+=4) arr[y*128+(int)((x-1)/4)+32]=getvalue(buffer,FrameNumber,y,x);//ma.dat.set(0,y,(int)((x-1)/4)+32,(double)getvalue(infile,buffer,128,128,x,y));
	 for (int x=2;x<128;x+=4) arr[y*128+(int)((x-2)/4)+64]=getvalue(buffer,FrameNumber,y,x);//ma.dat.set(0,y,(int)((x-2)/4)+64,(double)getvalue(infile,buffer,128,128,x,y));
	 for (int x=3;x<128;x+=4) arr[y*128+(int)((x-3)/4)+96]=getvalue(buffer,FrameNumber,y,x);//ma.dat.set(0,y,(int)((x-3)/4)+96,(double)getvalue(infile,buffer,128,128,x,y));
    }
   System.out.println("framenumber "+FrameNumber);
   FrameNumber+=1;
   return 1;
}


 public int getvalue(byte[] inbuffer, int frame, int y, int x){
    int loc=2*((frame%100)*(129*128)+(y*128+x));
    return (inbuffer[loc]&0xFF)|(inbuffer[loc+1]&0xFF)<<8;
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


public static void main(String[] arg){
System.out.println("redshirt reader test");
RedShirtDecoder pd=new RedShirtDecoder();
pd.OpenImageFile(arg[0]);
pd.write_matrix(arg[1]);
//pd.test();



}

}


