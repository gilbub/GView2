package gvdecoder;

public class Correlate{
 //these functions assume that this is a 2D array (zdim=1)

 public static double mean(Matrix m1){
	 double sum=0;
	 for (int y=0;y<m1.ydim;y++){
	  for (int x=0;x<m1.xdim;x++){
	   sum+=m1.dat.get(0,y,x);
      }
   }
   return sum/(m1.xdim*m1.ydim);
 }

 public static double value(Matrix m1, Matrix m2, int xoff, int yoff){
   double val=0;
   int count=1;
   for (int y=0;y<m1.ydim;y++){
    for (int x=0;x<m1.xdim;x++){
	  int y1=y+yoff;
	  int x1=x+xoff;
	  if ((x1>=0)&&(x1<m2.xdim)&&(y1>=0)&&(y1<m2.ydim)){
         val+=m1.dat.get(0,y,x)*m2.dat.get(0,y1,x1);
         count++;
	    }
      }
     }
    return val/count;
   }



  }