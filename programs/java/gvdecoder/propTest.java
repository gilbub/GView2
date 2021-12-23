package gvdecoder;
import java.util.*;
import java.io.*;

public class propTest{

Properties prp;

public propTest(){
 prp=new Properties();
}

public void set(){
 prp.setProperty("userdir","c:\\viewer");
 prp.setProperty("lastdir","d:\\take");
}

public void print(){
System.out.println("user "+prp.getProperty("userdir","default user dir"));
System.out.println("last "+prp.getProperty("lastdir","default last dir"));
}

public void SaveProp(){
try{
FileOutputStream ostream=new FileOutputStream(new File("reader.cfg"));
prp.store(ostream,"configuration file for image viewer. do not hand edit");
}catch (Exception e){e.printStackTrace();}

}

public void readProp(){
try{
FileInputStream istream=new FileInputStream(new File("reader.cfg"));
prp.load(istream);
}catch(Exception e){e.printStackTrace();}
}

public static void main(String[] arg){
propTest pt=new propTest();
pt.print();
pt.set();
pt.print();
pt.SaveProp();
System.out.println("reading.........");
propTest pt2=new propTest();
pt2.readProp();
pt2.print();
Enumeration en=pt2.prp.propertyNames();
 while(en.hasMoreElements()){
  String name=(String)en.nextElement();
  System.out.println("en name .... "+name+" val "+pt2.prp.getProperty(name));
  }


}}