package gvdecoder;
public abstract class JavaAbstract{

abstract public void behavior(int j);
abstract public void behavior(String s);
abstract public void behavior(int j, String s);
public static void testBehaviorInt(JavaAbstract jabs){
 jabs.behavior(1);
 }
public static void testBehaviourString(JavaAbstract jabs){
 jabs.behavior("baz");
}
public static void testBehaviorIntString(JavaAbstract jabs){
 jabs.behavior(1,"foo");
}
}