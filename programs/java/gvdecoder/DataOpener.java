 package gvdecoder;
 import java.nio.*;
 import java.nio.channels.*;
 import java.io.*;

 public class DataOpener{
 public int zdim=-1;
 public int ydim=-1;
 public int xdim=-1;
 public double[] dat;


 public double[] OpenImageFile(String filename, int position){
		try{
			 RandomAccessFile infile=new RandomAccessFile(filename,"r");
			 int version=infile.readInt(); //version
			 zdim=infile.readInt();
			 ydim=infile.readInt();
			 xdim=infile.readInt();
			 dat=new double[zdim*ydim*xdim];
			 FileChannel fc = infile.getChannel();
			 MappedByteBuffer buffer = fc.map (FileChannel.MapMode.READ_ONLY, position, fc.size());
			 DoubleBuffer db=buffer.asDoubleBuffer();
			 dat=db.array();
		     infile.close();
		     return dat;
			}catch(IOException e){e.printStackTrace();}
		return null;
		}






    /** Open and Save image file via OpenImageFile(filename) and SaveImageFile(filename)
    write data in 'gv' format - raw data as doubles with three ints leading as a header.
    **/
    /*
    public void SaveImageFile(String filename){
	    SaveImageFile(filename,0,zdim);
	}

    public void SaveImageFile(String filename, int start, int end){
	 try{
	 RandomAccessFile outfile=new RandomAccessFile(filename,"rw");

	 double[] tmp=new double[xdim*ydim];
	 byte[] bytes=new byte[xdim*ydim*8];

	 FileChannel fc=outfile.getChannel();
	 MappedByteBuffer buffer = fc.map (FileChannel.MapMode.READ_WRITE, 0, zdim*xdim*ydim*8+4*4);
     buffer.putInt(0); //version
	 buffer.putInt(zdim);
	 buffer.putInt(ydim);
	 buffer.putInt(xdim);
	  for (int z=start;z<end;z++){
		 for (int y=0;y<ydim;y++){
			 for (int x=0;x<xdim;x++){
				 //outfile.writeDouble(dat.get(z,x,y));
				 buffer.putDouble(dat.get(z,y,x));
	//		 	 for (int k=0;k<8;k++) bytes[(8*(x*ydim+y))+k]=a[k];
			 }
		 }
	 }
	outfile.close();
	}catch(IOException e){e.printStackTrace();}
}
*/
}