package gvdecoder;
   public class rotate{

   public double min4(double a, double b, double c, double d){
		if (a < b) {
			if (c < a) {
				if (d < c)
					return d;
				else
					return c;
			} else {
				if (d < a)
					return d;
				else
					return a;
			}
		} else {
			if (c < b) {
				if (d < c)
					return d;
				else
					return c;
			} else {
				if (d < b)
					return d;
				else
					return b;
			}
		}
	}


   public double max4(double a, double b, double c, double d){
		if (a > b) {
			if (c > a) {
				if (d > c)
					return d;
				else
					return c;
			} else {
				if (d > a)
					return d;
				else
					return a;
			}
		} else {
			if (c > b) {
				if (d > c)
					return d;
				else
					return c;
			} else {
				if (d > b)
					return d;
				else
					return b;
			}
		}
	}

  public double[] dst;
  public int dstX;
  public int dstY;

  public void go(double[][] src, double angle){
	  int imageWidth=src[0].length;
	  int imageHeight=src.length;
	  double[] tmp=new double[imageWidth*imageHeight];
	  for (int y=0;y<imageHeight;y++){
		  for (int x=0;x<imageWidth;x++){
			  tmp[y*imageWidth+x]=src[y][x];
		  }
	  }
	  go(tmp,imageWidth,imageHeight,angle);
  }

  public Matrix go(Matrix ma, double angle){
      double[] tmp=new double[ma.dat.ydim*ma.dat.xdim];
		  for (int y=0;y<ma.dat.ydim;y++){
			  for (int x=0;x<ma.dat.xdim;x++){
				  tmp[y*ma.dat.xdim+x]=ma.dat.get(y,x);
			  }
		  }
		  go(tmp,ma.dat.xdim,ma.dat.ydim,angle);
         Matrix mn=new Matrix(1,dstY,dstX);
         for (int y=0;y<dstY;y++){
			 for (int x=0;x<dstX;x++){
			   mn.dat.set(y,x,dst[y*dstX+x]);
			 }
		 }
        return mn;
  }

  public void go(double[]src, int imageWidth, int imageHeight,  double angle){
    double SrcX=imageWidth;
    double SrcY=imageHeight;

        // Rotate the bitmap around the center
	double CtX = ((double) SrcX) / 2;
	double CtY = ((double) SrcY) / 2;

	// First, calculate the destination positions for the four courners to get dstX and dstY
	double cA = (double) Math.cos(angle);
	double sA = (double) Math.sin(angle);

	double x1 = CtX + (-CtX) * cA - (-CtY) * sA;
	double x2 = CtX + (SrcX - CtX) * cA - (-CtY) * sA;
	double x3 = CtX + (SrcX - CtX) * cA - (SrcY - CtY) * sA;
	double x4 = CtX + (-CtX) * cA - (SrcY - CtY) * sA;

	double y1 = CtY + (-CtY) * cA + (-CtX) * sA;
	double y2 = CtY + (SrcY - CtY) * cA + (-CtX) * sA;
	double y3 = CtY + (SrcY - CtY) * cA + (SrcX - CtX) * sA;
	double y4 = CtY + (-CtY) * cA + (SrcX - CtX) * sA;

	int OfX = ((int) Math.floor(min4(x1, x2, x3, x4)));
	int OfY = ((int) Math.floor(min4(y1, y2, y3, y4)));

	dstX = ((int) Math.ceil(max4(x1, x2, x3, x4))) - OfX;
	dstY = ((int) Math.ceil(max4(y1, y2, y3, y4))) - OfY;

    dst=new double[dstX*dstY];
	//dstLine = dst;
	double divisor = cA*cA + sA*sA;
	// Step through the destination bitmap
	for (int stepY = 0; stepY < dstY; stepY++) {
		for (int stepX = 0; stepX < dstX; stepX++) {
			// Calculate the source coordinate
			double orgX = (cA * (((double) stepX + OfX) + CtX * (cA - 1)) + sA * (((double) stepY + OfY) + CtY * (sA - 1))) / divisor;
			double orgY = CtY + (CtX - ((double) stepX + OfX)) * sA + cA *(((double) stepY + OfY) - CtY + (CtY - CtX) * sA);
			int iorgX = (int) orgX;
			int iorgY = (int) orgY;
			if ((iorgX >= 0) && (iorgY >= 0) && (iorgX < SrcX) && (iorgY < SrcY)) {
				// Inside the source bitmap -> copy the bits
				//dstLine[dstX - stepX - 1] = src[iorgX + iorgY * SrcX];
		          dst[stepY*dstX+dstX-stepX-1] = src[iorgX + iorgY * (int)SrcX];

		    } else {

			}
		}
		//dstLine = dstLine + dstX;
	}
    // return dst;
   }


public static void main(String[] args){


	rotate r=new rotate();

	double[] data=new double[100];
    //r.dst=new double[100];
	for (int i=0;i<10;i++){
		for (int j=0;j<10;j++){
			data[i*10+j]=i;
   }
  }

  r.go(data,10,10,0.5f);
  for (int i=0;i<r.dstY;i++){
	  for (int j=0;j<r.dstX; j++){

		  System.out.print(r.dst[i*r.dstX+j]+" ");
      }
      System.out.println("");
  }


}


}