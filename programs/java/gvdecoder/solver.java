package gvdecoder;

import JSci.maths.matrices.IntegerSquareMatrix;

public class solver{

public double m00,m01,m02,m03;
public double m10,m11,m12,m13;
public double m20,m21,m22,m23;
public double m30,m31,m32,m33;


public void setD(){
 m00=0; m01=1; m02=1; m03=1;
 m10=1; m11=0; m12=1; m13=1;
 m20=1; m21=1; m22=0; m23=1;
 m30=1; m31=1; m32=1; m33=0;
}



public double D(){
	setD();
	return determinant();
}

public double Dw(double p1, double p2, double p3, double p4){
	setD();
	m00=p1;
	m10=p2;
	m20=p3;
	m30=p4;
	return determinant();
}

public double Dx(double p1, double p2, double p3, double p4){
	setD();
	m01=p1;
	m11=p2;
	m21=p3;
	m31=p4;
	return determinant();
}

public double Dy(double p1, double p2, double p3, double p4){
	setD();
	m02=p1;
	m12=p2;
	m22=p3;
	m32=p4;
	return determinant();
}

public double Dz(double p1, double p2, double p3, double p4){
	setD();
	m03=p1;
	m13=p2;
	m23=p3;
	m33=p4;
	return determinant();
}



public double[] solve4(double p1, double p2, double p3, double p4){
	double[] res=new double[4];
	double d=D();
	double dw=Dw(p1,p2,p3,p4);
	double dx=Dx(p1,p2,p3,p4);
	double dy=Dy(p1,p2,p3,p4);
	double dz=Dz(p1,p2,p3,p4);
	res[0]=dw/d;
	res[1]=dx/d;
	res[2]=dy/d;
	res[3]=dz/d;
	return res;
}




public double[][] NxN(int N){

double[][] arr=new double[N][N];
for (int i=0;i<N;i++){
	for (int j=0;j<N;j++){
		arr[i][j]=1;
		if (i==j) arr[i][j]=0;
	}
   }
   return arr;
}

public double[] solveN(double[] vals){
  int N=vals.length;
  double[][] arr=NxN(N);
  double[] result=new double[N];
  LUDecomposition lu=new LUDecomposition(arr,N);
  double d=lu.det();

  for (int i=0;i<N;i++){
	 arr=NxN(N);
	 for (int j=0;j<N;j++){
		arr[i][j]=vals[j];
	 }
	 result[i]=((new LUDecomposition(arr,N)).det())/d;
 }

return result;

}



public double determinant() {
      double value;
      value =
      m03 * m12 * m21 * m30-m02 * m13 * m21 * m30-m03 * m11 * m22 * m30+m01 * m13 * m22 * m30+
      m02 * m11 * m23 * m30-m01 * m12 * m23 * m30-m03 * m12 * m20 * m31+m02 * m13 * m20 * m31+
      m03 * m10 * m22 * m31-m00 * m13 * m22 * m31-m02 * m10 * m23 * m31+m00 * m12 * m23 * m31+
      m03 * m11 * m20 * m32-m01 * m13 * m20 * m32-m03 * m10 * m21 * m32+m00 * m13 * m21 * m32+
      m01 * m10 * m23 * m32-m00 * m11 * m23 * m32-m02 * m11 * m20 * m33+m01 * m12 * m20 * m33+
      m02 * m10 * m21 * m33-m00 * m12 * m21 * m33-m01 * m10 * m22 * m33+m00 * m11 * m22 * m33;
   return value;
   }


public int testjsci(){
  IntegerSquareMatrix ism=new IntegerSquareMatrix(16);
  for (int i=0;i<4;i++){
   for (int j=0;j<4;j++){
      ism.setElement(i,j,1);
     }
    }
   ism.setElement(0,0,0);
   ism.setElement(1,1,0);
   ism.setElement(2,2,0);
   ism.setElement(3,3,0);
   return ism.det();


}


public static void main(String[] arg){
 solver s=new solver();
 System.out.println("res="+ s.testjsci());

}


}