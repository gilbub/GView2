package gvdecoder;
import java.lang.Math;
import java.util.Arrays;
import java.util.Comparator;

public class cellularautomata1{


	cell[] cells;
    boolean verbose=false;

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
	  this.xdim=xdim;
	  this.ydim=ydim;
	  this.maxradius=maxradius;
	  cellcomp compare=new cellcomp();

	  cells=new cell[xdim*ydim];

	  for (int y=0;y<ydim;y++){
		  for (int x=0;x<xdim;x++){
			  cells[index(y,x)]=new cell(y+Math.random()-0.5, x+Math.random()-0.5, active, refractory);
		  }
	  }
	  int maxw=(int)(maxradius+1);
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



   public void setstate(int val){
     for (int p=0;p<xdim*ydim;p++){
		   cell c=cells[p];
		   c.state=val;
		   c.nextstate=val;
	   }
   }

   public void setstate(int val, ROI roi){
	   roi.findAllPixels(xdim,ydim);
	   for (int i=0;i<roi.arrs.length;i++){
	      		cell c=cells[roi.arrs[i]];
   		        c.state=val;
   		        c.nextstate=val;
			}
		}

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
			if (a/r>thr) c.nextstate=1;
		}


		}
	for (int p=0;p<xdim*ydim;p++){cells[p].state=cells[p].nextstate;}


   }

   public void copy(Matrix ma){
	for (int p=0;p<xdim*ydim;p++){
		cell c=cells[p];
		if (c.state==0) ma.dat.arr[p]=0;
		else ma.dat.arr[p]=c.active+c.refractory-c.state+1;

     }
  }

}

class cell{
	double x;
	double y;
	int state;
	int nextstate;
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