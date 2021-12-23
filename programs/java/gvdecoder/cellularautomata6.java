package gvdecoder;
import java.lang.Math;
import java.util.Arrays;
import java.util.Comparator;

public class cellularautomata6{


	cell[] cellst0;


	int xdim;
	int ydim;

	double maxr;
	int active=2;
	int refractory=3;

	public int index(int y, int x){return y*xdim+x;}

	public double distance(int i1, int i2){
		return Math.sqrt((cellst0[i1].y-cellst0[i2].y)*(cellst0[i1].y-cellst0[i2].y)+(cellst0[i1].x-cellst0[i2].x)*(cellst0[i1].x-cellst0[i2].x));
	}


	public void setup(int xdim, int ydim, double maxr){
	  this.xdim=xdim;
	  this.ydim=ydim;
	  this.maxr=maxr;
	  cellcomp compare=new cellcomp();

	  cellst0=new cell[xdim*ydim];

	  for (int y=0;y<ydim;y++){
		  for (int x=0;x<xdim;x++){
			  cellst0[index(y,x)]=new cell(y+Math.random()-0.5, x+Math.random()-0.5);
		  }
	  }
	  int maxw=(int)(maxr+1);
	  int nbspan=maxw*maxw*4;

	  for (int y=0;y<ydim;y++){
		  for (int x=0;x<xdim;x++){
			  cell c=cellst0[index(y,x)];
			  c.nbs=new nb[nbspan];
			  c.nbcount=0;
			  for (int yy=y-maxw;yy<=y+maxw;yy++){
				for (int xx=x-maxw;xx<=x+maxw;xx++){
				  if ((yy>=0)&&(yy<ydim)&&(xx>=0)&&(xx<xdim)){
				    cell nb=cellst0[index(yy,xx)];
				    double d=distance(index(y,x),index(yy,xx));
				    if (d<maxr) {
						c.nbs[c.nbcount]=new nb(index(yy,xx),d);
				        c.nbcount++;
					}
				  }//if
			  }//xx
		  }//yy
		  nb[] tmp=new nb[c.nbcount];
		  for (int p=0;p<c.nbcount;p++) tmp[p]=c.nbs[p];
		  c.nbs=new nb[c.nbcount];
		  for (int p=0;p<c.nbcount;p++) c.nbs[p]=tmp[p];
		  Arrays.sort(c.nbs,compare);

	  } //x
    } //y

   }//setup

   public void setradius(double r){
	   for (int p=0;p<xdim*ydim;p++){
		   cell c=cellst0[p];
		   for (int k=0;k<c.nbs.length;k++){
			   if (c.nbs[k].d>r) {
				   c.nbcount=k;
				   break;
			   }
		   }
	   }
   } //setradius

   public void iterate(double thr){
	double a,r;
	for (int p=0;p<xdim*ydim;p++){
		a=0;
		r=0;
		cell c=cellst0[p];
		if ((c.state>0)&&(c.state<=active+refractory)) c.nextstate=c.state+1;
		else if (c.state>active+refractory) c.nextstate=0;
		else{
			for (int k=1;k<c.nbcount;k++){
			 cell nbc=cellst0[c.nbs[k].i];
			 if ((nbc.state>0)&&(nbc.state<=active)) a++; else r++;
			}
			if (a/r>thr) c.nextstate=1;
		}


		}
	for (int p=0;p<xdim*ydim;p++){cellst0[p].state=cellst0[p].nextstate;}


   }

   public void copy(Matrix ma){
	for (int p=0;p<xdim*ydim;p++){
		cell c=cellst0[p];
		if (c.state==0) ma.dat.arr[p]=0;
		else ma.dat.arr[p]=active+refractory-c.state+1;

     }
  }

}

class cell{
	double x;
	double y;
	int state;
	int nextstate;
	nb[] nbs;
	int nbcount;
	public cell(double y, double x){
      this.y=y; this.x=x;
	}
}

class nb{
	int i; //index
	double d; //distance;
	public nb(int i, double d){this.i=i; this.d=d;}
}

class cellcomp implements Comparator<nb> {
	public int compare(nb c1,nb c2){

	 if (c1==null) return 1;
	 if (c2==null) return 1;
	 if	(c1.d<c2.d) return -1;
	 if (c1.d>c2.d) return 1;
	 return 0;
   }
}