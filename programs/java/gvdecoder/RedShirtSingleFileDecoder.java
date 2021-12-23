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






public class RedShirtSingleFileDecoder extends ImageDecoderAdapter {
RandomAccessFile infile;
int FrameNumber=0;
ArrayList images;
String filename;
String imagepath="";
long frameposition=0;
int orientation=2;
byte[] internalframe=new byte[129*128*2];
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


public RedShirtSingleFileDecoder(){
 FrameNumber=0;
 this.filename=filename;
}

public int FilterOperation(int OperationCode, int startx, int endx, int instance){return 0;}

public int CloseImageFile(int instance){
	try{
	infile.close();
	}catch(IOException e){e.printStackTrace();}
	return 0;
}

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

    byte[] buff=new byte[4];
    infile.read(buff); //version

    version=(int)(buff[ 0]&0xFF)|(buff[ 1]&0xFF)<<8|(buff[ 2]&0xFF)<<16|(buff[ 3]&0xFF)<<24;
    infile.read(buff);
    xdim=(int)(buff[ 0]&0xFF)|(buff[ 1]&0xFF)<<8|(buff[ 2]&0xFF)<<16|(buff[ 3]&0xFF)<<24;
    infile.read(buff);
    ydim=(int)(buff[ 0]&0xFF)|(buff[ 1]&0xFF)<<8|(buff[ 2]&0xFF)<<16|(buff[ 3]&0xFF)<<24;
    infile.read(buff);
    iNumFrames=(buff[ 0]&0xFF)|(buff[ 1]&0xFF)<<8|(buff[ 2]&0xFF)<<16|(buff[ 3]&0xFF)<<24;
    iNumFrames=iNumFrames*10;
    framesize=xdim*ydim*2;
    channel = infile.getChannel();
    System.gc();
    System.runFinalization();
    }
    catch(EOFException e){System.out.println("eof: number of frames read is "+j);}
    catch(IOException e){System.out.println("error reading the file, please check..."); e.printStackTrace();}
    return 1;
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

	arr[0]=xdim;
	arr[1]=ydim;
	arr[2]=1;
	arr[3]=iNumFrames;
	return 1;
}

 public int getvalue(byte[] inbuffer, int frame, int y, int x){
    int loc=2*(y*128+x);
    //return (int)((inbuffer[loc]&0xFF)|(inbuffer[loc+1]&0xFF)<<8);
    return (int)((inbuffer[loc])|(inbuffer[loc+1]&0xFF)<<8);
    //return (int)(inbuffer[loc]);
    //return (int)(inbuffer[loc+1]);
   }

public int UpdateImageArray(int[] arr, int xdim, int ydim, int instance){
    /*to compensate for trace by trace order
    frame[xdim][ydim]=
    */
    int val=0;
    try{

		buf = channel.map(FileChannel.MapMode.READ_ONLY, FrameNumber*xdim*ydim*2+16, (int)xdim*ydim*2);
		buf.order(ByteOrder.LITTLE_ENDIAN);
       // buf.get(internalframe);
   /*
	  for (int y=0;y<128;y++){
			 for (int x=0;x<128;x+=4) arr[y*128+(int)x/4]=getvalue(internalframe,FrameNumber,y,x);
			 for (int x=1;x<128;x+=4) arr[y*128+(int)((x-1)/4)+32]=getvalue(internalframe,FrameNumber,y,x);//ma.dat.set(0,y,(int)((x-1)/4)+32,(double)getvalue(infile,buffer,128,128,x,y));
			 for (int x=2;x<128;x+=4) arr[y*128+(int)((x-2)/4)+64]=getvalue(internalframe,FrameNumber,y,x);//ma.dat.set(0,y,(int)((x-2)/4)+64,(double)getvalue(infile,buffer,128,128,x,y));
			 for (int x=3;x<128;x+=4) arr[y*128+(int)((x-3)/4)+96]=getvalue(internalframe,FrameNumber,y,x);//ma.dat.set(0,y,(int)((x-3)/4)+96,(double)getvalue(infile,buffer,128,128,x,y));
    }
   */
	  for (int y=0;y<128;y++){
			 for (int x=0;x<128;x+=4) arr[y*128+(int)x/4]=buf.getShort(2*(y*128+x));//getvalue(internalframe,FrameNumber,y,x);
			 for (int x=1;x<128;x+=4) arr[y*128+(int)((x-1)/4)+32]=buf.getShort(2*(y*128+x));//getvalue(internalframe,FrameNumber,y,x);//ma.dat.set(0,y,(int)((x-1)/4)+32,(double)getvalue(infile,buffer,128,128,x,y));
			 for (int x=2;x<128;x+=4) arr[y*128+(int)((x-2)/4)+64]=buf.getShort(2*(y*128+x));//getvalue(internalframe,FrameNumber,y,x);//ma.dat.set(0,y,(int)((x-2)/4)+64,(double)getvalue(infile,buffer,128,128,x,y));
			 for (int x=3;x<128;x+=4) arr[y*128+(int)((x-3)/4)+96]=buf.getShort(2*(y*128+x));//getvalue(internalframe,FrameNumber,y,x);//ma.dat.set(0,y,(int)((x-3)/4)+96,(double)getvalue(infile,buffer,128,128,x,y));
        }
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


