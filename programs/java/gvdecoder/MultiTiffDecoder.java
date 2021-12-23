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


public class MultiTiffDecoder extends ImageDecoderAdapter {

public int FrameNumber=0;
public ArrayList images;
String filetype; //.jpg, .tif, .bmp, etc
String imagepath="";
JFrame debugFrame;
ImageCanvas imagecanvas=null;
public RenderedImage loadImage=null;
public RenderedImage dst=null;
public FileSeekableStream ss;
public ImageDecoder dec;

public MultiTiffDecoder(){

}

public int FilterOperation(int OperationCode, int startx, int endx, int instance){return 0;}

public int CloseImageFile(int instance){return -1;}

public int OpenImageFile(String filename){
//asumes filename is a directory that contains a series of images.
//gets the series in alphabetical order, puts each in a list
int count=1;
try{
 ss= new FileSeekableStream(filename);
 dec = ImageCodec.createImageDecoder("tiff", ss, null);
 count = dec.getNumPages();

}catch(Exception e){e.printStackTrace();}

 System.out.println("ending OpenImageFile with "+count+" images in directory");
 return 1;
}
   public int FilterOperation(int OperationCode){return -1;}
   public String ReturnSupportedFilters(){return("no filters supported");}
   public int ReturnFrameNumber(){return FrameNumber;}
   public int JumpToFrame(int framenum, int instance){FrameNumber=framenum; return FrameNumber;}


public int ReturnXYBandsFrames(int[] arr, int instance){
	try{
	loadImage =  dec.decodeAsRenderedImage(FrameNumber);

	int bands = loadImage.getSampleModel().getNumBands();
	int height = loadImage.getHeight();
	int width = loadImage.getWidth();
	int frames=dec.getNumPages();
	arr[0]=width;
	arr[1]=height;
	arr[2]=bands;
	arr[3]=frames;
	}catch (Exception e){e.printStackTrace();}
	return 1;
}


public int line,band,samp,val;

public int UpdateImageArray(int[] arr, int xdim, int ydim, int instance){

try{
loadImage =dec.decodeAsRenderedImage(FrameNumber);

}catch(Exception e){e.printStackTrace();}
//loadImage = JAI.create("fileload", fileName);
//System.out.println("\n\n**************\n loaded ="+fileName);

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


int height = loadImage.getHeight();
int width = loadImage.getWidth();


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
for (line=0; line < height; line++) {
		for (samp=0; samp < width; samp++) {
		  val=0;
		   for ( band=0; band < bands; band++) {
			val += iter.getSample(samp, line, band);
		}
		arr[line*width+samp]=val;
		//if ((line==(int)height/2)&&(samp==(int)width/2)) {System.out.println(val+" "+bands);}
	}
}
FrameNumber++;
System.out.println("exiting imageupdate in multitiff decoder."+FrameNumber);
return 1;
}
}


