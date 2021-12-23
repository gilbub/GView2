 package gvdecoder;
 import java.nio.*;
 import java.nio.channels.*;
 import java.io.*;

 public class Opener1{
  public int zdim=-1;
  public int ydim=-1;
  public int xdim=-1;
  public int version=-1;
  public double[] dat;

  //open using full path
  public double[] OpenImageFile(String filename){
		try{
			 RandomAccessFile infile=new RandomAccessFile(filename,"r");
			 version=infile.readInt(); //version - most cases this is '0'
			 zdim=infile.readInt();
			 ydim=infile.readInt();
			 xdim=infile.readInt();
			 dat=new double[zdim*ydim*xdim];
			 FileChannel fc = infile.getChannel();
			 //header size is 16 (4 integers)
			 MappedByteBuffer buffer = fc.map (FileChannel.MapMode.READ_ONLY, 16, fc.size()-16);
			 //in most cases the files are saved as doubles
			 DoubleBuffer db=buffer.asDoubleBuffer();
			 //copy the buffer to the double array
			 db.get(dat);
		     infile.close();
		     return dat;
			}catch(IOException e){e.printStackTrace();}
		return null;
		}

}