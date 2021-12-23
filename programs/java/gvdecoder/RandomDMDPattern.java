package gvdecoder;
import java.util.Random;

public class RandomDMDPattern{
public Matrix filled;
public Matrix last;
public int xd;
public int yd;
public int seed;
public int dim;
public java.util.Random rand;

public RandomDMDPattern(int xd, int yd, int seed){
 this.xd=xd;
 this.yd=yd;
 this.seed=seed;
 dim=xd*yd;
 filled=new Matrix(1,yd,xd);
 last=new Matrix(1,yd,xd);
 rand=new java.util.Random(seed);
}

public int doNext(int locs){
 int xl=0;
 int yl=0;
 int xyloc=0;
 int i=0;
 for (int j=0;j<last.dat.arr.length;j++) last.dat.arr[j]=0;
 int empty=dim-(int)filled.dat.sum();
 if (empty<=locs){
	 for (int p=0;p<dim;p++){
	  yl=(int)(((float)p)/xd);
      xl=p-yl*xd;
      if (filled.dat.get(0,yl,xl)==0){
		  filled.dat.set(0,yl,xl,1);
		  last.dat.set(0,yl,xl,1);

	  }
	}
   return empty;
 }
 for (i=0;i<locs;i++){
   xyloc=rand.nextInt(dim);
   for (int k=0;k<=dim;k++){
     yl=(int)(((float)xyloc)/xd);
     xl=xyloc-yl*xd;
     if (filled.dat.get(0,yl,xl)==0){
      filled.dat.set(0,yl,xl,1);
      last.dat.set(0,yl,xl,1);
      k=dim;
      break;
     }
     else{
	  if (i%4==0){
	   xyloc-=xd;
	   if (xyloc<0) xyloc=xyloc+dim;
	  }else
	  if (i%2==0){
	   xyloc+=xd;
	   if (xyloc>=dim) xyloc=xyloc-dim;
	  }
	  else
	  if (i%3==0){
		xyloc-=1;
	  }
	  else
      xyloc+=1;
      if (xyloc>=dim) xyloc=0;
      if (xyloc<0) xyloc=dim-1;

      }
     }

     }
     return i;
    }
 }




