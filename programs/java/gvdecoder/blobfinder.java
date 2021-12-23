package gvdecoder;
import java.util.*;

public class blobfinder{
public boolean inbounds(Matrix ma,int z, int y, int x){
  return ((y>=0)&&(y<ma.ydim)&&(x>=0)&&(x<ma.xdim) && (z>0)&&(z<ma.zdim));
}

public int fill(Matrix m_in, Matrix m_out, int oz, int oy, int ox, int r, double target, double set_to){
 int yy,xx,zz;
 int count=0;
 java.util.Stack stack=new java.util.Stack();
 stack.push(new point(oz,oy,ox));
 do{
  point pt=(point)stack.pop();
  for (int z=-1;z<=1;z++){
   for (int y=-1;y<=1;y++){
    for (int x=-1;x<=1;x++){
     yy=pt.y+y;
     xx=pt.x+x;
     zz=pt.z+z;
     if (inbounds(m_out,zz,yy,xx) && ((zz!=z)&&(yy!=y)&&(xx!=x))){ //not the same coordinate, and hasn't been checked

      if ((m_in.dat.get(zz,yy,xx)==target)&&(m_out.dat.get(zz,yy,xx)==0)){
        m_out.dat.set(zz,yy,xx,set_to);
        count+=1;
        stack.push(new point(zz,yy,xx));
       }
     }
    }
  }//y
  }
  }while (!stack.empty());
 return count;
}

}

class point{
 int z;
 int y;
 int x;

 public point(int zz, int yy, int xx){
  z=zz;
  y=yy;
  x=xx;
 }
}