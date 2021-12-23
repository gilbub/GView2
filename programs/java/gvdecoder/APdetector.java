package gvdecoder;

import gvdecoder.array.*;

public class APDetector2 extends function{
	double highthresh;
	double lowthresh;
	int zplus;
	int zminus;
	int yplus;
	int yminus;
	int xplus;
	int xminus;
	int z0;
    int x0;
    int y0;

public APDetector2(){}


public APDetector2(double highthresh, double lowthresh, int zminus, int zplus, int yminus, int yplus, int xminus, int xplus){
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
}


 public double compute(int x, double d){
	 return 2;
 }


public double [] compute(double[] arr){
	return null;

}
 public double compute(doubleArray3D dat){
     double v=0;
     //make a decision based on the idea that z t-1=low, z t=high, z t+1 midhigh
    if ((dat.get(z0,y0,x0)>highthresh) &&
        (dat.get(z0-1,y0,x0)<lowthresh) &&
        (dat.get(z0+1,y0,x0)>lowthresh))
        v=1.0;

     return Math.random();
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
