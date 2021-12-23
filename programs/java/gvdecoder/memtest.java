package gvdecoder;

class memtest{
public int[] a;

public memtest(){
try{
System.out.println("created memtest");
 a=new int[10000];
}finally{

System.out.println("call finally");
}


}





}