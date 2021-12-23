package gvdecoder;
//import com.ibm.math.array.*;


import org.python.core.*;

import gvdecoder.trace.*;
import java.io.*;

import java.nio.*;

import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.awt.FileDialog;
import java.util.*;
import JSci.maths.*;
import JSci.maths.wavelet.daubechies2.*;
import gvdecoder.array.*;
import java.awt.geom.GeneralPath;
/**

 -OverView-
 The Matrix class is class that holds several 3D data arrays (array.doubleArray3D),
 and links to a set of traces (via trace.TraceManager and trace.Trace). It is designed
 to be called from a Jython (JythonViewer.java or via an external script) scripting
 environment. A Matrix usually holds 3D intensity vs time data from a 2D imager (so
 the x and y axis represent a frame, and the z axis represents time). It uses JSci libraries
 throughout.

 -doubleArray3D-
 doubleArray3D is a class which allows a 1D array of doubles to be treated as
 a 3D array, with various set/get methods and section methods. (Accessing a 1D array
 this way is several times more fast than a regular (ie. arr[][][]) 3D array in Java).
 The most important doubleArray3D object in the Matrix class is 'dat'.
 The other arrays are 'imag' (for complex numbers), 'dat_undo' (for undo opperations),
 and 'frame' (representing a single frame (or z section) of the data.

 -Trace-
 Traces are integer arrays for intensity vs time information that will be plotted
 (either in a 'NavigatorGraph' object or a 'traceView'  object. A single trace
 is usually generated as a z section through a Matrix. The trace object also holds
 arrays for processed data and have some limited routines for processing data (these
 routines are for the most part depreciated.)

 -TraceManager-
 A utility class for accessing z-section data in a Matrix via Jython (see below).

 -Jython access-
 Jython is a Java port of Python, that has access to any public object in Matrix (or any
 other class). Several methods (those writen'__methodname__') have been writen to allow
 easy scripting. These methods are present in the Matrix class as well as in the TraceManager
 class. They have been writen to allow fast, 'in place' access to doubleArray3D data.
 For example the expression in Jython:
          ma=(ma-ma[1:10]) * -1
 does an in-place subtraction of the average of frames 1 through 10 from all frames
 in a matrix, and then multiplies all values by -1.
 The expression
        ma.trace[10,3]
 accesses a z-section of data at y=10, x=3.
 The expression:
    for i in range(len(ma.traces)):
     if (range(ma.traces[i])>500):
       ma.traces[i]=runningaverage(ma.traces[i],5))
     else:
      ma.traces[i]=0
 Runs through all traces in an array, checks to see which have a large range, performs
 a 5 pt running average on those with large range, and sets all others to 0.

 Jython can also access individual values in the Matrix (by either:
    'val=ma.dat.get(z,y,x)'  or even 'val=ma.dat.arr[index]'.(Everything is declared public).
 However, if you run through all matrix values using loops this way, access
 is very very slow through Jython (however in in Java classes, it is rather fast).


-ImageDecoder, reading, displaying and writing-
Matrix extends ImageDecoderAdapter, allowing it to be displayed in GView as a movie file. It also
allows the matrix to be opened from raw data directly, if the format of the file is writen
in 'gv' or 'ma' format (a very simple format with a tiny header and many doubles.) A matrix can
be saved into the same format using SaveImageFile(filename).

-Initialization routines-
Once an empty Matrix is created:

 import gvdecoder.Matrix as Matrix
 ma=new Matrix()

a matrix is initialized from an existing ImageDecoder instance. This can be obtained from an
open Viewer window (though Jython):
  ma.initialize(jh.presentviewer.im, start_z_index, end_z_index)
or, if the file isn't open elsewhere, via
  ma.initialize("filetype",start_z_index, end_z_index), where "filetype" is "spe", "ma", "pda", etc.

**/

public class Matrix extends ImageDecoderAdapter{
	public doubleArray3D dat;
	public doubleArray3D imag; // for complex numbers
	public doubleArray3D dat_undo=null;
	public doubleArray3D frame;
	public intArray3D am;
	public intArray3D cnt;
	//public Range xrange;
	//public Range yrange;
	//public Range zrange;
	public int framenum;
	public int xdim;
	public int ydim;
	public int zdim;
	public int offset=16;

	public int instance=0; //this is only used when directly processing spe's due to old .dll

	public boolean[][] mask=null;
	public double[][] corr=null;

	public boolean SHOWSINGULARITIES=false;
	public boolean COMPOSTSINGULARITIES=false;
	public doubleArray3D sing;

	public boolean showActiveMap=false;
	public float amThr=0.0f;
	public int amMinTimeAbove=3;
	public int amA=10;
	public int amR=10;

	public static final int NORMALIZE=1;
	public static final int  AVERAGE=2;
	public static final int  MEDIAN=3;
	public static final int SUBAVERAGE=4;
	public static final int THRESHOLD=5;
	public static final int SUPRESS=6;
	public static final int PHASE=7;
	public static final int ACTIVATION=8;
	public static final int PHASESINGULARITY=9;
	public static final int ACTIVATIONSINGULARITY=10;
	public static final int DVDT=11;
	public static final int V_CA_PHASE=12;
    public static final int FUNCTION=13;
    public static final int RANDOM=14;

	public Viewer2 vw=null;

	public boolean VERBOSE=true;

	public TraceManager traces;

public String help(){
return "Matrix\nConstructors:\n Matrix()\n Matrix(Matrix m) [copies]\n Matrix(Matrix m, startx, endx, starty, endy, startz, endz) [copies submatrix]\n Matrix(Matrix m, startx, endx, starty, endy, startz, endz, int mag, int active) [active not used, simple resample for bigger array]\n initialize(absfilename,filetype,start,stop)\n initialize(filetype,start,stop)\n initialize(ImageDecoder, start,stop)\n initialize(Viewer2,start,stop)\n initialize(double[][]arr, double mult) [not useful, probably in script, makes 3byXbyY array]\n \n Processing:\n processInSpace()[a median filter in space only]\n processByTrace(String type)\n processByTrace(String type, int arg)\n processByTrace(String type, int[] roi)\n processByTrace(String type,int arg, int[]roi)\n processByTrace(String type, int[] roi, function f)\n processByTrace(String type, int[] roi, function f)\n process(function f, int[] roi, int negz, int posz, int negy, int posy, int negx, int posx)\n \n BaseLineSubtract(int median, int average)[subtracts a median and average from the matrix, used in CAFilter]\n CAFilter(double thr1, double thr2, double thr3, int its, double attenuation, int baselinecompensate)[th1,th3 should be around 1, th2 around 0.5]\n \n suppressByTrace(threshold,percent)[determines if a traces total activity is greater than a percentage, and suppresses it if it isnt]\n suppress(int x, int y)[suppress this trace, sets to 0]\n suppress(int[][] arr)[arr holds x,y pairs, arr[n][0]=x, arr[n][1]=y]\n suppressEdges(int centerx, int centery, double r)[reduces edges by 50%, normalize to reverse]\n subtract(Matrix m)\n \n update_undo() [saves array in temp store]\n undo() [restores array]\n \n OpenImageFile(filename) [used to open file of type ma]\n SaveImageFile(filename) [used to save file of type ma]\n \n getDoubles(int x, int y) returns dat.section(x,y)\n getDoubles(int x, int y, int start, int end) [returns a subrange]\n getTrace(int x, int y)\n";
}



/**
Constructors (as called from Jython):
 import gvdecoder.Matrix as Matrix
 ma=Matrix()          #empty matrix object
 ma=Matrix(mb)        #initialize from content of a different matrix
 ma=Matrix(arr)       #initialize from a doubleArray3D
 ma=Matrix(zdim,ydim,xdim) #empty matrix of the given dimensions
 ma=Matrix(ma,sx,ex,sy,ey,sz,ez,1) #from a section of a different matrix (the '1' does nothing) *depreciated*
 ma=Matrix(ma,sx,ex,sy,ey,sz,ez,mag,1) #from a section of a different matrix, where the matrix is expanded my mag (the '1' does nothing) *depreciated*
 ma=Matrix(ma[])      #create one big matrix from an array of smaller matrix objects


 Note that some constructors call 'create'.
 Once instanciated, call 'initialize(...) to populate the matrix with data from a file or
 another source.

**/
  public Matrix(){
		 //
	//traces=new TraceManager(this);

	}

	public Matrix(Matrix m){
	// essentially clone
	this.zdim=m.zdim;
	this.xdim=m.xdim;
	this.ydim=m.ydim;
	create(zdim,ydim,xdim);
	//initialize the array
	dat.assign(m.dat);
    //traces=new TraceManager(this);
	}


    public Matrix(doubleArray3D dat){
		this.zdim=dat.zdim;
		this.xdim=dat.xdim;
		this.ydim=dat.ydim;
	    create(zdim,ydim,xdim);

	    dat.assign(dat);

      //  traces=new TraceManager(this);
	}


    public Matrix(int zdim, int ydim, int xdim){
	 this.zdim=zdim;
	 this.ydim=ydim;
	 this.xdim=xdim;
	 create(zdim,ydim,xdim);
	 //traces=new TraceManager(this);
	}




   /*changed zyx*/
	public Matrix(Matrix m,int startx, int endx, int starty, int endy, int startz, int endz, int mag){
		this.zdim=endz-startz;
		this.xdim=(endx-startx);
		this.ydim=(endy-starty);
		create(zdim,ydim,xdim);
	//	traces=new TraceManager(this);
		for (int z=startz;z<endz;z++){
			for (int y=starty;y<endy;y++){
			   for (int x=startx;x<endx;x++){
				// dat.set(z-startz,x-startx,y-starty,m.dat.get(z,x,y));
				 //dat.set(z,x,y,(z*ma.zdim+y*ma.ydim+x));
                   dat.set(z-startz,y-starty,x-startx,m.dat.get(z,y,x));
			   }
		   }
	   }

	}
    /*corrected zyx*/
	public Matrix(Matrix m,int startx, int endx, int starty, int endy, int startz, int endz, int mag, int active){
		this.zdim=endz-startz;
		this.xdim=(endx-startx)*mag;
		this.ydim=(endy-starty)*mag;
		create(zdim,ydim,xdim);
	//	traces=new TraceManager(this);
		for (int z=startz;z<endz;z++){
			for (int x=startx;x<endx;x++){
			   for (int y=starty;y<endy;y++){
				 //dat.set(z-startz,(x-startx)*mag,(y-starty)*mag,m.dat.get(z,x,y));
				 //dat.set(z,x,y,(z*ma.zdim+y*ma.ydim+x));
				 dat.set(z-startz,(y-starty)*mag,(x-startx)*mag,m.dat.get(z,y,x));

			   }
		   }

	 }


	}

/*corrected zyx*/
  public Matrix(Matrix[] matrices){
	 int newlen=0;
	 for (int i=0;i<matrices.length;i++){
		 newlen+=matrices[i].zdim;
	 }
	 this.zdim=newlen;
	 this.xdim=matrices[0].xdim;
	 this.ydim=matrices[0].ydim;
	 create(zdim,ydim,xdim);
	 //traces=new TraceManager(this);
	 int index=0;
	 for (int i=0;i<matrices.length;i++){
		 for (int z=0;z<matrices[i].zdim;z++){
			 for (int x=0;x<matrices[i].xdim;x++){
				 for (int y=0;y<matrices[i].ydim;y++){
					 dat.set(index,y,x,matrices[i].dat.get(z,y,x));
				 }
	          }
	     index++;
	   }
	 }
  }


/*corrected zyx*/
	public void create(int zdim, int ydim, int xdim){
     this.xdim=xdim;
	 this.ydim=ydim;
	 this.zdim=zdim;
	 dat=new doubleArray3D(zdim,ydim,xdim);
	 //dat_undo=new doubleArray3D(zdim,xdim,ydim);
	 frame=new doubleArray3D(ydim,xdim);
	 //xrange=new Range(0,xdim-1);
	 //yrange=new Range(0,ydim-1);
	 //zrange=new Range(0,zdim-1);
	 System.out.println("called create with "+zdim+" "+ydim+" "+xdim);
	 traces=new TraceManager(this);
	}

	public void set(int frame, int[] arr){
	 for (int i=0;i<ydim;i++){
	  for (int j=0;j<xdim;j++){
		dat.set(frame,i,j,(float)arr[i*xdim+j]);
		}
	}
	}


/*corrected zyx*/
   public void initialize(ImageDecoder id, int start, int end, int ystart, int yend, int xstart, int xend){
		    int[] tmp=new int[4];
			id.ReturnXYBandsFrames(tmp,instance);
			int[] tmpframe=new int[tmp[0]*tmp[1]];
			create((end-start),yend-ystart,xend-xstart); //xdim and ydim are set for the array
			for (int i=start;i<end;i++){
			   id.JumpToFrame(i,instance);
			   id.UpdateImageArray(tmpframe,tmp[0],tmp[1],instance);
			   // set((i-start),tmpframe);
			   for (int p=0;p<ydim;p++){
				   for (int q=0;q<xdim;q++){
					int y=p+ystart;
					int x=q+xstart;
					dat.set(i-start,p,q,tmpframe[y*tmp[0]+x]);
				   }
			   }
		     }
	}

