package gvdecoder.trace;

import gvdecoder.trace.*;
import java.awt.*;

public class DetectorTag extends Cursor{

public volatile Double maxVal;
public volatile Double minVal;
public volatile Double yVal;
public volatile Double difference;
public volatile Boolean AssertedTrigger;

public  double maxRelVal; //if base defined, calculate these values
public  double minRelVal;

public  int maxPosYVal;
public  int minPosYVal;
public  int posYVal;

public   int paramtype=1;//0=raw,1=norm, 2=trans;

public  Trace trace;
public  Trace detect_trace;
public  Trace ref_trace;

public  DetectorTag base;
public  String basename="";
public  double initialBaseVal=0;

public  Boolean relative=new Boolean(true);


public  Rectangle maxRect=new Rectangle(0,0,5,5);
public  Rectangle minRect=new Rectangle(0,0,5,5);

public  int userMaxOffset=0;
public  int userMinOffset=0;

 public DetectorTag(String filename,
			  int num,
			  double x_val,
			  double y_val,
			  double m_val,
			  int pos,
			  Color col,
			  boolean vis){
  super(filename,num,x_val,y_val,m_val,pos,col,vis);
  AssertedTrigger=new Boolean(false);

  }
  public void resetUserMinOffset(int yval){
   posYVal=trace.getScaled(position.intValue()-(int)x_value.doubleValue());
   userMinOffset=yval-posYVal+20;
   double tmp= trace.getInverseFromScaled(yval);
   minVal=new Double(tmp);

   check(0);

  }
  public void resetUserMaxOffset(int yval){
   posYVal=trace.getScaled(position.intValue()-(int)x_value.doubleValue());
   userMaxOffset=yval-posYVal-20;

   double tmp= trace.getInverseFromScaled(yval);
   maxVal=new Double(tmp);

   check(0);
  }

  public void setMode(int m){
  //m=0 choose tags mode, m==1; detect mode
	 if (m==0) trace=ref_trace;
	 else trace=detect_trace;
  }

  public void resetYVal(int offset){
   	yVal=new Double(getFromTrace(position.intValue()-offset));
   	//
	if (base!=null)
	 initialBaseVal=getFromTrace(base.position.intValue()-offset);

	check(0);
  }

  public double FindDifferenceFromBase(int index){
   double dif=0;
   if (base!=null)	{
	   dif= getYFromXValue(index+position.intValue())-getYFromXValue(index+base.position.intValue());
         }
    return dif;
  }


  public boolean check(int index){
	boolean AssertTrigger=false;
	color=Color.black;
	 if ((maxVal!=null)&&(minVal!=null)){
	double dif=Double.MAX_VALUE;
	if (relative.booleanValue()){
		dif=FindDifferenceFromBase(index);
	    }
	   else dif = getYFromXValue(index+position.intValue());
        difference=new Double(dif);
	if ((initialBaseVal+dif<maxVal.doubleValue())&&(initialBaseVal+dif>minVal.doubleValue())) AssertTrigger=true;
	 AssertedTrigger=new Boolean(AssertTrigger);
    }
     if (AssertTrigger) color=Color.gray;
     return AssertTrigger;

    }



  public int returnIndexFromPosition(){
	   return (int)(position.intValue()-x_value.doubleValue());
  }

  public double getYFromXValue(int value){
	  return getFromTrace(value-(int)x_value.doubleValue());
  }

  public double getFromTrace(int index){
     double val=0;
    if (index<trace.Size()){
    switch  (paramtype){
      case 0: val=trace.getRaw(index); break;
      case 1: val=trace.getNorm(index); break;
      case 2: val=trace.getTrans(index); break;
      default: val=trace.getViewed(index); break;
      }
     }
     return val;
 }

  public void paintTag(Graphics2D g,
  						int x_offset,
  						int x_range,
  						double x_scale,
  						Dimension windowSize, //scale to this
  						int height, //height of font
  						int width //width of font
						){
	    posYVal=trace.getScaled(position.intValue()-(int)x_value.doubleValue());
        int cursor_position=position.intValue();
        int loc=(int)((cursor_position-x_offset)/x_scale);
        maxPosYVal=posYVal-20+userMinOffset;
        minPosYVal=posYVal+20+userMaxOffset;
		maxRect.setLocation( loc-2,maxPosYVal-2);
		         g.setColor(Color.gray);
		         g.fill(maxRect);
		         g.setColor(Color.white);
		         g.drawString("+",loc-2,maxPosYVal+4);
         minRect.setLocation(loc-2,minPosYVal-2);
		         g.setColor(Color.gray);
		         g.fill(minRect);
		         g.setColor(Color.white);
		         g.drawString("-",loc-1,minPosYVal+4);
                 g.setColor(Color.white);
		     g.drawString("X",loc-3,posYVal+2);

     }

}