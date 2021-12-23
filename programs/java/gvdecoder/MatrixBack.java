package gvdecoder;
//import com.ibm.math.array.*;
import trace.*;
import java.io.*;

import java.nio.*;

import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

import java.util.*;
import JSci.maths.*;
import JSci.maths.wavelet.daubechies2.*;
import gvdecoder.array.*;
import java.awt.geom.GeneralPath;
/* 	1) average / mean in volumes
	2) compute running average, subtract from trace

	use phase map voltage and calcium
*/

public class Matrix extends ImageDecoderAdapter{
	public doubleArray3D dat;
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

	public Viewer2 vw=null;

	public boolean VERBOSE=true;

public String help(){
return "Matrix\nConstructors:\n Matrix()\n Matrix(Matrix m) [copies]\n Matrix(Matrix m, startx, endx, starty, endy, startz, endz) [copies submatrix]\n Matrix(Matrix m, startx, endx, starty, endy, startz, endz, int mag, int active) [active not used, simple resample for bigger array]\n initialize(absfilename,filetype,start,stop)\n initialize(filetype,start,stop)\n initialize(ImageDecoder, start,stop)\n initialize(Viewer2,start,stop)\n initialize(double[][]arr, double mult) [not useful, probably in script, makes 3byXbyY array]\n \n Processing:\n processInSpace()[a median filter in space only]\n processByTrace(String type)\n processByTrace(String type, int arg)\n processByTrace(String type, int[] roi)\n processByTrace(String type,int arg, int[]roi)\n processByTrace(String type, int[] roi, function f)\n processByTrace(String type, int[] roi, function f)\n process(function f, int[] roi, int negz, int posz, int negy, int posy, int negx, int posx)\n \n BaseLineSubtract(int median, int average)[subtracts a median and average from the matrix, used in CAFilter]\n CAFilter(double thr1, double thr2, double thr3, int its, double attenuation, int baselinecompensate)[th1,th3 should be around 1, th2 around 0.5]\n \n suppressByTrace(threshold,percent)[determines if a traces total activity is greater than a percentage, and suppresses it if it isnt]\n suppress(int x, int y)[suppress this trace, sets to 0]\n suppress(int[][] arr)[arr holds x,y pairs, arr[n][0]=x, arr[n][1]=y]\n suppressEdges(int centerx, int centery, double r)[reduces edges by 50%, normalize to reverse]\n subtract(Matrix m)\n \n update_undo() [saves array in temp store]\n undo() [restores array]\n \n OpenImageFile(filename) [used to open file of type ma]\n SaveImageFile(filename) [used to save file of type ma]\n \n getDoubles(int x, int y) returns dat.section(x,y)\n getDoubles(int x, int y, int start, int end) [returns a subrange]\n getTrace(int x, int y)\n";
}


	public Matrix(){
		 //
	 }

	public Matrix(Matrix m){
	// essentially clone
	this.zdim=m.zdim;
	this.xdim=m.xdim;
	this.ydim=m.ydim;
	create(zdim,xdim,ydim);
	//initialize the array
	dat.assign(m.dat);

	}


    public Matrix(int zdim, int ydim, int xdim){
	 this.zdim=zdim;
	 this.ydim=ydim;
	 this.xdim=xdim;
	 create(zdim,xdim,ydim);
	}



	public Matrix(Matrix m,int startx, int endx, int starty, int endy, int startz, int endz, int mag){
		this.zdim=endz-startz;
		this.xdim=(endx-startx);
		this.ydim=(endy-starty);
		create(zdim,xdim,ydim);
		for (int z=startz;z<endz;z++){
			for (int x=startx;x<endx;x++){
			   for (int y=starty;y<endy;y++){
				 dat.set(z-startz,x-startx,y-starty,m.dat.get(z,x,y));
				 //dat.set(z,x,y,(z*ma.zdim+y*ma.ydim+x));

			   }
		   }
	   }

	}

