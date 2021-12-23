package gvdecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.lang.Math;
import JSci.maths.ArrayMath;
import gvdecoder.fx.GeneralViewer;
import java.io.*;
import java.nio.channels.FileChannel;
import java.nio.MappedByteBuffer;
import java.nio.*;
import gvdecoder.PixelGetter;
import javafx.beans.property.*;

public class ConfocalDataSet implements PixelGetter{
 public double attenuationfactor=1.0/15.0; //this is to account for the voltage dividers in the control box.

  ArrayList<DataPoint> array;
  short sx,ex,sy,ey,sz,ez;
  int nx,ny,nz;
  boolean oversampled=false;
  int oversamplefactor=1;
  public boolean PHOTONCOUNTING=true;
  int writeposition=0;
  int readposition=0;
  Matrix sparse;
  Matrix mout;
  Matrix fakedata;
  short maxx,maxy,maxz,minx,miny,minz;
  double xspan,yspan,zspan,xcompress,ycompress,zcompress;
  GeneralViewer viewer;
  IMAGEMODE MODE=IMAGEMODE.Z_STACK;
  IMAGEMAGNIFICATION MAGNIFICATION=IMAGEMAGNIFICATION.TWENTYX;

  int undersamplefactor=4;
  int gutterpixels=20;

  int lastviewed_z=-1;
  public int averageWindow=10;
  public boolean averageSkip=false;
  public boolean averageError=false;
  public int averageCorrection=0;

  /** PixelGetter interface **/
  public IntegerProperty changed=new SimpleIntegerProperty();

 /* PixelGetter interface*/
 public double getPixelValue(int z, int y, int x){
  		if (lastviewed_z!=z) viewAverage((short)z,averageWindow,averageSkip,averageError,averageCorrection);
  		return mout.dat.get(0,y,x);
 }
 public int getZDim(){return nz;}
 public int getYDim(){return ny;}
 public int getXDim(){return nx;}
 public double getXMin(){return (double)minx;}
 public double getXMax(){return (double)maxx;}
 public double getYMin(){return (double)miny;}
 public double getYMax(){return (double)maxy;}
 public double getZMin(){return (double)minz;}
 public double getZMax(){return (double)maxz;}
 public void update(){if (mout!=null) getRange(mout);}
 public IntegerProperty getChangedProperty(){return changed;}
 /*     */




  java.awt.Point[] FourClosest=new java.awt.Point[4];

  public ConfocalDataSet(){
	   array=new ArrayList<DataPoint>();
  }

  public ConfocalDataSet( short[] pts){
     this((short)0,(short)0,(short)0,(short)0,(short)0,(short)0,(short)0,(short)0,(short)0,pts,1);
   }

  public ConfocalDataSet(short[] pts, int repeats){
	this((short)0,(short)0,(short)0,(short)0,(short)0,(short)0,(short)0,(short)0,(short)0,pts,repeats);
  }

  public ConfocalDataSet(short sx, short ex, int nx, short sy, short ey, int ny, short sz, short ez, int nz, short[] pts){
	   this(sx,ex,nx,sy,ey,ny,sz,ez,nz,pts,1);
  }
  public ConfocalDataSet(short sx, short ex, int nx, short sy, short ey, int ny, short sz, short ez, int nz, short[] pts, int repeats){
     this.sx=sx;
     this.ex=ex;
     this.sy=sy;
     this.ey=ey;
     this.sz=sz;
     this.ez=ez;
     this.nx=nx;
	 this.ny=ny;
     this.nz=nz;
     array=new ArrayList<DataPoint>(repeats*pts.length/3);
     for (int r=0;r<repeats;r++){
       for (int i=0;i<pts.length;i+=3){
		 	DataPoint pt=new DataPoint();
		 	pt.px=pts[i];
		 	pt.py=pts[i+1];
		 	pt.pz=pts[i+2];
            array.add(pt);
		}
	}
}


  public ConfocalDataSet(short sx, short ex, int nx, short sy, short ey, int ny, short sz, short ez, int nz){
    this.sx=sx;
    this.ex=ex;
    this.sy=sy;
    this.ey=ey;
    this.sz=sz;
    this.ez=ez;
    this.nx=nx;
    this.ny=ny;
    this.nz=nz;


    array= new ArrayList<DataPoint>(nx*ny*nz);
    double xstep=((double)(ex-sx)/nx);
    double ystep=((double)(ey-sy)/ny);
    double zstep=((double)(ez-sz)/nz);
    for (int z=0;z<nz;z++){
		for (int y=0;y<ny;y++){
			for (int x=0;x<nx;x++){
				DataPoint pt=new DataPoint();
				if (y%2==0) {pt.px=(short)(sx+x*xstep);  pt.R2L=false;}
				else {pt.px=(short)(sx+nx*xstep-(x*xstep)); pt.R2L=true;}
				if (z%2==0) pt.py=(short)(sy+y*ystep);
				else pt.py=(short)(sy+ny*ystep-(y*ystep));
                pt.pz=(short)(sz+z*zstep);
                array.add(pt);
			}
		  }
	   }
    }

public ConfocalDataSet(short sx, short ex, int nx, short sy, short ey, int ny, short sz, short ez, int nz, boolean oversample, int repeats){
    this.sx=sx;
    this.ex=ex;
    this.sy=sy;
    this.ey=ey;
    this.sz=sz;
    this.ez=ez;
    this.nx=nx;
    this.ny=ny;
    this.nz=nz;
    this.oversampled=true;
    this.oversamplefactor=repeats;

    array= new ArrayList<DataPoint>(nx*ny*nz);
    double xstep=((double)(ex-sx)/nx);
    double ystep=((double)(ey-sy)/ny);
    double zstep=((double)(ez-sz)/nz);
    for (int z=0;z<nz;z++){
		for (int y=0;y<ny;y++){
			for (int x=0;x<nx;x++){
			 if (oversample){
			  for (int k=0;k<repeats;k++){
				DataPoint pt=new DataPoint();
				if (y%2==0) {pt.px=(short)(sx+x*xstep); pt.R2L=false;}
				else {pt.px=(short)(sx+nx*xstep-(x*xstep)); pt.R2L=true;}
				if (z%2==0) pt.py=(short)(sy+y*ystep);
				else pt.py=(short)(sy+ny*ystep-(y*ystep));
                pt.pz=(short)(sz+z*zstep);
                array.add(pt);
		     }//repeats
			}//oversample
		  }
	   }
    }
}




