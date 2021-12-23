package gvdecoder;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
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

public class CompileSourceInMemory10{
  private static String classOutputFolder = "gvdecoder/scripts/anonclasses";
  public static String compile(String[] classname, String[] mycode) throws IOException {
    JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
    DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<JavaFileObject>();
    Iterable options = Arrays.asList("-d", classOutputFolder);
    ArrayList<JavaFileObject> compilationUnits = new ArrayList<JavaFileObject>();
    for (int i=0;i<classname.length;i++){
    JavaFileObject file = new JavaSourceFromString2(classname[i], mycode[i]);
    compilationUnits.add(file);
    }
    //Iterable<? extends JavaFileObject> compilationUnits = Arrays.asList(file);
    //CompilationTask task = compiler.getTask(null, null, diagnostics, options, null, compilationUnits);
    CompilationTask task = compiler.getTask(null, null, diagnostics, null, null, compilationUnits);

    boolean success = task.call();
    StringBuilder sb=new StringBuilder(256);
    for (Diagnostic diagnostic : diagnostics.getDiagnostics()) {
      sb.append("java snippet Line Number->" + diagnostic.getLineNumber()+"\n");
      sb.append("code->" + diagnostic.getCode()+"\n");
      sb.append("Message->"+ diagnostic.getMessage(java.util.Locale.ENGLISH)+"\n");
      sb.append("Source->"+diagnostic.getSource()+"\n");
    }
    sb.append("Success = "+success+"\n");
    return sb.toString();
  }
}

