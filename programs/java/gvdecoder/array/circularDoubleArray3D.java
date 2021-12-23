package gvdecoder.array;

public class circularDoubleArray3D extends gvdecoder.array.doubleArray3D{

public int frame;
public int fill;
/*
0 1 2 3 4  5 6 7 8 9
5 6 7 8 9  0 1 2 3 4

z changes  - index 0 is at index 5
*/

public circularDoubleArray3D(int z, int y, int x){
	super(z,y,x);
}

public double get(int z,int y, int x){
  //System.out.println("in crc get");
  return super.get((frame+z)%zdim,y,x);
}

public void set(int z, int y, int x, double val){
   //System.out.println("in crc set");
   super.set((frame+z)%zdim,y,x,val);
}


public void addFrame(double[] newframe){

	for (int i=0;i<newframe.length;i++){
	  arr[(frame*xdim*ydim)+i]=newframe[i];
	}
	frame++;
	if (frame==zdim) frame=0;
    fill++;
    if (fill==zdim) fill=zdim;
}

public void addFrame(int[] newframe){

	for (int i=0;i<newframe.length;i++){
	  arr[(frame*xdim*ydim)+i]=(double)newframe[i];
	}
	frame++;
	if (frame==zdim) frame=0;
    fill++;
    if (fill==zdim) fill=zdim;
}




}