package gvdecoder;
import gvdecoder.prefs.*;

public class ViewerParams{



public volatile boolean AutoLoadLastRois=false;        //loads
public volatile boolean AutoProcessLoadedRois=false;   //process' them to create Chart
public volatile boolean AutoSaveChartAsNavigator=false;//save this chart as the navigator graph
public volatile boolean AutoLoadLastFrameNumber=false;
public volatile boolean AutoTrackFrameNumber=false;
public volatile boolean UseScrollPane=false;
public volatile int     StartingFrameNumber=1;
public volatile int     DividerLocation=100;
public volatile int 	InitialWidth=500;
public volatile int 	InitialHeight=250;
public volatile int		InitialX=10;
public volatile int 	InitialY=10;
public volatile int     InitialFrame=1;



private ViewerParams(){
}



public static ViewerParams np;
public static ViewerParams getInstance(){
 if (np==null){
  np=new ViewerParams();
  PrefManager.getInstance().register("viewer", np);
 }
return np;
}






}