 public ConfocalDataSet(ConfocalDataSet old){
	this.sx=old.sx;
    this.ex=old.ex;
    this.sy=old.sy;
    this.ey=old.ey;
    this.sz=old.sz;
    this.ez=old.ez;
    this.nx=old.nx;
    this.ny=old.ny;
    this.nz=old.nz;
    array=new ArrayList<DataPoint>(nx*ny*nz);
    for (DataPoint p:old.array){
      DataPoint pt=new DataPoint();
      pt.px=p.px;
      pt.py=p.py;
      pt.pz=p.pz;
      pt.fx=p.fx;
      pt.fy=p.fy;
      pt.fz=p.fz;
      pt.d1=p.d1;
      pt.d2=p.d2;
      array.add(pt);
   }
   mout=old.mout;
   sparse=old.sparse;

 }

 public void load(String filename){
	RandomAccessFile buffer=null;
    try{
		int headerelements=12;
		buffer=new RandomAccessFile(filename,"r");
		int version=buffer.readInt(); //1
		if (version>0){
			int mag=buffer.readInt();
			if (mag==IMAGEMAGNIFICATION.TWENTYX.getValue()) MAGNIFICATION=IMAGEMAGNIFICATION.TWENTYX;
			if (mag==IMAGEMAGNIFICATION.FORTYX.getValue())  MAGNIFICATION=IMAGEMAGNIFICATION.FORTYX;
			if (mag==IMAGEMAGNIFICATION.SIXTYX.getValue())   MAGNIFICATION=IMAGEMAGNIFICATION.SIXTYX;

			headerelements=13;
		}else
		MAGNIFICATION=IMAGEMAGNIFICATION.TWENTYX;

		int mode=buffer.readInt();//2
		if (mode==IMAGEMODE.Z_STACK.getValue()) MODE=IMAGEMODE.Z_STACK;
		if (mode==IMAGEMODE.PATH.getValue()) MODE=IMAGEMODE.PATH;
		if (mode==IMAGEMODE.UNSTRUCTURED.getValue()) MODE=IMAGEMODE.UNSTRUCTURED;
		int size=buffer.readInt();//3
	    sx=(short)buffer.readInt();//4
	    ex=(short)buffer.readInt();//5
	    sy=(short)buffer.readInt();//6
	    ey=(short)buffer.readInt();//7
	    sz=(short)buffer.readInt();//8
	    ez=(short)buffer.readInt();//9
	    nx=buffer.readInt();//10
	    ny=buffer.readInt();//11
	    nz=buffer.readInt();//12
	    System.out.println("size="+size+" (sx,ex,sy,ey,sz,ez,nx,ny,nz) "+sx+","+ex+","+sy+","+ey+","+sz+","+ez+","+nx+","+nx+","+ny+","+nz);
	    mout=new Matrix(1,ny,nx);
	    FileChannel fc = buffer.getChannel();
	    MappedByteBuffer mbuffer = fc.map (FileChannel.MapMode.READ_ONLY, headerelements*4, size*9*2);
	    ShortBuffer db=mbuffer.asShortBuffer();
	    short[] tmp=new short[9];
	    array=new ArrayList<DataPoint>(size);
	    for (int i=0;i<size;i++){
			db.get(tmp,0,9);
			DataPoint pt=new DataPoint();
            if (i%1000==0) System.out.println("i="+i);
			pt.px=tmp[0];//buffer.readShort(); 1
			pt.py=tmp[1];//buffer.readShort(); 2
			pt.pz=tmp[2];//buffer.readShort(); 3
			pt.fx=tmp[3];//buffer.readShort(); 4
			pt.fy=tmp[4];//buffer.readShort(); 5
			pt.fz=tmp[5];//buffer.readShort(); 6
			pt.d1=tmp[6];//buffer.readShort(); 7
		    pt.d2=tmp[7];//buffer.readShort(); 8
		    short v=tmp[8];//buffer.readShort(); 9
		    if (v==0) pt.R2L=false;
		    else pt.R2L=true;
            array.add(pt);
		 }
	 }catch(IOException e){e.printStackTrace();}
	  finally{close(buffer);}

 }

 public void save(String filename){
   RandomAccessFile outfile=null;
	  try{
	 	 outfile=new RandomAccessFile(filename,"rw");
	 	 //double[] tmp=new double[xdim*ydim];
	 	 //byte[] bytes=new byte[xdim*ydim*8];
		 FileChannel fc=outfile.getChannel();
	 	 MappedByteBuffer buffer = fc.map (FileChannel.MapMode.READ_WRITE, 0, (array.size()*9*2)+13*4);
	     buffer.putInt(1); //version 1
	     buffer.putInt(MAGNIFICATION.getValue());
	     buffer.putInt(MODE.getValue()); //unstructured,z-scan,path etc
	     buffer.putInt(array.size()); //3
	     buffer.putInt((int)sx); //4
	     buffer.putInt((int)ex); //5
	     buffer.putInt((int)sy); //6
	     buffer.putInt((int)ey); //7
	     buffer.putInt((int)sz); //8
	     buffer.putInt((int)ez); //9
	     buffer.putInt(nx); //10
	     buffer.putInt(ny); //11
	     buffer.putInt(nz); //12
	     for (DataPoint p:array){
			 buffer.putShort(p.px);//1
			 buffer.putShort(p.py);//2
			 buffer.putShort(p.pz);//3
			 buffer.putShort(p.fx);//4
			 buffer.putShort(p.fy);//5
			 buffer.putShort(p.fz);//6
			 buffer.putShort(p.d1);//7
			 buffer.putShort(p.d2);//8
			 if (p.R2L) buffer.putShort((short)0);//9
			 else buffer.putShort((short)1);
		 }
		 }catch(IOException e){e.printStackTrace();}
         finally{close(outfile);}
}

public static void close(Closeable c){
      if (c == null) return;
      try {
          c.close();
      } catch (IOException e) {
         e.printStackTrace();
         }
  }


 /*this is just for testing purposes).*/
 public int add(int z, int y, int x){
	 DataPoint pt=new DataPoint();
	 pt.px=pt.fx=(short)x;
	 pt.py=pt.fy=(short)y;
	 pt.pz=pt.fz=(short)z;
	 array.add(pt);
	 return array.size();
 }

  public void reverse(){
	  Collections.reverse(array);
  }


