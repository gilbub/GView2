//package filters;

import java.awt.image.*;
import java.awt.image.renderable.*;
import javax.media.jai.*;
import javax.media.jai.operator.*;
import java.awt.RenderingHints;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;

public class Filter extends AbstractListModel{

java.util.ArrayList filters;
ParameterBlock pb1,pb2,pb3,pb4;
PlanarImage pl;
float scale=3.0f;
int ConvolveNum=0;
RenderedOp im1,im1c;
RenderedOp im2,im3,im4;
GeneralFilter mf,cf,tf;
BufferedImage bi_ref;
int original_width=1;
RenderedImage src;//for rescaling the background.
Object Interpolation=new InterpolationNearest();
BorderExtender bc=null;
RenderingHints rh=null;

float bg_scale=3.0f;
boolean SUPERIMPOSE=false;
String bg_img_name="backgrounddefault.jpg";
int bg_width=1;
int bg_height=1;

static final String[] filterNames={"median","convolve","lookup","subtract","edge","colour","sum","scale"};


public Object getElementAt(int index){return filters.get(index);}
public int getSize(){return filters.size();}
public void remove(int index){

 filters.remove(index);
 fireContentsChanged(this,0,filters.size());
 reProcess(bi_ref);
 System.out.println("removed.."+index);
 }


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
 }else
  if (filtername.equals("colour")){
    if (filters.size()==0) gf=new ColorFilter(bi_ref);
    else gf=new ColorFilter(((GeneralFilter)filters.get(filters.size()-1)).getOp());
 }else
  if (filtername.equals("sum")){
    if (filters.size()==0) gf=new ImageSumFilter(bi_ref);
    else gf=new ImageSumFilter(((GeneralFilter)filters.get(filters.size()-1)).getOp());
 }else
  if (filtername.equals("scale")){
    if (filters.size()==0) gf=new ScaleFilter(bi_ref);
    else gf=new ScaleFilter(((GeneralFilter)filters.get(filters.size()-1)).getOp());
 }
 if (gf==null) System.out.println("null filter, syntax err");
  else
 filters.add(gf);
 fireContentsChanged(this,0,filters.size());

}



public void setInterpolation(int val){

 if (val==0) Interpolation=new InterpolationNearest();
 else if (val==1) Interpolation=new InterpolationBilinear();
 else if (val==2) Interpolation=new InterpolationBicubic(8);
 else if (val==3) Interpolation=new InterpolationBicubic2(8);
 else Interpolation=new InterpolationNearest();

 // The interpolation

}
public void removeAllFilters(){

 filters=new ArrayList();
 fireContentsChanged(this,0,filters.size());
}

public Filter(){
	filters=new ArrayList();
	bc=BorderExtender.createInstance(BorderExtender.BORDER_REFLECT);
	rh=new RenderingHints(JAI.KEY_BORDER_EXTENDER,bc);
	System.out.println("setup rendering hints");
	}

public void Initialize(BufferedImage bi){

  bi_ref=bi;


//scale the image
   pb2 = new ParameterBlock();

   if (filters.size()==0) pb2.addSource(bi);
    else pb2.addSource(((GeneralFilter)filters.get(filters.size()-1)).getOp());
   	pb2.add(scale);                        // The xScale
   	pb2.add(scale);                        // The yScale
   	pb2.add(0.0F);                       // The x translation
   	pb2.add(0.0F);                       // The y translation
  	pb2.add(Interpolation); // The interpolation






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
  pb2.set(Interpolation,4);

//if (rh==null) System.out.println("rendering hints are NULL");
//else System.out.println("rendering hints set");
im2 = JAI.create("scale", pb2, rh);

if (SUPERIMPOSE){
//check if pb3 is null
 if (pb3==null){
	src = (RenderedImage)JAI.create("fileload",bg_img_name);
	//bg_scale=(float)src.getWidth()/original_width;
	//bg_scale=5;
	System.out.println("scaling operation bg_scale debug:"+bg_scale);
	pb3 = new ParameterBlock();
	        pb3.addSource(src);
	    	pb3.add(bg_scale);                        // The xScale
	    	pb3.add(bg_scale);                        // The yScale
	    	pb3.add(0.0F);                       // The x translation
	    	pb3.add(0.0F);                       // The y translation
	   	pb3.add(Interpolation); // The interpolation
	}

  //scale the background image
bg_scale=(float)src.getWidth()/original_width;
//System.out.println("bg_scale="+bg_scale+"srcwidth="+src.getWidth()+"org width="+original_width+" scale "+scale);
pb3.set(scale/bg_scale,0);
pb3.set(scale/bg_scale,1);
im3= JAI.create("scale", pb3, null);

  if (pb4==null){
	 pb4=new ParameterBlock();
	       pb4.addSource(im2);
	       pb4.addSource(im3);
	}
	else
	pb4.setSource(im2,0);
	pb4.setSource(im3,1);

  im4=JAI.create("add",pb4);
 }
else im4=im2;

return im4.createInstance().getAsBufferedImage();
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


