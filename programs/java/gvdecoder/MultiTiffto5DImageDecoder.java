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


public class MultiTiffTo5DImageDecoder extends ImageDecoderAdapter {

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
public int subframe_xdim;
public int subframe_ydim;
public int frames_per_page;


public MultiTiffTo5DImageDecoder(int subframe_xdim, int subframe_ydim, int frames_per_page){
  this.subframe_xdim=subframe_xdim;
  this.subframe_ydim=subframe_ydim;
  this.frames_per_page=frames_per_page;
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
   public int ReturnFrameNumber(){return FrameNumber/frames_per_page;}
   public int JumpToFrame(int framenum, int instance){FrameNumber=framenum*frames_per_page; return FrameNumber;}


public int ReturnXYBandsFrames(int[] arr, int instance){
	try{
	loadImage =  dec.decodeAsRenderedImage(FrameNumber);

	int bands = loadImage.getSampleModel().getNumBands();
	int height = loadImage.getHeight();
	int width = loadImage.getWidth();
	int frames=dec.getNumPages();
	arr[0]=width*frames_per_page;
	arr[1]=height;
	arr[2]=bands;
	arr[3]=frames/frames_per_page;
	}catch (Exception e){e.printStackTrace();}
	return 1;
}


public int line,band,samp,val;

public int UpdateImageArray(int[] arr, int xdim, int ydim, int instance){
for (int f=0;f<frames_per_page;f++){
	try{
	loadImage =dec.decodeAsRenderedImage(FrameNumber+f);

	}catch(Exception e){e.printStackTrace();}



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
		      //[y*(width*frames_per_page)+(x + width+f
			arr[line*(width*frames_per_page)+samp+width*f]=val;
			//if ((line==(int)height/2)&&(samp==(int)width/2)) {System.out.println(val+" "+bands);}
		}
	}
  }
FrameNumber+=frames_per_page;
System.out.println("exiting imageupdate in multitiff decoder."+FrameNumber);
return 1;
}
}


