package gvdecoder;
import java.lang.Math;
import java.util.Arrays;
import java.util.Comparator;

public class cellularautomatamatlab{


	cell[] cells;
    boolean verbose=false;
    int[][] matlabstate;

	int xdim;
	int ydim;

	double maxradius;
	//int active=2;
	//int refractory=3;

	public int index(int y, int x){return y*xdim+x;}

	public double distance(int i1, int i2){
		return Math.sqrt((cells[i1].y-cells[i2].y)*(cells[i1].y-cells[i2].y)+(cells[i1].x-cells[i2].x)*(cells[i1].x-cells[i2].x));
	}


	public void setup(int xdim, int ydim, double maxradius,int active,int refractory){
      setup(xdim,ydim,maxradius,active,refractory,1.0);
    }



	public void setup(int xdim, int ydim, double maxradius, int active, int refractory, double jitter){
	  this.xdim=xdim;
	  this.ydim=ydim;
	  this.maxradius=maxradius;
	  cellcomp compare=new cellcomp();
      matlabstate=new int[ydim][xdim];
	  cells=new cell[xdim*ydim];

	  for (int y=0;y<ydim;y++){
		  for (int x=0;x<xdim;x++){
			  double yl=y+jitter*Math.random()-jitter/2.0;
			  double xl=x+jitter*Math.random()-jitter/2.0;
			  cells[index(y,x)]=new cell(yl, xl, active, refractory);
		  }
	  }
	  int maxw=(int)(maxradius+jitter);
	  int nbspan=maxw*maxw*4;

	  for (int y=0;y<ydim;y++){
		  if ((verbose)&&(y%10==0)) System.out.println("cellularautomata: y="+y+"of "+ydim);
		  for (int x=0;x<xdim;x++){
			  cell c=cells[index(y,x)];
			  c.nbs=new nb[nbspan];
			  c.nbcount=0;
			  for (int yy=y-maxw;yy<=y+maxw;yy++){
				for (int xx=x-maxw;xx<=x+maxw;xx++){
				  if ((yy>=0)&&(yy<ydim)&&(xx>=0)&&(xx<xdim)){
				    cell nb=cells[index(yy,xx)];
				    double d=distance(index(y,x),index(yy,xx));
				    if (d<maxradius) {
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

/*
   public void setValueFromViewer(Viewer2 vw, int framenumber, double threshold, int binlevel, int val, boolean overwritenonzero){
	   vw.JumpToFrame(framenumber);
	   for (int y=0;y<vw.Y_dim;y+=binlevel){
		   for (int x=0;x<vw.X_dim;x+=binlevel){
			     //for (int yy=0;yy<binlevel;yy++){
			     int xx=0;
			     int yy=0;
			     // for (int xx=0;xx<binlevel;xx++){
					if ((y+yy<vw.Y_dim)&&(x+xx<vw.X_dim)){
	                if (overwritenonzero){
	                   if (vw.getPixel(framenumber,y+yy,x+xx)>threshold) cells[index((int)(y/binlevel),(int)(x/binlevel))].state=val;
				     }else{
						 if ((cells[index((int)(y/binlevel),(int)(x/binlevel))].state==0) && (vw.getPixel(framenumber,y+yy,x+xx)>threshold)) cells[index((int)(y/binlevel),(int)(x/binlevel))].state=val;
					 }
				 //}
				//}
			}
       }
    }
   }


  public int checkViewer(Viewer2 vw, int framenumber, int radius, int iteratedstate, int numthreshold, double threshold){
	   vw.JumpToFrame(framenumber);
	   int numsuppressed=0;
	   for (int y=radius;y<ydim-radius;y++){
		   for (int x=radius;x<xdim-radius;x++){
			   int i=index(y,x);
			   if ((cells[i].state<=iteratedstate)&&(cells[i].state>0)){//this is a newly active cell that as has been iterated to on state
			   //check if this cell is close to a viewer active cell
			   int numactive=0;
			   for (int yy=-radius;yy<=radius;yy++){
			    for (int xx=-radius;xx<=radius;xx++){
					if (vw.getPixel(framenumber,y+yy,x+xx)>threshold) numactive+=1;
				}
			   }
			   if (numactive<numthreshold) {cells[i].state=0;numsuppressed+=1;}
		}
	  }
     }
     return numsuppressed;
   }

*/

   public void setstate(int val){
     for (int p=0;p<xdim*ydim;p++){
		   cell c=cells[p];
		   c.state=val;
		   c.nextstate=val;
	   }
   }

/*
   public void setstate(ROI roi,int val){
	   roi.findAllPixels(xdim,ydim);
	   for (int i=0;i<roi.arrs.length;i++){
	      		cell c=cells[roi.arrs[i]];
   		        c.state=val;
   		        c.nextstate=val;
			}
		}
*/
   public void randomactivation(double p){
	   for (int i=0;i<xdim*ydim;i++){
		   cell c=cells[i];
		   if (c.state==0){
		    if (Math.random()<p){
			   c.nextstate=1;
		    }
		}
	}
  }

   public void setstate(int y, int x, int val){
	   cell c=cells[index(y,x)];
	   c.state=val;
	   c.nextstate=val;
   }

   public void setstate(int y, int x, double val){
   	   cell c=cells[index(y,x)];
   	   c.state=(int)val;
   	   c.nextstate=(int)val;
   }

   /* sets the state to the corresponding matrix object.
   m_in doesn't have to have the same dimensions.
   if AR = 0 it does a straight copy, else it inverts (so large numbers are active, small numbers are refractory)
   */
/*
   public void setstate(Matrix m_in, int z, int AR){
	   double xf=m_in.xdim/((double)xdim);
	   double yf=m_in.ydim/((double)ydim);
	   for (int y=0;y<ydim;y++){
	    for (int x=0;x<xdim;x++){
		  int v=(int)(m_in.dat.real_get(z,y*yf,x*xf));
		   if (v==0) setstate(y,x,0);
		   else
		   v=java.lang.Math.abs(AR-v);
		   setstate(y,x,v);
	   }
   }
}

*/

public int[][] getmatlabstate(){
	  for (int y=0;y<ydim;y++){
	    for (int x=0;x<xdim;x++){
			int cv=cells[index(y,x)].state;
		    matlabstate[y][x]=cv;
		}
	}
  return matlabstate;
}

public void setmatlabstate(int[][] st){
     for (int y=0;y<ydim;y++){
	    for (int x=0;x<xdim;x++){
			cells[index(y,x)].state=st[y][x];
		    cells[index(y,x)].nextstate=st[y][x];
		}
	}

}

/* compares the active wavefront (state==1) to a matrix object where active == AR.
   returns a score that reflects how close the fit is
 */
/*
public double score(Matrix m_in, int z, int AR){
	 double good=0;
	 double bad=1;
	 double xf=m_in.xdim/((double)xdim);
	 double yf=m_in.ydim/((double)ydim);
	 for (int y=0;y<ydim;y++){
	    for (int x=0;x<xdim;x++){
	      int mv=(int)(m_in.dat.real_get(z,y*yf,x*xf));
	      int cv=cells[index(y,x)].state;
	      if ((mv==AR)&&(cv==1)) good+=1;
	      if ((mv==AR)&&(cv!=1)) bad+=1;
	      if ((mv!=AR)&&(cv==1)) bad+=1;
	  }

}

return good/bad;
}

*/



   public void setradius(double r){
	   for (int p=0;p<xdim*ydim;p++){
		   cell c=cells[p];
		   for (int k=0;k<c.nbs.length;k++){
			   if (c.nbs[k].d>r) {
				   c.nbcount=k;
				   break;
			   }
		   }
	   }
   } //setradius

/*
   public void setradius(double r,ROI roi){
	roi.findAllPixels(xdim,ydim);
	for (int i=0;i<roi.arrs.length;i++){
		cell c=cells[roi.arrs[i]];
		for (int k=0;k<c.nbs.length;k++){
					   if (c.nbs[k].d>r) {
						   c.nbcount=k;
						   break;
					   }
		   }


      }
   }



   public void setAandR(int a, int r,ROI roi){
   	roi.findAllPixels(xdim,ydim);
   	for (int i=0;i<roi.arrs.length;i++){
   		cell c=cells[roi.arrs[i]];
   		c.refractory=r;
   		c.active=a;
         }
      }

*/
  public void setAandR(int a, int r){

     	for (int i=0;i<xdim*ydim;i++){
     		cell c=cells[i];
     		c.refractory=r;
     		c.active=a;
           }
      }

   public void iterate(double thr){
	double a,r;
	for (int p=0;p<xdim*ydim;p++){
		a=0;
		r=0;
		cell c=cells[p];
		if ((c.state>0)&&(c.state<=c.active+c.refractory)) c.nextstate=c.state+1;
		else if (c.state>c.active+c.refractory) c.nextstate=0;
		else{
			for (int k=1;k<c.nbcount;k++){
			 cell nbc=cells[c.nbs[k].i];
			 if ((nbc.state>0)&&(nbc.state<=c.active)) a++; else r++;
			}
			if ((a/(a+r))>thr) c.nextstate=1;
		}


		}
	for (int p=0;p<xdim*ydim;p++){cells[p].state=cells[p].nextstate;}

   }

public void iterate(double thr, double noise){
	double a,r;
	thr=thr-noise/2.0;
	for (int p=0;p<xdim*ydim;p++){
		a=0;
		r=0;
		cell c=cells[p];
		if ((c.state>0)&&(c.state<=c.active+c.refractory)) c.nextstate=c.state+1;
		else if (c.state>c.active+c.refractory) c.nextstate=0;
		else{
			for (int k=1;k<c.nbcount;k++){
			 cell nbc=cells[c.nbs[k].i];
			 if ((nbc.state>0)&&(nbc.state<=c.active)) a++; else r++;
			}
			if ((a/(a+r))>(thr+(Math.random()*noise))) c.nextstate=1;
		}


		}
	for (int p=0;p<xdim*ydim;p++){cells[p].state=cells[p].nextstate;}

   }



  public void clean(double thr){
	  double a,r;
	  for (int p=0;p<xdim*ydim;p++){
		  a=0;
		  r=0;
		  cell c=cells[p];
		  if ((c.state>0)&&(c.state<=2)){
			 for (int k=1;k<c.nbcount;k++){
			 		cell nbc=cells[c.nbs[k].i];
			 		if ((nbc.state>0)&&(nbc.state<=c.active)) a++; else r++;
		          }
		          if ((a/(a+r))<thr) c.nextstate=0;
		  }
	  }

	for (int p=0;p<xdim*ydim;p++){cells[p].state=cells[p].nextstate;}
   }


/*
  public void copy(Matrix ma){
	for (int p=0;p<xdim*ydim;p++){
		cell c=cells[p];
		if (c.state==0) ma.dat.arr[p]=0;
		else ma.dat.arr[p]=c.active+c.refractory-c.state+1;

     }
  }


 public void copy(Matrix ma, int z){
    	for (int y=0;y<ydim;y++){
		 for (int x=0;x<xdim;x++){
		   cell c=cells[index(y,x)];
		   if (c.state==0) ma.dat.set(z,y,x,0);
		   else ma.dat.set(z,y,x,c.active+c.refractory-c.state+1);

   }    }
}


public void copyimage(Matrix ma, int z){
	int y,x;
	for ( y=0;y<ma.ydim;y++){
	 for ( x=0;x<ma.xdim;x++){
		  ma.dat.set(z,y,x,0);
	  }

  }	for ( y=0;y<ydim;y++){
	  for ( x=0;x<xdim;x++){
		   cell c=cells[index(y,x)];
		   int xl=(int)Math.round(c.x);
		   int yl=(int)Math.round(c.y);
		   int st=c.active+c.refractory-c.state+1;
		   if (c.state==0) ma.dat.set(z,yl,xl,0);
		   else
		   if (ma.dat.get(z,yl,xl)<st) ma.dat.set(z,yl,xl,st);
	   }
 }

}/*/

}
class cell{
	public double x;
	public double y;
	public int state;
	public int nextstate;
	int active;
	int refractory;
	nb[] nbs;
	int nbcount;
	public cell(double y, double x, int active, int refractory){
      this.y=y; this.x=x; this.active=active; this.refractory=refractory;
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