package gvdecoder;
import java.awt.geom.*;
import java.awt.*;
import JSci.maths.Complex;

public class Ruler{

public int firstX;
public int firstY;
public int lastX=-1;
public int lastY=-1;
public double distance=0.0;
public double scaleddistance=0.0;
public double angle=0.0;
public int degrees=0;
public double mass;
public String dataString;
public boolean GETINTENSITYPROFILE=false;

Color color;
Viewer2 vi;


public Ruler(){
/*  constructor to be used with Decoration class and some scripts  -note updatesums etc wont work without vi defined*/
 // notifyRulerCreated();
}

public Ruler(Viewer2 vi, int x1, int y1, int x2, int y2){
	/*  constructor to be used with Decoration class and some scripts  -note updatesums etc wont work without vi defined*/
    this.vi=vi;
	firstX=x1; firstY=y1; lastX=x2; lastY=y2;
	this.color=Color.BLUE;
    //notifyRulerCreated();
}

public Ruler(Viewer2 vi,Color col){
   firstX=-1;
   firstY=-1;
   lastX=-1;
   lastY=-1;

   this.color=col;
   this.vi=vi;
  // notifyRulerCreated();
  }

public boolean isDrawable(){
return ((firstX>0)&&(firstY>0)&&(lastX>0)&&(lastY>0));
}

