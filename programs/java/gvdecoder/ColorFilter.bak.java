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
  int [][]lt;
/*

int lt[][]={
			{	0,	0,	0	},
			{	0,	0,	0	},
			{	0,	0,	0	},
			{	0,	0,	0	},
			{	0,	0,	0	},
			{	0,	0,	0	},
			{	0,	0,	0	},
			{	0,	0,	0	},
			{	0,	0,	0	},
			{	0,	0,	0	},
			{	0,	0,	0	},
			{	0,	0,	0	},
			{	0,	0,	0	},
			{	0,	0,	0	},
			{	0,	0,	0	},
			{	0,	0,	0	},
			{	0,	0,	0	},
			{	0,	0,	0	},
			{	0,	0,	0	},
			{	0,	0,	0	},
			{	0,	0,	0	},
			{	0,	0,	0	},
			{	0,	0,	0	},
			{	0,	0,	0	},
			{	0,	0,	0	},
			{	0,	0,	0	},
			{	0,	0,	0	},
			{	0,	0,	0	},
			{	0,	0,	0	},
			{	0,	0,	0	},
			{	0,	0,	0	},
			{	0,	0,	0	},
			{	0,	0,	0	},
			{	0,	0,	0	},
			{	0,	0,	0	},
			{	0,	0,	0	},
			{	0,	0,	0	},
			{	0,	0,	0	},
			{	0,	0,	0	},
			{	0,	0,	0	},
			{	0,	0,	0	},
			{	0,	0,	0	},
			{	0,	0,	0	},
			{	0,	0,	0	},
			{	0,	0,	0	},
			{	0,	0,	0	},
			{	0,	0,	0	},
			{	0,	0,	0	},
			{	0,	0,	0	},
			{	0,	0,	0	},
			{	0,	0,	0	},
			{	0,	0,	0	},
			{	0,	0,	0	},
			{	0,	0,	0	},
			{	0,	0,	0	},
			{	0,	0,	0	},
			{	0,	0,	0	},
			{	0,	0,	0	},
			{	0,	0,	0	},
			{	0,	0,	0	},
			{	0,	0,	0	},
			{	0,	0,	0	},
			{	0,	0,	0	},
			{	0,	0,	0	},
			{	0,	0,	0	},
			{	0,	0,	0	},
			{	0,	0,	0	},
			{	0,	0,	0	},
			{	0,	0,	0	},
			{	0,	0,	0	},
			{	0,	0,	0	},
			{	0,	0,	0	},
			{	0,	0,	0	},
			{	0,	0,	0	},
			{	0,	0,	0	},
			{	0,	0,	0	},
			{	0,	0,	0	},
			{	0,	0,	0	},
			{	0,	0,	0	},
			{	0,	0,	0	},
			{	0,	0,	0	},
			{	0,	0,	0	},
			{	0,	0,	0	},
			{	0,	0,	0	},
			{	0,	0,	0	},
			{	0,	0,	0	},
			{	0,	0,	0	},
			{	0,	0,	0	},
			{	0,	0,	0	},
			{	0,	0,	0	},
			{	0,	0,	0	},
			{	0,	0,	0	},
			{	0,	0,	0	},
			{	0,	0,	0	},
			{	0,	0,	0	},
			{	0,	0,	0	},
			{	0,	0,	0	},
			{	0,	0,	0	},
			{	0,	0,	0	},
			{	0,	0,	0	},
			{	0,	0,	0	},
			{	0,	0,	0	},
			{	0,	0,	0	},
			{	0,	0,	0	},
			{	0,	0,	0	},
			{	0,	0,	0	},
			{	0,	0,	0	},
			{	0,	0,	0	},
			{	0,	0,	0	},
			{	0,	0,	0	},
			{	0,	0,	0	},
			{	0,	0,	0	},
			{	0,	0,	0	},
			{	0,	0,	0	},
			{	0,	0,	0	},
			{	0,	0,	0	},
			{	0,	0,	0	},
			{	0,	0,	0	},
			{	0,	0,	0	},
			{	0,	0,	0	},
			{	0,	0,	0	},
			{	0,	0,	0	},
			{	0,	0,	0	},
			{	0,	5,	0	},
			{	0,	10,	0	},
			{	0,	15,	0	},
			{	0,	20,	0	},
			{	0,	25,	0	},
			{	0,	30,	0	},
			{	0,	35,	0	},
			{	0,	40,	0	},
			{	0,	45,	0	},
			{	0,	50,	0	},
			{	0,	55,	0	},
			{	0,	60,	0	},
			{	0,	61,	0	},
			{	0,	62,	0	},
			{	0,	63,	0	},
			{	0,	64,	0	},
			{	0,	65,	0	},
			{	0,	66,	0	},
			{	0,	67,	0	},
			{	0,	68,	0	},
			{	0,	69,	0	},
			{	0,	70,	0	},
			{	0,	72,	0	},
			{	0,	73,	0	},
			{	0,	74,	0	},
			{	0,	75,	0	},
			{	0,	76,	0	},
			{	0,	77,	0	},
			{	0,	78,	0	},
			{	0,	79,	0	},
			{	0,	80,	0	},
			{	0,	81,	0	},
			{	0,	82,	0	},
			{	0,	83,	0	},
			{	0,	84,	0	},
			{	0,	85,	0	},
			{	0,	86,	0	},
			{	0,	87,	0	},
			{	0,	88,	0	},
			{	0,	89,	0	},
			{	0,	90,	0	},
			{	0,	91,	0	},
			{	0,	92,	0	},
			{	0,	93,	0	},
			{	0,	94,	0	},
			{	0,	95,	0	},
			{	0,	96,	0	},
			{	0,	97,	0	},
			{	0,	98,	0	},
			{	5,	99,	0	},
			{	10,	100,	0	},
			{	15,	105,	0	},
			{	20,	110,	0	},
			{	25,	115,	0	},
			{	30,	120,	0	},
			{	35,	125,	0	},
			{	40,	130,	0	},
			{	45,	135,	0	},
			{	50,	138,	0	},
			{	55,	140,	0	},
			{	60,	142,	0	},
			{	65,	144,	0	},
			{	70,	145,	0	},
			{	75,	146,	0	},
			{	80,	147,	0	},
			{	85,	148,	0	},
			{	90,	149,	0	},
			{	95,	150,	0	},
			{	100,151,	0	},
			{	105,150,	0	},
			{	110,145,	0	},
			{	115,140,	0	},
			{	120,135,	0	},
			{	125,130,	0	},
			{	130,125,	0	},
			{	135,120,	0	},
			{	140,115,	0	},
			{	145,110,	0	},
			{	150,100,	0	},
			{	155,100,	0	},
			{	160,100,	0	},
			{	165,100,	0	},
			{	170,100,	0	},
			{	175,100,	0	},
			{	180,100,	0	},
			{	185,100,	0	},
			{	190,100,	0	},
			{	195,100,	0	},
			{	200,100,	0	},
			{	205,100,	0	},
			{	210,100,	0	},
			{	215,100,	0	},
			{	220,100,	0	},
			{	225,100,	0	},
			{	230,100,	0	},
			{	235,100,	0	},
			{	240,100,	0	},
			{	245,100,	0	},
			{	250,100,0	},
			{	255,100,10 },
			{	255,100,20	},
			{	255,100,30	},
			{	255,100,40	},
			{	255,100,50	},
			{	255,100,60	},
			{	255,100,70  },
			{	255,100,80	},
			{	255,100,90 	},
			{	255,100,100 },
			{	255,110,110 },
			{	255,120,120 },
			{	255,130,130 },
			{	255,140,140 },
			{	255,150,150 },
			{	255,140,140 },
			{	255,150,150 },
			{	255,160,160 },
			{	255,170,170 },
			{	255,180,180 },
			{	255,190,190 },
			{	255,195,195 },
			{	255,200,200 },
			{	255,205,205 },
			{	255,210,210 },
			{	255,215,215 },
			{	255,220,220 },
			{	255,225,225 },
			{	255,230,230 },
			{	255,235,235 },
			{	255,240,240 },
			{	255,245,245 },
			{	255,250,250 },
			{	255,255,255,},
		    };

*/
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



public ColorFilter(Object source){
int[] lookupInt=new int[256];

ColorLookupDialog.initialize(null, lookupInt, getHistogram(source), "Filter", "Lookup Filters");
 lt= ColorLookupDialog.showDialog(null);
 //lookupInt = lci.looku

 for (int i=0;i<10;i++){
	  System.out.println(lt[0][i]+" "+lt[1][i]+" "+lt[2][i]);
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