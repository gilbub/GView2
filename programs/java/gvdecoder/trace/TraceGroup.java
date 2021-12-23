package gvdecoder.trace;

import java.util.ArrayList;
import gvdecoder.prefs.*;

public class TraceGroup{

public gvdecoder.trace.TraceParams tp;
public ArrayList Traces;
public int[] x_values; //contains a copy of the scaled data.
public int[] raw_x;    //is a pointer to the raw data.

int length;

boolean debug=true;
public boolean holdYscale=false;


double x_scale=1.0;
int x_offset=0;

double user_Xscale=1.0; //suplied by user to scale data if the values
			//are requested. Not used in actual scaling of the
			//trace, just reporting.

public TraceGroup(){
 Traces=new ArrayList();
 tp=TraceParams.getInstance();
}

public void setXValues(int[] data){
 this.raw_x=data;
 this.length=raw_x.length;
 x_values=new int[length];
 if (debug) System.out.println("NavigatorGraph.tracegroup: x length="+length);

 reCalculateXValues();
}


public void setXValues(int val){
 this.raw_x=new int[val];
  x_values=new int[val];
 this.length=val;
 for (int i=0;i<val;i++){
  raw_x[i]=i;
  x_values[i]=i;
  }
 reCalculateXValues();

}

public void AddTrace(int[] y_vals){
 Trace trace=new Trace(y_vals);
 Traces.add(trace);
}

public void AddTrace(int index, int[] y_vals){
Trace  trace=new Trace(y_vals);
Traces.add(index,trace);

}



public void AddTrace(Trace trace){
  Traces.add(trace);
}

public void RemoveTrace(int index){
 try{
 Traces.remove(index);
}catch(Exception e){/*do nothing if it cant*/}
}





public double getXScale(){ return x_scale; }

public int getInverse(int val){
int i=0;
	for (i=0;i<raw_x.length-1;i++){
	 if ((raw_x[i]<=val)&&(raw_x[i+1]>=val)) break;
   }
return i;
}

/** Starting from index start_xrange and ending at index end_xrange,
    determines the scale so that
    x_values[start_xrange]=1 (start_xtarget),
    x_values[end_xrange]=end_xtarget
**/
public void scaleX(int start_xrange,
		   int end_xrange,
		   int start_xtarget,
		   int end_xtarget
		   ){
	int offset=0;
	if (end_xrange>=raw_x.length) end_xrange=raw_x.length-1;
	double source_range=raw_x[end_xrange-offset]-raw_x[start_xrange-offset];

	double target_range=end_xtarget-start_xtarget;
	x_scale=target_range/source_range;
	x_offset=raw_x[start_xrange-offset];
	reCalculateXValues();
	}


/** Starting from index start_xrange and ending at index end_xrange,
    scale  x_values so that x_values[start_xrange]=1 (start_xtarget),
    x_values[end_xrange]=end_xtarget (Calls scalex()). Then go
    to each trace, and scale it so that its maxima and minima between
    start_xrange and end_xrange fall between min_yval and max_yval (by
    calling trace.scale()).
**/
public void scaleXY(
				int start_xrange,
	            int end_xrange,
	            int start_xtarget,
	            int end_xtarget,
	            int min_yval,
	            int max_yval
	            ){
//if (debug) System.out.println("NavigatorGraph.tracegroup. start_xrange="+start_xrange +" end_xrange="+end_xrange+" start_xtarget="+start_xtarget+" end_xtarget="+end_xtarget);



    scaleX(start_xrange,end_xrange,start_xtarget, end_xtarget);

   if (holdYscale) return;

    if (!tp.Absolute){
    //scale each trace to its own max and min values.
	for (int i=0;i<Traces.size();i++){
	 ((Trace)Traces.get(i)).scale(start_xrange,end_xrange,min_yval,max_yval);
	 if (tp.StaggerTraces){
		//int stagval=(int)(((double)(max_yval))/(Traces.size()+2)); // leave 2 'spaces' for clarity
	    int stagval=20;
	    int increment=stagval;
	    for (int k=0;k<Traces.size();k++){
		 ((Trace)Traces.get(i)).y_offset+=increment;
		 increment+=stagval;
		}
	  }
	 }
   }//end !tp.Absolute
   else{

	 System.out.println("debug::: rescaling using absolute values....");
	//find the largest max min values for all traces in tracegroup, and scale each trace to that value.
	 double global_ymax=(double)Integer.MIN_VALUE+1;
	 double global_ymin=(double)Integer.MAX_VALUE-1;
	for (int i=0;i<Traces.size();i++){
		Trace tmp=(Trace)Traces.get(i);

		int[] result= tmp.findAbsoluteMaxMin(start_xrange, end_xrange);
		if (result[0]<global_ymin) global_ymin=(double)result[0];
		if (result[1]>global_ymax) global_ymax=(double)result[1];
	 }
	 tp.ScaleMaxValue=global_ymax;
	 tp.ScaleMinValue=global_ymin;
	 for (int i=0;i<Traces.size();i++){
	 ((Trace)Traces.get(i)).scale(start_xrange,end_xrange,min_yval,max_yval);
     }

	}


  }


public void flipdata(){
for (int i=0;i<Traces.size();i++){
		Trace tmp=(Trace)Traces.get(i);
        tmp.flipdata();
	}

}

public void reCalculateXValues(){
 for (int i=0;i<length;i++){
  x_values[i]=(int)((double)(raw_x[i]-x_offset)*x_scale);
 // System.out.println("raw = "+raw_x[i]+" x_values="+x_values[i]);
 }

}



public int[] getYs(int index){

	 return ((Trace)Traces.get(index)).y_values;

}

public Trace getTrace(int index){

   return ((Trace)Traces.get(index));
}

public void reCalculateAllYValues(){
	for (int i=0;i<Traces.size();i++){
	 ((Trace)Traces.get(i)).reCalculateYValues();
 }
}

public void reCalculateAllTransformations(){
	for (int i=0;i<Traces.size();i++){
	 ((Trace)Traces.get(i)).reCalculateTransformations();
 }
}

public void printXValues(){
	//for debugging only
	System.out.println("\n debug data out");
	for (int i=0;i<length;i++){
		System.out.println(i+"\t"+raw_x[i]+"\t"+x_values[i]);
	 }
}

public static void main(String[] args){
 int[] data={10,20,30,40,50,60,70,80,90,100,110,120,130,140,150};

 TraceGroup tg=new TraceGroup();
 tg.setXValues(data);
 tg.printXValues();
 tg.scaleX(5,10,0,50);
 tg.printXValues();



}

}