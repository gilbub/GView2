//package filters;

import java.awt.image.*;
import java.awt.image.renderable.*;
import javax.media.jai.*;
import javax.media.jai.operator.*;

public class ThresholdFilter implements GeneralFilter{

RenderedOp op;
ParameterBlock pb;
double[] min = new double[1];
double[] max = new double[1];
double[] to = new double[1];

public RenderedOp getOp(){return op;}

public ThresholdFilter(Object source, double min, double max, double to){
pb = new ParameterBlock();
     pb.addSource(source);
     this.min[0]=min;
	 this.max[0]=max;
	 this.to[0]=to;
	 pb.add(this.min);
     pb.add(this.max);
     pb.add(this.to);  
 op = JAI.create("threshold", pb);

}


public String toString(){

 return ("threshold ["+min[0]+","+max[0]+"]->"+to[0]);

}
   
public RenderedOp process(Object source){
   pb.setSource(source,0);
   return op;
}
 

}