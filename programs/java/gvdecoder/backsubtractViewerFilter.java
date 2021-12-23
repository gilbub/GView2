package gvdecoder;

public class backsubtractViewerFilter implements ViewerFilter{
Viewer2 vw;
public int[] back;
public void run(Viewer2 v, int[] dat){
  for (int i=0;i<dat.length;i++){
   dat[i]=dat[i]-back[i];
  }
}

public backsubtractViewerFilter(Viewer2 v){
 this.vw=v;
 back=new int[vw.X_dim*vw.Y_dim];
 for (int i=0;i<back.length;i++){
   back[i]=vw.datArray[i];
 }

}

}