package gvdecoder;

public class MultiPVCAMController{
 public int numberofcameras;

 static {
      System.loadLibrary("photometrics2cam");
   }

   native int test();
   native int  StartSystem();
   native void StartSequence();
   native synchronized int StartCamera(int camnum, String filename);
   native synchronized long GetData(int camnum, int[] dat, int width, int height);
   native synchronized int GetDataChunk(int camnum, int[] dat, int zdim, int width, int height);
   native synchronized void StopCamera(int camnum);
   //native void ShutdownCamera(int camnum);
   native void StopSequence();
   native int StopSystem();

   public int Hello(){ return 20;}


}