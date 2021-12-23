package gvdecoder;
import java.awt.image.*;
import java.awt.image.renderable.*;
import javax.media.jai.*;
import javax.media.jai.iterator.*;
import javax.media.jai.widget.*;
import java.awt.color.*;
import com.sun.media.jai.codec.*;
import java.io.*;
import java.util.*;
import utilities.*;
import gvdecoder.log.*;
import gvdecoder.prefs.*;

public class preprocessCWRU{

public int OpenImageFile(String filename){
 int j=0;
 //open the file...
 try{

      String navfilename=FileNameManager.getInstance().FindNavFileName(filename,"");
      File tmp=new File(navfilename);
      FileSeekableStream CWRUfile=new FileSeekableStream(new RandomAccessFile(filename,"r"));

	 if (!tmp.exists()){
	  System.out.println("creating a new .nav file");
 	   //for the sake of compatibility, generate a nav file like object emmediately
      PrintWriter file=new PrintWriter(new FileWriter(navfilename),true);
      CWRUfile.seek(512);
	  while (true){
      CWRUfile.readShort();

	  file.println((j)+" "+CWRUfile.readShort()+" " //blue
	  					  +CWRUfile.readShort()+" " //green
	  					  +CWRUfile.readShort()+" " //majenta
	  					  +CWRUfile.readShort()+" " //red
	  					  +CWRUfile.readShort()    //yellow
	  			 		);
	  j++;
	  CWRUfile.seek(j*528+512);
    }
   }
  }
  catch(EOFException e){System.out.println("eof: number of frames read is "+j);}
  catch(IOException e){System.out.println("error reading the file, please check..."); e.printStackTrace();}
   return 1;

  }

  public static void main(String[] args){

  preprocessCWRU ppc=new preprocessCWRU();
  //month. date. begin end
  try{
  int start=Integer.parseInt(args[2]);
  int end=Integer.parseInt(args[3]);
  String filename="";
  for (int i=start;i<end;i++){
   if (i<10) filename="00"+i+".log";
   else if (i<100) filename="0"+i+".log";
   else filename=""+i+".log";
   System.out.println("checking "+filename);
    ppc.OpenImageFile("\\\\Ie-workstation\\F\\DATA\\2002\\"+args[0]+"\\"+args[1]+"\\"+filename);
    //System.out.println("\\\\Ie-workstation\\F\\DATA\\2002\\July\\03\\");
   }
  }catch(Exception e){e.printStackTrace(); System.exit(0);}
  }
}
