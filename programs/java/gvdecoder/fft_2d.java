package gvdecoder;
/*-------------------------------------------------------------------------
   Perform a 2D FFT inplace given a complex 2D array
   The direction dir, 1 for forward, -1 for reverse
   The size of the array (nx,ny)
   Return false if there are memory problems or
      the dimensions are not powers of 2
*/
import gvdecoder.array.*;

public class fft_2d{

//convert the matrix to a two dimensional array.
//hold imaginary numbers in the class

public complex[][] c;


public static int whatpowerof2(int val){
	for (int i=0;i<18;i++){
		if (Math.pow(2,i)==val) return i;
	}
  return -1;
}

public void go(Matrix m, int zindex, int nx, int ny, int dir){

 	c=new complex[m.ydim][m.xdim];
 	for (int y=0;y<m.ydim;y++){
		for (int x=0;x<m.xdim;x++){
		c[y][x]=new complex();
		c[y][x].r=m.dat.get(zindex,y,x);
        c[y][x].i=0.0;
		}
	}
   FFT2D(c,nx,ny,dir);
}

public Matrix getMatrix(){
	complex[][] sc=shiftOrigin(c);
	Matrix fm=new Matrix(1,sc.length,sc[0].length);
	for (int y=0;y<sc.length;y++){
		for (int x=0;x<sc[0].length;x++){
			fm.dat.set(0,y,x,Math.sqrt(sc[y][x].r*sc[y][x].r+sc[y][x].i*sc[y][x].i));
		}
  }
  return fm;
}






public static void FFT2D(Matrix c,int nx,int ny,int dir){



   int i,j;
   int m,twopm;
   double []real;
   double []imag;

   if (c.imag==null){
	   c.imag=new doubleArray3D(c.dat.zdim,c.dat.ydim,c.dat.xdim);
   }
   /* Transform the rows */
   real = new double[nx];//(double *)malloc(nx * sizeof(double));
   imag = new double[nx];//(double *)malloc(nx * sizeof(double));
   if (real == null || imag == null)
      return;
   //if (!Powerof2(nx,&m,&twopm) || twopm != nx)
   //   return(FALSE);
   m=whatpowerof2(nx);
   for (j=0;j<ny;j++) {
      for (i=0;i<nx;i++) {
         real[i] = c.dat.get(0,i,j);//c[i][j].r;
         imag[i] = 0;// c[i][j].i;
      }
      FFT(dir,m,real,imag);
      for (i=0;i<nx;i++) {
         //c.dat.set(0,i,j,100);
         c.dat.set(0,i,j,real[i]);//c[i][j].r = real[i];
         c.imag.set(0,i,j,imag[i]);
      }
   }
  // free(real);
  // free(imag);

   /* Transform the columns */
   real = new double[ny];//(double *)malloc(ny * sizeof(double));
   imag = new double[ny]; //(double *)malloc(ny * sizeof(double));
   if (real == null || imag == null)
      return;
   m=whatpowerof2(ny);
   //if (!Powerof2(ny,&m,&twopm) || twopm != ny)
   //   return(FALSE);
   for (i=0;i<nx;i++) {
      for (j=0;j<ny;j++) {
         real[j] = c.dat.get(0,i,j);//c[i][j].r;
         imag[j] = c.imag.get(0,i,j);//c[i][j].i;
      }
      FFT(dir,m,real,imag);
      for (j=0;j<ny;j++) {
         c.dat.set(0,i,j,real[j]); //c[i][j].r = real[j];
         c.imag.set(0,i,j,imag[i]);
         //c.dat.set(0,i,j,100);
         //c[i][j].i = imag[j];
      }
   }
   //free(real);
   //free(imag);


}




public static void FFT2D(complex[][] c,int nx,int ny,int dir){



   int i,j;
   int m,twopm;
   double []real;
   double []imag;

   /* Transform the rows */
   real = new double[nx];//(double *)malloc(nx * sizeof(double));
   imag = new double[nx];//(double *)malloc(nx * sizeof(double));
   if (real == null || imag == null)
      return;
   m=whatpowerof2(nx);
   if (m==-1) return;
   //if (!Powerof2(nx,&m,&twopm) || twopm != nx)
   //   return(FALSE);
   for (j=0;j<ny;j++) {
      for (i=0;i<nx;i++) {
         real[i] = c[i][j].r;
         imag[i] = c[i][j].i;
      }
      FFT(dir,m,real,imag);
      for (i=0;i<nx;i++) {
         c[i][j].r = real[i];
         c[i][j].i = imag[i];
      }
   }
  // free(real);
  // free(imag);

   /* Transform the columns */
   real = new double[ny];//(double *)malloc(ny * sizeof(double));
   imag = new double[ny]; //(double *)malloc(ny * sizeof(double));
   if (real == null || imag == null)
      return;
   m=whatpowerof2(ny);
   if (m==-1) return;
   //if (!Powerof2(ny,&m,&twopm) || twopm != ny)
   //   return(FALSE);
   for (i=0;i<nx;i++) {
      for (j=0;j<ny;j++) {
         real[j] = c[i][j].r;
         imag[j] = c[i][j].i;
      }
      FFT(dir,m,real,imag);
      for (j=0;j<ny;j++) {
         c[i][j].r = real[j];
         c[i][j].i = imag[j];
      }
   }
   //free(real);
   //free(imag);


}

/*-------------------------------------------------------------------------
   This computes an in-place complex-to-complex FFT
   x and y are the real and imaginary arrays of 2^m points.
   dir =  1 gives forward transform
   dir = -1 gives reverse transform

     Formula: forward
                  N-1
                  ---
              1   \          - j k 2 pi n / N
      X(n) = ---   >   x(k) e                    = forward transform
              N   /                                n=0..N-1
                  ---
                  k=0

      Formula: reverse
                  N-1
                  ---
                  \          j k 2 pi n / N
      X(n) =       >   x(k) e                    = forward transform
                  /                                n=0..N-1
                  ---
                  k=0
*/
public static void FFT(int dir,int m,double []x,double []y){
   int nn,i,i1,j,k,i2,l,l1,l2;
   double c1,c2,tx,ty,t1,t2,u1,u2,z;

   /* Calculate the number of points */
   nn = 1;
   for (i=0;i<m;i++)
      nn *= 2;

   /* Do the bit reversal */
   i2 = nn >> 1;
   j = 0;
   for (i=0;i<nn-1;i++) {
      if (i < j) {
         tx = x[i];
         ty = y[i];
         x[i] = x[j];
         y[i] = y[j];
         x[j] = tx;
         y[j] = ty;
      }
      k = i2;
      while (k <= j) {
         j -= k;
         k >>= 1;
      }
      j += k;
   }

   /* Compute the FFT */
   c1 = -1.0;
   c2 = 0.0;
   l2 = 1;
   for (l=0;l<m;l++) {
      l1 = l2;
      l2 <<= 1;
      u1 = 1.0;
      u2 = 0.0;
      for (j=0;j<l1;j++) {
         for (i=j;i<nn;i+=l2) {
            i1 = i + l1;
            t1 = u1 * x[i1] - u2 * y[i1];
            t2 = u1 * y[i1] + u2 * x[i1];
            x[i1] = x[i] - t1;
            y[i1] = y[i] - t2;
            x[i] += t1;
            y[i] += t2;
         }
         z =  u1 * c1 - u2 * c2;
         u2 = u1 * c2 + u2 * c1;
         u1 = z;
      }
      c2 = Math.sqrt((1.0 - c1) / 2.0);
      if (dir == 1)
         c2 = -c2;
      c1 = Math.sqrt((1.0 + c1) / 2.0);
   }

   /* Scaling for forward transform */
   if (dir == 1) {
      for (i=0;i<nn;i++) {
         x[i] /= (double)nn;
         y[i] /= (double)nn;
      }
   }

  // return(TRUE);
}


public static complex[][] shiftOrigin(complex[][] data){
    int numberOfRows = data.length;
    int numberOfCols = data[0].length;
    int newRows;
    int newCols;

    complex[][] output =
          new complex[numberOfRows][numberOfCols];

    //Must treat the data differently when the
    // dimension is odd than when it is even.

    if(numberOfRows%2 != 0){//odd
      newRows = numberOfRows +
                            (numberOfRows + 1)/2;
    }else{//even
      newRows = numberOfRows + numberOfRows/2;
    }//end else

    if(numberOfCols%2 != 0){//odd
      newCols = numberOfCols +
                            (numberOfCols + 1)/2;
    }else{//even
      newCols = numberOfCols + numberOfCols/2;
    }//end else

    //Create a temporary working array.
    complex[][] temp =
                    new complex[newRows][newCols];

    //Copy input data into the working array.
    for(int row = 0;row < numberOfRows;row++){
      for(int col = 0;col < numberOfCols;col++){
        temp[row][col] = data[row][col];
      }//col loop
    }//row loop

    //Do the horizontal shift first
    if(numberOfCols%2 != 0){//shift for odd
      //Slide leftmost (numberOfCols+1)/2 columns
      // to the right by numberOfCols columns
      for(int row = 0;row < numberOfRows;row++){
        for(int col = 0;
                 col < (numberOfCols+1)/2;col++){
          temp[row][col + numberOfCols] =
                                  temp[row][col];
        }//col loop
      }//row loop
      //Now slide everything back to the left by
      // (numberOfCols+1)/2 columns
      for(int row = 0;row < numberOfRows;row++){
        for(int col = 0;
                       col < numberOfCols;col++){
          temp[row][col] =
             temp[row][col+(numberOfCols + 1)/2];
        }//col loop
      }//row loop

    }else{//shift for even
      //Slide leftmost (numberOfCols/2) columns
      // to the right by numberOfCols columns.
      for(int row = 0;row < numberOfRows;row++){
        for(int col = 0;
                     col < numberOfCols/2;col++){
          temp[row][col + numberOfCols] =
                                  temp[row][col];
        }//col loop
      }//row loop

      //Now slide everything back to the left by
      // numberOfCols/2 columns
      for(int row = 0;row < numberOfRows;row++){
        for(int col = 0;
                       col < numberOfCols;col++){
          temp[row][col] =
                 temp[row][col + numberOfCols/2];
        }//col loop
      }//row loop
    }//end else
    //Now do the vertical shift
    if(numberOfRows%2 != 0){//shift for odd
      //Slide topmost (numberOfRows+1)/2 rows
      // down by numberOfRows rows.
      for(int col = 0;col < numberOfCols;col++){
        for(int row = 0;
                 row < (numberOfRows+1)/2;row++){
          temp[row + numberOfRows][col] =
                                  temp[row][col];
        }//row loop
      }//col loop
      //Now slide everything back up by
      // (numberOfRows+1)/2 rows.
      for(int col = 0;col < numberOfCols;col++){
        for(int row = 0;
                       row < numberOfRows;row++){
          temp[row][col] =
             temp[row+(numberOfRows + 1)/2][col];
        }//row loop
      }//col loop

    }else{//shift for even
      //Slide topmost (numberOfRows/2) rows down
      // by numberOfRows rows
      for(int col = 0;col < numberOfCols;col++){
        for(int row = 0;
                     row < numberOfRows/2;row++){
          temp[row + numberOfRows][col] =
                                  temp[row][col];
        }//row loop
      }//col loop

      //Now slide everything back up by
      // numberOfRows/2 rows.
      for(int col = 0;col < numberOfCols;col++){
        for(int row = 0;
                       row < numberOfRows;row++){
          temp[row][col] =
                 temp[row + numberOfRows/2][col];
        }//row loop
      }//col loop
    }//end else

    //Shifting of the origin is complete.  Copy
    // the rearranged data from temp to output
    // array.
    for(int row = 0;row < numberOfRows;row++){
      for(int col = 0;col < numberOfCols;col++){
        output[row][col] = temp[row][col];
      }//col loop
    }//row loop
    return output;
  }//end shiftOrigin method




public static void main(String[] args){

Matrix m =new Matrix(1,16,16);
 for (int i=0;i<16;i++){
  for (int j=0;j<16;j++){
	  if ((i>8)&&(i<13)&&(j>8)&&(j<13)){
		 m.dat.set(i,j,100.0);
	 }
   }
}
  for (int i=0;i<16;i++){
	  for (int j=0;j<16;j++){
		  System.out.print(m.dat.get(i,j)+"  ");
 }
 System.out.println("");
}
FFT2D(m,16,16,1);
System.out.println("after\n\n");
for (int i=0;i<16;i++){
		for (int j=0;j<16;j++){
			System.out.print(m.dat.get(i,j)+"  ");
        }
        System.out.println("");
   }



/*
complex[][] c=new complex[16][16];
	for (int i=0;i<16;i++){
		for (int j=0;j<16;j++){
			c[i][j]=new complex();
		  if ((i>8)&&(i<13)&&(j>8)&&(j<13)){
			  c[i][j].r=100;
			  c[i][j].i=0;
		  }
		  else{
			 c[i][j].r=0;
			 c[i][j].i=0;
		  }
		}
	}
  for (int i=0;i<16;i++){
		for (int j=0;j<16;j++){
			System.out.print(c[i][j].r+","+c[i][j].i+"  ");
        }
   System.out.println("");

   }

FFT2D(c,16,16,1);
System.out.println("after\n\n");
for (int i=0;i<16;i++){
		for (int j=0;j<16;j++){
			System.out.print(c[i][j].r+","+c[i][j].i+"  ");
        }
        System.out.println("");
   }

*/



}
}
class complex{
	public double r;
	public double i;
	public String toString(){return r+","+i;}
}

