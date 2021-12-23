package gvdecoder;

import java.io.*;

import java.nio.*;

import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

public class SacconiHelper2{

 public void copy(gvdecoder.Viewer2 vw, Matrix ma, int z){

  for (int y=0;y<ma.ydim;y++){
   for (int x=0;x<ma.xdim;x++){
     ma.dat.set(z,y,x,vw.datArray[y*ma.xdim+x]);
   }
  }

 }

 public void copy_camera_to_matrix(gvdecoder.Viewer2 vw, Matrix ma, int z, int fr, int xdim, int ydim){
  for (int y=0;y<ydim;y++){
   for (int x=0;x<xdim;x++){
    int v=vw.datArray[y*7*xdim+x+fr*xdim];
    ma.dat.set(z,y,x,vw.datArray[(y*7*xdim)+x+(fr*xdim)]);
   }
   }
 }

 public void copyrange(gvdecoder.Viewer2 vw, Matrix ma, int z, int fr, int xdim, int ydim){
  for (int y=0;y<ydim;y++){
   for (int x=0;x<xdim;x++){
     ma.dat.set(z,y,fr*xdim+x,vw.datArray[y*xdim+x]);
   }
  }
 }



 public void decodeViewerToMatrix(gvdecoder.Viewer2 vw, Matrix ma, int zstart, int zend){
  for (int z=zstart;z<zend;z++){

    for (int i=0;i<7;i++){
     vw.JumpToFrame(z*7+i);
     for (int y=0;y<ma.ydim;y++){
      for (int x=0;x<ma.ydim;x++){

        ma.dat.set(z-zstart,y,x+i*ma.ydim,vw.datArray[y*ma.ydim+x]);
      }
     }
     }
   }


 }

 public void multitifftogv(gvdecoder.Viewer2 vw, String filename){
	 	 int ydim=400;
	 	 int zdim=950;
	 	 int xdim=400*7;
	 	 int x,y,z;
	 	 Matrix ma=new Matrix(1,400,400*7);
	 	 try{
	 	 RandomAccessFile outfile=new RandomAccessFile(filename,"rw");

	 	 double[] tmp=new double[xdim*ydim];
	 	 byte[] bytes=new byte[xdim*ydim*8];

	 	 FileChannel fc=outfile.getChannel();
	 	 MappedByteBuffer buffer = fc.map (FileChannel.MapMode.READ_WRITE, 0, zdim*xdim*ydim+4*4);
	     buffer.putInt(2); //version
	 	 buffer.putInt(zdim);
	 	 buffer.putInt(ydim);
	 	 buffer.putInt(xdim);
	 	  for (z=0;z<zdim;z++){
		   for (c=0;c<7;c++){
			 vw.JumpToFrame(z*7+c);
	 		 for (y=0;y<400;y++){
	 			 for (x=0;x<400;x++){
					 ma.dat.set(0,y,x+400*c,vw.datArray[y*400+x]);
	 				 //outfile.writeDouble(dat.get(z,x,y));
				   }//x
			    }//y
		      }//c
		     for (y=0;y<400;y++){
			  for (x=0;x<400*7;x++){
	 				 int val=(int)ma.dat.get(0,y,x);
	 				 buffer.put((byte)val);
	 	//		 	 for (int k=0;k<8;k++) bytes[(8*(x*ydim+y))+k]=a[k];
	 			 }//x
	 		 }//y
	  	   }//z

	 	outfile.close();
	 	}catch(IOException e){e.printStackTrace();}
}




}