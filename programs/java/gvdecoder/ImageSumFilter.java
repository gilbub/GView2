package gvdecoder;
//package filters;

import java.awt.image.*;
import java.awt.image.renderable.*;
import javax.media.jai.*;
import javax.media.jai.operator.*;

public class ImageSumFilter implements GeneralFilter{

RenderedOp op;
ParameterBlock pb, pb2;
String description;



public RenderedOp getOp(){return op;}


public ImageSumFilter(Object source){
  RenderedImage img = (RenderedImage)JAI.create("fileload","test.jpg");





 pb=new ParameterBlock();
 pb.addSource(source);
 pb.addSource(img);


  op=JAI.create("add",pb);

}

public String toString(){
 return ("Image sum "+description);
}

public RenderedOp process(Object source){
   pb.setSource(source,0);
   return op;
}

public RenderedOp reCreate(Object source){
 pb.setSource(source,0);

 op=JAI.create("add",pb);
 return op;
 }

}