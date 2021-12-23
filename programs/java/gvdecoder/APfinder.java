package gvdecoder;
import java.util.*;
import gvdecoder.array.doubleArray3D;
public class APfinder{

 public Matrix tmp;
 public Matrix m_in;
 public int minP,maxP;
 public double[] apShape;
 public int apStart;
 public double[] lastconvolve;


 public APfinder(Matrix ma){
	 this.m_in=ma;

 }

 public void setShape(double[] buf){
	 apShape=buf;
	 rescale(apShape,-1000,1000);
	 minP=buf.length;
	 apStart=(int)minP/4;
 }

 public void populatebuf(int t, int y, int x, double[] buff){
	 //scales betweeen -1000.0 and +1000.0
	 int z;
	 for (z=t;z<t+buff.length;z++){
		 buff[z-t]=m_in.dat.get(z,y,x);
	 }
     rescale(buff,-1000.0, 1000.0);
   }


 public void rescale(double[] buff,double minv, double maxv){
	 double mint=Double.MAX_VALUE;
	 double maxt=Double.MIN_VALUE;
	 int z;
	 for (z=0;z<buff.length;z++){
	 		 if (buff[z]>maxt) maxt=buff[z];
	 		 if (buff[z]<mint) mint=buff[z];
	      }
     double scale=(maxv-minv)/(maxt-mint);
     for (z=0;z<buff.length;z++){
		 buff[z]=minv+(buff[z]-mint)*scale;
	 }
 }

 public double[] findInTrace(int y, int x){
	 double [] res=new double[m_in.zdim];
	 double [] buff=new double[minP];
	 for (int t=0;t<m_in.zdim-minP;t++){
	   populatebuf(t,y,x,buff);
	   double mval=0;
	   for (int k=0;k<minP;k++){
		mval+=buff[k]*apShape[k];
	   }
	   res[t+apStart]=mval;

      }
      rescale(res,0,1000);
      lastconvolve=res;
      return res;
     }

double[] APStartTrace;
public double[] findLocalMaxima(double[] arr, int minp,double threshold){
	double sumleft;
	double sumright;
	boolean checkspikes=false;
	int spiketime;
	List<Integer> spikes=new ArrayList<Integer>();
	for (int t=minp;t<arr.length-minp;t++){
		if ((arr[t]>threshold)&&(arr[t]>arr[t-1])&&(arr[t]>arr[t+1])){
	      sumleft=0;
	      sumright=0;
	      for (int a=1;a<minp;a++){
			  sumleft+=arr[t-a];
			  sumright+=arr[t+a];
		 }
		 sumleft=sumleft/(minp-1);
		 sumright=sumright/(minp-1);
		 if ((sumright<arr[t])&&(sumleft<arr[t])){
			 spikes.add(t);
		 }
	     }
	 }

    if ((APStartTrace==null)||(APStartTrace.length!=arr.length))
	       APStartTrace=new double[arr.length];
	for (int x=0;x<arr.length;x++) APStartTrace[x]=0;

    for (int m=0;m<spikes.size();m++) {
        spiketime=spikes.get(m);
		if (spiketime>0){
	      APStartTrace[spiketime]=1;
		  for (int n=spiketime-minp;n<spiketime+minp;n++){
			  if (APStartTrace[n]==1){ // already a spike nearby
				  if (n!=spiketime){
				  if(arr[spiketime]>arr[n]){APStartTrace[n]=0;}
				  else{APStartTrace[spiketime]=0;}
			   }
			  }
		  }
	    }
	}

    return APStartTrace;
}

public void ConvolveAllTraces(Matrix m_in, Matrix m_out){
 for (int y=0;y<m_in.ydim;y++){
	 if (y%10==0) System.out.println("trace col="+y);
	 for (int x=0;x<m_in.xdim;x++){
	  double[] conv= findInTrace(y,x);
	  for (int z=0;z<m_out.zdim;z++){
		  m_out.dat.set(z,y,x,conv[z]);
	  }
   }
 }
}

public void LocalMaximaAllTraces(Matrix m_out, Matrix probs,int minp, double threshold){
	doAllTraces(m_out,probs,minp,threshold);
}

public void doAllTraces(Matrix m_out, Matrix probs,int minp, double threshold){
 for (int y=0;y<m_in.ydim;y++){
	 if (y%10==0) System.out.println("trace col="+y);
	 for (int x=0;x<m_in.xdim;x++){
	  double[] conv=findInTrace(y,x);
	  for (int z=0;z<probs.zdim;z++) probs.dat.set(z,y,x,conv[z]);
	  double[] lm=findLocalMaxima(conv,minp,threshold);
	  for (int z=0;z<m_out.zdim;z++){
		  m_out.dat.set(z,y,x,lm[z]);
	  }
   }
 }

}


}//end

