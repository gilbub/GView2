package gvdecoder;
import java.util.ArrayList;

/*
 basic premise:
  find all cells in ca that aren't active
    neighbours of these cells can't count these when deciding to be active.
    The program needs a boolean mask that has activateable cells
    allways off cells can become active if alot of their neibours are active (a fill step after the ca is populated with data)


  when deciding if a cell is active based on input data:
    check that at t+1 some parts of the wave have progressed
     require that at t+1, there are less active cells  at x,y that at t
     require that at t+1, there are over th cells a distance R from x,y
     in practice, check r at t and at t+1, ratio active should be greater at t than t+1
               find all active cells at t+1 in 2R.
               find their center of mass
               at xc,yc, find active






*/

public class CAHelper14{


 double thr;
 double rt;
 double rtp1;
 double tt;
 double ttp1;
 double car;
 double cat;

 public double[][][] distancemasks;
 public int[][][] circlemasks;
 public boolean[][] activemask;
 public int numberofmasks=40;
 double[] lastratio=new double[3];
 interpSet[][] interps;

 Matrix ca;
 Matrix data;
 Matrix buffer;
 Matrix params;
 Matrix activetimes;
 int A;
 int R;

 /*count neighbours in data at t with neighbourhood (rt)
   count neighbours a larger region (rtp1) in data at t+1,
   if neighbours(rt)>tt &&  if neighbours(rtp1)>ttp1
    Set ca(i) to E

   iterate:




 */






 public CAHelper14(Matrix data, Matrix ca, int A, int R){
  initializemasks();
  this.ca=ca;
  this.data=data;
  this.A=A;
  this.R=R;
  buffer=new Matrix(1,ca.ydim,ca.xdim);
 }

public void initializeactivationmask(){
	activemask=new boolean[ca.ydim][ca.xdim];
	for (int y=0;y<ca.ydim;y++){
		for (int x=0;x<ca.xdim;x++){
			activemask[y][x]=true;
		}
	}
}

public void setInterpPoints(int y, int x , int y1, int x1, double d1, int y2, int x2, double d2, int y3, int x3, double d3, int y4, int x4, double d4){
   if (interps==null){
	   interps=new interpSet[ca.ydim][ca.xdim];
	   initializeactivationmask();
	   }
   interps[y][x]=new interpSet(y1, x1,  d1, y2,  x2, d2, y3, x3,  d3,  y4,  x4,  d4);
   activemask[y][x]=false;
 }

