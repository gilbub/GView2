package gvdecoder;

import gvdecoder.array.PatternMatcher;
import org.python.core.*;
import java.util.Vector;

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
public JythonViewer jv;


public Vector operations;
public boolean COMPILE=false;

public static final int LINEARREGRESSION=0;
public static final int SUBTRACTCONSTANT=1;
public static final int SUBTRACTARRAY=2;
public static final int SUBTRACT=10;
public static final int ADDCONSTANT=3;
public static final int ADDARRAY=4;
public static final int MULTIPLYCONSTANT=5;
public static final int MULTIPLYARRAY=6;
public static final int SCALE=7;
public static final int GET=8;

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
  for (int j=0;j<ma.dat.zdim;j++){
    		vals[j]=ma.dat.get(j,y,x);
    	}
}

//[ma.traces[i].set(ma.traces[i]*[1,1,1,1]-ma.traces[i].linearRegression()) for i in len(ma.traces)]

/** when iterating through many traces, keeps an internal representation to save memory **/

int last_index;
public TraceManager __getitem__(int i){
  if (i==ma.dat.frame) return this;
  if (i>__len__()) throw Py.IndexError("index out of range");

  last_index=i;
  if (COMPILE) {operations.add(new operation(GET,null,0,0,i));return this;}

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
    if (COMPILE) {operations.add(new operation(SUBTRACT,null,0,0,0));return this;}

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
	   for (int z=0;z<opa.res.length;z++){
	    opa.res[z]=opa.res[z]*m[z];
	   }
	 }else{

	 if ((tmp==null) || (tmp.length!=opa.res.length) ) tmp=new double[opa.res.length];
	 for (int i=0;i<tmp.length;i++){
		 tmp[i]=opa.res[i];
	 }

	 double sum=0;
     for (int k=0;k<(ma.zdim-L);k++){
	   sum=0;
	   for (int j=0;j<L;j++){
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
if (COMPILE) {operations.add(new operation(LINEARREGRESSION,null,0,0,0));return this;}

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


public void compile(String arg){
	operations=new Vector();
	COMPILE=true;
	jv.interp.exec(arg);
	COMPILE=false;
}

public void run(int start, int stop){

 int c,k,o;
 COMPILE=false;
 double max,min,val;
 for (c=start;c<stop;c++){
   System.out.println("in run, "+c);
   int opsize=operations.size();
   for (o=0;o<opsize;o++){
	   operation op=(operation)operations.elementAt(o);
	   switch(op.optype){
		   case LINEARREGRESSION: {linearRegression(); break;}
		   case GET: {__getitem__(c); break;}
		   case SUBTRACT: {__sub__(this); break;}

           }//switch
         }//operations
   __setitem__(c,this);
 }
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

}


public double find(int index, String regex){

	if (!matchstring.equals(regex)){
     matchstring=regex;
     pm.compile(matchstring);
     System.out.println("compiled regex");
	}
    opresult opa=(opresult)stack.pop();
    return pm.find(index,opa.res);
}


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

public void set(int y, int x,double[] vals){
  for (int z=0;z<vals.length;z++) ma.dat.set(z,y,x,vals[z]);
}


public void set(int i, TraceManager tr){
	__setitem__(i,tr);

}


public void set(TraceManager tr){
	__setitem__(last_index,tr);
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

class operation{
  double[] res;
  double a;
  double b;
  int index;
  int optype;
  public operation(int optype, double[] res, double a, double b, int index){
	  this.optype=optype;
	  this.res=res;
	  this.a=a;
	  this.b=b;
	  this.index=index;
     }

}







