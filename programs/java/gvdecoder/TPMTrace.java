package gvdecoder;
import java.util.*;
/*
for each frame, grab pixels in a nxn region based on a 2D array of times
   000111222
   000111222
   000111222
   333444555
   333444555
   333444555
   666777888
   666777888
   666777888

   returns array with sum of all 0s, followed by sum of all 1s etc

*/
public class TPMTrace{
  Viewer2 vw;
  int[][] ys; // this is the sequence of exposures

  public TPMTrace(Viewer2 vw){
	  this.vw=vw;

  }

  public void setViewer(Viewer2 vw){
	  this.vw=vw;
  }

  /*
  def genarray(N,M):

   ys=[]
   for y in range(N*M):
    ys.append([0]*(N*M))
   count=-1
   for i in range(0,N*M,M):
    for j in range(0,N*M,M):
     count=count+1
     #w.print(("%s %s %s\n"%(i,j,count)))
     for y in range(i,i+M):
      for x in range(j,j+M):
       #w.print(("  %s %s %s"%(y,x,count)))
       ys[y][x]=count
       #w.print(ys)

     #w.print(ys)
 return ys

  */
  public int[][] genArray(int N, int M){
	  ys=new int[N*M][N*M];
	  int count=-1;
	  for (int i=0;i<N*M;i+=M){
		  for (int j=0;j<N*M;j+=M){
			  count++;
			  for (int y=i;y<i+M;y++){
				  for (int x=j;x<j+M;x++)
					  ys[y][x]=count;
				  }
			  }
		  }
	return ys;
  }

  public double[] decode(int startf,int endf, int stx, int sty, int maxtime, int[][] arr){
    double[] res=new double[maxtime*(endf-startf)];
    int index;
	int y,x,val;
	for (int f=startf; f<endf;f++){
	vw.JumpToFrame(f);
	for (int i=0;i<arr.length;i++){
			 for (int j=0;j<arr[0].length;j++){
				y=sty+i;
				x=stx+j;
				index=y*vw.X_dim+x;
				val=arr[i][j];
				if ((val>=0)&&(val<maxtime))
				 res[(f-startf)*maxtime+val]+=vw.viewArray[index];
			 }
	 }
	}
	return res;
  }

  public double[] decode(int frame, int stx, int sty, int maxtime, int[][] arr){
	 double[] res=new double[maxtime];
	 int index;
	 int y,x,val;
	 vw.JumpToFrame(frame);

	 for (int i=0;i<arr.length;i++){
		 for (int j=0;j<arr[0].length;j++){
				y=sty+i;
				x=stx+j;
				index=y*vw.X_dim+x;
				val=arr[i][j];
				if ((val>=0)&&(val<maxtime))
				 res[val]+=vw.viewArray[index];
			 }
	 }
   return res;
  }

  public void decode(int frame, Matrix ma, int stepx, int stepy, int maxtime, int[][] arr){
	  vw.JumpToFrame(frame);
	  int mx,my,x,y,val,index;
	  for (int yy=0;yy<1000;yy+=stepy){
		  for (int xx=0;xx<1000;xx+=stepx){
			 mx=(int)((double)xx/stepx);
			 my=(int)((double)yy/stepy);
	 		 for (int i=0;i<arr.length;i++){
	 	 		 for (int j=0;j<arr[0].length;j++){
	  				y=yy+i;
	  				x=xx+j;
	  				index=y*vw.X_dim+x;
	  				val=arr[i][j];
	  				if ((val>=0)&&(val<maxtime))
	  				 ma.dat.set(val,my,mx,ma.dat.get(val,my,mx)+vw.viewArray[index]);

	  			 }
	 		}
 		}
 	}
  }

  public  int dvdtmax(int frame,int stx, int sty, int maxtime, int[][]arr){

	 double[] res=decode(frame,stx,sty,maxtime,arr);
	 double max=res[res.length-1];
	 double min=res[0];
	 double dvdtmax=0;
	 int dvdtmaxindex=-1;
	 for (int i=1;i<res.length;i++){
		 double dvdt=res[i]-res[0];
	     if (dvdt<0) dvdt=dvdt*-1;
	     if (dvdt>dvdtmax){
			 dvdtmax=dvdt;
		     dvdtmaxindex=i-1;
		 }

	 }
	/*
	int xdim=arr.length;
	int xloc=dvdtmaxindex%xdim;
	int yloc=(dvdtmaxindex-xloc)/xdim;
	return new java.awt.Point(xloc,yloc);
    */
    return dvdtmaxindex;
  }

}