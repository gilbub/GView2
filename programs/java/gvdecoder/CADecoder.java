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
/*
 How to deal with 2 channel data?
 Approach, dump all into one large field separated by several blank lines.
 	add aditional methods for comparing the two datasets
 	call ReturnXYBandsFrames. Then
 				if 2 channel, then its known that it will have stack, 2 long
 	for this week, though I need several different speedups.
 	1) read a single channel quickly without loading the wholde frame..
 	   use java nio to read in the whole frame.
 	   i think that nio also has a way to skip bytes
 	2) in filteroperation

*/


/*


Header Information:

The header OMPRO files (from BumRac Choi)

struct GenDataHdr {
    int16_t verMajor;
    int16_t verMinor;
    int16_t sTimeMonth;
    int16_t sTimeDate;
    int16_t sTimeYear;
    int16_t sTimeHour;
    int16_t sTimeMin;
    int16_t sTimeSec;
    int16_t sDataType;
    int16_t sDataByte;
    int16_t sNumChns;
    int16_t sChnStcVer;
    int32_t iNumFrames;
    int16_t sAcqDevice;
    int16_t sChnInt;
    float   fScanInt;
    float   fCaAmp;
    float   fAmp;
    int16_t sIsAnalyzed;
    int16_t sAnalType;
    int32_t iCommentSize;
    char    rest[256];
}OMPRO[MAXOMPRO];;

two frames is 16x16+16=272
*/






public class CADecoder extends ImageDecoderAdapter {
RandomAccessFile Picfile;

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

int xdim;int ydim;

long header_size=50;

public CADecoder(){
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
    Picfile=new RandomAccessFile(filename,"r");
    xdim=Integer.parseInt(Picfile.readLine());
    ydim=Integer.parseInt(Picfile.readLine());
    header_size=(int)Picfile.getFilePointer();
    numframes=(int)((Picfile.length()-header_size)/(xdim*ydim));
    System.out.println("xdim="+xdim+" ydim="+ydim+" header="+header_size+" numframes="+numframes);
  }

  catch(EOFException e){System.out.println("eof: number of frames read is "+j);}
  catch(IOException e){System.out.println("error reading the file, please check..."); e.printStackTrace();}
   return 1;

}


public void test(){
	try{
	for (int i=1;i<100;i++){
	Picfile.seek (header_size+((520*2)*i));
	System.out.println(getOMPROShort(Picfile)+" "+getOMPROShort(Picfile));
	}
	}catch(Exception e){e.printStackTrace();}
	}
   public int FilterOperation(int OperationCode){return -1;}
   public String ReturnSupportedFilters(){return("no filters supported");}
   public int ReturnFrameNumber(){return FrameNumber;}
   public int JumpToFrame(int framenum, int instance){
	   //seek to the right location in the file...
       FrameNumber=framenum;
	   //try {
	   //Picfile.seek(((FRAMESTEP*framenum)*sNumChns*2)+header_size+256);
       //}catch(IOException e){System.out.println("error jumping in PicDecoder");}
	   return FrameNumber;
	   }


public int ReturnXYBandsFrames(int[] arr, int instance){
	//String fileName=imagepath+File.separator+(String)images.get(FrameNumber);

	arr[0]=xdim;
	arr[1]=ydim;
	arr[2]=1;
	arr[3]=numframes;
	return 1;

}


public int UpdateImageArray(int[] arr, int xdim, int ydim, int instance){

    int val=0;
    for (int j=0;j<arr.length;j++)arr[j]=0;


	 try{

	 for (int i=0;i<xdim*ydim;i++){
		       Picfile.seek(FrameNumber*xdim*ydim+(i)+header_size);
		       val=(int)Picfile.readChar();//getOMPROShort(Picfile);
		    //   arr[(int)(i/2)]=val;
		    //   val=getOMPROShort(Picfile);
		       arr[i]=val;
		       }


     FrameNumber++;
    }  catch(Exception e){
	System.out.println("error reading into filebuffer in PicDecoder");

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

int getOMPROShort(RandomAccessFile f) throws IOException{
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

/*
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
     if (endframe<0) endframe=numframes; //a shortcut for scanning whole record
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
		 System.out.println("USING UNIT ROI OMPRO METHOD");
		 for (int k=startframe;k<endframe;k++){
		 for (int i=0;i<rois.length;i++){
		 for (int f=0;f<FRAMESTEP;f++){
		  sum[k-startframe][i]=(int)buffer.getShort((rois[i][0]*2)+(int)(header_size+256+(k*FRAMESTEP+f)*sNumChns*2));//getOMPROShort(Picfile);
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
*/
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
System.out.println("biorad reader test");
CADecoder pd=new CADecoder();
pd.OpenImageFile(arg[0]);
//pd.test();



}

}