	public Matrix(Matrix m,int startx, int endx, int starty, int endy, int startz, int endz, int mag, int active){
		this.zdim=endz-startz;
		this.xdim=(endx-startx)*mag;
		this.ydim=(endy-starty)*mag;
		create(zdim,xdim,ydim);
		for (int z=startz;z<endz;z++){
			for (int x=startx;x<endx;x++){
			   for (int y=starty;y<endy;y++){
				 dat.set(z-startz,(x-startx)*mag,(y-starty)*mag,m.dat.get(z,x,y));
				 //dat.set(z,x,y,(z*ma.zdim+y*ma.ydim+x));

			   }
		   }

	 }


	}


  public void subtract(Matrix m){

	  dat.subtract(m.dat);

  }



   double[][] tmparr;
   public void fillmatrix(){
	   if (tmparr==null) tmparr=new double[xdim][ydim];


	   for (int z=0;z<zdim;z++){
		   for (int q=0;q<xdim;q++){
		   				for (int p=0;p<ydim;p++){
		   				tmparr[q][p]=0;
		   				}
			}
		   for (int x=0;x<xdim;x++){
			   for (int y=0;y<ydim;y++){
				if (dat.get(z,x,y)==0){
					double sum=0;
					int numnbs=0;
					for (int i=-1;i<=1;i++){
						for (int j=-1;j<=1;j++){
							int xloc=x+i;
							int yloc=y+j;
							if ((xloc>0)&&(yloc>0)&&(xloc<xdim)&&(yloc<ydim)){
								double val=dat.get(z,xloc,yloc);
								if (val!=0){
								   sum+=dat.get(z,xloc,yloc);
								   numnbs+=1;
							     }

						}
					}
				}

			   if (numnbs!=0) tmparr[x][y]=sum/numnbs;
		      }
		  }
	   }
	   for (int q=0;q<xdim;q++){
		   for (int p=0;p<ydim;p++){
			if (tmparr[q][p]!=0)
			  dat.set(z,q,p, tmparr[q][p]);
		  }
	  }

	  }
 }





	public void update_undo(){
		dat_undo=new doubleArray3D(dat);
	}

	public void undo(){
	 if (dat_undo!=null) dat=new doubleArray3D(dat_undo);
	 else System.out.println("NOT UNDOABLE");
	}