 public void interpolate(Matrix m_in, int z){
	 for (int y=0;y<m_in.ydim;y++){
		 for (int x=0;x<m_in.xdim;x++){
			 if ((interps[y][x]!=null)&&(m_in.dat.get(z,y,x)==0)){
				 double v=0;
				 interpSet ins=interps[y][x];
				 v+=m_in.dat.get(z,ins.p1.y,ins.p1.x);
				 v+=m_in.dat.get(z,ins.p2.y,ins.p2.x);
				 v+=m_in.dat.get(z,ins.p3.y,ins.p3.x);
				 v+=m_in.dat.get(z,ins.p4.y,ins.p4.x);
				 m_in.dat.set(z,y,x,v/4.0);

			 }
       }
   }
}



boolean dochecknearbyactive=true;
double reducethresholdby=0.75;
double minshiftdistance=0.5;
int rejectedbychecknearbyactive=0;
public boolean checknearbyactive(int z, int y, int x, int r, double datathr, double nbthr){
	// find center of mass of all active cells in 2r, make sure there are greater than nbthr*0.75 active there and that its at least r/2 distance away from x,y
	if (!dochecknearbyactive) return true;
	int R=2*r;
	int yi,xi;
	double cx=0;
	double cy=0;
	int count=0;
	for (int yy=-R;yy<=R;yy++){
		for (int xx=-R;xx<=R;xx++){
	        yi=yy+y;
	        xi=xx+x;
			if ((yi>=0)&&(yi<data.ydim)&&(xi>=0)&&(xi<data.xdim)){
				if (data.dat.get(z,y,x)>datathr){
	              count++;
	              cx+=x;
	              cy+=y;
				}
			}
		}
	  }
	 cx=cx/count;
	 cy=cy/count;
	 if  (dist(x,y,(int)(cx+0.5),(int)(cy+0.5))>(r*minshiftdistance)){
		 ratio(data,z,(int)(cy+0.5),(int)(cx+0.5),r,datathr);
		 if (lastratio[0]>nbthr*reducethresholdby) return true;

	 }
  rejectedbychecknearbyactive+=1;
  return false;
}

public Matrix activeToMatrix(){
	Matrix mo=new Matrix(1,ca.ydim,ca.xdim);
	for (int y=0;y<ca.ydim;y++){
		for (int x=0;x<ca.xdim;x++){
			if (activemask[y][x]) mo.dat.set(0,y,x,100);
 }
}
return mo;
}


public void initializeCA(int z,int r, double datathr,double nbthr){
  rejectedbychecknearbyactive=0;
  for (int y=0;y<data.ydim;y++){
		for (int x=0;x<data.xdim;x++){
		   if ((ca.dat.get(z,y,x)==0)&&(activemask[y][x])){
			ratio(data,z,y,x,r,datathr);
			if (lastratio[0]>nbthr){
			 if (checknearbyactive(z+1,y,x,r,datathr, nbthr))	ca.dat.set(z,y,x,A+R);

			}

		  }
		}
	}
}

public void copy_up(int z){
	 double zm1,zm;
	 for (int y=0;y<ca.ydim;y++){
		 for (int x=0;x<ca.xdim;x++){
			 zm1=ca.dat.get(z-1,y,x);
			 zm=ca.dat.get(z,y,x);
			 if ((zm==0)&&(zm1>0)){
		       ca.dat.set(z,y,x,zm1-1);
		   }
	   }
   }
}

public void inplane_fill(int zindex,int r,double thr,double nbthr, int setto, double cmdistance){
   buffer.dat.set(0);
   double mthr;
   getCM=true;
   for (int y=0;y<ca.ydim;y++){
	   for (int x=0;x<ca.xdim;x++){
		 if (ca.dat.get(zindex,y,x)==0){
		 if (params!=null)
		   mthr=params.dat.get(0,y,x)*thr;
		 else
		   mthr=thr;
	     ratio(ca,zindex,y,x,r,mthr);

	     if ((lastratio[0]>nbthr)&&(lastratio[2]<cmdistance))
	      buffer.dat.set(0,y,x,setto);
	  }
    }
 }
  for (int y=0;y<ca.ydim;y++){
	  for (int x=0;x<ca.xdim;x++){
		  if (buffer.dat.get(0,y,x)>0) ca.dat.set(zindex,y,x,setto);
	  }
   }
  getCM=false;
}

public void inplane_clean(int z, int r, int value, double thr,int setto){
	for (int y=0;y<ca.ydim;y++){
			for (int x=0;x<ca.xdim;x++){
	           if (ca.dat.get(z,y,x)==value){
				   ratio(ca,z,y,x,r,value);
				   if (lastratio[0]<thr)
				     ca.dat.set(z,y,x,setto);
			   }
		   }
	   }
}


public void iterate(int z, int r, double nbthr){
	for (int y=0;y<ca.ydim;y++){
		for (int x=0;x<ca.xdim;x++){
		 if (ca.dat.get(z,y,x)==0){
			ratio(ca,z,y,x,r,R);
			if (lastratio[1]>nbthr) ca.dat.set(z+1,y,x,R+A);
		  }
		}
	}
}

public void iteratethr(int z, int r, double nbthr,double factor){
	for (int y=0;y<ca.ydim;y++){
		for (int x=0;x<ca.xdim;x++){
		 if (ca.dat.get(z,y,x)==0){
			ratio(ca,z,y,x,r,R);
			if (lastratio[1]>nbthr){
				params.dat.set(0,y,x,factor);
			    if(ca.dat.get(z,y,x)<=R)ca.dat.set(z,y,x,0);
			    }
		  }
		}
	}
}


public boolean getCM=false;
public ArrayList<pair> points=new ArrayList<pair>(128);
public double[] ratio(Matrix ma, int z, int y0, int x0, int r, double thr){
	int circleweight=0;
	double distanceweight=0.0;
	double active=0;
	double inactive=0;
	double wactive=0;
	double winactive=0;
	int yi,xi;
	double val;
	if (getCM) points.clear();

	for (int y=-r;y<=r;y++){
		for (int x=-r;x<=r;x++){
			circleweight=circlemasks[r][y+r][x+r];
			distanceweight=distancemasks[r][y+r][x+r];
			  yi=y0+y;
			  xi=x0+x;
			  if ((yi>=0)&&(yi<ma.ydim)&&(xi>=0)&&(xi<ma.xdim)){
				if (activemask[yi][xi])  {  // check that this cell is capable of showing activity
				  val=ma.dat.get(z,yi,xi);
				  if (circleweight>0){
					  if (val>=thr) {
						  active+=1;
						  if (getCM){
						     points.add(new pair(xi,yi));
						  }
					  }
					  else inactive+=1;
				     }
				  if (val>=thr)
				        wactive+=distanceweight;
				  else winactive+=distanceweight;
			    }//activemask
			  }//in bounds
		}
	}
  if ((active==0)&&(inactive==0)) lastratio[0]=0;
  else
  if ((active>0)&&(inactive==0)) lastratio[0]=1;
  else
  lastratio[0]=active/inactive;

  if ((wactive==0)&&(winactive==0)) lastratio[1]=0;
  else
  if ((wactive>0)&&(winactive==0)) lastratio[1]=1;
  else
  lastratio[1]=wactive/winactive;
  if (getCM){
	  double cx=0;
	  double cy=0;
	  for (pair p: points){
		  cx+=p.x;
		  cy+=p.y;
    	  }
    	 cx=cx/points.size();
    	 cy=cy/points.size();
    lastratio[2]=Math.sqrt( (x0-cx)*(x0-cx)+(y0-cy)*(y0-cy) );

  }
  return lastratio;
}





public double dist(int x1, int y1, int x2, int y2){
 return Math.sqrt( (x2-x1)*(x2-x1)+(y2-y1)*(y2-y1) );
 }

public void initializemasks(){
distancemasks=new double[numberofmasks][][];
circlemasks=new int[numberofmasks][][];
double d;
double v;
for (int n=0;n<numberofmasks;n++){
 distancemasks[n]=new double[2*n+1][2*n+1];
 circlemasks[n]=new int[2*n+1][2*n+1];
 for (int x=-n;x<=n;x++){
  for (int y=-n;y<=n;y++){
    d=dist(0,0,x,y);
    v=0;
    if (d>0) v=1.0/d;
    distancemasks[n][x+n][y+n]=v;
    if (v>=1.0/n) circlemasks[n][x+n][y+n]=1;
    else circlemasks[n][x+n][y+n]=0;
    }//y
   }//x
  }//n
}

class interpSet{
	pair p1;
	double d1;
	pair p2;
	double d2;
	pair p3;
	double d3;
	pair p4;
	double d4;

	public interpSet(int y1, int x1, double d1, int y2, int x2, double d2, int y3, int x3, double d3, int y4, int x4, double d4){
		p1=new pair(x1,y1);
		this.d1=d1;
		p2=new pair(x2,y2);
		this.d2=d2;
		p3=new pair(x3,y3);
		this.d3=d3;
		p4=new pair(x4,y4);
		this.d4=d4;
	}


}

class pair{
	int x;
	int y;
	public pair(int x, int y){
		this.x=x; this.y=y;
	}

}

}



