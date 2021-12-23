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






public class RedShirtDecoder extends ImageDecoderAdapter {
RandomAccessFile Picfile;
int FrameNumber=0;
String filename;





public static String navfilename;

int width;
int height;
int numframes;
int pixelsize;

long header_size=5120;

public int xdim;
public int ydim;
public int zdim;
public int iNumFrames;
public int remainder;
public int[] internal;

public int FRAMESTEP=1;


public int[] xybfarr;
public int[] matlabReturnXYBandsFrames() {
    if (xybfarr == null) {
	xybfarr = new int[4];
    }
    ReturnXYBandsFrames(xybfarr, 0);
    return xybfarr;
}


/** routines that can be called directly from matlab **/
public int[] framearray;
public int[] matlabUpdateImageArray() {
    if (xybfarr == null) {
	matlabReturnXYBandsFrames();;
    }

    if (framearray == null) {
	framearray = new int[80*80];
    }

    UpdateImageArray(framearray, xybfarr[0], xybfarr[1], 0);
    return framearray;
}

public RedShirtDecoder(){

}

public RedShirtDecoder(int averageover){

  FRAMESTEP=averageover;

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


  Picfile.seek(4*2);
  iNumFrames=getInt(Picfile);
  zdim=(int)(((double)iNumFrames)/FRAMESTEP);
  remainder=iNumFrames-zdim*FRAMESTEP;
  Picfile.seek(384*2);
  xdim=getRedShirtShort(Picfile);
  ydim=getRedShirtShort(Picfile);

  System.out.println("internal frames="+iNumFrames+" zdim="+zdim+" xdim="+xdim+" ydim="+ydim);

   internal=new int[xdim*ydim*zdim];
   convert_nio();

  }
  catch(EOFException e){System.out.println("eof: number of frames read is "+j);}
  catch(IOException e){System.out.println("error reading the file, please check..."); e.printStackTrace();}
   return 1;

  }
/*
public void convert(){
  try{
  Picfile.seek(5120);
  int cnt=0;
  for (int x=0;x<xdim;x++){
	for (int y=0;y<ydim;y++){
	for (int n=0;n<iNumFrames;n++){
		internal[n*(xdim*ydim)+(x*xdim+y)]=(int)getRedShirtShort(Picfile);
	}
  }
 }
}
 catch(Exception e){e.printStackTrace();}

}
*/

public void convert_nio(){
	 int sum=0;
     try{
	  FileChannel fc = Picfile.getChannel();
	  MappedByteBuffer buffer = fc.map (FileChannel.MapMode.READ_ONLY, 5120, fc.size()-5120);
	  buffer.order(ByteOrder.LITTLE_ENDIAN);
	  for (int x=0;x<xdim;x++){
	  	for (int y=0;y<ydim;y++){
	  	 for (int z=0;z<zdim;z++){
	      sum=0;
	  	  for (int f=0;f<FRAMESTEP;f++){
	  	   sum+=(int)buffer.getShort();
	  	   }
	  	  internal[z*(xdim*ydim)+(x*xdim+y)]=sum;

	  	}
	  	for (int k=0;k<remainder;k++){buffer.getShort();}

	    }
      }
  }catch(Exception e){e.printStackTrace();}

}


public void write_matrix(String filename){
	 try{
		 RandomAccessFile outfile=new RandomAccessFile(filename,"rw");

		 double[] tmp=new double[xdim*ydim];
		 byte[] bytes=new byte[xdim*ydim*8];

		 FileChannel fc=outfile.getChannel();
		 MappedByteBuffer buffer = fc.map (FileChannel.MapMode.READ_WRITE, 0, zdim*xdim*ydim*8+4*4);
	     buffer.putInt(1); //version
		 buffer.putInt(zdim);
		 buffer.putInt(ydim);
		 buffer.putInt(xdim);


		  for (int z=0;z<zdim;z++){
			 for (int y=0;y<ydim;y++){
				 for (int x=0;x<xdim;x++){
					 //outfile.writeDouble(dat.get(z,x,y));
					 buffer.putShort((short)internal[z*(xdim*ydim)+(x*xdim+y)]);
		//		 	 for (int k=0;k<8;k++) bytes[(8*(x*ydim+y))+k]=a[k];
				 }
			 }


		 }

		outfile.close();
		}catch(IOException e){e.printStackTrace();}
	}




public void test(){
	try{
	for (int i=1;i<50;i++){
	Picfile.seek (header_size+((512*2)*i));
	System.out.println(getRedShirtShort(Picfile)+" "+getRedShirtShort(Picfile));
	}
	}catch(Exception e){e.printStackTrace();}
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

	//int frames=images.size();
	arr[0]=xdim;
	arr[1]=ydim;
	arr[2]=1;
	arr[3]=zdim;
	return 1;
}

public int UpdateImageArray(int[] arr, int xdim, int ydim, int instance){
    /*to compensate for trace by trace order
    frame[xdim][ydim]=
    */
    int val=0;
    try{


	 for (int i=0;i<xdim*ydim;i++){
		      arr[i]=internal[FrameNumber*xdim*ydim+i];
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

int getRedShirtShort(RandomAccessFile f) throws IOException{
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
System.out.println("redshirt reader test");
RedShirtDecoder pd=new RedShirtDecoder();
pd.OpenImageFile(arg[0]);
pd.write_matrix(arg[1]);
//pd.test();



}

}


