package gvdecoder;
import java.util.ArrayList;

/*
 Ca helper should allow the definition of a CA where each element has its own 'r' and 'th' parameter.
 Activation is defined by th=(active)/(excitable)
 Cells are counted via the use of a mask.
 An input array is for raw experimental data, where active cells are compared to CA cells to initialize the CA and to compare its output to the next frame.
 The ca looks at 3 frames in succession
   F(t-1) defines (likely) Refractory cells
   F(t) defines (likely) active  cells
   F(t+1) defines (likely) excited cells

   active cells are iterated N times (n usually =1), and the output related to F(t+1). If they are not consistent with F(t+1) they revert to excitable. Checks are made using a low threshold

   if there are cells that are active in F(t+1) (high threshold) that aren't predicted by the CA


*/

public class CAHelper12{


 double thr;
 double rt;
 double rtp1;
 double tt;
 double ttp1;
 double car;
 double cat;

 public double[][][] distancemasks;
 public int[][][] circlemasks;
 public int numberofmasks=20;
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


 public void setInterpPoints(int y, int x , int y1, int x1, double d1, int y2, int x2, double d2, int y3, int x3, double d3, int y4, int x4, double d4){
   if (interps==null){ interps=new interpSet[ca.ydim][ca.xdim];	 }
   interps[y][x]=new interpSet(y1, x1,  d1, y2,  x2, d2, y3, x3,  d3,  y4,  x4,  d4);

 }

 public void interpolate(Matrix m_in, int z){
	 for (int y=0;y<m_in.ydim;y++){
		 for (int x=0;x<m_in.xdim;x++){
			 if (interps[y][x]!=null){
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




 public CAHelper12(Matrix data, Matrix ca, int A, int R){
  initializemasks();
  this.ca=ca;
  this.data=data;
  this.A=A;
  this.R=R;
  buffer=new Matrix(1,ca.ydim,ca.xdim);
 }

public void initializeCA(int z,int r, double datathr,double nbthr){

  for (int y=0;y<data.ydim;y++){
		for (int x=0;x<data.xdim;x++){
		   if (ca.dat.get(z,y,x)==0){
			ratio(data,z,y,x,r,datathr);
			if (lastratio[0]>nbthr) ca.dat.set(z,y,x,A+R);
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



