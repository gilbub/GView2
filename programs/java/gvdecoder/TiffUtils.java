package gvdecoder;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.awt.image.Raster;
import java.awt.image.ColorModel;
import java.awt.Point;

import javax.media.jai.NullOpImage;
import javax.media.jai.OpImage;
import javax.media.jai.PlanarImage;

import com.sun.media.jai.codec.ImageEncoder;
import com.sun.media.jai.codec.SeekableStream;
import com.sun.media.jai.codec.FileSeekableStream;
import com.sun.media.jai.codec.ImageDecoder;
import com.sun.media.jai.codec.ImageCodec;
import com.sun.media.jai.codec.TIFFEncodeParam;

public class TiffUtils {

 public static BufferedImage deepCopy(BufferedImage bi) {
  ColorModel cm = bi.getColorModel();
  boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
  WritableRaster raster = bi.copyData(null);
  return new BufferedImage(cm, raster, isAlphaPremultiplied, null);
}


 public static BufferedImage deepCopy(BufferedImage bi, int sx, int sy, int width, int height){
  ColorModel cm = bi.getColorModel();
  boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
  Raster tmp=bi.getData(new java.awt.Rectangle(sx,sy,width,height));
  WritableRaster wr=Raster.createWritableRaster(tmp.getSampleModel(),tmp.getDataBuffer(),new Point(0,0));
  return new BufferedImage(cm,wr, isAlphaPremultiplied,null);
  //raster.createCompatibleWritableRaster()
 }

 public static boolean encode(List <BufferedImage>list,String outputpath){
	  try{
	   TIFFEncodeParam params = new TIFFEncodeParam();
	   params.setCompression(TIFFEncodeParam.COMPRESSION_DEFLATE);
	   params.setExtraImages(list.listIterator(1));
	   OutputStream out = new FileOutputStream(outputpath);
	   ImageEncoder encoder = ImageCodec.createImageEncoder("tiff", out, params);
       encoder.encode(list.get(0));
       out.close();
      }catch(Exception e){e.printStackTrace(); return false;}
      return true;
  }


 public static void extract_one_cam(Viewer2 vw, int camnum, Matrix ma){
	 for (int z=0;z<ma.zdim;z++){
		 vw.SilentJumpToFrame(z*7+camnum,false);
		 for (int y=0;y<ma.ydim;y++){
			 for (int x=0;x<ma.xdim;x++){
				    ma.dat.set(z,y,x,vw.datArray[y*ma.xdim+x]);
				}
			}
       }
 }


 public static void extract_all(Viewer2 vw, int st, int en, Matrix ma){
 	 for (int z=st;z<en;z++){
      for (int c=0;c<7;c++){
 		 vw.SilentJumpToFrame(z*7+c,false);
 		 for (int y=0;y<400;y++){
 			 for (int x=0;x<400;x++){
 				    ma.dat.set(z-st,y,x+400*c,vw.datArray[y*400+x]);
 				}
 			}
        }
     }
  }
	 /*

	 def splitmultitiff(vw,camnum):
	  ma=Matrix(950,400,400)
	  for i in range(ma.zdim):
	   vw.SilentJumpToFrame(i*7+camnum,0)
	   for y in range(ma.ydim):
	    for x in range(ma.xdim):
	     ma.dat.set(i,y,x,vw.datArray[y*ma.xdim+x])
 return ma
	 */


  public static void main(String[] args) throws Exception {

    // 2 single page TIF to be in a multipage
    String [] tifs = {
        "C:/temp/test01.tif",
        "C:/temp/test02.tif"
    };
    int numTifs = tifs.length;  // 2 pages

    BufferedImage image[] = new BufferedImage[numTifs];
    for (int i = 0; i < numTifs; i++) {
        SeekableStream ss = new FileSeekableStream(tifs[i]);
        ImageDecoder decoder = ImageCodec.createImageDecoder("tiff", ss, null);
        PlanarImage pi = new NullOpImage
            (decoder.decodeAsRenderedImage(0),null,null,OpImage.OP_IO_BOUND);
        image[i] = pi.getAsBufferedImage();
        ss.close();
    }

    TIFFEncodeParam params = new TIFFEncodeParam();
    params.setCompression(TIFFEncodeParam.COMPRESSION_DEFLATE);
    OutputStream out = new FileOutputStream("C:/temp/multipage.tif");
    ImageEncoder encoder = ImageCodec.createImageEncoder("tiff", out, params);
    List <BufferedImage>list = new ArrayList<BufferedImage>(image.length);
    for (int i = 1; i < image.length; i++) {
        list.add(image[i]);
    }
    params.setExtraImages(list.iterator());
    encoder.encode(image[0]);
    out.close();

    System.out.println("Done.");
  }
}