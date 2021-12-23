package gvdecoder;
import JSci.maths.*;
import JSci.awt.*;
import JSci.swing.*;
import java.awt.*;
import ptolemy.plot.*;

public class jscitest{

public static void main(String[] arg){
double[] tmp=new double[256];
int count=0;
double pi=3.141;
DefaultGraph2DModel valModel;
EditablePlot p1;

for (double i=0;i<256;i+=1.0){
    double x=i/1000;
	tmp[count++]=Math.sin((2*pi*50*x)+0.342)+Math.sin((2*pi*120*x));
}

 Complex[] cmp=JSci.maths.FourierMath.transform(tmp);
 Complex[] cnj=new Complex[256];
 for (int j=0;j<cmp.length;j++){
     cnj[j]=cmp[j].conjugate();
     cmp[j]=cmp[j].multiply(cnj[j]);
	// System.out.println((((double)j)/256*1000)+" "+tmp[j]+" "+cmp[j].real()+" "+cnj[j]+" "+cmp[j].multiply(cnj[j]));
 }


float[]res=new float[256];
float[] xaxis=new float[256];
for (int k=0;k<res.length;k++){
	res[k]=(float)cmp[k].real();
	xaxis[k]=((float)k);
	}

Frame fr=new Frame();

fr.setLayout(new BorderLayout());
fr.setSize(700,600);
Panel graph3=new Panel();
p1=new  EditablePlot();
boolean first=true;
for (int i = 0; i < res.length; i++) {
		   p1.addPoint(0, (double)i/256.0*1000.0,(double)res[i], !first);
		   first = false;
		  }

graph3.add(p1);
fr.add(graph3);
fr.pack();
fr.show();

}
}
