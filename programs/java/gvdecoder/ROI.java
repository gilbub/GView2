package gvdecoder;
import java.awt.geom.*;
import java.awt.*;
import java.util.*;
import java.io.*;


//should contain editable list of points. Don't add to shape
public class ROI implements Serializable{

 public Polygon poly;
 public int[] arrs;
 Color color;
 double scale;
 int offset;
 transient Viewer2 vi;
 int numPixels=-1;
 int sumPixels=-1;
 double averageValue=-1.0;
 int firstX;
 int firstY;

//this constructor is useful when driven from a script only
 public ROI(){
	 poly=new Polygon();
	 scale=1.0;
	 offset=0;
	 this.vi=null;
	 this.color=Color.blue;//default not used
 }


 public ROI(int xdim, int ydim, ROI roi){
    poly=new Polygon(roi.poly.xpoints, roi.poly.ypoints, roi.poly.npoints);
    findAllPixels(xdim,ydim);
    scale=1.0;
    offset=0;
    this.vi=null;
	this.color=Color.blue;//default, not used
 }

 public ROI(int xdim, int ydim, int xtop, int ytop, int xbot, int ybot){

	 poly=new Polygon();
	 poly.addPoint(xtop,ytop);
	 poly.addPoint(xbot,ytop);
	 poly.addPoint(xbot,ybot);
	 poly.addPoint(xtop,ybot);
	 findAllPixels(xdim,ydim);
	 scale=1.0;
	 offset=0;
	 this.vi=null;
	 this.color=Color.blue;//default, not used
 }

 public ROI(Ruler ru, double fraction, int width, int length){
   Point pt=ru.pointOnRuler(fraction);
   this.vi=ru.vi;
   this.color=Color.blue;
   scale=1.0;
   offset=0;
   int we=(int)(length/2.0);
   poly=new Polygon();
   poly.addPoint(-we,-width);
   poly.addPoint(-we,width);
   poly.addPoint(we,width);
   poly.addPoint(we,-width);
   poly.addPoint(-we,-width);
   rotatedegrees(-1*(ru.degrees-90));
   poly.translate((int)(pt.x),(int)(pt.y));
   findAllPixels(vi.X_dim,vi.Y_dim);
   vi.jp.rois.add(this);
   vi.repaint();
   vi.jp.repaint();
 }

 public ROI(Viewer2 vi,Color col){
   poly=new Polygon();
   this.color=col;
   this.vi=vi;
   scale=1.0;
   offset=0;
  }

  public String toString(){
	  if (arrs==null) return null;
	  else return "ROI length="+arrs.length+" from "+vi.filename;
  }

public void moveTo(Point px){
	moveTo(px.x,px.y);
}

public void moveTo(int cx, int cy){
	Point pc=getCenter();
	int dx=cx-pc.x;
	int dy=cy-pc.y;
	poly.translate(dx,dy);
	findAllPixels(vi.X_dim,vi.Y_dim);
	vi.repaint();
    vi.jp.repaint();
}

public double getDiameter(){
 /* very inefficient - should use convext hull */
  int x,y,x1,y1;
  double dist=0;
  double maxdist=0;
  for (int i=0;i<poly.npoints;i++){
	 x=poly.xpoints[i];
	 y=poly.ypoints[i];
	 for (int j=0;j<poly.npoints;j++){
		if (j!=i){
			x1=poly.xpoints[j];
			y1=poly.ypoints[j];
			dist=Math.sqrt( (x-x1)*(x-x1)+ (y-y1)*(y-y1) );
			if (dist>maxdist) maxdist=dist;

		  }// j!=i
	   }//j
   }//i

return maxdist;
}


// return the centroid of the polygon. Not accurate.
 public Point getCentroid() {
        double cx = 0.0, cy = 0.0, sum=0.0;
        double xi,xi1,yi,yi1;
        for (int i = 0;i < poly.npoints-1;i++) {
			xi=poly.xpoints[i];
			yi=poly.ypoints[i];
			xi1=poly.xpoints[i+1];
			yi1=poly.ypoints[i+1];
			sum = sum + (xi * yi1) - (yi* xi1);
            cx = cx + (xi + xi1) * (xi * yi1 - yi * xi1);
            cy = cy + (yi + yi1) * (xi * yi1 - yi * xi1);
        }
        double area=sum/2;
        cx /= (6 * area);
        cy /= (6 * area);
        return new Point((int)cx, (int)cy);
    }


//returns the center of the bounding rectangle.
public Point getCenter(){
	Rectangle r=poly.getBounds();
	return new Point ( (int)(r.x+r.width/2.0), (int)(r.y+r.height/2.0));

}

