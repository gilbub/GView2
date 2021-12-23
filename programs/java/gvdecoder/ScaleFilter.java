//package filters;
package gvdecoder;
import java.awt.image.*;
import java.awt.image.renderable.*;
import javax.media.jai.*;
import javax.media.jai.operator.*;

public class ScaleFilter implements GeneralFilter{

RenderedOp op;
ParameterBlock pb2;
String description;



public RenderedOp getOp(){return op;}


public ScaleFilter(Object source){


     pb2 = new ParameterBlock();
           pb2.addSource(source);                   // The source image
           pb2.add(3.0F);                        // The xScale
           pb2.add(3.0F);                        // The yScale
           pb2.add(0.0F);                       // The x translation
           pb2.add(0.0F);                       // The y translation
           pb2.add(new InterpolationNearest()); // The interpolation

      // Create the scale operation
    op = JAI.create("scale", pb2, null);




}

public String toString(){
 return ("scale "+description);
}

public RenderedOp process(Object source){
   pb2.setSource(source,0);
   return op;
}

public RenderedOp reCreate(Object source){
 pb2.setSource(source,0);

 op=JAI.create("scale",pb2);
 return op;
 }

}