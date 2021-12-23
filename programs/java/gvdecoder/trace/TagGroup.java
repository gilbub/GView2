package gvdecoder.trace;

import java.awt.*;
import java.util.*;

/**
 The idea is that a taggroup is a series of detector tags that
 1) maintain a measurement based on a particular postion of a trace.
 2) can be transposed to any position on any trace and return true or
    false to signify whether that section of the trace is similar  to (1).
The taggroup keeps track of which trace generated the parameters for the tags it
contains. It also knows how to draw each tag relative to some value. For
now, we assume that the first tag is set to 0, and subsequent tags in the group
are drawn relative to tag0. Therefor tag 1-N have only a relative position.
TagGroup then maintains the absolute position of the tags

**/


public class TagGroup{

ArrayList tags;    //contains all the tags;
Trace EventTrace;  //trace snippet that is used to generate the values for detectortags
Trace WholeTrace;  //the trace that contains the EventTrace
Trace PresentTrace;//the trace presently being detected

String filename;  //name of the file that the traces come from.

int TraceNumber;   //the number of the trace in a TraceGroup that this TagGroup is geared toward

public boolean DisplayTags=true;
public boolean DisplayTagsRelative=false;

int absolutePosition; //the position of the first tag when it was created
int relativePosition;

Color color;

public TagGroup(String filename, Trace trace, int traceNumber, Color color){
 this.EventTrace=trace;
 this.WholeTrace=trace;
 this.PresentTrace=trace;
 this.TraceNumber=traceNumber;
 this.filename=filename;
 this.color=color;
 tags=new ArrayList();

}


/*reset for viewing, and also prior to detection, set xpos to 0*/
public void resetPosition(int xpos){
	DetectorTag dt_0=(DetectorTag)tags.get(0);
	int shift=dt_0.position.intValue()-xpos;
	for (int i=1;i<tags.size();i++){
	DetectorTag dt_n=(DetectorTag)tags.get(i);
	dt_n.position=new Integer(dt_n.position.intValue()-shift);
	}
	dt_0.position=new Integer(xpos);
}

public void shiftPosition(int shift){
for (int i=1;i<tags.size();i++){
	DetectorTag dt_n=(DetectorTag)tags.get(i);
	dt_n.position=new Integer(dt_n.position.intValue()+shift);
	}
	if (checkAll()) ((DetectorTag)tags.get(0)).color=Color.green;
	 else ((DetectorTag)tags.get(0)).color=Color.black;

}

public void resetTrace(Trace trace){
//reset the trace in this group
	this.EventTrace=trace;
	this.WholeTrace=trace;
	this.PresentTrace=trace;
	for (int i=0;i<tags.size();i++){
	 DetectorTag dt_n=(DetectorTag)tags.get(i);
	 dt_n.trace=trace;
	 dt_n.detect_trace=trace;
	 dt_n.ref_trace=trace;
   }
}

public boolean checkAll(){
 boolean trigged=true;
   for (int j=1;j<tags.size();j++){
	trigged=((DetectorTag)tags.get(j)).check(0);
	if (!trigged) break;
	 }

	return trigged;
}

public void addTag (int position, int baseoffset){
	DetectorTag tag=new DetectorTag(filename,tags.size(),(double)baseoffset,0.0,0.0,position,color,true);
	tag.trace=EventTrace;
	tag.ref_trace=WholeTrace;
	tag.detect_trace=WholeTrace;

	if (tags!=null){
	  tags.add(tag);
	  tag.base=(DetectorTag)tags.get(0);
	  tag.relative=new Boolean(true);
	  System.out.println("DetectorTag:added tag");
	  }
	 else System.out.println("DetectorTag: error: tag group not present");
}

public void addTag(DetectorTag tag){
 tags.add(tag);
}

public void removeTag(DetectorTag tag){
 //tags.remove(tag);
}


public void paint(Graphics2D g,int x_offset,int x_range,double x_scale, Dimension windowSize, int GenericWidth, int height){
 if (DisplayTags){
	 for (int j=0;j<tags.size();j++){
	 	  DetectorTag tag=(DetectorTag)(tags.get(j));
	 	  tag.paintCursor(g,x_offset,x_range,x_scale,windowSize,GenericWidth,height,false);
	 	  tag.paintTag(g,x_offset,x_range,x_scale,windowSize,GenericWidth,height);
		}
   }//if displaytags
 }

}