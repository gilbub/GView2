package gvdecoder;
import java.awt.image.BufferedImage;
import java.util.*;
import delaunay_triangulation.*;

public class ImageMaker11{

int[] arr;
int groupsize;

public void getRGBi(int pixval,int[] rgbs){
  int R= (pixval & 0x00FF0000) >> 16;
  int G= (pixval & 0x0000FF00) >> 8;
  int B= pixval & 0x000000FF;
  rgbs[0]=R;
  rgbs[1]=G;
  rgbs[2]=B;
}


public int RGBtoPix(int R, int G, int B){
	return 0xFF000000+(R<<16)+(G<<8)+B;
}


public BufferedImage makeimage(int outpix_x, int outpix_y, BufferedImage moving, BufferedImage still,  int moving_x, int moving_y, int still_x, int still_y, int shiftdistance){
  int green=RGBtoPix(0,255,0);
  int mwidth=moving.getWidth();
  int mheight=moving.getHeight();
  int[] cols=new int[3];
  int[] r_array=new int[((mwidth+shiftdistance)*mheight)];
  int[] g_array=new int[((mwidth+shiftdistance)*mheight)];
  int[] b_array=new int[((mwidth+shiftdistance)*mheight)];
  BufferedImage outimage=new BufferedImage(outpix_x,outpix_y,java.awt.image.BufferedImage.TYPE_3BYTE_BGR);
  java.awt.Graphics g=outimage.getGraphics();

  for (int i=0;i<shiftdistance;i++){
	  g.setColor(java.awt.Color.WHITE);
	  g.fillRect(0,0,outpix_x,outpix_y);
	  g.drawImage(still,still_x,still_y,null);
	  for (int y=0;y<mheight;y++){
	       for (int x=0;x<mwidth;x++){
	         int p=moving.getRGB(x,y);
	         if (p!=green){
                outimage.setRGB(x+moving_x+i,y+moving_y,p);
			}
		 }
	   }
	  for (int y=0;y<mheight;y++){
		 for (int x=0;x<mwidth+shiftdistance;x++){
			  int pixel=outimage.getRGB(x+moving_x,y+moving_y);
			  getRGBi(pixel,cols);
			  int index=y*(mwidth+shiftdistance)+x;
			  r_array[index]+=cols[0];
			  g_array[index]+=cols[1];
			  b_array[index]+=cols[2];
		  }
	  }
    }//shiftdistnce
   g.setColor(java.awt.Color.WHITE);
   g.fillRect(0,0,outpix_x,outpix_y);
   g.drawImage(still,still_x,still_y,null);
   for (int y=0;y<mheight;y++){
	   for (int x=0;x<mwidth+shiftdistance;x++){
		       int index=y*(mwidth+shiftdistance)+x;
		       int R=(int)(r_array[index]/((double)shiftdistance));
		       int G=(int)(g_array[index]/((double)shiftdistance));
		       int B=(int)(b_array[index]/((double)shiftdistance));
		       int pixel=RGBtoPix(R,G,B);
      		   outimage.setRGB(x+moving_x,y+moving_y,pixel);
		  }
	  }
  g.dispose();
  return outimage;
}

public int[] getIndexArray(int elements){
	int[] res=new int[elements];
	for (int i=0;i<res.length;i++){
		res[i]=i;
	}
	return res;
}

public boolean randomize=true;
public void setIndexArray(int elements, int groupsize, long seed){
	int si,v1,v2,ii;
	this.groupsize=groupsize;
	java.util.Random rand=new java.util.Random(seed);
	arr=new int[elements];
	for (int i=0;i<elements;i+=groupsize){

		for (int j=0;j<groupsize;j++){
			arr[i+j]=j;
		}
		if (randomize){
		for (int j=0;j<groupsize;j++){
			si=rand.nextInt(groupsize);
			v1=arr[i+j];
		    v2=arr[i+si];
		    arr[i+j]=v2;
			arr[i+si]=v1;
         }
	     }
		}
	}


public void encode(int[] tmp, BufferedImage image, int subcount, int t_index, BufferedImage bi_out, java.util.Random rand){

 int xi,yi,ci,z,m_y,m_x,x,y;
 int xdim=image.getWidth();
 int ydim=image.getHeight();
 int s_xdim=(int)((double)xdim/subcount);
 int s_ydim=(int)((double)ydim/subcount);
 for ( x=0;x<s_xdim;x++){
   for ( y=0;y<s_ydim;y++){
     scramble(tmp,rand);
     xi=x*subcount;
     yi=y*subcount;
     ci=-1;
     for (z=0;z<tmp.length;z++){
		 if (tmp[z]==t_index){
	       ci=z;
	       break;
		 }
	 }
	 m_y=(int)((double)ci/subcount);
	 m_x=ci-(m_y*subcount);
     bi_out.setRGB(xi+m_x,yi+m_y,image.getRGB(xi+m_x,yi+m_y));
 }
 }
}


int xi,yi,ci,z,m_y,m_x,x,y,index,i,j;

public void encode(BufferedImage image, int subcount, int t_index, BufferedImage bi_out){

 //int xi,yi,ci,z,m_y,m_x,x,y,index;
 int xdim=image.getWidth();
 int ydim=image.getHeight();
 int s_xdim=(int)((double)xdim/subcount);
 int s_ydim=(int)((double)ydim/subcount);
 for ( x=0;x<s_xdim;x++){
   for ( y=0;y<s_ydim;y++){
     //scramble(tmp,rand);
     xi=x*subcount;
     yi=y*subcount;
     index=yi*xdim+xi;
     ci=-1;
     for (z=index;z<index+groupsize;z++){
		 if (arr[z]==t_index){
	       ci=z-index;
	       break;
		 }
	 }
	 m_y=(int)((double)ci/subcount);
	 m_x=ci-(m_y*subcount);
     bi_out.setRGB(xi+m_x,yi+m_y,image.getRGB(xi+m_x,yi+m_y));
 }
 }
}



public void decode(int [] tmp, BufferedImage image, int subcount, int t_index, BufferedImage bi_out, java.util.Random rand){
  int xi,yi,ci,z,m_y,m_x,x,y;
  int xdim=image.getWidth();
  int ydim=image.getHeight();
  int s_xdim=(int)((double)xdim/subcount);
  int s_ydim=(int)((double)ydim/subcount);
  for ( x=0;x<s_xdim;x++){
	for ( y=0;y<s_ydim;y++){
	     scramble(tmp,rand);
	     xi=x*subcount;
	     yi=y*subcount;
	     ci=-1;
	     for (z=0;z<tmp.length;z++){
			 if (tmp[z]==t_index){
		       ci=z;
		       break;
			 }
		 }
		 m_y=(int)((double)ci/subcount);
		 m_x=ci-(m_y*subcount);
         bi_out.setRGB(x,y,image.getRGB(xi+m_x,yi+m_y));
	 }
	 }
}


public void decode(BufferedImage image, int subcount, int t_index, BufferedImage bi_out){
  //int xi,yi,ci,z,m_y,m_x,x,y,index;
  int xdim=image.getWidth();
  int ydim=image.getHeight();
  int s_xdim=(int)((double)xdim/subcount);
  int s_ydim=(int)((double)ydim/subcount);
  for ( x=0;x<s_xdim;x++){
	for ( y=0;y<s_ydim;y++){
	     //scramble(tmp,rand);
	     xi=x*subcount;
	     yi=y*subcount;
	     index=yi*xdim+xi;
		 ci=-1;
		 for (z=index;z<index+groupsize;z++){
		   if (arr[z]==t_index){
		 	     ci=z-index;
		 	     break;
		 	}
	     }
		 m_y=(int)((double)ci/subcount);
		 m_x=ci-(m_y*subcount);
         bi_out.setRGB(x,y,image.getRGB(xi+m_x,yi+m_y));
	 }
	 }
}

public boolean[] touched;

public void decode_hi(BufferedImage image, int subcount, int t_index, BufferedImage bi_out){
  //int xi,yi,ci,z,m_y,m_x,x,y,i,j,index;
//  int black=0x00000000; //set pixels transparent, use this as a way to tell that the pixel val has been assigned
  int xdim=image.getWidth();
  int ydim=image.getHeight();
  int s_xdim=(int)((double)xdim/subcount);
  int s_ydim=(int)((double)ydim/subcount);
  touched=new boolean[xdim*ydim];
  for (x=0;x<xdim;x++){
  	  for (y=0;y<ydim;y++){
  		touched[y*xdim+x]=false;
  	  }
    }
  for ( x=0;x<s_xdim;x++){
	for ( y=0;y<s_ydim;y++){
	     //scramble(tmp,rand);
	     xi=x*subcount;
	     yi=y*subcount;
	     index=yi*xdim+xi;
		 ci=-1;
		 for (z=index;z<index+groupsize;z++){
		   if (arr[z]==t_index){
		 	     ci=z-index;
		 	     break;
		 	}
	     }
		 m_y=(int)((double)ci/subcount);
		 m_x=ci-(m_y*subcount);
         bi_out.setRGB(xi+m_x,yi+m_y,image.getRGB(xi+m_x,yi+m_y));
         touched[(yi+m_y)*xdim+(xi+m_x)]=true;
	 }
	 }



}

public int getAlpha(int pixval){
   int A= (pixval & 0xFF000000) >> 24;
   if (A<0){A=256+A;}
   return A;
}

public void interp(BufferedImage image, int sx, int sy, int ex, int ey, int w){
	int[] rgb_1=new int[3];
	int[] rgb_2=new int[3];
	int[] rgb_3=new int[3];
    int xdim=image.getWidth();
    int ydim=image.getHeight();
	int x,y,i,j,c1,c2,c3;
	double x1,y1,x2,y2,x3,y3,ri,gi,bi;

	ArrayList list=new ArrayList();
	for ( x=sx;x<ex;x++){
		for ( y=sy;y<ey;y++){
		     //scramble(tmp,rand);
		     if (!touched[y*xdim+x]){
			 list.clear();
		     for (i=-w;i<(w+1);i++){
				 for (j=-w;j<(w+1);j++){
				   if (touched[(y+i)*xdim+(x+j)]){
					   //add this to list of potential pixels
					   list.add(new Point_dt(x+j,y+i));
				   }
				}
			  }
			  Point_dt[] dts=new Point_dt[list.size()];
			  for (int k=0;k<list.size();k++){ dts[k]=(Point_dt)list.get(k);}
			  Delaunay_Triangulation dt=new Delaunay_Triangulation(dts);
			  Triangle_dt tri=dt.find(new Point_dt(x,y));
			  x1=tri.p1().x(); y1=tri.p1().y();
			  x2=tri.p2().x(); y2=tri.p2().y();
			  x3=tri.p3().x(); y3=tri.p3().y();
			  c1=image.getRGB((int)x1,(int)y1);
			  c2=image.getRGB((int)x2,(int)y2);
			  c3=image.getRGB((int)x3,(int)y3);
			  getRGBi(c1,rgb_1);
			  getRGBi(c2,rgb_2);
			  getRGBi(c3,rgb_3);
			  Triangle_dt redt  =new Triangle_dt(new Point_dt(x1,y1,(double)rgb_1[0]),new Point_dt(x2,y2,(double)rgb_1[0]),new Point_dt(x3,y3,(double)rgb_1[0]));
			  Triangle_dt greent=new Triangle_dt(new Point_dt(x1,y1,(double)rgb_1[1]),new Point_dt(x2,y2,(double)rgb_1[1]),new Point_dt(x3,y3,(double)rgb_1[1]));
			  Triangle_dt bluet =new Triangle_dt(new Point_dt(x1,y1,(double)rgb_1[2]),new Point_dt(x2,y2,(double)rgb_1[2]),new delaunay_triangulation.Point_dt(x3,y3,(double)rgb_1[2]));
			  ri=redt.z(x,y);
			  gi=greent.z(x,y);
			  bi=bluet.z(x,y);
			  //transparent color
			  image.setRGB(x,y,((int)ri<<16)+((int)gi<<8)+(int)bi);




			  //Collections.sort(list,dc);
              // grab first three


		    }

		  }
	   }



}



public void scramble(int[] marr, java.util.Random rand){
	 int si,v1,v2;
	 for (int i=0; i<marr.length;i++){
	  si=rand.nextInt(marr.length);
	  v1=marr[i];
	  v2=marr[si];
	  marr[i]=v2;
      marr[si]=v1;
  }
}

}



class xy{
	int x;
	int y;
	public xy(int x, int y){
	  this.x=x;
	  this.y=y;

  }
}