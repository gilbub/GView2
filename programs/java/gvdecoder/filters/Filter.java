//package filters;

import java.awt.image.*;
import java.awt.image.renderable.*;
import javax.media.jai.*;
import javax.media.jai.operator.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;

public class Filter extends AbstractListModel{

java.util.ArrayList filters;
ParameterBlock pb1,pb2,pb3;
PlanarImage pl;
float scale=3.0f;
int ConvolveNum=0;
RenderedOp im1,im1c;
RenderedOp im2;
GeneralFilter mf,cf,tf;
BufferedImage bi_ref;


//public void addListDataListener(ListDataListener l) {}
public Object getElementAt(int index){return filters.get(index);}
public int getSize(){return filters.size();}
public void remove(int index){

 filters.remove(index);
 fireContentsChanged(this,0,filters.size());
 reProcess(bi_ref);
 System.out.println("removed.."+index);
 }
//public void removeListDataListener(ListDataListener l){}

public void addFilter(String filtername){
 GeneralFilter gf=null;
 if (filtername.equals("median")){
  if (filters.size()==0) gf=new MedianFilter(bi_ref);
  else gf=new MedianFilter(((GeneralFilter)filters.get(filters.size()-1)).getOp());
 }else
 if (filtername.equals("convolve")){
   if (filters.size()==0) gf=new ConvolveFilter(bi_ref);
   else gf=new ConvolveFilter(((GeneralFilter)filters.get(filters.size()-1)).getOp());
 }else
 if (filtername.equals("lookup")){
   if (filters.size()==0) gf=new LookupFilter(bi_ref);
   else gf=new LookupFilter(((GeneralFilter)filters.get(filters.size()-1)).getOp());
 }else
  if (filtername.equals("subtract")){
    if (filters.size()==0) gf=new SubtractFilter(bi_ref);
    else gf=new SubtractFilter(((GeneralFilter)filters.get(filters.size()-1)).getOp());
 }else
  if (filtername.equals("edge")){
    if (filters.size()==0) gf=new EdgeFilter(bi_ref);
    else gf=new EdgeFilter(((GeneralFilter)filters.get(filters.size()-1)).getOp());
 }
 filters.add(gf);
 fireContentsChanged(this,0,filters.size());

}

public void removeAllFilters(){

 filters=new ArrayList();
 fireContentsChanged(this,0,filters.size());
}

public Filter(){filters=new ArrayList();}

public void Initialize(BufferedImage bi){

  bi_ref=bi;
  //filters=new ArrayList();


   pb2 = new ParameterBlock();

   if (filters.size()==0) pb2.addSource(bi);
    else pb2.addSource(((GeneralFilter)filters.get(filters.size()-1)).getOp());
   	pb2.add(scale);                        // The xScale
   	pb2.add(scale);                        // The yScale
   	pb2.add(0.0F);                       // The x translation
   	pb2.add(0.0F);                       // The y translation
  	pb2.add(new InterpolationNearest()); // The interpolation



}

public void draw(BufferedImage bi, Graphics2D g){
	bi_ref=bi;
	RenderedOp ro=null;
	GeneralFilter gf;
	for (int i=0;i<filters.size();i++){
	  gf=(GeneralFilter)filters.get(i);
	  if (i==0) gf.process(bi);
	   else gf.process(ro);
	 ro=gf.getOp();
	}
	if (filters.size()==0) pb2.setSource(bi,0);
		else pb2.setSource(ro,0);
	  pb2.set(scale,0);
	  pb2.set(scale,1);

      im2 = JAI.create("scale", pb2, null);
      g.drawImage(im2,null,0,0);
}

public BufferedImage go(BufferedImage bi){
bi_ref=bi;
RenderedOp ro=null;
GeneralFilter gf;
for (int i=0;i<filters.size();i++){
  gf=(GeneralFilter)filters.get(i);
  if (i==0) gf.process(bi);
   else gf.process(ro);
 ro=gf.getOp();
}
if (filters.size()==0) pb2.setSource(bi,0);
	else pb2.setSource(ro,0);
  pb2.set(scale,0);
  pb2.set(scale,1);

im2 = JAI.create("scale", pb2, null);

return im2.createInstance().getAsBufferedImage();
}

public void reProcess(BufferedImage bi){

RenderedOp ro=null;
RenderedOp newro=null;
GeneralFilter gf;
for (int i=0;i<filters.size();i++){
  gf=(GeneralFilter)filters.get(i);
  if (i==0) newro= gf.reCreate(bi);
   else newro=gf.reCreate(ro);
 System.out.println("reprocessing "+gf);
 ro=gf.getOp();
}
if (filters.size()==0) pb2.setSource(bi,0);
	else pb2.setSource(ro,0);
  pb2.set(scale,0);
  pb2.set(scale,1);
 JAI.create("scale", pb2, null);

}




}


