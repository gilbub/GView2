package gvdecoder;
import java.util.*;


public class textPreprocessor2{
 static int session=0;
 List<String> javasections= new ArrayList<String>();
 List<String> classnames=new ArrayList<String>();
 String parseerror=null;
 String pythonstring=null;
 StringBuilder sb;
 String classname;

 public void process(String s){
	 session+=1;
	 int start=0;
     javasections.clear();
     parseerror=null;
     sb=new StringBuilder();
     int pystart=0;
     int pyend=0;
	 while(start!=-1){
		 start=s.indexOf("@java",start+1);
		 if (start!=-1){

			 pyend=start;
			 sb.append(s.substring(pystart,pyend));
			 int end=s.indexOf("@/java",start+1);
			 if (end!=-1){
			  int classend=s.indexOf("\n",start+1);
			  String tmp=s.substring(start+6,classend);
			  classname=tmp+session;
			  pystart=end+6;
			  String commandstring=s.substring(classend,end);
			  int newlines=countnewlines(commandstring);
			  for (int j=0;j<newlines;j++) sb.append("#blank\n");
			  StringBuilder cs=new StringBuilder(256);
			  cs.append("package gvdecoder.scripts.anonclasses;\n");
			  cs.append("import gvdecoder.Matrix; \n");
			  cs.append("import gvdecoder.doubleArray3D;\n");
			  cs.append("public class ");
			  cs.append(classname);
			  classnames.add(classname);

			  cs.append("{\n");
			  cs.append(commandstring);
			  cs.append("}\n");
			  javasections.add(cs.toString());

		   }else{
			   /*something wrong*/
			   parseerror="Missing end tag near "+start;

		   }
		 }

      }
      sb.append(s.substring(pystart,s.length()));
  }

public int countnewlines(String s){
	int start=0;
	int count=0;
	while(start!=-1){
	  start=s.indexOf("\n",start+1);
      if (start!=-1) count++;
   }
  return count;
 }

}