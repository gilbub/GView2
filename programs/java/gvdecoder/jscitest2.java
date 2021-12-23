import JSci.maths.*;
import JSci.awt.*;
import JSci.swing.*;
import java.awt.*;
import ptolemy.plot.*;

public class jscitest2{

public static void main(String[] arg){
double[] tmp=new double[20];
double[] tmp1=new double[20];
double[] tmp2=new double[20];
int count=0;
double pi=3.141;
DefaultGraph2DModel valModel;
EditablePlot p1;

for (double i=0;i<20;i+=1.0){
    double x=i/1000;
	tmp[count++]=Math.random()*1000;
}

for ( int i=0;i<20;i+=1.0){

	tmp1[i]=tmp[i]/10;
	tmp2[i]=tmp[i]/100;
}


 double[] cmp=JSci.maths.ArrayMath.normalize(tmp);
double[] cmp1=JSci.maths.ArrayMath.normalize(tmp1);
double[] cmp2=JSci.maths.ArrayMath.normalize(tmp2);


for (int j=0;j<20;j++){

	System.out.println(tmp[j]+" "+cmp[j]+" "+cmp1[j]+" "+cmp2[j]);

	}
}
}