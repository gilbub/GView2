package gvdecoder;

public class textStatusMonitor implements StatusMonitor{
  boolean cancelled=false;
  public void showProgress(double val){System.out.println("progress "+val);}
  public boolean isCancelled(){ return cancelled; }
  public void cancel(){ cancelled=true;}

}