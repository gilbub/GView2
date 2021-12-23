package gvdecoder.array;

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



public void subtract(doubleArray3D old){
	if (old.arr.length==arr.length){

	for (int i=0;i<arr.length;i++){
		arr[i]=arr[i]-old.arr[i];
	}
  }else System.out.println("doubleArray3D:: the arrays must be the same length to subtract");
}

public void assign(doubleArray3D old){
	//should do checks here for bounds!!!
	for (int z=0;z<zdim;z++){
		for (int y=0;y<ydim;y++){
			for (int x=0;x<xdim;x++){
				set(z,y,x,old.get(z,y,x));
			}
		}
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



public double get(int z,int y,int x){
	index=z*frame+y*xdim+x;

	if ((index<size)&&(index>=0))
	 return arr[index];
	else return 0;
  }

public void set (int y, int x, double val){
 set (0,y,x,val);
}

 public void set (int z, int y, int x, double val){
    index=z*frame+y*xdim+x;
	if ((index<size)&&(index>=0))
	 arr[index]=val;
 }

 public double[] section(int x, int y, int startz, int endz){
	 double[] tmp=new double[endz-startz];
	 	for (int i=startz;i<endz;i++){
	 		tmp[i-startz]=get(i,x,y);
	 	}
	return tmp;
 }

 public double[] section(int x, int y){
	double[] tmp=new double[zdim];
	for (int i=0;i<zdim;i++){
		tmp[i]=get(i,x,y);
	}
	return tmp;
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

//for now multiplies two arrays together with z=0, arr2 is assumed to be smaller than arr, result dim=arr2
public void multiply2D(doubleArray3D input, int xoffset, int yoffset, doubleArray3D result){


 for (int i=0;i<input.size;i++){
	int z1=(int)(i/input.frame);
	int y1=(int)((i-(z1*input.frame))/input.xdim);
	int x1=i-(z1*input.frame)-y1*input.xdim;
	result.arr[i]=input.arr[i]*get(z1,y1+yoffset,x1+xoffset);
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

