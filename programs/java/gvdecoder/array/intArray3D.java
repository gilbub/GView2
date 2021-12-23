package gvdecoder.array;

public class intArray3D extends GArray{

int[] arr;
int zdim;
int xdim;
int ydim;
int frame;
int size;
int index; //dummy

public intArray3D(int z, int y, int x){
 arr=new int[z*y*x];
 zdim=z;
 xdim=x;
 ydim=y;
 frame=x*y;
 size=frame*z;
}

public intArray3D(int y, int x){
	arr=new int[y*x];
	zdim=1;
	xdim=x;
	ydim=y;
	frame=x*y;
	size=x*y;
}

public intArray3D(doubleArray3D old){
 //creates a copy
 zdim=old.zdim;
 xdim=old.xdim;
 ydim=old.ydim;
 frame=xdim*ydim;
 size=frame*zdim;
 arr=new int[zdim*ydim*xdim];

}


public int get(int y, int x){
	return get(0,y,x);
}


public int get(int z,int y,int x ){
	index=z*frame+y*xdim+x;
	if (index<size)
	 return arr[index];
	else return 0;
  }


public void assign(intArray3D old){
	//should do checks here for bounds!!!
	for (int z=0;z<zdim;z++){
		for (int y=0;y<ydim;y++){
			for (int x=0;x<xdim;x++){
				set(z,y,x,old.get(z,y,x));
			}
		}
	}
}

public void set (int y, int x, int val){
 set (0,y,x,val);
}

public void set (int z, int y, int x, int val){
    index=z*frame+y*xdim+x;
	if (index<size)
	 arr[index]=val;
 }


 public int[] section(int x, int y){
	int[] tmp=new int[zdim];
	for (int i=0;i<zdim;i++){
		tmp[i]=get(i,x,y);
	}
	return tmp;
 }

}