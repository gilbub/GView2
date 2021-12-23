package gvdecoder;
import java.util.*;
import gvdecoder.array.doubleArray3D;
public class WaveFinder{

 public Matrix tmp;
 public Matrix m_in;
 public Matrix conv;
 public Matrix mask;
 public Matrix mact;
 public int minP,maxP;
 public double[] apShape;
 public int apStart;
 public double[] lastconvolve;
 public double[] buff; //store the trace to be convolved (made public for debugging)

 public WaveFinder(Matrix ma){
	 this.m_in=ma;
	 conv=new Matrix(ma.zdim,ma.ydim,ma.xdim);
	 mact=new Matrix(ma.zdim,ma.ydim,ma.xdim);
	 mask=new Matrix(1,ma.ydim,ma.xdim);
	 setShape((int)(ma.ydim/2.0),(int)(ma.xdim/2.0),0,ma.zdim);
 }

 public void setShape(double[] tmp){
	 apShape=tmp;
	 rescale(apShape,-1000,1000);
	 minP=tmp.length;
	 apStart=(int)minP/4;
	 buff=new double[minP];
 }

 public void setShape(int y, int x, int st, int en){
	 double[] tmp=new double[en-st];
	 for (int z=st;z<en;z++) tmp[z-st]=m_in.dat.get(z,y,x);
	 setShape(tmp);
}

 public void populatebuf(int t, int y, int x){
	 //scales betweeen -1000.0 and +1000.0
	 int z;
	 double val=0;
	 int mid=(int)(buff.length/2);
	 for (z=t;z<t+buff.length;z++){
		 if (z-mid<0) val=m_in.dat.get(0,y,x);
		 else if (z-mid>=m_in.zdim) val=m_in.dat.get(m_in.zdim-1,y,x);
		 else val=m_in.dat.get(z-mid,y,x);
		 buff[z-t]=val;
	 }
     rescale(buff,-1000.0, 1000.0);
   }


