package gvdecoder;
public class FitLine{

public static double SumX;
public static double SumY;
public static double XSquared;
public static double XY;
public static double YSquared;
public static double Yave;
public static double Xave;
public static double StdDevX;
public static double StdDevY;
public static double a;
public static double b;

public static double r;
public static double r_covariance;

public static double[] fit(double[] xs, double[] ys){
  int size=xs.length;
  SumX=0;
  SumY=0;
  XSquared=0;
  XY=0;
  YSquared=0;
  Yave=0;
  StdDevX=0;
  StdDevY=0;

  for (int i=0;i<size;i++){
    SumX+=xs[i];
    SumY+=ys[i];
    XSquared+=xs[i]*xs[i];
    YSquared+=ys[i]*ys[i];
    XY+=xs[i]*ys[i];
    }

   Yave=SumY/size;
   Xave=SumX/size;

  double sumresx=0;
  double sumresy=0;
  double sumresxy=0;
  for (int k=0;k<size;k++){
	sumresx+=(xs[k]-Xave)*(xs[k]-Xave);
	sumresy+=(ys[k]-Yave)*(ys[k]-Yave);
	sumresxy+=(xs[k]-Xave)*(ys[k]-Yave);
  }
  StdDevX=Math.sqrt(sumresx/(size-1));
  StdDevY=Math.sqrt(sumresy/(size-1));
  r_covariance=(sumresxy/(size-1))/(StdDevX*StdDevY);


   a=(SumY*XSquared - SumX*XY)/(size*XSquared-SumX*SumX);
   b=(size*XY-SumX*SumY)/(size*XSquared - SumX*SumX);
   r=XY/Math.sqrt(XSquared*YSquared);
   double ExplainedVariation=0;
   double TotalVariation=0;

   for (int j=0;j<size;j++){
     double yest=xs[j]*b+a;
     ExplainedVariation+=(yest-Yave)*(yest-Yave);
   	 TotalVariation+=(ys[j]-Yave)*(ys[j]-Yave);


     }
   r=ExplainedVariation/TotalVariation;
   double[] res=new double[4];
   res[0]=a;
   res[1]=b;
   res[2]=r;
   res[3]=r_covariance;
   return res;
}


public static void main(String[] arg){
 double[] x=new double[5];
 double[] y=new double[5];
 x[0]=0;
 x[1]=1;
 x[2]=2;
 x[3]=3;
 x[4]=4;

 y[0]=0;
 y[1]=1;
 y[2]=2;
 y[3]=3;
 y[4]=3;
 double[] res=FitLine.fit(x,y);
 System.out.println("a="+res[0]+" b="+res[1]+" r="+res[2]+" rc="+res[3]);



}

}