    public void initialize(ImageDecoder id, int start, int end){
	    int[] tmp=new int[4];
		id.ReturnXYBandsFrames(tmp,instance);
		int[] tmpframe=new int[tmp[0]*tmp[1]];
		create((end-start),tmp[1],tmp[0]);
		for (int i=start;i<end;i++){
		   id.JumpToFrame(i,instance);
		   id.UpdateImageArray(tmpframe,tmp[0],tmp[1],instance);
		   set((i-start),tmpframe);
	     }


	}


/**
If the matrix is intitialized from an ImageDecoder instance from another file, the instance
is kept in 'im'.
**/
public ImageDecoder im;

/**
  to initialize a matrix object from more than one data file, where start is the framenumber in
  the first file, and end is the frame number in the last file (so start>end).

*/
public void initialize(String[] filenames, String filetype, int start, int end){
	im=ImageDecoderFactory.getDecoder(filetype);
	int[] tmp=new int[4];
	int zdim=0;
	for (int f=0;f<filenames.length;f++){
	     instance=im.OpenImageFile(filenames[f]);
	     im.ReturnXYBandsFrames(tmp,instance);
	     zdim+=tmp[3];
	     im.CloseImageFile(instance);
	}
	System.out.println("multifile init: the maximum zdim="+zdim);
	zdim-=start;
	zdim-=(tmp[3]-end);
	System.out.println("multifile init: the final zdim="+zdim);

	create(zdim,tmp[1],tmp[0]);
	int[] tmpframe=new int[tmp[0]*tmp[1]];
	int framenum=0;
	for (int f=0;f<filenames.length;f++){
	  instance=im.OpenImageFile(filenames[f]);
	  im.ReturnXYBandsFrames(tmp, instance);
      int tmpstart=0;
	  int tmpend=tmp[3];
	  if (f==0) tmpstart=start;
	  if (f==filenames.length-1) tmpend=end;
	  System.out.println("initializing from "+tmpstart+" to "+tmpend);
		  for (int z=tmpstart; z<tmpend; z++){
			  im.JumpToFrame(z,instance);
			  im.UpdateImageArray(tmpframe,tmp[0],tmp[1],instance);
			  for (int p=0;p<ydim;p++){
			    for (int q=0;q<xdim;q++){
			  	  dat.set(framenum,p,q,tmpframe[p*tmp[0]+q]);
			  	  }
			  	}
		     framenum++;

		     }
		    im.CloseImageFile(instance);
		  }
	  }





public void initialize(String absolutefilename, String filetype, int start, int end){
	   im=ImageDecoderFactory.getDecoder(filetype);
       instance=im.OpenImageFile(absolutefilename);
       initialize(im,start,end);
   }

public void reinitialize(int start, int end){
	initialize(im,start,end);
}


public void initialize(String absolutefilename, String filetype){
	   im=ImageDecoderFactory.getDecoder(filetype);
       instance=im.OpenImageFile(absolutefilename);
       int[] tmp=new int[4];
       im.ReturnXYBandsFrames(tmp, instance);
	   initialize(im,0,tmp[3]-1);
}



public String initialize(String filetype, int start, int end){

       String absfilename=FilePicker.getFilePicker().openFile(FilePicker.DataDir);
       if (absfilename!=null){
         initialize(absfilename,filetype,start,end);
       }
       return absfilename;

   }

public void initialize(Viewer2 v2, int start, int end){

		initialize(v2.im,start,end);

}


public void initialize(Matrix ma,int zoffset, int yoffset, int xoffset){
	for (int z=0;z<ma.zdim;z++){
	  for (int y=0;y<ma.ydim;y++){
		  for (int x=0;x<ma.xdim;x++){
		     dat.set(z+zoffset,y+yoffset,x+xoffset,ma.dat.get(z,y,x));
		  }
	  }
  }

}


public String info(){
 return "Inside Matrix";
}

/*corrected zyx create*/
public void initialize(double[][] arr, double mult){
	System.out.println("in init debug (0,0)="+arr[0][0]+" (1,1)="+arr[1][1]);

	ydim=arr.length;
	xdim=arr[0].length;
	zdim=3;
	create(3,ydim,xdim);
	for (int y=0;y<ydim;y++){
		for (int x=0;x<xdim; x++){
		dat.set(0,y,x,arr[y][x]*mult);
		dat.set(1,y,x,arr[y][x]*mult); //shameless buggery
		dat.set(2,y,x,arr[y][x]*mult);
		}
	}


}


public void show(GView gv, String title){
	vw=gv.openImageFile(this,title);

}


/**
Jython overloaded functions. Allow calling matrix objects using
 ma=ma*-1 (and similar)
notation.

Also see TraceManager.

**/
public String dbg="";

/*
public Object __call__(PyTuple tup){
	int len=tup.__len__();
	if (len==1){
	 int z=((PyInteger)tup.__getitem__(0)).getValue();
	 if (z>=zdim) throw Py.IndexError("index out of range");
	 return new Matrix(this,0,this.xdim,0,this.ydim,z,z+1,1);
	}else
	if (len==2){
	  int y=((PyInteger)tup.__getitem__(0)).getValue();
	  int x=((PyInteger)tup.__getitem__(1)).getValue();
      int i=y*xdim+x;
      return traces.__getitem__(i);
	}else
	if (len==3){
	  int x=((PyInteger)tup.__getitem__(2)).getValue();
	  int z=((PyInteger)tup.__getitem__(0)).getValue();
	  int y=((PyInteger)tup.__getitem__(1)).getValue();
	 return new Double(dat.get(z,y,x));

	}else
	return null;

}
*/


public Object __getitem__(PyTuple tup){
//	  dbg+="getting matrix tracemanager";
      int len=tup.__len__();
      if (len==2){
	  int y=((PyInteger)tup.__getitem__(0)).getValue();
	  int x=((PyInteger)tup.__getitem__(1)).getValue();
      int i=y*xdim+x;
      return traces.__getitem__(i);
      }else
      if (len==3){
      int x=((PyInteger)tup.__getitem__(2)).getValue();
	  int z=((PyInteger)tup.__getitem__(0)).getValue();
	  int y=((PyInteger)tup.__getitem__(1)).getValue();
	  return new Double(dat.get(z,y,x));
	  }
	  else
	  return null;
}

public void __setitem__(PyTuple tup, TraceManager tr){
//   dbg+="setting matrix tracemanager";
   int y=((PyInteger)tup.__getitem__(0)).getValue();
   int x=((PyInteger)tup.__getitem__(1)).getValue();
   int i=y*xdim+x;
   traces.__setitem__(i,tr);
}


public void __setitem__(PyTuple tup, double[] arr){
//   dbg+="setting matrix tracemanager";
   int y=((PyInteger)tup.__getitem__(0)).getValue();
   int x=((PyInteger)tup.__getitem__(1)).getValue();
   dat.set(y,x,arr);
}


public void __setitem__(PyTuple tup, Double d){
//   dbg+="setting matrix tracemanager";
  int x=((PyInteger)tup.__getitem__(2)).getValue();
  int z=((PyInteger)tup.__getitem__(0)).getValue();
  int y=((PyInteger)tup.__getitem__(1)).getValue();
  dat.set(z,y,x,d.doubleValue());
}



/*
public Matrix __getitem__(int[] range){
  Matrix newma=new Matrix();
  newma.create(range.length,ydim,xdim);
  for (int z=0;z<range.length;z++){
   newma.dat.set(z,dat.section(range[z]));
 }
 return newma;
}
*/


public Matrix __getitem__(PySlice slice){
  return new Matrix(this,0,xdim,0,ydim,((PyInteger)slice.start).getValue(),((PyInteger)slice.stop).getValue(),1);

}


public Matrix __getitem__(int z){
	if (z>=zdim) throw Py.IndexError("index out of range");
	return new Matrix(this,0,this.xdim,0,this.ydim,z,z+1,1);
}


/**
 Replaces values with 1 or 0 depending on relation to threshold
**/
public Matrix __gt__(double threshold){
	for (int i=0;i<dat.arr.length;i++){
		if (dat.arr[i]>threshold) dat.arr[i]=1.0;
		else dat.arr[i]=0.0;
	}
  return this;
}

/**
 Replaces values with 1 or 0 depending on relation to threshold
**/
public Matrix __ge__(double threshold){
	for (int i=0;i<dat.arr.length;i++){
		if (dat.arr[i]>=threshold) dat.arr[i]=1.0;
		else dat.arr[i]=0.0;
	}
  return this;
}


/**
 Replaces values with 1 or 0 depending on relation to threshold
**/
public Matrix __lt__(double threshold){
	for (int i=0;i<dat.arr.length;i++){
		if (dat.arr[i]<threshold) dat.arr[i]=1.0;
		else dat.arr[i]=0.0;
	}
  return this;
}

/**
 Replaces values with 1 or 0 depending on relation to threshold
**/
public Matrix __le__(double threshold){
	for (int i=0;i<dat.arr.length;i++){
		if (dat.arr[i]<=threshold) dat.arr[i]=1.0;
		else dat.arr[i]=0.0;
	}
  return this;
}


public Matrix __sub__(Matrix ma){

	//if ma.zdim==1, subtract each from the original
 if (ma.zdim==1){
    dat.frame_subtract(ma.dat);
    dbg="frame_subtract";
 }
 else
 if (ma.zdim==zdim){
    dat.subtract(ma.dat);
   dbg="subtract all";
 }
 else{
 dat.frame_subtract(ma.getFrameAverage().dat);
 dbg="subtracted average";
}
return this;
}


public Matrix __add__(Matrix ma){

	//if ma.zdim==1, subtract each from the original
 if (ma.zdim==1){
    dat.frame_add(ma.dat);
    dbg="frame_add";

 }
 else
 if (ma.zdim==zdim){
    dat.add(ma.dat);
    dbg="add all";

 }
 else{
 dat.frame_add(ma.getFrameAverage().dat);
 dbg="added average";
 }
return this;
}

public Matrix __add__(double val){
	dat.constant_add(val);
	return this;
}

/*
public Matrix __iadd__(double val){
	System.out.println("IADD called");
	return this;
}
*/

public Matrix __sub__(double val){
	dat.constant_add(-1*val);
	return this;
}


public Matrix __mul__(double val){
	dat.constant_multiply(val);
	return this;
}

public Matrix __mul__(double[][][] vals){
	dat.convolve_all(vals);
	return this;
}

public Matrix __mul__(double[][] vals){
	double[][][] tmp=new double[1][][];
	tmp[0]=vals;
	dat.convolve_all(tmp);
	return this;
}


public Matrix __div__(double val){
	dat.constant_multiply(1.0/val);
	return this;
}





/**
routine that allow undo (1 level). Prior to an undoable operation, a routine
calles update_undo().
**/

public void update_undo(){
		dat_undo=new doubleArray3D(dat);
}


/**
routine that allow undo (1 level). Prior to an undoable operation, a routine
calles update_undo().
**/

public void undo(){
	 if (dat_undo!=null) dat=new doubleArray3D(dat_undo);
	 else System.out.println("NOT UNDOABLE");
}


/** A routine for generating a new Matrix which has smaller dimensions than the old
matrix, by summing data (Binning) in the z,x,y direction.
Usage
 m2=ma.subsample(ma.zdim,16,16) #generates a 16x16 version of a larger (say 80x80) array.
**/

public Matrix subsample(int newzdim, int newydim, int newxdim){

	 Matrix newma=new Matrix(newzdim,newydim,newxdim);
	 int z_shrink=(int)(zdim/newzdim);
	 int y_shrink=(int)(ydim/newydim);
	 int x_shrink=(int)(xdim/newxdim);
	 for (int z=0;z<newzdim;z++){
		 for (int y=0;y<newydim;y++){
			 for (int x=0;x<newxdim;x++){
				 int oldz=z*z_shrink;
				 int oldx=x*x_shrink;
				 int oldy=y*y_shrink;
				 newma.dat.set(z,y,x,dat.sumsection(oldz,oldz+z_shrink,oldy,oldy+y_shrink,oldx,oldx+x_shrink));


			 }
		 }

	 }

   return newma;
 }

/**
 Returns a new 1*X*Y matrix by compressing the original matrix down
 to a single frame, by averaging allong the z axis
**/
public Matrix getFrameAverage(){
 Matrix ma=new Matrix();
 ma.create(1,ydim,xdim);
 for (int x=0;x<xdim;x++){
	 for (int y=0;y<ydim;y++){
	    ma.dat.set(0,y,x,dat.sumsection(y,x)/zdim);

	 }
 }
 return ma;

 }

 /**
 Same as getFrameAverage()
 **/
 public Matrix average(){
	 return getFrameAverage();
 }

/** in place subtraction of two matrices **/

  public void subtract(Matrix m){

	  dat.subtract(m.dat);

  }



/**
   ImageDecoder specific routines.  These allow the matrix to be displayed
   in GView, and load and save raw data (in 'ma' format)
   See ImageDecoder.java.

  potential problem - the program displays things in x,y format. Matrix now holds things in
  y,x format.
 **/
	 public int UpdateImageArray(int[] arr,int xdim,int ydim, int instance){
	  int i=0;
	  int j=0;
	  if (framenum>=zdim) framenum=framenum-1;
	    try{
	    for (i=0;i<xdim;i++){
			for (j=0;j<ydim;j++){
				if ((SHOWSINGULARITIES)&&(sing!=null)){

					if (COMPOSTSINGULARITIES)
						arr[j*xdim+i]=(int)(sing.get(framenum,j,i)+dat.get(framenum,j,i));
					else
						arr[j*xdim+i]=(int)sing.get(framenum,j,i);

				}else
				arr[j*xdim+i]=(int)dat.get(framenum,j,i);
			}
		}
		}catch(Exception e){System.out.println("i="+i+" j = "+j+" xdim="+xdim+" ydim="+ydim+" arr.length="+arr.length);e.printStackTrace();}
	   framenum++;
	   return 1;
	 }

	 public int CloseImageFile(int instance){return 0;}
	 public int FilterOperation(int OperationCode, int startx, int endx, int instance){return 0;}
	 public String ReturnSupportedFilters(){return "";}
	 public int ReturnFrameNumber(){return 0;}
	 public int JumpToFrame(int frame, int instance){framenum=frame; return framenum;}
     public int ReturnXYBandsFrames(int[] dat, int instance){
		 dat[0]=xdim;
		 dat[1]=ydim;
		 dat[2]=1;
		 dat[3]=zdim;
	 	 return 1;
	 }

