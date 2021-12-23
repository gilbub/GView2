package gvdecoder;
import java.util.*;
import gvdecoder.array.doubleArray3D;
public class APfinder_5j{

 public Matrix tmp;
 public Matrix m_in;
 public int APD;
 public int uD;
 public int pD;
 public int minP,maxP;
 public double[] apShape;
 public int apStart;
 public double[] lastconvolve;


 public APfinder_5j(Matrix ma, int apd, int upstrokeduration, int plateauduration, int minperiod, int maxperiod ){
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


public void doAllTraces(Matrix m_out,int minp, double threshold){
 for (int y=0;y<m_in.ydim;y++){
	 if (y%10==0) System.out.println("trace col="+y);
	 for (int x=0;x<m_in.xdim;x++){
	  double[] conv=findInTrace(y,x);
	  double[] lm=findLocalMaxima(conv,minp,threshold);
	  for (int z=0;z<m_out.zdim;z++){
		  m_out.dat.set(z,y,x,lm[z]);
	  }
   }
 }

}


public void clean(Matrix m_in, Matrix m_out, double spikethreshold, double ca_threshold, int radius, int R,boolean iterate){
// suppress isolated spikes
Matrix frame=new Matrix(1,m_in.ydim,m_in.xdim);
double val=0;

for (int z=1;z<m_in.zdim-1;z++){
	if (z%10==0) System.out.println("iterating "+z);
	for (int y=0;y<m_in.ydim;y++){
		for (int x=0;x<m_in.xdim;x++){
			double v=m_out.dat.get(z-1,y,x);
			if (v>0) m_out.dat.set(z,y,x,v-1);
			if (m_out.dat.get(z,y,x)==0){
				double count_0=ratioNeighboursOverThreshold(m_in.dat,z,y,x,radius,10);
				double countm1=ratioNeighboursOverThreshold(m_in.dat,z-1,y,x,radius,10);
				if (count_0+countm1>spikethreshold){
					m_out.dat.set(z,y,x,R);
				}//if over thresh
			   }//if not zero

			}//x
		}//y
  if (iterate){
	frame.dat.set(0);
	for (int y=0;y<m_in.ydim;y++){
		for (int x=0;x<m_in.xdim;x++){
			if (m_out.dat.get(z,y,x)==0){
				if (ratioNeighboursOverThreshold(m_out.dat,z,y,x,radius,R)>ca_threshold) frame.dat.set(0,y,x,R);
			}
	  }
    }


   for (int y=0;y<m_in.ydim;y++){
	   for (int x=0;x<m_in.xdim;x++){
		   val=frame.dat.get(0,y,x);
		   if (val>0)m_out.dat.set(z,y,x,val);
	   }
   }
 }
}//z

}//clean

double[][] mask;
public double distance(int x1, int y1, int x2, int y2){
	return Math.sqrt((x1-x2)*(x1-x2)+(y1-y2)*(y1-y2));
}

public double[][] makemask(int r){
	int d=r*2+1;
	mask=new double[d][d];
	int c=r+1;

	for (int x=0;x<d;x++){
		for (int y=0;y<d;y++){
			double di=distance(c,c,x,y);
			if (di>0.0) mask[x][y]=1.0/di;
		}
	}
	return mask;
}
public double countNeighboursOverThreshold(doubleArray3D da,int z, int y, int x, int r, double val){
	double sum=0;
	int ybegin=y-r;
	int yend=y+r;
	int xbegin=x-r;
	int xend=x+r;
	int index;
	if ((mask==null)||(mask.length!=(r*2+1))){
		mask=makemask(r);
	}
	if (ybegin<0) ybegin=0;
	if (xbegin<0) xbegin=0;
	if (yend>=da.ydim) yend=da.ydim-1;
	if (xend>=da.xdim) xend=da.xdim-1;
	for (int yy=ybegin;yy<=yend;yy++){
		for (int xx=xbegin;xx<=xend;xx++){
		  index=z*da.frame+yy*da.xdim+xx;
		  if (da.arr[index]>=val) sum+=mask[yy-y+r][xx-x+r];
		}
	}
	return sum;
}


public double ratioNeighboursOverThreshold(doubleArray3D da,int z, int y, int x, int r, double val){
	double sum=0;
	double non=0;
	int ybegin=y-r;
	int yend=y+r;
	int xbegin=x-r;
	int xend=x+r;
	int index;
	if ((mask==null)||(mask.length!=(r*2+1))){
			mask=makemask(r);
	}
	if (ybegin<0) ybegin=0;
	if (xbegin<0) xbegin=0;
	if (yend>=da.ydim) yend=da.ydim-1;
	if (xend>=da.xdim) xend=da.xdim-1;
	for (int yy=ybegin;yy<=yend;yy++){
		for (int xx=xbegin;xx<=xend;xx++){
		  index=z*da.frame+yy*da.xdim+xx;
		  if (da.arr[index]>=val) sum+=mask[yy-y+r][xx-x+r];
		  else non+=mask[yy-y+r][xx-x+r];
		}
	}

	if ((non==0)&&(sum==0)) return 0;
	return (sum/(non+sum));
}

int xbegin,xend,ybegin,yend, index;
public void upsample(doubleArray3D d_in, doubleArray3D d_out, int z_in, int z_out, double out_state, double in_state, double thresh, int r){
	int index_in=0;
	int index_out=0;
	for (int y=0;y<d_in.ydim;y++){
		for (int x=0;x<d_in.xdim;x++){
			index_in=z_in*d_in.frame+y*d_in.xdim+x;
			index_out=z_out*d_out.frame+y*d_in.xdim+x;
			if ((d_out.arr[index_out]==out_state)&&(d_in.arr[index_in]==outstate)){
				if (ratioNeighboursEqualThreshold(d_in,z_in,y,x,r,in_state)>=thresh)
				d_out.arr[index_out]=in_state;
			}
		}
	}
}

public double ratioNeighboursEqualThreshold(doubleArray3D d,int z, int y, int x, int r, double val){
	double sum=0;
	double non=0;
	ybegin=y-r;
	yend=y+r;
	xbegin=x-r;
	xend=x+r;
	if (ybegin<0) ybegin=0;
	if (xbegin<0) xbegin=0;
	if (yend>=d.ydim) yend=d.ydim-1;
	if (xend>=d.xdim) xend=d.xdim-1;
	for (int yy=ybegin;yy<=yend;yy++){
		for (int xx=xbegin;xx<=xend;xx++){
		  index=z*d.frame+yy*d.xdim+xx;
		  if (d.arr[index]==val) sum++;
		  else non++;
		}
	}

	if ((non==0)&&(sum==0)) return 0;
	return (sum/(non+sum));
}

}//end

