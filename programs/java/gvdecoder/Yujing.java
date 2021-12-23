import java.io.*;
import java.nio.*;
import java.nio.MappedByteBuffer;


public class Yujing{
public double[] mydata;
public int OpenImageFile(String filename){
		try{
			 RandomAccessFile infile=new RandomAccessFile(filename,"r");
			 int version=infile.readInt(); //version
			 zdim=infile.readInt();
			 ydim=infile.readInt();
			 xdim=infile.readInt();
			 //the line below should initialize a 3D array
			 //if (create(zdim,ydim,xdim))
			 data =new double[zdim*ydim*xdim;;
			 FileChannel fc = infile.getChannel();
			 MappedByteBuffer buffer = fc.map (FileChannel.MapMode.READ_ONLY, 0, fc.size());
			 if (version==0){
			 DoubleBuffer db=buffer.asDoubleBuffer();
			 double[] tmp=new double[xdim*ydim];

			 for (int z=0;z<zdim;z++){
				 db.get(tmp,0,xdim*ydim);
				 for (int y=0;y<ydim;y++){
					 for (int x=0;x<xdim;x++){

						 data[z*(xdim*ydim)+y*xdim+x]=tmp[y*xdim+x];
					 }
				 }
			 }
		    }
		    if (version==1){
			 ShortBuffer db=buffer.asShortBuffer();
			 short[] tmp=new short[xdim*ydim];

			 			 for (int z=0;z<zdim;z++){
			 				 db.get(tmp,0,xdim*ydim);
			 				 for (int y=0;y<ydim;y++){
			 					 for (int x=0;x<xdim;x++){

			 						data[z*(xdim*ydim)+y*xdim+x]=tmp[y*xdim+x];
			 					 }
			 				 }
			 }
			}

			infile.close();

			}catch(IOException e){e.printStackTrace();}
		return 0;
		}


 public Yujing(String filename){
	 OpenImageFile(filename);

 }



}