	 public int UpdateImageArray(int[] arr,int xdim,int ydim, int instance){
	  int i=0;
	  int j=0;
	  if (framenum>=zdim) framenum-=1;
	    try{
	    for (i=0;i<xdim;i++){
			for (j=0;j<ydim;j++){
				if ((SHOWSINGULARITIES)&&(sing!=null)){

					if (COMPOSTSINGULARITIES)
						arr[j*xdim+i]=(int)(sing.get(framenum,i,j)+dat.get(framenum,i,j));
					else
						arr[j*xdim+i]=(int)sing.get(framenum,i,j);

				}else
				arr[j*xdim+i]=(int)dat.get(framenum,i,j);
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

    public int OpenImageFile(String filename){
		try{
			 RandomAccessFile infile=new RandomAccessFile(filename,"r");
			 int version=infile.readInt(); //version
			 zdim=infile.readInt();
			 ydim=infile.readInt();
			 xdim=infile.readInt();
			 create(zdim,xdim,ydim);


			  FileChannel fc = infile.getChannel();
			  MappedByteBuffer buffer = fc.map (FileChannel.MapMode.READ_ONLY, 0, fc.size());

			 if (version==0){
			 DoubleBuffer db=buffer.asDoubleBuffer();
			 double[] tmp=new double[xdim*ydim];

			 for (int z=0;z<zdim;z++){
				 db.get(tmp,0,xdim*ydim);
				 for (int y=0;y<ydim;y++){
					 for (int x=0;x<xdim;x++){
						 dat.set(z,x,y,tmp[x*ydim+y]);
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
			 						 dat.set(z,x,y,(double)tmp[x*ydim+y]);
			 					 }
			 				 }
			 }


			}

			infile.close();

			}catch(IOException e){e.printStackTrace();}
		return 0;
		}







    public void SaveImageFile(String filename){
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


	  for (int z=0;z<zdim;z++){
		 for (int y=0;y<ydim;y++){
			 for (int x=0;x<xdim;x++){
				 //outfile.writeDouble(dat.get(z,x,y));
				 buffer.putDouble(dat.get(z,x,y));
	//		 	 for (int k=0;k<8;k++) bytes[(8*(x*ydim+y))+k]=a[k];
			 }
		 }


	 }

	outfile.close();
	}catch(IOException e){e.printStackTrace();}
	}


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

	public void ApplyMask(){
	 for (int z=0;z<zdim;z++){
		for (int y=0;y<ydim;y++){
			for (int x=0;x<xdim;x++){
				if (!mask[y][x]) dat.set(z,x,y,0);
	 	 }
		}
	}
	}

	public void correlation(){
		double average=0;
		if (corr==null) corr=new double[offset][xdim];
		for (int x=0;x<xdim;x++){
			for (int y=0;y<offset;y++){

					double[] tmpV=dat.section(x,y);
					double[] tmpC=dat.section(x,y+offset);
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


	public void  fftcorrelation(){
			double average=0;
			if (corr==null) corr=new double[offset][xdim];
			for (int x=0;x<xdim;x++){
				for (int y=0;y<offset;y++){

						double[] tmpV=dat.section(x,y);
						double[] tmpC=dat.section(x,y+offset);
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

	public void create(int zdim, int xdim, int ydim){
     this.xdim=xdim;
	 this.ydim=ydim;
	 this.zdim=zdim;
	 dat=new doubleArray3D(zdim,xdim,ydim);
	 //dat_undo=new doubleArray3D(zdim,xdim,ydim);
	 frame=new doubleArray3D(xdim,ydim);
	 //xrange=new Range(0,xdim-1);
	 //yrange=new Range(0,ydim-1);
	 //zrange=new Range(0,zdim-1);
	 System.out.println("called create with "+xdim+" "+ydim+" "+zdim);
	}

	public void set(int frame, int[] arr){
	 for (int i=0;i<xdim;i++){
	  for (int j=0;j<ydim;j++){
		dat.set(frame,i,j,(float)arr[j*xdim+i]);
		}
	}
	}


   public void initialize(ImageDecoder id, int start, int end, int xstart, int xend, int ystart, int yend){
		    int[] tmp=new int[4];
			id.ReturnXYBandsFrames(tmp,instance);
			int[] tmpframe=new int[tmp[0]*tmp[1]];
			create((end-start),xend-xstart,yend-ystart); //xdim and ydim are set for the array
			for (int i=start;i<end;i++){
			   id.JumpToFrame(i,instance);
			   id.UpdateImageArray(tmpframe,tmp[0],tmp[1],instance);
			   // set((i-start),tmpframe);
			   for (int p=0;p<xdim;p++){
				   for (int q=0;q<ydim;q++){
					int y=q+ystart;
					int x=p+xstart;
					dat.set(i-start,p,q,tmpframe[y*tmp[0]+x]);
				   }
			   }
		     }
	}

    public void initialize(ImageDecoder id, int start, int end){
	    int[] tmp=new int[4];
		id.ReturnXYBandsFrames(tmp,instance);
		int[] tmpframe=new int[tmp[0]*tmp[1]];
		create((end-start),tmp[0],tmp[1]);
		for (int i=start;i<end;i++){
		   id.JumpToFrame(i,instance);
		   id.UpdateImageArray(tmpframe,tmp[0],tmp[1],instance);
		   set((i-start),tmpframe);
	     }


	}



public ImageDecoder im;

   public void initialize(String absolutefilename, String filetype, int start, int end){
	   im=ImageDecoderFactory.getDecoder(filetype);
       instance=im.OpenImageFile(absolutefilename);
       initialize(im,start,end);
   }

public void reinitialize(int start, int end){
	initialize(im,start,end);
}


   public String initialize(String filetype, int start, int end){
      GView gv=JythonHelper2.getGView();
       gv.fc.setVisible(true);

	   String filename = gv.fc.getFile();
	   String directory= gv.fc.getDirectory();
	   File file = new File(directory+filename);
	   String absfilename=file.getAbsolutePath();
       initialize(absfilename,filetype,start,end);
       return absfilename;

   }

	public void initialize(Viewer2 v2, int start, int end){

		initialize(v2.im,start,end);

	   }

/*
public void initialize(Viewer2 v2, int start, int end){

		//ImageDecoder id=v2.im;
       System.out.println("called initialize");
	   int[] tmp=new int[4];
       v2.im.ReturnXYBandsFrames(tmp,v2.instance);
//		int[] tmpframe=new int[tmp[0]*tmp[1]];
		create((end-start),tmp[0],tmp[1]);
		for (int i=start;i<end;i++){
		   id.JumpToFrame(i,v2.instance);
		   id.UpdateImageArray(tmpframe,tmp[0],tmp[1],v2.instance);
		   set((i-start),tmpframe);
	     }

	   }

*/
public String info(){
 return "Inside Matrix";
}

public void initialize(double[][] arr, double mult){
	System.out.println("in init debug (0,0)="+arr[0][0]+" (1,1)="+arr[1][1]);

	ydim=arr.length;
	xdim=arr[0].length;
	zdim=3;
	create(3,xdim,ydim);
	for (int y=0;y<ydim;y++){
		for (int x=0;x<xdim; x++){
		dat.set(0,x,y,arr[x][y]*mult);
		dat.set(1,x,y,arr[x][y]*mult); //shameless buggery
		dat.set(2,x,y,arr[x][y]*mult);
		}
	}


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



public void createContourMap(int start, int end, int timestep){
	int numsteps=(int)(((double)(end-start))/(double)timestep);
	cnt=new intArray3D(xdim,ydim);

	//assumes that we have a threshold function already
	for (int i=start; i<end;i+=timestep){
	 for (int y=0;y<ydim;y++){
		 for (int x=0;x<xdim;x++){
			 if ((dat.get(i,x,y)>500)&&(cnt.get(x,y)==0))cnt.set(x,y,i-start);
			 }
		 }
		}
    System.out.println("done creating contour map data");
		try{
		   		 PrintWriter file=new PrintWriter(new FileWriter("tmpgnudat.dat"),true);
				 for (int y=0;y<ydim;y++){
					for (int x=0;x<xdim;x++){
					file.println(y+" "+x+" "+cnt.get(x,y));
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
 sing=new doubleArray3D(zdim,xdim,ydim);
 for (int z=0;z<zdim;z++){
	 int[][] s=phaseSingularity(z);
	 for (int i=0;i<s.length;i++){
		sing.set(z,s[i][0],s[i][1],1000);
		}
   }
}


public void FilterSingularitiesInTime(){
//score higher if they are adjacent to others in time
if (sing!=null){
	for (int z=0;z<zdim-1;z++){
		for (int x=0;x<xdim;x++){
			for (int y=0;y<ydim;y++){

				if (sing.get(z,x,y)>0){
				  if ((sing.get(z+1,x,y)>0)||
					 (sing.get(z+1,x+1,y)>0) ||
					 (sing.get(z+1,x,y+1)>0) ||
					 (sing.get(z+1,x-1,y)>0) ||
					 (sing.get(z+1,x,y-1)>0) ||
					 (sing.get(z+1,x+1,y+1)>0)||
					 (sing.get(z+1,x-1,y-1)>0)||
					 (sing.get(z+1,x-1,y+1)>0)||
					 (sing.get(z+1,x+1,y-1)>0)) {
					   sing.set(z,x,y,1001);
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
		for (int x=1;x<xdim-1;x++){
			for (int y=1;y<ydim-1;y++){
				if (sing.get(z,x,y)<1001) sing.set(z,x,y,0);

					if ((sing.get(z,x+1,y)>1000) ||
					 (sing.get(z,x,y+1)>1000) ||
					 (sing.get(z,x-1,y)>1000) ||
					 (sing.get(z,x,y-1)>1000) ||
					 (sing.get(z,x+1,y+1)>1000)||
					 (sing.get(z,x-1,y-1)>1000)||
					 (sing.get(z,x-1,y+1)>1000)||
					 (sing.get(z,x+1,y-1)>1000)) {
					   sing.set(z,x,y,0);
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
		 double v= dat.get(z,x,y);
		 double c= dat.get(z,x,y+offset);
	     double p=1;
	     p=Math.atan2(c,v);


		 dat.set(z,x,y,p*1000);
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






public void processInSpace(){
update_undo();
double[] tmp=null;

	for (int z=0;z<zdim;z++){
		ProgressDisplayer.displayProgress(((double)z)/((double)zdim));

		//extract the frame as a 2darray, pad it
		for (int x=0;x<xdim;x++){
			for (int y=0;y<ydim;y++){
				//extract the subarray
				int index=0;
				tmp=new double[10];
				  for (int i=-1;i<=1;i++){
					  for (int j=-1;j<=1;j++){
					   if (((x+i)>=0)&&((x+i)<xdim)&&((y+j)>=0)&&((y+j)<ydim)){

						   double val=dat.get(z,x+i,y+j);
						   tmp[index]=val;
						   //System.out.print(tmp[index]+" ");
						   index++;
						   if (index>9) System.out.println("index="+index+" (i,j)="+i+" "+j+" (z,x,y)="+z+" "+x+" "+y);
					   		}
					  }
				  }
				  //tmp is now populated to index
				  tmp=JSci.maths.ArrayMath.extract(0,index,tmp);
				  frame.set(x,y,JSci.maths.ArrayMath.median(tmp));
			}
		}
		for (int x=0;x<xdim;x++){
			for (int y=0;y<ydim;y++){
					dat.set(z,x,y,frame.get(x,y));
				}
			}
	}
}

public void suppressByTrace(double threshold, double percent){
	update_undo();
	for (int i=0;i<xdim;i++){
			for (int j=0;j<ydim;j++){
			double over=0;
			double under=0;
			double[] tmp=dat.section(i,j);
			for (int x=0;x<tmp.length;x++){
				if (tmp[x]>threshold) over++; else under++;
			}
			if( (100*(over/under))<percent)
			   for (int x=0;x<tmp.length;x++) tmp[x]=0;
			for (int z=0;z<zdim;z++)dat.set(z,i,j,tmp[z]);

		}
	}
}




public void suppress(int y ,int x){
	for (int z=0;z<zdim;z++) dat.set(z,y,x,0);
}

public void suppress(int[][] arr){
	for (int n=0;n<arr.length;n++){
		suppress(arr[n][0],arr[n][1]);
	}
}

public void processByTrace(String type){
	processByTrace(type,0,0.0,null,null);
}

public void processByTrace(String type, int arg){
	processByTrace(type,arg,0.0,null,null);
}

public void processByTrace(String type,int[]roi){
	processByTrace(type,0,0.0,roi,null);
}

public void processByTrace(String type,int arg, int[]roi){
	processByTrace(type,arg,0.0,roi,null);
}


public void processByTrace(String type, int[] roi, function f){
    processByTrace(type,0,0.0,roi,f);
}

public void processByTrace(String type, function f){
	processByTrace(type,0,0.0,null,f);

}


public void threePointMedianByTrace(){
double[] tmp=new double[zdim];
double a,b,c;

	for (int i=0;i<xdim;i++){
 			for (int j=0;j<ydim;j++){
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

	for (int i=0;i<xdim;i++){
 			for (int j=0;j<ydim;j++){
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
				intensity+=dat.get(z,x,y);
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
					dat.set(z,x,y,dat.get(z,x,y)-aveintensity[z]);
					}
				}

		}
  aveintensity=null;
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


doubleArray3D arr2d;
public int[] fillblob3(int zstart, int y, int x, double empty, double blobnum, boolean checkforcontraction, boolean usethreshold, double thresholdxy, double thresholdz){
	//
	int blobsize=0;

//	if (arr2d==null) arr2d=new doubleArray3D(ydim,xdim);
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
			if (usethreshold) newblob=
			 newblob=dat.fill2DThresh(z+1,y,x,empty,blobnum,thresholdz,1);
			else newblob=dat.fill2D(z+1,y,x,empty,blobnum);
			if (checkforcontraction){
			 if (newblob<=2)
			   blobsize+=newblob;
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
    System.out.print("blob starts="+zstart+" duration="+(z-zstart));
    blobinfo[1]=blobsize;
    blobinfo[2]=zstart;
    blobinfo[3]=z-zstart;
    blobinfo[4]=x;
    blobinfo[5]=y;
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
int sum=0;
Vector v=new Vector();
for (int z=1;z<zdim-1;z++){
	for (int y=0;y<ydim;y++){
		for (int x=0;x<xdim;x++){
		  double blnum=dat.get(z,y,x);
		  double newblnum=Math.random()*blnum*1000;
		  if (blnum!=-1){
			  if (dat.countNeighbours(z,y, x, 1, blnum)<3){
			   if (dat.get(z-1,y,x)==-1){
				 if (dat.countNeighbours(z+1,y,x,1,blnum)>1){
		           sum++;

		           //int[] exp_size=fillblob3(z,y,x,1,1,blnum,newblnum,1,false,false,0);
			       //System.out.println("expansion at ("+z+","+y+","+x+") size="+exp_size[0]);
			 //      v.add(exp_size);
			       }
		         }

		     }
		  }

		}
	}
  }


  Hashtable hash=new Hashtable(100);
  for (int i=0;i<v.size();i++){
  	int []tmp=(int[])v.elementAt(i);


  	Integer key=new Integer(tmp[1]);
      if (hash.containsKey(key)){
  		int val=((Integer)(hash.get(key))).intValue();
  		hash.put(key,new Integer(val+1));
  	}else{
  		hash.put(key,new Integer(1));
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




   return dist;

}

public double[][] BlobCount2(double binarythreshold,int maxsizestart, int minduration, int rz, boolean do_undo, int minblobsize, boolean checkforcontraction, boolean usethreshold, double thresholdxy, double thresholdz){
//count blobs
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
	ProgressDisplayer.displayProgress(((double)z)/((double)zdim));
	 for (int y=0;y<ydim;y++){
		 for (int x=0;x<xdim;x++){
          if (dat.get(z,y,x)==0) {
			  //public int[] fillblob3(int z, int y, int x, double empty, double blobnum, boolean checkforcontraction, boolean usethreshold, double threshold){

			  int[] bsize=fillblob3(z,y,x,0,(double)blobnum++,checkforcontraction,usethreshold,thresholdxy, thresholdz);
              //int[] bsize=fillblob(z,y,x,(double)blobnum++);
              v.add(bsize);
             // System.out.println(" blob num="+blobnum+" size="+bsize);

		     }// if
    	   }//x
        }//y
   }//z

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
	ProgressDisplayer.displayProgress(((double)z)/((double)zdim));
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
		 if (dat.get(z,x,y)>thr) ca.set(z+1,x,y,R);
		 if (dat.get(z+1,x,y)>thr) ca.set(z+1,x,y,1);
	   }
      }
 }

dat=ca;
}


public Matrix VolumeFilter(double thr, double thrtotal, double thrback, double thrfront){
	Matrix res=new Matrix();
	res.create(zdim,xdim,ydim);
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
			double presentval=dat.get(z,x,y);
			if (presentval==0){

			 totalval=0;
			 for (int i=-1;i<=1;i++){
			   for (int j=-1;j<=1;j++){
				  double v=dat.get(z,x+i,y+j);
				  if ((v>0)&&(v<=A)) totalval+=1;
				 }//i
		 	   }//j
		     if (totalval>thr){
				 ca1.set(x,y,A+1);
			  }


		     }

			 //if (dat.get(z,x,y)>thr) ca.set(z+1,x,y,R);
			 //if (dat.get(z+1,x,y)>thr) ca.set(z+1,x,y,1);
		   }
	     }
	   for (int x=0;x<xdim;x++){
		  for (int y=0;y<ydim;y++){
	       	double presentval=dat.get(z,x,y);
            if (presentval>0) presentval+=1;
            if (presentval>=A+R) presentval=0;
            if ((presentval==0)&&(ca1.get(x,y)==1)) presentval=A+1;
            if (dat.get(z+1,x,y)==0) dat.set(z+1,x,y,presentval);
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
	ProgressDisplayer.displayProgress(((double)z)/((double)zdim));

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
			 if ((z==110)&&(x==20)&&(y>20)&&(y<25)) System.out.println("iterate="+f+" thr="+totalval/25);
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
						dat.set(z+1,x,y,attenuation*dat.get(z+1,x,y));
						//totalreduced++;
						}
				}
			}
}
//System.out.println("total reduced="+totalreduced+" from "+xdim*ydim);



}//z

report="Ave activated="+(total_active_firststep/(double)zdim)+" ave expanded="+(total_expanded_secondstep/(double)zdim)+" ave reduced="+(total_reduced_thirdstep/(double)zdim);
System.out.println(report);

ca1=null;
ca2=null;
aveintensity=null;
return report;
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
    ProgressDisplayer.displayProgress(((double)z)/((double)zdim));
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
 return nm;
}


public void processByTrace(String type, int arg1, double arg2, int[] roi, function f){
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

	else System.out.println("Warning, the command wasn't found in processByTrace()");
    int [] arr=null;
    if (roi==null){
	   arr=new int[xdim*ydim];

	   for (int i=0;i<xdim*ydim;i++) arr[i]=i;
	   //perform op on whole field
	} else{
	 arr=roi;
    }
//	for (int i=0;i<xdim;i++){
//		if (VERBOSE)System.out.println("processbytrace line="+i+" of "+xdim);
//		for (int j=0;j<ydim;j++){
//			if (VERBOSE)System.out.println("processbytrace line="+i+","+j+" of "+xdim);
			//tmp=dat.section(zrange,i,j).toJava();

 for (int kk=0;kk<arr.length;kk++){
	ProgressDisplayer.displayProgress(((double)kk)/((double)arr.length));

	 //unpack the number
	int  j=(int)((double)arr[kk]/xdim);
	int  i=(int)((double)arr[kk]%xdim);
			tmp=dat.section(i,j);
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
			  default:
			  		new IllegalArgumentException();
			} //end switch
			for (int z=0;z<zdim;z++)dat.set(z,i,j,tmp[z]);
			tmp=null;
		}
			//System.gc();
		//}//jj
         //System.gc();
	  //}//ii


	}




	public double[] getDoubles(int x, int y){
		return dat.section(x,y);
	}

	public double[] getDoubles(int x, int y, int start, int end){
		double[] tmp=new double[end-start];
		for (int i=start;i<end;i++){
			tmp[i-start]=dat.get(i,x,y);
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
    public GeneralPath getGeneralPath(int x, int y, int start, int end, float width, float height){
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
			 c=-1*dat.get((int)i,x,y);
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


	public Trace getTrace(int x, int y){
	//	doubleArray1D fa=dat.section(x,y);
     int[] tmp=new int[zdim];
	   for (int i=0;i<zdim;i++){
	 		tmp[i]=(int)dat.get(i,x,y);
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
					dat.set(z,x,y,(z*zdim+y*ydim+x));
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
ma.zdim=3;
ma.xdim=10;
ma.ydim=10;
ma.dat=new doubleArray3D(5,10,10);
	for (int i=0;i<ma.dat.size;i++) ma.dat.setIndex(i,1);
	for (int x=4;x<=6;x++){
		for (int y=4;y<=6;y++){
		  ma.dat.set(0,y,x,0);
		  ma.dat.set(1,y,x,0);
		  ma.dat.set(2,y,x,0);
		}
	}
	ma.dat.set(1,7,7,0);
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

//System.out.println(ma.dat.print(0,3,0,10,0,10));
int[] bsize=ma.fillblob3(0,5,5,0,5.0,false,true,2,2);

System.out.println(ma.dat.print(0,3,0,10,0,10));



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