 public Shape returnShape(){
  GeneralPath gp=new GeneralPath(poly);
  AffineTransform at=new AffineTransform();
  at.setToScale((double)vi.viewScale,(double)vi.viewScale);
  at.translate((double)vi.jp.offsetx,(double)vi.jp.offsety);
  Shape newpg=gp.createTransformedShape(at);
  return newpg;
 }


 //this should only be used if vi !=null
 public int setPixels(){
	if (vi!=null)
	 return findAllPixels(vi.X_dim,vi.Y_dim);
    else return -1;
  }

 public int findAllPixels(int xdim, int ydim){
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
  return numPixels;
  }

 public int findAllOutsidePixels(int xdim, int ydim){

  ArrayList al=new ArrayList();
  for (int i=0;i<xdim;i++){
   for (int j=0;j<ydim;j++){
     if (!poly.contains(i,j)) al.add(new Integer(j*xdim+i));
     }
  }
  arrs=new int[al.size()];
  for (int k=0;k<arrs.length;k++){
   arrs[k]=((Integer)al.get(k)).intValue();
  }
  numPixels=arrs.length;
  return numPixels;
  }

 public void rotatedegrees(double degrees){
	 rotate(degrees*0.0174532925);
 }

 public void rotate(double radians){

	  GeneralPath gp=new GeneralPath(poly);
	  AffineTransform af=new AffineTransform();
	  Point p=getCenter();
	  af.rotate(radians,p.x,p.y);
	  GeneralPath newpg=new GeneralPath(gp.createTransformedShape(af));
	  poly.reset();
	  PathIterator pi=newpg.getPathIterator(null);
	  float[] farray= new float[6];
	  while(!pi.isDone()){
	   pi.currentSegment(farray);
	   poly.addPoint((int)(farray[0]+0.5),(int)(farray[1]+0.5));
       pi.next();
       }
   findAllPixels(vi.X_dim,vi.Y_dim);
   vi.repaint();
   vi.jp.repaint();
}

 public int sumAllPixels(int framenumber){
   int sum=0;
   vi.JumpToFrame(framenumber);
   if ((arrs==null) || (arrs.length==0)) findAllPixels(vi.X_dim,vi.Y_dim);
   if (arrs[0]==-1) findAllPixels(vi.X_dim,vi.Y_dim);
   for (int j=0;j<arrs.length;j++)sum+=vi.datArray[arrs[j]];
   sumPixels=sum;
   averageValue=(double)sumPixels/(double)numPixels;
   return sum;
}

 public int sumAllPixels(int framewidth, int frameheight){
  int sum=0;


  //note that this utility is not used when processing more than one roi.
  int[] tmparray=new int[framewidth*frameheight];
  vi.im.UpdateImageArray(tmparray,framewidth,frameheight,vi.instance); //load image
  findAllPixels(framewidth,frameheight);  // find the pixels that are in the roi

         for (int j=0;j<arrs.length;j++){ //go to each element in the roi, and sum the value of the frame.
           sum+=tmparray[arrs[j]];

       }//j
   sumPixels=sum;
   averageValue=(double)sumPixels/(double)numPixels;

  return sum;
 }

 public double[] getPixelValues(int framewidth, int frameheight){

   //note that this utility is not used when processing more than one roi.
   int[] tmparray=new int[framewidth*frameheight];
   double[] res=new double[arrs.length];
   vi.im.UpdateImageArray(tmparray,framewidth,frameheight,vi.instance); //load image
   findAllPixels(framewidth,frameheight);  // find the pixels that are in the roi

          for (int j=0;j<arrs.length;j++){
            res[j]=tmparray[arrs[j]];

        }//j

   return res;
 }

 public double getAverageVal(int xdim, int ydim){
  if (averageValue<0){
	  //findAllPixels(xdim,ydim);
	  sumAllPixels(xdim,ydim);
  }
  return averageValue;
  }

 }



