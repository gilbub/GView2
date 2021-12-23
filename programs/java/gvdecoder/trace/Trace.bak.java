package gvdecoder.trace;

import java.util.prefs.*;
import JSci.maths.*;
import java.util.ArrayList;
/** Trace holds the integer data of the line to be plotted, as
    well as scaling relations   and some data about the raw data
    it scales to. Trace can be made part of TraceGroup,
    which controls x axis information
**/
public class Trace{
//holds various scales for transforming datarr to varr.
int data_ymax= Integer.MIN_VALUE;
int data_ymin= Integer.MAX_VALUE;
int trace_ymax=Integer.MIN_VALUE;
int trace_ymin=Integer.MAX_VALUE;
int length=0;
public TraceParams tp;

public double y_scale =1.0;
public double y_offset=0.0;

double user_Yscale=1.0; //suplied by user to scale data if the values
						//are requested. Not used in actual scaling of the
						//trace, just reporting.

public int start_xrange=0; //user selected indexes to scale to.
public int end_xrange=0;   //user selected indexes to scale to.

boolean isSelected=false;
boolean debug=true; //prints status on command line.


public int[] y_values; //contains a copy of the scaled data.

public int[] raw_y;            //is a pointer to the raw data.
public double[] normalized_y;  //local value of normalized data
public double[] absolutenormalized_y;
public double[] transformed_y; //transformed data
public double[] pointer;       //this points to either normalized_y or transformed_y
public String name; 				   //this can be used to identify the trace

/** constructor takes an array of data for x and for y values, then calculates the
    max and min of this data for future use **/
public Trace( int[] raw_y){

	tp=TraceParams.getInstance();
 	this.raw_y=raw_y; //pointers to the original data
 	length=raw_y.length;

 	y_values=new int[length];
    normalized_y=new double[length];
    transformed_y=new double[length];

 	int yval=0;
 	if (tp.FlipData){
		 for (int k=0;k<length;k++){
		 raw_y[k]= (-1)*raw_y[k];
		}
	}
 	for (int i=0;i<length;i++){
	//determine max and min of raw data.
		 yval=raw_y[i];
		 if (yval>data_ymax) data_ymax=yval;
		 if (yval<data_ymin) data_ymin=yval;
	}//end for

    for (int i=0;i<length;i++){
	   normalized_y[i]=raw_y[i]-data_ymin;
	   normalized_y[i]=normalized_y[i]/(data_ymax-data_ymin);
	   transformed_y[i]=normalized_y[i];
	   pointer=normalized_y;
	}

    //if (debug) System.out.println("NavigatorGraph.trace: data_ymax="+data_ymax+" data_ymin="+data_ymin);
   }//end constructor.




public void absoluteNormalize(int min, int max){
	if (absolutenormalized_y==null){
		absolutenormalized_y=new double[length];
	    for (int i=0;i<length;i++){
		   absolutenormalized_y[i]=raw_y[i]-min;
		   absolutenormalized_y[i]=absolutenormalized_y[i]/(max-min);
	   }

	}
	pointer=absolutenormalized_y;

}

public Trace subTrace(int start, int end){
	int[] sub=JSci.maths.ArrayMath.extract(start,end,raw_y);
	Trace newtrace=new Trace(sub);
	newtrace.normalized_y=JSci.maths.ArrayMath.extract(start,end,normalized_y);
	newtrace.transformed_y=JSci.maths.ArrayMath.extract(start,end,transformed_y);
	if (pointer==normalized_y) newtrace.pointer=newtrace.normalized_y;
	  else newtrace.pointer=newtrace.transformed_y;

   return newtrace;

}

/** finds the max and min values between a defined start and end xrange.
    used for global scaling in a tracegroup
**/


public double[] findMaxMin(int start_xrange, int end_xrange){
    	double local_ymax=Double.MIN_VALUE;
		double local_ymin=Double.MAX_VALUE;
		for (int i=start_xrange;i<end_xrange;i++){
		  double yval=pointer[i];
		  if (yval>local_ymax) local_ymax=yval;
		  if (yval<local_ymin) local_ymin=yval;
    }
double [] result=new double[2];
result[0]=local_ymin;
result[1]=local_ymax;
return result;
}

public int[] findAbsoluteMaxMin(int start_xrange, int end_xrange){
    	int local_ymax=Integer.MIN_VALUE;
		int local_ymin=Integer.MAX_VALUE;
		for (int i=start_xrange;i<end_xrange;i++){
		  int yval=raw_y[i];
		  if (yval>local_ymax) local_ymax=yval;
		  if (yval<local_ymin) local_ymin=yval;
    }
int [] result=new int[2];
result[0]=local_ymin;
result[1]=local_ymax;
return result;
}


/** scales the y and x values of its internal data set to
    fit within min_yval and max_yval, for the range of
    values starting at start_xrange and ending at end_xrange **/

public void scale(int start_xrange, /*starting x index of raw data*/
				  int end_xrange,	/*ending  x index of raw data*/
				  int min_yval, 	/*start target y val*/
				  int max_yval		/*end target y val*/
				  ){

	System.out.println("debug.... in scale(,,,,,);");
//find the maxima and minima of the raw data within the ranges given
	this.start_xrange=start_xrange;
	this.end_xrange=end_xrange;

    //here, we should decide whether to use local scaling or global scaling.
    double local_ymax=Double.MIN_VALUE;
	double local_ymin=Double.MAX_VALUE;
    if (tp.Absolute){
		//why this doenst work. Im finding max/min in absolute values, and scaling already normalized data
		absoluteNormalize((int)tp.ScaleMinValue, (int)tp.ScaleMaxValue);
		local_ymax=1.0;
		local_ymin=0;
    }
    else{//use local scaling
      pointer=normalized_y;

	for (int i=start_xrange;i<end_xrange;i++){
	  double yval=pointer[i];
	  if (yval>local_ymax) local_ymax=yval;
	  if (yval<local_ymin) local_ymin=yval;
     }
    }
    System.out.println("NavigatorGraph.trace.scale: local_ymax="+local_ymax+" local_ymin="+local_ymin);
    //the scale and offset depend on min_yval and max_yval
    double ydatarange=local_ymax-local_ymin;
    double yreqrange =max_yval-min_yval;
    y_scale= yreqrange/ydatarange;

    //the offset is calculated based on where the minimum datapoint is, and sets this to
    //the min point.
    int minpt=0;
    if (tp.UseMedianForScaling){
	  minpt=(int)(JSci.maths.ArrayMath.median(pointer)*y_scale);
	}else
      minpt=(int)((double)local_ymin*y_scale);

    //offset is added to each scaled val, so calculate accordingly
    y_offset=min_yval-minpt;

    //if (debug) System.out.println("NavigatorGraph.trace.scale: minpt="+minpt);
    //if (debug) System.out.println("NavigatorGraph.trace.scale: y_offset="+y_offset+" y_scale="+y_scale);
    //if (debug) printYValues();
    reCalculateYValues();

}


/** based on mouse movements, if the mouse moved from oldloc to new loc
    move the trace accordingly **/

public void shiftOffset(int oldloc, int newloc){
	y_offset+=(newloc-oldloc);
	reCalculateYValues();
}

/** arbitrary method for adjusting the scale based on the distance a
	mouse is moved. **/
public void adjustScale(int oldloc, int newloc){
	//arbitrary rule for translating scale.
	double scalefactor=1.0+(double)(newloc-oldloc)/100;
	//first, remember the location of the first scaled point
	int oldypos=y_values[start_xrange];
	//modify the scale
	y_scale=y_scale*scalefactor;
	//the value of the new point is now...
	int newypos=(int)(pointer[start_xrange]*y_scale+y_offset);
	//want to reset y_offset so that newypos=oldypos
	y_offset=y_offset-(newypos-oldypos);

	reCalculateYValues();

}

public void reCalculateYValues(){
 System.out.println("debug - calculating y values with y_scale="+y_scale+" y_offset="+y_offset);
 for (int i=0;i<length;i++){
  y_values[i]=(int)((double)pointer[i]*y_scale+y_offset);
 }
 //printYValues();
}




public void reCalculateTransformations(){
	//System.out.println("values: \n Displaytransformation="+tp.DisplayTransformation+"\n subtractmedian="+tp.SubtractMedian+" , "+tp.SubtractMedianWindow);

	if (!tp.DisplayTransformation){
		 pointer=normalized_y;

	 }
	  else{
	 if (tp.SubtractMedian){
		transformed_y=JSci.maths.EngineerMath.runningMedian(normalized_y,tp.SubtractMedianWindow);
		transformed_y=JSci.maths.ArrayMath.subtract(normalized_y,transformed_y);
		pointer=transformed_y;

	 }
	 if (tp.Median){
		transformed_y=JSci.maths.EngineerMath.runningMedian(transformed_y,tp.MedianWindow);
		pointer=transformed_y;
	 }
	 if (tp.Average){
		transformed_y=JSci.maths.EngineerMath.runningAverage(transformed_y,tp.AverageWindow);
		pointer=transformed_y;
       }


    }//end else.
    reCalculateYValues();
}

public int getZero(){
	return y_values[start_xrange];

}

public int Size(){
	return length;
}

public void printYValues(){
	//for debugging only
	System.out.println("\n debug data out");
	for (int i=0;i<length;i++){
		System.out.println(raw_y[i]+"\t"+y_values[i]+"\t"+pointer[i]);
	 }
}

public double getRaw(int index){ return (double)raw_y[index];}
public double getNorm(int index){ return normalized_y[index];}
public double getTrans(int index){ return transformed_y[index];}
public int getScaled(int index){try{ return y_values[index];}catch(Exception e){ return 1;}}
public double getViewed(int index){ return pointer[index];}
public double getInverseFromScaled(double val){ return (val-y_offset)/y_scale;}
public int getLength(){return raw_y.length;}

public static void main(String[] args){

int[] tst={1,1,1,1,1,1,1,1,1,1,5,9,3,10,10,10,10,10,15,19,10,10,10,1,1,1,5,9,1,1,1};



Trace t=new Trace(tst);
Detector dt=new Detector(t,0,20,2);
dt.add(0,2,0,7,15);
ArrayList ar=dt.find();

if (ar!=null){
	for (int i=0;i<ar.size();i++){
		 System.out.println(ar.get(i));

	}

}

}
}