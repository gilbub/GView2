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
import com.sun.media.jai.codec.*;

import gvdecoder.log.*;
import gvdecoder.utilities.*;

/*
Header Information:

The header of Bio-Rad .PIC files is fixed in size, and is 76 bytes.

------------------------------------------------------------------------------
'C' Definition              byte    size    Information
                                   (bytes)
------------------------------------------------------------------------------
int nx, ny;                 0       2*2     image width and height in pixels
int npic;                   4       2       number of images in file
int ramp1_min, ramp1_max;   6       2*2     LUT1 ramp min. and max.
NOTE *notes;                10      4       no notes=0; has notes=non zero
BOOL byte_format;           14      2       bytes=TRUE(1); words=FALSE(0)
int n;                      16      2       image number within file
char name[32];              18      32      file name
int merged;                 50      2       merged format
unsigned color1;            52      2       LUT1 color status
unsigned file_id;           54      2       valid .PIC file=12345
int ramp2_min, ramp2_max;   56      2*2     LUT2 ramp min. and max.
unsigned color2;            60      2       LUT2 color status
BOOL edited;                62      2       image has been edited=TRUE(1)
int _lens;                  64      2       Integer part of lens magnification
float mag_factor;           66      4       4 byte real mag. factor (old ver.)
unsigned dummy[3];          70      6       NOT USED (old ver.=real lens mag.)
------------------------------------------------------------------------------
*/



public class BioRadPicDecoder extends ImageDecoderAdapter {
FileSeekableStream Picfile;
int FrameNumber=0;
ArrayList images;
String filename;
String imagepath="";
long frameposition=0;
int orientation=2;
byte[] internalframe=new byte[528];
public static String navfilename;

int width;
int height;
int numframes;
int pixelsize;


public BioRadPicDecoder(){
 FrameNumber=0;
 this.filename=filename;
}

public int FilterOperation(int OperationCode, int startx, int endx, int instance){return 0;}

public int CloseImageFile(int instance){return -1;}

//public int FilterOperation(int a, int b, int c, int d){ return 0;}

public int OpenImageFile(String filename){
 int j=0;
 //open the file...
 try{

      navfilename=FileNameManager.getInstance().FindNavFileName(filename,"");
      File tmp=new File(navfilename);
      Picfile=new FileSeekableStream(new RandomAccessFile(filename,"r"));

  //read width and height
  width=getShort(Picfile);
  height=getShort(Picfile);
  //read numframes
  numframes=getShort(Picfile);
  Picfile.seek(14);
  pixelsize=getShort(Picfile);
  if (pixelsize==0) pixelsize=8; else pixelsize=16;
  /*change*/
  pixelsize=8;
  System.out.println("opened file, with width="+width+" height="+height+" frames="+numframes+" size="+pixelsize);

  Picfile.seek(76);

  }
  catch(EOFException e){System.out.println("eof: number of frames read is "+j);}
  catch(IOException e){System.out.println("error reading the file, please check..."); e.printStackTrace();}
   return 1;

  }

   public int FilterOperation(int OperationCode){return -1;}
   public String ReturnSupportedFilters(){return("no filters supported");}
   public int ReturnFrameNumber(){return FrameNumber;}
   public int JumpToFrame(int framenum, int instance){
	   //seek to the right location in the file...

	   try {
	   Picfile.seek(framenum*(width*height)*(pixelsize/4)+76);
       }catch(IOException e){System.out.println("error jumping in PicDecoder");}
	   return FrameNumber;
	   }


public int ReturnXYBandsFrames(int[] arr, int instance){
	//String fileName=imagepath+File.separator+(String)images.get(FrameNumber);

	//int frames=images.size();
	arr[0]=width;
	arr[1]=height;
	arr[2]=1;
	arr[3]=numframes;
	return 1;
}

public int UpdateImageArray(int[] arr, int xdim, int ydim, int instance){

    int val=0;
    try{
	 for (int i=0;i<xdim*ydim;i++){
		       val=(int)getByte(Picfile);
		       arr[i]=val;
		       }
    FrameNumber++;

    }  catch(IOException e){
	System.out.println("error reading into filebuffer in PicDecoder");
	}

return 1;
}

int getByte(FileSeekableStream f) throws IOException {
		int b = f.read();
		if (b ==-1) throw new IOException("unexpected EOF");
		return b;
	}

int getShort(FileSeekableStream f) throws IOException {
	int b0 = getByte(f);
	int b1 = getByte(f);
	return ((b1 << 8) + b0);
}


 public int SumROIs(int[][] rois, String outfile, int startframe, int endframe,int instance){
	 /*in this case we want to divide each value by the average value for the whole frame*/




     System.out.println("in adapter SumROIs start="+startframe+" end="+endframe);
     //determine x,y dimensions
     double averagepixel=0.0;
     int[] dims=new int[4];
     ReturnXYBandsFrames(dims,0);
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


public static void main(String[] arg){
System.out.println("biorad reader test");
BioRadPicDecoder pd=new BioRadPicDecoder();
pd.OpenImageFile(arg[0]);



}

}


