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
import utilities.*;

import java.nio.*;

import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;



/*
Header Information:

The header RedShirt files (from BumRac Choi)


*/






public class GViewFormatDecoder extends ImageDecoderAdapter {
RandomAccessFile infile;
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
int framesize;

long header_size=16;

int xdim;
int ydim;
int iNumFrames;
int version=0;
int[] internal;





public GViewFormatDecoder(){
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
      infile=new RandomAccessFile(filename,"r");

 version=infile.readInt(); //version
 iNumFrames=infile.readInt();
 ydim=infile.readInt();
 xdim=infile.readInt();
 framesize=xdim*ydim*2;
 if (version==0) framesize=xdim*ydim*8;

  System.out.println("frames="+iNumFrames+" xdim="+xdim+" ydim="+ydim);



  }
  catch(EOFException e){System.out.println("eof: number of frames read is "+j);}
  catch(IOException e){System.out.println("error reading the file, please check..."); e.printStackTrace();}
   return 1;

  }

public void convert(){
  try{
  infile.seek(5120);
  int cnt=0;
  for (int x=0;x<xdim;x++){
	for (int y=0;y<ydim;y++){
	for (int n=0;n<iNumFrames;n++){
		internal[n*(xdim*ydim)+(x*xdim+y)]=(int)getRedShirtShort(infile);
	}
  }
 }
}
 catch(Exception e){e.printStackTrace();}

}


public void convert_nio(){
     try{
	  FileChannel fc = infile.getChannel();
	  MappedByteBuffer buffer = fc.map (FileChannel.MapMode.READ_ONLY, 0, fc.size());
	  buffer.order(ByteOrder.LITTLE_ENDIAN);
	  for (int x=0;x<xdim;x++){
	  	for (int y=0;y<ydim;y++){
	  	for (int n=0;n<iNumFrames;n++){
	  		//internal[n*(xdim*ydim)+(x*xdim+y)]=Unsigned.getUnsignedShort((ByteBuffer)buffer);

	  	internal[n*(xdim*ydim)+(x*xdim+y)]=(int)buffer.getShort();

	  	}
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
		 MappedByteBuffer buffer = fc.map (FileChannel.MapMode.READ_WRITE, 0, iNumFrames*xdim*ydim*8+4*4);
	     buffer.putInt(1); //version
		 buffer.putInt(iNumFrames);
		 buffer.putInt(ydim);
		 buffer.putInt(xdim);


		  for (int z=0;z<iNumFrames;z++){
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
	infile.seek (header_size+((512*2)*i));
	System.out.println(getRedShirtShort(infile)+" "+getRedShirtShort(infile));
	}
	}catch(Exception e){e.printStackTrace();}
	}

public int FilterOperation(int OperationCode){return -1;}
public String ReturnSupportedFilters(){return("no filters supported");}
public int ReturnFrameNumber(){return FrameNumber;}
public int JumpToFrame(int framenum, int instance){
 try {
	   infile.seek(((framenum)*framesize)+16);
       }catch(IOException e){System.out.println("error jumping in PicDecoder");}

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
    /*to compensate for trace by trace order
    frame[xdim][ydim]=
    */
    int val=0;
    try{


	 for (int i=0;i<xdim*ydim;i++){
		     if (version==0) arr[i]=(int)infile.readDouble();
		     if (version==1) arr[i]=(int)infile.readShort();
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
GViewFormatDecoder pd=new GViewFormatDecoder();
pd.OpenImageFile(arg[0]);
pd.write_matrix(arg[1]);
//pd.test();



}

}


