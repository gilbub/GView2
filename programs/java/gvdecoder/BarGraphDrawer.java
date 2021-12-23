package gvdecoder;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.text.*;
import java.awt.geom.GeneralPath;
//import java.awt.geom.GeneralPath;
import java.awt.Graphics2D.*;
import java.awt.geom.AffineTransform;

public class BarGraphDrawer{
	Color background;
    double Value;    //actual value contained in the component
    double maxValue; //user supplied max
    double minValue; //user supplied min
    int displayVal;  //the offset of the bar.
    public boolean TOGGLENUMBERDISPLAY=false;
    public boolean HIGHLIGHT=false;
    public boolean FILLBACKGROUND=false;
    public boolean RECALCSHAPE=true;

    Color backColor;
    Color traceColor;
    Color highlightColor;
    double Increment;
    double[] data;
    int arraylength;
    float step;
    AffineTransform at;
    float tracexscale,traceyscale;
    double max,min;

    int startval,endval; //these used to determine userstart and userend
    int userstart,userend;
    int thickness=1;
    int spacing=1;

    Shape shape;

    public BarGraphDrawer(double[] data){
		this.data=data;
		backColor=Color.white;
		traceColor=Color.blue;
		highlightColor=Color.yellow;
		this.userstart=0;
		this.userend=data.length-1;
		HIGHLIGHT=false;
		FILLBACKGROUND=true;
		TOGGLENUMBERDISPLAY=false;

	  }

    public BarGraphDrawer(double[] data, int thickness, int spacing){
    	this.data=data;
    	this.thickness=thickness;
    	this.spacing=spacing;
    }


    public BarGraphDrawer(double[] data,Color backc, Color tracec, Color highc, boolean fillback, boolean highlight, boolean numbers, int userstart, int userend){
		this.data=data;
		backColor=backc;
		traceColor=tracec;
		highlightColor=highc;
		this.userstart=userstart;
		this.userend=userend;
		HIGHLIGHT=highlight;
		FILLBACKGROUND=fillback;
		TOGGLENUMBERDISPLAY=numbers;
	}

    public void  paint(Graphics2D g,Rectangle bounds){
       /* bounds.height=bounds.height-20;
        bounds.y=bounds.y-15;
        bounds.x=bounds.x-1;
        bounds.width=bounds.width-5;*/
		if (FILLBACKGROUND){
		 g.setColor(backColor);
		 g.fillRect(bounds.x,bounds.y,bounds.width,bounds.height);
		}
		if ((HIGHLIGHT)&&(endval>startval)){
		 g.setColor(highlightColor);
		 g.fillRect(startval,bounds.y,endval-startval,(int)bounds.getHeight());
		}
		g.setColor(traceColor);
		if ((shape==null)||(RECALCSHAPE)) {
			getShape(userstart,userend,bounds);
			RECALCSHAPE=false;
		}
		g.draw(shape);
    }

    public AffineTransform gettransform(Rectangle oldbounds, Rectangle newbounds){

        AffineTransform at1=new AffineTransform();
        float xscale=((float)newbounds.width)/((float)oldbounds.width);
        float yscale=((float)newbounds.height)/((float)oldbounds.height);
        at1.setToScale(xscale,yscale);
        float xpos=oldbounds.x*xscale;
        float ypos=oldbounds.y*yscale;
        float xpost=newbounds.x-xpos;
        float ypost=newbounds.y-ypos;
        at1.translate(xpost/xscale,ypost/yscale);
        return at1;
	}

    Shape tmp;
    float xt,yt;

    //boolean userandest=true;
	public Shape getShape(int start, int end, Rectangle bounds){
		arraylength=(int)(bounds.width*2.0f);
	   	if (end-start<bounds.width*2) arraylength=end-start;
	   	step=((float)(end-start))/arraylength;
        /*
        if (step>5) userandest=true;
         else userandest=false;
        */
	   	GeneralPath gp=new GeneralPath(GeneralPath.WIND_EVEN_ODD,arraylength+1);

	   	min=Double.POSITIVE_INFINITY;
	   	max=Double.NEGATIVE_INFINITY;
	   	double val=0;

	   	int count=0;
	    for (int i=start;i<end;i++){

			val=data[i];
			if (val>max) max=val;
	   		if (val<min) min=val;
		}
		gp.moveTo(0.0f,(float)max);
        gp.lineTo((float)0.0,(float)(max-data[start]));
        gp.lineTo((float)thickness,(float)(max-data[start]));
        gp.lineTo((float)thickness,(float)max);
	   	for (float f=(float)start+step;f<end;f+=step){

		   /* if (userandest){ f+=(float)(java.lang.Math.random()-0.5)*step;}
            if (f<start) f=start;
            if (f>end) f=end;
	   		*/
	   		val=data[(int)f];
	   		gp.lineTo((float)++count,max);
	   		gp.lineTo((float)count,(float)max-(float)(val));
	   		gp.lineTo((float)count+thickness,(float)max-(float)(val));
	   		gp.lineTo((float)count+thickness,max);
	   		gp.lineTo((float)count+spacing,max);
	   	}
	   	at=gettransform(gp.getBounds(),bounds);

	   	shape= at.createTransformedShape(gp);
	   	return shape;
	   	//scale this to fit in bounds.
	   }

    public void setRange(int st, int en){
		userstart=st;
		if (st<0) userstart=0;
		userend=en;
		if (en>=data.length) userend=data.length-1;
      }

    public void setHighlightRange(int st, int en){
		HIGHLIGHT=true;
	    startval=st;
	    endval=en;
	    if (st>=en) HIGHLIGHT=false;
	 }

	public void setHighlightRange(float st, float en){
		HIGHLIGHT=true;
	    startval=(int)(st*data.length);
	    endval=(int)(en*data.length);
	    if (st>=en) HIGHLIGHT=false;
	 }

}