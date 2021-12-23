package gvdecoder.array;

import java.lang.Math;
import gvdecoder.Histogram;

public class doubleArray3D {

public double[] arr;
public int zdim;
public int xdim;
public int ydim;
public int frame;
public int size;
public int index; //dummy

public doubleArray3D(int y, int x){
	arr=new double[y*x];
	zdim=1;
	xdim=x;
	ydim=y;
	frame=x*y;
	size=x*y;
}




public doubleArray3D(int z, int y, int x){
 arr=new double[z*y*x];
 zdim=z;
 xdim=x;
 ydim=y;
 frame=x*y;
 size=frame*z;
}


public doubleArray3D(double[][][] vals){
 zdim=vals.length;
 ydim=vals[0].length;
 xdim=vals[0][0].length;
 arr=new double[zdim*ydim*xdim];
 frame=ydim*xdim;
 size=frame*zdim;
 index=0;
 for (int z=0;z<zdim;z++){
	 for (int y=0;y<ydim;y++){
		 for (int x=0;x<xdim;x++){
	      arr[index]=vals[z][y][x];
		  index++;
		 }
	 }
 }
}

public doubleArray3D(doubleArray3D old){
 //creates a copy
 zdim=old.zdim;
 xdim=old.xdim;
 ydim=old.ydim;
 frame=xdim*ydim;
 size=frame*zdim;
 arr=new double[zdim*ydim*xdim];
 for (int i=0;i<arr.length;i++){
	 arr[i]=old.arr[i];
}
}

/* utility method for getting a 2D array*/
public double[][] get2Darray(int zindex){
	double[][] arr=new double[ydim][xdim];
	for (int y=0;y<ydim;y++){
		for (int x=0;x<xdim;x++){
		 arr[y][x]=get(zindex,y,x);
		}
	}
	return arr;
}


public double[] get1Darray(int zindex){
	double[] arr=new double[ydim*xdim];
	for (int y=0;y<ydim;y++){
		for (int x=0;x<xdim;x++){
		 arr[y*xdim+x]=get(zindex,y,x);
		}
	}
	return arr;
}

public void fill1Darray(int zindex, double[] arr){

	for (int y=0;y<ydim;y++){
		for (int x=0;x<xdim;x++){
		 arr[y*xdim+x]=get(zindex,y,x);
		}
	}

}

public void copyframe(int z, double[] source){
  	for (int y=0;y<ydim;y++){
		for (int x=0;x<xdim;x++){
			arr[z*(xdim*ydim)+y*xdim+x]=source[y*xdim+x];
		}
	}
}

public void copyframe(int z, float[] source){
  	for (int y=0;y<ydim;y++){
		for (int x=0;x<xdim;x++){
			arr[z*(xdim*ydim)+y*xdim+x]=(double)source[y*xdim+x];
		}
	}
}

public void copyframe(int z, int[] source){
  	for (int y=0;y<ydim;y++){
		for (int x=0;x<xdim;x++){
			arr[z*(xdim*ydim)+y*xdim+x]=(double)source[y*xdim+x];
		}
	}
}

public void copyframe(int z, short[] source){
  	for (int y=0;y<ydim;y++){
		for (int x=0;x<xdim;x++){
			arr[z*(xdim*ydim)+y*xdim+x]=(double)source[y*xdim+x];
		}
	}
}



public void copyframe(int z, byte[] source){
  	for (int y=0;y<ydim;y++){
		for (int x=0;x<xdim;x++){
			arr[z*(xdim*ydim)+y*xdim+x]=(double)source[y*xdim+x];
		}
	}
}


public void copysubframe(int z, double[] source, int sx, int sy, int sourcexdim){
  	for (int y=0;y<ydim;y++){
		for (int x=0;x<xdim;x++){
			arr[z*(xdim*ydim)+y*xdim+x]=(double)source[(y+sy)*sourcexdim+(x+sx)];
		}
	}
}

public void copysubframe(int z, float[] source, int sx, int sy, int sourcexdim){
  	for (int y=0;y<ydim;y++){
		for (int x=0;x<xdim;x++){
			arr[z*(xdim*ydim)+y*xdim+x]=(double)source[(y+sy)*sourcexdim+(x+sx)];
		}
	}
}

public void copysubframe(int z, int[] source, int sx, int sy, int sourcexdim){
  	for (int y=0;y<ydim;y++){
		for (int x=0;x<xdim;x++){
			arr[z*(xdim*ydim)+y*xdim+x]=(double)source[(y+sy)*sourcexdim+(x+sx)];
		}
	}
}

public void copysubframe(int z, short[] source, int sx, int sy, int sourcexdim){
  	for (int y=0;y<ydim;y++){
		for (int x=0;x<xdim;x++){
			arr[z*(xdim*ydim)+y*xdim+x]=(double)source[(y+sy)*sourcexdim+(x+sx)];
		}
	}
}

public void copysubframe(int z, byte[] source, int sx, int sy, int sourcexdim){
  	for (int y=0;y<ydim;y++){
		for (int x=0;x<xdim;x++){
			arr[z*(xdim*ydim)+y*xdim+x]=(double)source[(y+sy)*sourcexdim+(x+sx)];
		}
	}
}

public void IJconvert(double val){
 for (int i=0;i<arr.length;i++){
	 if (arr[i]<0) arr[i]=arr[i]+val;
 }
}


public double convolve(int zi,int yi, int xi, doubleArray3D vals){

	int zoff=(int)(((double)vals.zdim)/2.0);
	int yoff=(int)(((double)vals.ydim)/2.0);
    int xoff=(int)(((double)vals.xdim)/2.0);
	double sum=0;
	for (int z=0;z<vals.zdim;z++){
		for (int y=0;y<vals.ydim;y++){
			for (int x=0;x<vals.xdim;x++){
              sum+=get(zi+z-zoff,yi+y-yoff,xi+x-xoff)*vals.get(z,y,x);
			}
		}
	}
  return sum;
}



public void convolve_all(double[][][] vals){
	int valzdim=vals.length;
	int valydim=vals[0].length;
	int valxdim=vals[0][0].length;
	if ((valzdim>zdim)||(valydim>ydim)||(valxdim>xdim)) {
		System.out.println("error in doubleArray3D convolve operation, out of bounds");
		return;
	}else
	if ((valzdim==zdim)&&(valydim==ydim)&&(valxdim==xdim)){
		index=0;
		for (int z=0;z<valzdim;z++){
			for (int y=0;y<valydim;y++){
				for (int x=0;x<valxdim;x++){
				arr[index]=arr[index]*vals[z][y][x];
				index++;
				}//x
			}//y
		}//z
	}//if
	else{
		doubleArray3D kernel=new doubleArray3D(vals);
		doubleArray3D tmp=new doubleArray3D(zdim,ydim,xdim);
		for (int z=0;z<zdim;z++){
			for (int y=0;y<ydim;y++){
				for (int x=0;x<xdim;x++){
				 tmp.set(z,y,x,convolve(z,y,x,kernel));
				}
			}
		}
	 copy(tmp);
	}

}


public void constant_multiply(double val){
	for (int i=0;i<arr.length;i++){
		arr[i]=arr[i]*val;
	}
}


public void constant_add(double val){
	for (int i=0;i<arr.length;i++){
		arr[i]=arr[i]+val;
	}
}


public void frame_subtract(doubleArray3D old){
	for (int z=0;z<zdim;z++){
	 for (int i=0;i<old.arr.length;i++){
		index=z*frame+i;
		arr[index]-=old.arr[i];
	 }
	}
}


public void frame_multiply(doubleArray3D old){
	for (int z=0;z<zdim;z++){
	 for (int i=0;i<old.arr.length;i++){
		index=z*frame+i;
		arr[index]*=old.arr[i];
	 }
	}
}


public void frame_divide(doubleArray3D old){
	for (int z=0;z<zdim;z++){
	 for (int i=0;i<old.arr.length;i++){
		index=z*frame+i;
		if (old.arr[i]!=0)
		 arr[index]= arr[index]/old.arr[i];
	 }
	}
}

public void frame_add(doubleArray3D old){
	for (int z=0;z<zdim;z++){
	 for (int i=0;i<old.arr.length;i++){
		index=z*frame+i;
		arr[index]+=old.arr[i];
	 }
	}
}


public void subtract(doubleArray3D old){
	if (old.arr.length==arr.length){

	for (int i=0;i<arr.length;i++){
		arr[i]=arr[i]-old.arr[i];
	}
  }else System.out.println("doubleArray3D:: the arrays must be the same length to subtract");
}

public void add(doubleArray3D old){
	if (old.arr.length==arr.length){

	for (int i=0;i<arr.length;i++){
		arr[i]=arr[i]+old.arr[i];
	}
  }else System.out.println("doubleArray3D:: the arrays must be the same length to add");
}


public void assign(doubleArray3D old){
	//should do checks here for bounds!!!
	for (int i=0;i<size;i++){
		arr[i]=old.arr[i];
	}
}

public double get(int y, int x){
	return get(0,y,x);
}


public double getIndex(int index){
	if (index<size)

	return arr[index];
	else return 0;
}

public void setIndex(int index,double val){
	if (index<size)

	 arr[index]=val;
}


/*
 if i want to propagate a signal its a waste to look at points not near the signal
 propagate if arr[z,y,x]=num in 2d
  if (z,y,x)=num, set bounds=y-1, y+1, x-1, x+1
  for (y=lowy;y<=hiy;y++){
	  for (x=lowx;x<=hix;x++){
		if ((get(z,y,x)==num) {

		}
	  }
  }
*/


public boolean isNeighbour(int index1, int index2){
	int z1=(int)(index1/frame);
	int y1=(int)((index1-(z1*frame))/xdim);
	int x1=index1-(z1*frame)-y1*xdim;
	int z2=(int)(index2/frame);
	int y2=(int)((index2-(z2*frame))/xdim);
	int x2=index2-(z2*frame)-y2*xdim;
	return ((Math.abs(z2-z1)<=1) &&(Math.abs(y2-y1)<=1) &&(Math.abs(x2-x1)<=1));

}

public double distance(int index1, int index2){
	int z1=(int)(index1/frame);
	int y1=(int)((index1-(z1*frame))/xdim);
	int x1=index1-(z1*frame)-y1*xdim;
	int z2=(int)(index2/frame);
	int y2=(int)((index2-(z2*frame))/xdim);
	int x2=index2-(z2*frame)-y2*xdim;
	return Math.sqrt((z1-z2)*(z1-z2)+(y1-y2)*(y1-y2)+(x1-x2)*(x1-x2));

}

/***
 Shepards algorithm
 def real_get(ma, z, y , x):
     v=[0,0,0,0,0,0,0,0]

     v[0]=ma.dat.get(int(z), int(y), int(x))
     v[1]=ma.dat.get(int(z), int(y), int(x)+1);
     v[2]=ma.dat.get(int(z), int(y)+1,int(x));
     v[3]=ma.dat.get(int(z), int(y)+1, int(x)+1);
     v[4]=ma.dat.get(int(z)+1,int(y), int(x));
     v[5]=ma.dat.get(int(z)+1, int(y), int(x)+1);
     v[6]=ma.dat.get(int(z)+1, int(y)+1,int(x));
     v[7]=ma.dat.get(int(z)+1, int(y)+1, int(x)+1);

     mz2=(z%1)*(z%1);
     my2=(y%1)*(y%1);
     mx2=(x%1)*(x%1);
     mz12=(1-z%1)*(1-z%1);
     my12=(1-y%1)*(1-y%1);
     mx12=(1-x%1)*(1-x%1);

     sqrt3=1.7320508075688772;

     d=[0,0,0,0,0,0,0,0]

     d[0]=Math.sqrt(mz2+my2+mx2);
     d[1]=Math.sqrt(mz2+my2+mx12);
     d[2]=Math.sqrt(mz2+my12+mx2);
     d[3]=Math.sqrt(mz2+my12+mx12);
     d[4]=Math.sqrt(mz12+my2+mx2);
     d[5]=Math.sqrt(mz12+my2+mx12);
     d[6]=Math.sqrt(mz12+my12+mx2);
     d[7]=Math.sqrt(mz12+my12+mx12);

     R=1.7320508075688772
     sumdists=0;
     for i in range(8):
         sumdists+=((R-d[i])/(R*d[i]))**2

 	res=0;
     for i in range(8):
         w=((R-d[i])/(R*d[i]))**2/sumdists;
         res+=w*v[i];
     return res,sumdists,d

***/
public double real_get(double z, double y ,double x){
	if ((z%1==0.0)&&(y%1==0.0)&&(x%1==0.0)) return get((int)z,(int)y,(int)x);

	double[] v=new double[8];
    v[0]=get((int)z, (int)y, (int)x);
	v[1]=get((int)z, (int)y, (int)x+1);
	v[2]=get((int)z, (int)y+1,(int)x);
	v[3]=get((int)z, (int)y+1, (int)x+1);
	v[4]=get((int)z+1,(int)y, (int)x);
	v[5]=get((int)z+1, (int)y, (int)x+1);
	v[6]=get((int)z+1, (int)y+1,(int)x);
	v[7]=get((int)z+1, (int)y+1, (int)x+1);

	double mz2=(z%1)*(z%1);
	double my2=(y%1)*(y%1);
	double mx2=(x%1)*(x%1);
	double mz12=(1-z%1)*(1-z%1);
	double my12=(1-y%1)*(1-y%1);
	double mx12=(1-x%1)*(1-x%1);

	double R=1.7320508075688772;

    double[] d=new double[8];

	d[0]=Math.sqrt(mz2+my2+mx2);
	d[1]=Math.sqrt(mz2+my2+mx12);
	d[2]=Math.sqrt(mz2+my12+mx2);
	d[3]=Math.sqrt(mz2+my12+mx12);
	d[4]=Math.sqrt(mz12+my2+mx2);
	d[5]=Math.sqrt(mz12+my2+mx12);
	d[6]=Math.sqrt(mz12+my12+mx2);
	d[7]=Math.sqrt(mz12+my12+mx12);

    double sumdists=0;
    for (int i=0;i<8;i++){
//		sumdists+=1.0/(d[i]*d[i]);
        sumdists+=Math.pow(((R-d[i])/(R*d[i])),2);
	}

	double res=0;
	for (int i=0;i<8;i++){
	     double w=Math.pow(((R-d[i])/(R*d[i])),2)/sumdists;

	// double w=(1.0/d[i]*d[i])/sumdists;
		res+=w*v[i];
	}

    return res;
}

public double get(int z,int y,int x){
	if (z<0)z=0;
	if (z>=zdim) z=zdim-1;
	if (x<0) x=0;
    if (x>=xdim) x=xdim-1;
    if (y<0) y=0;
    if (y>=ydim) y=ydim-1;
	//index=z*frame+y*xdim+x;
	return arr[z*frame+y*xdim+x];
  }

public void set (int y, int x, double val){
 set (0,y,x,val);
}

/**
Sets a frame to the values in newarr. Doesnt bounds check
**/
public void set (int z,double[] newarr){
 for (int i=0;i<newarr.length;i++){
	 index=z*frame+i;
	 arr[index]=newarr[i];
 }
}

public void set (int y, int x, double[] newarr){
 for (int z=0;z<newarr.length;z++){
   index=z*frame+y*xdim+x;
   arr[index]=newarr[z];
  }
}

public void set (int z, int y, int x, double val){
    index=z*frame+y*xdim+x;
	if ((index<size)&&(index>=0))
	 arr[index]=val;
}


public void set(double val){
	for (int i=0;i<arr.length;i++) arr[i]=val;
}

