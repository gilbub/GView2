package gvdecoder;

import com.sun.jna.Library;
import com.sun.jna.Native;

public class dmd_control3{
 public interface dmd extends Library{
  public void blink();
  }
  public dmd getDMD(){
   dmd tmp = (dmd) Native.loadLibrary("DMD_dll", dmd.class);
   return tmp;
  }
  public dmd getDMDDir(){
	 dmd tmp = (dmd) Native.loadLibrary("C:\\CD\\programs\\c\\alp42\\Debug\\DMD_dll", dmd.class);
   return tmp;
  }
}
