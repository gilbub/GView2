package gvdecoder;

import JSci.maths.*;

public class calc{

//public TraceManager tr;

public calc(){
}

//public calc(TraceManager tr){
//	this.tr=tr;
//}

public static double max(double[] tmp){
 return JSci.maths.ArrayMath.max(tmp);
}

public static double min(double[] tmp){
 return JSci.maths.ArrayMath.min(tmp);
}

public static double range(double[] tmp){
 return max(tmp)-min(tmp);
}

public static double max(TraceManager tr){
  return JSci.maths.ArrayMath.max(tr.presenttrace);
}


public static double min(TraceManager tr){
  return JSci.maths.ArrayMath.min(tr.presenttrace);
}

public static double range(TraceManager tr){
 return max(tr)-min(tr);
}


public static double[] runningaverage(double[] tmp, int arg1){
 return JSci.maths.EngineerMath.runningAverage(tmp,arg1);
}

public static TraceManager runningaverage(TraceManager tr, int arg1){
	tr.presenttrace=JSci.maths.EngineerMath.runningAverage(tr.presenttrace,arg1);
	return tr;
}

public static double[] runningmedian(double[] tmp, int arg1){
 return JSci.maths.EngineerMath.runningMedian(tmp,arg1);

}

public static TraceManager runningmedian(TraceManager tr, int arg1){
	tr.presenttrace=JSci.maths.EngineerMath.runningMedian(tr.presenttrace,arg1);
    return tr;
}

public static TraceManager normalize(TraceManager tr){
    double max=JSci.maths.ArrayMath.max(tr.presenttrace);
    double min=JSci.maths.ArrayMath.min(tr.presenttrace);
    double range=max-min;
    if (range==0) range=1;
    for (int k=0;k<tr.presenttrace.length;k++){
     tr.presenttrace[k]=1000*(tr.presenttrace[k]-min)/(range);
  }
   return tr;
}



}