 public void sinewave(double period, int repeats, double mX, double oX, double mY, double oY, double mZ, double oZ ){
	 array.clear();
	 for (int r=0;r<repeats;r++){
	  for(int i=0;i<(int)period;i++){
		 DataPoint pt=new DataPoint();
		 double v=Math.sin(i*(2*Math.PI/period));
		 pt.px=(short)(v*mX+oX);
		 pt.py=(short)(v*mY+oY);
		 pt.pz=(short)(v*mZ+oZ);
		 array.add(pt);
	  }
     }
 }

  public void reset(){writeposition=0; readposition=0; lastsimulated=0; lastviewed=0;}

  public void writePositionArray(short[] posarray, int n){
	  int _n=n*4;
	  if (_n>posarray.length) _n=posarray.length;
	  for (int i=0;i<_n;i+=4){
		  DataPoint pt=array.get( (writeposition+i/4)%array.size() );
		  posarray[i]=pt.px;
		  posarray[i+1]=pt.py;
		  posarray[i+2]=pt.pz;
		  posarray[i+3]=pt.pz;//this isn't used
	   }
	   writeposition+=_n/4;
  }

  public void writePositionArray(short[] posarray){
	   writePositionArray(posarray,posarray.length/4);
   }


  public void readFeedbackDataArray(short[] fbarray, short[] data, int n){
	  int _n=n*4;
	  if (_n>fbarray.length) _n=fbarray.length;
	  for (int i=0;i<_n;i+=4){
		  DataPoint pt=array.get( (readposition+i/4)%array.size() );
		  pt.fx=fbarray[i];
		  pt.fy=fbarray[i+1];
		  pt.fz=fbarray[i+2];
		  if (PHOTONCOUNTING) pt.d1=data[i/2];
		  else pt.d1=fbarray[i+3];

	      pt.d2=data[i/2+1];
	   }
	  readposition+=_n/4;
 }



  public void readFeedbackDataArray(short[] fbarray, short[] data){
     readFeedbackDataArray(fbarray,data,fbarray.length/4);
 }



  public DataPoint get(int z, int y, int x){
	 if (y%2==0) return array.get(z*(nx*ny)+y*nx+x);
	 else return array.get(z*(nx*ny)+y*nx+(nx-x-1));
  }

 /*
  assumes d1 holds data
 */
 public double generateImage(int z){
   int xi,yi;
   int u=undersamplefactor;
   if (sparse==null) sparse=new Matrix(1,ny*u, nx*u);
   if (mout==null) mout=new Matrix(1,ny,nx);
   sparse.set(-1);
   double xspan=(double)(ex-sx);
   double yspan=(double)(ey-sy);
   double xcompress=xspan/sparse.xdim;
   double ycompress=yspan/sparse.ydim;
   double collisions=0;
   for (DataPoint pt:array){
	       xi=(int)(Math.round((pt.fx-sx)/xcompress));
	       yi=(int)(Math.round((pt.fy-sy)/ycompress));
           double v=sparse.dat.get(0,yi,xi);
           if (v!=-1) collisions+=1;
           sparse.dat.set(0,yi,xi,pt.d1);
	   }

	for (int y=0;y<mout.ydim;y++){
			for (int x=0;x<mout.xdim;x++){
			   mout.dat.set(0,y,x,weighted(y,x,u));
		  }
	  }
	  return collisions/(nx*ny);
   }


 public int makeSparse(){
	   int xi,yi;
	    int u=undersamplefactor;
	    if (sparse==null) sparse=new Matrix(1,ny*u, nx*u);
	    sparse.set(-1);
	    //double xspan=(double)(ex-sx);
	    //double yspan=(double)(ey-sy);
	    double xcompress=xspan/sparse.xdim;
	    double ycompress=yspan/sparse.ydim;
	    int collisions=0;
	    for (DataPoint pt:array){
	 	       xi=(int)(Math.round((pt.fx-sx)/xcompress));
	 	       yi=(int)(Math.round((pt.fy-sy)/ycompress));
	            double v=sparse.dat.get(0,yi,xi);
	            if (v!=-1) collisions+=1;
	            sparse.dat.set(0,yi,xi,pt.d1);
	   }
	  return collisions;
 }