    /** Open and Save image file via OpenImageFile(filename) and SaveImageFile(filename)
    write data in 'gv' format - raw data as doubles with three ints leading as a header.
    **/
    public int OpenImageFile(String filename){
		try{
			 RandomAccessFile infile=new RandomAccessFile(filename,"r");
			 int version=infile.readInt(); //version
			 zdim=infile.readInt();
			 ydim=infile.readInt();
			 xdim=infile.readInt();
			 create(zdim,ydim,xdim);
			 FileChannel fc = infile.getChannel();
			 MappedByteBuffer buffer = fc.map (FileChannel.MapMode.READ_ONLY, 0, fc.size());
			 if (version==0){
			 DoubleBuffer db=buffer.asDoubleBuffer();
			 double[] tmp=new double[xdim*ydim];

			 for (int z=0;z<zdim;z++){
				 db.get(tmp,0,xdim*ydim);
				 for (int y=0;y<ydim;y++){
					 for (int x=0;x<xdim;x++){
						 dat.set(z,y,x,tmp[y*xdim+x]);
					 }
				 }
			 }
		    }
		    if (version==1){
			 ShortBuffer db=buffer.asShortBuffer();
			 short[] tmp=new short[xdim*ydim];

			 			 for (int z=0;z<zdim;z++){
			 				 db.get(tmp,0,xdim*ydim);
			 				 for (int y=0;y<ydim;y++){
			 					 for (int x=0;x<xdim;x++){
			 						 dat.set(z,y,x,(double)tmp[y*xdim+x]);
			 					 }
			 				 }
			 }


			}

			infile.close();

			}catch(IOException e){e.printStackTrace();}
		return 0;
		}






    /** Open and Save image file via OpenImageFile(filename) and SaveImageFile(filename)
    write data in 'gv' format - raw data as doubles with three ints leading as a header.
    **/
    public void SaveImageFile(String filename){
	    SaveImageFile(filename,0,zdim);
	}

    public void SaveImageFile(String filename, int start, int end){
	 try{
	 RandomAccessFile outfile=new RandomAccessFile(filename,"rw");

	 double[] tmp=new double[xdim*ydim];
	 byte[] bytes=new byte[xdim*ydim*8];

	 FileChannel fc=outfile.getChannel();
	 MappedByteBuffer buffer = fc.map (FileChannel.MapMode.READ_WRITE, 0, zdim*xdim*ydim*8+4*4);
     buffer.putInt(0); //version
	 buffer.putInt(zdim);
	 buffer.putInt(ydim);
	 buffer.putInt(xdim);
	  for (int z=start;z<end;z++){
		 for (int y=0;y<ydim;y++){
			 for (int x=0;x<xdim;x++){
				 //outfile.writeDouble(dat.get(z,x,y));
				 buffer.putDouble(dat.get(z,y,x));
	//		 	 for (int k=0;k<8;k++) bytes[(8*(x*ydim+y))+k]=a[k];
			 }
		 }
	 }
	outfile.close();
	}catch(IOException e){e.printStackTrace();}
}








/**
 Rotates the data around pt x,y for the given number of degrees.
 Not great.
**/
   public void rotate(double deg, double x0, double y0){
	   update_undo();
       doubleArray3D dat_rotate=new doubleArray3D(dat.zdim,dat.ydim,dat.xdim);
       for (int y1=0;y1<ydim;y1++){
		   for (int x1=0;x1<xdim;x1++){
			   double x2=Math.cos(deg)*(x1-x0)-Math.sin(deg)*(y1-y0)+x0;// (x1-x0+x0)
			   double y2=(Math.sin(deg)*(x1-x0)-Math.cos(deg)*(y1-y0)+y0);// -y1

			   //double x2=Math.cos(deg)*(x1-x0)-Math.sin(deg)*(y1-y0)+x0;// (x1-x0+x0)
			   //double y2=Math.sin(deg)*(x1-x0)-Math.cos(deg)*(y1-y0)+y0;// -y1
			   //find nearest pixel


			   dat_rotate.set(y1,x1,dat.get((int)y2,(int)x2));
		   }
	   }
		dat.assign(dat_rotate);
     }
/**
Masks are boolean arrays used to block out parts of a Matrix (due to excessive noise, edge
of camera effects, etc
SaveMask saves a given mask
**/

public void SaveMask(String filename){
		try{
	 		RandomAccessFile outfile=new RandomAccessFile(filename,"rw");
	 		for (int y=0;y<ydim;y++){
				for (int x=0;x<xdim;x++){
				outfile.writeBoolean(mask[y][x]);
				}
			}
			outfile.close();
		}catch(IOException e){e.printStackTrace();}
	}

/**
Masks are boolean arrays used to block out parts of a Matrix (due to excessive noise, edge
of camera effects, etc
OpenMask reads a given mask from a file
**/

public void OpenMask(String filename){
		 mask=new boolean[ydim][xdim];
		try{
	 		RandomAccessFile infile=new RandomAccessFile(filename,"r");
	 		for (int y=0;y<ydim;y++){
				for (int x=0;x<xdim;x++){
				mask[y][x]=infile.readBoolean();
				}
			}
			infile.close();
		}catch(IOException e){e.printStackTrace();}

	}

/**
Masks are boolean arrays used to block out parts of a Matrix (due to excessive noise, edge
of camera effects, etc
Prints a mask to stdout.

**/
public void PrintMask(){
		System.out.println("");
		for (int y=0;y<ydim;y++){
						for (int x=0;x<xdim;x++){
						if (mask[y][x])  System.out.print("1");
						else System.out.print("0");
						}
					System.out.println("");
			}
	}

/**
Masks are boolean arrays used to block out parts of a Matrix (due to excessive noise, edge
of camera effects, etc
Applys the mask to a Matrix by setting all points without a true bit to 0.
**/

public void ApplyMask(){
	 for (int z=0;z<zdim;z++){
		for (int y=0;y<ydim;y++){
			for (int x=0;x<xdim;x++){
				if (!mask[y][x]) dat.set(z,y,x,0);
	 	 }
		}
	}
	}






/*************************************************************************************************

In place processing routines. These routines are not as widely used now that Matrix class is
overloaded for direct Jython access, but they are still usefull.
See: processByTrace below.

**********************************************************************************************/



/**
Perform a three point median in x,y space.
**/

public void processInSpace(){
update_undo();
double[] tmp=null;

	for (int z=0;z<zdim;z++){
		ProgressDisplayer.getProgressDisplayer().displayProgress(((double)z)/((double)zdim));

		//extract the frame as a 2darray, pad it
		for (int x=0;x<xdim;x++){
			for (int y=0;y<ydim;y++){
				//extract the subarray
				int index=0;
				tmp=new double[9];
				  //get an array of 9 nearest neighbours
				  for (int i=-1;i<=1;i++){
					  for (int j=-1;j<=1;j++){
					   if (((x+i)>=0)&&((x+i)<xdim)&&((y+j)>=0)&&((y+j)<ydim)){
						   double val=dat.get(z,y+j,x+i);
						   tmp[index]=val;
						   //System.out.print(tmp[index]+" ");
						   index++;
						   //if (index>9) System.out.println("index="+index+" (i,j)="+i+" "+j+" (z,x,y)="+z+" "+x+" "+y);
					   		}
					  }
				  }
				  if (index==9){
				  //tmp is now populated to index
				  //tmp=JSci.maths.ArrayMath.extract(0,index,tmp);
				  frame.set(y,x,JSci.maths.ArrayMath.median(tmp));
			     } else frame.set(y,x,dat.get(z,y,x));
			}
		}
		for (int x=0;x<xdim;x++){
			for (int y=0;y<ydim;y++){
					dat.set(z,y,x,frame.get(y,x));
				}
			}

	}
	ProgressDisplayer.getProgressDisplayer().displayProgress(1.0);
}


/**
 Suppresses (sets to 0) any thrace that has a certain percentage of values under threshold
**/
public void suppressByTrace(double threshold, double percent){
	update_undo();
	for (int i=0;i<xdim;i++){
			for (int j=0;j<ydim;j++){
			double over=0;
			double under=0;
			double[] tmp=dat.section(j,i);
			for (int x=0;x<tmp.length;x++){
				if (tmp[x]>threshold) over++; else under++;
			}
			if( (100*(over/under))<percent)
			   for (int x=0;x<tmp.length;x++) tmp[x]=0;
			for (int z=0;z<zdim;z++)dat.set(z,j,i,tmp[z]);

		}
	}
}



/**
 Sets all values in the given trace (x,y section) to 0.
**/
public void suppress(int y ,int x){
	for (int z=0;z<zdim;z++) dat.set(z,y,x,0);
}

/**
 Suppresses an array of indices (see suppress)
**/
public void suppress(int[][] arr){
	for (int n=0;n<arr.length;n++){
		suppress(arr[n][0],arr[n][1]);
	}
}


/**
ProcessByTrace(...) calls ProcessByTrace(String type, int arg1, double arg2, int[] roi, function f)
**/
public void processByTrace(String type){
	processByTrace(type,0,0.0,null,null);
}

/**
ProcessByTrace(...) calls ProcessByTrace(String type, int arg1, double arg2, int[] roi, function f)
**/
public void processByTrace(String type, int arg){
	processByTrace(type,arg,0.0,null,null);
}

/**
ProcessByTrace(...) calls ProcessByTrace(String type, int arg1, double arg2, int[] roi, function f)
**/
public void processByTrace(String type, int arg, double arg2){
	processByTrace(type,arg,arg2,null,null);
}

/**
ProcessByTrace(...) calls ProcessByTrace(String type, int arg1, double arg2, int[] roi, function f)
**/
public void processByTrace(String type,int[]roi){
	processByTrace(type,0,0.0,roi,null);
}

/**
ProcessByTrace(...) calls ProcessByTrace(String type, int arg1, double arg2, int[] roi, function f)
**/
public void processByTrace(String type,int arg, int[]roi){
	processByTrace(type,arg,0.0,roi,null);
}

/**
ProcessByTrace(...) calls ProcessByTrace(String type, int arg1, double arg2, int[] roi, function f)
**/
public void processByTrace(String type, int[] roi, function f){
    processByTrace(type,0,0.0,roi,f);
}

/**
ProcessByTrace(...) calls ProcessByTrace(String type, int arg1, double arg2, int[] roi, function f)
**/
public void processByTrace(String type, function f){
	processByTrace(type,0,0.0,null,f);

}

/**
In place processing of a z section of data.
Usage: (in most cases a short form works ie "n" instead of "normalize".)
 ma.processByTrace("normalize") normalizes all values (per each trace) between 0 and 1000
 ma.processByTrace("average", 5) running average over 5 values (any odd value works)
 ma.processByTrace("median",7) 7 pt running median (odd values only)
 ma.processByTrace("subaverage",101) subtract a 101 running average of the trace from each value in the trace
 ma.processByTrace("dvdt") return the first derivative of the trace
 ma.processByTrace("threshold",1,1000) keep  values greater than 1000, else set to 0, (use -1 for setting keeping values less than 1000).
 ma.processByTrace("function", function) runs a 'function.class' object on the trace
**/


public void processByTrace(String type, int arg1, double arg2, int[] roi, function f){
	//vw.CANCEL_LOOP=false;
	double[] tmp;
	int op=0;
	update_undo();
	if (type.equalsIgnoreCase("normalize")) 	op=NORMALIZE;
	else if (type.equalsIgnoreCase("n"))		op=NORMALIZE;
	else if (type.equalsIgnoreCase("average")) 	op=AVERAGE;
	else if (type.equalsIgnoreCase("a")) 		op=AVERAGE;
	else if (type.equalsIgnoreCase("median")) 	op=MEDIAN;
	else if (type.equalsIgnoreCase("m")) 		op=MEDIAN;
	else if (type.equalsIgnoreCase("subaverage"))op=SUBAVERAGE;
	else if (type.equalsIgnoreCase("s")) 		op=SUBAVERAGE;
	else if (type.equalsIgnoreCase("threshold"))op=THRESHOLD;
	else if (type.equalsIgnoreCase("t")) 		op=THRESHOLD;
	else if (type.equalsIgnoreCase("supress"))  op=SUPRESS;
	else if (type.equalsIgnoreCase("sup")) 		op=SUPRESS;
	else if (type.equalsIgnoreCase("phase"))  	op=PHASE;
	else if (type.equalsIgnoreCase("p")) 		op=PHASE;
	else if (type.equalsIgnoreCase("activation"))op=ACTIVATION;
	else if (type.equalsIgnoreCase("act")) 		op=ACTIVATION;
	else if (type.equalsIgnoreCase("phasesingularity"))op=PHASESINGULARITY;
	else if (type.equalsIgnoreCase("ps")) 		op=PHASESINGULARITY;
	else if (type.equalsIgnoreCase("activationsingularity"))op=ACTIVATIONSINGULARITY;
	else if (type.equalsIgnoreCase("ps")) 		op=ACTIVATIONSINGULARITY;
	else if (type.equalsIgnoreCase("dvdt")) 	op=DVDT;
    else if (type.equalsIgnoreCase("function")) op=FUNCTION;
    else if (type.equalsIgnoreCase("f")) 		op=FUNCTION;
    else if (type.equalsIgnoreCase("r"))        op=RANDOM;
    else if (type.equalsIgnoreCase("random"))   op=RANDOM;
	else System.out.println("Warning, the command wasn't found in processByTrace()");

    int [] arr=null;
    if (roi==null){
	   arr=new int[xdim*ydim];

	   for (int i=0;i<xdim*ydim;i++) arr[i]=i;
	   //perform op on whole field
	} else{
	 arr=roi;
    }

 for (int kk=0;kk<arr.length;kk++){
	if (ProgressDisplayer.getProgressDisplayer().displayProgress(((double)kk)/((double)arr.length))) {
		System.out.println("CANCELLING in Matrix");
		break;
	}
	/*
	if (vw.CANCEL_LOOP){
		kk=arr.length;
		vw.CANCEL_LOOP=false;
		ProgressDisplayer.getProgressDisplayer().displayProgress(1.0);
	}
   */
	 //unpack the number
	int  j=(int)((double)arr[kk]/xdim);
	int  i=(int)((double)arr[kk]%xdim);
			tmp=dat.section(j,i); //y,x
			switch (op){
			  case FUNCTION:
			  		    for (int k=0;k<tmp.length;k++){
						  tmp[k]=f.compute(k,tmp[k]);
						}
					break;
			  case NORMALIZE:
			 		    double max=JSci.maths.ArrayMath.max(tmp);
			  			double min=JSci.maths.ArrayMath.min(tmp);
			  			double range=max-min;
			  			if (range==0) range=1;
			  			for (int k=0;k<tmp.length;k++){
			  				tmp[k]=1000*(tmp[k]-min)/(range);
							}
			  		break;
			  case AVERAGE:
			  		tmp=JSci.maths.EngineerMath.runningAverage(tmp,arg1);
			  		break;
			  case MEDIAN:
					tmp=JSci.maths.EngineerMath.runningMedian(tmp,arg1);
					break;
			  case SUBAVERAGE:
			  		double[] sub=JSci.maths.EngineerMath.runningAverage(tmp,arg1);
			  		tmp=JSci.maths.ArrayMath.subtract(tmp,sub);
			  		break;
			  case THRESHOLD:
			  		for (int k=0;k<tmp.length;k++){
						if (arg1>=0){
						if (tmp[k]>=arg2) tmp[k]=1000.0;
						else tmp[k]=0;
						}
						if (arg1<0){
						if (tmp[k]<=arg2) tmp[k]=1000.0;
						else tmp[k]=0;
						}
					}
					break;
			  case SUPRESS:
			  		for (int k=0;k<tmp.length;k++){
			  		  if (tmp[k]<=arg2) tmp[k]=0.0;
			  			else tmp[k]=tmp[k];
			  			}
					break;
			  case PHASE:
			        //first, run through trace, set upwardcrossing at 500 to 1
			        for (int k=0;k<tmp.length-10;k++){
						if ((tmp[k]<=arg2)&&(tmp[k+1]>arg2+50)) tmp[k]=1000; else tmp[k]=0;
						}
					//next, find 1k, then next 1k, replace 0's with value scaled between 1 and 1000
                    for (int jj=0;jj<tmp.length;jj++){
						if (jj>0){ //this works, because jj incremented elswhere, although its nonsense
							int t1=jj;
							if (t1+arg1<tmp.length-1) jj=t1+arg1;//refractory period
							do{
							  if (jj<tmp.length-1) jj++;
						    }while((tmp[jj]==0)&&(jj<tmp.length-2));
						    int t2=jj;
						    double total=t2-t1;
						    for (int k=t1;k<t2;k++){
							 tmp[k]=(k-t1)*(1000/total);
							}
						   }
						}

					break;
			 case ACTIVATION:
						//first, run through trace, set upwardcrossing at 500 to 1
					for (int k=0;k<tmp.length-1;k++){
						if ((tmp[k]<=arg2)&&(tmp[k+1]>arg2)) tmp[k]=1000; else tmp[k]=0;
						}
					//next, find 1k, replace subsequent arg1 elements with 500
					for (int jj=0;jj<tmp.length;jj++){
					 if(tmp[jj]==1000){
						for (int k=jj+1;k<jj+arg1;k++){
						if (k<tmp.length) tmp[k]=500;
						}
					  }
					 }
					break;
			  case PHASESINGULARITY:



			  		break;
			  case ACTIVATIONSINGULARITY:

			  		break;
			  case DVDT:
			  		double[] dvdt=new double[tmp.length];
			  		for (int k=0;k<tmp.length-1;k++){
			  			dvdt[k]=tmp[k+1]-tmp[k];
					}
					tmp=dvdt;
			  		break;
			  case RANDOM:
			  	   for (int k=0;k<tmp.length;k++){
					   tmp[k]+=java.lang.Math.random()*arg1;
				   }
			  default:
			  		new IllegalArgumentException();
			} //end switch
			for (int z=0;z<zdim;z++)dat.set(z,j,i,tmp[z]);
			tmp=null;
		}
	    ProgressDisplayer.getProgressDisplayer().displayProgress(1.0);

			//System.gc();
		//}//jj
         //System.gc();
	  //}//ii


	}



public void threePointMedianByTrace(){
double[] tmp=new double[zdim];
double a,b,c;

	for (int i=0;i<ydim;i++){
 			for (int j=0;j<xdim;j++){
 				for (int z=1;z<zdim-1;z++){
 					a=dat.get(z-1,i,j);
 					b=dat.get(z,i,j);
 					c=dat.get(z+1,i,j);
 					if ((b>=a) && (c>b)) tmp[z]=b; else
 					if ((a>=b) && (a<c)) tmp[z]=a; else
 					tmp[z]=c;
				}
			for (int u=1;u<zdim-1;u++){
			 dat.set(u,i,j,tmp[u]);
			}
}
}

}

public void fivePointAverageByTrace(){
double[] tmp=new double[zdim];
double a,b,c,d,e;

	for (int i=0;i<ydim;i++){
 			for (int j=0;j<xdim;j++){
 				for (int z=2;z<zdim-2;z++){
 					a=dat.get(z-2,i,j);
 					b=dat.get(z-1,i,j);
 					c=dat.get(z,i,j);
 					d=dat.get(z+1,i,j);
 					e=dat.get(z+2,i,j);


 					tmp[z]=(a+b+c+d+e)/5;
				}
			for (int u=2;u<zdim-2;u++){
			 dat.set(u,i,j,tmp[u]);
			}
}
}

}


public void BaseLineSubtract(int median, int average){
	 double[] aveintensity=new double[zdim];
	 double totalintensity=0;
	    for (int z=0;z<zdim;z++){
			double intensity=0;
			for (int x=0;x<xdim;x++){
				for (int y=0;y<ydim;y++){
				intensity+=dat.get(z,y,x);
				}
			}
			aveintensity[z]=intensity/(xdim*ydim);
			totalintensity+=aveintensity[z];
		}

	aveintensity=JSci.maths.EngineerMath.runningMedian(aveintensity,median);
	aveintensity=JSci.maths.EngineerMath.runningAverage(aveintensity,average);
      for (int z=0;z<zdim;z++){
				double intensity=0;
				for (int x=0;x<xdim;x++){
					for (int y=0;y<ydim;y++){
					dat.set(z,y,x,dat.get(z,y,x)-aveintensity[z]);
					}
				}

		}
  aveintensity=null;
}


/**
depreciated
**/
   double[][] tmparr;
   public void fillmatrix(){
	   if (tmparr==null) tmparr=new double[ydim][xdim];


	   for (int z=0;z<zdim;z++){
		   for (int q=0;q<xdim;q++){
		   				for (int p=0;p<ydim;p++){
		   				tmparr[p][q]=0;
		   				}
			}
		   for (int x=0;x<xdim;x++){
			   for (int y=0;y<ydim;y++){
				if (dat.get(z,y,x)==0){
					double sum=0;
					int numnbs=0;
					for (int i=-1;i<=1;i++){
						for (int j=-1;j<=1;j++){
							int xloc=x+i;
							int yloc=y+j;
							if ((xloc>0)&&(yloc>0)&&(xloc<xdim)&&(yloc<ydim)){
								double val=dat.get(z,yloc,xloc);
								if (val!=0){
								   sum+=dat.get(z,yloc,xloc);
								   numnbs+=1;
							     }

						}
					}
				}

			   if (numnbs!=0) tmparr[y][x]=sum/numnbs;
		      }
		  }
	   }
	   for (int q=0;q<xdim;q++){
		   for (int p=0;p<ydim;p++){
			if (tmparr[p][q]!=0)
			  dat.set(z,p,q, tmparr[p][q]);
		  }
	  }

	  }
 }






/**
depreciated, specific to dual vm ca MOVE
**/
public void correlation(){
		double average=0;
		if (corr==null) corr=new double[offset][xdim];
		for (int x=0;x<xdim;x++){
			for (int y=0;y<offset;y++){

					double[] tmpV=dat.section(y,x);
					double[] tmpC=dat.section(y+offset,x);
					corr[y][x]=JSci.maths.ArrayMath.correlation(tmpV,tmpC);
					average+=corr[y][x];
				}
			}
		System.out.println("average correlation = "+average/(xdim*offset));
	}

public int poweroftwo(double [] dat){
		int len=dat.length;
		int setlengthto=0;
		boolean notfound=true;
		int first=2;
		int second=4;
		do{

		  if ((len>=first)&&(len<second)){
			setlengthto=first;
			notfound=false;
		  }
		  first=second;
		  second=second*2;
		}while(notfound);
		//System.out.println("Setting length from "+dat.length+" to "+setlengthto);
		return setlengthto;

}


public double[] fft(double[] dbl){
		//recast

		    double[] tmp=JSci.maths.ArrayMath.setLengthFromBeginning(dbl,poweroftwo(dbl));
			Complex[] cmp=JSci.maths.FourierMath.transform(tmp);
			Complex[] cnj=new Complex[cmp.length];
			double [] res=new double[cmp.length];
			for (int i=0;i<cnj.length;i++){
				cnj[i]=cmp[i].conjugate();
				cmp[i]=cmp[i].multiply(cnj[i]);
				res[i]=(double)cmp[i].real();
			}
		    return res;


	}

/**specific to dual voltage calcium array measurements MOVE**/
public void  fftcorrelation(){
			double average=0;
			if (corr==null) corr=new double[offset][xdim];
			for (int x=0;x<xdim;x++){
				for (int y=0;y<offset;y++){

						double[] tmpV=dat.section(y,x);
						double[] tmpC=dat.section(y+offset,x);
						double[] fftV=fft(tmpV);
						double[] fftC=fft(tmpC);
						fftV=JSci.maths.ArrayMath.extract(1,80,fftV);
						fftC=JSci.maths.ArrayMath.extract(1,80,fftC);
						corr[y][x]=JSci.maths.ArrayMath.correlation(fftV,fftC);
						average+=corr[y][x];
					}
				}
			System.out.println("average fft correlation = "+average/(xdim*offset));
}

public void PrintCorrelation(){
		System.out.println("");
		if (corr!=null){
		for (int y=0;y<offset;y++){
						for (int x=0;x<xdim;x++){
							System.out.print((float)corr[y][x]+"\t");
		}
		System.out.println("");
	}
	}
	}

