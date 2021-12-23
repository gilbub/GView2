package gvdecoder;
import java.util.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.util.Arrays;
import java.util.ArrayList;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import javax.tools.ToolProvider;
import javax.tools.JavaCompiler.CompilationTask;
import javax.tools.JavaFileObject.Kind;

public class PythonPreprocessorV6{
 static int session=0;
 List<CodeSegment> codes= new ArrayList<CodeSegment>();
 List<Parameter> params=new ArrayList<Parameter>();
 StringWriter writer=new StringWriter();
 String parseerror=null;
 String pythonstring=null;
 StringBuilder sb;
 String classname;
 boolean parsesuccess=false;
 boolean compilesuccess=false;
 String compileerror=null;

 public boolean fileexists(String classname){
	//check if the classname exists in gvdecoder.scripts.anonclasses
	String filename= "gvdecoder"+File.separator+"scripts"+File.separator+"anonclasses"+File.separator+classname;
	File f = new File(filename);
    return f.exists();

 }

 public void reset(){
	 codes.clear();
 }

 public void reset(int i){
	 codes.remove(i);
 }

 public boolean processParameters(String s){
	int start=0;
	int end=0;
	int eq=0;
	while (start!=-1){
	 start=s.indexOf("@p",start+1);
	 System.out.println("p start="+start);
	 if (start>-1){
	 end=s.indexOf("\n", start+1);
	 System.out.println("p end="+end);

	 String paramstring=s.substring(start,end);
	 System.out.println("p paramstring="+paramstring);
	 eq=paramstring.indexOf("=");
	 System.out.println("p eq="+eq);

	 Parameter par=new Parameter();
	 par.linenumber=countlines(s,0,end);
	 if (eq>-1){
		 System.out.println("feeding the following into substring "+(start+2)+" "+eq);
		 par.paramname=paramstring.substring(2,eq);
		 par.paramvalue=paramstring.substring(eq+1);
		 par.ok=true;
		} else{
		 par.ok=false;
		}
     params.add(par);
   }
  }
 return true;
}


