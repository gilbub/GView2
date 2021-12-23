package gvdecoder;
/*given an array, transforms into an array with xdim+2, ydim+2 of doubles
  put Double.MINVALUE in the edges, other values in center
  finds max min of original values, divides by num
*/

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.awt.geom.*;
import java.util.*;
import java.awt.image.*;
import java.awt.image.renderable.*;
import javax.media.jai.*;

public class ContourHelper extends JPanel{

 public int num_contours;
 public double max;
 public double min;

 public double[][] dat;
 public int[][] blobs;
 public GeneralPath [] contour;
 int xdim;
 int ydim;
 int[][] clockwise;


 public Dimension getPreferredSize(){
 	return new Dimension(xdim*10,ydim*10);
	}

 public ContourHelper(double[][] raw, int num_contours){
	dat=new double[raw.length+2][raw[0].length+2];
	contour=new GeneralPath[num_contours];
    this.num_contours=num_contours;
	ydim=raw.length+2;
	xdim=raw[0].length+2;
	blobs=new int[ydim][xdim];
	findMaxMin(raw);
	for (int y=0;y<ydim;y++){
		for (int x=0;x<xdim;x++){
		dat[y][x]=Double.MIN_VALUE;
  		}
     }
     for (int y=1;y<ydim-1;y++){
	 		for (int x=1;x<xdim-1;x++){
	 		dat[y][x]=raw[y-1][x-1];
	   		}
     }
     clockwise=new int[8][3];
     clockwise[0][1]=0;
     clockwise[0][2]=-1;

     clockwise[1][1]=1;
     clockwise[1][2]=-1;

     clockwise[2][1]=1;
     clockwise[2][2]=0;

     clockwise[3][1]=1;
     clockwise[3][2]=1;

     clockwise[4][1]=0;
     clockwise[4][2]=1;

     clockwise[5][1]=-1;
     clockwise[5][2]=1;

     clockwise[6][1]=-1;
     clockwise[6][2]=0;

     clockwise[7][1]=-1;
     clockwise[7][2]=-1;

	printArray();


 }

 public boolean inside(double val, double hi, double lo){
	return ((val>=lo)&&(val<hi));
 }

 public boolean isConnected(int y, int x, int blobnum){
	  boolean connected=false;
	  for (int i=-1;i<=1;i++){
	 	 for (int j=-1;j<=1;j++){
	 		 int yn=y+i;
	 		 int xn=x+j;
			     if (!((yn==y)&&(xn==x))){
				   if (blobs[yn][xn]==blobnum){
					   connected=true;
				       break;
				    }

				 }
				 if (connected) break;
				 }
			}
  return connected;
 }

 public int generateBlobs(double hi, double lo){
  for (int y=0;y<ydim;y++){
	 for (int x=0;x<xdim;x++){
		 blobs[y][x]=0;
	 }
   }
   int blobnum=1;
   boolean found=true;
   while (found){
	 found=false;
    for (int y=1;y<ydim-1;y++){
   	 for (int x=1;x<xdim-1;x++){
		 if (!found){
		 if ((inside(dat[y][x],hi,lo))&&(blobs[y][x]==0)){
			 blobs[y][x]=blobnum;
			 found=true;
		     break;
		   }
	      }
		  if (found) break;
	     }
	     if (found) break;
        }
    if (found){
    for (int y=1;y<ydim-1;y++){
		for (int x=1;x<xdim-1;x++){
			if ((inside(dat[y][x],hi,lo))&&(isConnected(y,x,blobnum)))
			  blobs[y][x]=blobnum;
		}
	  }
     }//found
   if (found) blobnum++;
  }

  printBlobs();
  return blobnum-1;
 }

 public boolean initgp=false;
 public void addToPath(GeneralPath gp, int y, int x, int blob){
	 //starting from y,x, circle around and find nbs part of blob,
	 //and add a pt to general path
	boolean done=false;
	for (int q=0;q<clockwise.length;q++){
			int yn=y+clockwise[q][2];
		    int xn=x+clockwise[q][1];
			if ((xn>=0)&&(xn<xdim)&&(yn>=0)&&(yn<ydim)){
			 boolean found=false;
			 if (!done){

			   if ((blobs[yn][xn]==blob)){
				   float xf=((float)(xn+x)/2.0f);
				   float yf=((float)(yn+y)/2.0f);
				   if (!initgp){
					gp.moveTo(xf*10,yf*10);
					initgp=true;
				   }else{
					 gp.lineTo(xf*10,yf*10);
				   }
			       found=true;
			     }
			      else{
					 if (found){//ive started the path, but ran into a wall
						done=true;
					 }
				  }
	 	}
	   }

   }
 }


 public void addToPath(GeneralPath gp,int y, int x, int by, int bx, int blob){
	  float xa=((float)(x+bx))/2f;
	  float ya=((float)(y+by))/2f;
      System.out.println("adding to path=("+x+"+"+bx+"="+xa+")  , ("+y+"+"+by+"="+ya+")");
	  if (!initgp){
	 					gp.moveTo(xa*10,ya*10);
	 					initgp=true;
	 				   }else{
	 					 gp.lineTo(xa*10,ya*10);
				   }

 }
 int nbx=0;
 int nby=0;
 public boolean hasBlobNb(int y, int x, int blob){
	 	boolean found=false;
	 	for (int j=-1;j<=1;j++){
		 for (int i=-1;i<=1;i++){
			 int xn=x+i;
			 int yn=y+j;
			 if ((xn>=0)&&(xn<xdim)&&(yn>=0)&&(yn<ydim)){
				if (blobs[yn][xn]==blob) {
					nbx=xn;
					nby=yn;
					found=true;
					break;
					}
		 }
		if (found) break;
	 }
	 if (found) break;
 }
 System.out.println("nb="+nbx+" "+nby);
 return found;
}

