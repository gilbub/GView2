//package filters;
package gvdecoder;
import java.awt.image.*;
import java.awt.image.renderable.*;
import javax.media.jai.*;
import javax.media.jai.operator.*;



public class SubtractFilter implements GeneralFilter{

RenderedOp op;
ParameterBlock pb;
PlanarImage back;
PlanarImage tmp;




public RenderedOp getOp(){return op;}


public SubtractFilter(Object source){
  System.out.println("creating subtract filter 1");
  if (source instanceof BufferedImage)
    tmp =   (PlanarImage.wrapRenderedImage((BufferedImage)source)).createSnapshot() ;
 else
   tmp = ((RenderedOp) source).createInstance();





        ParameterBlock pbtemp = new ParameterBlock();
        pbtemp.addSource(tmp);
        double[] constants = new double[1]; // or however many bands
        constants[0] = 50;
		pbtemp.add(constants);
        PlanarImage back = JAI.create("SubtractConst", pbtemp, null);


   pb = new ParameterBlock();
   pb.addSource(source);
   pb.addSource(back);
   op = JAI.create("Subtract", pb);
   System.out.println("created subtract filter");
}

public SubtractFilter(Object source, String shape, int size){

 //op=JAI.create("Subtractfilter",pb);
}

public String toString(){
 return ("Subtract");
}

public RenderedOp process(Object source){
   pb.setSource(source,0);
   return op;
}

public RenderedOp reCreate(Object source){

pb.setSource(source,0);
op = JAI.create("Subtract", pb);
return op;
}

}