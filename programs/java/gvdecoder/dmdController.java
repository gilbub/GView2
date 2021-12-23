package gvdecoder;

public class dmdController{
 static {System.loadLibrary("DMD_dll");}


  public native long setConfigFile(String filename);
  public native long getMemory();
  public native long setPolygon(long sequence, long position, long numpoints, double[] xs, double[] ys);
  public native long Start(long seqenceid);
  public native long StartCont(long sequenceid);
  public native long StartReps(long sequenceid, long numreps);
  public native long SeqFree(long sequenceid);
  public native long setTiming(long sequenceid, long frametime);
  public native long Initialize(String filename);
  public native long UnInitialize();
  public native long Halt();
  public native long WriteStatus();
  public native long InitializeLED();
  public native long UnInitializeLED();
  public native long LEDLevel(long val);
  public native long Setup(int numberofsequences, long timebetween);
  public native long ProjControl(long ControlType, long ControlValue);
  public native long SlaveMode();
  public native long MasterMode();
  public native long InvertImage();
  public native long NormalImage();
  public native long PutPattern(long sequence, long position, long xdim, long ydim, long[] data);
  public native long PutTarget(long sequence, long position, long w);


  public native long testtext();

}