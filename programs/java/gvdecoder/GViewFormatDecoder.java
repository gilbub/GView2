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


FileChannel channel;
ByteBuffer buf;
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
 if (version==2) framesize=xdim*ydim;

  System.out.println("frames="+iNumFrames+" xdim="+xdim+" ydim="+ydim+" version="+version);
  channel = infile.getChannel();
  System.gc();
  System.runFinalization();
  //buf = channel.map(FileChannel.MapMode.READ_ONLY, 0, (int)channel.size());




  }
  catch(EOFException e){System.out.println("eof: number of frames read is "+j);}
  catch(IOException e){System.out.println("error reading the file, please check..."); e.printStackTrace();}
   return 1;

  }


/*
public static void write(ImageDecoder in, String out){
	static int [] specs=new int[4];

	in.ReturnXYBandsFrames(specs,0);


	static int in_x=specs[0];
	static int in_y=specs[1];
	static int bands=specs[2];
	static int frames=specs[3];

	try{
			 static RandomAccessFile outfile=new RandomAccessFile(out,"rw");

			 static double[] tmp=new double[in_x*in_y];
			 static byte[] bytes=new byte[in_x*in_y*8];
	 		 static int[] arr=new int[in_x*in_y];

			 FileChannel fc=outfile.getChannel();
			 MappedByteBuffer buffer = fc.map (FileChannel.MapMode.READ_WRITE, 0, iNumFrames*xdim*ydim*8+4*4);
		     buffer.putInt(1); //version
			 buffer.putInt(iNumFrames);
			 buffer.putInt(ydim);
		     buffer.putInt(xdim);
		     for (int z=0;z<frames;z++){
				in.jumpToFrame(z,0);
				in.updateImageArray(arr,in_x,in_y,0);
				 for (int y=0;y<ydim;y++){
					 for (int x=0;x<xdim;x++){
						 buffer.putShort((short)internal[z*(xdim*ydim)+(x*xdim+y)]);

					 }
				 }

	}
    specs=null;
    arr=null;
    tmp=null;
    bytes=null;
	outfile.close();
}catch(Exception e){e.printStackTrace();}
}
*/
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

	if (version==0){
		buf = channel.map(FileChannel.MapMode.READ_ONLY, FrameNumber*xdim*ydim*8+16, (int)xdim*ydim*8);

		for (int i=0;i<xdim*ydim;i++){
			int index1=i*8;
			arr[i]=(int)buf.getDouble(index1);
		}
	}
	else
	if (version==1){

		buf = channel.map(FileChannel.MapMode.READ_ONLY, FrameNumber*xdim*ydim*2+16, (int)xdim*ydim*2);
		for (int i=0;i<xdim*ydim;i++){
					int index1=i*2;
					arr[i]=(int)buf.getShort(index1);
		}
	}
	else
	if (version==2){
			buf = channel.map(FileChannel.MapMode.READ_ONLY, FrameNumber*xdim*ydim*1+16, (int)xdim*ydim*1);
				for (int i=0;i<xdim*ydim;i++){
							int index1=i*1;
							arr[i]=(int)(buf.get(index1)& 0xFF);
		}

	}



//	 for (int i=0;i<xdim*ydim;i++){
//		     if (version==0) arr[i]=(int)infile.readDouble();
//		     if (version==1) arr[i]=(int)infile.readShort();
//		       }
    FrameNumber++;

    }  catch(Exception e){
	System.out.println("error reading into filebuffer in PicDecoder");
	e.printStackTrace();

	}

return 1;
}






public static void main(String[] arg){
System.out.println("redshirt reader test");
GViewFormatDecoder pd=new GViewFormatDecoder();
pd.OpenImageFile(arg[0]);
pd.write_matrix(arg[1]);
//pd.test();



}

}


