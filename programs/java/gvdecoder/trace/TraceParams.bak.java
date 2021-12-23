package gvdecoder.trace;
import gvdecoder.prefs.*;

public class TraceParams{


public volatile boolean DisplayTransformation=false;
public volatile boolean Normalize=false;
public volatile boolean Median=false;
public volatile boolean Average=false;
public volatile boolean DeNoise=false;
public volatile boolean SubtractMedian=false;
public volatile int SubtractMedianWindow=9;
public volatile int	MedianWindow=5;
public volatile int AverageWindow=5;
public volatile boolean UseMedianForScaling=false;
public volatile boolean StaggerTraces=false;
public volatile boolean FlipData=false;
public volatile boolean Absolute=false;





private TraceParams(){
}



public static TraceParams tp;
public static TraceParams getInstance(){
 if (tp==null){
  tp=new TraceParams();
  PrefManager.getInstance().register("trace", tp);
 }
return tp;
}






}