 public void rescale(double[] buff,double minv, double maxv){
	 double mint=Double.POSITIVE_INFINITY;
	 double maxt=Double.NEGATIVE_INFINITY;
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

 public double[] convolve(int y, int x){
  double [] res=new double[m_in.zdim];
  for (int t=0;t<m_in.zdim;t++){
	 	   populatebuf(t,y,x);
	 	   double mval=0;
	 	   for (int k=0;k<minP;k++){
	 		mval+=buff[k]*apShape[k];
	 	   }
	 	   res[t]=mval;
        }
    lastconvolve=res;
   return res;
}

public int findMaximaIndex(double[] arr){
   double v=arr[0];
   int maxindex=-1;
   for (int i=0;i<arr.length;i++){
	  if (arr[i]>v) {v=arr[i]; maxindex=i;}
	}
	return maxindex;
}

 public int findMaximaIndex(Matrix m, int y, int x){
    double v=m.dat.get(0,y,x);
    int maxindex=-1;
    for (int i=0;i<m.zdim;i++){
 	  if (m.dat.get(i,y,x)>v) {v=m.dat.get(i,y,x); maxindex=i;}
 	}
 	return maxindex;
 }


public int findDvDtMaxIndex(double[] arr, int st, int en){
	 double dvdtmax=Double.NEGATIVE_INFINITY;
	 int maxindex=-1;
	 if (en>arr.length-1) en=arr.length-1;
	 for (int i=st;i<en;i++){
		 double tmp=arr[i+1]-arr[i];
		 if (tmp>dvdtmax) {dvdtmax=tmp;maxindex=i;}
	 }
	 return maxindex;
}

public int findDvDtMaxIndex(int y, int x){
	 double dvdtmax=Double.NEGATIVE_INFINITY;
	 int maxindex=-1;
	 for (int i=0;i<m_in.zdim-1;i++){
		 double tmp=m_in.dat.get(i+1,y,x)-m_in.dat.get(i,y,x);
		 if (tmp>dvdtmax) {dvdtmax=tmp; maxindex=i;}
	 }
	 return maxindex;
}

public double findThresholdCrossingTime(int y, int x, double thr){
	 double dvdtmax=Double.NEGATIVE_INFINITY;
	 for (int i=0;i<m_in.zdim-1;i++){
	   double v2=m_in.dat.get(i+1,y,x);
	   double v1=m_in.dat.get(i,y,x);
       if ((v2>=thr) && (v1<thr)){
       	return (i+(1-(v2-thr)/(v2-v1)));
       }
     }
    return -1;
}


public void convolveAll(){
	int z,y,x;
	for (y=0;y<m_in.ydim;y++){
		 for (x=0;x<m_in.xdim;x++){
		    double[] res=convolve(y,x);
		    for (z=0;z<m_in.zdim;z++) conv.dat.set(z,y,x,res[z]);
		    }
       }
   conv.dat.scale(0,1.0);
}
public void setMask(double threshold){
	mask.set(0);
   int x,y;
   for (y=0;y<conv.ydim;y++){
	   for (x=0;x<conv.xdim;x++){
		   int i=findMaximaIndex(conv,y,x);
		   if (conv.dat.get(i,y,x)>threshold) mask.dat.set(0,y,x,1);
	   }
  }
}


public Matrix collapse(){
  Matrix m_out=new Matrix(1,mact.ydim,mact.xdim);
  for (int z=1;z<mact.zdim;z++){
   for (int y=0;y<mact.ydim;y++){
    for (int x=0;x<mact.xdim;x++){
      if ((mact.dat.get(z-1,y,x)==0)&&(mact.dat.get(z,y,x)>0)&&(m_out.dat.get(0,y,x)==0))
         m_out.dat.set(0,y,x,z);
      }
     }
   }
  return m_out;
  }


public void dilate(int z){
  double val=1;
  double tmpval=-1;
  for (int y=0;y<mact.ydim;y++){
   for (int x=0;x<mact.xdim;x++){
    if (mact.dat.get(z,y,x)==val){
      if (mact.dat.get(z,y,x+1)<=0) mact.dat.set(z,y,x+1,tmpval);
      if (mact.dat.get(z,y,x-1)<=0) mact.dat.set(z,y,x-1,tmpval);
      if (mact.dat.get(z,y-1,x)<=0) mact.dat.set(z,y-1,x,tmpval);
      if (mact.dat.get(z,y+1,x)<=0) mact.dat.set(z,y+1,x,tmpval);
     }
    }
   }
   replace(mact,z,tmpval,val);
  }

public void erode(int z){
  double val=1;
  double tmpval=-1;
  for (int y=0;y<mact.ydim;y++){
   for (int x=0;x<mact.xdim;x++){
    if (mact.dat.get(z,y,x)==0){
      if (mact.dat.get(z,y,x+1)==val) mact.dat.set(z,y,x+1,tmpval);
      if (mact.dat.get(z,y,x-1)==val) mact.dat.set(z,y,x-1,tmpval);
      if (mact.dat.get(z,y-1,x)==val) mact.dat.set(z,y-1,x,tmpval);
      if (mact.dat.get(z,y+1,x)==val) mact.dat.set(z,y+1,x,tmpval);
     }
    }
   }
   replace(mact,z,tmpval,0);
}

public void replace(Matrix ma, int z, double origvalue, double newvalue){
 for (int y=0;y<ma.ydim;y++){
  for (int x=0;x<ma.xdim;x++){
    if (ma.dat.get(z,y,x)==origvalue) ma.dat.set(z,y,x,newvalue);
   }
  }
}

/*convolutionthreshold should be betweeon 0 and 1. How much must each trace be like the AP in order to count as an AP?
  convolveAll should be called first, after an appropriate AP shape is set via setShape
*/
public Matrix getAPTimesDvDtMax(double convolutionthreshold){
	int z,y,x;
	Matrix m_out=new Matrix(1,m_in.ydim,m_in.xdim);
	m_out.set(-1);

 // double maxval=Double.NEGATIVE_INFINITY;
 
// for (x=0;x<conv.dat.arr.length;x++){ if (conv.dat.arr[x]>maxval) maxval=conv.dat.arr[x];}
    if (convolutionthreshold>0) setMask(convolutionthreshold);

  for (y=0;y<conv.ydim;y++){
		for (x=0;x<conv.xdim;x++){
			 if ((convolutionthreshold==0)||(mask.dat.get(0,y,x)>0)){
		      	m_out.dat.set(0,y,x,findDvDtMaxIndex(y,x));
			 }
		   }
	     }

	return m_out;
 }
/*convolutionthreshold should be betweeon 0 and 1. How much must each trace be like the AP in order to count as an AP?
  APThreshold is a standard threshold value.
  convolveAll should be called first, after an appropriate AP shape is set via setShape
*/
public Matrix getAPTimesThreshold(double convolutionthreshold, double APThreshold){
  int z,y,x;
  Matrix m_out=new Matrix(1,m_in.ydim,m_in.xdim);
  m_out.set(-1);
  if (convolutionthreshold>0) setMask(convolutionthreshold);
  for (y=0;y<conv.ydim;y++){
		for (x=0;x<conv.xdim;x++){
	          if ((convolutionthreshold==0)||(mask.dat.get(0,y,x)>0)){
		      	m_out.dat.set(0,y,x,findThresholdCrossingTime(y,x,APThreshold));
			 }
		   }
	     }
	return m_out;
 }

//requires a matrix object with ap start times from one of the two above routines to get going
public Matrix getAPDuration(Matrix APStartTimes, int UpstrokeDuration, double APThresholdFraction){
  int z,y,x;
  Matrix m_out=new Matrix(1,m_in.ydim,m_in.xdim);
  m_out.set(-1);

  for (y=0;y<m_out.ydim;y++){
		for (x=0;x<m_out.xdim;x++){
			  int ts=(int)APStartTimes.dat.get(0,y,x);
	          if (ts>-1){
				    double minval=Double.POSITIVE_INFINITY;
				    double maxval=Double.NEGATIVE_INFINITY;
				    for (z=ts-UpstrokeDuration; z<ts+UpstrokeDuration;z++){
						if ((z>=0)&&(z<m_in.zdim)){
							double v=m_in.dat.get(z,y,x);
							if (v<minval) minval=v;
							if (v>maxval) maxval=v;
						  }
					  }
					 double apdthreshold=(minval+(maxval-minval)*APThresholdFraction);
					 for (int k=ts+UpstrokeDuration;k<m_in.zdim;k++){
						 if ((m_in.dat.get(k-1,y,x)>apdthreshold)&&(m_in.dat.get(k,y,x)<=apdthreshold)){
							 m_out.dat.set(0,y,x,(k-ts));
							 break;
						 }
					 }//k
			     }//ts>0
			 }//x
		 }//y
	 return m_out;
	 }





/* the functions below make more sense for a record with several events in time*/
 public double[] findInTrace(int y, int x){
	  double [] res=convolve(y,x);
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
	_doAllTraces(m_out,probs,minp,threshold);
}

public void _doAllTraces(Matrix m_out, Matrix probs,int minp, double threshold){
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