	public void SaveCorrelation(String filename){
		try{
		PrintWriter file=new PrintWriter(new FileWriter(filename),true);
			System.out.println("");
			if (corr!=null){
			for (int y=0;y<offset;y++){
							for (int x=0;x<xdim;x++){
								file.print((float)corr[y][x]+"\t");
			}
			file.println("");
		}
		}
		file.close();
		}catch(Exception e){e.printStackTrace();}
		}



	public void SetCorrelationMask(double threshold){
		mask=new boolean[ydim][xdim];
		for (int y=0;y<offset;y++){
						for (int x=0;x<xdim;x++){
						mask[y][x]=mask[y+offset][x]=(corr[y][x]>threshold);
						}
					}

	}

public static int nextpowerof2(int val){
	for (int i=0;i<18;i++){
		if (Math.pow(2,i)==val) return (int)Math.pow(2,i);
		if ((Math.pow(2,i)<val) &&(Math.pow(2,i+1)>val)) return (int)Math.pow(2,i+1);
	}
  return -1;
}

//this only makes sense on a matrix with dimension z=1
public void resizeToPowerOf2(){
 if (zdim>1) {
	 System.out.println("resizeToPowerOf2() only works on a z=1 Matrix");
	 return;
 }
 int oldxdim=xdim;
 int oldydim=ydim;
 int newxdim=nextpowerof2(xdim);
 int newydim=nextpowerof2(ydim);

 doubleArray3D newarr=new doubleArray3D(1,newydim,newxdim);
 //set the values in the new array

 int xoffset=(newxdim-oldxdim)/2;
 int yoffset=(newydim-oldydim)/2;

 for (int y=0;y<ydim;y++){
	 for (int x=0;x<xdim;x++){
		 newarr.set(y+yoffset,x+xoffset,dat.get(y,x));
	 }
 }


 dat=newarr;

 xdim=newxdim;
 ydim=newydim;



}


/*
public void reset(int start, int end){

		int[] tmp=new int[4];
		id.ReturnXYBandsFrames(tmp);
		int[] tmpframe=new int[tmp[0]*tmp[1]];
		create((end-start),tmp[0],tmp[1]);
		for (int i=start;i<end;i++){
		 id.JumpToFrame(i,v2.instance);
		 id.UpdateImageArray(tmpframe,tmp[0],tmp[1],v2.instance);
		 set((i-start),tmpframe);
	     }
	   }

*/


public void textreader(String filename){
	 try {
	        // Create the tokenizer to read from a file
	        FileReader rd = new FileReader(filename);
	        StreamTokenizer st = new StreamTokenizer(rd);

	        // Prepare the tokenizer for Java-style tokenizing rules
	        st.parseNumbers();
	        st.wordChars('_', '_');
	        st.eolIsSignificant(true);

	        // If whitespace is not to be discarded, make this call
	        st.ordinaryChars(0, ' ');

	        // These calls caused comments to be discarded
	        st.slashSlashComments(true);
	        st.slashStarComments(true);

	        // Parse the file
	        int token;
	        token = st.nextToken();
	        double zd=st.nval;
	        token = st.nextToken();
	        double yd=st.nval;
	        token= st.nextToken();
	        double xd= st.nval;
	        token=st.nextToken();
	        System.out.println("z="+zd+" yd="+yd+" xd="+xd);
	        rd.close();
	    } catch (IOException e) {
    }



}

public String GnuplotPathString="C:\\downloads\\gnuplot\\bin\\wgnuplot.exe c:\\CD\\programs\\java\\decoder\\gnucommand.gnu";

public void createContourMap(int start, int end, int timestep){
	int numsteps=(int)(((double)(end-start))/(double)timestep);
	cnt=new intArray3D(xdim,ydim);

	//assumes that we have a threshold function already
	for (int i=start; i<end;i+=timestep){
	 for (int y=0;y<ydim;y++){
		 for (int x=0;x<xdim;x++){
			 if ((dat.get(i,y,x)>500)&&(cnt.get(y,x)==0))cnt.set(y,x,i-start);
			 }
		 }
		}
    System.out.println("done creating contour map data");
		try{
		   		 PrintWriter file=new PrintWriter(new FileWriter("tmpgnudat.dat"),true);
				 for (int y=0;y<ydim;y++){
					for (int x=0;x<xdim;x++){
					file.println(y+" "+x+" "+cnt.get(y,x));
					}
					file.println("");
				}
					file.close();
				}
				catch(Exception e){e.printStackTrace();}
				Runtime r=Runtime.getRuntime();

				try{
				Process p = r.exec(GnuplotPathString);
		}catch(Exception e){e.printStackTrace();}


}

public void createCorrelationContourMap(){
	try{
			   		 PrintWriter file=new PrintWriter(new FileWriter("tmpgnudat.dat"),true);
					 for (int y=0;y<offset;y++){
						for (int x=0;x<xdim;x++){
						file.println(y+" "+x+" "+corr[y][x]);
						}
						file.println("");
					}
						file.close();
					}
				catch(Exception e){e.printStackTrace();}


	try{
		Runtime r=Runtime.getRuntime();
		Process p = r.exec("C:\\downloads\\bub\\gnuplot2\\wgnuplot.exe c:\\programs\\java\\decoder\\gnuccommand.gnu");
		}catch(Exception e){e.printStackTrace();}
}



public void FindSingularityCandidates(){
 sing=new doubleArray3D(zdim,ydim,xdim);
 for (int z=0;z<zdim;z++){
	 int[][] s=phaseSingularity(z);
	 for (int i=0;i<s.length;i++){
		sing.set(z,s[i][1],s[i][0],1000);
		}
   }
}


public void FilterSingularitiesInTime(){
//score higher if they are adjacent to others in time
if (sing!=null){
	for (int z=0;z<zdim-1;z++){
		for (int y=0;y<ydim;y++){
			for (int x=0;x<xdim;x++){

				if (sing.get(z,y,x)>0){
				  if ((sing.get(z+1,y,x)>0)||
					 (sing.get(z+1,y+1,x)>0) ||
					 (sing.get(z+1,y,x+1)>0) ||
					 (sing.get(z+1,y-1,x)>0) ||
					 (sing.get(z+1,y,x-1)>0) ||
					 (sing.get(z+1,y+1,x+1)>0)||
					 (sing.get(z+1,y-1,x-1)>0)||
					 (sing.get(z+1,y-1,x+1)>0)||
					 (sing.get(z+1,y+1,x-1)>0)) {
					   sing.set(z,y,x,1001);
					  }
				 }

			}
		}

	}
  }
  else System.out.println("WARNING, no singularity canditates detected yet");
}


public void FilterSingularitiesInSpace(){

if (sing!=null){

	for (int z=0;z<zdim;z++){
		for (int y=1;y<ydim-1;y++){
			for (int x=1;x<xdim-1;x++){
				if (sing.get(z,y,x)<1001) sing.set(z,y,x,0);

					if ((sing.get(z,y+1,x)>1000) ||
					 (sing.get(z,y,x+1)>1000) ||
					 (sing.get(z,y-1,x)>1000) ||
					 (sing.get(z,y,x-1)>1000) ||
					 (sing.get(z,y+1,x+1)>1000)||
					 (sing.get(z,y-1,x-1)>1000)||
					 (sing.get(z,y-1,x+1)>1000)||
					 (sing.get(z,y+1,x-1)>1000)) {
					   sing.set(z,y,x,0);
					 }

				 }
			 }
		 }
	 } else System.out.println("WARNING, no singularity canditates detected yet");
}






public void V_Ca_Phasemap(){
/*the idea is that a time difference exists between v, ca
   at each point in time v should lead ca (10 ms difference)
   we plot c as a function of v
    vh cl

*/
update_undo();
for (int z=0;z<zdim;z++){
	for (int x=0;x<xdim;x++){
		for (int y=0;y<(ydim/2);y++){
		 double v= dat.get(z,y,x);
		 double c= dat.get(z,y+offset,x);
	     double p=1;
	     p=Math.atan2(c,v);


		 dat.set(z,y,x,p*1000);
		}
		}
		}
}








public int[][] phaseSingularity(int framenum){
//in a frame, move through the x/y plane
int numsingularity=0;
int[][] res=new int[xdim*ydim][2];
for (int x=1;x<xdim-1;x++){
	for (int y=1;y<ydim-1;y++){
		boolean pi_025=false;
		boolean pi_050=false;
		boolean pi_075=false;
		boolean pi_100=false;
		for (int i=-1;i<=1;i++){
			for (int j=-1;j<=1;j++){
				int ix=x+i;
				int iy=y+j;
				double val=dat.get(framenum,ix,iy);
				if (val<250) pi_025=true;
				if ((val>=250)&&(val<500)) pi_050=true;
				if ((val>=500)&&(val<750)) pi_075=true;
				if (val>=750) pi_100=true;
				}
			}
		if (pi_025 && pi_050 && pi_075 && pi_100){
			res[numsingularity][0]=x;
			res[numsingularity][1]=y;
			numsingularity++;
			System.out.println("found singularity at "+x+","+y);
			}

	}
  }
  //reshape res
  int[][] ret=new int[numsingularity][2];
  for (int i=0;i<numsingularity;i++){
	  ret[i][0]=res[i][0];
	  ret[i][1]=res[i][1];
  }
  return ret;
}


public Matrix resampleInSpace(int multiplier){
	/*
			1 1 1 1 1 1 1 1
			 m m m m m m m
			1 1 1 1 1 1 1 1

	 */
 return null;
}



/**
fillblob, is called by BlobCount, and finds a blob and fills it with its blobnumber in
the z,y,x plane, advancing in the z direction (blobs diverge but don't merge)

whats the goal? t=   bl
how do i detect a split? rerun, if 7 has 3 distinct areas then we know that 7 split 3
times

whats the goal? distinguish between waves generated by wavebreaks and those generated
by spontaneous oscillation

at each z, count blobs. if blob
data set 1
z  blobs distinct
1  1     1
2  3     1
3  4     2 //1 spontaneously arrises
4  3     2 //merge
5  1     1
6  0     0

blob size (z,x,y) frequency (p(blob))

**/


public int[] fillblob(int zstart, int y, int x, double blobnum){
	//
	dat.set(zstart,y,x,blobnum);
	int blobsize=1;
	int z=zstart;
    boolean uplevel=false;
    int[] blobinfo=new int[6];
//	System.out.println("debug:: in fillblob with blobnum="+blobnum);
    boolean firstlevel=true;
	do{
	//fill all in this slice
    boolean addedone=false;
	uplevel=false;

//	System.out.println("z="+z);
	do{

	  //System.out.println("debug:: in fillblob doloop1 with blobsize="+blobsize+" blobnum="+blobnum);
	  addedone=false;
	  for (y=0;y<ydim;y++){
		  for (x=0;x<xdim;x++){
			  if (
				  (dat.get(z,y,x)==0)&&
			      (
				   ((y+1<ydim)&&(dat.get(z,y+1,x)==blobnum))||
			       ((y-1>=0)&&(dat.get(z,y-1,x)==blobnum))||
			       ((x+1<xdim)&&(dat.get(z,y,x+1)==blobnum))||
			       ((x-1>=0)&&(dat.get(z,y,x-1)==blobnum))
			      )
			     ){
				  dat.set(z,y,x,blobnum);
				  blobsize++;
				  addedone=true;
				  }
     	}//x
      }//y
      if (firstlevel){
		  System.out.println("blob size at level 1 is ="+blobsize);
		  blobinfo[0]=blobsize;
		  firstlevel=false;
	  }
	}while(addedone);
	//now propagate upward from z to z+1

	for (y=0;y<ydim;y++){
			  for (x=0;x<xdim;x++){
				  if (
					  (dat.get(z,y,x)==blobnum)&&
					    (
										   ((y+1<ydim)&&(dat.get(z,y+1,x)!=blobnum))||
									       ((y-1>=0)&&(dat.get(z,y-1,x)!=blobnum))||
									       ((x+1<xdim)&&(dat.get(z,y,x+1)!=blobnum))||
									       ((x-1>=0)&&(dat.get(z,y,x-1)!=blobnum))
				        )
				       )
				   {

					dat.set(z,y,x,0);

				   }

			 }
	 }


	  for (y=0;y<ydim;y++){
		  for (x=0;x<xdim;x++){
			  if (
				  (dat.get(z+1,y,x)==0)&&
				  (dat.get(z,y,x)==blobnum)
				 ){
					 dat.set(z+1,y,x,blobnum);
					 blobsize++;
					 uplevel=true;
				}
	     	}//x
	      }//y
         z++;
    }while(uplevel);
    System.out.print("blob starts="+zstart+" duration="+(z-zstart));
    blobinfo[1]=blobsize;
    blobinfo[2]=zstart;
    blobinfo[3]=z-zstart;
    blobinfo[4]=x;
    blobinfo[5]=y;
    return blobinfo;
  }


public int[] fillblob2(int zstart, int y, int x, int rxy, int rz, double blobnum){
	//
	dat.set(zstart,y,x,blobnum);
	int blobsize=1;
	int z=zstart;
    boolean uplevel=false;
    int[] blobinfo=new int[6];
//	System.out.println("debug:: in fillblob with blobnum="+blobnum);
    boolean firstlevel=true;
	do{
	//fill all in this slice
    boolean addedone=false;
	uplevel=false;

//	System.out.println("z="+z);
	do{

	  //System.out.println("debug:: in fillblob doloop1 with blobsize="+blobsize+" blobnum="+blobnum);
	  addedone=false;
	  for (y=0;y<ydim;y++){
		  for (x=0;x<xdim;x++){
			  if (
				  (dat.get(z,y,x)==0)&&
			      (
				   ((y+1<ydim)&&(dat.get(z,y+1,x)==blobnum))||
			       ((y-1>=0)&&(dat.get(z,y-1,x)==blobnum))||
			       ((x+1<xdim)&&(dat.get(z,y,x+1)==blobnum))||
			       ((x-1>=0)&&(dat.get(z,y,x-1)==blobnum))
			      )
			     ){
				  dat.set(z,y,x,blobnum);
				  blobsize++;
				  addedone=true;
				  }
     	}//x
      }//y
      if (firstlevel){
		  System.out.println("blob size at level 1 is ="+blobsize);
		  blobinfo[0]=blobsize;
		  firstlevel=false;
	  }
	}while(addedone);
	//now propagate upward from z to z+1
	  for (y=0;y<ydim;y++){
		  for (x=0;x<xdim;x++){
			  if  (dat.get(z+1,y,x)==0){
				  for (int xx=-rz;xx<=rz;xx++){
					  for (int yy=-rz;yy<=rz;yy++){
						  if ((x+xx>=0)&&(x+xx<xdim)&&(y+yy>=0)&&(y+yy<ydim)){
							if (dat.get(z,y+yy,x+xx)==blobnum){
							  dat.set(z+1,y,x,blobnum);
					          blobsize++;
					          uplevel=true;
						  }
				}//bounds
	     	}//xx
	      }//yy
	     }//if
        }//x
	   }//y
       z++;
    }while(uplevel);
    System.out.print("blob starts="+zstart+" duration="+(z-zstart));
    blobinfo[1]=blobsize;
    blobinfo[2]=zstart;
    blobinfo[3]=z-zstart;
    blobinfo[4]=x;
    blobinfo[5]=y;
    return blobinfo;
  }



public boolean compileaddedstats=false;
public double fractionation;
public double wavebreaks;
public double addedstats_surfacearea;
public double addedstats_size;
public double addedstats_surfaceoversize;
public double addedstats_surfaceoversizeadjusted;
public double addedstats_propagateddistance;
public double addedstats_propagatespeed;
public double addedstats_xspanned;
public double addedstats_yspanned;
public doubleArray3D arr2d;
public doubleArray3D arr1;
public doubleArray3D arr2;
public doubleArray3D extent;
public doubleArray3D overlap;

public double addedstats_extent;
public double addedstats_filltime;
public double addedstats_wavefront;
public double addedstats_addedarea;
public double addedstats_maxvel;
public double addedstats_boundingbox;
public double addedstats_timetoboundingbox;
public int[] fillblob3(int zstart, int y, int x, double empty, double blobnum, boolean checkforcontraction, boolean usethreshold, double thresholdxy, double thresholdz){
	//
	int blobsize=0;
    int xstart=x;
    int ystart=y;
//	for (int i=0;i<xdim;i++){ for (int j=0;j<ydim;j++) { arr2d.set(j,i,0);}}
    for(int i=-1;i<=1;i++){

		for (int j=-1;j<=1;j++){
	     if (dat.get(zstart,y+i,x+j)==empty){
	      dat.set(zstart,y+i,x+j,blobnum);
	      blobsize++;
	  }
	 }
	 }

	int z=zstart;
    boolean uplevel=false;
    int[] blobinfo=new int[6];
//	System.out.println("debug:: in fillblob with blobnum="+blobnum);
    boolean firstlevel=true;
	do{
	//fill all in this slice
    boolean addedone=false;
	uplevel=false;

    //System.out.println("diagnoztic z="+z);


	int levelblobsize=0;

	//fill all connected with z,y,x.
    //System.out.println("diagnostic blobnum="+blobnum+" ydim="+ydim+" xdim="+xdim);

	for (y=0;y<ydim;y++){

		for (x=0;x<xdim;x++){
		 //System.out.println("diagnostic at z="+z+" dat="+dat.get(z,y,x));

		if (dat.get(z,y,x)==blobnum){
	     if (usethreshold) blobsize+=dat.fill2DThresh(z,y,x,empty,blobnum,thresholdxy,0);

		 			else blobsize+=dat.fill2D(z,y,x,empty,blobnum);
         //System.out.println("diagnostic blob size="+blobsize);
	     //blobsize+=dat.fill2D(z,y,x,empty,blobnum);

 	     if (firstlevel) {

		   blobinfo[0]=blobsize;

		   firstlevel=false;
	       }
	    }


       }
     }
    if (z+1<zdim){
    for (y=0;y<ydim;y++){
		for (x=0;x<xdim;x++){
		  if (dat.get(z,y,x)==blobnum){

			int newblob;
			if (usethreshold) newblob=dat.fill2DThresh(z+1,y,x,empty,blobnum,thresholdxy,1);
			else newblob=dat.fill2D(z+1,y,x,empty,blobnum);
			if (checkforcontraction){
			 }else
			if (newblob>0){
				uplevel=true;
				blobsize+=newblob;
			}

		  }
		}
	   }
   }else {
	   uplevel=false;
   }

   //check for contraction at this point.


	//now propagate upward from z to z+1
	 z++;
    }while(uplevel);
    //System.out.print("blob starts="+zstart+" duration="+(z-zstart));
    blobinfo[1]=blobsize;
    blobinfo[2]=zstart;
    blobinfo[3]=z-zstart;
    blobinfo[4]=xstart;
    blobinfo[5]=ystart;



    if (compileaddedstats){
	   int zend=z;
       int surfacearea=0;
       double blobsonthislevel=0;
       double blobsonlastlevel=0;
       double bsizetotal=0;
       double totalblobs=0;
       wavebreaks=0;
       addedstats_propagateddistance=0;
       addedstats_extent=0;
       addedstats_addedarea=0;
       addedstats_wavefront=0;
       addedstats_maxvel=0;
       addedstats_boundingbox=0;
       if ((arr2d==null)||(arr2d.xdim!=xdim)||(arr2d.ydim!=ydim))
	              arr2d=new doubleArray3D(ydim,xdim);
	   if ((arr1==null)||(arr1.xdim!=xdim)||(arr1.ydim!=ydim))
	              arr1=new doubleArray3D(ydim,xdim);

	   if ((arr2==null)||(arr2.xdim!=xdim)||(arr2.ydim!=ydim))
	              arr2=new doubleArray3D(ydim,xdim);

	   if ((extent==null)||(extent.xdim!=xdim)||(extent.ydim!=ydim))
	              extent=new doubleArray3D(ydim,xdim);

       if ((overlap==null)||(overlap.xdim!=xdim)||(overlap.ydim!=ydim))
	              overlap=new doubleArray3D(ydim,xdim);


	   for (x=0;x<xdim;x++){for (y=0;y<ydim;y++){
		   extent.set(y,x,0);
		   arr1.set(y,x,0);
		   arr2.set(y,x,0);
		   }
		 }


       int hits_lowx=0;
	   int hits_hix=0;
	   int hits_lowy=0;
	   int hits_hiy=0;
	   int minx=10000;
	   int maxx=0;
	   int miny=10000;
	   int maxy=0;
       int blobsonpreviouslevel=0;
       int boundschanged=0;
       for (z=zstart;z<zend;z++){


        for (x=0;x<xdim;x++){
			for (y=0;y<ydim;y++){
				   arr1.set(y,x,0);
				   arr2.set(y,x,0);
				   }
				 }



       blobsonthislevel=0;
       for ( x=0;x<xdim;x++){
		   for (y=0;y<ydim;y++){

			   if (dat.get(z,y,x)==blobnum){
				  extent.set(y,x,1);
			      arr2d.set(y,x,0);
			      arr1.set(y,x,1);
			      if (x<10) hits_lowx=1;
			      if (x>xdim-10) hits_hix=1;
			      if (y<10) hits_lowy=1;
			      if (y>ydim-10) hits_hiy=1;
			      if (x<minx) {minx=x;boundschanged=z;}
			      if (x>maxx) {maxx=x;boundschanged=z;}
			      if (y<miny) {miny=y;boundschanged=z;}
			      if (y>maxy) {maxy=y;boundschanged=z;}
			      }
			      else arr2d.set(y,x,-1);
			  }
		    }
		for (x=0;x<xdim;x++){
			for (y=0;y<ydim;y++){
			 if (dat.get(z+1,y,x)==blobnum){
				 arr2.set(y,x,1);
			 }
			}
		}
		blobsize=0;
		for (x=0;x<xdim;x++){
			for (y=0;y<ydim;y++){

			  if (arr2d.get(y,x)==0){
              blobsonthislevel+=1;
              //System.out.println("found blob at ="+x+","+y+" level="+z+" thresholdxy="+thresholdxy);
                     if (arr2d.get(y+1,x)==0){arr2d.set(y+1,x,blobsonthislevel);blobsize++;}
			  	     if (arr2d.get(y-1,x)==0){arr2d.set(y-1,x,blobsonthislevel);blobsize++;}
			  	     if (arr2d.get(y,x)==0)  {arr2d.set(y,x,blobsonthislevel);blobsize++;  }
                     if (arr2d.get(y,x+1)==0){arr2d.set(y,x+1,blobsonthislevel);blobsize++;}
                     if (arr2d.get(y,x-1)==0){arr2d.set(y,x-1,blobsonthislevel);blobsize++;}
			  	     blobsize+=arr2d.fill2DThresh(0,y,x,0,blobsonthislevel,thresholdxy,0);
			    }
		  }
	     }
		  for (x=0;x<xdim;x++){
			  for (y=0;y<ydim;y++){
				  if ((arr2d.get(0,y,x)==blobsonthislevel)&& (arr2d.countNeighbours(0, y, x, 1, -1)>1))
				   surfacearea++;

			  }
		     }
            bsizetotal+=blobsize;
            totalblobs+=blobsonthislevel;
            if (blobsonthislevel>blobsonlastlevel)
              wavebreaks+=blobsonthislevel-blobsonlastlevel;
            blobsonlastlevel=blobsonthislevel;

		  //calculate wavespeed with

		  for (x=0;x<xdim;x++){
			  for (y=0;y<ydim;y++){
				// reset overlap on array 2
				if ((arr1.get(0,y,x)==1)&&(arr2.get(0,y,x)==1)) arr2.set(0,y,x,0);
			  }
		  }

		   for (x=0;x<xdim;x++){
		  			  for (y=0;y<ydim;y++){
		  				// reset overlap on array 2
		  				if ((arr1.get(0,y,x)==1)&&(arr2.countNeighbours(0,y,x,1,1)>1)) arr1.set(0,y,x,2);
		  			  }
		  		  }


           double presentwavefront=0;
           double presentaddedarea=0;
           for (x=0;x<xdim;x++){
			   for (y=0;y<ydim;y++){
				 if (arr1.get(0,y,x)==2) {
					 presentwavefront+=1;
					 //find addearea caused by this piece of wavefront

				 }
			  }

		   }
           for (x=0;x<xdim;x++){
			   for (y=0;y<ydim;y++){

               if (arr2.get(0,y,x)==1) {
			   					 presentaddedarea+=1;
			   				 }
				}
			}


		   if ((z>=zstart)&&(z<=zend-1)){

		    if (presentwavefront>0){
			 double vel=presentaddedarea/presentwavefront;
		     if (addedstats_maxvel<vel) addedstats_maxvel=vel;
             addedstats_wavefront+=presentwavefront;
             addedstats_addedarea+=presentaddedarea;
	        }
		   }

		  }//z
         //try to figure out how fast the blob itself got filled

         for (x=0;x<xdim;x++){
			 for (y=0;y<ydim;y++){
			 addedstats_extent+=extent.get(0,y,x);

			 }
		 }
         for (x=0;x<xdim;x++){for (y=0;y<ydim;y++){extent.set(y,x,0);}}

         int counter=0;
         double extentstofar;
         addedstats_filltime=10000000;
         boolean done=false;
         for (z=zstart;z<zend;z++){
			if (done) break;
			for (y=0;y<ydim;y++){
				for(x=0;x<xdim;x++){

		         	   if (dat.get(z,y,x)==blobnum){
				       extent.set(y,x,1);
		               counter++;

				    }//if dat==blobnum
			}//x
	     }//y
	     if (counter>=addedstats_extent*0.95){
		 						   double extentsofar=0;
		 						   for (int i=0;i<ydim;i++){
		 							   for (int j=0;j<xdim;j++){
		 								    extentsofar+=extent.get(0,i,j);
		 							   }
		 						     }
		 						   if (extentsofar>=addedstats_extent*0.95) {addedstats_filltime=z-zstart;done=true;}

					   }
	      }//z
         if ((zend-zstart>3)&&(addedstats_extent>8)){
         double tmpoverlap;
         for (x=0;x<xdim;x++){
			 for (y=0;y<ydim;y++){
 				tmpoverlap=overlap.get(0,y,x);
 				overlap.set(0,y,x,tmpoverlap+extent.get(0,y,x));
			 }
		 }
	    }
         addedstats_extent=addedstats_extent/(xdim*ydim*0.78); //ratio of circle/square
		 if (zend-zstart>1){
		  fractionation=totalblobs/(zend-zstart);
	      wavebreaks=wavebreaks/(zend-zstart);
	      }
           else {
			   fractionation=1;
			   wavebreaks=0;
	       }
           //System.out.println("totalblobs="+totalblobs);
           addedstats_surfacearea=surfacearea;
           addedstats_size=bsizetotal;
           addedstats_surfaceoversize=(double)surfacearea/bsizetotal;
           addedstats_surfaceoversizeadjusted=Math.pow(addedstats_surfaceoversize,(0.333333));
	       if (hits_lowx+hits_hix==2) addedstats_propagateddistance+=2;
	       if (hits_lowy+hits_hiy==2) addedstats_propagateddistance+=2;
 	       addedstats_boundingbox=((double)((maxx-minx)*(maxy-miny)))/(xdim*ydim);
           addedstats_timetoboundingbox=boundschanged-zstart;

     //addedstats_boundingbox=(maxx-minx)*(maxy-miny);

	     }


    return blobinfo;
  }


/*problem with this algorithm is that it will track the whole blob, sum its size,
  and then find another expansion within the same blob later. Each blob gives many
  blobs with decreasing size
   x
  xx
   x x
    xx
  */

public double[][] findExpansions(){


   return null;

}


public double totalfractionation;
public double totalfractionatedblobs;
public double totalsurface;
public double totalwavebreaks;
public double totalfractionation_adjusted;
public double totaldistance;
public double totaldistance_sub;
public double max_extent;
public double average_extent;
public double filltime;
public double bigblobstarts;
public double totalwavefront;
public double totaladdedarea;
public double totalvel;
public double max_boundingbox;
public double max_boundingboxtime;
public double totalcoverage;
public double totaloverlap;
public double totalsumoverlap;
public double[][] BlobCount2(double binarythreshold,int maxsizestart, int minduration, int rz, boolean do_undo, int minblobsize, boolean checkforcontraction, boolean usethreshold, double thresholdxy, double thresholdz){
//count blobs
totalfractionation=0;
totalfractionatedblobs=0;
totalsurface=0;
totalfractionation_adjusted=0;
totalwavebreaks=0;
totaldistance=0;
totaldistance_sub=0;
max_extent=0;
average_extent=0;
totaladdedarea=0;
totalwavefront=0;
totalvel=0;
max_boundingbox=0;
max_boundingboxtime=0;
totaloverlap=0;
totalcoverage=0;
totalsumoverlap=0;
update_undo();

Vector v=new Vector();

//doubleArray3D blobs=new doubleArray3D(ydim,xdim);
int[] arr=new int[zdim];
for (int z=0;z<zdim;z++){
	 for (int y=0;y<ydim;y++){
		 for (int x=0;x<xdim;x++){
           if (dat.get(z,y,x)<binarythreshold) dat.set(z,y,x,-1);
             else dat.set(z,y,x,0);
		 }//x
		}//y
	 }//z
int blobnum=1;
for (int z=0;z<zdim;z++){
	ProgressDisplayer.getProgressDisplayer().displayProgress(((double)z)/((double)zdim));
	 for (int y=0;y<ydim;y++){
		 for (int x=0;x<xdim;x++){
          if (dat.get(z,y,x)==0) {
			  //public int[] fillblob3(int z, int y, int x, double empty, double blobnum, boolean checkforcontraction, boolean usethreshold, double threshold)

			  int[] bsize=fillblob3(z,y,x,0,(double)blobnum++,checkforcontraction,usethreshold,thresholdxy, thresholdz);
              //int[] bsize=fillblob(z,y,x,(double)blobnum++);
              if (compileaddedstats){
				  if ((bsize[3]>=minduration)&&(bsize[0]<=maxsizestart)){
                    totalfractionation+=fractionation;
                    totalfractionation_adjusted+=fractionation/bsize[3];
                    totalwavebreaks+=wavebreaks;
                    totalfractionatedblobs++;
                    totalsurface+=addedstats_surfaceoversize;
                    if (addedstats_extent>max_extent) {
						max_extent=addedstats_extent;
					    filltime=addedstats_filltime;
					    totalwavefront=addedstats_wavefront;
			            totaladdedarea=addedstats_addedarea;
					    bigblobstarts=bsize[2];
					    }
                    if (addedstats_boundingbox>max_boundingbox){
						max_boundingbox=addedstats_boundingbox;
						max_boundingboxtime=addedstats_timetoboundingbox;
					}


                    average_extent+=addedstats_extent;
                    if (addedstats_propagateddistance==4) totaldistance+=1;
                    totaldistance_sub+=addedstats_propagateddistance;
			        if (bsize[3]>=minduration){
			         //totalwavefront+=addedstats_wavefront;
			         //totaladdedarea+=addedstats_addedarea;
			         totalvel+=addedstats_maxvel;
				     }


			      }
			    }
              v.add(bsize);
             // System.out.println(" blob num="+blobnum+" size="+bsize);

		     }// if
    	   }//x
        }//y



   }//z
   ProgressDisplayer.getProgressDisplayer().displayProgress(1.0);


   if (compileaddedstats){
		 double tmplap;
		 for (int x=0;x<xdim;x++){
			 for (int y=0;y<ydim;y++){
		        tmplap=overlap.get(0,y,x);
		        if (tmplap>0) totalcoverage++;
		        if (tmplap>1){
					totaloverlap++;
		            totalsumoverlap+=tmplap;
				}
		        overlap.set(0,y,x,0);
			}
		}


	  }


Hashtable hash=new Hashtable(100);
for (int i=0;i<v.size();i++){
	int []tmp=(int[])v.elementAt(i);

    if ((tmp[0]<=maxsizestart) && (tmp[3]>=minduration)){
	Integer key=new Integer(tmp[1]);
    if (hash.containsKey(key)){
		int val=((Integer)(hash.get(key))).intValue();
		hash.put(key,new Integer(val+1));
	}else{
		hash.put(key,new Integer(1));
	}
  }
  else{
	 // System.out.println("excluding (start,size,z,duration,x,y)="+tmp[0]+","+tmp[1]+","+tmp[2]+","+tmp[3]+","+tmp[4]+","+tmp[5]);
  }
}

    double[][] dist=new double[2][hash.size()];
	Vector s = new Vector(hash.keySet());
    Collections.sort(s);
    for (int j=0; j < s.size(); j++){
		Integer clustersize=(Integer)s.elementAt(j);
		Integer totalclusters=(Integer)hash.get(clustersize);
		dist[0][j]=clustersize.doubleValue();
		dist[1][j]=totalclusters.doubleValue();
	}




if (do_undo){
	undo();
}
return dist;
}//blobcount


public boolean isNeighbour(int index1,int index2){
	return dat.isNeighbour(index1,index2);
}


public double[][] blobdata=new double[80][10];
/**
 double threshold = value to find clusters over or under.
 int radius = distance to search (should be 1 or more)
 boolean lookover = if true then find clusters with values greater than threshold, else lower
 int minsize = ignore clusters less than this particular size

**/
public Vector BlobCount2D(double threshold, int radius, boolean lookover, int minsize){
	update_undo();
   Vector results=new Vector();
	int z=0;

	 for (int y=0;y<ydim;y++){
			 for (int x=0;x<xdim;x++){
	           if (lookover){
	           if (dat.get(z,y,x)<threshold) dat.set(z,y,x,-1);
	             else dat.set(z,y,x,0);
		      }else{
		       if (dat.get(z,y,x)>threshold) dat.set(z,y,x,-1);
			  	             else dat.set(z,y,x,0);
		      }

			 }//x
		}//y
	int blobcount=1;
    for (int y=radius;y<ydim-radius;y++){
	  for (int x=radius;x<xdim-radius;x++){
          if (dat.get(z,y,x)==0) {
			  dat.set(z,y,x,blobcount);
			  int blobsize=1;
			  boolean hasmore=true;
			  int minx=x-radius;
			  int maxx=x+radius;
			  int miny=y-radius;
			  int maxy=y+radius;
			  //grow the size of the box searching for elements under threshold

			  do{
				hasmore=false;
				for (int y1=miny;y1<=maxy;y1++){
					for (int x1=minx;x1<=maxx;x1++){
						if (dat.get(z,y1,x1)==0){
							hasmore=true;
							dat.set(z,y1,x1,blobcount);
							blobsize+=1;
							if ((y1==maxy)&&(y1<ydim-radius)) maxy+=radius;
							if ((y1==miny)&&(y1>-radius)) miny-=radius;
							if ((x1==maxx)&&(x1<xdim-radius)) maxx+=radius;
							if ((x1==minx)&&(x1>-radius)) minx-=radius;
						    }
						}
					}
			  }while(hasmore);

            //since radius was increased and values not found, determine real bounds
            int maxx_f=-100000;
            int minx_f=100000;
            int maxy_f=-100000;
            int miny_f=100000;
            for (int y1=miny;y1<=maxy;y1++){
			 for (int x1=minx;x1<=maxx;x1++){
				 if (dat.get(z,y,x)==blobcount){
					if (y1<=miny_f) miny_f=y1;
					if (y1>=maxy_f) maxy_f=y1;
					if (x1<=minx_f) minx_f=x1;
					if (x1>=maxx_f) maxx_f=x1;
				 }
			 }
		    }

		    //fill in blobs that are less than a mininum size.
		    if (blobsize<minsize){
			  for (int y1=miny;y1<maxy;y1++){
				  for (int x1=minx;x1<maxx;x1++){
					if (dat.get(z,y1,x1)==blobcount){

						dat.set(z,y1,x1,-1);
					}

				  }
			  }
			}else{
			    int[] blobstats=new int[5];

				blobstats[0]=blobsize;
				blobstats[1]=minx_f;
				blobstats[2]=miny_f;
				blobstats[3]=maxx_f;
				blobstats[4]=maxy_f;

			    results.add(blobstats);



			   blobcount+=1;
		    }

          }
         }
        }
     return results;
	}



public double[][] BlobCount(double threshold,int maxsizestart, int minduration, int rz, boolean do_undo){
//count blobs
update_undo();
Vector v=new Vector();

//doubleArray3D blobs=new doubleArray3D(ydim,xdim);
int[] arr=new int[zdim];
for (int z=0;z<zdim;z++){
	 for (int y=0;y<ydim;y++){
		 for (int x=0;x<xdim;x++){
           if (dat.get(z,y,x)<threshold) dat.set(z,y,x,-1);
             else dat.set(z,y,x,0);
		 }//x
		}//y
	 }//z
int blobnum=1;
for (int z=0;z<zdim;z++){
	ProgressDisplayer.getProgressDisplayer().displayProgress(((double)z)/((double)zdim));
	 for (int y=0;y<ydim;y++){
		 for (int x=0;x<xdim;x++){
          if (dat.get(z,y,x)==0) {
			  int[] bsize=fillblob2(z,y,x,1,rz, (double)blobnum++);
              //int[] bsize=fillblob(z,y,x,(double)blobnum++);
              v.add(bsize);
              System.out.println(" blob num="+blobnum+" size="+bsize);

		     }// if
    	   }//x
        }//y

   }//z
    ProgressDisplayer.getProgressDisplayer().displayProgress(1.0);

Hashtable hash=new Hashtable(100);
for (int i=0;i<v.size();i++){
	int []tmp=(int[])v.elementAt(i);

    if ((tmp[0]<=maxsizestart) && (tmp[3]>=minduration)){
	Integer key=new Integer(tmp[1]);
    if (hash.containsKey(key)){
		int val=((Integer)(hash.get(key))).intValue();
		hash.put(key,new Integer(val+1));
	}else{
		hash.put(key,new Integer(1));
	}
  }
  else{
	  System.out.println("excluding (start,size,z,duration,x,y)="+tmp[0]+","+tmp[1]+","+tmp[2]+","+tmp[3]+","+tmp[4]+","+tmp[5]);
  }
}

    double[][] dist=new double[2][hash.size()];
	Vector s = new Vector(hash.keySet());
    Collections.sort(s);
    for (int j=0; j < s.size(); j++){
		Integer clustersize=(Integer)s.elementAt(j);
		Integer totalclusters=(Integer)hash.get(clustersize);
		dist[0][j]=clustersize.doubleValue();
		dist[1][j]=totalclusters.doubleValue();
	}




if (do_undo){
	undo();
}
return dist;
}//blobcount





/**

ca
 1) assume that CAFilter or similar sets an unambigous threshold value first

**/


public void CA(double thr, int A, int R){
	update_undo();

	doubleArray3D ca=new doubleArray3D(zdim,ydim,xdim);

	if (ca1==null) ca1=new doubleArray3D(ydim,xdim);
	if (ca2==null) ca2=new doubleArray3D(ydim,xdim);
	for (int z=1;z<zdim-1;z++){
	 for (int x=0;x<xdim;x++){
	  for (int y=0;y<ydim;y++){
		 if (dat.get(z,y,x)>thr) ca.set(z+1,y,x,R);
		 if (dat.get(z+1,y,x)>thr) ca.set(z+1,y,x,1);
	   }
      }
 }

dat=ca;
}


public Matrix VolumeFilter(double thr, double thrtotal, double thrback, double thrfront){
	Matrix res=new Matrix();
	res.create(zdim,ydim,xdim);
	for (int z=1;z<zdim-1;z++){
	 for (int y=1;y<ydim-1;y++){
	   for (int x=1;x<xdim-1;x++){
		   double totalsum=0;
		   double totalfront=0;
		   double totalback=0;
		   for (int zi=-1;zi<=1;zi++){
			   for (int yi=-1;yi<=1;yi++){
				   for (int xi=-1;xi<=1;xi++){
					if (dat.get(z+zi,y+yi,x+xi)>thr){
						totalsum++;
					    if (zi==1) totalfront++;
					    if (zi==-1) totalback++;
					}
				  }
			  }
		  }

		  if ((totalfront/9>thrfront)&&(totalback/9>thrback)&&(totalsum/27>thrtotal)){
			  res.dat.set(z,y,x,1);
		  }
	  }
    }
 }
 return res;
}

public void iterate(int thr,int A, int R){
   update_undo();
   if (ca1==null) ca1=new doubleArray3D(xdim,ydim);
   if (ca2==null) ca2=new doubleArray3D(xdim,ydim);
   int totalval;
	for (int z=1;z<zdim-1;z++){
	 for (int x=1;x<xdim-1;x++){
		  for (int y=1;y<ydim-1;y++){
			double presentval=dat.get(z,y,x);
			if (presentval==0){

			 totalval=0;
			 for (int i=-1;i<=1;i++){
			   for (int j=-1;j<=1;j++){
				  double v=dat.get(z,x+i,y+j);
				  if ((v>0)&&(v<=A)) totalval+=1;
				 }//i
		 	   }//j
		     if (totalval>thr){
				 ca1.set(y,x,A+1);
			  }


		     }

			 //if (dat.get(z,x,y)>thr) ca.set(z+1,x,y,R);
			 //if (dat.get(z+1,x,y)>thr) ca.set(z+1,x,y,1);
		   }
	     }
	   for (int x=0;x<xdim;x++){
		  for (int y=0;y<ydim;y++){
	       	double presentval=dat.get(z,y,x);
            if (presentval>0) presentval+=1;
            if (presentval>=A+R) presentval=0;
            if ((presentval==0)&&(ca1.get(x,y)==1)) presentval=A+1;
            if (dat.get(z+1,y,x)==0) dat.set(z+1,y,x,presentval);
	    }
	 }
}
}

/*
public void CAForward(double thr1, double thr2){
		 for (int x=0;x<xdim;x++){
		  for (int y=0;y<ydim;y++){
			for (int z=1;x<zdim-1;z++){
			 if ((dat.get(z-1,x,y)<thr2)
			     &&(dat.get(z,x,y)>thr1)
			     &&(dat.get(z+1,x,y)>thr2))
			      ca.set(z+1,x,y,R);
		   }
	      }
         }

}

*/

/**cafilter
  Summary: Initialize a ca based on data, iterate, and compare to data some time in
  the future. If both the CA and the raw data agree, then enhance the data at this spatial point.
   thr1=raw data comparison, should be a value around 1
   thr2=ca iteration. Should be between 0.3 and 0.6
   thr3=raw data comparison (z+1). Should be close to thr1
   its=number of times to iterate the CA before the comparison (1 or 2)
   attenuation=points that do not agree with the CA are multiplied by this
   baselinecomp=1 or 0. toggles internal baseline compensation. If used, can also
      set cafilter_median and cafilter_average, which work on a trace by trace basis.

**/

doubleArray3D ca1;
doubleArray3D ca2;

public int cafilter_median=5;
public int cafilter_average=31;
public int propagateforward=0; //set this to 1, in order to propagate the enhanced wave (dangerous!!!)


public String CAFilter(double thr1, double thr2, double thr3, int its, double attenuation, int baselinecompensate){
	//populate an array with data from the image file, iterate, then
	//replace the data in the original with the result.
 System.out.println("begin CAFilter");
 update_undo();
 String report="";
 //determine a running average of the max/min for each frame
    double[] aveintensity=new double[zdim];
    double totalintensity=0;
    for (int z=0;z<zdim;z++){
		double intensity=0;
		for (int x=0;x<xdim;x++){
			for (int y=0;y<ydim;y++){
			intensity+=dat.get(z,x,y);
			}
		}
		aveintensity[z]=intensity/(xdim*ydim);
		totalintensity+=aveintensity[z];
	}
if (baselinecompensate==1){
 	aveintensity=JSci.maths.EngineerMath.runningMedian(aveintensity,cafilter_median);
	aveintensity=JSci.maths.EngineerMath.runningAverage(aveintensity,cafilter_average);
   }else
   {
  for (int z=0;z<zdim;z++)
   aveintensity[z]=totalintensity/zdim;
   }


	if (ca1==null) ca1=new doubleArray3D(xdim,ydim);
	if (ca2==null) ca2=new doubleArray3D(xdim,ydim);
	//1 iterate
	double totalval;


   double total_active_firststep=0;
   double total_expanded_secondstep=0;
   double total_reduced_thirdstep=0;

	for (int z=0;z<zdim;z++){
	ProgressDisplayer.getProgressDisplayer().displayProgress(((double)z)/((double)zdim));

    //System.out.println("z="+z);
       for (int x=1;x<xdim-1;x++){
			for (int y=1;y<ydim-1;y++){

			  totalval=0;
			  for (int i=-1;i<=1;i++){
				  for (int j=-1;j<=1;j++){
					totalval+=dat.get(z,x+i,y+j);
				}//i
		 	}//j
			if ((totalval/9)>aveintensity[z]*thr1) {ca1.set(x,y,1); total_active_firststep+=1;}
			 else ca1.set(x,y,0);
			 //if ((z==110)&&(x==20)&&(y>20)&&(y<25)) System.out.println("totalval="+totalval/9+" aveintensity="+aveintensity[z]+" z="+z);
		}
	}
	//System.out.println("finished initializing ca array for z="+z);


	//iterate
	for (int f=0;f<its;f++){
	//	System.out.println("starting iterate="+f);
		for (int x=2;x<xdim-2;x++){
			for (int y=2;y<ydim-2;y++){
			  totalval=0;
			  for (int i=-2;i<=2;i++){
				  for (int j=-2;j<=2;j++){
					totalval+=ca1.get(x+i,y+j);
				}
			}
			if ((totalval/25)>thr2) {ca2.set(x,y,1); total_expanded_secondstep+=1;}
			else ca2.set(x,y,0);
			 ///if ((z==110)&&(x==20)&&(y>20)&&(y<25)) System.out.println("iterate="+f+" thr="+totalval/25);
		 }
  	   }//xdim

	//copy
		for (int x=1;x<xdim-1;x++){
				for (int y=1;y<ydim-1;y++){
					ca1.set(x,y,ca2.get(x,y));
				}
			}

	}//f


//missing comparison step.
//remove ca1s that aren't close to active cells in the next iterate
if (z<zdim-1){
for (int x=1;x<xdim-1;x++){
			for (int y=1;y<ydim-1;y++){
              if (ca1.get(x,y)==1){
			  totalval=0;
			  for (int i=-1;i<=1;i++){
				  for (int j=-1;j<=1;j++){
					totalval+=dat.get(z+1,x+i,y+j);
				}//i
		 	}//j
			if ((totalval/9)<aveintensity[z+1]*thr3) {ca1.set(x,y,0); total_reduced_thirdstep+=1;}
		}//if
		}//ydim
   }//xdim
}
	//replace old data
//int totalreduced=0;
if (z<zdim-1){
		for (int x=1;x<xdim-1;x++){
				for (int y=1;y<ydim-1;y++){
					if (ca1.get(x,y)==1){
						dat.set(z+propagateforward,x,y,attenuation*dat.get(z+1,x,y));
						//totalreduced++;
						}
				}
			}
}
//System.out.println("total reduced="+totalreduced+" from "+xdim*ydim);



}//z
ProgressDisplayer.getProgressDisplayer().displayProgress(1.0);

report="Ave activated="+(total_active_firststep/(double)zdim)+" ave expanded="+(total_expanded_secondstep/(double)zdim)+" ave reduced="+(total_reduced_thirdstep/(double)zdim);
System.out.println(report);

ca1=null;
ca2=null;
aveintensity=null;
return report;
}





// if at z, neighbours > threshold, and at z+1 neighbours> threshold
public void FillFilter(double thr1, double nbslevel1, double thr2, double nbslevel2, double attenuation){

	if (ca1==null) ca1=new doubleArray3D(xdim,ydim);
	double tmp;
	int sum;
	//assume normalized (0-1000) prior to FillFilter
    for (int z=0;z<zdim-1;z++){
		//step 1
		for (int x=0;x<xdim;x++){
			for (int y=0;y<ydim;y++){
			   //
			   //if x,y < thr1 and there are many neigbours> thr1 then set x,y to 1 else set to 0.
			   //gives an array of 0s and 1s that should fill locally blobs
			   tmp=dat.get(z,y,x);
			   if(tmp<thr1){
			    sum=dat.countNeighboursOverThreshold(z,y,x,1,thr1);
			    if (sum>nbslevel1) ca1.set(y,x,1);
			    else ca1.set(y,x,0);
		       }else ca1.set(y,x,1);
			}
		}


		for (int x=1;x<xdim-1;x++){
			for (int y=1;y<ydim-1;y++){
				if (ca1.get(y,x)==1){
					//look at neighbourhood and determine if there is a cluster of active cells nearby
					boolean activelevelup=false;
					tmp=dat.get(z,y,x);
					for (int i=-1;i<=1;i++){
						for (int j=-1;j<=1;j++){
							sum=dat.countNeighboursOverThreshold(z+1,y,x,1,thr2);
							if (sum>nbslevel2) activelevelup=true;
							break;
						}//j
					 if (activelevelup) break;
					}//i
				if (activelevelup)
				 dat.set(z,y,x,tmp*attenuation);
			}//if
		}//y

	}//x




	}//z


}

public void suppressEdges(int centerx, int centery, double r){
update_undo();
for (int z=0;z<zdim;z++){
	for (int x=0;x<xdim;x++){
		for (int y=0;y<ydim;y++){
			double dist=Math.sqrt((x-centerx)*(x-centerx)+(y-centery)*(y-centery));
			if (dist>r)dat.set(z,x,y,0.50*dat.get(z,x,y));
		}
}
}
}


/*
 what should it do. pass the function an array of data points
*/


public Matrix process(function f, int[] roi, int negz, int posz, int negy, int posy, int negx, int posx){
 //pass the function a 3d array with dimensions relative to the
 //present position
 //march through the pixels in the roi. for each, get a subset of dat

 //1)create the new matrix object that will be populated by f

	Matrix nm=new Matrix();
	nm.xdim=xdim;
    nm.ydim=ydim;
    nm.zdim=zdim;
	nm.dat=new doubleArray3D(zdim,xdim,ydim);
	nm.frame=new doubleArray3D(xdim,ydim);

 //2)create the empty array that will be continuously re-used, filled around the point of interest
     doubleArray3D mini_arr=new doubleArray3D((posz+negz),(posy+negy),(posx+negx));


   int [] arr=null;
       if (roi==null){
   	   arr=new int[xdim*ydim];

   	   for (int i=0;i<xdim*ydim;i++) arr[i]=i;
   	   //perform op on whole field
   	} else{
   	 arr=roi;
    }

 //3)pass the array to the function, and put the result in the new matrix

   for (int z=0;z<zdim;z++){
    //System.out.println("in process z="+z);
    //vw.ifd.jv.displayProgress(((double)z)/((double)zdim));
    ProgressDisplayer.getProgressDisplayer().displayProgress(((double)z)/((double)zdim));
    for (int kk=0;kk<arr.length;kk++){
	 //unpack the number
	int  x=(int)((double)arr[kk]/xdim);
	int  y=(int)((double)arr[kk]%xdim);
			//tmp=dat.section(i,j);
        //the point of interest is now z,y,x

     //populate the mini 3darray
      for (int zn=z-negz;zn<z+posz;zn++){
		  for (int yn=y-negy;yn<y+posy;yn++){
			  for (int xn=x-negx;xn<x+posx;xn++){
				  //check bounds
				  if ((zn>=0)&&(zn<zdim)&&(yn>=0)&&(yn<ydim)&&(xn>=0)&&(xn<xdim)){
					mini_arr.set(zn-(z-negz),yn-(y-negy),xn-(x-negx),dat.get(zn,yn,xn));
				}//bounds
			}//xn
		}//yn
	}//zn

	 //pass the array to the function f
	 // System.out.println("mini_arr.arr[1]="+mini_arr.arr[1]);
      nm.dat.set(z,y,x,f.compute(mini_arr));
   }//kk

  }//z
  ProgressDisplayer.getProgressDisplayer().displayProgress(1.0);
 return nm;
}



