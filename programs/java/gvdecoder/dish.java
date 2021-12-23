package gvdecoder;

import java.awt.*;
import java.util.*;
import java.lang.reflect.*;

public class dish extends Canvas implements drawable {
Graphics bg;
Image buffer;

int xScreenDimension;
int yScreenDimension;
Color[] lastframe;
int[] cellarray;
int xwidth;
int ywidth;
int tilesize;
sim spv;  // a link to the program running the simulation
double scale=1; // the scaling between display and actual position.
int cellWidth=5;



public Image returnimage(){return buffer;}

public dish(int[] cellarray, int xwidth, int ywidth,  int screensize, sim sm){
 this.xwidth=xwidth;
 this.ywidth=ywidth;
 this.cellarray=cellarray;
 this.spv=sm;
 lastframe=new Color[cellarray.length];
 scale=(double)screensize/xwidth; //screensize is the x dimension of the screen, y is scaled.
 xScreenDimension=screensize+1;
 yScreenDimension=(int)((double)ywidth*scale)+1;
 tilesize=(int)scale;
 }

public Dimension getPreferredSize(){
 return new Dimension(xScreenDimension,yScreenDimension);
}
public void update(Graphics g){
   	paint(g);
}

public void paint(Graphics g){
 if (cellarray!=null) draw();
}






public void draw(){
 Color val;
 int thisCell;
 try{
 if (buffer==null){
      buffer=this.createImage(xScreenDimension, yScreenDimension);
	  bg=buffer.getGraphics();
	  bg.setColor(Color.black);
      bg.fillRect(0,0,xScreenDimension,yScreenDimension);
     }
 if (bg==null) bg=buffer.getGraphics();
 for (int j=0;j<ywidth;j++){
  for (int i=0;i<xwidth;i++){
       thisCell=cellarray[j*xwidth+i];
	   val=Color.gray;
	   if (thisCell==0) val=Color.black; else
	   if (thisCell==1) val=Color.red; else
	   if (thisCell==2) val=Color.blue;
	   if (val!=lastframe[j*xwidth+i]){
	    lastframe[i]=val;
	    bg.setColor(val);
   	    bg.fillRect((int)(i*scale),(int)(j*scale),tilesize,tilesize);
       }//if val
 }//i
}//j
 Graphics g=this.getGraphics();
 g.drawImage(buffer,0,0,this);
 bg.dispose();
 g.dispose();
 }catch(Exception e){System.out.println("problem getting graphics");}

}






}