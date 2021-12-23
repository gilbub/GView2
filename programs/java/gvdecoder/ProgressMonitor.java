package gvdecoder;
public class ProgressMonitor{

public static GView gv;

public ProgressMonitor(GView gv){
  this.gv=gv;
  }

public static void displayProgress(double val){
  if (gv.jv!=null)
    gv.jv.displayProgress(val);
}

}