	public double[] getDoubles(int y, int x){
		return dat.section(y,x);
	}

	public double[] getDoubles(int y, int x, int start, int end){
		double[] tmp=new double[end-start];
		for (int i=start;i<end;i++){
			tmp[i-start]=dat.get(i,y,x);
		}
		return tmp;
		//Range tmprange=new Range(start,end-1);
		//return dat.section(tmprange,x,y).toJava();
	}


     /*
      if end-start>width
     */
   // public int gp_path_length;
   // public int gp_start;
   // public int gp_end;
   // public int gp_array_length;
   // public float gp_step;
   // public float gp_h_scale;
   // public float gp_w_scale;
   // public float gp_width;


    public float gp_last_x;
    public GeneralPath getGeneralPath(int y, int x, int start, int end, float width, float height){
		int arraylength=(int)(width*2.0f);
		if (end-start<width*2) arraylength=end-start;
		float step=((float)(end-start))/arraylength;
		//
	//	gp_step=step;
	//	gp_start=start;
	//	gp_end=end;
		//
		double[] tmp=new double[arraylength];
		double min=1000000;
		double max=-1000000;
		double c;
		GeneralPath gp=new GeneralPath(GeneralPath.WIND_EVEN_ODD,tmp.length+1);
		int index=0;
		for (float i=(float)start;i<end;i+=step){
			 c=-1*dat.get((int)i,y,x);
			 index++;
			 if (index<tmp.length)
			   tmp[index]=c;
			 if (min>c) min=c;
			 if (max<c) max=c;
		}
		float h_scale=height/((float)(max-min));
		float w_scale=width/tmp.length;
		gp.moveTo(0.0f,(float)((tmp[0]-min)*h_scale));
        //
   //     gp_h_scale=h_scale;
   //     gp_w_scale=w_scale;
   //     gp_array_length=tmp.length;
   //     gp_path_length=tmp.length;
   //     gp_width=width;

		for (int ii=1;ii<tmp.length;ii++){
		    gp.lineTo((float)ii*w_scale,(float)(tmp[ii]-min)*h_scale);
            gp_last_x=(float)ii*w_scale;
		}
	  return gp;
	}


