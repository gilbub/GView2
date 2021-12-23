package gvdecoder;

public class HoughTransform{

//double[] accumulator;

public Matrix data;
public Matrix accumulator;
int xdim,ydim;
//public int[] accumulator;
public double deltaTheta=Math.PI/100;
public int thetabins,rbins;
public double deltatheta,deltar;
public double threshold;
public double TwoPI;
/*
 r=x*cos(theta)+y*sin(theta)
 r is positive with max bounds = sqrt(xdim**2+ydim**2)
 theta is between 0 and 2PI.
*/

public HoughTransform(Matrix data, int threshold, int thetabins){
 this.data=data;
 this.xdim=data.xdim;
 this.ydim=data.ydim;
 this.thetabins=thetabins;
 this.threshold=threshold;
 TwoPI=Math.PI*2;
 deltatheta=2*Math.PI/thetabins;
 rbins=(int)(Math.sqrt(xdim*xdim+ydim*ydim));
 deltar=1.0;
 //accumulator=new Matrix(1,rbins,thetabins); //r=y axis, theta=x axis
     accumulator=new Matrix();
     accumulator.xdim=thetabins;
	 accumulator.ydim=rbins;
	 accumulator.zdim=1;
	 accumulator.dat=new gvdecoder.array.doubleArray3D(1,rbins,thetabins);
 }

public void transform(){
 int i;
 double r;
 double a;
 for (int y=0;y<ydim;y++){
  for (int x=0;x<xdim;x++){

   if (data.dat.get(0,y,x)>=threshold){
     for (a=0;a<TwoPI;a+=deltatheta){
       r=x*Math.cos(a)+y*Math.sin(a);
       System.out.println("x,y,r,a="+x+","+y+","+r+","+a);
       if (r>=0){
         int rindex=(int)r;
         int thetaindex=(int)(a/deltatheta);
         double oldval=accumulator.dat.get(0,rindex,thetaindex);
         accumulator.dat.set(rindex,thetaindex,(oldval+1));
         System.out.println("setting rindex,thetaindex="+rindex+","+thetaindex+","+oldval);
       }

     }//a
     }//threshold
     }//xdim
     }//ydim
     }//transform


 public void clearAccumulator(){
   for (int i=0;i<accumulator.dat.arr.length;i++) accumulator.dat.arr[i]=0.0;
 }

 public void clearData(){
   for (int i=0;i<data.dat.arr.length;i++) data.dat.arr[i]=0.0;

 }

public void clear(){
	clearAccumulator();
	clearData();
}


}