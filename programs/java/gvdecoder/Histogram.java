package gvdecoder;
import gvdecoder.array.doubleArray3D;

public class Histogram{


 public double min;
 public double max;
 public int[] bins;
 public double[] dbins;
 public int samples;

 public int numberOfBins=100;

 public void findMaxMin(double[] data){
  min=Double.MAX_VALUE;
  max=Double.MAX_VALUE*-1.0;
  samples=0;
  double val;
  for (int i=0;i<data.length;i++){
    val=data[i];
    if (val<min) min=val;
    if (val>max) max=val;
    samples++;
  }

 }

 public void findMaxMin(int[] data){
   min=Double.MAX_VALUE;
   max=Double.MAX_VALUE*-1.0;
   samples=0;
   int val;
   for (int i=0;i<data.length;i++){
     val=data[i];
     if (val<min) min=val;
     if (val>max) max=val;
     samples++;
   }
 }


public void findMaxMin(Viewer2 vw, int start, int end){
    min=Double.MAX_VALUE;
    max=Double.MAX_VALUE*-1.0;
    samples=0;
    int val;

    for (int z=start;z<end;z++){
     for (int y=0;y<vw.Y_dim;y++){
	  for (int x=0;x<vw.X_dim;x++){
		 val= vw.getPixel(z,y,x);
         if (val<min) min=val;
         if (val>max) max=val;
         samples++;
	 }
    }
   }
 }

 public void findMaxMin(Matrix ma, int start, int end){
     min=Double.MAX_VALUE;
     max=Double.MAX_VALUE*-1.0;
     samples=0;
     double val;

     for (int z=start;z<end;z++){
      for (int y=0;y<ma.ydim;y++){
 	  for (int x=0;x<ma.xdim;x++){
 		 val= ma.dat.get(z,y,x);
          if (val<min) min=val;
          if (val>max) max=val;
          samples++;
 	 }
     }
    }

 }

 public void findMaxMin(gvdecoder.array.doubleArray3D dat, int start, int end){
      min=Double.MAX_VALUE;
      max=Double.MAX_VALUE*-1.0;
      samples=0;
      double val;

      for (int z=start;z<end;z++){
       for (int y=0;y<dat.ydim;y++){
  	  for (int x=0;x<dat.xdim;x++){
  		 val= dat.get(z,y,x);
           if (val<min) min=val;
           if (val>max) max=val;
           samples++;
  	 }
      }
     }

  }



 public void findMaxMin(Viewer2 vw,int start, int end, int[] indexes){
	min=Double.MAX_VALUE;
	max=Double.MAX_VALUE*-1.0;
	samples=0;
    int val;
    for (int z=start;z<end;z++){
        vw.JumpToFrame(z);
        for (int i=0;i<indexes.length;i++){
			val=vw.datArray[indexes[i]];
			if (val<min) min=val;
			if (val>max) max=val;
            samples++;
		}
	}
 }

 public void findMaxMin(Matrix ma,int start, int end, int[] indexes){

 	samples=0;
     double val;
     for (int z=start;z<end;z++){
        // vw.JumpToFrame(z);
         for (int i=0;i<indexes.length;i++){
 			val=ma.dat.arr[z*ma.xdim*ma.ydim+indexes[i]];
 			if (val<min) min=val;
 			if (val>max) max=val;
             samples++;
 		}
 	}
  }

  public void findMaxMin(Matrix ma,int startz, int endz, int starty, int endy, int startx, int endx){
       min=Double.MAX_VALUE;
       max=Double.MAX_VALUE*-1.0;
   	   samples=0;
       double val;
       for (int z=startz;z<endz;z++){
		 for (int y=starty;y<endy;y++){
		   for (int x=startx;x<endx;x++){
   			val=ma.dat.get(z,y,x);
   			if (val<min) min=val;
   			if (val>max) max=val;
               samples++;
   		}
	   }
   	}
  }




public void findMaxMin(gvdecoder.array.doubleArray3D dat,int start, int end, int[] indexes){
 	min=Double.MAX_VALUE;
 	max=Double.MAX_VALUE*-1.0;
 	samples=0;
     double val;
     for (int z=start;z<end;z++){
        // vw.JumpToFrame(z);
         for (int i=0;i<indexes.length;i++){
 			val=dat.arr[z*dat.xdim*dat.ydim+indexes[i]];
 			if (val<min) min=val;
 			if (val>max) max=val;
             samples++;
 		}
 	}
  }


public Histogram(double[] data, int numberOfBins){

  this.numberOfBins=numberOfBins;
  findMaxMin(data);
  bins=new int[numberOfBins+1];
  double scale= numberOfBins/(max-min);
  for (int i=0;i<samples;i++){
    bins[(int)((data[i]-min)*scale)]+=1;
  }
 }


public Histogram(int[] data, int numberOfBins){

   this.numberOfBins=numberOfBins;
   findMaxMin(data);
   bins=new int[numberOfBins+1];
   double scale=numberOfBins/(max-min);
   for (int i=0;i<samples;i++){
     bins[(int)((data[i]-min)*scale)]+=1;
   }
 }


public Histogram(Viewer2 vw, int startframe, int endframe, int numberOfBins){

   this.numberOfBins=numberOfBins;
   findMaxMin(vw,startframe,endframe);
   bins=new int[numberOfBins+1];
   double scale=numberOfBins/(max-min);
   for (int z=startframe;z<endframe;z++){
	   for (int y=0;y<vw.Y_dim;y++){
		   for (int x=0;x<vw.X_dim;x++){
             bins[(int)((vw.getPixel(z,y,x)-min)*scale)]+=1;
		 }
	 }
   }
 }


