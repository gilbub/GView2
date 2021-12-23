package gvdecoder;


import java.io.File;
import java.io.FileReader;
import java.util.Date;
import java.io.*;

public class fileChecker{

 public RandomAccessFile stream=null;



 public boolean printIfCompletelyWritten(File file) {
	    stream = null;
	    try {
	        stream = new RandomAccessFile(file, "rw");
	        System.out.println("" + stream.readLine());
	        stream.close();
	        System.exit(0); //lazy
	        return true;
	    } catch (Exception e) {
	        return false;
	      }
}



public static void main(String[] args) throws Exception {
        // parse arguments
        if (args.length == 0 || args.length > 1)
           return;

         fileChecker fC=new fileChecker();

         long currtime=System.currentTimeMillis();

         File file = new File(args[0]);


         for (int i=0;i<20;i++){
			 file=new File(args[0]);
			 long filetime=file.lastModified();
			 if (filetime-currtime>-1000)
			   fC.printIfCompletelyWritten(file);
			 java.lang.Thread.sleep(500);

		  }
          System.out.println("error in file read");


    }
}


