package gvdecoder;
import gvdecoder.array.*;

public class APDetector5 extends function{
	double highthresh;
	double lowthresh;
	double thr;
	int zplus;
	int zminus;
	int yplus;
	int yminus;
	int xplus;
	int xminus;
	int z0;
    int x0;
    int y0;

public APDetector5(){}


public APDetector5(double highthresh, double lowthresh, double thr, int zminus, int zplus, int yminus, int yplus, int xminus, int xplus){
	this.highthresh=highthresh;
	this.lowthresh=lowthresh;
	this.zminus=zminus;
	this.zplus=zplus;
	this.yminus=yminus;
	this.yplus=yplus;
	this.xminus=xminus;
	this.xplus=xplus;
	this.z0=zminus;
	this.x0=xminus;
	this.y0=yminus;
	this.thr=thr;
}


 public double compute(int x, double d){
	 return 2;
 }


public double [] compute(double[] arr){
	return null;

}

public double sum(doubleArray3D dat, int zmin, int zmax, int ymin, int ymax, int xmin, int xmax){
	int count=0;

	double total=0;

	for (int z=zmin;z<zmax;z++){
		for (int y=ymin;y<ymax;y++){
	 		for (int x=xmin;x<xmax;x++){
	             			count++;
	             			total+=dat.get(z,y,x);
			}
		}
	}
	return total/count;

}


 public double compute(doubleArray3D dat){
     double v=0;
     //make a decision based on the idea that z t-1=low, z t=high, z t+1 midhigh
     if(
       ((dat.get(z0,y0,x0)-dat.get(z0-1,y0,x0)) >highthresh) &&
        (dat.get(z0+1,y0,x0)>lowthresh) &&
        (sum(dat,0,zminus-1,0,(yminus+yplus+1),0,(xminus+xplus+1))>thr)
        )


        v=1000;


     return v;
   }


}
/*
public double average(doubleArray3D arr){
	double val=0;
	for (int x=0;x<arr.arr.length;x++){
	   val+=arr.arr[x];
	}
	return val;
}
*/
