package gvdecoder.trace;
import java.util.ArrayList;
/**
 allow 2 pts at dif x_vals to be compared


**/
public class Detector{
int start_range;
int end_range;
int MinTimeBetweenEvents=1;
Trace trace;
ArrayList ds;

public Detector(Trace trace,
		int start_range,
		int end_range,
		int MinTimeBetweenEvents){
 this.trace=trace;
 this.start_range=start_range;
 this.end_range=end_range;
 this.MinTimeBetweenEvents=MinTimeBetweenEvents;
 ds=new ArrayList();
}


public void add(int pt1,
		int pt2,
		int paramtype,
		double minval,
		double maxval){
ds.add(new DetectCompare (trace,pt1,pt2,paramtype,minval,maxval));

}


public ArrayList find(){
 //for each xval
 ArrayList ar=new ArrayList();
 for (int i=start_range;i<end_range;i++){
 //for each conditionpair
 boolean trigger_event=true;
 for (int k=0;k<ds.size();k++){
   //&&
   DetectCompare dp= (DetectCompare)ds.get(k);
   if (dp.check(i)==false) {
       trigger_event=false;
       break;
      }
   }//checked all conditions
    //if still true, all comparisons were true
    if (trigger_event) {
     ar.add(new Integer(i));
     i+=MinTimeBetweenEvents;
     }
  }//advance to next pt
 if (ar.size()==0) return null;
 else return ar;
 }//end find





}



class DetectCompare{


 Trace trace;
 double x_val;
 int relative_x_1;
 int relative_x_2;
 int paramtype=0;//0=raw,1=norm, 2=trans;
 double minValue;
 double maxValue;

 public DetectCompare(Trace trace,
 			int p1,
 			int p2,
 			int paramtype,
 			double minValue,
 			double maxValue){
  this.relative_x_1=p1;
  this.relative_x_2=p2;
  this.trace=trace;
  this.paramtype=paramtype;
  this.minValue=minValue;
  this.maxValue=maxValue;
 }

 public boolean check(int index){

  boolean assert_trigger=false;
  double val1=getFromTrace(index+relative_x_1);
  double val2=getFromTrace(index+relative_x_2);
  if ( ((val2-val1)>minValue) && ((val2-val1)<maxValue) ) assert_trigger =true;
  return assert_trigger;
  }

 public double getFromTrace(int index){
  double val=0;
   switch  (paramtype){
    case 0: val=trace.getRaw(index); break;
    case 1: val=trace.getNorm(index); break;
    case 2: val=trace.getTrans(index); break;
    }
   return val;
 }


}