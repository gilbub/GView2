package gvdecoder;
/*
 SPEUtils class holds some general purpose routines for reading SPE files.

 function readSPEHeader reads the header of the spe, and returns a class SPEInfo with a summary of information.

 function SPE2Sum  reads a whole spe file, and writes a summed value for a rectangular ROI
 to a file. The default behaviour is to output the data to filename.sum if the file was called
 filename.spe, where the rectangular region is a 3x3 square near the center of the field.

*/

import java.util.*;
import java.io.*;



public class SPEUtils {

	RandomAccessFile spefile;
	int X_dim;//x dimension of the frame
	int Y_dim; //y dimension of the frame
	long numberofframes; //the number of frames
	long LengthOfFile; //the number of scan lines in the y direction for all frames.
    int[] rawdata;
	byte[] spedata;
	int[] flippeddata;
	int mode=4;
	int DataType;
	int offset;
	int framenumber=0;
	double stretch;
	int framesize;
	int x1,x2,y1,y2;


void println(String arg){System.out.println(arg);}

int flipbits(byte[] twobts){
return	(int)(((twobts[1] & 0xff)<<8)|(twobts[0] & 0xff));
}

int flip4bits(byte[] fourbts){
return (int)(((fourbts[3] & 0xff)<<24)|((fourbts[2] & 0xff)<<16)|((fourbts[1] &
0xff)<<8)|(fourbts[0] & 0xff));
}

int readDOSInt(RandomAccessFile file, long position){
	//reads dos int from randomaccess file, and returns as java int
	byte[] bytes=new byte[2];

	try{
		file.seek(position);
		file.readFully(bytes);
	} catch(IOException e){println("file error in readDOSInt");}
	return flipbits(bytes);
}

int readDOS4Int(RandomAccessFile file, long position){
	//reads dos int from randomaccess file, and returns as java int
	byte[] bytes=new byte[4];

	try{
		file.seek(position);
		file.readFully(bytes);
	} catch(IOException e){println("file error in readDOSInt");}
	return flip4bits(bytes);
}



int readSubArray(RandomAccessFile file, int totalframesize, int x, int y, int width, int height){
//tries to sum a roi from a large frame without reading the other data
 long pt=file.getFilePointer();
 int sum=0;
 if (totalframesize==0) totalframesize=X_dim*Y_dim*4;
 //advance to the first x,y position
 for (int i=x;i<x+width;i++){
  for (int j=y;j<y+height;j++){
   sum+=readDOS4Int(file,pt+(y*Y_dim+x)*4);
  }
 }
//advance the pointer
pt+=totalframesize;
file.seek(pt);
return sum;
}

void flipframe(byte[] dosframe, int[] javaframe, int mode){
int j=0;
for (int i=0;i<dosframe.length;i+=4){
flippeddata[j]=(int)((((dosframe[i+3]&0xff<<24)|(dosframe[i+2]&0xff<<16)|(dosframe[i+1]&0xff<<8)|(dosframe[i]&0xff))));
//javaframe[j]=(int)(stretch*(flippeddata[j]-background[j]-offset));
  j++;
}
}

int flippedArrayElement(byte[] dosframe, int x, int y){
int i=4*(y*X_dim+x);
return (int)((((dosframe[i+3]&0xff<<24)|(dosframe[i+2]&0xff<<16)|(dosframe[i+1]&0xff<<8)|(dosframe[i]&0xff))));
}

public boolean readHeader(String directory, String filename){
   try{
	byte[] twobts=new byte[2];
	byte bt1;
	if (spefile!=null) spefile.close();

	spefile=new RandomAccessFile(directory+filename,"r");
    long filesize=spefile.length();

    X_dim=readDOSInt(spefile,42);
	DataType=readDOSInt(spefile,108);

    int temp_numberofframes=readDOSInt(spefile,34);
    System.out.println("tempframes "+temp_numberofframes);
	if( temp_numberofframes == 65535 )//65535=-1
		    numberofframes = (long)readDOS4Int(spefile,664);
		else
		    numberofframes = (long)temp_numberofframes;

	//if (numberofframes<1) numberofframes=1;
	Y_dim = readDOSInt(spefile,656);

    numberofframes=(int)(((filesize-4100)/4)/(X_dim*Y_dim));
    x1=(int)(X_dim/2)-2;
	x2=(int)(X_dim/2)+2;
	y1=(int)(Y_dim/2)-2;
	y2=(int)(Y_dim/2)+2;

	System.out.println("X_Dim :"+X_dim);
	System.out.println("Y_Dim :"+Y_dim);
	System.out.println("x1 :"+x1+" x2 "+x2+" y1 "+y1+" y2 "+y2);

	System.out.println("NumberOfFrames :"+numberofframes);
	System.out.println("frames: "+numberofframes/Y_dim);
    System.out.println("DataType 0=float, 1=long, 2=int, 3=unsigned "+DataType);

    spedata =new byte[X_dim*Y_dim*mode];

    spefile.seek(4100);
    spefile.readFully(spedata);
    rawdata=new int[X_dim*Y_dim];
	flippeddata=new int[X_dim*Y_dim];

	framesize=4*X_dim*Y_dim;



 }catch(IOException e){
  e.printStackTrace();
  return false;
 }

framenumber=1;
return true;
}




public SPEreader(){


}





public void readAndDisplay(){
try{
  // spefile.readFully(spedata);
  // flipframe(spedata,rawdata,mode);
   int b_val=readSubArray(spefile,0,x1,y1,3,3);
/*
	int b_val=0;
	 for (int bx=x1; bx<x2; bx++){
	  for (int by=y1; by<y2; by++){
	   b_val+=flippedArrayElement(spedata,bx,by);
	   //b_val+=flippeddata[by*X_dim+bx];
	   }
	   }

*/

	 System.out.println(framenumber+" "+b_val);





   framenumber++;

   }catch(IOException e){;}


  }







public static void main(String[] args){

SPEreader spereader=new SPEreader();
//if no arguments, list directories in	N:\Sharepoint\Share1\shrierlab\Shared Documents\gils
String directory="N:\\Sharepoint\\Share1\\shrierlab\\Shared Documents\\gils\\";

String filename=args[0];
spereader.readHeader(directory,filename);
long startTime = System.currentTimeMillis();
for (int i=0;i<spereader.numberofframes;i++){
spereader.readAndDisplay();
}
long endTime=System.currentTimeMillis();
long timeDifference=endTime-startTime;
System.out.println("# operation took "+timeDifference);

}
}