 public double[] section(int y, int x, int startz, int endz){
	 double[] tmp=new double[endz-startz];
	 	for (int i=startz;i<endz;i++){
	 		tmp[i-startz]=get(i,y,x);
	 	}
	return tmp;
 }




public double[] section(int startz, int endz, int starty, int endy, int startx, int endx){
	double[] tmp=new double[(endz-startz)*(endy-starty)*(startx-endx)];
	int cnt=0;
	for (int z=startz;z<endz;z++){
		for (int y=starty;y<endy;y++){
			for (int x=startx;x<endx;x++){

			  tmp[cnt]=get(z,y,x);
			  cnt++;
			}
		}
	}
  return tmp;
}

public double sumsection(int startz, int endz, int starty, int endy, int startx, int endx){
	double tmp=0;//new double[(endz-startz)*(endy-starty)*(startx-endx)];

	for (int z=startz;z<endz;z++){
		for (int y=starty;y<endy;y++){
			for (int x=startx;x<endx;x++){

			  tmp +=get(z,y,x);

			}
		}
	}
  return tmp;
}

 public double[] section(int y, int x){
	double[] tmp=new double[zdim];
	for (int i=0;i<zdim;i++){
		tmp[i]=get(i,y,x);
	}
	return tmp;
 }



/*NOT CONSISTENT*/
 public double sumsection(int y, int x){
	double tmp=0;
	for (int i=0;i<zdim;i++){
		tmp+=get(i,y,x);
	}
	return tmp;
 }


public double sum(){
	double tmp=0.0;
	for (int i=0;i<size;i++){
		tmp+=arr[i];
	}
	return tmp;
}


public double average(){
	return sum()/size;
}

public double[] section(int z){
	double[] tmp=new double[frame];
	for (int i=0;i<frame;i++){
		tmp[i]=arr[frame*z+i];
	}
	return tmp;
 }


