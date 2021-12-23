package gvdecoder;

public class convolver{
/*
only for 3x3 kernel
*/
public void convolve(Matrix min, Matrix mout,double[][]kernel){
for (int z=0;z<min.zdim;z++){
for (int y=0;y<min.ydim;y++){
 for (int x=0;x<min.xdim;x++){
  double sum=0;
  if ((x==0)||(y==0)||(x==min.xdim-1)||(y==min.ydim-1)){
   sum=min.dat.get(z,y,x);
  }
  else{
  for (int i=-1;i<=1;i++){
    for (int j=-1;j<=1;j++){
     sum  += min.dat.get(z,y+i,x+j)*kernel[i+1][j+1];
    }
    }
    }
   mout.dat.set(z,y,x,sum);
   }
   }
   }

}


}