 int lastviewed=0;
 public void preview(Matrix ma){
  int xi,yi;
  setupPreview(ma);
  //ma.dat.set(0);
  for (int i=lastviewed;i<readposition;i++){
      DataPoint pt=array.get(i);
	  xi=(int)(Math.round((pt.fx-sx)/xcompress));
	  yi=(int)(Math.round((pt.fy-sy)/ycompress));
	  ma.dat.set(0,yi,xi,pt.d1);
 }
 lastviewed=readposition;
 if (ma.vw!=null) ma.vw.JumpToFrame(0);
}

public boolean PreviewFirstPass=false;
public boolean PreviewSecondPass=false;
public void setupPreview(Matrix ma){
  if (!PreviewFirstPass){
   xspan=attenuationfactor*(double)(ex-sx);
   yspan=attenuationfactor*(double)(ey-sy);
   zspan=(double)(ez-sz);
   xcompress=xspan/ma.xdim;
   ycompress=yspan/ma.ydim;
   zcompress=zspan/ma.zdim;
   minx=(short)(attenuationfactor*sx);
   miny=(short)(attenuationfactor*sy);
   minz=(short)sz;
   PreviewFirstPass=true;
  }else{
    if ((!PreviewSecondPass)&&(readposition>(nx*ny))){
	   getRange(ma);
	   PreviewSecondPass=true;
	 }
   }
}

public void maybeDraw(Matrix ma){
	if (viewer!=null){
		viewer.draw(ma);
	}
    //else
    //if (ma.vw!=null) ma.vw.JumpToFrame(0);
	else System.out.println("ConfocalDataSet: no viewers");
}

public void maybeDraw(Matrix ma, int z){
	if (viewer!=null){
		viewer.draw(ma,z);
        System.out.println("drew z="+z);
    }
    //else
    //if (ma.vw!=null) ma.vw.JumpToFrame(z);
	else System.out.println("ConfocalDataSet: no viewers");
}

public void previewOversampled(Matrix ma,boolean return_error){
  int xi,yi, xi_last,diff;
  double v;

  //ma.dat.set(0);
  int i;
  xi_last=Integer.MAX_VALUE;
  setupPreview(ma);
  for (i=lastviewed;i<readposition-oversamplefactor;i+=oversamplefactor){
	double xs=0;
	double ys=0;
	double vs=0;
	for (int k=0;k<oversamplefactor;k++){
      int j=i+k;
      if (j<array.size()){
       DataPoint pt=array.get(j);
       xs+=pt.fx;
       ys+=pt.fy;
       vs+=pt.d1;
       }
     }
	  xi=(int)(Math.round((xs/oversamplefactor-minx)/xcompress));
	  yi=(int)(Math.round((ys/oversamplefactor-miny)/ycompress));
	  if (return_error){
       if (xi_last==Integer.MAX_VALUE) xi_last=xi;
	   diff=Math.abs(xi-xi_last);
	   xi_last=xi;
       ma.dat.set(0,yi,xi,diff);
      }
	  else{
	  v=-vs/oversamplefactor;
	  for (int xx=-1;xx<=1;xx++){
		  for (int yy=-1;yy<=1;yy++){
			  ma.dat.set(0,yi+yy,xi+xx,v);
		  }
	  }
     }
 }
 lastviewed=i;
 maybeDraw(ma);

}


public void previewOversampledSkip(Matrix ma,boolean skip){
  int xi,yi, xi_last,diff;
  double v;

  //ma.dat.set(0);
  int i;
  xi_last=Integer.MAX_VALUE;
  setupPreview(ma);
  for (i=lastviewed;i<readposition-oversamplefactor;i+=oversamplefactor){
	double xs=0;
	double ys=0;
	double vs=0;
	for (int k=0;k<oversamplefactor;k++){
      int j=i+k;
      if (j<array.size()){
       DataPoint pt=array.get(j);
       xs+=pt.fx;
       ys+=pt.fy;
       vs+=pt.d1;
       }
     }
	  xi=(int)(Math.round((xs/oversamplefactor-minx)/xcompress));
	  yi=(int)(Math.round((ys/oversamplefactor-miny)/ycompress));
	  if ((skip)&& (yi%2==0)){
      v=-vs/oversamplefactor;
	  for (int xx=-1;xx<=1;xx++){
		  for (int yy=-1;yy<=1;yy++){
			  ma.dat.set(0,yi+yy,xi+xx,v);
		  }
	  }
     }
	else if (!skip) {
	  v=-vs/oversamplefactor;
	  for (int xx=-1;xx<=1;xx++){
		  for (int yy=-1;yy<=1;yy++){
			  ma.dat.set(0,yi+yy,xi+xx,v);
	 	  }
	     }
     }
 }
 lastviewed=i;
 maybeDraw(ma);
}


public boolean autoscale=true;
public void previewOversampledInput(Matrix ma,boolean skip){
  int xi,yi, xi_last,diff;
  double v;

  int i;
  xi_last=Integer.MAX_VALUE;
  double xspan=attenuationfactor*(double)(ex-sx);
  double yspan=attenuationfactor*(double)(ey-sy);
  double xcompress=xspan/ma.xdim;
  double ycompress=yspan/ma.ydim;
  for (i=lastviewed;i<readposition-oversamplefactor;i+=oversamplefactor){
	double xs=0;
	double ys=0;
	double vs=0;
	for (int k=0;k<oversamplefactor;k++){
      int j=i+k;
      if (j<array.size()){
       DataPoint pt=array.get(j);
       xs+=pt.px;
       ys+=pt.py;
       vs+=pt.d1;
       }
     }
	  xi=(int)(Math.round((xs/oversamplefactor-sx)/xcompress));
	  yi=(int)(Math.round((ys/oversamplefactor-sy)/ycompress));
	  if ((!skip)||((skip)&&(yi%2==0))) {
	  v=-vs/oversamplefactor;
	  for (int xx=-1;xx<=1;xx++){
		  for (int yy=-1;yy<=1;yy++){
			  ma.dat.set(0,yi+yy,xi+xx,v);
		  }
	  }
     }
 }
 lastviewed=i;
 maybeDraw(ma);
}


/*
  if (yi%2==0) ma.dat.set(0,yi,xi,pt.d1);
	  else ma.dat.set(0,yi,xi,ma.dat.get(0,yi-1,xi));

	        DataPoint pt=array.get(i);
	  	  xi=(int)(Math.round((pt.px-sx)/xcompress));
	  yi=(int)(Math.round((pt.py-sy)/ycompress));
*/
/*

 public void previewOversampledSkip(Matrix ma){
  int xi,yi,yo;
  double v;
  DataPoint pt,pt_old;

  double xspan=(double)(ex-sx);
  double yspan=(double)(ey-sy);
  double xcompress=xspan/ma.xdim;
  double ycompress=yspan/ma.ydim;
  //ma.dat.set(0);
  for (int i=lastviewed;i<readposition;i+=oversamplefactor){
	double xs=0;
	double ys=0;
	double vs=0;
	for (int k=0;k<oversamplefactor;k++){
      pt=array.get(i+k);
      vs+=pt.d1;
     }
      pt=array.get(i);
      if (i>0) pt_old=array.get(i-1);
      else pt_old=pt;
	  xi=(int)(Math.round((pt.px-sx)/xcompress));
	  yi=(int)(Math.round((pt.py-sy)/ycompress));
	  yo=(int)(Math.round((pt_old.py-sy)/ycompress));
	  v=-vs/oversamplefactor;
	  if (yi%2==0){ ma.dat.set(0,yi,xi,v); ma.dat.set(0,yi+1,xi,v);}
	   //else if (yi<yo) ma.dat.set(0,yi,xi,ma.dat.get(0,yi-1,xi));
	   //else ma.dat.set(0,yi,xi,ma.dat.get(0,yi+1,xi));
    }

 lastviewed=readposition;
 if (ma.vw!=null) {
	 if (autoscale) rescale(ma);
	 ma.vw.JumpToFrame(0);
    }
}
*/

/*
public void previewAverage(Matrix ma, int w, boolean skip, boolean return_error, int shiftcomp){
  int xi,yi,i,k,j,diff,xi_last;

  double x_sum,y_sum;
  DataPoint pt;
  setupPreview(ma);
  xi_last=Integer.MAX_VALUE;
  xps=new double[w*2+1];
  yps=new double[w*2+1];
  for (i=lastviewed;i<readposition;i++){
   x_sum=y_sum=0;
   if ((i>=w)&&(i<array.size()-w)){ //bounds
	for (k=0;k<w*2+1;k++){
       pt=array.get(i+(k-w));
	   xps[k]=pt.fx;
	   yps[k]=pt.fy;
     }
    for (j=0;j<w*2+1;j++){
	 x_sum+=xps[j];
	 y_sum+=yps[j];
    }
    double mx=x_sum/(2*w+1);
    double my=y_sum/(2*w+1);
    pt=array.get(i);
    double v=(double)pt.d1;
    xi=(int)(Math.round((mx-minx)/xcompress));
    if (pt.R2L) xi+=shiftcomp; //shiftcomp tries to compensate for inertia error. Should really be a float
    yi=(int)(Math.round((my-miny)/ycompress));
    if (return_error){
	       if (xi_last==Integer.MAX_VALUE) xi_last=xi;
		   diff=Math.abs(xi-xi_last);
		   xi_last=xi;
		   v=diff;
    }
    if ((!skip) || ((skip)&&(!pt.R2L)))  {
    for (int xx=-1;xx<=1;xx++){
		  for (int yy=-1;yy<=1;yy++){
			  ma.dat.set(0,yi+yy,xi+xx,v);
		  }
	  }
    }

  }
 }
 lastviewed=readposition;
 maybeDraw(ma);
 //if (ma.vw!=null) ma.vw.JumpToFrame(0);
}
*/

public void previewAverage(Matrix ma, int w, boolean skip, boolean return_error, int shiftcomp){
  int xi,yi,zi,i,k,j,diff,xi_last;

  double x_sum,y_sum,z_sum;
  DataPoint pt;
  setupPreview(ma);
  xi_last=Integer.MAX_VALUE;
  zi=0;

  xps=new double[w*2+1];
  yps=new double[w*2+1];
  zps=new double[w*2+1];

  averageWindow=w;
  averageSkip=skip;
  averageError=return_error;
  averageCorrection=shiftcomp;


  for (i=lastviewed;i<readposition;i++){
   z_sum=x_sum=y_sum=0;
   if ((i>=w)&&(i<array.size()-w)){ //bounds
	for (k=0;k<w*2+1;k++){
       pt=array.get(i+(k-w));
	   xps[k]=pt.fx;
	   yps[k]=pt.fy;
	   zps[k]=pt.pz; //right now its not monitored correctly - take the set value
     }
    for (j=0;j<w*2+1;j++){
	 x_sum+=xps[j];
	 y_sum+=yps[j];
	 z_sum+=zps[j];
    }
    double mx=x_sum/(2*w+1);
    double my=y_sum/(2*w+1);
    double mz=z_sum/(2*w+1);
    pt=array.get(i);
    double v=(double)pt.d1;
    xi=(int)(Math.round((mx-minx)/xcompress));
    if (pt.R2L) xi+=shiftcomp; //shiftcomp tries to compensate for inertia error. Should really be a float
    yi=(int)(Math.round((my-miny)/ycompress));
    if (zcompress==0) zi=0; else
    zi=(int)(Math.round((mz-minz)/zcompress));
    if (return_error){
	       if (xi_last==Integer.MAX_VALUE) xi_last=xi;
		   diff=Math.abs(xi-xi_last);
		   xi_last=xi;
		   v=diff;
    }
    if ((!skip) || ((skip)&&(!pt.R2L)))  {
    for (int xx=-1;xx<=1;xx++){
		  for (int yy=-1;yy<=1;yy++){
			  ma.dat.set(0,yi+yy,xi+xx,v);
		  }
	  }
    }
  }
 }
 lastviewed_z=0;
 lastviewed=readposition;
 changed.set(readposition);
}

public short z_window=1;
public void viewAverage(short target_z, int w, boolean skip, boolean return_error, int shiftcomp){
  System.out.println("viewAverage called with z="+target_z);
  mout.set(-1);
  int xi,yi,zi,i,k,j,diff,xi_last;
  double x_sum,y_sum,z_sum;
  DataPoint pt,cpt;
  int count=0;
  if ((mout==null)||(mout.xdim!=nx)||(mout.ydim!=ny)) mout=new Matrix(1,ny,nx);
  getRange(mout);
  xi_last=Integer.MAX_VALUE;
  zi=0;
  xps=new double[w*2+1];
  yps=new double[w*2+1];
  zps=new double[w*2+1];
  int start=0;
  int end=array.size();
  if (MODE==IMAGEMODE.Z_STACK){

 start=target_z*nx*ny;

 end=(target_z+1)*nx*ny;

}  for (i=start;i<end;i++){
   cpt=array.get(i);
   if ((MODE==IMAGEMODE.Z_STACK)||((cpt.pz >= target_z-z_window)&&(cpt.pz <=target_z+z_window))) {
   count+=1;
   //z_sum=
   x_sum=y_sum=0;
   if ((i>=w)&&(i<array.size()-w)){ //bounds
	for (k=0;k<w*2+1;k++){
       pt=array.get(i+(k-w));
	   xps[k]=pt.fx;
	   yps[k]=pt.fy;
	//   zps[k]=pt.pz; //right now its not monitored correctly - take the set value
     }
    for (j=0;j<w*2+1;j++){
	 x_sum+=xps[j];
	 y_sum+=yps[j];
	// z_sum+=zps[j];
    }
    double mx=x_sum/(2*w+1);
    double my=y_sum/(2*w+1);
    //double mz=z_sum/(2*w+1); //not used
    double v=(double)cpt.d1;
    xi=(int)(Math.round((mx-minx)/xcompress));
    if (cpt.R2L) xi+=shiftcomp; //shiftcomp tries to compensate for inertia error. Should really be a float
    yi=(int)(Math.round((my-miny)/ycompress));
    //if (zcompress==0) zi=0; else zi=(int)(Math.round((mz-minz)/zcompress)); //not used
    if ((xi<0)||(yi<0)||(xi>512)||(yi>512)) System.out.println("xi="+xi+" yi="+yi+" minx="+minx+" xcompress="+xcompress);

    if ((!skip) || ((skip)&&(!cpt.R2L)))  {
    for (int xx=-1;xx<=1;xx++){
		  for (int yy=-1;yy<=1;yy++){
			  mout.dat.set(0,yi+yy,xi+xx,v);
		  }
	  }
    }
   }
  }//is z in range
  //System.out.println("count ="+count);
 }//main loop
 lastviewed_z=target_z;
}


public void resample(Matrix ma, double dx_even, double dx_odd){
	int x,y;
	double[] tmp=new double[ma.xdim];
	for (y=0;y<ma.ydim;y++){
		for (x=0;x<ma.xdim;x++) tmp[x]=0;
		for (x=0;x<ma.xdim;x++){
			 if (y%2==0){
				if ((x+dx_even>0)&&(x+dx_even<ma.xdim))tmp[x]=ma.dat.real_get(0,y,x+dx_even);
		       }
		      if (y%2==1){
					if ((x+dx_odd>0)&&(x+dx_odd<ma.xdim))tmp[x]=ma.dat.real_get(0,y,x+dx_odd);
			       }
			   }
	    for (x=0;x<ma.xdim;x++) ma.dat.set(0,y,x,tmp[x]);
     }
    ma.dat.constant_multiply(1.05);
    //if (ma.vw!=null) ma.vw.JumpToFrame(0);
    maybeDraw(ma);
}


public void previewAverageError(Matrix ma, int w){
   int xi,yi,i,k,j;
  double max_x, min_x, max_y, min_y;
  double x_sum,y_sum;
  DataPoint pt;
  double xspan=(double)(ex-sx);
  double yspan=(double)(ey-sy);
  double xcompress=xspan/ma.xdim;
  double ycompress=yspan/ma.ydim;
  xps=new double[w*2+1];
  yps=new double[w*2+1];

  for (i=lastviewed;i<readposition;i++){
   max_y=max_x=Double.NEGATIVE_INFINITY;
   min_y=min_x=Double.POSITIVE_INFINITY;
   x_sum=y_sum=0;
   if ((i>w)&&(i<array.size()-w)){ //bounds
	for (k=0;k<w*2+1;k++){
       pt=array.get(i+(k-w));
	   xps[k]=pt.fx;
	   yps[k]=pt.fy;

     }
    for (j=0;j<w*2+1;j++){
	  x_sum+=xps[j];
	  y_sum+=yps[j];
    }
    double mx=x_sum/(2*w+1);
    double my=y_sum/(2*w+1);
    pt=array.get(i);

    xi=(int)(Math.round((mx-sx)/xcompress));
    yi=(int)(Math.round((my-sy)/ycompress));
	ma.dat.set(0,yi,xi,Math.abs(mx-pt.px));
  }
 }
 lastviewed=readposition;
 maybeDraw(ma);
 //if (ma.vw!=null) ma.vw.JumpToFrame(0);
}






