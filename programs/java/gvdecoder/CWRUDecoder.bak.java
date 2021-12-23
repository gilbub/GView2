import java.io.*;
import java.util.*;


import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.awt.geom.*;

import java.awt.image.*;
import java.awt.image.renderable.*;
import javax.media.jai.*;
import javax.media.jai.iterator.*;
import javax.media.jai.widget.*;
import java.awt.color.*;
import com.sun.media.jai.codec.*;

import log.*;
import gvdecoder.utilities.*;





public class CWRUDecoder extends ImageDecoderAdapter {
FileSeekableStream CWRUfile;
int FrameNumber=0;
ArrayList images;
String filename;
String imagepath="";
long frameposition=0;
int orientation=2;
byte[] internalframe=new byte[528];
public static String navfilename;

public CWRUDecoder(String filetype){
 FrameNumber=0;
 this.filename=filename;
}

public int FilterOperation(int OperationCode, int startx, int endx, int instance){return 0;}

public int CloseImageFile(int instance){return -1;}

//public int FilterOperation(int a, int b, int c, int d){ return 0;}

public int OpenImageFile(String filename){
 int j=0;
 //open the file...
 try{

      navfilename=FileNameManager.getInstance().FindNavFileName(filename,"");
      File tmp=new File(navfilename);
      CWRUfile=new FileSeekableStream(new RandomAccessFile(filename,"r"));

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
	  					  +CWRUfile.readShort()   //yellow
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

   public int FilterOperation(int OperationCode){return -1;}
   public String ReturnSupportedFilters(){return("no filters supported");}
   public int ReturnFrameNumber(){return FrameNumber;}
   public int JumpToFrame(int framenum, int instance){
	   //seek to the right location in the file...

	   try {
	   CWRUfile.seek(framenum*(264)*2);
       }catch(IOException e){System.out.println("error jumping in CWRUDecoder");}
	   return FrameNumber;
	   }


public int ReturnXYBandsFrames(int[] arr){
	//String fileName=imagepath+File.separator+(String)images.get(FrameNumber);

	//int frames=images.size();
	arr[0]=16;
	arr[1]=16;
	arr[2]=1;
	arr[3]=2800;
	return 1;
}

public int UpdateImageArray(int[] arr, int xdim, int ydim, int instance){
    byte[] cwrubytes=new byte[16];
    int[][] from=new int[16][16];
    int[][] to=new int[16][16];

    int val=0;
    try{

	 switch(orientation){
	  case 0: for (int i=0;i<xdim*ydim;i++){
		       val=(int)CWRUfile.readShort();
		       arr[i]=Short.MAX_VALUE-val;
		       }
	          break;
	  case 1:
	        for (int i=0;i<xdim*ydim;i++){val=(int)CWRUfile.readShort();arr[xdim*ydim-i-1]=Short.MAX_VALUE-val;}
	        break;

	  case 2:{
	      for (int i=0;i<xdim;i++){
	       for (int j=0;j<ydim;j++){
			 val=(int)CWRUfile.readShort();
				from[j][xdim-i-1]=Short.MAX_VALUE-val;;
			  }
			}
		   for (int i=0;i<xdim;i++){
	        for (int j=0;j<ydim;j++){
				arr[i*xdim+j]=from[i][j];
			}
		   }
	       }
	       break;



     }

   CWRUfile.readFully(cwrubytes);
}catch(IOException e){System.out.println("error reading into filebuffer in CWRUDecoder");}
FrameNumber++;
return 1;
}
}


