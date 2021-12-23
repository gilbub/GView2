package gvdecoder;

   /** LU Decomposition.
   <P>
   This is taken from the Jama matrix package.

   For an m-by-n JamaMatrix A with m >= n, the LU decomposition is an m-by-n
   unit lower triangular JamaMatrix L, an n-by-n upper triangular JamaMatrix U,
   and a permutation vector piv of length m so that A(piv,:) = L*U.
   If m < n, then L is m-by-m and U is m-by-n.
   <P>
   The LU decompostion with pivoting always exists, even if the JamaMatrix is
   singular, so the constructor will never fail.  The primary use of the
   LU decomposition is in the solution of square systems of simultaneous
   linear equations.  This will fail if isNonsingular() returns false.
   */

public class LUDecomposition implements java.io.Serializable {

/* ------------------------
   Class variables
 * ------------------------ */

   /** Array for internal storage of decomposition.
   @serial internal array storage.
   */
   private double[][] LU;

   /** Row and column dimensions, and pivot sign.
   @serial column dimension.
   @serial row dimension.
   @serial pivot sign.
   */
   private int m, n, pivsign;

   /** Internal storage of pivot vector.
   @serial pivot vector.
   */
   private int[] piv;

/* ------------------------
   Constructor
 * ------------------------ */

   /** LU Decomposition
   @param  A   Rectangular JamaMatrix
   @return     Structure to access L, U and piv.
   */



public LUDecomposition (double[][] arr, int N) {

   // Use a "left-looking", dot-product, Crout/Doolittle algorithm.


      m = N;
      n = N;

      LU = new double[m][n];
	        for (int i = 0; i < m; i++) {
	           for (int j = 0; j < n; j++) {
	              LU[i][j] = arr[i][j];
	           }
      }


      piv = new int[m];
      for (int i = 0; i < m; i++) {
         piv[i] = i;
      }
      pivsign = 1;
      double[] LUrowi;
      double[] LUcolj = new double[m];

      // Outer loop.

      for (int j = 0; j < n; j++) {

         // Make a copy of the j-th column to localize references.

         for (int i = 0; i < m; i++) {
            LUcolj[i] = LU[i][j];
         }

         // Apply previous transformations.

         for (int i = 0; i < m; i++) {
            LUrowi = LU[i];

            // Most of the time is spent in the following dot product.

            int kmax = Math.min(i,j);
            double s = 0.0;
            for (int k = 0; k < kmax; k++) {
               s += LUrowi[k]*LUcolj[k];
            }

            LUrowi[j] = LUcolj[i] -= s;
         }

         // Find pivot and exchange if necessary.

         int p = j;
         for (int i = j+1; i < m; i++) {
            if (Math.abs(LUcolj[i]) > Math.abs(LUcolj[p])) {
               p = i;
            }
         }
         if (p != j) {
            for (int k = 0; k < n; k++) {
               double t = LU[p][k]; LU[p][k] = LU[j][k]; LU[j][k] = t;
            }
            int k = piv[p]; piv[p] = piv[j]; piv[j] = k;
            pivsign = -pivsign;
         }

         // Compute multipliers.

         if (j < m & LU[j][j] != 0.0) {
            for (int i = j+1; i < m; i++) {
               LU[i][j] /= LU[j][j];
            }
         }
      }
   }

/* ------------------------
   Public Methods
 * ------------------------ */

   /** Is the JamaMatrix nonsingular?
   @return     true if U, and hence A, is nonsingular.
   */

   public boolean isNonsingular () {
      for (int j = 0; j < n; j++) {
         if (LU[j][j] == 0)
            return false;
      }
      return true;
   }


   public double det () {
      if (m != n) {
         throw new IllegalArgumentException("JamaMatrix must be square.");
      }
      double d = (double) pivsign;
      for (int j = 0; j < n; j++) {
         d *= LU[j][j];
      }
      return d;
   }





public static void main(String[] args){
	double[][] tst=new double[4][4];
	for (int i=0;i<4;i++){
		for (int j=0;j<4;j++){
			tst[i][j]=1;
		}
	}
 tst[0][0]=0;
 tst[1][1]=0;
 tst[2][2]=0;
 tst[3][3]=0;
 LUDecomposition  lu= new LUDecomposition(tst,4);
 System.out.println("res="+lu.det());
}



}
