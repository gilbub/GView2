package gvdecoder;
class NativeHelloWorld implements SayHello{
    public native void displayHelloWorld();

    static {
        System.loadLibrary("hello");
    }


}

