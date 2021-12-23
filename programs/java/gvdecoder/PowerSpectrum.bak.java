package gvdecoder;
import JSci.maths.*;
import JSci.maths.wavelet.daubechies2.*;

public class PowerSpectrum{

public double[] create(float[] dat){
	//recast
	double[] dbl=JSci.util.ArrayCaster.toDouble(dat);
    double[] tmp=JSci.maths.ArrayMath.setLengthFromBeginning(dbl,poweroftwo(dbl));
	Complex[] cmp=JSci.maths.FourierMath.transform(tmp);
	Complex[] cnj=new Complex[cmp.length];
	double [] res=new double[cmp.length];
	for (int i=0;i<cnj.length;i++){
		cnj[i]=cmp[i].conjugate();
		cmp[i]=cmp[i].multiply(cnj[i]);
		res[i]=(double)cmp[i].real();
	}
    return res;

    }


public static double[] SubSpectrum(double[] dat, int index, int len){

	//extract an array of length len starting from index
	double[] dbl=JSci.maths.ArrayMath.extract(index,(index+len),dat);
	Complex[] cmp=JSci.maths.FourierMath.transform(dbl);
	Complex[] cnj=new Complex[cmp.length];
	double [] res=new double[cmp.length];
	for (int i=0;i<cnj.length;i++){
			cnj[i]=cmp[i].conjugate();
			cmp[i]=cmp[i].multiply(cnj[i]);
			res[i]=(double)cmp[i].real();
	}
	return res;
}




public double[] createSine(int len, double period){
	double pi=3.141;
	double [] tmp=new double[len];
	for (int i=0;i<len;i++){
		tmp[i]=Math.sin(2*pi*(1/period)*i);
	}
	return tmp;

}

public void print(double[] dat1, double[] dat2){
	for (int i=0;i<dat2.length;i++){
		System.out.println(i+" "+dat1[i]+" "+dat2[i]);
	}
}


public int poweroftwo(double [] dat){
	int len=dat.length;
	int setlengthto=0;
	boolean notfound=true;
	int first=2;
	int second=4;
	do{

	  if ((len>=first)&&(len<second)){
		setlengthto=first;
		notfound=false;
	  }
	  first=second;
	  second=second*2;
	}while(notfound);
	return setlengthto;

}

public static void main(String[] args){
	PowerSpectrum ps=new PowerSpectrum();
	double[] dat=ps.createSine(512,20);
	float[] datf=JSci.util.ArrayCaster.toFloat(dat);
	FastDaubechies2 deb=FastDaubechies2();
	deb.transform(datf);
	double[] datd=JSci.util.ArrayCaster.toDouble(datf);
	//double[] spc=ps.SubSpectrum(dat,0,255);
	ps.print(dat,datd);




}


}