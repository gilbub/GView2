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

import java.nio.*;

import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

import gvdecoder.log.*;
import gvdecoder.utilities.*;




public class StanfordDecoder extends ImageDecoderAdapter {
RandomAccessFile Picfile;
public int FRAMESTEP=1;
int FrameNumber=0;
ArrayList images;
String filename;
String imagepath="";
long frameposition=0;
int orientation=2;
byte[] internalframe=new byte[528];
public static String navfilename;

public int width;
public int height;
public int numframes;
public  int pixelsize;

public boolean baselinesubtract=false;
public int firstindex=0;
public int lastindex=2028;

public  long header_size=50;

public int verMajor;
public int verMinor;
public int sTimeMonth;
public int sTimeDate;
public int sTimeYear;
public int sTimeHour;
public int sTimeMin;
public int sTimeSec;
public int sDataType;
public int sDataByte;
public int sNumChns;
public int sChnStcVer;
public int iNumFrames;
public int sAcqDevice;
public int sChnInt;
public float fScanInt;
public float fCamp;
public float fAmp;
public int sIsAnalyzed;
public int sAnalType;
public int iCommentSize;
public char[] rest=new char[256];

public MappedByteBuffer buffer;
public ShortBuffer shortbuffer;
public short[] singleframe;

public boolean SHOW_V_ONLY=false;
public boolean PAD=false;

public boolean SHOW_AS_FRAME=true;

public StanfordDecoder(){
 FrameNumber=0;
 this.filename=filename;
}

 int blocknumber=-1;
 public int JumpToFrame(int framenum, int instance){
	   //seek to the right location in the file...
       FrameNumber=framenum;

	   if (SHOW_AS_FRAME){
		 shortbuffer.position(framenum*500*190);

	   }else{

	   int needblock=(int)((double)FrameNumber/190.0);
	   if (needblock!=blocknumber){
		   shortbuffer.position(needblock*(500*190));
		   shortbuffer.get(singleframe,0, singleframe.length);
           blocknumber=needblock;
	   }
      }



	   return FrameNumber;
	   }

public int ReturnXYBandsFrames(int[] arr, int instance){
	//String fileName=imagepath+File.separator+(String)images.get(FrameNumber);

	//int frames=images.size();
if (SHOW_AS_FRAME){
	arr[0]=190;
	arr[1]=500;
	arr[2]=1;
	arr[3]=(int)(NumberOfFrames);
 }else{
	arr[0]=500;
	arr[1]=1;
	arr[2]=1;
	arr[3]=(int)(NumberOfFrames);


 }
	return 1;
}


public int CloseImageFile(int instance){return -1;}


public int NumberOfFrames=2028;

//public int FilterOperation(int a, int b, int c, int d){ return 0;}

public int OpenImageFile(String filename){
 int j=0;
 //open the file...
 try{

      Picfile=new RandomAccessFile(filename,"r");
      FileChannel fc = Picfile.getChannel();
      int size=(int)(fc.size()/16);
      buffer = fc.map (FileChannel.MapMode.READ_ONLY, 0, fc.size());
      buffer.order(ByteOrder.LITTLE_ENDIAN);
      if (SHOW_AS_FRAME){
	  NumberOfFrames=(int)((double)size/(500*190));
	  }else{
      NumberOfFrames=(int)((double)size/500.0);
      }
      singleframe=new short[190*500];
      shortbuffer=buffer.asShortBuffer();

      return 0;

  }
  catch(EOFException e){System.out.println("eof: number of frames read is "+j);}
  catch(IOException e){System.out.println("error reading the file, please check..."); e.printStackTrace();}
   return 1;

  }


public void stich(){
	//keep header of the first one, load in successive frames

}


public int UpdateImageArray(int[] arr, int xdim, int ydim, int instance){
try{
if (SHOW_AS_FRAME){
     shortbuffer.get(singleframe,0,singleframe.length);
     for (int k=0;k<arr.length;k++) arr[k]=singleframe[k];
     FrameNumber++;
}
else{
for (int j=0;j<arr.length;j++) arr[j]=0;
    //frame in singleframe is equal to framenumber % 190
    int index=FrameNumber%190;

	 for (int j=0;j<500;j++)arr[j]=(int)singleframe[j*190+index];

  	FrameNumber++;
  	JumpToFrame(FrameNumber,0);
}
}  catch(Exception e){
	System.out.println("error reading into filebuffer in Hamamatsu Decoder ");
	e.printStackTrace();
}
return 1;
}


int getByte(RandomAccessFile f) throws IOException {
		int b = f.read();
		if (b ==-1) throw new IOException("unexpected EOF");
		return b;
	}

int getShort(RandomAccessFile f) throws IOException {
	int b0 = getByte(f);
	int b1 = getByte(f);
	return ((b1 << 8) + b0);
}

int getHamamatsuShort(RandomAccessFile f) throws IOException{
	int tmp=getShort(f);
	if (tmp>=32768) tmp-=65536;
	return tmp;


}

float getFloat(RandomAccessFile f) throws IOException {
	 int accum=0;
	 for (int shiftBy=0;shiftBy<32;shiftBy+=8){
		 accum|=(getByte(f) & 0xff)<<shiftBy;
	 }
	 return Float.intBitsToFloat(accum);


}


int getInt(RandomAccessFile f) throws IOException{

	 int accum=0;
		 for (int shiftBy=0;shiftBy<32;shiftBy+=8){
			 accum|=(getByte(f) & 0xff)<<shiftBy;
		 }
		 return accum;

}


short getShortInt(RandomAccessFile f) throws IOException{

	 short accum=0;
		 for (int shiftBy=0;shiftBy<16;shiftBy+=8){
			 accum|=(getByte(f) & 0xff)<<shiftBy;
		 }
		 return accum;

}

char getChar(RandomAccessFile f) throws IOException{
	int low=getByte(f)&0xff;
	int high=getByte(f);
	return (char)(high<<8|low);

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

     //this special case checks for unit rois and treats them differently
     boolean unitroi=true;
     for (int i=0;i<rois.length;i++){
		 if (rois[i].length!=1) unitroi=false;
		 }



	 if (unitroi){
		 System.out.println("USING UNIT ROI Hamamatsu METHOD");
		 for (int k=startframe;k<endframe;k++){
		 for (int i=0;i<rois.length;i++){
		 for (int f=0;f<FRAMESTEP;f++){
		  sum[k-startframe][i]=(int)buffer.get(rois[i][0]+(int)((k*FRAMESTEP+f)*sNumChns));//getHamamatsuShort(Picfile);
	    }
		}
   		}
	 }else{


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
	}//else
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
System.out.println("hamamatsu reader test");
HamamatsuDecoder pd=new HamamatsuDecoder();
pd.OpenImageFile(arg[0]);
pd.test();



}

}


