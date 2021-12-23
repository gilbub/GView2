package gvdecoder;
import java.util.*;
public class MatrixIterator implements Iterator{

 public int i;
 public int z;
 public int y;
 public int x;
 double v;
 public Matrix ma;

 public MatrixIterator(Matrix ma){
  this.ma=ma;
  i=0;

 }

 public void remove(){

}

 public boolean hasNext(){
  return i<(ma.zdim*ma.ydim*ma.xdim);
 }

 public MatrixIterator next(){
   i=i+1;
   if (hasNext()){
   z=(int)(i/(ma.ydim*ma.xdim));
   y=(int)((i-z*ma.ydim*ma.xdim)/ma.xdim);
   x=(int)(i-z*ma.ydim*ma.xdim-y*ma.xdim);
   v=ma.dat.get(z,y,x);
   return this;
   }
   return null;
   }

  public double nbValue(int zd, int yd, int xd){
	  int zi=z+zd;
	  int yi=y+yd;
	  int xi=x+xd;
	  if ((zi>=0)&&(zi<ma.zdim)&&(yi>=0)&&(yi<ma.ydim)&&(xi>=0)&&(xi<ma.xdim)){
		  return ma.dat.get(zi,yi,xi);
	  }
      return 0;

}



}
