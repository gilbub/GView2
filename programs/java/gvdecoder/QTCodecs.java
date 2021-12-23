package gvdecoder;



import quicktime.*;
import quicktime.io.*;
import quicktime.qd.*;
import quicktime.std.*;
import quicktime.std.movies.*;
import quicktime.std.movies.media.*;
import quicktime.std.image.*;
import quicktime.util.*;
/** This should only be called by QuickTimeEncoder.java. If called elsewhere, execute QTSession.close() **/
public class QTCodecs{
  public int[] codecs;
  public String[] names;

  public String[] excludenames={"Planar RGB",
  								"BMP",
  								"H.264",
  								"DV/DVCPRO - NTSC",
  								"DV - PAL",
  								"DVCPRO - PAL",
  								"Photo - JPEG",
  								"JPEG 2000",
  								"Motion JPEG A",
  								"Motion JPEG B",
  								"PNG",
  								"None",
  								"TGA",
  								"TIFF",
  								"Component Video"};


  public QTCodecs(){
	 try{
	 if (!QTSession.isInitialized()) QTSession.open();
     CodecNameList cnl=new CodecNameList(1);
     int useablecodecs=cnl.getCount()-excludenames.length;

     codecs=new int[useablecodecs];
     names=new String[useablecodecs];
     int index=0;
     for (int i=0;i<cnl.getCount();i++){

       CodecName cn=cnl.getNth(i);
       String name=cn.getTypeName();
       boolean exclude=false;
       for (int j=0;j<excludenames.length;j++){
		   if (name.equals(excludenames[j])){
		                      exclude=true;
		                      break;
						  }
	   }
	   if (!exclude){
         names[index]=name;
         codecs[index]=cn.getCType();
         index++;
       }
     }
    //QTSession.close();

  }catch(Exception e){;}

  }
}