 public double[] xps;
 public double[] yps;
 public double[] zps;
 public void previewMedian(Matrix ma,int w){
  int xi,yi;
  double xspan=(double)(ex-sx);
  double yspan=(double)(ey-sy);
  double xcompress=xspan/ma.xdim;
  double ycompress=yspan/ma.ydim;
  xps=new double[w*2+1];
  yps=new double[w*2+1];
  //ma.dat.set(0);
  for (int i=lastviewed;i<readposition;i++){
	   for (int k=-w;k<=w;k++){
	  	 int j=i+k;
	  	 if ((j>=0)&&(j<array.size())){
		   DataPoint pt=array.get(j);
		   xps[k+w]=pt.fx;
		   yps[k+w]=pt.fy;
		 }
	    }
      double mx=JSci.maths.ArrayMath.median(xps);
      double my=JSci.maths.ArrayMath.median(yps);
      DataPoint pt=array.get(i);
	  xi=(int)(Math.round((mx-sx)/xcompress));
	  yi=(int)(Math.round((my-sy)/ycompress));
	  ma.dat.set(0,yi,xi,pt.d1);
 }
 lastviewed=readposition;
 maybeDraw(ma);
 //if (ma.vw!=null) ma.vw.JumpToFrame(0);
}




 public void previewInput(Matrix ma){
  int xi,yi;
  setupPreview(ma);
  //ma.dat.set(0);
  for (int i=lastviewed;i<readposition;i++){
      DataPoint pt=array.get(i);
	  xi=(int)(Math.round((pt.px-sx)/xcompress));
	  yi=(int)(Math.round((pt.py-sy)/ycompress));
	  ma.dat.set(0,yi,xi,pt.d1);
 }
 lastviewed=readposition;
 maybeDraw(ma);
 //if (ma.vw!=null) ma.vw.JumpToFrame(0);

}

