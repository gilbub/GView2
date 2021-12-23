package gvdecoder.utilities;

import java.util.regex.*;
import java.util.*;
import java.io.*;

public class CWRUExplogDecoder implements ImageFileManager{

Pattern log;
Matcher m;
int lastIndex=0;
boolean foundLast=false;
String lastFoundRecord="";

public String explog="";
public static String[] fieldnames={"file"," ","time","s1","s*","s2","s3","s4","ch","comment"};

ArrayList arraylist; //holds all the records
String path;

public CWRUExplogDecoder(){


}

public void TrimHeader(){
m.find();
lastIndex=m.start();

}

public String getNextRecord(){
	String result="";
	foundLast=false;
	if (m.find()){
		 result=explog.substring(lastIndex,m.start());
		 lastFoundRecord=result;
	     lastIndex=m.start();
	     foundLast=true;
	   }
    return result;
}

public void getStringFromFile(String filename){
	String result="";
	char[] chars=new char[64000];
	try{
		  File f=new File(filename);
		  path=f.getParent();

		  FileReader pr=new FileReader(f);
		  BufferedReader bi=new BufferedReader(pr);
         String tmp="";
         int numchars=1;
		 try{
		  while(numchars>0){
		  numchars= bi.read(chars,0,64000);
		  if (numchars>0) tmp=new String(chars,0,numchars);
		  result+=tmp;

		  }
		 }
		 catch(IOException e){;}

	 }catch(IOException e){e.printStackTrace();}

   explog=result.replaceAll("ms"," ");
   log=Pattern.compile("\\d\\d\\d\\.log");
   m=log.matcher(explog);
}



public CWRUrecord fillRecord(){
String[] strtmp=new String[12];
CWRUrecord record=new CWRUrecord();

//find the file name
if (lastFoundRecord.length()>20){
record.filename=(lastFoundRecord.substring(0,7)).trim();
int start=lastFoundRecord.indexOf("S1-S1 =");
int end=lastFoundRecord.indexOf("Channel =")+12;
record.time=(lastFoundRecord.substring(7,start)).trim();
String mainstring=lastFoundRecord.substring(start,end);
StringTokenizer st = new StringTokenizer(mainstring,",");
for (int k=0;k<strtmp.length;k++){
     strtmp[k]=(st.nextToken()).trim();
     }
record.s1_s1=((strtmp[0]).substring(7)).trim();
record.s1_star=((strtmp[1]).substring(7)).trim();
record.s1_s2=((strtmp[4]).substring(7)).trim();
record.s2_s3=((strtmp[5]).substring(7)).trim();
record.s3_s4=((strtmp[6]).substring(7)).trim();
record.channel=((strtmp[11]).substring(9)).trim();
record.comment=(lastFoundRecord.substring(end)).trim();
}
//record.printRecord();
return record;

}





public ArrayList ReturnRecords(String filename){
	ArrayList arr=new ArrayList();
	getStringFromFile(filename);
	TrimHeader();
	foundLast=true;
	while(foundLast){
		 getNextRecord();
		 CWRUrecord record=fillRecord();
		 if (record.filename!=null)
         arr.add(record);
	 }

	return arr;

}

public ImageFileRecord[] ReturnSelectedFileNames(int start_row, int end_row){
 ImageFileRecord[] result=new ImageFileRecord[end_row-start_row+1];
 for (int i=start_row;i<end_row+1;i++){
	 CWRUrecord cwru=(CWRUrecord)arraylist.get(i);
	 if (cwru.record==null){
	   cwru.record=new ImageFileRecord();
	   cwru.record.absolutepath=path+File.separator+cwru.filename;
	  }
	 result[i-start_row]= cwru.record;
	 }
 return result;
}

public void ReadArrayList(String filename){
	arraylist=ReturnRecords(filename);

}

public String[] GetRecordNames(){
	   return fieldnames;
}


public String GetFileType(){

 return "log";

}

public Object GetRecordValue(int row, int col){
 Object val=null;
 if (arraylist!=null){
	CWRUrecord cwru= (CWRUrecord)arraylist.get(row);
	switch (col){

		case 0: val=cwru.filename; break;
		case 1: {boolean open=false;
		         if (cwru.record!=null){
				  if ((cwru.record.viewerwindow!=null)||(cwru.record.navwindow!=null)) open=true;
				 }
			    val=new Boolean(open);
			     break;
			    }
		case 2: val=cwru.time; break;
		case 3: val=cwru.s1_s1; break;
		case 4: val=cwru.s1_star; break;
		case 5: val=cwru.s1_s2; break;
		case 6: val=cwru.s2_s3; break;
		case 7: val=cwru.s3_s4; break;
		case 8: val=cwru.channel; break;
		case 9: val=cwru.comment;break;
	  }
	}


 return val;
}


public int GetRowCount(){
 return arraylist.size();
}

public static void main (String [] arg){

CWRUExplogDecoder ced=new CWRUExplogDecoder();
ArrayList arr=ced.ReturnRecords("explog3.txt");

for (int i=0;i<arr.size();i++){
	((CWRUrecord)arr.get(i)).printRecord();
	System.out.println("+++++++++++++++++++");
}

}


}

class CWRUrecord{
String filename;
String time;
String s1_s1;
String s1_star;
String s1_s2;
String s2_s3;
String s3_s4;
String channel;
String comment="";
ImageFileRecord record=null;


public void printRecord(){

System.out.println("filename ="+filename);
System.out.println("time ="+time);
System.out.println("s1_s1 ="+s1_s1);
System.out.println("s1_star ="+s1_star);
System.out.println("s1_s2 ="+s1_s2);
System.out.println("s2_s3 ="+s2_s3);
System.out.println("s3_s4 ="+s3_s4);
System.out.println("channel ="+channel);
System.out.println("comment ="+comment);



}
}