 public boolean process(String s){
	 session+=1;
	 int start=0;

     parseerror=null;
     parsesuccess=true;
     sb=new StringBuilder();
     int pystart=0;
     int pyend=0;


	 while(start!=-1){
		 start=s.indexOf("@java",start+1);
		 if (start!=-1){
             CodeSegment newcode=new CodeSegment();

			 pyend=start;
			 sb.append(s.substring(pystart,pyend));
			 int end=s.indexOf("@/java",start+1);
			 if (end!=-1){
			  int classend=s.indexOf("\n",start+1);
			  newcode.basename=s.substring(start+6,classend);
			  newcode.classname=newcode.basename+"_"+session;
			  newcode.scriptlinenumber=countlines(s,0,start);
			  pystart=end+6;
			  newcode.commandstring=s.substring(classend,end);
			  int newlines=countlines(newcode.commandstring,0,-1);
			  for (int j=0;j<newlines;j++) sb.append("#blank\n");
			  StringBuilder cs=new StringBuilder(256);
			  cs.append("package gvdecoder.scripts.anonclasses;\n");
			  for (int k=0;k<newcode.scriptlinenumber-5;k++) cs.append("/* */\n");
			  cs.append("import gvdecoder.Matrix; \n");
			  cs.append("import gvdecoder.array.doubleArray3D;\n");
			  cs.append("public class ");
			  cs.append(newcode.classname);

			  cs.append("{\n");
			  cs.append(newcode.commandstring);
			  cs.append("}\n");
			  newcode.code=cs.toString();
			  boolean foundnewcode=true;

			  for (CodeSegment existingcode:codes){
				  if (existingcode.basename.equals(newcode.basename)){
					  foundnewcode=false;
					  existingcode.recompile=false;
					  existingcode.scriptlinenumber=newcode.scriptlinenumber;
					  if ((!existingcode.commandstring.equals(newcode.commandstring))||(!fileexists(existingcode.classname+".class"))){

						  existingcode.code=newcode.code;
						  existingcode.recompile=true;
						  existingcode.commandstring=newcode.commandstring;
						  existingcode.successfulcompile=false;
						  existingcode.classname=newcode.classname;
					  }
			  }
		    }
		    if (foundnewcode) codes.add(newcode);
			  //javasections.add(cs.toString());

		   }else{
			   /*something wrong*/
			   parseerror="Missing end tag near "+start;
			   parsesuccess=false;

		   }
		 }

      }




      sb.append(s.substring(pystart,s.length()));
      pythonstring=sb.toString();

      return parsesuccess;
  }




public int  countlines(String s, int startposition, int endposition){
	int lines=0;
	if (endposition>s.length()||endposition==-1) endposition=s.length();
	for( int pos = 0; pos < endposition; pos++){
	    char c = s.charAt(pos);
	    if(  c== '\n' ) {
	        lines++;
	    }
    }
    return lines;
}

public boolean compile(int i) throws IOException {
    compilesuccess=true;
    CodeSegment code=codes.get(i);
    ArrayList<JavaFileObject> compilationUnits = new ArrayList<JavaFileObject>();
    JavaFileObject file = new JavaSource(code.classname,code.code);
    compilationUnits.add(file);
    code.recompile=false;
     JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
	 DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<JavaFileObject>();
	 Iterable options = Arrays.asList("-d", ".");

     CompilationTask task = compiler.getTask(writer, null, diagnostics, options, null, compilationUnits);

     compilesuccess = task.call();
     StringBuilder sb=new StringBuilder(256);
     for (Diagnostic diagnostic : diagnostics.getDiagnostics()) {
	  sb.append("\n java file                -> " + diagnostic.getSource()+"\n");
      sb.append("   java snippet Line Number -> " + diagnostic.getLineNumber()+"\n");
      sb.append("   java snippet code        -> " + diagnostic.getCode()+"\n");
      sb.append("   java snippet Message     -> "+ diagnostic.getMessage(java.util.Locale.ENGLISH)+"\n");
     }
     sb.append("Success = "+compilesuccess+"\n");
     compileerror=sb.toString();
    return compilesuccess;
  }


public boolean compile() throws IOException {
    compilesuccess=true;
    ArrayList<JavaFileObject> compilationUnits = new ArrayList<JavaFileObject>();
    for (CodeSegment code:codes){
	 if (code.recompile){
      JavaFileObject file = new JavaSource(code.classname,code.code);
      compilationUnits.add(file);
      code.recompile=false;
       }
    }
    if (compilationUnits.size()>0){
     JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
	 DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<JavaFileObject>();
	 Iterable options = Arrays.asList("-d", ".");

     CompilationTask task = compiler.getTask(writer, null, diagnostics, options, null, compilationUnits);

     compilesuccess = task.call();
     StringBuilder sb=new StringBuilder(256);
     for (Diagnostic diagnostic : diagnostics.getDiagnostics()) {
	  if (((diagnostic.getSource()).toString()).indexOf("RegularFileObject")>-1){
	  sb.append("\n External class file error -> " + diagnostic.getSource()+"\n");
	  sb.append("               Line Number -> " + diagnostic.getLineNumber()+"\n");
	  sb.append("                     Code  -> " + diagnostic.getCode()+"\n");
      sb.append("                  Message  -> "+ diagnostic.getMessage(java.util.Locale.ENGLISH)+"\n");
      }else{
      sb.append("\n java snippet Line Number -> " + diagnostic.getLineNumber()+"\n");
      if (diagnostic.getKind()==javax.tools.Diagnostic.Kind.ERROR)
        sb.append("              Error Code  -> " + diagnostic.getCode()+"\n");
       else
        sb.append("              Warning Code-> " + diagnostic.getCode()+"\n");
      sb.append("              Message     -> "+ diagnostic.getMessage(java.util.Locale.ENGLISH)+"\n");
      }
      }
     sb.append("Success = "+compilesuccess+"\n");
     compileerror=sb.toString();
   }//end if
   else compileerror= "No java to compile";
   return compilesuccess;
  }

}


class CodeSegment{
	String basename;
	String classname;
	boolean recompile;
	String code;
	String commandstring;
	int scriptlinenumber;
	int errorlinenumber;
	boolean successfulcompile;
	String errors;
	public CodeSegment(){
		errors=null;
		successfulcompile=false;
		scriptlinenumber=-1;
		errorlinenumber=-1;
		recompile=true;

	}
}


class Parameter{
	String paramname;
	String paramvalue;
	int linenumber;
	boolean ok;
}

class JavaSource extends SimpleJavaFileObject {
  String code;

  JavaSource(String name, String code) {
    super(URI.create("string:///" + name.replace('.','/') + Kind.SOURCE.extension),Kind.SOURCE);
    this.code = code;
  }
  @Override
  public CharSequence getCharContent(boolean ignoreEncodingErrors)
              throws IOException {
          return code ;
    }
  public void setSourceCode(String sourceCode) {
	        this.code = sourceCode;
    }
     public String getSourceCode() {
	        return code;
    }
}

