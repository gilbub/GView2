package gvdecoder;
import java.io.IOException;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import java.net.URI;
import javax.tools.SimpleJavaFileObject;

class JavaSourceFromString2 extends SimpleJavaFileObject {
  String code;

  JavaSourceFromString2(String name, String code) {
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