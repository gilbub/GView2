//package filters;

import java.awt.image.*;
import java.awt.image.renderable.*;
import javax.media.jai.*;
import javax.media.jai.operator.*;

public class MedianFilter implements GeneralFilter{

RenderedOp op;
ParameterBlock pb;
String description;

private static MedianFilterShape[] medianShapes = {
        MedianFilterDescriptor.MEDIAN_MASK_SQUARE,
        MedianFilterDescriptor.MEDIAN_MASK_SQUARE_SEPARABLE,
        MedianFilterDescriptor.MEDIAN_MASK_PLUS,
        MedianFilterDescriptor.MEDIAN_MASK_X
    };

public RenderedOp getOp(){return op;}


public MedianFilter(Object source){
 String[] names = {"3x3 mask", "3x3 mask seperable", "3x3 plus", "3x3 X",
                   "5x5 mask", "5x5 mask seperable", "5x5 plus", "5x5 X",
                   "7x7 mask", "7x7 mask seperable", "7x7 plus", "7x7 X"};
 ListDialog.initialize(null, names, "Filter",
                              "Median Filters");
 description = ListDialog.showDialog(null,null);
 pb=new ParameterBlock();
 pb.addSource(source);
 int i=0;
 for (i=0;i<names.length;i++){
  if (description.equals(names[i])) break;
  }
  pb.add(medianShapes[i%4]);
  if (i<4) pb.add(3);
  if ((i>=4)&&(i<8)) pb.add(5);
  if (i>=8) pb.add(7);
  op=JAI.create("medianfilter",pb);

}

public MedianFilter(Object source, String shape, int size){
  
 pb=new ParameterBlock();
 pb.addSource(source);
 String tmp="";
 if (shape.equals("square")) {tmp="mask"; pb.add(medianShapes[0]);} else
 if (shape.equals("squareseperable")) {tmp="mask seperable"; pb.add(medianShapes[1]);} else
 if (shape.equals("maskplus")) {tmp="plus";pb.add(medianShapes[2]);} else
 if (shape.equals("maskx")) {tmp="X";pb.add(medianShapes[3]);} else
   pb.add(medianShapes[0]);
 pb.add(size);
 description=size+"x"+size+" "+tmp;
 op=JAI.create("medianfilter",pb);
}

public String toString(){
 return ("Median "+description);
} 
   
public RenderedOp process(Object source){
   pb.setSource(source,0);
   return op;
}

public RenderedOp reCreate(Object source){
 pb.setSource(source,0);
 op=JAI.create("medianfilter",pb);
 return op;
 }

}