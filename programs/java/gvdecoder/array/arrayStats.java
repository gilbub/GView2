package gvdecoder.array;

public class arrayStats{

 public double[] pixels;

 public int xdim;
 public int ydim;
 public int zdim;
 public int NumberOfPixelsOverThreshold;
 public double AveragePixelValue;
 public double MaxPixelValue;
 public double MinPixelValue;
 public double Max90PixelValue;
 public double Min90PixelValue;
 public double MedianPixelValue;


 private static long comparisons = 0;
 private static long exchanges   = 0;


 public arrayStats(){

 }

 public arrayStats(double[] arr){
  pixels=new double[arr.length];
  for (int i=0;i<arr.length;i++){
    pixels[i]=arr[i];
  }


 }

 public int  __cmp__(arrayStats arr){
	 if (arr.AveragePixelValue<AveragePixelValue) return 1; else
	 if (arr.AveragePixelValue>AveragePixelValue) return -1;
	 return 0;
 }

public void calcStats(){
	  quicksort(pixels);
	  int length=pixels.length;
	  MedianPixelValue=pixels[(int)(length/2.0)];
	  MaxPixelValue=pixels[length-1];
	  MinPixelValue=pixels[0];
	  Max90PixelValue=pixels[(int)(length*0.9)];
	  Min90PixelValue=pixels[(int)(length*0.1)];
      double sum=0;
      for (int i=0;i<length;i++){
		sum+=pixels[i];
	  }
	  AveragePixelValue=sum/length;
}

 public double PercentilePixelValue(double val){
	 return pixels[(int)(pixels.length*val)];
 }


   public static void quicksort(double[] a) {
         shuffle(a);                        // to guard against worst-case
         quicksort(a, 0, a.length - 1);
     }
     public static void quicksort(double[] a, int left, int right) {
         if (right <= left) return;
         int i = partition(a, left, right);
         quicksort(a, left, i-1);
         quicksort(a, i+1, right);
     }

     private static int partition(double[] a, int left, int right) {
         int i = left - 1;
         int j = right;
         while (true) {
             while (less(a[++i], a[right]))      // find item on left to swap
                 ;                               // a[right] acts as sentinel
             while (less(a[right], a[--j]))      // find item on right to swap
                 if (j == left) break;           // don't go out-of-bounds
             if (i >= j) break;                  // check if pointers cross
             exch(a, i, j);                      // swap two elements into place
         }
         exch(a, i, right);                      // swap with partition element
         return i;
     }

     // is x < y ?
     private static boolean less(double x, double y) {
         comparisons++;
         return (x < y);
     }

     // exchange a[i] and a[j]
     private static void exch(double[] a, int i, int j) {
         exchanges++;
         double swap = a[i];
         a[i] = a[j];
         a[j] = swap;
     }

     // shuffle the array a
     private static void shuffle(double[] a) {
         int N = a.length;
         for (int i = 0; i < N; i++) {
             int r = i + (int) (Math.random() * (N-i));   // between i and N-1
             exch(a, i, r);
         }
     }



}