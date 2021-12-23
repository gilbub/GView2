package gvdecoder;
import gvdecoder.prefs.*;

public class NavigatorGraphParams{



public volatile boolean AutoLoadLastChartRange=false;
public volatile boolean AutoLoadLastChartCursors=false;
public volatile boolean LoadCursorsOnUserInitiatedLoad=false;
public volatile boolean AutoLoadLastFrameNumber=false;
public volatile boolean AutoTrackFrameNumber=false;
public volatile boolean AutoTrackXRange=false;
public volatile boolean FlipData=false;
public volatile int     StartingFrameNumber=1;
public volatile int  	InitialX=10;
public volatile int 	InitialY=100;
public volatile int 	InitialWidth=200;
public volatile int 	InitialHeight=150;
public volatile int     ROI_width=4; //this is used for figuring out how large a region around the mouse replace trace uses as a ROI
public volatile boolean UseUnitROI=false;//this is used for determining if replace_trace uses a single pixel or a ROI of width ROI_width;



private NavigatorGraphParams(){
}



public static NavigatorGraphParams np;
public static NavigatorGraphParams getInstance(){
 if (np==null){
  np=new NavigatorGraphParams();
  PrefManager.getInstance().register("NavigatorGraph", np);
 }
return np;
}






}