package gvdecoder;
import  java.util.prefs.*;

public class tester{

public void foo(){

Preferences prefs = Preferences.userNodeForPackage(tester.class);
     prefs.put("num_rows", "93");
     prefs.put("num_cols", "593");
     prefs.put("a", "87");
     prefs.put("b", "pickle");


}
public static void main(String[] arg){
tester t=new tester();
t.foo();

}
}