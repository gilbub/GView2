package gvdecoder;
 import java.io.*;
 import java.util.*;
 import java.util.regex.*;

 public class StringTest{


 public StringTest(){
 String line="1 2 3 32 54 1 9";
 Pattern p=Pattern.compile("\\s");

 String[] vals=p.split(line) ;
 System.out.println("hello ...");
 for (int i=0;i<vals.length;i++){
  System.out.println(i+" = "+vals[i]);
 }

 }

 public static void main(String[] arg){

  StringTest st=new StringTest();

 }


 }