 public Histogram(Matrix ma, int startframe, int endframe, int numberOfBins){

    this.numberOfBins=numberOfBins;
    findMaxMin(ma,startframe,endframe);
    bins=new int[numberOfBins+1];
    double scale=numberOfBins/(max-min);
    for (int z=startframe;z<endframe;z++){
 	   for (int y=0;y<ma.ydim;y++){
 		   for (int x=0;x<ma.xdim;x++){
              bins[(int)((ma.dat.get(z,y,x)-min)*scale)]+=1;
 		 }
 	 }
    }
 }

 public Histogram(doubleArray3D dat, int startframe, int endframe, int numberOfBins){

     this.numberOfBins=numberOfBins;
     findMaxMin(dat,startframe,endframe);
     bins=new int[numberOfBins+1];
     double scale=numberOfBins/(max-min);
     for (int z=startframe;z<endframe;z++){
  	   for (int y=0;y<dat.ydim;y++){
  		   for (int x=0;x<dat.xdim;x++){
               bins[(int)((dat.get(z,y,x)-min)*scale)]+=1;
  		 }
  	 }
     }
 }


 public int[] findIndexes(int xdim, int ydim, java.awt.Polygon poly){
	 java.util.Vector v=new java.util.Vector();

	 for (int i=0;i<xdim;i++){
	    for (int j=0;j<ydim;j++){
	      if (poly.contains(i,j)) v.add(new Integer(j*xdim+i));
	      }
     }

	 int[] res=new int[v.size()];
	 for (int i=0;i<v.size();i++){
		 res[i]=((Integer)(v.elementAt(i))).intValue();
	 }
	 return res;
 }

 public Histogram(Viewer2 vw, int startframe,int endframe, int numberOfBins, java.awt.Polygon poly){
	this.numberOfBins=numberOfBins;
	//find indexes of the pixels within the polygon given vw's dimensions
	int[] indexes=findIndexes(vw.X_dim, vw.Y_dim,  poly);
	findMaxMin(vw,startframe,endframe,indexes);
	double scale=numberOfBins/(max-min);
	bins=new int[numberOfBins+1];
	for (int z=startframe;z<endframe;z++){
		 vw.JumpToFrame(z);
		 for (int i=0;i<indexes.length;i++){
		    bins[(int)((vw.datArray[indexes[i]]-min)*scale)]+=1;
	}
 }
}

 public Histogram(Matrix ma, int startframe,int endframe, int numberOfBins, java.awt.Polygon poly){
	this.numberOfBins=numberOfBins;
	//find indexes of the pixels within the polygon given vw's dimensions
	int[] indexes=findIndexes(ma.xdim, ma.ydim,  poly);
	findMaxMin(ma,startframe,endframe,indexes);
	double scale=numberOfBins/(max-min);
	bins=new int[numberOfBins+1];
	for (int z=startframe;z<endframe;z++){
		 for (int i=0;i<indexes.length;i++){
		    bins[(int)((ma.dat.arr[indexes[i]+ma.xdim*ma.ydim*z]-min)*scale)]+=1;
	}
 }
}


public Histogram(doubleArray3D dat, int startframe,int endframe, int numberOfBins, java.awt.Polygon poly){
	this.numberOfBins=numberOfBins;
	//find indexes of the pixels within the polygon given vw's dimensions
	int[] indexes=findIndexes(dat.xdim, dat.ydim,  poly);
	findMaxMin(dat,startframe,endframe,indexes);
	double scale=numberOfBins/(max-min);
	bins=new int[numberOfBins+1];
	for (int z=startframe;z<endframe;z++){
		 for (int i=0;i<indexes.length;i++){
		    bins[(int)((dat.arr[indexes[i]+dat.xdim*dat.ydim*z]-min)*scale)]+=1;
	}
 }
}

public void calculate(Matrix ma, int startz, int endz, int starty, int endy, int startx, int endx,boolean weighted){
	findMaxMin(ma,startz,endz,starty,endy,startx,endx);
	for (int i=0;i<bins.length;i++){bins[i]=0;}
	double scale=numberOfBins/(max-min);
	int cx=(int)((endx+startx)/2.0);
	int cy=(int)((endy+starty)/2.0);
	scale=scale-(scale*0.0001);
	for (int z=startz;z<endz;z++){
	  for (int y=starty;y<endy;y++){
	    for (int x=startx;x<endx;x++){
			 if (weighted){
				 double d=java.lang.Math.sqrt( (x-cx)*(x-cx)+(y-cy)*(y-cy));
				 int w=(int)((cx-startx)-d+1);
				 bins[(int)((ma.dat.get(z,y,x)-min)*scale)]+=w;


			 }else
			 bins[(int)((ma.dat.get(z,y,x)-min)*scale)]+=1;
		 }
	   }
   }
   normalize();
}

public void normalize(){
	double sum=0;
	int i;
	for (i=0;i<numberOfBins;i++){
		sum+=bins[i];
	}
	for (i=0;i<numberOfBins;i++){
		dbins[i]=bins[i]/sum;
	}
}

public int mode(){
	int max=-1;
	int k=-1;
	for (int i=0;i<numberOfBins;i++){
		if (bins[i]>max){
		 k=i;
		 max=bins[i];
		}
    }
  return k;
}

public Histogram(Matrix ma, int startz, int endz, int starty, int endy, int startx, int endx, int numberOfBins,boolean weighted){
	this.numberOfBins=numberOfBins;
	bins=new int[numberOfBins+1];
	dbins=new double[numberOfBins+1];
    calculate(ma,startz,endz,starty,endy,startx,endx,weighted);

}

}