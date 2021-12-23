package gvdecoder;
import java.io.*;




public class GVFileWriter2{

  OutputStream ost;
  DataOutputStream dst;

  public boolean open(String filename){
    try{
       ost = new FileOutputStream(filename);
       dst = new DataOutputStream(ost);
       }catch(IOException e){e.printStackTrace();return false;}
     return true;
   }

  public boolean writeHeader(int version, int x, int y, int frames){
    try{
    dst.writeInt(version);
    dst.writeInt(frames);
    dst.writeInt(y);
    dst.writeInt(x);

    }catch(IOException e){e.printStackTrace(); return false;}
    return true;
    }

   public boolean writeFrameAsBytes(Viewer2 vw, int xdim, int framenumber){
    vw.JumpToFrame(framenumber);
    try{
     for (int i=0;i<(vw.X_dim*vw.Y_dim);i++){
       dst.writeByte((byte)(vw.datArray[i]/256.0));
      }

    }catch(IOException e){e.printStackTrace(); return false;}
    return true;
    }


  public boolean close(){
    try{
    ost.close();
    dst.close();
    }catch(IOException e){e.printStackTrace(); return false;}
    return true;
   }
}