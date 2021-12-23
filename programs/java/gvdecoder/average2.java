package gvdecoder;
import gvdecoder.array.*;

public class average2 extends function{
 public double compute(int x, double d){
	 return 2;
 }

 public double compute(doubleArray3D da){
     double v=0;
     for (int i=0;i<da.arr.length;i++){
       v+=da.arr[i]*2;
     }
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
