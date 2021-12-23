package gvdecoder;
/*
 SPEUtils class holds some general purpose routines for reading SPE files.

 function readHeader opens a spe, reads its header and populates a SPEInfo object

 function SPE2Sum  reads a whole spe file, and writes a summed value for a rectangular ROI
 to a file. The default behaviour is to output the data to filename.sum if the file was called
 filename.spe, where the rectangular region is a 3x3 square near the center of the field.

 function readInfo(String directory, String filename) reads the header of the spe and returns a string with the info

 function createSummary(String Directory, String filename) reads the spe file (filename.spe), writes a summary (filename.inf) and a sum
 (filename.sum) in the same directory.

 function createSummary(String Directory) attempts to read each spe file in the directory and create a summary.
*/

import java.util.*;
import java.io.*;
import java.util.regex.*;



public class SPEUtils {
    JSPE spe;
	int x1,x2,y1,y2;


static void println(String arg){System.out.println(arg);}

static int flipbits(byte[] twobts){
return	(int)(((twobts[1] & 0xff)<<8)|(twobts[0] & 0xff));
}

static int flip4bits(byte[] fourbts){
return (int)(((fourbts[3] & 0xff)<<24)|((fourbts[2] & 0xff)<<16)|((fourbts[1] &
0xff)<<8)|(fourbts[0] & 0xff));
}

static int readDOSInt(RandomAccessFile file, long position){
	//reads dos int from randomaccess file, and returns as java int
	byte[] bytes=new byte[2];

	try{
		file.seek(position);
		file.readFully(bytes);
	} catch(IOException e){println("file error in readDOSInt");}
	return flipbits(bytes);
}

static int readDOS4Int(RandomAccessFile file, long position){
	//reads dos int from randomaccess file, and returns as java int
	byte[] bytes=new byte[4];

	try{
		file.seek(position);
		file.readFully(bytes);
	} catch(IOException e){println("file error in readDOSInt");}
	return flip4bits(bytes);
}



int readSubArray(RandomAccessFile file, int totalframesize, int x, int y, int width, int height){

//tries to sum a roi from a large frame without reading the other data. Not faster or better than readfully.
 int sum=0;
 byte[] bytes=new byte[4];
try{
 long pt=file.getFilePointer();


 if (totalframesize==0) totalframesize=spe.X_dim*spe.Y_dim*4;
 //advance to the first x,y position
 for (int i=x;i<x+width;i++){
  for (int j=y;j<y+height;j++){
   file.seek(pt+(y*spe.X_dim+x)*4);
   file.readFully(bytes);
   sum+=(int)((((bytes[3]&0xff<<24)|(bytes[2]&0xff<<16)|(bytes[1]&0xff<<8)|(bytes[0]&0xff))));

  }
 }
//advance the pointer
pt+=totalframesize;
file.seek(pt);
}catch(IOException e){e.printStackTrace(); System.exit(0);}
return sum;
}

void flipframe(byte[] dosframe, int[] javaframe, int mode){
int j=0;
for (int i=0;i<dosframe.length;i+=4){
spe.flippeddata[j]=(int)((((dosframe[i+3]&0xff<<24)|(dosframe[i+2]&0xff<<16)|(dosframe[i+1]&0xff<<8)|(dosframe[i]&0xff))));
//javaframe[j]=(int)(stretch*(flippeddata[j]-background[j]-offset));
  j++;
}
}

int flippedArrayElement(byte[] dosframe, int x, int y){
int i=4*(y*spe.X_dim+x);
return (int)((((dosframe[i+3]&0xff<<24)|(dosframe[i+2]&0xff<<16)|(dosframe[i+1]&0xff<<8)|(dosframe[i]&0xff))));
}

public boolean openSPE(String filename){

spe=new JSPE(filename,false);
x1=(int)(spe.X_dim/2)-2;
x2=(int)(spe.X_dim/2)+2;
y1=(int)(spe.Y_dim/2)-2;
y2=(int)(spe.Y_dim/2)+2;
return true;

}

public boolean closeSPE(){
spe.close();
return true;
}


public SPEUtils(){


}

public static boolean findNavFile(String filename,SPEInfo inf){
String navfile="nonexistantfile";
Pattern p = Pattern.compile(".spe",Pattern.CASE_INSENSITIVE);
Matcher m = p.matcher(filename);
if (m.find()){navfile=m.replaceAll(".nav");}
File file = new File(navfile);
if (file.exists()) {inf.NavFile=navfile; inf.HasNavFile=true; return true;}
else {inf.NavFile=navfile; inf.HasNavFile=false; return false;}
}



public static SPEInfo getSPEInfo(String filename){
 SPEInfo speinfo=new SPEInfo();
 RandomAccessFile spefile;
 try{
   	speinfo.Filename=filename;

 	spefile=new RandomAccessFile(filename,"r");

	long filesize=spefile.length();

     speinfo.X_dim=SPEUtils.readDOSInt(spefile,42);
 	 speinfo.DataType=SPEUtils.readDOSInt(spefile,108);
 	 speinfo.Y_dim = SPEUtils.readDOSInt(spefile,656);
 	 int sizeof;
 	 if (speinfo.DataType<=2) sizeof=4; else sizeof=2;
     speinfo.NumberOfFrames=(int)(((filesize-4100)/sizeof)/(speinfo.X_dim*speinfo.Y_dim));

	 //find nav file? try directory where spe is first.
	 if (findNavFile(filename,speinfo)) System.out.println("found it in spe directory");
	 else
	 if (findNavFile((new File(filename).getName()),speinfo)) System.out.println("found it in local directory");
	 else System.out.println("Cant find the nav file...");


  }catch(IOException e){speinfo.Error=true;speinfo.Note="Error reading file.";}

 return speinfo;

}

public int getSum(int x1,int y1,int x2,int y2){
  spe.readFrame();
	int b_val=0;
	 for (int bx=x1; bx<x2; bx++){
	  for (int by=y1; by<y2; by++){
	   b_val+=flippedArrayElement(spe.spedata,bx,by);
	   }
	   }

   spe.framenumber++;
   return b_val;

  }


