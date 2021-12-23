package gvdecoder;

import JSci.maths.Complex;

import org.python.core.*;

public class TimeSeries{

 double[] arr;
 public int length;
 public double maxvalue;
 public double minvalue;
 public int maxvalueindex;
 public int minvalueindex;


 public TimeSeries(double[] tmp){
  this.arr=tmp;
  length=tmp.length;
  findRange();
 }

 public TimeSeries(int length){
	 this.length=length;
	 arr=new double[length];
 }

 public TimeSeries(TraceManager tm){
	 this.length=tm.ma.zdim;
	 arr=new double[length];
	 for (int i=0;i<this.length;i++){
    	 arr[i]=tm.presenttrace[i];
  }
}


public void append(double[] newvals){
	double[] tmp=new double[length+newvals.length];
	for (int i=0;i<arr.length;i++) tmp[i]=arr[i];
	for (int j=0;j<newvals.length;j++) tmp[length+j]=newvals[j];
	arr=tmp;
	length=arr.length;
}

public double get(int z){
	return arr[z];
}

public void findRange(){ findRange(0,length);}
//based on index
public void findRange(int start, int end){
	  if (end>length) end=length;
	  if (end<start) end=length;
	  if (start>end) start=0;
	  maxvalue=Double.NEGATIVE_INFINITY;
	  minvalue=Double.POSITIVE_INFINITY;
		  int maxindex=-1;
		  for (int i=start;i<end;i++){
			double v=arr[i];
			if (v>maxvalue) {maxvalue=v; maxvalueindex=i;}
			if (v<minvalue) {minvalue=v; minvalueindex=i;}
			}
}

//based on value
public void findLowHighValues(double lowest, double highest){

	      double lowdif =Double.POSITIVE_INFINITY;
		  double highdif=Double.POSITIVE_INFINITY;
		  for (int i=0;i<length;i++){
			double v=arr[i];
			if ((v>=lowest) &&(v-lowest<lowdif)  ) {lowdif=v-lowest; minvalue=v; minvalueindex=i;}
			if ((v<=highest)&&(highest-v<highdif)) {highdif=highest-v; maxvalue=v; maxvalueindex=i;}
			}
}




public double[] doubles(){return arr;}

public TimeSeries copy(){
	double[] tmp=new double[length];
	System.arraycopy(arr,0,tmp,0,length);
	TimeSeries cp=new TimeSeries(tmp);
    return cp;
}

public TimeSeries copy(int start, int end){
	if (end>length) end=length;
	if (end<start) end=length;
	if (start>end) start=0;
	double[] tmp=new double[end-start];
	System.arraycopy(arr,start,tmp,0,end-start);
	TimeSeries cp=new TimeSeries(tmp);
    return cp;
}

public TimeSeries copy(int start, int end, int step){
	if (step<=1) return copy(start,end);
	if (end>length) end=length;
	if (end<start) end=length;
	if (start>end) start=0;
	int ltmp=(end-start)/step;
	double[] tmp=new double[ltmp];
	for (int i=0;i<ltmp;i++){
		tmp[i]=arr[i*step];
	}

	TimeSeries cp=new TimeSeries(tmp);
    return cp;
}

/*****/
public TimeSeries __getitem__(PySlice slice){
 // if (slice.step!=null)  return copy(((PyInteger)slice.start).getValue(),((PyInteger)slice.stop).getValue(),((PyInteger)slice.step).getValue());
  return copy(((PyInteger)slice.start).getValue(),((PyInteger)slice.stop).getValue());
}



public Double __getitem__(int z){
	if (z>=length) throw Py.IndexError("index out of range");
	return new Double( arr[z] );
}

public void __setitem__(int z, Double val){
	if (z>=length) throw Py.IndexError("index out of range");
	arr[z]=val.doubleValue();
}


public int maxima(int start, int end){
	  if (end>length) end=length;
      if (end<start) end=length;
	  if (start>end) start=0;
	  double maxval=Double.NEGATIVE_INFINITY;
	  int maxindex=-1;
	  for (int i=start+1;i<end-1;i++){
		if ((arr[i]>arr[i-1])&&(arr[i]>arr[i+1])&& (arr[i]>maxval)){
			maxval=arr[i];
			maxindex=i;
		}
	  }
	return maxindex;
}


public int minima(int start, int end){
	  if (end>length) end=length;
      if (end<start) end=length;
	  if (start>end) start=0;
	  double minval=Double.POSITIVE_INFINITY;
	  int minindex=-1;
	  for (int i=start+1;i<end-1;i++){
		if ((arr[i]<arr[i-1])&&(arr[i]<arr[i+1])&& (arr[i]<minval)){
			minval=arr[i];
			minindex=i;
		}
	  }
	return minindex;
}


public double average(){
	double sum=0;
	for (int i=0;i<length;i++){
		sum+=arr[i];
	}
	return sum/length;
}

public double average(int start, int end){
	 if (end>length) end=length;
	 if (end<start) end=length;
     if (start>end) start=0;
     double sum=0;
     for (int i=start;i<end;i++){
	     sum+=arr[i];
     }
     return sum/(end-start);
}

public void scale(){
	scale(0,1.0,0,length);
}


public void scale(double min, double max){
  scale(min,max,0,length);
 }

/*scale all the data, but calculate scale based on a subrange of values*/
public void scale(double min, double max, int st, int en){
  double maxval=Double.NEGATIVE_INFINITY;
  double minval=Double.POSITIVE_INFINITY;
  double v;
  maxvalue=maxval; //used later - these are globals
  minvalue=minval; //used later
  if (st<0) st=0;
  if (en<st) en=st+1;
  if (en>length) en=length;
  for (int i=st;i<en;i++){
	if (arr[i]>maxval) {maxval=arr[i];maxvalueindex=i;}
    if (arr[i]<minval) {minval=arr[i];minvalueindex=i;}
  }
  if (minval==maxval) return;
  double sc=(max-min)/(maxval-minval);
  for (int j=0;j<length;j++){
	v=(arr[j]-minval)*sc+min;
	if (v>maxvalue) maxvalue=v;
	if (v<minvalue) minvalue=v;
	arr[j]=v;
  }
}

public int over(double thresh, int start){
 if (start>=length) start=1;
 if (start==0) start=1;
 for (int i=start;i<length;i++){
   if ((arr[i]>=thresh) && (arr[i-1]<thresh)){
	  return i;
   }
  }
  return -1;
}

public int under(double thresh, int start){
 if (start>=length) start=1;
 if (start==0) start=1;
 for (int i=start;i<length;i++){
   if ((arr[i]<=thresh) && (arr[i-1]>thresh)){
	  return i;
   }
}
  return -1;

}


int _index=-1;
public int nextunder(double thresh){
 if (_index==-1) _index=0;
 for (int i=_index+1;i<length;i++){
   if ((arr[i]<=thresh) && (arr[i-1]>thresh)){
	  _index=i;
	  return i;
   }
  }
  return -1;
}

public int nextover(double thresh){
 if (_index==-1) _index=0;
 for (int i=_index+1;i<length;i++){
   if ((arr[i]>=thresh) && (arr[i-1]<thresh)){
	  _index=i;
	  return i;
   }
}
  return -1;
}


public double period(int start, int end, double minperiod, double maxperiod){
	  if (end>length) end=length;
	  if (end<start) end=length;
	  if (start>end) start=0;
	  int seqlength=end-start;
      TimeSeries dft=new TimeSeries(seqlength);
	  double[] tmp=new double[seqlength];
	  for (int j=0;j<(end-start);j++){
		  tmp[j]=arr[j+start];
	  }
	  Complex[] cmp=dft1d.go(tmp);
	  Complex cnj,cmpi;

	  for (int i=0; i<cmp.length;i++){
		 cnj=cmp[i].conjugate();
	     cmpi=cmp[i].multiply(cnj);
         dft.arr[i]=cmpi.real();
	  }
      double minfreq=((double)seqlength)/maxperiod;
      double maxfreq=((double)seqlength)/minperiod;
      return (seqlength/dft.maxima((int)minfreq,(int)maxfreq));
}



public TimeSeries(int[] tmp){
   arr=new double[tmp.length];
   length=tmp.length;
   for (int i=0;i<length;i++){
     arr[i]=(double)tmp[i];
   }
 findRange();
 }

public int __len__(){
	return length;
}

public TimeSeries __mul__(TimeSeries vals){
  return __mul__(vals.arr);
}



public TimeSeries __imul__(TimeSeries vals){
  return __imul__(vals.arr);
}


public TimeSeries __mul__(double[] vals){
	TimeSeries ts=copy();
	ts.convolve_all(vals);
	return ts;
}

public TimeSeries __imul__(double[] vals){
	convolve_all(vals);
	return this;
}

public TimeSeries __mul__(double val){
	TimeSeries ts=new TimeSeries(length);
	for (int i=0;i<length;i++){
		ts.arr[i]=arr[i]*val;
	}
	ts.findRange();
	return ts;
}

public TimeSeries __imul__(double val){
	for (int i=0;i<length;i++){
		arr[i]=arr[i]*val;
	}
	findRange();
	return this;
}

public TimeSeries __div__(double val){
	TimeSeries ts=new TimeSeries(length);

	if (val!=0){
	for (int i=0;i<length;i++){
		ts.arr[i]=arr[i]/val;
	}
   }
   ts.findRange();
   return ts;
}

public TimeSeries __div__(TimeSeries vals){
  return __div__(vals.arr);
}
public TimeSeries __div__(double[] vals){
	TimeSeries ts=new TimeSeries(length);
	if (vals.length==length){
	for (int i=0;i<length;i++){
		if (vals[i]!=0)
		 ts.arr[i]=arr[i]/vals[i];
	}
   }
    ts.findRange();
	return ts;
}

public TimeSeries __add__(double val){
	TimeSeries ts=new TimeSeries(length);
	for (int i=0;i<length;i++){
		ts.arr[i]=arr[i]+val;
	}
	ts.findRange();
	return ts;
}

public TimeSeries __iadd__(double val){

	for (int i=0;i<length;i++){
		arr[i]+=val;
	}
	findRange();
	return this;
}

public TimeSeries __sub__(double val){
	TimeSeries ts=new TimeSeries(length);
	for (int i=0;i<length;i++){
		ts.arr[i]=arr[i]-val;
	}
	ts.findRange(); //this is really lazy
	return ts;
}

public TimeSeries __isub__(double val){

	for (int i=0;i<length;i++){
		arr[i]-=val;
	}
	findRange();
	return this;
}


public TimeSeries __sub__(TimeSeries vals){
 return __sub__(vals.arr);
}

public TimeSeries __sub__(double[] vals){
	TimeSeries ts=new TimeSeries(length);
	if (vals.length==length){
	for (int i=0;i<length;i++){
		if (vals[i]!=0)
		 ts.arr[i]=arr[i]-vals[i];
	}
   }
    ts.findRange();
	return ts;
}


public TimeSeries __add__(TimeSeries vals){
  return __add__(vals.arr);
}

public TimeSeries __add__(double[] vals){
	TimeSeries ts=new TimeSeries(length);
	if (vals.length==length){
	for (int i=0;i<length;i++){
		if (vals[i]!=0)
		 ts.arr[i]=arr[i]+vals[i];
	}
   }
   ts.findRange();
   return ts;
}

public TimeSeries __iadd__(double[] vals){

	if (vals.length==length){
	for (int i=0;i<length;i++){
		if (vals[i]!=0)
		 arr[i]=arr[i]+vals[i];
	}
   }
    findRange();
	return this;
}

public TimeSeries __isub__(double[] vals){

	if (vals.length==length){
	for (int i=0;i<length;i++){
		if (vals[i]!=0)
		 arr[i]=arr[i]-vals[i];
	}
   }
    findRange();
	return this;
}

int showlength;
public String toString(){
	String res="TimeSeries l="+length+" [";
	showlength=length;
	if (length>=50) showlength=50;
	for (int i=0;i<showlength-1;i++){
		res+=arr[i]+",";
	}
	if (showlength==50)
	 res+=arr[showlength-1]+"...]";
	else res+=arr[showlength-1]+"]";
	return res;
}

public double convolve(int zi, double[] vals, int zoff){

	double sum=0;

	for (int z=0;z<vals.length;z++){
		int index=(zi+z-zoff);
	      if ((index>=0) && (index<length))
                  sum+=arr[index]*vals[z];

	}
  return sum;
}



public void convolve_all(double[] vals){
	int index=0;
	int valzdim=vals.length;
	if ((valzdim>length)) {
		System.out.println("error in  convolve operation, out of bounds");

	}else
	if ((valzdim==length)){
		index=0;
		for (int z=0;z<valzdim;z++){

				arr[index]=arr[index]*vals[z];
				index++;

		}//z

	}//if
	else{
	     int zoff=(int)(((double)vals.length)/2.0);
        double[] tmp=new double[length];
		for (int z=0;z<length;z++){
				tmp[z]=convolve(z,vals,zoff);
				}
		 arr=tmp;

		}
	 findRange();
	}



}