package gvdecoder;

import java.awt.*;
import java.awt.image.*;
import java.awt.geom.*;
import java.awt.Graphics2D.*;
import java.util.*;
import java.awt.image.WritableRaster;

import java.awt.image.renderable.*;
import javax.media.jai.*;
import javax.media.jai.operator.*;

public class DataToAwt{

public int imageWidth=10;
public int imageHeight=10;

public void print(float[] data){
	for (int i=0;i<imageHeight;i++){
		for (int j=0;j<imageWidth;j++){
			System.out.print(data[i*imageWidth+j]+" ");
		}
		System.out.println("");
	}
}


  public static RenderedImage createRenderedImage(float[][] theData,
                                                  int width,
                                                  int height,
                                                  int numBands) {
    int len = width * height;
    Point origin = new Point(0,0);

    // create a float sample model
    SampleModel sampleModel =
      RasterFactory.createBandedSampleModel(DataBuffer.TYPE_FLOAT,
                                            width,
                                            height,
                                            numBands);

    // create a compatible ColorModel
    ColorModel colourModel =
      PlanarImage.createColorModel(sampleModel);

    // create a TiledImage using the float SampleModel
    TiledImage tiledImage = new TiledImage(origin,
                                           sampleModel,
                                           width,
                                           height);

    // create a DataBuffer from the float[][] array
    javax.media.jai.DataBufferFloat dataBuffer = new javax.media.jai.DataBufferFloat(theData, len);

    // create a Raster
    Raster raster = RasterFactory.createWritableRaster(sampleModel,
                                                       dataBuffer,
                                                       origin);

    // set the TiledImage data to that of the Raster
    tiledImage.setData(raster);

    RenderedImageAdapter img =
      new RenderedImageAdapter((RenderedImage)tiledImage);

    return img;
  }

public Raster rotate(RenderedImage rendered){


	Raster result=null;

	try
			{
				ParameterBlock params= new ParameterBlock();
				params.addSource(rendered);
				params.add(5);
				params.add(5);
				params.add(0.2);
				params.add(new InterpolationNearest());
				RenderedOp rotatedImg = JAI.create("rotate",params,null);
				result=rotatedImg.getData();
				//rotatedImg.getAsBufferedImage();  // to execute the rotation
				//System.out.println("Rotated Image boundaries: "+rotatedImg.getWidth()+","+rotatedImg.getHeight());
			}
			catch (Throwable ee)
			{
				ee.printStackTrace();
		}
		return result;
}


public void create(){
  float data[][] = new float[1][imageWidth*imageHeight];
        int ratio = 16/imageHeight;
        for ( int i = 0; i < imageHeight; i++ ) {
            for ( int j = 0; j < imageWidth; j++ ) {
                data[0][i*imageWidth+j] = (float)(i*ratio);
            }
        }

  RenderedImage ren=createRenderedImage(data,10,10,1);
  print(ren.getData().getPixels(0,0,imageWidth,imageHeight,(float[])null));
  Raster res=rotate(ren);
  print(((javax.media.jai.DataBufferFloat)res.getDataBuffer()).getData());

/*
  javax.media.jai.DataBufferDouble dataBuffer = new javax.media.jai.DataBufferDouble ( (double[])data , imageWidth );

  print(dataBuffer.getData());

  BandedSampleModel sm= new BandedSampleModel(DataBuffer.TYPE_DOUBLE, imageWidth, imageHeight, 1);

  WritableRaster raster=Raster.createWritableRaster( sm, dataBuffer, new Point(0,0));

  AffineTransform tx = new AffineTransform();
  //tx.scale(scalex, scaley);
  //tx.shear(shiftx, shifty);
  //tx.translate(x, y);
  //tx.rotate(0.0, imageWidth/2, imageHeight/2);
  tx.scale(0.5 ,0.5); //Note This

  AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_BILINEAR);
    //bufferedImage = op.filter(bufferedImage, null);
  WritableRaster newraster=op.createCompatibleDestRaster(raster);
  op.filter(raster,newraster);
  System.out.println("rotated \n\n");
  print(((javax.media.jai.DataBufferDouble)newraster.getDataBuffer()).getData());
*/




}

public static void main(String[] arg){

 DataToAwt dt=new DataToAwt();
 dt.create();

}


}
