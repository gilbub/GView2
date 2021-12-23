package gvdecoder;
import com.protomatter.syslog.*;
import java.io.*;
public class testlog{
public void say(){
File file = new File("syslogconfig.sl");
 // Syslog.configure(file, "org.apache.xerces.parsers.SAXParser");
Syslog.setLogMask("DEBUG");
System.out.println("here");
Syslog.debug(this, "I'm in here");
Syslog.debug(this, "Hello there... this is a debug message");
  Syslog.info(this, "Hello there... this is an info message");
  Syslog.warning(this, "Hello there... this is a warning message");
  Syslog.error(this, "Hello there... this is an error message");
  Syslog.fatal(this, "Hello there... this is a fatal message");



}

public static void main(String[] arg){
testlog tl=new testlog();
tl.say();

}

}