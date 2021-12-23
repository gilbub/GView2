package gvdecoder;
//package filters;

import java.awt.image.*;
import java.awt.image.renderable.*;
import javax.media.jai.*;
import javax.media.jai.operator.*;

public class ConvolveFilter implements GeneralFilter{

RenderedOp op;
String description="";
int k=0; //kernel

float[]  edgeData = new float[]{  0.0F, -1.0F,  0.0F,
                 				-1.0F,  5.0F, -1.0F,
                 				 0.0F, -1.0F,  0.0F };

float[]  laplaceData = new float[]{  1.0F, -2.0F,  1.0F,
                 				    -2.0F,  5.0F, -2.0F,
                 				     1.0F, -2.0F,  1.0F };


float[]  smoothData = new float[]{  0.11F, 0.11F,  0.11F,
                 				    0.11F, 0.11F,  0.11F,
                 				    0.11F, 0.11F,  0.11F };

float[]  smoothData5 = new float[]{ 0.04F, 0.04F,  0.04F, 0.04F, 0.04F,
                 				    0.04F, 0.04F,  0.04F, 0.04F, 0.04F,
                 				    0.04F, 0.04F,  0.04F, 0.04F, 0.04F,
                 				    0.04F, 0.04F,  0.04F, 0.04F, 0.04F,
                 				    0.04F, 0.04F,  0.04F, 0.04F, 0.04F
                 				    };



KernelJAI[] kernel=new KernelJAI[]{new KernelJAI(3, 3, edgeData),
								   new KernelJAI(3, 3, smoothData),
								   new KernelJAI(3, 3, laplaceData),
								   new KernelJAI(5,5,smoothData5)};






public RenderedOp getOp(){return op;}

public String toString(){
 return ("Convolve "+description);
 }

public ConvolveFilter(Object source){
 String[] names = {"3x3 edge", "3x3 smooth", "3x3 laplace", "5x5 smooth"};
 ListDialog.initialize(null, names, "Filter","Convolution Filters");
 description = ListDialog.showDialog(null,null);

 if (description.equals("3x3 smooth")) k=1; else
 if (description.equals("3x3 edge")) k=0; else
 if (description.equals("5x5 smooth")) k=3; else
 if (description.equals("3x3 laplace")) k=2;

 if (source instanceof BufferedImage) op=JAI.create("convolve", (BufferedImage)source ,kernel[k]);
 else op=JAI.create("convolve", (RenderedOp)source ,kernel[k]);
}

public ConvolveFilter(Object source, String sort, int size){


 if (sort.equals("smooth")) k=1; else
 if (sort.equals("edge")) k=0; else
 if (sort.equals("laplace")) k=2;

 op=JAI.create("convolve", (RenderedOp)source ,kernel[k]);

}



public RenderedOp process(Object source){
   //pb.setSource(source,0);
   return op;
}

public RenderedOp reCreate(Object source){
  if (source instanceof BufferedImage) op=JAI.create("convolve", (BufferedImage)source ,kernel[k]);
  else op=JAI.create("convolve", (RenderedOp)source ,kernel[k]);
  return op;
}
}