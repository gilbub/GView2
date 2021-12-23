package gvdecoder.utilities;

import java.util.Comparator;

public class CompareOmproFiles implements Comparator{

 public int val(String str){
  return Integer.parseInt( (str.substring( (str.indexOf(".s")+2),str.length())));
 }

 public int compare(Object a, Object b){
    String s1=(String)a;
    String s2=(String)b;
    //find postfix on s1
    int Is1=val(s1);
    int Is2=val(s2);
    if (Is1==Is2) return 0;
    else
    if (Is1<Is2) return -1;
    return 1;

 }

 public boolean equals(Object a){
   return (this==a);
 }

}