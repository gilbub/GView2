package gvdecoder;
import java.awt.Point;


public class Circuit implements java.util.Iterator{
 int r=1;
 int n;
 int i;
 Point[] pts;


 public Circuit(int radius){
  this.r=radius;
  this.n=2*r*4;
  pts=new Point[this.n];
  int count=0;
  int x,y;
  for (x=0;x<=2*r;x++) pts[count++]=new Point(-r+x,-r);
  for (y=1;y<=2*r;y++) pts[count++]=new Point(r,-r+y);
  for (x=1;x<=2*r;x++) pts[count++]=new Point(r-x,r);
  for (y=1;y<2*r;y++)  pts[count++]=new Point(-r,r-y);
  }

 public boolean hasNext(){
   return i<n;
 }

 public Point next(){
  return pts[i++];
  }

 public void reset(){
  i=0;
 }


 }