	public void plotContour(int frame, int beginX, int endX, int beginY, int endY){
		//write the data to a file
		try{
   		 PrintWriter file=new PrintWriter(new FileWriter("tmpgnudat.dat"),true);
		 for (int y=beginY;y<endY;y++){
			for (int x=beginX;x<endX;x++){
			file.println(y+" "+x+" "+dat.get(frame,y,x));
			}
			file.println("");
		}
			file.close();
		}
		catch(Exception e){e.printStackTrace();}
		Runtime r=Runtime.getRuntime();

		try{
		Process p = r.exec("C:\\downloads\\bub\\gnuplot2\\wgnuplot.exe c:\\programs\\java\\decoder\\gnucommand.gnu");
		}catch(Exception e){e.printStackTrace();}
	}


    public Trace getTrace(int x,int y){
		return getTraceXY(x,y);
	}

    public Trace getTraceXY(int x, int y){
	 return getTraceYX(y,x);
	}

	public Trace getTraceYX(int y, int x){
	//	doubleArray1D fa=dat.section(x,y);
     int[] tmp=new int[zdim];
	   for (int i=0;i<zdim;i++){
	 		tmp[i]=(int)dat.get(i,y,x);
	 	}

		Trace tr=new Trace(tmp);
		return tr;
	}


public void memtestloop(){

	int[] tmp=new int[1000];
	for (int i=0;i<1000;i++){
		tmp[i]=i;
	}

}
public void memorytest(){
	//this is here to test that the gc is working, or whether its jythons fault.

   // Matrix ma=new Matrix();
    create(500,80,80);
    for (int z=0;z<500;z++){
			for (int x=0;x<80;x++){
				for (int y=0;y<80;y++){
					dat.set(z,y,x,(z*zdim+y*ydim+x));
				}
			}
	}
	for (int i=0;i<1000;i++){
		memtestloop();
		 try {
		                Thread.currentThread().sleep(100);
		            }
		            catch (InterruptedException e) {
            }
}


}


public static void main(String[] arg){
Matrix ma = new Matrix();
ma.zdim=6;
ma.xdim=10;
ma.ydim=10;
ma.compileaddedstats=true;
ma.dat=new doubleArray3D(6,10,10);
	for (int i=0;i<ma.dat.size;i++) ma.dat.setIndex(i,1);
	for (int x=0;x<=2;x++){
		for (int y=0;y<=10;y++){
		    if (x%2==0) ma.dat.set(1,y,x,0);
		  //ma.dat.set(1,y,x,0);
		  //ma.dat.set(2,y,x,0);
		  //ma.dat.set(3,y,x,0);
		  //ma.dat.set(4,y,x,0);
		}
	}


   for (int x=2;x<=6;x++){
   		for (int y=0;y<=10;y++){
   		    if (y%2==0) ma.dat.set(2,y,x,0);

   		  //ma.dat.set(2,y,x,0);
   		  //ma.dat.set(1,y,x,0);
   		  //ma.dat.set(2,y,x,0);
   		  //ma.dat.set(3,y,x,0);
   		  //ma.dat.set(4,y,x,0);
   		}
	}


 for (int x=2;x<=10;x++){
   		for (int y=0;y<=10;y++){
  if ((y+1)%2==0) ma.dat.set(3,y,x,0);

   		//  ma.dat.set(3,y,x,0);
   		  //ma.dat.set(1,y,x,0);
   		  //ma.dat.set(2,y,x,0);
   		  //ma.dt.set(3,y,x,0);
   		  //ma.dat.set(4,y,x,0);
   		}
	}



for (int i=0;i<0;i++){
int ii=(int)(Math.random()*10);
int jj=(int)(Math.random()*10);
int kk=(int)(Math.random()*4);
  ma.dat.set(kk,jj,ii,1);

}
   /*

   ma.dat.set(1,5,1,1);
   ma.dat.set(1,5,2,1);
   ma.dat.set(1,5,3,1);
    ma.dat.set(1,5,4,1);
     ma.dat.set(1,5,5,1);
      ma.dat.set(1,5,6,1);
       ma.dat.set(1,5,7,1);
	*/

	//ma.dat.set(1,7,7,0);
	//ma.dat.set(0,4,4,5);
	/*

	ma.dat.set(0,7,7,0);
	ma.dat.set(0,9,9,0);
	ma.dat.set(0,2,2,0);
	ma.dat.set(0,2,5,0);
	ma.dat.set(0,4,4,5);
	ma.dat.set(0,4,5,5);
    */
System.out.println(ma.dat.print(0,3,0,10,0,10));
//public int[] fillblob3(int zstart, int y, int x, double empty, double blobnum, int newblobsize, boolean checkforcontraction, boolean usethreshold, double threshold){
	//
ma.compileaddedstats=true;
//System.out.println(ma.dat.print(0,3,0,10,0,10));
int[] bsize=ma.fillblob3(1,2,2,0,5.0,false,true,0,0);

System.out.println(ma.dat.print(0,3,0,10,0,10));
System.out.println("fractionation="+ma.fractionation);
System.out.println("surfacearea="+ma.addedstats_surfacearea);
System.out.println("size="+ma.addedstats_size);
System.out.println("area/vol"+ma.addedstats_surfaceoversize);
System.out.println("adjusted="+ma.addedstats_surfaceoversizeadjusted);
System.out.println("addedarea="+ma.addedstats_addedarea);
System.out.println("wavefront="+ma.addedstats_wavefront);
System.out.println("wavespeed="+ma.addedstats_addedarea/ma.addedstats_wavefront);

//ma.memorytest();



/*	Matrix ma=new Matrix();
	ma.create(5,9,9);
	for (int z=0;z<5;z++){
		for (int x=0;x<9;x++){
			for (int y=0;y<9;y++){
				ma.dat.set(z,x,y,(z*ma.zdim+y*ma.ydim+x));
			}
		}
	}


for (int z=0;z<5;z++){
		for (int x=0;x<9;x++){
			for (int y=0;y<9;y++){
				System.out.print(ma.dat.get(z,x,y)+" ");
			}
			System.out.println("");
		}
	}

ma.processInSpace();
System.out.println("");
System.out.println("");
for (int z=0;z<5;z++){
		for (int x=0;x<9;x++){
			for (int y=0;y<9;y++){
				System.out.print(ma.dat.get(z,x,y)+" ");
			}
			System.out.println("");
		}
	}

*/
}



}







