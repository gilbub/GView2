package gvdecoder;
import javax.swing.*;
import java.awt.event.*;

public class ProgressDisplayer implements ActionListener{

public  GView gv;
public  javax.swing.Timer dl;
public  double progress=0.0;
public  long timesincelastdelay=0;
public static ProgressDisplayer ref;


public static synchronized ProgressDisplayer getProgressDisplayer(){
      if (ref==null) ref=new ProgressDisplayer(null);
      return ref;
    }

public ProgressDisplayer(GView gv){
  this.gv=gv;
  ref=this;
 }


public void actionPerformed(ActionEvent e) {
 if (gv.jv!=null) gv.jv.displayProgress(progress);
 System.out.println("dispay progress displaying");
 }


public boolean displayProgress(double val){
  if (gv==null) return false;
  System.out.println("dislay progress called");
  long t=System.currentTimeMillis();
  if (t-timesincelastdelay>900){
	  timesincelastdelay=t;
	  try{
	  Thread.sleep(100);
      System.out.println("display progress sleeping");
      }catch(Exception e){}

  }


  progress=val;
  //System.out.println(val+" "+progress);
  if ((dl==null)){
	 dl=new javax.swing.Timer(250, this);
     dl.start();
	}

  if ( !dl.isRunning() && (progress<=1.0) ){
	 dl.restart();
  }

  if ((progress>=1.0)&&(dl!=null)) {
	  gv.jv.displayProgress(1.0);
      //System.out.println("stopped progress displayer");
      dl.stop();
      }

  if (gv.jv!=null){
	  if (gv.jv.CANCEL){
	   gv.jv.displayProgress(1.0);
	   dl.stop();
	   gv.jv.CANCEL=false;
	   return true;
      }
   }
  return false;

  }

}