package gvdecoder;
   public class rotate{

   public float min4(float a, float b, float c, float d){
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


   public float max4(float a, float b, float c, float d){
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

  public float[] dst;
  public int dstX;
  public int dstY;

  public void go(float[]src, int imageWidth, int imageHeight,  float angle){
    float SrcX=imageWidth;
    float SrcY=imageHeight;

        // Rotate the bitmap around the center
	float CtX = ((float) SrcX) / 2;
	float CtY = ((float) SrcY) / 2;

	// First, calculate the destination positions for the four courners to get dstX and dstY
	float cA = (float) Math.cos(angle);
	float sA = (float) Math.sin(angle);

	float x1 = CtX + (-CtX) * cA - (-CtY) * sA;
	float x2 = CtX + (SrcX - CtX) * cA - (-CtY) * sA;
	float x3 = CtX + (SrcX - CtX) * cA - (SrcY - CtY) * sA;
	float x4 = CtX + (-CtX) * cA - (SrcY - CtY) * sA;

	float y1 = CtY + (-CtY) * cA + (-CtX) * sA;
	float y2 = CtY + (SrcY - CtY) * cA + (-CtX) * sA;
	float y3 = CtY + (SrcY - CtY) * cA + (SrcX - CtX) * sA;
	float y4 = CtY + (-CtY) * cA + (SrcX - CtX) * sA;

	int OfX = ((int) Math.floor(min4(x1, x2, x3, x4)));
	int OfY = ((int) Math.floor(min4(y1, y2, y3, y4)));

	dstX = ((int) Math.ceil(max4(x1, x2, x3, x4))) - OfX;
	dstY = ((int) Math.ceil(max4(y1, y2, y3, y4))) - OfY;

    dst=new float[dstX*dstY];
	//dstLine = dst;
	float divisor = cA*cA + sA*sA;
	// Step through the destination bitmap
	for (int stepY = 0; stepY < dstY; stepY++) {
		for (int stepX = 0; stepX < dstX; stepX++) {
			// Calculate the source coordinate
			float orgX = (cA * (((float) stepX + OfX) + CtX * (cA - 1)) + sA * (((float) stepY + OfY) + CtY * (sA - 1))) / divisor;
			float orgY = CtY + (CtX - ((float) stepX + OfX)) * sA + cA *(((float) stepY + OfY) - CtY + (CtY - CtX) * sA);
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

	float[] data=new float[100];
    //r.dst=new float[100];
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