 public void previewInputSkip(Matrix ma){
  int xi,yi;
  setupPreview(ma);
  //ma.dat.set(0);
  for (int i=lastviewed;i<readposition;i++){
      DataPoint pt=array.get(i);
	  xi=(int)(Math.round((pt.px-sx)/xcompress));
	  yi=(int)(Math.round((pt.py-sy)/ycompress));
	  if (yi%2==0) ma.dat.set(0,yi,xi,pt.d1);
	  else ma.dat.set(0,yi,xi,ma.dat.get(0,yi-1,xi));
 }
 lastviewed=readposition;
 maybeDraw(ma);
 //if (ma.vw!=null) ma.vw.JumpToFrame(0);
}


 public void previewInputSkipError(Matrix ma){
  int xi,yi;
  double xspan=(double)(ex-sx);
  double yspan=(double)(ey-sy);
  double xcompress=xspan/ma.xdim;
  double ycompress=yspan/ma.ydim;
  //ma.dat.set(0);
  for (int i=lastviewed;i<readposition;i++){
      DataPoint pt=array.get(i);
	  xi=(int)(Math.round((pt.px-sx)/xcompress));
	  yi=(int)(Math.round((pt.py-sy)/ycompress));
	  if (yi%2==0) ma.dat.set(0,yi,xi,pt.fx-pt.px);
	  else ma.dat.set(0,yi,xi,ma.dat.get(0,yi-1,xi));
 }
 lastviewed=readposition;
 maybeDraw(ma);
 //if (ma.vw!=null) ma.vw.JumpToFrame(0);
}

 public void previewError(Matrix ma){
  int xi,yi;
  double xspan=(double)(ex-sx);
  double yspan=(double)(ey-sy);
  double xcompress=xspan/ma.xdim;
  double ycompress=yspan/ma.ydim;
  //ma.dat.set(0);
  for (int i=lastviewed;i<readposition;i++){
      DataPoint pt=array.get(i);
	  xi=(int)(Math.round((pt.px-sx)/xcompress));
	  yi=(int)(Math.round((pt.py-sy)/ycompress));
	  ma.dat.set(0,yi,xi,Math.abs(pt.fx-pt.px));
 }
 lastviewed=readposition;
 maybeDraw(ma);
 //if (ma.vw!=null) ma.vw.JumpToFrame(0);
}


public void rescale(Matrix ma){
	double max=Double.NEGATIVE_INFINITY;
	double min=Double.POSITIVE_INFINITY;
	int cx=ma.xdim/2;
	int cy=ma.ydim/2;
	double v;
	for (int x=cx-20;x<cx+20;x++){
		for (int y=cy-20;y<cy+20;y++){
			v=ma.dat.get(0,y,x);
			if (v!=0){
				if (v>max) max=v;
				if (v<min) min=v;
			}
		}
	  }
	  ma.vw.scale=((double)255)/((double)(max-min));
	  ma.vw.offset=(int)min;
}


