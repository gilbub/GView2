package gvdecoder;
import gvdecoder.array.*;

public abstract class function{

public abstract double compute(int index, double val);

public abstract double[] compute(double[] arr);

public abstract double compute(doubleArray3D val);

public static void test(function f){
   System.out.println("compute returns="+f.compute(2,1.0));
 }

}