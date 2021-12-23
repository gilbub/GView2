package gvdecoder.trace;

import java.util.*;
import java.io.*;
import gvdecoder.trace.*;
import java.awt.*;

public class DetectorManager{

ArrayList taggroups;
String filename;
TraceGroup tg;


boolean foundSelectedTag=false;
boolean foundSelectedMaxRect=false;
boolean foundSelectedMinRect=false;
TagGroup selectedTagGroup=null;
Trace selectedTrace=null;
TraceGroup selectedTraceGroup=null;
DetectorTag selectedDetectorTag=null;




private DetectorManager(){

this.filename="";
taggroups=new ArrayList();
}


/**
given a trace, determines which tagroup it belongs to and adds the tag,
else creates a new taggroup.
**/
public void addTag(Trace trace, int tracenumber, int pos, int baseoffset){
	//adds a tag to the appropriate taggroup
	boolean foundgroup=false;
	for (int i=0;i<taggroups.size();i++){
	 TagGroup tg=(TagGroup)taggroups.get(i);
	 if (tg!=null){
	 if (tg.EventTrace.equals(trace)){
	   tg.addTag(pos,baseoffset);
	   foundgroup=true;
	   break;
	   }
      }
	 }
	 if (!foundgroup){//create a new group for this trace.
	  TagGroup tg=new TagGroup(filename, trace, tracenumber, Color.blue);
	  tg.addTag(pos,baseoffset);
	  taggroups.add(tg);
	  }

	System.out.println("DetectorManager:added tag at"+pos);
	}


public TagGroup returnTagGroup(Trace trace){
	TagGroup found_tg=null;
	boolean foundgroup=false;
	try{
		for (int i=0;i<taggroups.size();i++){
		 TagGroup tg=(TagGroup)taggroups.get(i);

		 if (tg.EventTrace.equals(trace)){
		   found_tg=tg;
		   selectedTagGroup=tg;
		   selectedTrace=trace;
		   break;
		   }
	   }
	 }
	 catch(Exception e){;}
	return found_tg;
}


public boolean findSelectedTag(TraceGroup tracegroup, Trace trace, int x, int y){
	foundSelectedTag=false;
	foundSelectedMaxRect=false;
	foundSelectedMinRect=false;
	boolean foundTagElement=false;
	TagGroup tg=returnTagGroup(trace);
	if (tg!=null){
	  for (int i=0;i<tg.tags.size();i++){
	   DetectorTag dt=(DetectorTag)tg.tags.get(i);
	   if (dt.SelectRect.contains(x, y)){

		 selectedTagGroup=tg;
		 selectedTrace=trace;
		 selectedTraceGroup=tracegroup;
		 selectedDetectorTag=dt;

		 foundSelectedTag=true;
		 foundTagElement=true;

	   }
	   if (foundTagElement) break;
	 if (dt.maxRect.contains(x,y)){
		 selectedTagGroup=tg;
		 foundSelectedMaxRect=true;
		 foundSelectedMinRect=false;
		 foundTagElement=true;
		 selectedDetectorTag=dt;

	   }
	 else if (dt.minRect.contains(x,y)){
		 selectedTagGroup=tg;
		 selectedDetectorTag=dt;
		 foundSelectedMinRect=true;
		 foundSelectedMaxRect=false;
		 foundTagElement=true;

       }

   }//tg.size
  }//tg!=null
  return foundTagElement;
}

public boolean dragSelected( int x, int y, int offset, double scale){
boolean dragged=true;

if      (foundSelectedMinRect) {selectedDetectorTag.resetUserMaxOffset(y); }
else if (foundSelectedMaxRect) {selectedDetectorTag.resetUserMinOffset(y);}
else if (foundSelectedTag) {
	       int oldPosition=selectedDetectorTag.position.intValue();
	       int newPosition=(int)(x*scale)+offset;
	       selectedDetectorTag.position=new Integer(newPosition);
	       selectedDetectorTag.resetYVal(offset);
		   if (selectedDetectorTag.equals((DetectorTag)selectedTagGroup.tags.get(0)))
		    selectedTagGroup.shiftPosition(newPosition-oldPosition);
          }
else dragged=false;
return dragged;
}


public void paint(Graphics2D g, Trace trace, int x_offset, int x_range, double x_scale, Dimension windowSize, int genericWidth, int height){

	TagGroup tg=returnTagGroup(trace);
	if (tg!=null)
	 tg.paint(g,x_offset,x_range,x_scale,windowSize,genericWidth,height);


}


public void resetPosition(Trace trace, int xpos){
	TagGroup tg=returnTagGroup(trace);
	tg.resetPosition( xpos);
}


public void loadTraces(String filename){
	tg=new TraceGroup();

		 getArrayFromFile gaff=new getArrayFromFile(filename);
		 int[][] td=gaff.returnTransposedArray();
		 tg.length=td[0].length;
		 tg.setXValues(td[0]);
		 for (int q=1;q<td.length;q++){
		  tg.AddTrace(td[q]);
	  }

     //unsure if necissary, but safer this way.
     tg.scaleXY(0,tg.length,0,tg.length,0,1000);
     tg.reCalculateAllTransformations();


}

public int[] runDetection(Trace trace, int  tgnumber, int start, int end){

ArrayList result=new ArrayList();
selectedTagGroup=(TagGroup)taggroups.get(tgnumber);
selectedTagGroup.resetTrace(trace);
for (int i=start;i<end;i++){
 selectedTagGroup.resetPosition(i);
 if (selectedTagGroup.checkAll()) result.add(new Integer(i));
 }
int [] arr = new int[result.size()];
 for (int j=0;j<result.size();j++){
   arr[j]=((Integer)result.get(j)).intValue();
 }

return arr;
}


public void runDetection(TraceGroup tracegroup){

//for each taggroup in taggroups, find the TraceNumber.
//the, go to the tracegroup, and set the traces in the taggroup to the
//traces corresponding to tracenumber.
//then run detection
System.out.println("taggroups size = "+taggroups.size());
for (int i=0;i<taggroups.size();i++){
  TagGroup tg=(TagGroup)taggroups.get(i);
  Trace trace=(Trace)tracegroup.Traces.get(tg.TraceNumber);
  int[] result=runDetection(trace,i,1000,27000);
  for (int j=0;j<result.length;j++)
   System.out.println("Trace Number="+tg.TraceNumber+ " = "+result[j]);
}

}


static private DetectorManager sm_instance;
static public DetectorManager getInstance()
     {
         if ( sm_instance == null ) sm_instance = new DetectorManager();
         return sm_instance;
     }



}