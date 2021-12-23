package gvdecoder;
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




public class JavaSeriesDecoder extends ImageDecoderAdapter {


 public static final int RED=0;
 public static final int GREEN=1;
 public static final int BLUE=2;
 public static final int LUMINANCE=3;



 public int outmode=LUMINANCE;

public int FrameNumber=0;
public ArrayList images;
String filetype; //.jpg, .tif, .bmp, etc
public String imagepath="";
JFrame debugFrame;
ImageCanvas imagecanvas=null;
public PlanarImage loadImage=null;
public PlanarImage dst=null;

public JavaSeriesDecoder(String filetype){
 FrameNumber=0;
 this.filetype=filetype;
 images=new ArrayList();
}

public int FilterOperation(int OperationCode, int startx, int endx, int instance){return 0;}

public int CloseImageFile(int instance){return -1;}

public int OpenImageFile(String filename){
//asumes filename is a directory that contains a series of images.
//gets the series in alphabetical order, puts each in a list


 File dir=new File(filename);
 System.out.println("the file information is "+dir+" filetype="+filetype);
 String[] tmp=null;
 if (!dir.exists()) {System.out.println("directory not found"); return 0;}
 if (!dir.isDirectory()){
	 System.out.println("the file isn't a directory");
     images.add(dir.getName());
     imagepath=dir.getParent();
     }
 else{
 if (dir.isDirectory()){
   imagepath=filename;
   tmp=dir.list();
  }
  Arrays.sort(tmp);
 for (int i=0;i<tmp.length;i++){
  if (tmp[i].endsWith(filetype) || tmp[i].endsWith(filetype.toUpperCase())) images.add(tmp[i]);
  }
}
  System.out.println("ending OpenImageFile with "+images.size()+" images in directory, path="+imagepath);
 return 1;
}
   public int FilterOperation(int OperationCode){return -1;}
   public String ReturnSupportedFilters(){return("no filters supported");}
   public int ReturnFrameNumber(){return FrameNumber;}
   public int JumpToFrame(int framenum, int instance){if (framenum<images.size()) FrameNumber=framenum; return FrameNumber;}


public int ReturnXYBandsFrames(int[] arr, int instance){
	String fileName=imagepath+File.separator+(String)images.get(FrameNumber);
	System.out.println("filename in returnxybansframes="+fileName);
	loadImage = JAI.create("fileload", fileName);
	int bands = loadImage.getSampleModel().getNumBands();
    height = loadImage.getHeight();
    width = loadImage.getWidth();
	int frames=images.size();
	arr[0]=width;
	arr[1]=height;
	arr[2]=bands;
	arr[3]=frames;
	return 1;
}

public int getWidth(){return height;}
public int getHeight(){return width;}
public String fileName="";
public int height;
public int width;
public int UpdateImageArray(int[] arr, int xdim, int ydim, int instance){

fileName=imagepath+File.separator+(String)images.get(FrameNumber);
loadImage = JAI.create("fileload", fileName);
System.out.println("\n\n**************\n loaded ="+fileName);

//attempt to display the PlanarImage
/*
if (debugFrame==null){
 debugFrame = new JFrame("file="+fileName);
 imagecanvas = new ImageCanvas(loadImage);
 debugFrame.getContentPane().add(imagecanvas);
 debugFrame.pack();
 debugFrame.show();
}
else {imagecanvas.set(loadImage);}
*/


height = loadImage.getHeight();
width = loadImage.getWidth();
if (arr.length!=width*height){
	arr=new int[width*height];
}

// used to access the source image

  dst = null;

          if(loadImage.getColorModel() instanceof IndexColorModel) {
              IndexColorModel icm = (IndexColorModel)loadImage.getColorModel();
              byte[][] data = new byte[3][icm.getMapSize()];

              icm.getReds(data[0]);
              icm.getGreens(data[1]);
              icm.getBlues(data[2]);

              LookupTableJAI lut = new LookupTableJAI(data);

              dst = JAI.create("lookup", loadImage, lut);
          } else {
              dst = loadImage;
          }


RandomIter iter = RandomIterFactory.create(dst, null);
int bands = dst.getSampleModel().getNumBands();

if (bands==1){
	for (int line=0; line < height; line++) {
			for (int samp=0; samp < width; samp++) {

			arr[line*width+samp]=iter.getSample(samp, line, 0);
			//if ((line==(int)height/2)&&(samp==(int)width/2)) {System.out.println(val+" "+bands);}
		}
      }

}
else

if ((bands==3)&&(outmode!=LUMINANCE)){
	for (int line=0; line < height; line++) {
			for (int samp=0; samp < width; samp++) {
			arr[line*width+samp]=iter.getSample(samp,line,outmode);
			//if ((line==(int)height/2)&&(samp==(int)width/2)) {System.out.println(val+" "+bands);}
		}
}


}else //in this case, gets only sum, as rgb not there.
{
for (int line=0; line < height; line++) {
		for (int samp=0; samp < width; samp++) {
		  int val=0;
		   for (int band=0; band < bands; band++) {
			val += iter.getSample(samp, line, band);
		   }
		arr[line*width+samp]=val;
		//if ((line==(int)height/2)&&(samp==(int)width/2)) {System.out.println(val+" "+bands);}
	}
}
}
if (FrameNumber<images.size()-1) FrameNumber++;
System.out.println("exiting imageupdate in javaseries decoder.");
return 1;
}
}


