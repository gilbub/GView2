package gvdecoder;
import java.awt.geom.*;
import java.awt.*;
import java.util.*;
import java.io.*;


//should contain editable list of points. Don't add to shape
public class ROI implements Serializable{

 Polygon poly;
 int[] arrs;
 Color color;
 double scale;
 int offset;
 transient Viewer2 vi;
 int numPixels=-1;
 int sumPixels=-1;
 double averageValue=-1.0;
 int firstX;
 int firstY;

 public ROI(Viewer2 vi,Color col){
   poly=new Polygon();
   this.color=col;
   this.vi=vi;
   scale=1.0;
   offset=0;
  }

 public Shape returnShape(){
  GeneralPath gp=new GeneralPath(poly);
  AffineTransform at=new AffineTransform();
  at.setToScale((double)vi.viewScale,(double)vi.viewScale);
  Shape newpg=gp.createTransformedShape(at);
  return newpg;
 }

 public int findAllPixels(int xdim, int ydim){
  int count=0;
  if (numPixels>=0) count=numPixels;
  else{
  ArrayList al=new ArrayList();
  for (int i=0;i<xdim;i++){
   for (int j=0;j<ydim;j++){
     if (poly.contains(i,j)) al.add(new Integer(j*xdim+i));
     }
  }
  arrs=new int[al.size()];
  for (int k=0;k<arrs.length;k++){
   arrs[k]=((Integer)al.get(k)).intValue();
  }
  numPixels=arrs.length;
  count=arrs.length;
  }
  return count;
  }

 public int sumAllPixels(int framewidth, int frameheight){
  int sum=0;
  if (sumPixels>=0) sum=sumPixels;
  else{
  //note that this utility is not used when processing more than one roi.
  int[] tmparray=new int[framewidth*frameheight];
  vi.im.UpdateImageArray(tmparray,framewidth,frameheight,vi.instance); //load image
  findAllPixels(framewidth,frameheight);  // find the pixels that are in the roi

         for (int j=0;j<arrs.length;j++){ //go to each element in the roi, and sum the value of the frame.
           sum+=tmparray[arrs[j]];

       }//j
   sumPixels=sum;
   averageValue=(double)sumPixels/(double)numPixels;
   }
  return sum;
 }

 public double getAverageVal(int xdim, int ydim){
  if (averageValue<0){
	  //findAllPixels(xdim,ydim);
	  sumAllPixels(xdim,ydim);
  }
  return averageValue;
  }

 }