 public String print(int startz, int endz, int starty, int endy, int startx, int endx){
	 String res="";
	 for (int z=startz; z<endz; z++){
		 for (int y=starty;y<endy;y++){
			 for (int x=startx;x<endx;x++){
				res+=" "+get(z,y,x);
			 }
			res+="\n";
		 }
		 res+="\n\n";
	 }
	 return res;
 }

//returns the number of elements filled
int lowx, hix, lowy, hiy, xx,yy;
int lowx_n, hix_n, lowy_n, hiy_n;
boolean newbounds=false;
public int fill2D(int z, int y, int x, double empty, double set_to){
	int numset=0;
	if (x>0) lowx=x-1; else lowx=x;
	if (x<xdim) hix=x+1; else hix=x;
	if (y>0) lowy=y-1; else lowy=y;
	if (y<ydim) hiy=y+1; else hiy=y;
	lowx_n=lowx;
	hix_n=hix;
	lowy_n=lowy;
	hiy_n=hiy;
	do{

	newbounds=false;
	for (xx=lowx;xx<=hix;xx++){
		 for (yy=lowy;yy<=hiy;yy++){
			 if (get(z,yy,xx)==empty){
				  set(z,yy,xx,set_to);
				  //reset the bounds
				   numset++;
			       if (xx+1>hix_n) {hix_n=xx+1; newbounds=true;}
			       if (xx-1<lowx_n){lowx_n=xx-1;newbounds=true;}
			       if (yy+1>hiy_n) {hiy_n=yy+1; newbounds=true;}
			       if (yy-1<lowy_n){lowy_n=yy-1;newbounds=true;}
			      }
		 }
	 }

    lowx=lowx_n;
    hix=hix_n;
    lowy=lowy_n;
    hiy=hiy_n;
   }while (newbounds);
  return numset;
}

public int fill2DThresh(int z, int y, int x, double empty, double set_to, double thresh, int offset){
//offset allows to fill one level based on the data of another, normally set to 0 for 2D
	//System.out.println("in fill2DThresh = ("+z+","+y+","+x);

	int numset=0;
	if (x>0) lowx=x-1; else lowx=x;
	if (x<xdim) hix=x+1; else hix=x;
	if (y>0) lowy=y-1; else lowy=y;
	if (y<ydim) hiy=y+1; else hiy=y;
	lowx_n=lowx;
	hix_n=hix;
	lowy_n=lowy;
	hiy_n=hiy;
	do{
	newbounds=false;
	for (xx=lowx;xx<=hix;xx++){
		 for (yy=lowy;yy<=hiy;yy++){
			 if (get(z,yy,xx)==empty){
				  if (countNeighbours(z-offset,yy,xx,1,set_to)>thresh){
				   set(z,yy,xx,set_to);

				  //reset the bounds
				   numset++;
			       if (xx+1>hix_n) {hix_n=xx+1; newbounds=true;}
			       if (xx-1<lowx_n){lowx_n=xx-1;newbounds=true;}
			       if (yy+1>hiy_n) {hiy_n=yy+1; newbounds=true;}
			       if (yy-1<lowy_n){lowy_n=yy-1;newbounds=true;}
			       }
			      }
		 }
	 }

    lowx=lowx_n;
    hix=hix_n;
    lowy=lowy_n;
    hiy=hiy_n;
   }while (newbounds);
  return numset;
}


public int fill2DThreshInPlane(int z, int y, int x, double empty, double set_to, double thresh, int radius, int minzeros){
//offset allows to fill one level based on the data of another, normally set to 0 for 2D
	//System.out.println("in fill2DThresh = ("+z+","+y+","+x);

	int numset=0;
	if (x>0) lowx=x-1; else lowx=x;
	if (x<xdim) hix=x+1; else hix=x;
	if (y>0) lowy=y-1; else lowy=y;
	if (y<ydim) hiy=y+1; else hiy=y;
	lowx_n=lowx;
	hix_n=hix;
	lowy_n=lowy;
	hiy_n=hiy;
	do{
	newbounds=false;
	for (xx=lowx;xx<=hix;xx++){
		 for (yy=lowy;yy<=hiy;yy++){
			 if (get(z,yy,xx)<thresh){
				 if (countNeighboursUnderThreshold(z,yy,xx,radius,10.0)>minzeros){ //am I close to a empty?
				   set(z,yy,xx,set_to);

				  //reset the bounds
				   numset++;
			       if (xx+1>hix_n) {hix_n=xx+1; newbounds=true;}
			       if (xx-1<lowx_n){lowx_n=xx-1;newbounds=true;}
			       if (yy+1>hiy_n) {hiy_n=yy+1; newbounds=true;}
			       if (yy-1<lowy_n){lowy_n=yy-1;newbounds=true;}
			       }
			      }
		 }
	 }

    lowx=lowx_n;
    hix=hix_n;
    lowy=lowy_n;
    hiy=hiy_n;
   }while (newbounds);
  return numset;
}



int ybegin;
int yend;
int xbegin;
int xend;
public int countNeighbours(int z, int y, int x, int r, double val){
	int sum=0;
	ybegin=y-r;
	yend=y+r;
	xbegin=x-r;
	xend=x+r;
	if (ybegin<0) ybegin=0;
	if (xbegin<0) xbegin=0;
	if (yend>=ydim) yend=ydim-1;
	if (xend>=xdim) xend=xdim-1;
	for (int yy=ybegin;yy<=yend;yy++){
		for (int xx=xbegin;xx<=xend;xx++){
		  index=z*frame+yy*xdim+xx;
		  if (arr[index]==val) sum++;
		}
	}
	return sum;
}


public int countNeighboursOverThreshold(int z, int y, int x, int r, double val){
	int sum=0;
	ybegin=y-r;
	yend=y+r;
	xbegin=x-r;
	xend=x+r;
	if (ybegin<0) ybegin=0;
	if (xbegin<0) xbegin=0;
	if (yend>=ydim) yend=ydim-1;
	if (xend>=xdim) xend=xdim-1;
	for (int yy=ybegin;yy<=yend;yy++){
		for (int xx=xbegin;xx<=xend;xx++){
		  index=z*frame+yy*xdim+xx;
		  if (arr[index]>=val) sum++;
		}
	}
	return sum;
}


public double ratioNeighboursOverThreshold(int z, int y, int x, int r, double val){
	double sum=0;
	double non=0;
	ybegin=y-r;
	yend=y+r;
	xbegin=x-r;
	xend=x+r;
	if (ybegin<0) ybegin=0;
	if (xbegin<0) xbegin=0;
	if (yend>=ydim) yend=ydim-1;
	if (xend>=xdim) xend=xdim-1;
	for (int yy=ybegin;yy<=yend;yy++){
		for (int xx=xbegin;xx<=xend;xx++){
		  index=z*frame+yy*xdim+xx;
		  if (arr[index]>=val) sum++;
		  else non++;
		}
	}

	if ((non==0)&&(sum==0)) return 0;
	return (sum/(non+sum));
}

public int countNeighboursUnderThreshold(int z, int y, int x, int r, double val){
	int sum=0;
	ybegin=y-r;
	yend=y+r;
	xbegin=x-r;
	xend=x+r;
	if (ybegin<0) ybegin=0;
	if (xbegin<0) xbegin=0;
	if (yend>=ydim) yend=ydim-1;
	if (xend>=xdim) xend=xdim-1;
	for (int yy=ybegin;yy<=yend;yy++){
		for (int xx=xbegin;xx<=xend;xx++){
		  index=z*frame+yy*xdim+xx;
		  if (arr[index]<=val) sum++;
		}
	}
	return sum;
}


public int countNeighboursBetweenThresholds(int z, int y, int x, int r, double val1, double val2){
	int sum=0;
	ybegin=y-r;
	yend=y+r;
	xbegin=x-r;
	xend=x+r;
	if (ybegin<0) ybegin=0;
	if (xbegin<0) xbegin=0;
	if (yend>=ydim) yend=ydim-1;
	if (xend>=xdim) xend=xdim-1;
	for (int yy=ybegin;yy<=yend;yy++){
		for (int xx=xbegin;xx<=xend;xx++){
		  index=z*frame+yy*xdim+xx;
		  if ((arr[index]>=val1)&&(arr[index]<=val2)) sum++;
		}
	}
	return sum;
}


public final static int MIN_STATE_NBS_OVER_THRESH=0;
public final static int MAX_STATE_NBS_OVER_THRESH=1;
public final static int MIN_STATE_NBS_UNDER_THRESH=2;
public final static int MAX_STATE_NBS_UNDER_THRESH=3;
public final static int MIN_STATE_NBS_BETWEEN_THRESH=4;
public final static int MAX_STATE_NBS_BETWEEN_THRESH=5;
public final static int MIN_STATE_NBS_RATIO_OVER_THRESH=6;
public final static int MAX_STATE_NBS_RATIO_OVER_THRESH=7;
public final static int STD_DIV=8;
public final static int MAX_STATE_A_AND_B_OVER_THRESH=9;



public void iterate(int z, double presentstate, double threshold, double optional_threshold, double nbsoverthreshold, double setto, int r, doubleArray3D result, int mode){
	for (int y=0;y<ydim;y++){
		for (int x=0;x<xdim;x++){
			index=z*frame+y*xdim+x;
			switch(mode){
				case MIN_STATE_NBS_OVER_THRESH:
			        if (arr[index]<=presentstate){
			        	if (countNeighboursOverThreshold(z,y,x,r,threshold)>nbsoverthreshold){
			         		result.arr[index]=setto;
			         	}
			            }
			            break;
			    case MAX_STATE_NBS_OVER_THRESH:
			    	 if (arr[index]>=presentstate){
						  if (countNeighboursOverThreshold(z,y,x,r,threshold)>nbsoverthreshold){
							 result.arr[index]=setto;
					    }
						}
					    break;
				case MIN_STATE_NBS_UNDER_THRESH:
			        if (arr[index]<=presentstate){
			        	if (countNeighboursUnderThreshold(z,y,x,r,threshold)>nbsoverthreshold){
			         		result.arr[index]=setto;
			         	}
			            }
			            break;
			    case MAX_STATE_NBS_UNDER_THRESH:
			    	 if (arr[index]>=presentstate){
						  if (countNeighboursUnderThreshold(z,y,x,r,threshold)>nbsoverthreshold){
							 result.arr[index]=setto;
					    }
						}
					    break;
				case MIN_STATE_NBS_BETWEEN_THRESH:
			        if (arr[index]<=presentstate){
			        	if (countNeighboursBetweenThresholds(z,y,x,r,threshold,optional_threshold)>nbsoverthreshold){
			         		result.arr[index]=setto;
			         	}
			            }
			            break;
			    case MAX_STATE_NBS_BETWEEN_THRESH:
			    	 if (arr[index]>=presentstate){
						 if (countNeighboursBetweenThresholds(z,y,x,r,threshold,optional_threshold)>nbsoverthreshold){
							 result.arr[index]=setto;
					    }
						}
					    break;
				case MIN_STATE_NBS_RATIO_OVER_THRESH:
			        if (arr[index]<=presentstate){
			        	if (ratioNeighboursOverThreshold(z,y,x,r,threshold)>nbsoverthreshold){
			         		result.arr[index]=setto;
			         	}
			            }
			            break;
			    case MAX_STATE_NBS_RATIO_OVER_THRESH:
			    	 if (arr[index]>=presentstate){
						 if (ratioNeighboursOverThreshold(z,y,x,r,threshold)>nbsoverthreshold){
							 result.arr[index]=setto;
					    }
						}
					    break;
	            case STD_DIV:
						neighbourhoodStats(z,y,x,r);
						result.arr[index]=last_stdiv;
					    break;
				case MAX_STATE_A_AND_B_OVER_THRESH:
			         if (arr[index]>=presentstate){
				      int c1=countNeighbours(z,y,x,r,threshold);
				      int c2=countNeighbours(z,y,x,r,optional_threshold);
				      if ((c1>nbsoverthreshold)&&(c2>nbsoverthreshold)){
						 result.arr[index]=setto;
					    }
				      }
				      break;
		}
	}
   }


}


public void copy(doubleArray3D res){
 if (res.arr.length!=arr.length) return;
 for (int i=0;i<arr.length;i++){
		arr[i]=res.arr[i];
	}
}

public void copy(int[] iarr){
	if (iarr.length!=arr.length) return;
	for (int i=0;i<arr.length;i++){
		arr[i]=iarr[i];
	}
}


public void copyOverThreshold(doubleArray3D res, double thr){
	if (res.arr.length!=arr.length) return;
	for (int i=0;i<arr.length;i++){
		if ((res.arr[i]>thr)&&(res.arr[i]>arr[i])) arr[i]=res.arr[i];
	}
}

public void copyOverThreshold(int[] res, double thr){
	if (res.length!=arr.length) return;
	for (int i=0;i<arr.length;i++){
		if ((res[i]>thr)&&(res[i]>arr[i])) arr[i]=res[i];
	}
}

/**
Calculates the sobel edge value for a 3x3  nbhood and dumps result in destination doubleArray3D
**/
 public void sobel(doubleArray3D dest){
        for(int i=0;i<arr.length;i++){
            try {

                double a = arr[i  -1*xdim  -1] ;
                double b = arr[i  -1*xdim  +0];
                double c = arr[i  -1*xdim  +1] ;
                double d = arr[i + 0*xdim  -1];
                double e = arr[i + 0*xdim  +1];
                double f = arr[i + 1*xdim  -1];
                double g = arr[i + 1*xdim  +0];
                double h = arr[i + 1*xdim  +1];
                double hor = (a+d+f) - (c+e+h);
                double vert = (a+b+c) - (f+g+h);
                dest.arr[i]=Math.sqrt(hor*hor+vert*vert);

            } catch (ArrayIndexOutOfBoundsException e) {
               // i = arr.length;
            }
        }

 }


public void compare(double threshold1, double threshold2, doubleArray3D in, doubleArray3D out){
	for (int i=0;i<arr.length;i++){
		if ((arr[i]>threshold1)&&(in.arr[i]>threshold2)) out.arr[i]=1.0; else out.arr[i]=0.0;
	}
}


public double last_average;
public double last_max;
public double last_min;
public double last_stdiv;
public double last_median;
/**
Calculates statistics on local neighbourhood and dumps the values into last_max,last_min,last_average,last_stdiv.
**/
public void neighbourhoodStats(int z, int y, int x, int r){
	last_max=Double.MIN_VALUE;
	last_min=Double.MAX_VALUE;
	last_average=0;
	int count=0;
	last_stdiv=0;
	double val;
	int sum=0;
	ybegin=y-r;
	yend=y+r;
	xbegin=x-r;
	xend=x+r;
	if (ybegin<0) ybegin=0;
	if (xbegin<0) xbegin=0;
	if (yend>=ydim) yend=ydim-1;
	if (xend>=xdim) xend=xdim-1;
	for (int yy=ybegin;yy<=yend;yy++){
		for (int xx=xbegin;xx<=xend;xx++){
		  val=arr[z*frame+yy*xdim+xx];
		  last_average+=val;
		  count+=1;
		  if (val>last_max) last_max=val;
		  if (val<last_min) last_min=val;
	  }
     }
    last_average=last_average/count;
	for (int yy=ybegin;yy<=yend;yy++){
		for (int xx=xbegin;xx<=xend;xx++){
		  val=arr[z*frame+yy*xdim+xx];
		  val=val-last_average;
		  last_stdiv+=val*val;
		 }
     }
     last_stdiv=last_stdiv/count;
     last_stdiv=java.lang.Math.sqrt(last_stdiv);

}

public double aveIntensityOverThreshold(double threshold){
	double sum=0;
	int num=0;
    for (int i=0;i<size;i++){
			if (arr[i]>=threshold) {
				sum+=arr[i];
				num++;
			}
	}
	return sum/num;
}


public double aveIntensityUnderThreshold(double threshold){
	double sum=0;
	int num=0;
    for (int i=0;i<size;i++){
			if (arr[i]<=threshold) {
				sum+=arr[i];
				num++;
			}
	}
	return sum/num;
}

//for now multiplies two arrays together with z=0, arr2 is assumed to be smaller than arr, result dim=arr2
public void multiply2D(doubleArray3D input, int xoffset, int yoffset, doubleArray3D result){


 for (int i=0;i<input.size;i++){
	int z1=(int)(i/input.frame);
	int y1=(int)((i-(z1*input.frame))/input.xdim);
	int x1=i-(z1*input.frame)-y1*input.xdim;
	result.arr[i]=input.arr[i]*get(z1,y1+yoffset,x1+xoffset);
  }

}

public void multiply(doubleArray3D input){
	for (int i=0;i<arr.length;i++){
	   if (i<input.arr.length){
		   arr[i]*=input.arr[i];
	   }
	}
}


public void divide(doubleArray3D input){
   for (int i=0;i<arr.length;i++){
	   if (i<input.arr.length){
		   if (input.arr[i]!=0)
		      arr[i]=arr[i]/input.arr[i];
	   }
	}
}

public int countOverThreshold(double threshold){
	int count=0;

	for (int i=0;i<size;i++){
		if (arr[i]>=threshold) count++;

	}
	return count;
}


public int countOverThreshold(double threshold,int startz, int endz, int startx, int endx, int starty, int endy){
	int count=0;
	for (int zz=startz;zz<endz;zz++){
	  for (int xx=startx;xx<endx;xx++){
		for (int yy=starty;yy<endy;yy++){

		if (get(zz,yy,xx)>=threshold) count++;
   	  }
    }
   }
  return count;
}



public int changeValue(int z, double oldval, double newval){
    int sum=0;

	for (int yy=0;yy<ydim;yy++){
	 for (int xx=0;xx<xdim;xx++){
		index=z*frame+yy*xdim+xx;
        if (arr[index]==oldval) {
			arr[index]=newval;
			sum++;
		}

	}
	}
	return sum;
}

/**

scales the values in the array so that they are between min and max

**/
public void scale(double newmin, double newmax){
double min=Double.POSITIVE_INFINITY;
double max=Double.NEGATIVE_INFINITY;

for (int i=0;i<arr.length;i++){
 if (arr[i]<min) min=arr[i];
 if (arr[i]>max) max=arr[i];
}
double scale=(newmax-newmin)/(max-min);
for (int i=0;i<arr.length;i++){
	arr[i]=(arr[i]-min)*scale+newmin;
}

}

/** scales values between minval and maxval to be between newmin and newmax. It doesn't touch the other values.  **/

public void scale(double minval, double maxval, double newmin, double newmax){
	double scale=(newmax-newmin)/(maxval-minval);
	double val=0;
	for (int i=0;i<arr.length;i++){
	   val=arr[i];
	   if ((val>minval)&&(val<=maxval)){
		   arr[i]=(val-minval)*scale+newmin;
	   }

	}
}

public Histogram scale_histogram(double newmin, double newmax, double low, double high, gvdecoder.ROI roi){
//ublic Histogram(doubleArray3D dat, int startframe,int endframe, int numberOfBins, java.awt.Polygon poly)
Histogram hist=new Histogram(this,0,zdim,100,roi.poly);


double tot=0;
double scale_low=0;
double scale_high=0;
 double scale=(100.0)/(hist.max-hist.min);
for (int i=0;i<100;i++){
  tot+=hist.bins[i];
  if (tot>(hist.samples*low)&&(scale_low==0)) scale_low=(double)i;
  if (tot>(hist.samples*high)&&(scale_high==0)) scale_high=(double)i;

}
double desired_bot=hist.min+scale_low/scale;
double desired_top=hist.min+scale_high/scale;



double rescale=(newmax-newmin)/(desired_top-desired_bot);
for (int i=0;i<arr.length;i++){
	arr[i]=(arr[i]-desired_bot)*rescale;
}


System.out.println("scale : desired_bot="+desired_bot+" desired_top="+desired_top+" rescale="+rescale);

return hist;

}

public Histogram scale_histogram(double newmin, double newmax, double low, double high){
//ublic Histogram(doubleArray3D dat, int startframe,int endframe, int numberOfBins, java.awt.Polygon poly)
Histogram hist=new Histogram(this,0,zdim,100);


double tot=0;
double scale_low=0;
double scale_high=0;
double scale=(100.0)/(hist.max-hist.min);
for (int i=0;i<100;i++){
  tot+=hist.bins[i];
  if (tot>(hist.samples*low)&&(scale_low==0)) scale_low=(double)i;
  if (tot>(hist.samples*high)&&(scale_high==0)) scale_high=(double)i;

}
double desired_bot=hist.min+scale_low/scale;
double desired_top=hist.min+scale_high/scale;



double rescale=(newmax-newmin)/(desired_top-desired_bot);
for (int i=0;i<arr.length;i++){
	arr[i]=(arr[i]-desired_bot)*rescale;
}


System.out.println("scale : desired_bot="+desired_bot+" desired_top="+desired_top+" rescale="+rescale);

return hist;

}

public int[] scale_ignore_top(double fraction){
double min=Double.POSITIVE_INFINITY;
double max=Double.NEGATIVE_INFINITY;

for (int i=0;i<arr.length;i++){
 if (arr[i]<min) min=arr[i];
 if (arr[i]>max) max=arr[i];
}
int[] hist=new int[100];
double scale=(100.0)/(max-min);
for (int i=0;i<arr.length;i++){
   int k=((int)((arr[i]-min)*scale));
   if (k<=99)
     hist[k]+=1;
    else System.out.println("err k="+k);
}

double tot=0;
double scale_to=0.0;
for (int i=99;i>0;i--){
  tot+=hist[i];
  if (tot>(arr.length*fraction)){
	  scale_to=(double)i;
	  break;
  }
}
double desired_top=min+((max-min)*(scale_to/100.0));
double factor=(max-min)/(desired_top-min);
scale(min,(max*factor));
System.out.println("scaleto="+scale_to+"desired_top="+desired_top+" factor="+factor);
return hist;

}


public int[] scale_ignore_top_above_threshold(double fraction, double threshold){
double min=Double.POSITIVE_INFINITY;
double max=Double.NEGATIVE_INFINITY;
int totalpts=0;
for (int i=0;i<arr.length;i++){
 if (arr[i]<min) min=arr[i];
 if (arr[i]>max) max=arr[i];
}
int[] hist=new int[100];
double scale=(100.0)/(max-min);
for (int i=0;i<arr.length;i++){
   if (arr[i]>threshold){
   totalpts++;
   int k=((int)((arr[i]-min)*scale));
   if (k<=99){
     hist[k]+=1;
     }
    else System.out.println("err k="+k);
}
}

double tot=0;
double scale_to=0.0;
for (int i=99;i>0;i--){
  tot+=hist[i];
  if (tot>(totalpts*fraction)){
	  scale_to=(double)i;
	  break;
  }
}
double desired_top=min+((max-min)*(scale_to/100.0));
double factor=(max-min)/(desired_top-min);
scale(min,(max*factor));
System.out.println("scaleto="+scale_to+"desired_top="+desired_top+" factor="+factor);
return hist;

}



public static void main(String[] arg){
	doubleArray3D arr=new doubleArray3D(10,10);
	for (int i=0;i<arr.size;i++) arr.setIndex(i,1);
	for (int x=4;x<=6;x++){
		for (int y=4;y<=6;y++){
		  arr.set(y,x,0);
		}
	}
	arr.set(7,7,0);
	arr.set(9,9,0);
	arr.set(2,2,0);
	arr.set(2,5,0);
	arr.set(4,4,5);
	arr.set(4,5,5);

	int filled=arr.fill2DThresh(0,4,4,0,5,1,0);
	System.out.println("filled="+filled);
	System.out.println(arr.print(0,1,0,10,0,10));



	System.out.println("\n count nbs="+arr.countNeighbours(0,7,7,1,6.0));
	System.out.println("\n count nbs="+arr.countNeighbours(0,7,7,2,6.0));


}

}

