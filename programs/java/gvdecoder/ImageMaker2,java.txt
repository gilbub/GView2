package gvdecoder;
import java.awt.image.BufferedImage;

public class ImageMaker2{

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
  return outimage;
}
}
