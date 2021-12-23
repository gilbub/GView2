package gvdecoder;
import java.util.Vector;

public class ClusterAnalysis{

Matrix ma;
Matrix blobs;
int zdim;
int ydim;
int xdim;

int count;
int lx,hx,ly,hy;

int boundschangedtimes=0;
int blobsize=0;
int blobcount=0;


public ClusterAnalysis(Matrix ma){
 this.ma=ma;
 blobs=new Matrix(ma.zdim,ma.ydim,ma.xdim);
 zdim=ma.zdim;
 ydim=ma.ydim;
 xdim=ma.xdim;
 blobcount=0;
 }

public void reset(){
	boundschangedtimes=0;
	blobsize=0;
	count=0;
	blobs.dat.constant_multiply(0);
	blobcount=0;
}

public int countOverThreshold(int zc, int yc, int xc, int r, double threshold){
  count=0;
  for (int y=yc-r;y<=yc+r;y++){
   for (int x=xc-r;x<=xc+r;x++){
    if ((ma.dat.get(zc,y,x)>threshold)) count++;
    }
    }
   //System.out.println("z="+zc+" y="+yc+" x="+xc+" count="+count);
   return count;

}

public boolean resetbounds(int yp, int xp){
 boolean boundschanged=false;
  if ((xp+1>=hx)&&(xp+1<ma.xdim)) {hx=xp+1; boundschanged=true;}
  if ((xp-1< lx)&&(xp-1>0))       {lx=xp-1; boundschanged=true;}
  if ((yp+1>=hy)&&(yp+1<ma.ydim)) {hy=yp+1; boundschanged=true;}
  if ((yp-1< ly)&&(yp-1>0))       {ly=yp-1; boundschanged=true;}
  return boundschanged;
}

public void findClusterInPlane(int zstart, int sy, int sx, int r, double threshold, int minactivenbs, int blobnumber){
 lx=sx-1;
 ly=sy-1;
 hx=sx+1;
 hy=sy+1;
 boundschangedtimes=0;
 blobsize=0;

 boolean boundschanged=resetbounds(sy,sx);
  while (boundschanged){
    boundschanged=false;
    int mly=ly;
    int mhy=hy;
    int mlx=lx;
    int mhx=hx;

    for (int y=mly;y<=mhy;y++){
     for (int x=mlx;x<=mhx;x++){
      if (blobs.dat.get(zstart,y,x)==0){
       if (countOverThreshold(zstart,y,x,r,threshold)>=minactivenbs){
           blobs.dat.set(zstart,y,x,blobnumber);
           blobsize++;
           if (resetbounds(y,x)) {
			   boundschanged=true;
			   boundschangedtimes++;
			   //System.out.println("inplane bounds="+lx+","+ly+","+hx+","+hy);

		   }
      }
     }
    }
  }
  }//while

 }



 public Vector findClusterStartPoints(int z, int r, double threshold, int minactivenbs, int blobnumber){
	if (z>=ma.zdim) return null;
	Vector v=new java.util.Vector();
	for (int x=0;x<ma.xdim;x++){
		for (int y=0;y<ma.ydim;y++){
			if (blobs.dat.get(z,y,x)==blobnumber){
				//check one plane up
				if ((blobs.dat.get(z+1,y,x)==0)&&(countOverThreshold(z+1,y,x,r,threshold)>=minactivenbs)){
					v.add(new location(y,x));
				}
       }
    }
   }
   if (v.size()==0) return null; else return v;
  }


public int findClusterNextPlane(int z, Vector v, int r, double threshold, int minactivenbs, int blobnumber){
  if (z>=ma.zdim) return 0;
  int totblobsize=0;
  for (int i=0;i<v.size();i++){
	  location l=(location)v.elementAt(i);
	  findClusterInPlane(z+1,l.y,l.x,r,threshold,minactivenbs,blobnumber);
	  totblobsize+=blobsize;
  }
  return totblobsize;
}




public blobinfo findCluster(int z, int y, int x, int r, int rz, double threshold, int minactivenbs, int blobnumber){
	//findClusterInPlane(int zstart, int sy, int sx, int r, double threshold, int minactivenbs, int blobnumber)
  blobinfo inf=new blobinfo(0,blobnumber,0);
  Vector v;
  int inplaneblobsize=0;
  //System.out.println("first call");

  findClusterInPlane(z,y,x,r,threshold,minactivenbs,blobnumber);
  inf.size+=blobsize;
  //System.out.println("second call  ");
  if (blobsize>0){
	  do{
        inplaneblobsize=0;
	 	inf.duration+=1;

	 	v=findClusterStartPoints(z,rz,threshold,minactivenbs,blobnumber);
	 	if ((v!=null)&&(v.size()>0)){
		 inplaneblobsize=findClusterNextPlane(z,v,r,threshold,minactivenbs,blobnumber);
	 	}
	    inf.size+=inplaneblobsize;
	    z++;
	    System.out.println("z="+z+"inf.size="+inf.size);
    }while(inplaneblobsize>0);

  }
  return inf;
 }

 public blobinfo findCluster(int z, int y, int x, double threshold, int mode){
   int inplaneradius=0;  //x,y
   int outplaneradius=1; //z
   int activenbs=1;
   if (mode==0){
   //local in plane fill (only units over threshold counted), join clusters at different levels only if they overlap
    inplaneradius=0;
    outplaneradius=1;
    activenbs=1;
   }
   if (mode==1){
	//wide in plane fill (checks nbhood), join clusters at different levels only if they overlap
    inplaneradius=1;
    outplaneradius=1;
    activenbs=3;
   }
   if (mode==3){
	//wide in plane fill (checks nbhood), join clusters at different levels within 2 units
	 inplaneradius=1;
	 outplaneradius=3;
	 activenbs=3;
   }
   if (mode==4){
	 //local in plane fill (only units over threshold counted), join clusters at different levels within 2 units
	 inplaneradius=0;
	 outplaneradius=3;
	 activenbs=1;
   }
   //System.out.println("before");
   blobinfo inf=findCluster(z,y,x,inplaneradius,outplaneradius,threshold,activenbs,++blobcount);
   //ma.undo();
   return inf;
 }



}



class blobinfo{
 int size;
 int number;
 int duration;
 public blobinfo(int size, int number, int duration){
	 this.size=size;
	 this.number=number;
	 this.duration=duration;
     }
}


class location{
	int y;
	int x;
	public location(int y, int x){ this.y=y; this.x=x;}

}


