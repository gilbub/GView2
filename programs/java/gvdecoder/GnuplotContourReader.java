package gvdecoder;
 import java.io.*;
 import java.util.*;
 import java.util.regex.*;
 import java.awt.*;
 import java.awt.geom.Area;
 import java.awt.geom.GeneralPath;
 import javax.swing.*;


 public class GnuplotContourReader{
 public int[][] arr;
 public Vector v;
 public int index=0;
 public Vector all;

 public GnuplotContourReader(String filename){

 all=new Vector();

 Pattern p=Pattern.compile("\\s");
 try{
	  File f=new File(filename);
	  FileReader pr=new FileReader(f);
	  BufferedReader bi=new BufferedReader(pr);

	 try{
	  v=new Vector();
	  while(true){
	  String line=bi.readLine();
	  if (line==null) break;
      if (line.length()<2){
	   if (v!=null) all.add(v);
	   v=new Vector();
	  }
	  //ignore lines starting with #
	  if ((!line.startsWith("#"))&&(line.length()>0))
	  {
	  String[] s=p.split(line);
	  Vector oneline=new Vector();
	  for (int j=0;j<s.length;j++){
	   if (s[j].length()>0)
	   oneline.add(new Float(s[j].trim()));

	   }
      float[] tmp=new float[oneline.size()];
      for (int k=0;k<oneline.size();k++){tmp[k]=((Float)oneline.elementAt(k)).floatValue();}
	  v.add(tmp);
          }
	  }
	 }
	 catch(NumberFormatException e){e.printStackTrace();}
	 catch(EOFException e){e.printStackTrace();}
	 catch(Exception e){e.printStackTrace();}


	 }catch(IOException e){e.printStackTrace();}
 }

public float[] getLevels(){
	Vector contours=new Vector();
	for (int i=0;i<all.size();i++){
		Vector v=(Vector)all.elementAt(i);
		for (int j=0;j<v.size();j++){
			float[] tmp=(float[])v.elementAt(j);
	//		System.out.println(tmp[0]+" "+tmp[1]+" "+tmp[2]);
	        if ( contours.indexOf(new Float(tmp[2])) ==-1)
	         contours.add(new Float(tmp[2]));
	      }
		}
		float[] res=new float[contours.size()];
		for (int k=0;k<contours.size();k++){
		  res[k]=((Float)contours.elementAt(k)).floatValue();
	  }
      return res;
	}

public Vector getContours(float level, float scale){

	Vector contours=new Vector();
	for (int i=0;i<all.size();i++){

			Vector v=(Vector)all.elementAt(i);
	        if (v.size()>0){
			float tmp=((float[])v.elementAt(0))[2];
			if (tmp==level){
			GeneralPath p1 = new GeneralPath();
	        p1.moveTo(((float[])v.elementAt(0))[0]*scale,((float[])v.elementAt(0))[1]*scale);
			for (int j=1;j<v.size();j++){
				float[] tp=(float[])v.elementAt(j);
		        p1.lineTo(tp[0]*scale,tp[1]*scale);
		        }
		        //p1.closePath();
			    contours.add(p1);
			  }
		   }
		  }
         return contours;
	 }








public static void main(String[] arg){

 GnuplotContourReader gc=new GnuplotContourReader("testgnu2.dat");
 System.out.println("read in "+gc.all.size());
 //System.out.println( gc.findLevels());
 System.out.println(gc.getContours(0.05f,10.0f));


}
}