 double weighted(int y, int x, int u){
	    int xx,yy;
	    double d;
	    int count=0;
	    double weights=0;
	    double values=0;
	    double w=0;
		for (int i=-u;i<=u;i++){
		 for (int j=-u;j<=u;j++){
			yy=y*u+i;
			xx=x*u+j;
			if ((yy>=0)&&(yy<sparse.ydim)&&(xx>=0)&&(xx<sparse.xdim)){
				if (sparse.dat.get(0,yy,xx)!=-1){
	             d=distance_yx(y*u,yy,x*u,xx);
	             if (d==0) return sparse.dat.get(0,yy,xx);
	             if (d<u){
		           w=1.0/(d*d);
		           values+=w*sparse.dat.get(0,yy,xx);
		           weights+=w;
			   }
		   }//-1
	     }//bounds
       }//j
      }//i
     return values/weights;
 }

  public boolean copy(Matrix ma){
	  if ((ma.zdim!=nz)||(ma.ydim!=ny)||(ma.xdim!=nx)) return false;
	  for (int z=0;z<nz;z++){
		  for (int y=0;y<ny;y++){
			  for (int x=0;x<nx;x++){
				  DataPoint pt=get(z,y,x);
				  ma.dat.set(z,y,x,(double)pt.d1);
			  }
		  }
	  }
	  return true;
  }
  //note the funny order of inputs.
  double distance_zyx(short z1, short z2, short y1, short y2, short x1, short x2){ return Math.sqrt( (z1-z2)*(z1-z2)+(y1-y2)*(y1-y2)+(x1-x2)*(x1-x2) );}
  double distance_yx( short y1, short y2, short x1, short x2){ return Math.sqrt((y1-y2)*(y1-y2)+(x1-x2)*(x1-x2) );}
  double distance_yx(int y1, int y2, int x1, int x2){return Math.sqrt((y1-y2)*(y1-y2)+(x1-x2)*(x1-x2) );}


  public boolean frameInfo(Matrix ma){
  	  if ((ma.zdim!=5)||(ma.ydim!=ny)||(ma.xdim!=nx)) return false;
  		  for (int y=0;y<ny;y++){
  			  for (int x=0;x<nx;x++){
  				  DataPoint pt=get(0,y,x);
  				  ma.dat.set(0,y,x,(double)distance_yx(pt.py,pt.fy,pt.px,pt.fx));
  				  ma.dat.set(1,y,x,pt.px);
  				  ma.dat.set(2,y,x,pt.fx);
  				  ma.dat.set(3,y,x,pt.py);
  				  ma.dat.set(4,y,x,pt.fy);
  			  }
  		  }
  	  return true;
  }


  int lastsimulated=0;

  public double simulateImage(){
	  if ((fakedata==null)||(fakedata.xdim!=nx)||(fakedata.ydim!=ny)){
		  fakedata=new Matrix(1,ny,nx);
		  fakedata.set(20);
		  for (int y=0;y<ny;y++){
			  for (int x=0;x<nx;x++){
			   if (((y/10)%2==0)&&((x/10)%2==0)) fakedata.dat.set(0,y,x,10);
		   }
	     }
        }
      double scaley=(2.0*(ey-sy))/((double)ny);
      double scalex=(2.0*(ex-sx))/((double)ny);
      for (int i=lastsimulated;i<readposition;i++){
		  DataPoint pt=array.get(i);
		  double xx=(pt.fx-sx)/scalex+1;
          double yy=(pt.fy-sy)/scaley+1;
          pt.d1=(short)(fakedata.dat.real_get(0,yy,xx));
	  }
	lastsimulated=readposition;
	return scalex;
}


public int sign(DataPoint p1, DataPoint p2, DataPoint p3){
	return (p1.fx - p3.fx) * (p2.fy - p3.fy) - (p2.fx - p3.fx) * (p1.fy - p3.fy);
}

public boolean PointInTriangle(DataPoint pt, DataPoint v1, DataPoint v2, DataPoint v3){

	boolean b1,b2,b3;
	b1=(sign(pt,v1,v2)<0);
	b2=(sign(pt,v2,v3)<0);
	b3=(sign(pt,v3,v1)<0);
	return((b1==b2)&&(b2==b3));

}

public double area(DataPoint p0, DataPoint p1, DataPoint p2){
  return 0.5*(-p1.fy*p2.fx + p0.fy*(-p1.fx + p2.fx) + p0.fx*(p1.fy - p2.fy) + p1.fx*p2.fy);
}

public boolean BarycentricPointInTriangle(DataPoint p, DataPoint p0, DataPoint p1, DataPoint p2){
	double A=area(p0,p1,p2);

	double s = 1/(2*A)*(p0.fy*p2.fx - p0.fx*p2.fy + (p2.fy - p0.fy)*p.fx + (p0.fx - p2.fx)*p.fy);
	double t = 1/(2*A)*(p0.fx*p1.fy - p0.fy*p1.fx + (p0.fy - p1.fy)*p.fx + (p1.fx - p0.fx)*p.fy);
    return ((s>0)&&(t>0)&&(1-s-t>0));

}


public double weightedValue(DataPoint p, DataPoint p1, DataPoint p2, DataPoint p3){
	//assumes point in triangle
	double TA=area(p1,p2,p3);
	double A1=area(p,p1,p2);
	double A2=area(p,p2,p3);
	double A3=area(p,p3,p1);
	return p1.d1*A2/TA+p2.d1*A3/TA+p3.d1*A1/TA;
 }

public boolean inBounds(Matrix ma, int y, int x){
	return ((x>=0)&&(x<ma.xdim)&&(y>=0)&&(y<ma.ydim));
}


ArrayList <Circuit> circuits=new ArrayList<Circuit>(20);
public int findFourClosest(Matrix ma, int y, int x){

  int r=0;
  int k=0;
  int count=0;
  if ((circuits==null)||(circuits.size()==0)){
	  for (k=0;k<20;k++){ circuits.add(new Circuit(k+1));}
  }
  for (k=0;k<4;k++) FourClosest[k]=null;
  while (((FourClosest[0]==null))||((FourClosest[1]==null))||((FourClosest[2]==null))||((FourClosest[3]==null))){
	  Circuit cc=circuits.get(r);
	  cc.reset();
	  while (cc.hasNext()){
		  java.awt.Point p=cc.next();

		  if (inBounds(ma,p.y+y, p.x+x)&& ma.dat.get(0,p.y+y,p.x+x)!=-1){
			if ((FourClosest[0]==null)&&(p.y<0)&&(p.x<0)){FourClosest[0]=p; count++;}
			if ((FourClosest[1]==null)&&(p.y<0)&&(p.x>=0)){FourClosest[1]=p; count++;}
			if ((FourClosest[2]==null)&&(p.y>=0)&&(p.x>=0)){FourClosest[2]=p; count++;}
			if ((FourClosest[3]==null)&&(p.y>=0)&&(p.x<=0)){FourClosest[3]=p; count++;}
		  }//if ma
	   }//while cc.hasNext
	 r+=1;
	 if (r==20) return count;
    }//while not all found
    return count;
  }

