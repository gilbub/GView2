package gvdecoder;

/*
 Ca helper should allow the definition of a CA where each element has its own 'r' and 'th' parameter.
 Activation is defined by th=(active)/(excitable)
 Cells are counted via the use of a mask.
 An input array is for raw experimental data, where active cells are compared to CA cells to initialize the CA and to compare its output to the next frame.
 The ca looks at 3 frames in succession
   F(t-1) defines (likely) Refractory cells
   F(t) defines (likely) active  cells
   F(t+1) defines (likely) excited cells

   active cells are iterated N times (n usually =1), and the output related to F(t+1). If they are not consistent with F(t+1) they revert to excitable. Checks are made using a low threshold

   if there are cells that are active in F(t+1) (high threshold) that aren't predicted by the CA


*/

public class CAHelper2{

 Matrix data;
 Matrix ca;
 Matrix swap;
 double thr;
 double rt;
 double rtp1;
 double tt;
 double ttp1;
 double car;
 double cat;

 public double[][][] distancemasks;
 public int[][][] circlemasks;
 public int numberofmasks=10;


 /*count neighbours in data at t with neighbourhood (rt)
   count neighbours a larger region (rtp1) in data at t+1,
   if neighbours(rt)>tt &&  if neighbours(rtp1)>ttp1
    Set ca(i) to E

   iterate:




 */


 public CAHelper2(){
  initializemasks();
 }
public double dist(int x1, int y1, int x2, int y2){
 return Math.sqrt( (x2-x1)*(x2-x1)+(y2-y1)*(y2-y1) );
 }

public void initializemasks(){
distancemasks=new double[numberofmasks][][];
circlemasks=new int[numberofmasks][][];
double d;
double v;
for (int n=0;n<numberofmasks;n++){
 distancemasks[n]=new double[2*n+1][2*n+1];
 circlemasks[n]=new int[2*n+1][2*n+1];
 for (int x=-n;x<=n;x++){
  for (int y=-n;y<=n;y++){
    d=dist(0,0,x,y);
    v=0;
    if (d>0) v=1.0/d;
    distancemasks[n][x+n][y+n]=v;
    if (v>=1.0/n) circlemasks[n][x+n][y+n]=1;
    else circlemasks[n][x+n][y+n]=0;
    }//y
   }//x
  }//n
}
}



