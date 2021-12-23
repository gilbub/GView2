//package filters;
package gvdecoder;
import java.awt.image.*;
import java.awt.image.renderable.*;
import javax.media.jai.*;
import javax.media.jai.operator.*;

public class EdgeFilter implements GeneralFilter{

RenderedOp op;
ParameterBlock pb;
String description;
KernelJAI kern_h=KernelJAI.GRADIENT_MASK_SOBEL_HORIZONTAL;
KernelJAI kern_v=KernelJAI.GRADIENT_MASK_SOBEL_VERTICAL;

float[] roberts_h_data        = { 0.0F,  0.0F, -1.0F,
                                       0.0F,  1.0F,  0.0F,
                                       0.0F,  0.0F,  0.0F
     };
     float[] roberts_v_data        = {-1.0F,  0.0F,  0.0F,
                                       0.0F,  1.0F,  0.0F,
                                       0.0F,  0.0F,  0.0F
     };


float[] prewitt_h_data        = { 1.0F,  0.0F, -1.0F,
                                       1.0F,  0.0F, -1.0F,
                                       1.0F,  0.0F, -1.0F
     };
     float[] prewitt_v_data        = {-1.0F, -1.0F, -1.0F,
                                       0.0F,  0.0F,  0.0F,
                                       1.0F,  1.0F,  1.0F
     };



float[] freichen_h_data        = { 1.0F,   0.0F,   -1.0F,
                                        1.414F, 0.0F,   -1.414F,
                                        1.0F,   0.0F,   -1.0F
     };
     float[] freichen_v_data        = {-1.0F,  -1.414F, -1.0F,
                                        0.0F,   0.0F,    0.0F,
                                        1.0F,   1.414F,  1.0F
     };





public RenderedOp getOp(){return op;}


public EdgeFilter(Object source){
 System.out.println("in edge");
 String[] names = {"Sobel", "Roberts", "Prewitt", "FreiChen"};
 ListDialog.initialize(null, names, "Filter",
                              "Edge Filters");
 description = ListDialog.showDialog(null,null);
 pb=new ParameterBlock();
 pb.addSource(source);
 int i=0;
 for (i=0;i<names.length;i++){
  if (description.equals(names[i])) break;
  }

 switch(i){
  case 0: break;
  case 1:
     kern_h = new KernelJAI(3,3,roberts_h_data);
     kern_v = new KernelJAI(3,3,roberts_v_data);
     break;
  case 2:
    kern_h = new KernelJAI(3,3,prewitt_h_data);
    kern_v = new KernelJAI(3,3,prewitt_v_data);
	break;
  case 3:
    kern_h = new KernelJAI(3,3,freichen_h_data);
    kern_v = new KernelJAI(3,3,freichen_v_data);
    break;
  }
  if (source instanceof BufferedImage) op=JAI.create("gradientmagnitude", (BufferedImage)source, kern_h, kern_v);
  else
   op=JAI.create("gradientmagnitude", (RenderedOp)source, kern_h, kern_v);



}

public EdgeFilter(Object source, String shape, int size){

}

public String toString(){
 return ("Edge "+description);
}

public RenderedOp process(Object source){

   return op;
}

public RenderedOp reCreate(Object source){
 if (source instanceof BufferedImage) op=JAI.create("gradientmagnitude", (BufferedImage)source, kern_h, kern_v);
   else
    op=JAI.create("gradientmagnitude", (RenderedOp)source, kern_h, kern_v);

 return op;
 }

}