package gvdecoder;

import java.io.*;
import java.util.*;

import java.nio.*;
import java.nio.MappedByteBuffer;




public class GenericDecoder extends ImageDecoderAdapter {

public static int LITTLEENDIAN=0;
public static int BIGENDIAN=1;

RandomAccessFile Picfile;
int FrameNumber=0;
String filename;





public int xdim;
public int ydim;
public int zdim;
public int headersize;
public int bytespervalue;
public byte[] bytearray;
public int byteorder;
public String header;

public int FRAMESTEP=1;


public GenericDecoder(){

}

public GenericDecoder(int headersize, int bytespervalue, int byteorder, int xdim, int ydim, int zdim){

  this.headersize=headersize;
  this.bytespervalue=bytespervalue;
  this.xdim=xdim;
  this.ydim=ydim;
  this.zdim=zdim;
  this.byteorder=byteorder;
  this.bytespervalue=bytespervalue;
  bytearray=new byte[xdim*ydim*bytespervalue];

}


public int parseFITS(){
  this.xdim=parseFITS("NAXIS1");
  System.out.println("parseFITS xdim="+xdim);
  this.ydim=parseFITS("NAXIS2");
  this.zdim=parseFITS("NAXIS3");
  int tmp=parseFITS("BITPIX");
  this.bytespervalue=tmp/8;
  this.headersize=2880;
  this.byteorder=LITTLEENDIAN;//default
  if (ByteOrder.nativeOrder().equals(ByteOrder.BIG_ENDIAN)) this.byteorder=BIGENDIAN;
  if ((xdim>0)&&(ydim>0)&&(zdim>0)&&(bytespervalue>0)) {
   bytearray=new byte[xdim*ydim*bytespervalue];
   return 1;
  }
  return 0;
}


public int parseFITS(String keystring){
 if (header==null){
 try{
 	byte[] tmp=new byte[2880];
 	Picfile.seek(0);
 	Picfile.read(tmp);
 	header=new String(tmp);
   }catch(IOException e){e.printStackTrace(); return -1;}
   }
   int st=header.indexOf(keystring);
   if (st==-1) return -1;
   int eq=header.indexOf("=",st);
   String frag=header.substring(eq+1,st+80);
   return java.lang.Integer.parseInt(frag.trim());
}

public int FilterOperation(int OperationCode, int startx, int endx, int instance){return 0;}

public int CloseImageFile(int instance){
	 try{
	    Picfile.close();
	}catch(IOException e){e.printStackTrace(); return -1;}
	 return 1;
	}

//public int FilterOperation(int a, int b, int c, int d){ return 0;}

public int OpenImageFile(String filename){
 try{
   Picfile=new RandomAccessFile(filename,"r");
  }
  catch(IOException e){System.out.println("error reading the file, please check..."); e.printStackTrace(); return -1;}
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

     Picfile.seek(headersize+(xdim*ydim*FrameNumber*bytespervalue));
     Picfile.read(bytearray);
     ByteBuffer bb = ByteBuffer.wrap(bytearray);
     if (byteorder==LITTLEENDIAN){
      bb.order( ByteOrder.LITTLE_ENDIAN);
      if (bytespervalue==1) for (int i=0;i<xdim*ydim;i++) arr[i]=(int)bb.get(i);
      else if (bytespervalue==2) for (int i=0;i<xdim*ydim;i++) arr[i]=(int)bb.getShort(i*bytespervalue);
      else if (bytespervalue==4) for (int i=0;i<xdim*ydim;i++) arr[i]=(int)bb.getInt(i*bytespervalue);
      }
     else if (byteorder==BIGENDIAN){
      bb.order( ByteOrder.BIG_ENDIAN);
      if (bytespervalue==1) for (int i=0;i<xdim*ydim;i++) arr[i]=(int)bb.get(i);
      else if (bytespervalue==2) for (int i=0;i<xdim*ydim;i++) arr[i]=(int)bb.getShort(i*bytespervalue);
      else if (bytespervalue==4) for (int i=0;i<xdim*ydim;i++) arr[i]=(int)bb.getInt(i*bytespervalue);
      }

    FrameNumber++;

    } catch(Exception e){
	    System.out.println("error reading into filebuffer in PicDecoder");
	    e.printStackTrace();
	    return -1;
	   }

return 1;
}




}