 public GeneralPath findContour2(double hi, double lo){
	GeneralPath gp=new GeneralPath();

	int numblobs= generateBlobs(hi,lo);
	for (int b=1;b<=numblobs;b++){
		int startx=0;
		int starty=0;
		initgp=false;
		boolean found=false;
		for (int y=1;y<ydim-1;y++){
		 for (int x=1;x<xdim-1;x++){
			if (!found){
			if (blobs[y][x]==b){
				startx=x;
				starty=y;
				found=true;
		       }
			}
			if (found) break;
		  }
		 if (found) break;//necissary?
	    }

	    //now march around edge
	    starty-=1;
	    int xo=startx;
	    int yo=starty;
	    int newx=-1;
	    int newy=-1;
	    int yn=0;
	    int xn=0;
	    addToPath(gp,starty,startx,starty+1,startx,b);
	    System.out.println("starting search with "+xo+" "+yo);
	    while (!((startx==newx)&&(starty==newy))){
	    boolean foundnext=false;
	    //for (int i=-1;i<=1;i++){
		//	for (int j=-1;j<=1;j++){
			 for (int q=0;q<clockwise.length;q++){
				    yn=yo+clockwise[q][2];
				    xn=xo+clockwise[q][1];

					System.out.println("check="+xn+" "+yn);
                     if ((xn>=0)&&(xn<xdim)&&(yn>=0)&&(yn<ydim)){
					if ((blobs[yn][xn]>=0)&&
					    (blobs[yn][xn]!=b)&&
					    (hasBlobNb(yn,xn,b))){
						newx=xn;
						newy=yn;
						foundnext=true;
						blobs[yn][xn]=b*-1;
						System.out.println("found next="+xn+" "+yn);
					  }

				 if (foundnext)break;
			     }
			 }

		   addToPath(gp,newy,newx,nby,nbx,b);
		   yo=newy;
		   xo=newx;
	   }




	}//blobs
	printBlobs();
	return gp;
 }

 public GeneralPath findContour(double hi,double lo){
	 //draw a generalpath around values between hi,low
	 GeneralPath gp=new GeneralPath();
	 boolean init=false;
	// int blobs= generateBlobs(hi,lo);
	 for (int y=1;y<ydim-1;y++){
		 for (int x=1;x<xdim-1;x++){
		   if (inside(dat[y][x],hi,lo)){
			  for (int i=-1;i<=1;i++){
				  for (int j=-1;j<=1;j++){
					  int yn=y+i;
					  int xn=x+j;
					  if (!((yn==y)&&(xn==x))){
						 if (!inside(dat[yn][xn],hi,lo)){
							float xloc=(float)(((float)(xn+x))/2);
							float yloc=(float)(((float)(yn+y))/2);
							if (!init){
							     gp.moveTo(xloc*10,yloc*10);
							     init=true;
							     }else{
								  gp.lineTo(xloc*10,yloc*10);
							     }
							   //System.out.println("added "+xloc+" "+yloc);
							 }
						   }

						 }//j
					  }//i

		   			}//is inside

		   }//x

		 }//y

	  return gp;
	 }



 public void findAllContours(){
	 contour[0]=findContour2(4.1,1.0);
     System.out.println("contour[0]="+(contour[0]!=null));
 }

 public void paint(Graphics g){
	 render();
 }

 public void render(){
	Graphics2D g2=(Graphics2D)this.getGraphics();
	g2.setStroke(new BasicStroke(2.0f));
		 for (int i=0;i<num_contours;i++){
		 if (contour[i]!=null){
		   g2.draw(contour[0]);
		   System.out.println("drew contour ");
	     }
	   }
	 g2.dispose();

 }

 public void findMaxMin(double[][] raw){
  max=Double.MIN_VALUE;
  min=Double.MAX_VALUE;
  for (int i=0;i<raw.length;i++){
	  for (int j=0;j<raw[0].length;j++){
		double val=raw[i][j];
		if (val>max) max=val;
		if (val<min) min=val;
	  }
  	}
 }

 public void printArray(){
	for (int y=0;y<ydim;y++){
		for (int x=0;x<xdim;x++){
		 if (dat[y][x]==Double.MIN_VALUE) System.out.print("# \t");
		  else System.out.print((float)dat[y][x]+"\t");

		}
	  System.out.println("");
	}
 }

 public void printBlobs(){
 	for (int y=0;y<ydim;y++){
 		for (int x=0;x<xdim;x++){

 		  System.out.print(blobs[y][x]+"\t");

 		}
 	  System.out.println("");
 	}
 }

public static void main(String[] arg){

	double[][] dat=new double[5][5];
	for (int i=0;i<5;i++){
		for (int j=0;j<5;j++){
		  dat[i][j]=i*j;
		}
	}

	//dat[3][3]=2.0;
	dat[4][4]=2.0;
	ContourHelper ch=new ContourHelper(dat,5);
	ch.findAllContours();
	System.out.println("blobs="+ch.generateBlobs(4.1,1.0));

	JFrame frame=new JFrame("hello");


	frame.getContentPane().add(ch);
	frame.pack();
	frame.show();
	ch.render();



}




}