  public double closestNeighbourWeight(int nbs, Matrix m_in, int y, int x){
	  double ws=0.00001;
	  double vs=0;
	  for (int i=0;i<nbs;i++){
		  java.awt.Point p=FourClosest[i];
		  double d= distance_yx(p.y,0,p.x,0);
		  double w=1.0/Math.pow(d,1.5);
	      vs+=w*m_in.dat.get(0,p.y+y,p.x+x);
	      ws+=w;
	  }
	  return vs/ws;
  }

  public int generateClosestNeighbourImage(Matrix m_in, Matrix m_out){
	  m_out.dat.set(-1);
	  double xscale=m_in.xdim/m_out.xdim;
	  double yscale=m_in.ydim/m_out.ydim;
	  int misses=0;
	  for (int y=0;y<m_out.ydim;y++){
		   for (int x=0;x<m_out.xdim;x++){
			  int ys=(int)(yscale*y);
			  int xs=(int)(xscale*x);
			  double v=m_in.dat.get(0,ys,xs);
			  if (v!=-1) m_out.dat.set(0,y,x,v);
			  else{
				  int nbs=findFourClosest(m_in,ys,xs);
				  if (nbs==4)      m_out.dat.set(0,y,x,closestNeighbourWeight(nbs,m_in,ys,xs));
				  else misses+=1;
			    }//else
             }//x
		   }//y
    return misses;
}


public int clean(){
	makeSparse();
	int v=generateClosestNeighbourImage(sparse,mout);
	mout.vw.JumpToFrame(0);
	return v;
}

 /*
 need, for every pixel 4 (y,x) indices and 4 weigths
 in matrix object (0 = view, (1=x1, 2=y1, 3= w) 456 789, 10,11,12)
 */

public int setupClosestNeighbourImage(Matrix m_in, Matrix m_out){
	  m_out.dat.set(-1000); //magic number
	  double xscale=m_in.xdim/m_out.xdim;
	  double yscale=m_in.ydim/m_out.ydim;
	  int misses=0;
	  for (int y=0;y<m_out.ydim;y++){
		   for (int x=0;x<m_out.xdim;x++){
			  int ys=(int)(yscale*y);
			  int xs=(int)(xscale*x);
			  double v=m_in.dat.get(0,ys,xs);
			  if (v!=0){
				  int nbs=findFourClosest(m_in,ys,xs);
				  if (nbs==4){

				    for (int k=0;k<4;k++){
						java.awt.Point p=FourClosest[k];
						m_out.dat.set(1+k*3,y,x,p.x);
						m_out.dat.set(1+k*3+1,y,x,p.y);
						double d= distance_yx(p.y,0,p.x,0);
		                double w=1.0/Math.pow(d,1.5);
		                m_out.dat.set(1+k*3+2,y,x,w);

					   }
				  }
				  else misses+=1;
			    }//else
             }//x
		   }//y
    return misses;
}

public void getClosestNeighbourImage(Matrix m_in, Matrix m_out){
    double xscale=m_in.xdim/m_out.xdim;
	double yscale=m_in.ydim/m_out.ydim;
	for (int y=0;y<m_out.ydim;y++){
		   for (int x=0;x<m_out.xdim;x++){
			   m_out.dat.set(0,y,x,-1);
			   if (m_out.dat.get(1,y,x)!=-1000){
				 double wts=0.00001;
				 double vs=0;
				 for (int k=0;k<4;k++){
					 int xd=(int)m_out.dat.get(1+k*3,y,x);
					 int yd=(int)m_out.dat.get(1+k*3+1,y,x);
					 double w=m_out.dat.get(1+k*3+2,y,x);
				      vs+=m_in.dat.get(0,(int)(y*yscale+yd),(int)(x*xscale+xd))*w;
				      wts+=w;
				  }
				  m_out.dat.set(0,y,x,vs/wts);
				 }//>-1
			   }//x
		      }//y

}

public void getRange(Matrix ma){
     maxz=maxy=maxx=Short.MIN_VALUE;
     minz=miny=minx=Short.MAX_VALUE;
     for (int i=20;i<array.size()-20;i++){
		DataPoint p=array.get(i);
		if (p.fx>maxx) maxx=p.fx;
		if (p.fy>maxy) maxy=p.fy;
		if (p.pz>maxz) maxz=p.pz;//no feedback on z yet
		if (p.fx<minx) minx=p.fx;
		if (p.fy<miny) miny=p.fy;
		if (p.pz<minz) minz=p.pz;//no feedback on z yet
	}
    xspan=(double)(maxx-minx);
    yspan=(double)(maxy-miny);
    zspan=(double)(maxz-minz);
    xcompress=(double)xspan/ma.xdim;
    ycompress=(double)yspan/ma.ydim;
    if (ma.zdim==1) zcompress=(double)zspan;
    else zcompress=(double)(zspan/(ma.zdim-1));
    //zcompress=(double)zspan/ma.zdim;
 }
}

 class DataPoint{
    short px,py,pz;
    short fx,fy,fz;
    short d1,d2;
    boolean R2L;
    }

 enum IMAGEMODE{
	 UNSTRUCTURED(0),Z_STACK(1),PATH(2);
	 private final int value;
	 private IMAGEMODE(int value) {
	         this.value = value;
	 }
	 public int getValue() {
	         return value;
    }
}
enum IMAGEMAGNIFICATION{
	FIVEX(5),TENX(10),TWENTYX(20),FORTYX(40),FIFTYX(50),SIXTYX(60),HUNDREDX(100);
	private final int value;
    private IMAGEMAGNIFICATION(int value) {
		 this.value = value;
     }
	public int getValue() {
	   return value;
    }
}