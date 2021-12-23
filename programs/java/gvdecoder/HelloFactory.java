public class HelloFactory{


public static SayHello getHello(){

 return new NativeHelloWorld();

 }

}