 public void generateSumFile(String filename){
  PrintWriter file;
  try{
  file=new PrintWriter(new FileWriter(filename),true);
  file.println("# "+spe.longSummary());
  for (int i=0;i<spe.NumberOfFrames;i++){
  	file.println(spe.framenumber+" "+getSum(x1,y1,x2,y2));
	}
  file.close();
  }catch(IOException e){e.printStackTrace();System.exit(0);}
 }

 public void generateGnuplotFile(String filename, String dataname){
  PrintWriter file;
  try{
   file=new PrintWriter(new FileWriter(filename),true);
   file.println("plot \""+dataname+"\" u 1:2 w l");
   file.close();
   }catch(IOException e){e.printStackTrace();System.exit(0);}
 }

 public void generateSummary(String filename){
 String sumname="";
 String infname="";
 openSPE(filename);
 Pattern p = Pattern.compile(".spe",Pattern.CASE_INSENSITIVE);
 Matcher m = p.matcher(filename);
 if (m.find()){
   sumname=m.replaceAll(".sum");
   infname=m.replaceAll(".gnu");
   }else  System.out.println("not spe file?");

 if (spe.NumberOfFrames>50) spe.Note="sumfile generated";
  else spe.Note="sumfile not generated";

 generateGnuplotFile(infname,sumname);
 if (spe.NumberOfFrames>50)generateSumFile(sumname);
 spe.close();
 }



public static void main(String[] args){

SPEUtils spereader=new SPEUtils();
//if no arguments, list directories in	N:\Sharepoint\Share1\shrierlab\Shared Documents\gils
String directory="N:\\Sharepoint\\Share1\\shrierlab\\Shared Documents\\gils\\";

String filename=args[0];

spereader.generateSummary(args[0]);
/*
spereader.openSPE(directory,filename);
long startTime = System.currentTimeMillis();
for (int i=0;i<spereader.spe.NumberOfFrames;i++){
spereader.getSum(spereader.x1,spereader.y1,spereader.x2,spereader.y2);
}
long endTime=System.currentTimeMillis();
long timeDifference=endTime-startTime;
System.out.println("# operation took "+timeDifference);
*/


}

}

