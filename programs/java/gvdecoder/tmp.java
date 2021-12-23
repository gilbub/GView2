package gvdecoder;
public class tmp{
public void clean(Matrix m_in,int R,double thr){
 Matrix tmp=new Matrix(1,m_in.ydim,m_in.xdim);
 int count1,count2,yi,xi;
 int intthr=(int)(thr*((2*R+1)*(2*R+1)));
 for (int z=0;z<m_in.zdim-1;z++){

  tmp.dat.set(0);
  for (int y=0;y<m_in.ydim;y++){
   for (int x=0;x<m_in.xdim;x++){
	 if (m_in.dat.get(z,y,x)>0){
     count1=0;
     count2=0;

     for (int yy=-R; yy<=R;yy++){
      for (int xx=-R;xx<=R;xx++){
       yi=y+yy;
       xi=x+xx;
       if ((xi>=0)&&(xi<m_in.xdim)&&(yi>=0)&&(yi<m_in.ydim)){
         count1+=m_in.dat.get(z,yi,xi);
         count2+=m_in.dat.get(z+1,yi,xi);
       }//in bounds
      }//xx
      }//yy
       if ((count1<intthr)&&(count2<intthr)) tmp.dat.set(0,y,x,1);
      }//if>0

      }//x
      }//y
    for (int x=0;x<m_in.xdim;x++){
     for (int y=0;y<m_in.ydim;y++){
       if (tmp.dat.get(0,y,x)==1) m_in.dat.set(z,y,x,0);
      }
     }
    }//z
  }
}