 public Shape returnShape(){
   Polygon pg=new Polygon();
   pg.addPoint(firstX,firstY);
   pg.addPoint(lastX,lastY);
   GeneralPath gp=new GeneralPath(pg);
   AffineTransform at=new AffineTransform();
   at.setToScale((double)vi.viewScale,(double)vi.viewScale);
   at.translate((double)vi.jp.offsetx,(double)vi.jp.offsety);
   Shape newpg=gp.createTransformedShape(at);
   return newpg;
 }


public Shape returnTics(){
   AffineTransform at=new AffineTransform();
   at.setToScale((double)vi.viewScale,(double)vi.viewScale);
   Shape newpg=gp.createTransformedShape(at);
   return newpg;
}

public double findDistance(){
	distance=Math.sqrt(Math.pow(lastX-firstX,2)+Math.pow(lastY-firstY,2));
	if (vi!=null) scaleddistance=distance*vi.realpixelscale;
	return distance;
}
public double findAngle(){
    angle=Math.atan( ((double)(firstX-lastX))/((double)(firstY-lastY)) );
    degrees=(int)(angle*360.0/(Math.PI*2));
    return angle;
}

public Point pointOnRuler(double fraction){
	double x1=firstX;
	double y1=firstY;
	double x2=lastX;
	double y2=lastY;
	double d=distance;
	double m=(1.0*(y2-y1))/(x2-x1);
	double xi=x1+fraction*(x2-x1);
	double yi=m*(xi-x1)+y1;
    return new Point((int)(xi+0.5),(int)(yi+0.5));
}

public void findMass(){
  if (sumarray==null) return;
  double sum=0;
  for (int i=0;i<sumarray.length;i++){
	sum+=sumarray[i];
  }
  mass=sum;
}



public double[] sumarray;
public double[] wsumarray; //weighted sums
public GeneralPath gp;
public int width=3;
public void updateSums(){
	if (vi==null) return;
	//create an array of doubles the length of distance
	sumarray=new double[(int)distance];
	wsumarray=new double[(int)distance];

	gp=new GeneralPath();

	boolean firstmoveto=false;

	if (firstX==lastX){
	  //straight vertical line
      int multiplier=1;
	  if (firstY>lastY) multiplier=-1;
	  for (int i=0;i<sumarray.length;i++){
		  for (int j=-width;j<=width;j++){
		   sumarray[i]+=vi.getvalue(firstX+j,firstY+i*multiplier);
          wsumarray[i]+=vi.getvalue(firstX+j,firstY+i*multiplier);

	     }
	  }
	 return;
	}

	if (firstY==lastY){
		  //straight horizontal line
	      int multiplier=1;
		  if (firstX>lastX) multiplier=-1;
		  for (int i=0;i<sumarray.length;i++){
			  for (int j=-width;j<=width;j++){
			    sumarray[i]+=vi.getvalue(firstX+i*multiplier,firstY+j);
			   wsumarray[i]+=vi.getvalue(firstX+i*multiplier,firstY+j);

	         }
		  }
		 return;
	}

	double M=((double)(firstY-lastY))/((double)(firstX-lastX));
	double B=firstY-M*firstX;
	double x;
	double y;
	double xinv;
	double yinv;
	double Minv=-1/M;
	double invangle=Math.atan(Minv);
	//System.out.println("invangle="+invangle);
	for (int i=0;i<sumarray.length;i++){
		x=firstX+i* ((double)(lastX-firstX))/sumarray.length;
		y=M*x+B;
		//System.out.println("x="+x+" y ="+y);
	    //	-1/m*x+y(6)+6/m
	    //at each point , march on the inverse at that point up and down a certain distance (10 pix)
	    double y2inv=Math.sin(invangle)*5.0+y;
	    double x2inv=Math.cos(invangle)*5.0+x;
	    //System.out.println("x2inv="+x2inv+" y2inv="+y2inv);
	    //sumarray[i]=vi.getvalue((int)x,(int)y);
	    //the equation of the perpendicular line is:
	        //yn= -1/m*xn+(m*x+b+1/m*x)
	    double Bi=y2inv-Minv*x2inv;
	    for (int j=-width;j<=width;j++){
		  xinv=j*(x2inv-x)/((double)width)+x;
		  yinv=Minv*xinv+Bi;
		  if (!firstmoveto){
			  gp.moveTo((float)xinv,(float)yinv);
			  firstmoveto=true;
		     }
		    else gp.lineTo((float)xinv,(float)yinv);

		    sumarray[i]+=vi.getvalue((int)xinv,(int)yinv);
		    double summed=0;
		     for (int ii=-1;ii<=1;ii++){
				 for (int jj=-1;jj<=1;jj++){
			       int ti=(int)(xinv+ii);
			       int tj=(int)(yinv+jj);
			       double d=Math.sqrt( (xinv-ti)*(xinv-ti)+(yinv-tj)*(yinv-tj) );
				   if (d<1.414) summed+=(1.414-d)*vi.getvalue(ti,tj);
			   }
		     }
		    wsumarray[i]+=summed;
	  }
   }

}




public void drawRulerTics(float offset,float period){
	//create an array of doubles the length of distance
	double[] rulerarray=new double[(int)distance];
	gp=new GeneralPath();

	boolean firstmoveto=false;

	if (firstX==lastX){
	 return;
	}
	double M=((double)(firstY-lastY))/((double)(firstX-lastX));
	double B=firstY-M*firstX;
	double x;
	double y;
	double xinv;
	double yinv;
	double Minv=-1/M;
	double invangle=Math.atan(Minv);
	//System.out.println("invangle="+invangle);
	for (int i=0;i<rulerarray.length;i++){
		x=firstX+i* ((double)(lastX-firstX))/rulerarray.length;
		y=M*x+B;
		//System.out.println("x="+x+" y ="+y);
	    //	-1/m*x+y(6)+6/m
	    //at each point , march on the inverse at that point up and down a certain distance (10 pix)

	    if (Math.abs( ( (float)i%period)-offset)<1.0){
	    double y2inv=Math.sin(invangle)*5.0+y;
	    double x2inv=Math.cos(invangle)*5.0+x;
	    //System.out.println("x2inv="+x2inv+" y2inv="+y2inv);
	    //sumarray[i]=vi.getvalue((int)x,(int)y);
	    //the equation of the perpendicular line is:
	        //yn= -1/m*xn+(m*x+b+1/m*x)
	    for (int j=-width;j<=width;j++){
		  xinv=-width*(x2inv-x)/((double)width)+x;
		  yinv=-1/M*xinv+(M*x+B+1/M*x);
		  gp.moveTo((float)xinv,(float)yinv);
		  xinv=width*(x2inv-x)/((double)width)+x;
		  yinv=-1/M*xinv+(M*x+B+1/M*x);
		  gp.lineTo((float)xinv,(float)yinv);

		}

		}
	}
}




public void resizeRulerEnd(double newdistance){
 double sinval=	(lastY-firstY)/distance;
 double cosval= (lastX-firstX)/distance;
 double newy=newdistance*sinval+firstY;
 double newx=newdistance*cosval+firstX;
 lastX=(int)newx;
 lastY=(int)newy;
}


public void resizeRulerFront(double newdistance){
 double sinval=	(firstY-lastY)/distance;
 double cosval= (firstX-lastX)/distance;
 double newy=newdistance*sinval+lastY;
 double newx=newdistance*cosval+lastX;
 firstX=(int)newx;
 firstY=(int)newy;
}



public int dominantFrequency(double minfreq, double maxfreq){
	 if (sumarray==null) updateResults();
	 double[] fft=new double[sumarray.length];
	 Complex[] cmp=dft1d.go(sumarray);
	 for (int i =0;i<cmp.length;i++){
	      Complex cnj=cmp[i].conjugate();
	      Complex cmpi=cmp[i].multiply(cnj);
          fft[i]=cmpi.real();
	  }
	 double max_val=0;
	 int frequency=0;
	 for (int x =(int)minfreq; x<(int)Math.round(maxfreq);x++){
	      if ((fft[x-1]<fft[x]) && (fft[x+1]<fft[x]) && (fft[x]>max_val)){
	           max_val=fft[x];
	           frequency=x;
		   }
  }
  return frequency;
}




public void rotateruler(double delta_degrees){
	double centery,centerx;
	centerx=((double)(firstX+lastX))/2.0;
    centery=((double)(firstY+lastY))/2.0;
    distance=Math.sqrt(Math.pow(lastX-firstX,2)+Math.pow(lastY-firstY,2));
	rotateruler(delta_degrees,distance,centerx,centery);

}


/**only works from center. Call rotateruler(degrees)!! **/
public void rotateruler(double delta_degrees, double dist, double centerx, double centery){

	//System.out.println("before "+ firstX+" "+firstY+" , "+lastX+" "+lastY+" dist="+distance);
	boolean inverse=false;
	if (firstX>lastX){
		 inverse=true;
	     int tmp=lastX;
	     lastX=firstX;
	     firstX=tmp;
	     tmp=lastY;
	     lastY=firstY;
	     firstY=tmp;
    }

    double newdistance=dist/2.0;
	double delta_rads=(Math.PI/180.0)*delta_degrees;
	double sinangle;
	if (dist<(lastY-firstY)) sinangle=Math.asin(0.999999);
	 else sinangle=Math.asin((lastY-firstY)/dist);
    //double cosangle=Math.acos((lastX-centerx)/newdistance);
    System.out.println("angles="+sinangle+" delta="+delta_rads+" newdist="+newdistance+" lastX="+lastX+ " centerx="+centerx);

    lastY=(int)Math.round(newdistance*Math.sin(sinangle+delta_rads)+centery);
    lastX=(int)Math.round(newdistance*Math.cos(sinangle+delta_rads)+centerx);
    firstY=(int)Math.round(newdistance*Math.sin(sinangle+delta_rads+Math.PI)+centery);
    firstX=(int)Math.round(newdistance*Math.cos(sinangle+delta_rads+Math.PI)+centerx);
    System.out.println("after "+firstX+" "+firstY+" , "+lastX+" "+lastY+ " center="+centerx+","+centery);
    if (inverse){
		int tmp=lastX;
	    lastX=firstX;
		firstX=tmp;
	    tmp=lastY;
	    lastY=firstY;
	    firstY=tmp;
	}
	System.out.println("after inverse "+firstX+" "+firstY+" , "+lastX+" "+lastY+ " center="+centerx+","+centery);

}



public boolean USE_SHORT_WIDTH=true;
public void updateResults(){

	distance=Math.sqrt(Math.pow(lastX-firstX,2)+Math.pow(lastY-firstY,2));
	if (vi!=null) scaleddistance=distance*vi.realpixelscale;
	angle=Math.atan( ((double)(firstX-lastX))/((double)(firstY-lastY)) );
    degrees=(int)(angle*360.0/(Math.PI*2));
	//System.out.println("D="+distance+ " ("+firstX+","+firstY+")("+lastX+","+lastY+") scaled="+scaleddistance);
    if (USE_SHORT_WIDTH) width=1;
    updateSums();
    findMass();
    width=3;
}


public void notifyRulerCreated(){
	if (vi!=null){
		 if (vi.ifd.jv!=null) vi.ifd.jv.interp.exec("rulercreated()");
	}
}

public static void main(String[] args){
	Ruler r=new Ruler(null,Color.black);
	r.firstX=100;
	r.firstY=100;
	r.lastX=100;
	r.lastY=110;
	r.updateResults();
	r.updateSums();
	//if (r.gp!=null) System.out.println(r.gp.getBounds());

}

}