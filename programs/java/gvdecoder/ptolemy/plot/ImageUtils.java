package ptolemy.plot;

import java.awt.geom.*;
import java.util.*;
import java.awt.image.*;
import java.awt.image.renderable.*;
import javax.media.jai.*;
import java.io.*;
import java.awt.*;
public class ImageUtils{
//helper class that will save an image or a series of images.
public static int imagenum;
public static String absolutefilename;
public static String filename;

/*

public static void WriteImage(String outfile, String type){
     OutputStream os = new FileOutputStream(fileToWriteTo);
     BMPEncodeParam param = new BMPEncodeParam();
     ImageEncoder enc = ImageCodec.createImageEncoder("BMP", os,
                                                      param);
     enc.encode(op);
     os.close();


}

*/

public static void WriteImage(String outputFile, BufferedImage src, String imgtype){

try{



     FileOutputStream stream =
         new FileOutputStream(outputFile);
     JAI.create("encode", src, stream, imgtype, null);


     JAI.create("filestore", src, outputFile, imgtype, null);
     System.out.println("wrote "+outputFile +" as a "+imgtype);


}catch(IOException e){e.printStackTrace();}
}
public void test(){
	RenderedOp loadImage;
     //ParameterBlockJAI loadPB = new ParameterBlockJAI("fileload");
	//	        loadPB.set("input.jpg", "filename");
	//	        loadImage = JAI.create("fileload", loadPB);

     BufferedImage bi=new BufferedImage(100,100,BufferedImage.TYPE_BYTE_INDEXED);
     Graphics g=bi.getGraphics();
     g.setColor(Color.red);
     g.drawLine(10,10,20,20);
    // loadImage=
     WriteImage("newfile.bmp",bi,"bmp");



}

public static void main(String[] args){

	ImageUtils iu=new ImageUtils();
	iu.test();

}
}