package gvdecoder;
public class ProgressDisplayer{

public static GView gv;

public ProgressDisplayer(GView gv){
  this.gv=gv;
  }

public static void displayProgress(double val){
  if (gv.jv!=null)
    gv.jv.displayProgress(val);
}

}