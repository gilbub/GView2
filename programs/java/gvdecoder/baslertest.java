package gvdecoder;
public class baslertest{
 static {
  System.loadLibrary("BaslerPylonAdapter");
 }

  private native void testCamera();

  private native void sayHello();

   public static void main(String[] args) {
        new baslertest().sayHello();  // invoke the native method
     }
}