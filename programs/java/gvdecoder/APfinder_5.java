package gvdecoder;

public class APfinder_5{

 public Matrix tmp;
 public Matrix m_in;
 public int APD;
 public int uD;
 public int pD;
 public int minP,maxP;
 public double[] apShape;
 public int apStart;
 public double[] lastconvolve;


 public APfinder_5(Matrix ma, int apd, int upstrokeduration, int plateauduration, int minperiod, int maxperiod ){
	 this.m_in=ma;
	 this.APD=apd;
	 this.uD=upstrokeduration;
	 this.minP=minperiod;
	 this.maxP=maxperiod;
	 this.pD=plateauduration;
	 apShape=new double[minperiod];
	 apStart=minperiod/2-APD/2;
	 int upstrokeEnd=apStart+uD;
	 int plateauEnd=upstrokeEnd+pD;
	 int apEnd=apStart+APD;
	 for (int i=0;i<minperiod;i++){
		 if (i<apStart) apShape[i]=-1000.0;
		 if ((i>=apStart)&&(i<upstrokeEnd)){
			 int k=i-apStart;
			 apShape[i]=-1000.0+(k*2000.0/uD);
		     }
		 if ((i>=upstrokeEnd)&&(i<plateauEnd)) apShape[i]=1000.0;
	}

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

 public double[] findLocalMaxima(double[] arr, int minp){
   double[] res=new double[arr.length];
   for (int i=0;i<arr.length-minp;i+=minp/4){
	 double maxv=Double.MIN_VALUE;
	 int maxt=0;
	 for (int k=0;k<minp;k++){
		 int t=i+k;
		 if (arr[t]>maxv){
			maxv=arr[t];
			maxt=t;
		    }
	   }
	   res[maxt]=maxv;
	   int lookback=maxt-minp;
	   if (lookback<0) lookback=0;
	   for (int p=lookback;p<maxt;p++){
		   if (res[p]!=0){
			  if (res[p]>maxv)
				 res[maxt]=0;
				 else res[p]=0;

		     }
		   }
    }
   return res;
}


public void doAllTraces(Matrix m_out,int minp){
 for (int y=0;y<m_in.ydim;y++){
	 if (y%10==0) System.out.println("trace col="+y);
	 for (int x=0;x<m_in.xdim;x++){
	  double[] conv=findInTrace(y,x);
	  double[] lm=findLocalMaxima(conv,minp);
	  for (int z=0;z<m_out.zdim;z++){
		  m_out.dat.set(z,y,x,lm[z]);
	  }
   }
 }

}


public void clean(Matrix m_in, Matrix m_out, int spikethreshold, int radius, int R){
// suppress isolated spikes
for (int z=1;z<m_in.zdim-1;z++){
	if (z%10==0) System.out.println("iterating "+z);
	for (int y=0;y<m_in.ydim;y++){
		for (int x=0;x<m_in.xdim;x++){
			double v=m_out.dat.get(z-1,y,x);
			if (v>0) m_out.dat.set(z,y,x,v-1);
			if (m_out.dat.get(z,y,x)==0){
				int count_0=m_in.dat.countNeighboursOverThreshold(z,y,x,radius,10);
				int countm1=m_in.dat.countNeighboursOverThreshold(z-1,y,x,radius,10);
				if (count_0+countm1>spikethreshold){
					m_out.dat.set(z,y,x,R);
				}//if over thresh
			   }//if not zero

			}//x
		}//y


	}//z

  }//clean





}//end

