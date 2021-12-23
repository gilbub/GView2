package gvdecoder;
import javafx.application.Platform;
import javafx.beans.property.*;
//import org.python.core.*;


public class LaserProperties{
	//extends PyObject{
private static LaserProperties ref;

 BooleanProperty laserOn;
 BooleanProperty shutterOpen;
 BooleanProperty requestShutterOpen;
 IntegerProperty laserWavelength;
 DoubleProperty laserAttenuation;
 BooleanProperty laserReady;
 BooleanProperty laserEmitting;
 BooleanProperty laserModeLocked;
 BooleanProperty laserAdjusting;
 IntegerProperty galvoDwellTime;
 IntegerProperty pmtTicks;
 IntegerProperty galvoStartX;
 IntegerProperty galvoStopX;
 IntegerProperty galvoStartY;
 IntegerProperty galvoStopY;
 IntegerProperty galvoStartZ;
 IntegerProperty galvoStopZ;
 IntegerProperty galvoStepsX;
 IntegerProperty galvoStepsY;
 IntegerProperty galvoStepsZ;
 IntegerProperty galvoCenterX;
 IntegerProperty galvoCenterY;
 IntegerProperty imageShowError;
 IntegerProperty imageSkipOddLines;
 IntegerProperty imageAveragePositionWindow;
 IntegerProperty imageShiftCompensation;


 public String toString(){
	 return "\nshutterOpen="+shutterOpen.get()+"\n"+
	        "requestShutterOpen="+requestShutterOpen.get()+"\n"+
            "laserOn="+laserOn.get()+"\n"+
            "laserWavelength="+laserWavelength.get()+"\n"+
            "laserAttenuation="+laserAttenuation.get()+"\n"+
            "laserReady="+laserReady.get()+"\n"+
            "laserEmitting="+laserEmitting.get()+"\n"+
            "laserModeLocked="+laserModeLocked.get()+"\n"+
            "laserAdjusting="+laserAdjusting.get()+"\n";
 }
public static synchronized LaserProperties getLaserProperties(){
      if (ref == null)
          ref = new LaserProperties();
      return ref;
 }


private LaserProperties(){
  laserOn=new SimpleBooleanProperty(); //should be called RequestLaserOn - laser is on when laserEmitting property is true
  shutterOpen=new SimpleBooleanProperty();
  requestShutterOpen=new SimpleBooleanProperty();
  laserWavelength=new SimpleIntegerProperty();
  laserAttenuation=new SimpleDoubleProperty();
  laserReady=new SimpleBooleanProperty();
  laserEmitting=new SimpleBooleanProperty();
  laserModeLocked=new SimpleBooleanProperty();
  laserAdjusting=new SimpleBooleanProperty();
  galvoDwellTime=new SimpleIntegerProperty() ;
  pmtTicks=new SimpleIntegerProperty();
  galvoStartX=new SimpleIntegerProperty() ;
  galvoStopX=new SimpleIntegerProperty() ;
  galvoStartY=new SimpleIntegerProperty() ;
  galvoStopY=new SimpleIntegerProperty() ;
  galvoStartZ=new SimpleIntegerProperty() ;
  galvoStopZ=new SimpleIntegerProperty() ;
  galvoStepsX=new SimpleIntegerProperty() ;
  galvoStepsY=new SimpleIntegerProperty() ;
  galvoStepsZ=new SimpleIntegerProperty();
  galvoCenterX=new SimpleIntegerProperty();
  galvoCenterY=new SimpleIntegerProperty();
  imageShowError=new SimpleIntegerProperty();
  imageSkipOddLines=new SimpleIntegerProperty();
  imageAveragePositionWindow=new SimpleIntegerProperty();
  imageShiftCompensation=new SimpleIntegerProperty();
}

 public final boolean getLaserModeLocked(){return laserModeLocked.get();}
 public final boolean getLaserAdjusting(){return laserAdjusting.get();}
 public final boolean getLaserOn(){return laserOn.get();}
 public final boolean getShutterOpen(){return shutterOpen.get();}
 public final boolean getRequestShutterOpen(){return requestShutterOpen.get();}
 public final int getLaserWavelength(){return laserWavelength.get();}
 public final double getLaserAttenuation(){return laserAttenuation.get();}
 public final boolean getLaserReady(){return laserReady.get();}
 public final boolean getLaserEmitting(){return laserEmitting.get();}

 public final void setLaserAdjusting(boolean v){laserAdjusting.set(v);}
 public final void setLaserModeLocked(boolean v){laserModeLocked.set(v); }
 public final void setLaserReady(boolean v) {laserReady.set(v);}
 public final void setLaserEmitting(boolean v){laserEmitting.set(v);}
 public final void setLaserOn(boolean v){laserOn.set(v);}
 public final void setShutterOpen(boolean v){shutterOpen.set(v);}
 public final void setRequestShutterOpen(boolean v){requestShutterOpen.set(v);}
 public final void setLaserWavelength(int v){laserWavelength.set(v);}
 public final void setLaserAttenuation(double v){laserAttenuation.set(v);}

 public BooleanProperty laserAdjustingProperty(){return laserAdjusting;}
 public BooleanProperty laserModeLockedProperty(){return laserModeLocked;}
 public BooleanProperty laserEmittingProperty(){return laserEmitting;}
 public BooleanProperty laserReadyProperty(){return laserReady;}
 public BooleanProperty laserOnProperty(){return laserOn;}
 public IntegerProperty laserWavelengthProperty(){return laserWavelength;}
 public BooleanProperty shutterOpenProperty(){return shutterOpen;}
 public DoubleProperty laserAttenuationProperty(){return laserAttenuation;}
 public BooleanProperty requestShutterOpenProperty(){return requestShutterOpen;}
 public IntegerProperty galvoDwellTimeProperty(){return galvoDwellTime;}
 public IntegerProperty galvoStartXProperty(){return galvoStartX;}
 public IntegerProperty galvoStartYProperty(){return galvoStartY;}
 public IntegerProperty galvoStartZProperty(){return galvoStartZ;}
 public IntegerProperty galvoStopXProperty(){return galvoStopX;}
 public IntegerProperty galvoStopYProperty(){return galvoStopY;}
 public IntegerProperty galvoStopZProperty(){return galvoStopZ;}
 public IntegerProperty galvoStepsXProperty(){return galvoStepsX;}
 public IntegerProperty galvoStepsYProperty(){return galvoStepsY;}
 public IntegerProperty galvoStepsZProperty(){return galvoStepsZ;}
 public IntegerProperty pmtTicksProperty(){return pmtTicks;}
 public IntegerProperty galvoCenterXProperty(){return galvoCenterX;}
 public IntegerProperty galvoCenterYProperty(){return galvoCenterY;}
 public IntegerProperty imageShowErrorProperty(){return imageShowError;}
 public IntegerProperty imageSkipOddLinesProperty(){return imageSkipOddLines;}
 public IntegerProperty imageAveragePositionWindowProperty(){return imageAveragePositionWindow;}
 public IntegerProperty imageShiftCompensationProperty(){return imageShiftCompensation;}
 }