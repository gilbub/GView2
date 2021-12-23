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






public class NeoDecoder extends ImageDecoderAdapter {
RandomAccessFile Picfile;
int FrameNumber=0;
String filename;

Viewer2 vi=null;





public static String navfilename;

int width;
int height;
int numframes;
int pixelsize;
int dec=0;

long header_size=5120;

public int xdim;
public int ydim;
public int zdim;
public int iNumFrames;
public int remainder;
public int[] internal;
public byte[] buffer;

public String filetype;

public ArrayList images;

public String imagepath="";

public int FRAMESTEP=1;



public NeoDecoder(String filetype){
	FrameNumber=0;
	this.filetype=filetype;
    images=new ArrayList();
}


public int FilterOperation(int OperationCode, int startx, int endx, int instance){return 0;}

public int CloseImageFile(int instance){
	images=null;
	internal=null;
	buffer=null;
	return -1;
	}

//public int FilterOperation(int a, int b, int c, int d){ return 0;}

public int OpenImageFile(String filename){
//asumes filename is a directory that contains a series of images.
//gets the series in alphabetical order, puts each in a list


 File dir=new File(filename);
 System.out.println("the file information is "+dir+" filetype="+filetype);
 String[] tmp=null;
 if (!dir.exists()) {System.out.println("directory not found"); return 0;}
 if (!dir.isDirectory()){
	 System.out.println("the file isn't a directory");
     images.add(dir.getName());
     imagepath=dir.getParent();
     }
 else{
 if (dir.isDirectory()){
   imagepath=filename;
   tmp=dir.list();
  }
  Arrays.sort(tmp);
 for (int i=0;i<tmp.length;i++){
  if (tmp[i].endsWith(filetype) || tmp[i].endsWith(filetype.toUpperCase())) images.add(tmp[i]);
  }
}
  System.out.println("ending OpenImageFile with "+images.size()+" images in directory, path="+imagepath);

  long length=(new File(dir+File.separator+images.get(0))).length();
  if      (length==(((2592*2160)*3)/2)){ xdim=2592; ydim=2160; dec=1;} //8398080
  else if (length==(((2544*2160)*3)/2)){ xdim=2544; ydim=2160; dec=1;} //8274960
  else if (length==(((2064*2048)*3)/2)){ xdim=2064; ydim=2048; dec=1;} //6340608
  else if (length==(((1776*1760)*3)/2)){ xdim=1776; ydim=1760; dec=1;} //4688640
  else if (length==(((1920*1080)*3)/2)){ xdim=1920; ydim=1080; dec=1;} //3110400
  else if (length==(((1392*1040)*3)/2)){ xdim=1392; ydim=1040; dec=1;} //2171520
  else if (length==(((528*512)*3)/2)  ){ xdim=528;  ydim=512; dec=1;}  // 405504
  else if (length==(((240*256)*3)/2)  ){ xdim=240;  ydim=256; dec=1;}  //  92160
  else if (length==(((144*128)*3)/2)  ){ xdim=144;  ydim=128; dec=1;}  //  27648
  else if (length==(((2592*304)*3)/2) ){ xdim=2592; ydim=304; dec=1;}  //1181952
  else if (length==(((2592*2160)*2))){ xdim=2592; ydim=2160; dec=2;}
  else if (length==(((2544*2160)*2))){ xdim=2544; ydim=2160; dec=2;} //8274960
  else if (length==(((2064*2048)*2))){ xdim=2064; ydim=2048; dec=2;} //6340608
  else if (length==(((1776*1760)*2))){ xdim=1776; ydim=1760; dec=2;} //4688640
  else if (length==(((1920*1080)*2))){ xdim=1920; ydim=1080; dec=2;} //3110400
  else if (length==(((1392*1040)*2))){ xdim=1392; ydim=1040; dec=2;} //2171520
  else if (length==(((528*512)*2))  ){ xdim=528;  ydim=512; dec=2;}  // 405504
  else if (length==(((240*256)*2))  ){ xdim=240;  ydim=256; dec=2;}  //  92160
  else if (length==(((144*128)*2))  ){ xdim=144;  ydim=128; dec=2;}  //  27648
  else if (length==(((2592*304)*2)) ){ xdim=2592; ydim=304; dec=2;}  //1181952
  else

  {
	  System.out.println(("Error: file format not supported - only 12bit packed or 16 bit please! "+dir+" Length="+length));
  }
  zdim=images.size();
  if (dec==1) buffer=new byte[(xdim*ydim*3)/2];
  else if (dec==2) buffer=new byte[(xdim*ydim*2)];
  width=xdim;height=ydim;
  System.out.println("Setting xdim="+xdim+" ydim="+ydim+" zdim="+zdim);
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
    int frames=images.size();
	arr[0]=xdim;
	arr[1]=ydim;
	arr[2]=1;
	arr[3]=frames;
	return 1;

}

/*
public void extract2from3(unsigned char* _buffer, int[])
{
  _i_returns[1] = (_buffer[0] << 4) + (_buffer[1] & 0xF);
  _i_returns[0] = (_buffer[2] << 4) + (_buffer[1] >> 4) ;
}

*/

public int UpdateImageArray(int[] arr, int xdim, int ydim, int instance){
   String fileName=imagepath+(String)images.get(FrameNumber);
   System.out.println("trying to open "+fileName);
   try{
   File f=new File(fileName);
   DataInputStream in = new DataInputStream(new FileInputStream(f));
   in.readFully(buffer);
   in.close();
   }catch(IOException e){e.printStackTrace();}
   int j=0;
   if (dec==1){
   for (int i=0;i<arr.length;i+=2){
	   arr[i]=(buffer[j]<<4)+(buffer[j+1] & 0xF);
	   arr[i+1]  =(buffer[j+2]<<4)+(buffer[j+1] >> 4);
       j=j+3;
   }
   }else if (dec==2){
	for (int i=0;i<arr.length;i+=1){
	   //arr[i+1]=(buffer[j]<<4)+(buffer[j+1] & 0xF);
	   //arr[i]  =(buffer[j]<<0)+(buffer[j+1]<<8);
       //arr[i]=buffer[j+1];
       //arr[i]=buffer[j+1]<<8 | buffer[j]; **
       //arr[i]=buffer[j]<<8 | buffer[j+1];
       arr[i]=((buffer[j+1] & 0xFF) << 8) | (buffer[j] & 0xFF);
       j=j+2;
   }


   }
   FrameNumber+=1;
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


public int SumROIs(int[][] rois, String outfile, int startframe, int endframe,int instance){




     System.out.println("in Neo SumROIs start="+startframe+" end="+endframe);
     //determine x,y dimensions
     double averagepixel=0.0;
     int[] dims=new int[4];
     ReturnXYBandsFrames(dims,0);
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
     if (vi!=null) {
		if ((vi.viewerfilter!=null)&&(vi.useviewerfilter)){
		System.out.println("using viewer filter");
		vi.viewerfilter.run(vi,tmparray);
	   }
     }

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


public static void main(String[] arg){
System.out.println("redshirt reader test");
RedShirtDecoder pd=new RedShirtDecoder();
pd.OpenImageFile(arg[0]);
pd.write_matrix(arg[1]);
//pd.test();



}

}


