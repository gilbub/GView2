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

import com.sun.media.jai.codec.FileSeekableStream;
import com.sun.media.jai.codec.ImageDecoder;
import com.sun.media.jai.codec.ImageCodec;
import com.sun.media.jai.codec.TIFFEncodeParam;

import java.awt.image.RenderedImage;
import javax.media.jai.RenderedOp;
import javax.media.jai.JAI;
import java.awt.image.renderable.ParameterBlock;


public class MultiPageTiffDecoder extends ImageDecoderAdapter {


 public static final int RED=0;
 public static final int GREEN=1;
 public static final int BLUE=2;
 public static final int LUMINANCE=3;



 public int outmode=LUMINANCE;

public int FrameNumber=0;
public int count;
String filetype; //.jpg, .tif, .bmp, etc
public String imagepath="";
JFrame debugFrame;
ImageCanvas imagecanvas=null;
public RenderedImage loadImage=null;
public RenderedImage dst=null;

public FileSeekableStream ss;
public com.sun.media.jai.codec.ImageDecoder dec;


public MultiPageTiffDecoder(String filetype){
 FrameNumber=0;
 this.filetype=filetype;

}

public int FilterOperation(int OperationCode, int startx, int endx, int instance){return 0;}

public int CloseImageFile(int instance){return -1;}

public int OpenImageFile(String filename){
//asumes filename is a directory that contains a series of images.
//gets the series in alphabetical order, puts each in a list
 try{
        FileSeekableStream ss = new FileSeekableStream(filename);
        ImageDecoder dec = ImageCodec.createImageDecoder("tiff", ss, null);
        count = dec.getNumPages();
}catch(IOException e){e.printStackTrace();}

  System.out.println("ending OpenImageFile with "+count+" images in directory, path="+imagepath);
 return 1;
}
   public int FilterOperation(int OperationCode){return -1;}
   public String ReturnSupportedFilters(){return("no filters supported");}
   public int ReturnFrameNumber(){return FrameNumber;}
   public int JumpToFrame(int framenum, int instance){if (framenum<count) FrameNumber=framenum; return FrameNumber;}


public int ReturnXYBandsFrames(int[] arr, int instance){
 try{
	loadImage = dec.decodeAsRenderedImage(0);
	int bands = loadImage.getSampleModel().getNumBands();
    height = loadImage.getHeight();
    width = loadImage.getWidth();
	//nt frames=images.size();
	arr[0]=width;
	arr[1]=height;
	arr[2]=bands;
	arr[3]=count;
}catch (IOException e){e.printStackTrace();}
	return 1;
}

public int getWidth(){return height;}
public int getHeight(){return width;}
public String fileName="";
public int height;
public int width;
public int UpdateImageArray(int[] arr, int xdim, int ydim, int instance){
	try{
    RenderedImage loadImage = dec.decodeAsRenderedImage(0);

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
if (FrameNumber<count-1) FrameNumber++;
}catch(IOException e){e.printStackTrace();}
System.out.println("exiting imageupdate in javaseries decoder.");
return 1;
}
}


