 package gvdecoder.trace;

 import java.io.*;
 import java.util.*;
 import java.util.regex.*;

 public class getArrayFromFile{
  int[][] arr;
  List v;
  int index=0;
  public Vector cursors;
 public getArrayFromFile(String filename){

 Pattern p=Pattern.compile("\\s");
 try{
	  v=new ArrayList();
	  File f=new File(filename);
	  FileReader pr=new FileReader(f);
	  BufferedReader bi=new BufferedReader(pr);

	 try{
	  while(true){
	  String line=bi.readLine();
	  //ignore lines starting with # (comments) and & (commands)
	  if ((!line.startsWith("#"))&&(!line.startsWith("&"))){
	  String[] s=p.split(line);
	  int[] oneline=new int[s.length];
	  for (int j=0;j<s.length;j++){
	    oneline[j]=Integer.parseInt(s[j].trim());
	   }
	    v.add(index++,oneline);
       }else{
		if (line.startsWith("&cursor")){
			String info=line.substring(line.indexOf('=')+1,line.length());
			gvdecoder.trace.Cursor c=new gvdecoder.trace.Cursor();
			c.fromString(info);
			if (cursors==null){cursors=new Vector();}
			cursors.add(c);
		}

	   }
	  }
	 }
	 catch(NumberFormatException e){e.printStackTrace();}
	 catch(EOFException e){;}
	 catch(Exception e){;}
   }catch(IOException e){e.printStackTrace();}
}

public int[][] returnArray(){
int[][] arr=new int[index][((int[])(v.get(0))).length];
for (int i=0;i<index;i++){
 Object myI=v.get(i);
 int[] oneline=(int[])myI;
 for (int j=0;j<oneline.length;j++) arr[i][j]=oneline[j];
 }
return arr;
}

public int[][] returnTransposedArray(){
int[][] old_arr=returnArray();
int cols=old_arr.length;
int rows=old_arr[0].length;
int[][] new_arr=new int[rows][cols];
for (int i=0;i<cols;i++){
 for (int j=0;j<rows;j++){
	 new_arr[j][i]=old_arr[i][j];
 }

}
return new_arr;

}



public static void main(String[] arg){

getArrayFromFile ft=new getArrayFromFile(arg[0]);
int[][] arr=ft.returnArray();
for (int i=0;i<arr.length;i++){
 for (int j=0;j<arr[i].length;j++){
  System.out.print(arr[i][j]+",");
}
System.out.println("");
}

int[][] tarr=ft.returnTransposedArray();
for (int i=0;i<tarr.length;i++){
 for (int j=0;j<tarr[i].length;j++){
  System.out.print(tarr[i][j]+",");
}
System.out.println("");
}

}


}




