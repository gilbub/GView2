package gvdecoder;
//package filters;

import java.awt.image.*;
import java.awt.image.renderable.*;
import javax.media.jai.*;
import javax.media.jai.operator.*;
import java.awt.geom.*;
import java.awt.*;
import javax.media.jai.iterator.*;


public class ColorFilter implements GeneralFilter{

RenderedOp op;
ParameterBlock pb;
TiledImage dst;
String description;
 public int [][]lt;

 private static int TILE_WIDTH = 128;
 private static int TILE_HEIGHT = TILE_WIDTH;
 private static int NUMBANDS = 3;
 Rectangle bounds = null;


public RenderedOp getOp(){return op;}

public int[] getHistogram(Object image) {
        // set up the histogram
        int[] bins = { 256 };
        double[] low = { 0.0D };
        double[] high = { 256.0D };

        Histogram histogram = new Histogram(bins, low, high);

        ParameterBlock pbtmp = new ParameterBlock();

        pbtmp.addSource(image);
        pbtmp.add(null);
        pbtmp.add(1);
        pbtmp.add(1);
        pbtmp.add(bins);
		pbtmp.add(low);
		pbtmp.add(high);

        RenderedOp op = JAI.create("histogram", pbtmp, null);
        histogram = (Histogram) op.getProperty("histogram");

        // get histogram contents
        int[] local_array = new int[histogram.getNumBins(0)];
        int max=0;
		for ( int i = 0; i < histogram.getNumBins(0); i++ ) {
            local_array[i] = histogram.getBinSize(0, i);
            if (local_array[i]>max) max=local_array[i];
		}
        //normalize
		double scale=(double)max/256.0;
		for (int j=0;j<local_array.length;j++) local_array[j]=(int)((double)local_array[j]/scale);
        return local_array;
    }




public ColorFilter(Object source, int[][] scriptlt){
int[] lookupInt=new int[256];

if (scriptlt==null){
ColorLookupDialog.initialize(null, lookupInt, getHistogram(source), "Filter", "Lookup Filters");
 lt= ColorLookupDialog.showDialog(null);
 //lookupInt = lci.looku

}else{

	lt=scriptlt;
}
//convernt source into a renderedOp, this is a stupid way
ParameterBlock pbtmp = new ParameterBlock();
     pbtmp.addSource(source);
     double[] constants = new double[1]; // or however many bands
     constants[0] = 0;
     pbtmp.add(constants);
     PlanarImage src = (PlanarImage)JAI.create("addconst", pbtmp, null);



 if(bounds == null) {
                 bounds = src.getBounds();
             } else {
                 bounds = bounds.intersection(src.getBounds());
                 if(bounds.isEmpty()) {
                    System.out.println("Null intersection");
                    System.exit(0);
                 }
            }

 if (dst==null){
  SampleModel sm =
              RasterFactory.createPixelInterleavedSampleModel(
                                                      DataBuffer.TYPE_BYTE,
                                                      TILE_WIDTH,
                                                      TILE_HEIGHT,
                                                      NUMBANDS);
          ColorModel cm = PlanarImage.createColorModel(sm);
          dst = new TiledImage(bounds.x, bounds.y,
                                          bounds.width, bounds.height,
                                          bounds.x, bounds.y,
                                        sm, cm);

 }


         int bands = src.getSampleModel().getNumBands();
         int height = src.getHeight();
         int width = src.getWidth();

         // used to access the source image
         RandomIter iter = RandomIterFactory.create(src, null);


             for (int line=0; line < height; line++) {
                 for (int samp=0; samp < width; samp++) {

                     int dn = iter.getSample(samp, line,0);
					  int red=dn;
					  int green=dn;
					  int blue=dn;
					  //if (dn<90) red=dn*2+60;
					 // else if (dn<180) green=dn+60;
					 // else blue=dn;
					 red=lt[0][dn];
					 blue=lt[1][dn];
					 green=lt[2][dn];

                    for (int band=0;band<NUMBANDS;band++){

					  if (band==0) dst.setSample(samp, line, band, red);
					  if (band==1) dst.setSample(samp, line, band, green);
                      if (band==2) dst.setSample(samp, line, band, blue);
					}
				 }
             }

	 pbtmp = new ParameterBlock();
     pbtmp.addSource(dst);
     constants = new double[NUMBANDS]; // or however many bands
     constants[0] = 0;
	 constants[1] = 0;
     constants[2] = 0;
	 pbtmp.add(constants);
     op = JAI.create("addconst", pbtmp, null);


}






public ColorFilter(Object source, String shape, int size){

}

public String toString(){
 return ("Color ");
}

public RenderedOp process(Object source){

  ParameterBlock pbtmp = new ParameterBlock();
       pbtmp.addSource(source);
       double[] constants = new double[1]; // or however many bands
       constants[0] = 0;
       pbtmp.add(constants);
     PlanarImage src = (PlanarImage)(JAI.create("addconst", pbtmp, null)).createInstance();

           int bands = src.getSampleModel().getNumBands();
           int height = src.getHeight();
           int width = src.getWidth();

           // used to access the source image
           RandomIter iter = RandomIterFactory.create(src, null);


               for (int line=0; line < height; line++) {
                   for (int samp=0; samp < width; samp++) {

                       int dn = iter.getSample(samp, line,0);
  					  int red=dn;
  					  int green=dn;
  					  int blue=dn;
  					  /*
  					  if (dn<80) green=dn*2;
  					  if (dn<160) {blue=dn+60; red=0;}
  					  if (dn>160) {green=green/2; blue=blue/2;}
                      if (dn>220) {green=0; blue=0;}
					  */
					   if ((dn>=0)&&(dn<256)){
					  	red=lt[0][dn];
					   	blue=lt[1][dn];
                       	green=lt[2][dn];
				       }

                       for (int band=0;band<NUMBANDS;band++){

  					  if (band==0) dst.setSample(samp, line, band, red);
  					  if (band==1) dst.setSample(samp, line, band, green);
                      if (band==2) dst.setSample(samp, line, band, blue);
  					}
  				 }
               }


  	pbtmp = new ParameterBlock();
       pbtmp.addSource(dst);
       constants = new double[NUMBANDS]; // or however many bands
       constants[0] = 0;
  	 constants[1] = 0;
     constants[2] = 0;
  	 pbtmp.add(constants);
     op = JAI.create("addconst", pbtmp, null);
   return op;
}

public RenderedOp reCreate(Object source){
 //pb.setSource(source,0);
 //if (source instanceof BufferedImage) op=JAI.create("Color",(BufferedImage)source,table);
 //else
 // op=JAI.create("Color",(RenderedOp)source,table);
 return op;
 }

}