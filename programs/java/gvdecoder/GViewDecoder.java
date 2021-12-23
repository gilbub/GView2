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
*/






public class OMPRODecoder extends ImageDecoderAdapter {
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

long header_size=50;

int verMajor;
int verMinor;
int sTimeMonth;
int sTimeDate;
int sTimeYear;
int sTimeHour;
int sTimeMin;
int sTimeSec;
int sDataType;
int sDataByte;
int sNumChns;
int sChnStcVer;
int iNumFrames;
int sAcqDevice;
int sChnInt;
float fScanInt;
float fCamp;
float fAmp;
int sIsAnalyzed;
int sAnalType;
int iCommentSize;
char[] rest=new char[256];





public OMPRODecoder(){
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
      Picfile=new RandomAccessFile(filename,"r");



  verMajor=getShort(Picfile);//1
  verMinor=getShort(Picfile);
  sTimeMonth=getShort(Picfile);
  sTimeDate=getShort(Picfile);
  sTimeYear=getShort(Picfile);
  sTimeHour=getShort(Picfile);
  sTimeMin=getShort(Picfile);
  sTimeSec=getShort(Picfile);
  sDataType=getShort(Picfile);
  sDataByte=getShort(Picfile);
  sNumChns=getShort(Picfile);
  sChnStcVer=getShort(Picfile);
  iNumFrames=getInt(Picfile);
  sAcqDevice=getShort(Picfile);
  sChnInt=getShort(Picfile);
  fScanInt=getFloat(Picfile);
  fCamp=getFloat(Picfile);
  fAmp=getFloat(Picfile);
  sIsAnalyzed=getShort(Picfile);
  sAnalType=getShort(Picfile);
  iCommentSize=getShort(Picfile);
  header_size=Picfile.getFilePointer();
  System.out.println("header = "+header_size+ " total = "+Picfile.length());

  System.out.println("verMajor="+verMajor+
  					"\n verMinor="+verMinor+
   					"\n sTimeMonth="+sTimeMonth+
   					"\n sTimeDate="+sTimeDate+
   					"\n sTimeYear="+sTimeYear+
   					"\n sTimeHour="+sTimeHour+
   					"\n sTimeSec="+sTimeSec+
   					"\n sDataType="+sDataType+
   					"\n sDataByte="+sDataByte+
   					"\n sNumChns="+sNumChns+
   					"\n ChnStcVer="+sChnStcVer+
   					"\n iNumFrames="+iNumFrames+
   					"\n sAcqDevice="+sAcqDevice+
   					"\n sChnInt="+sChnInt +
   					"\n fScanInt"+fScanInt+
   					"\n sCamp="+fCamp+
   					"\n sAmp="+fAmp+
   					"\n sIsAnalyzed="+sIsAnalyzed+
   					"\n sAnalType="+sAnalType+
   					"\n sCommentSize="+iCommentSize
   					);

  // for (int jj=0;jj<iCommentSize;jj++){
	//   System.out.print(rest[jj]);
   //}

  }
  catch(EOFException e){System.out.println("eof: number of frames read is "+j);}
  catch(IOException e){System.out.println("error reading the file, please check..."); e.printStackTrace();}
   return 1;

  }


public void test(){
	try{
	for (int i=1;i<50;i++){
	Picfile.seek (header_size+256+((512*2)*i));
	System.out.println(getOMPROShort(Picfile)+" "+getOMPROShort(Picfile));
	}
	}catch(Exception e){e.printStackTrace();}
	}
   public int FilterOperation(int OperationCode){return -1;}
   public String ReturnSupportedFilters(){return("no filters supported");}
   public int ReturnFrameNumber(){return FrameNumber;}
   public int JumpToFrame(int framenum, int instance){
	   //seek to the right location in the file...

	   try {
	   Picfile.seek((framenum*512*2)+header_size+256);
       }catch(IOException e){System.out.println("error jumping in PicDecoder");}
	   return FrameNumber;
	   }


public int ReturnXYBandsFrames(int[] arr, int instance){
	//String fileName=imagepath+File.separator+(String)images.get(FrameNumber);

	//int frames=images.size();
	arr[0]=16;
	arr[1]=32;
	arr[2]=1;
	arr[3]=iNumFrames;
	return 1;
}

public int UpdateImageArray(int[] arr, int xdim, int ydim, int instance){

    int val=0;
    try{


	 for (int i=0;i<xdim*ydim;i++){
		       val=getOMPROShort(Picfile);
		    //   arr[(int)(i/2)]=val;
		    //   val=getOMPROShort(Picfile);
		       arr[(int)(i)]=val;
		       }
    FrameNumber++;

    }  catch(IOException e){
	System.out.println("error reading into filebuffer in PicDecoder");
	}

return 1;
}

/*
public int UpdateImageArray(int[] arr, int xdim, int ydim, int instance){

    int val=0;
    try{


	 for (int i=0;i<xdim*ydim;i+=2){
		       val=getOMPROShort(Picfile);
		       arr[(int)(i/2)]=val;
		       val=getOMPROShort(Picfile);
		       arr[(int)(i/2)+256]=val;
		       }
    FrameNumber++;

    }  catch(IOException e){
	System.out.println("error reading into filebuffer in PicDecoder");
	}

return 1;
}
*/

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
OMPRODecoder pd=new OMPRODecoder();
pd.OpenImageFile(arg[0]);
pd.test();



}

}


