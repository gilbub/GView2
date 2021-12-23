package gvdecoder;

import com.sun.jna.Library;
import com.sun.jna.Native;

public class dmd_control2{
 public interface dmd extends Library{
  //public void blink();
  }
  public dmd getDMD(){
   dmd tmp = (dmd) Native.loadLibrary("alpV42", dmd.class);
   return tmp;
  }
}
