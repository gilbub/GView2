package gvdecoder;
 import java.io.*;

 public class filetest{

 public filetest(String filename){
 try{
	  File f=new File(filename);
	  FileReader pr=new FileReader(f);
	  BufferedReader bi=new BufferedReader(pr);
	 try{
	  while(true){
	  System.out.println(Integer.parseInt(bi.readLine()));}
	 }
	 catch(NumberFormatException e){;}
	 catch(EOFException e){;}


	  }catch(IOException e){e.printStackTrace();}
}
public static void main(String[] arg){

filetest ft=new filetest(arg[0]);
}
}