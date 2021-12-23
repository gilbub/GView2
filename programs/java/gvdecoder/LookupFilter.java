package gvdecoder;
//package filters;

import java.awt.image.*;
import java.awt.image.renderable.*;
import javax.media.jai.*;
import javax.media.jai.operator.*;

public class LookupFilter implements GeneralFilter{

RenderedOp op;
ParameterBlock pb;
String description;
int[] lookupInt;
byte[][] lookupByte;
LookupTableJAI table;


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

public LookupFilter(Object source, int[] lt){

 lookupInt=new int[256];
 lookupByte=new byte[3][256];

 for (int i=0;i<256;i++) lookupInt[i]=i;

 int[] offsets=new int[3];
 offsets[0]=0;
 offsets[1]=0;
 offsets[2]=0;

 if (lt==null){

 LookupDialog.initialize(null, lookupInt, getHistogram(source), "Filter", "Lookup Filters");
 LookupColorInfo lci= LookupDialog.showDialog(null);
 lookupInt = lci.lookup;

 for (int i=0;i<256;i++) {
 for (int j=0;j<3;j++){
  lookupByte[j][i]=(byte)lookupInt[i];
  }
 }

 offsets[0]=lci.redOffset;
 offsets[1]=lci.greenOffset;
 offsets[2]=lci.blueOffset;
 }
 else{
 for (int i=0;i<256;i++) {
 for (int j=0;j<3;j++){
  lookupByte[j][i]=(byte)lt[i];
  }
 }
 }
 table = new LookupTableJAI(lookupByte,offsets);
 //LookupColorInfo lci=new LookupTableJAI(lookupByte,offsets);
  //table=lookupInt;
  if (source instanceof BufferedImage) op=JAI.create("lookup",(BufferedImage)source,table);
   else
  op=JAI.create("lookup",(RenderedOp)source,table);
}

public LookupFilter(Object source, String shape, int size){

}

public String toString(){
 return ("Lookup ");
}

public RenderedOp process(Object source){
   //pb.setSource(source,0);
   return op;
}

public RenderedOp reCreate(Object source){
 //pb.setSource(source,0);
 if (source instanceof BufferedImage) op=JAI.create("lookup",(BufferedImage)source,table);
 else
  op=JAI.create("lookup",(RenderedOp)source,table);
 return op;
 }

}