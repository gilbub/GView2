package gvdecoder;

import gvdecoder.array.PatternMatcher;
import org.python.core.*;

/** utility class used for jython scripts **/


public class TraceManager{

public Matrix ma;
public String dbg="";
public java.util.Stack stack;
public double[] presenttrace;
public static int lasty;
public static int lastx;
double rangeMax,rangeMin;
PatternMatcher pm;
String matchstring;

public TraceManager(Matrix ma){
 this.ma=ma;
 presenttrace=new double[ma.zdim];
 stack=new java.util.Stack();
 pm=new PatternMatcher();
 matchstring=new String("");
}


public int __len__(){
 return ma.dat.frame;
}



public void getPresentTrace(double[] vals, int y, int x){
  double v;
  for (int j=0;j<ma.dat.zdim;j++){
    		v=ma.dat.get(j,y,x);
    		vals[j]=v;
    		presenttrace[j]=v;
    	}
}

//[ma.traces[i].set(ma.traces[i]*[1,1,1,1]-ma.traces[i].linearRegression()) for i in len(ma.traces)]

/** when iterating through many traces, keeps an internal representation to save memory **/

int last_index;
public TraceManager __getitem__(int i){
  if (i==ma.dat.frame) return this;
  if (i>__len__()) throw Py.IndexError("index out of range");
  last_index=i;
  int y=(int)((double)i/ma.xdim);
  int x=i-y*ma.xdim;
  double[] vals=new double[ma.zdim];
  getPresentTrace(vals,y,x);
  stack.push(new opresult(vals));
  //dbg+=" get "+i;
  return this;
}


public void __setitem__(int i, TraceManager tr){
	if (i>ma.xdim*ma.ydim) throw Py.IndexError("index out of range");
	int y=(int)((double)i/ma.xdim);
	int x=i-y*ma.xdim;
	if (this != tr){
		//dbg+="(ext = tracemanager)";
		//tr.stack is empty?
		if (tr.stack.size()==0){
		 for (int z=0;z<ma.zdim;z++){
			 ma.dat.set(z,y,x,tr.ma.dat.get(z,y,x));
		 }
	    }
		else{
	     opresult op=(opresult)tr.stack.pop();
         for (int z=0;z<ma.zdim;z++) ma.dat.set(z,y,x,op.res[z]);
		}
	   tr.stack.clear();
	  }else{
	  if (stack.size()==0) {
	   //dbg+="(empty stack return)";
	   return;
	  }
      //dbg+="(internal = tracemanager)";
      opresult op=(opresult)stack.pop();
      for (int z=0;z<ma.zdim;z++) ma.dat.set(z,y,x,op.res[z]);
      tr.stack.clear();
      }
}

public void __setitem__(int i, double j){
    if (i>ma.xdim*ma.ydim) throw Py.IndexError("index out of range");
    int y=(int)((double)i/ma.xdim);
    int x=i-y*ma.xdim;
    dbg="(internal set integer to tracemanager - constant)";
    for (int z=0;z<ma.zdim;z++) ma.dat.set(z,y,x,j);
}

public void __setitem__(int i, double[] js){
    if (i>ma.xdim*ma.ydim) throw Py.IndexError("index out of range");
    int y=(int)((double)i/ma.xdim);
    int x=i-y*ma.xdim;
    dbg="(internal set integer to tracemanager - array)";
    for (int z=0;z<js.length;z++) ma.dat.set(z,y,x,js[z]);
}


/*
public void store(){
 lastresult=new double[ma.zdim];
 for (int z=0;z<ma.zdim;z++){
	lastresult[z]=presenttrace[z];
 }
 if (results==null) results=new Vector();
 results.add(lastresult);
}
*/

public void init(opresult op){
 if ((op.res==null)||(op.res.length!=ma.zdim))
  op.res=new double[ma.zdim];
}

/*
 returns all the crossing points of the array over threshold
*/
java.util.Vector threshcalc=null;
public  int[] __rshift__(double threshold){
  if (threshcalc==null) threshcalc=new java.util.Vector();
   else threshcalc.clear();

  opresult op=(opresult)stack.pop();
  for (int z=0;z<op.res.length-1;z++) {
	  if ((op.res[z+1]>=threshold)&&(op.res[z]<threshold)){
	    threshcalc.add(new Integer(z));
	}
  }
  int[] res=new int[threshcalc.size()];
  for (int i=0;i<res.length;i++){

	  res[i]=((Integer)threshcalc.elementAt(i)).intValue();
  }
 return res;
}


public  int[] __lshift__(double threshold){
  if (threshcalc==null) threshcalc=new java.util.Vector();
   else threshcalc.clear();

  opresult op=(opresult)stack.pop();
  for (int z=0;z<op.res.length-1;z++) {
	  if ((op.res[z+1]<=threshold)&&(op.res[z]>threshold)){
	    threshcalc.add(new Integer(z));
	}
  }
  int[] res=new int[threshcalc.size()];
  for (int i=0;i<res.length;i++){

	  res[i]=((Integer)threshcalc.elementAt(i)).intValue();
  }
 return res;
}



public TraceManager __mul__(double m){
   //ok to do this 'in place'.
  opresult op=(opresult)stack.pop();
  for (int z=0;z<op.res.length;z++) op.res[z]=op.res[z]*m;
  stack.push(op);
  //dbg+="(internal multiply constant)";
  return this;
}

public TraceManager __div__(double m){
	if (m==0) throw Py.IndexError("division by zero in section multiply");
	//dbg+="(internal divide constant)";
    return __mul__(1.0/m);
}


public TraceManager __add__(double m){
   //ok to do this 'in place'.
   opresult op=(opresult)stack.pop();
    for (int z=0;z<op.res.length;z++) op.res[z]=op.res[z]+m;
  stack.push(op);
  //dbg+="(internal add constant)";
  return this;
}



public TraceManager __sub__(double m){
	//dbg+="(internal subtract constant)";
	return __add__(m*-1);
}

public TraceManager __sub__(TraceManager tr){
    opresult opb=(opresult)stack.pop();
    opresult opa=(opresult)stack.pop();

	for (int z=0;z<opa.res.length;z++){
		 opa.res[z]=opa.res[z]-opb.res[z];
		}
	stack.push(opa);
	//dbg+="(internal subtract tracemanager)";

	return this;
}

public TraceManager __add__(TraceManager tr){
    opresult opb=(opresult)stack.pop();
    opresult opa=(opresult)stack.pop();

	for (int z=0;z<opa.res.length;z++){
		 opa.res[z]=opa.res[z]+opb.res[z];
		}
	stack.push(opa);
	//dbg+="(internal add tracemanager)";
	return this;
}



double[] tmp;
public TraceManager __mul__(double[] m){
	 opresult opa=(opresult)stack.pop();

	 int L=m.length;
	 int offset=L/2;
	 if (L>ma.zdim) throw Py.IndexError("index out of range");
	 if (L==ma.zdim){
	   for (int z=0;z<presenttrace.length;z++){
	    opa.res[z]=opa.res[z]*m[z];
	   }
	 }else{

	 if ((tmp==null) || (tmp.length!=presenttrace.length) ) tmp=new double[presenttrace.length];
	 for (int i=0;i<tmp.length;i++){
		 tmp[i]=opa.res[i];
	 }

	 double sum=0;
     for (int k=0;k<ma.zdim-L;k++){
	   sum=0;
	   for (int j=0;j<m.length;j++){
		sum+=tmp[k+j]*m[j];
	   }
	   opa.res[k+offset]=sum/m.length;
       }
     }
    stack.push(opa);
    //dbg+="(internal convolve)";

    return this;
}

public double[][] ts;

public TraceManager linearRegression(){
	/*
int i;
	if (ts==null) {
		ts=new double[2][ma.zdim];
		for ( i=0;i<ma.zdim;i++){
		ts[0][i]=(double)i;
		}
	}

  opresult opa=(opresult)stack.pop();
  for (i=0;i<ma.zdim;i++){
	  ts[1][i]=opa.res[i];
  }

  JSci.maths.DoubleVector dv= JSci.maths.LinearMath.linearRegression(ts);
  double M=dv.getComponent(1);
  double B=dv.getComponent(0);

  for (i=0;i<ma.zdim;i++){
	  opa.res[i]=M*i+B;
  }
  stack.push(opa);
//  //dbg+="(linear regression)";
*/
  return this;
}


public TraceManager scale(double low, double high){
double max=Double.MIN_VALUE;
double min=Double.MAX_VALUE;
 int i;
 opresult opa=(opresult)stack.pop();
 for (i=0;i<opa.res.length;i++){
	  if (opa.res[i]>max) max=opa.res[i];
	  if (opa.res[i]<min) min=opa.res[i];
	  }
 for (i=0;i<opa.res.length;i++){
	 opa.res[i]=low+((opa.res[i]-min)/(max-min))*(high-low);
	}
 stack.push(opa);
 return this;

}


public void scale_all(double low, double high){
int c,k;
double max,min,val;
for (c=0;c<ma.dat.frame;c++){
   max=Double.MIN_VALUE;
   min=Double.MAX_VALUE;
   int  j=(int)((double)c/ma.xdim);
   int  i=(int)((double)c%ma.xdim);
   for (k=0;k<ma.zdim;k++){
	  val=ma.dat.get(k,j,i);
	  if (val>max) max=val;
	  if (val<min) min=val;
	  }
 for (k=0;k<ma.zdim;k++){
	 ma.dat.set(k,j,i,(low+((ma.dat.get(k,j,i)-min)/(max-min))*(high-low)));
	}

 }
}

public void detrend_all(){
 detrend_all(0,ma.zdim);
}


public void detrend_all(int startx, int endx){
/*
int c,k;
ts=new double[2][endx-startx];
double M,B,val;
for ( k=startx;k<endx;k++) ts[0][k]=(double)k;

for (c=0;c<ma.dat.frame;c++){
   int  j=(int)((double)c/ma.xdim);
   int  i=(int)((double)c%ma.xdim);

   for (k=startx;k<endx;k++){
	  ts[1][k]=ma.dat.get(k,j,i);
     }
   JSci.maths.DoubleVector dv= JSci.maths.LinearMath.linearRegression(ts);
   M=dv.getComponent(1);
   B=dv.getComponent(0);
   for (k=0;k<ma.zdim;k++){
	 val=ma.dat.get(k,j,i);
	 ma.dat.set(k,j,i,(val-(M*k+B)));
   }
 }
ts=null;
*/
}






/*
 trace[i].find(" ")

*/
public double find(int index, String regex){

	if (!matchstring.equals(regex)){
     matchstring=regex;
     pm.compile(matchstring);
     System.out.println("compiled regex");
	}
    opresult opa=(opresult)stack.pop();
    return pm.find(index,opa.res);
}

/*
public double find(comps[] cps){
	//expand
    opresult opa=(opresult)stack.pop();
 	int L=cps[0].length;
    double t=-1;

    for (int k=0;k<opa.res.length-L;k++){
	   boolean found=true;
	   for (int j=0;j<L;j++){
		if ((cps[1][j]*(opa.res[k+j]-cps[0][j]))>=0) {
			 found=false;
			 break;
	     }
	   }
	   if (found){

	   			 t=(double)k;
	   			 break;
		 }
     }

	return t;
}


public comps[] compile(String regex){

	String[] strs=regex.split(",");
	comp[] comps=new comp[strs.length];
	double[][] arr;
	int tot=0;
    int offset=0;
	int i;
	for (i=0;i<strs.length;i++){
		int op=0;
		double val;
		int repeat;
		char c=strs[i].charAt(0);
		if ((c=='t')||(c=='d')){
		 if (strs[i].charAt(1)=='m'){ //dmax
			String overstring=strs[i].substring(4);
		    over=Integer.parseInt(overstring);
		    comps[i]=new comp(true,true,true,over,offset); //interpolate,dvdt,dvdtmax,range
		 }else{
		  String valstring=strs[i].substring(1);
		  val=Double.parseDouble(valstring);

		  if (c=='t'){
			  comps[i]=new comp(true,false,val,offset);
		  }else
		  if (c=='d'){//for clarity
              comps[i]=new comp(true,true,val,offset);
		  }
		}
	   }//t or d
		else
		if (c=='.'){
		int m=strs[i].indexOf('{');
		int n=strs[i].indexOf('}');
		if (m>0){
			String repstring=strs[i].substring(m+1,n);
		    repeat=Integer.parseInt(repstring);
		  }else repeat=1;


		}
		else
        if ((c=='<')||(c=='>')){
		if (c=='<') op=-1;
		if (c=='>') op=1;
		int m=strs[i].indexOf('{');
		int n=strs[i].indexOf('}');
		if (m>0){
			String valstring=strs[i].substring(1,m);
			val=Double.parseDouble(valstring);
			String repstring=strs[i].substring(m+1,n);
		    repeat=Integer.parseInt(repstring);
		    }
		else {
			String valstring=strs[i].substring(1);
			val=Double.parseDouble(valstring);
			repeat=1;

		}
		comps[i]=new comp(op,val,repeat,offset);
	    offset+=repeat;
	   }//< or >
	 }
   return comps;
}
*/


public double[] __getitem__(PyTuple tup){
  int y=((PyInteger)tup.__getitem__(0)).getValue();
  int x=((PyInteger)tup.__getitem__(1)).getValue();
  dbg="get "+y+" "+x;
  return ma.dat.section(y,x);
}

public void __setitem__(PyTuple tup, double[] vals){
  int y=((PyInteger)tup.__getitem__(0)).getValue();
  int x=((PyInteger)tup.__getitem__(1)).getValue();
  dbg="set "+y+" "+x;
  for (int z=0;z<vals.length;z++) ma.dat.set(z,y,x,vals[z]);
}


public double[] get(int y, int x){
     return ma.dat.section(y,x);

}


public double[] getValue(){
	opresult op=(opresult)stack.peek();
	return op.res;
}

public double[] get(){
  return getValue();
}

public void set(int y, int x,double[] vals){
  for (int z=0;z<vals.length;z++) ma.dat.set(z,y,x,vals[z]);
}


public void set(int i, TraceManager tr){
	__setitem__(i,tr);

}


public void set(TraceManager tr){
	__setitem__(last_index,tr);
 }

public int toString_length=20;
public String toString(){
	String str="";
	if (!stack.empty()){
	 opresult op=(opresult)stack.peek();
	 tmp=op.res;
	 int displen=tmp.length;
	 if (displen>toString_length) displen=toString_length;

	 for (int i=0;i<displen;i++){
	   str+=" "+tmp[i];
	 }

    }
    else{
	  int y=(int)((double)last_index/ma.xdim);
      int x=last_index-y*ma.xdim;
      int displen=ma.zdim;
	  if (displen>toString_length) displen=toString_length;
      for (int i=0;i<displen;i++){
	   str+=" "+ma.dat.get(i,y,x);
	 }
	}
     return str;
	}

}

class opresult{
 int y;
 int x;
 double[] res;


 public opresult(int y, int x){
		 this.y=y;
	 this.x=x;
 }

 public opresult(double[] vals){
  this.res=vals;
  }


 public opresult(int zdim){
	 res=new double[zdim];
 }

}







