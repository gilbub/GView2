package gvdecoder;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;


public class sim implements Runnable{


static {
        System.loadLibrary("fntest");
		}
Thread animator_thread=null;
int[] cellcolors;
int xdim;
int ydim;
dish Dish;
Vector framevector;
public native void iterate();
public native void setup();
public native void updateDrawable(int[] cellcolors, int xdim, int ydim);
public native int queryXDimension();
public native int queryYDimension();



public void run(){

 for (;;){
 for (int i=0;i<5;i++) iterate();
 updateDrawable(cellcolors, xdim,ydim);

  if (framevector!=null){
    for (int j=0;j<framevector.size();j++){
      ((drawableframe)framevector.elementAt(j)).draw();
     }
  }
 try {Thread.sleep(50); } catch (InterruptedException e){;}
 }
}


public sim(){



 //read in the dimensions...

   xdim=200;
   ydim=200;
 cellcolors=new int[xdim*ydim];
 for (int i=0;i<cellcolors.length;i++) cellcolors[i]=1;
  Dish=new dish(cellcolors,xdim,ydim,400,this);
  setupdrawable(Dish);
 }

public void setupdrawable(drawable dr){
 if (framevector==null) framevector=new Vector();
 drawableframe drf=new drawableframe(dr,framevector);
 framevector.addElement(drf);
}

public void start(){if (animator_thread==null){animator_thread=new Thread(this);
   animator_thread.start();}}
public void stop() {if ((animator_thread!=null)){ animator_thread.stop();
    animator_thread=null; }}

public static void main(String[] args) {
        sim s=new sim();
		s.setup();
        s.start();
}



}