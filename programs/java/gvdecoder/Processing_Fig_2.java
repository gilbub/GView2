package gvdecoder;

import processing.core.*;
import processing.data.*;
import processing.event.*;
import processing.opengl.*;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import javax.imageio.ImageIO;

import java.awt.image.ColorModel;

import peasy.*;

import java.util.HashMap;
import java.util.ArrayList;
import java.io.File;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;

public class Processing_Fig_2 extends PApplet {

  PeasyCam cam;

  float x1=-7.9923553f;
  float x2=502.935f;
  float y1=-7.67119f;
  float y2=503.0f;
  float z1=-15.324852f;
  float z2=487.3602f;
  float xpos,ypos,zpos,tp,dist;


 //float _cx=0;
 //float _cy=0;
 //float _cz=0; //not used
 int framenumber=30;
 int maxframes=105;
 int minframes=30;
 public String savepath        = "D://data//sacconiscope//figure2//frames7//fr_######.png";
 //public String stlpath         = "D://data//sacconiscope//figure2//reduced.stl";
 public String stlpath         = "D://data//sacconiscope//figure2//fig_2_adjusted_sm.stl";
 public String voltagedatapath = "D://data//sacconiscope//figure2//voltage//";
 public int zmult=70;                 // subsequent layers displaced by this much (eg 70 pix in z = 40 um)
 public int zoffset=32;               // first layer starts here
 public int layer_view_threshold=100; //only display voltages if alpha is greater than this.
 public int mesh_alpha=25;
 public int bounds_alpha=100;
 public boolean draw_orientation_box;
 public int bounds_weight=1;
 public boolean bound_each_layer=true;
 public int xoffset_correction=0;
 public int background_value=255; //255=white, 0=black

public void setup() {

  openfile();
  cam = new PeasyCam(this, 100);
  cam.setActive(false);

  layerdata=new imagedata[7][10000];
  mesh=new FloatList();
  makemesh(5,12,0,0,0);
  //xpos=50;
  //zpos=340;
  //ypos=670;
  xpos=0;
  ypos=0;
  zpos=0;
  tp=1.0f;
  dist=500;
  //colorMode(RGB, 1);

  frameRate(10);


//minx,miny,minz=-7.9923553,-7.67119,-15.324852 maxx,maxy,maxz=502.935,503.0,487.3602

  //minx,miny,minz=-7.9923553,-7.67119,-6.385355 maxx,maxy,maxz=502.935,503.0,203.06674

  background(0);
  stroke(255,0,0,150);
  //camera(-100,-600,-500,(x1+x2)/2,(y1+y2)/2,(z1+z2)/2,0,1,0);
  noFill();
  //box(200);
  //translate(width/2, height/2, 0);

  drawmesh(0.25f);
  //heart.setStroke(color(255));
  //shape(heart);
  stroke(0,255,0,100);
  makebounds();
  fill(0,100,100);
  float xm=(x2-x1)/100.0f;
  float ym=(y2-y1)/100.0f;
  parseFiles();
  //addlayer(x1,y1,z1,xm*1.0,ym*1.0,10,0,0,255);
  //addlayer(x1,y1,z1+250,xm*1.0,ym*1.0,10,255,0,0);
  //addlayer(x1,y1,z1+350,xm*1.0,ym*1.0,10,0,255,0);
  adddatalayer(2,x1,y1,z1+250,xm*1.0f,ym*1.0f);
}

public void parseFiles(){
 for (int i=0;i<7;i++){
   parseFile(i);
 }
}


public void adddatalayer(int layer, float xoffset, float yoffset, float zoffset, float xm, float ym){
  //parseFile(layer);

  if (bound_each_layer){
	  stroke(255,0,0,bounds_alpha);
	  translate(xoffset+xoffset_correction,yoffset,zoffset);
	  noFill();
	  rect(0,0,100*xm, 100*ym);
	  translate(-(xoffset+xoffset_correction),-yoffset,-zoffset);
  }

  for (int y=0;y<100;y+=1){
    for(int x=0;x<100;x+=1){
      translate(xoffset+x*xm+xoffset_correction,yoffset+y*ym,zoffset);
      imagedata i=layerdata[layer][y*100+(99-x)];
      if (i.a>layer_view_threshold){
      stroke(i.r,i.g,i.b,i.a);
      fill(i.r,i.g,i.b,i.a);
      box(i.w,i.w,i.h/10.0f);
      }
      translate(-(xoffset+x*xm+xoffset_correction),-(yoffset+y*ym),-(zoffset));
    }
  }
}


public void addlayer(float xoffset, float yoffset, float zoffset, float xm, float ym, float w,int r, int g, int b){
  for (int x=0;x<100;x+=10){
    for(int y=0;y<100;y+=10){
      translate(xoffset+x*xm,yoffset+y*ym,zoffset);
      //println("x,y,z = "+(xoffset+x*xm)+","+(yoffset+y*ym)+","+zoffset);
      stroke(r,g,b,250);
      fill(r,g,b,250);
      box(w,w,w);
      translate(-(xoffset+x*xm),-(yoffset+y*ym),-(zoffset));
    }
  }
}
int imagenumber=0;
public void saveimage(){
 String filename="threed"+imagenumber+".tif";
 save(filename);
 imagenumber+=1;

}

boolean drawvoltage=true;
boolean drawthemesh=true;
int cx,cy,cz,cv;
boolean xposchanged=false;
boolean yposchanged=false;
boolean zposchanged=false;
boolean saveavi=false;
public void keyPressed(){
  xposchanged=false;
  yposchanged=false;
  zposchanged=false;
  if (key=='a'){ if (saveavi) saveavi=false; else saveavi=true;}
  if (key=='f'){framenumber+=1; if (framenumber>maxframes) framenumber=maxframes; parseFiles();}
  if (key=='F'){framenumber-=1; if (framenumber<minframes) framenumber=minframes; parseFiles();}
  if (key=='s'){saveimage();}
  if (key=='x'){ xpos=5; xposchanged=true;}
  if (key=='X'){ xpos=-5; xposchanged=true;}
  if (key=='y'){ ypos=5; yposchanged=true;}
  if (key=='Y'){ ypos=-5; yposchanged=true;}
  if (key=='z'){ zpos=5; zposchanged=true;}
  if (key=='Z'){ zpos=-5; zposchanged=true;}
  if (key=='d'){ dist+=30;}
  if (key=='D'){ dist-=30;}
  if (key=='t'){ tp+=0.05f; if (tp>1.0f) tp=1.0f;}
  if (key=='T'){ tp-=0.05f; if (tp<=0.0f) tp=0.0f;}
  if (key=='r'){ xpos=0.0f;ypos=0.0f;zpos=0.0f; dist=830.0f;}
  if (key=='l') parseFiles();
  if (key=='b') {if (getbufferedimage) getbufferedimage=false; else getbufferedimage=true;}
  if (key=='v') {if (drawvoltage) drawvoltage=false; else drawvoltage=true;}
  if (key=='m') {if (drawthemesh) drawthemesh=false; else drawthemesh=true;}
  if (key=='c') {cv++; cx=0;cy=0;cz=0; if (cv%3==0) cx=1; if (cv%3==1) cy=1; if (cv%3==2) cz=1;}
  print("dist="+dist+" xp="+xpos+" yp="+ypos+" zp="+zpos+" tp="+tp+"c(x,y,z)"+cx+","+cy+","+cz+"\n");
  drawit();
}

public void jrotate(float x, float y, float z){
	xposchanged=true;
	yposchanged=true;
	zposchanged=true;
	xpos=x;
	ypos=y;
	zpos=z;
	jython_request=true;
}

public void jback(float d){
	dist-=d;
	jython_request=true;
}

public void jsaveposition(){
	pushMatrix();
	jython_request=true;
}

public void jloadposition(){
	popMatrix();
    jython_request=true;
}

public void jforward(float d){
	dist+=d;
	jython_request=true;
}

public void jsave(boolean v){
	saveavi=v;
}

public boolean jython_request=false;
public void draw(){
	if (jython_request){
		drawit();
		jython_request=false;
	}
}

public String doNothing(){ return "hello";}



public void drawit(){
   background(background_value);

   if (xposchanged) cam.rotateX(xpos*0.01745329f);
   if (yposchanged) cam.rotateY(ypos*0.01745329f);
   if (zposchanged) cam.rotateZ(zpos*0.01745329f);
   //if (saveavi) cam.rotateY(0.01745329f);
   cam.lookAt((minx+maxx)/2,(miny+maxy)/2,(minz+maxz)/2,dist);
   if (draw_orientation_box){
    pushMatrix();
    translate( (minx+maxx)/2, (miny+maxy)/2, (minz+maxz)/2);
    stroke(255,0,255,255);
    box(5,5,5);
    popMatrix();
    box(5,5,5);
   }
   x1=minx; x2=maxx;
   y1=miny; y2=maxy;
   z1=minz;
   stroke(0,0,255,mesh_alpha);
   if (drawthemesh)drawmesh(tp);
   strokeWeight(bounds_weight);
   stroke(0,255,0,bounds_alpha);
   makebounds();
   strokeWeight(1);
   float xm=(-x1)/100.0f;
  float ym=(y2-y1)/100.0f;
  //addlayer(x1,y1,z1,xm*1.0,ym*1.0,10,0,0,255);
  // int offset=32;
  // int mult=70;
  if (drawvoltage){
   adddatalayer(0,x1,y1,z1+zoffset,xm*1.0f,ym*1.0f);
   adddatalayer(1,x1,y1,z1+zmult*1+zoffset,xm*1.0f,ym*1.0f);
   adddatalayer(2,x1,y1,z1+zmult*2+zoffset,xm*1.0f,ym*1.0f);
   adddatalayer(3,x1,y1,z1+zmult*3+zoffset,xm*1.0f,ym*1.0f);
   adddatalayer(4,x1,y1,z1+zmult*4+zoffset,xm*1.0f,ym*1.0f);
   adddatalayer(5,x1,y1,z1+zmult*5+zoffset,xm*1.0f,ym*1.0f);
   adddatalayer(6,x1,y1,z1+zmult*6+zoffset,xm*1.0f,ym*1.0f);
  }

  if (saveavi) saveFrame(savepath);
  if (getbufferedimage) getBufferedImage();
 }

public BufferedImage deepCopy(BufferedImage bi) {
 ColorModel cm = bi.getColorModel();
 boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
 WritableRaster raster = bi.copyData(null);
 return new BufferedImage(cm, raster, isAlphaPremultiplied, null);
}

BufferedImage bi;
boolean getbufferedimage=false;
public void getBufferedImage(){
	bi=(BufferedImage)g.getNative();
}
int[] last_rgba=new int[]{0,0,0,0};
public int[] getRGBA(int x, int y){
	return getRGBA(bi,x,y);
}
public int[] getRGBA(BufferedImage bi, int x, int y){
	int p=bi.getRGB(x,y);
	int a = (p>>24) & 0xff;
	int r = (p>>16) & 0xff;
	int g = (p>>8) & 0xff;
	int b = p & 0xff;
	last_rgba[0]=r;
	last_rgba[1]=g;
	last_rgba[2]=b;
	last_rgba[3]=a;
	return last_rgba;
}

public BufferedImage blend_images(BufferedImage bi1, BufferedImage bi2, double w1){
	int p1,p2,p3,a1,r1,g1,b1,a2,r2,g2,b2,a3,r3,g3,b3;
	int h=bi1.getHeight();
	int w=bi1.getWidth();
	if ((bi2.getHeight()!=h)||(bi2.getWidth()!=w)){
	 System.out.println("Processing_fig_2 error, dimensions of the buffered images don't match 1 w,h = "+w+","+h+" 2 w,h = "+bi2.getWidth()+","+bi2.getHeight() );
	 return null;
	}
    BufferedImage bout = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
    for (int y=0;y<h;y++){
		for (int x=0;x<w;x++){
			 p1=bi1.getRGB(x,y);
			 p2=bi2.getRGB(x,y);
			 a1 = (p1>>24) & 0xff;
			 r1 = (p1>>16) & 0xff;
		     g1 = (p1>>8) & 0xff;
	         b1 = p2 & 0xff;
			 a2 = (p2>>24) & 0xff;
			 r2 = (p2>>16) & 0xff;
		     g2 = (p2>>8) & 0xff;
	         b2 = p2 & 0xff;
	         a3=(int)(a1*w1+a2*(1-w1));
			 r3=(int)(r1*w1+r2*(1-w1));
			 g3=(int)(g1*w1+g2*(1-w1));
			 b3=(int)(b1*w1+b2*(1-w1));

			 p3 = (a3<<24) | (r3<<16) | (g3<<8) | b3;
			 bout.setRGB(x,y,p3);
		}
	}
   return bout;
}

public boolean writeBufferedImage(BufferedImage bi, String filename, String filetype){
	try{
		File outputfile = new File(filename);
        ImageIO.write(bi, filetype, outputfile);
	return true;
  }catch(Exception e){e.printStackTrace();}
  return false;
}

public void makeshape(float o){
  beginShape();
  float i=10+o;
vertex(-i, -i, -i);
vertex( i, -i, -i);
vertex(   o,    o,  i);

vertex( i, -i, -i);
vertex( i,  i, -i);
vertex(   o,    o,  i);

vertex( i, i, -i);
vertex(-i, i, -i);
vertex(   o,   o,  i);

vertex(-i,  i, -i);
vertex(-i, -i, -i);
vertex(   o,    o,  i);
endShape();
}


class imagedata{
  int r;
  int g;
  int b;
  int a;
  float w;
  float h;
  public imagedata(int rr,int bb, int gg, int aa, int hh){
    r=rr;
    g=gg;
    b=bb;
    a=aa;
    w=2;
    h=hh;
  }

 public imagedata(int i){
   r=i;
   g=i;
   b=i;
   a=255;
   w=2;
   h=2;
 }

 public imagedata(int rr, int gg, int bb, int aa, float ww, float hh){
   r=rr; g=gg; b=bb;
   a=aa; w=ww; h=hh;
 }

}
BufferedReader reader;
String line;
String v1,v2,v3;
String[] v1s,v2s,v3s;
String[] a_data;
float minx,maxx,miny,maxy,minz,maxz;
float p1,p2,p3,p4,p5,p6,p7,p8,p9;
PShape heart;
FloatList mesh;
imagedata [][] layerdata;

public void openfile() {
  // Open the file
  reader = createReader(stlpath);

  minx=10000;
  maxx=-10000;
  miny=minx;
  maxy=maxx;
  minz=minx;
  maxz=maxx;
}

public void updatebounds(float x, float y, float z){
  if (x>maxx) maxx=x;
  if (x<minx) minx=x;
  if (y>maxy) maxy=y;
  if (y<miny) miny=y;
  if (z>maxz) maxz=z;
  if (z<minz) minz=z;

}

public void  makebounds(){
  //box is centered, so add half width, half height, half depth
  translate((minx+(maxx-minx)/2),(miny+(maxy-miny)/2),(minz+(maxz-minz)/2));
  noFill();
  box(maxx-minx,maxy-miny,maxz-minz);
  println("minx,miny,minz="+minx+","+miny+","+minz+" maxx,maxy,maxz="+maxx+","+maxy+","+maxz);
  translate(-(minx+(maxx-minx)/2),-(miny+(maxy-miny)/2),-(minz+(maxz-minz)/2));
}

public void drawmesh(float sparse){
  for (int i=0;i<mesh.size();i+=9){
    if (random(0,1)<sparse){
    p1=mesh.get(i);
    p2=mesh.get(i+1);
    p3=mesh.get(i+2);
    p4=mesh.get(i+3);
    p5=mesh.get(i+4);
    p6=mesh.get(i+5);
    p7=mesh.get(i+6);
    p8=mesh.get(i+7);
    p9=mesh.get(i+8);
    maketriangle(p1,p2,p3,p4,p5,p6,p7,p8,p9);
    }
  }
}

public void parseFile(int filenumber) {
  // Open the file from the createWriter() example
 //BufferedReader reader = createReader("C:\\data\\figure2\\voltage\\f_"+framenumber+"_l_"+filenumber+".dat");
 BufferedReader reader = createReader(voltagedatapath+"f_"+framenumber+"_l_"+filenumber+".dat");

  String line = null;
  int c=0;
  try {
    while ((line = reader.readLine()) != null) {
     a_data=line.trim().split(" ");

     layerdata[filenumber][c]=new imagedata(PApplet.parseInt(a_data[0]), PApplet.parseInt(a_data[1]), PApplet.parseInt(a_data[2]), PApplet.parseInt(a_data[3]), PApplet.parseInt(a_data[4]));
     c++;
    }
    reader.close();
  } catch (IOException e) {
    e.printStackTrace();
  }
}

public void makemesh(float m, float n, float xo, float yo, float zo) {

  print("Reading file\n");
  boolean readok=true;
  int count=0;
  heart=createShape();
  //beginShape();
  while(readok){
  try {
    line = reader.readLine();
    count++;
  } catch (IOException e) {
    e.printStackTrace();
    line = null;
    readok=false;
  }
  if (line == null) {
    // Stop reading because of an error or file is empty
    print("null found "+count+"\n");
    readok=false;

  } else {

    String lt=line.trim();
    if (lt.startsWith("vertex")){
     v1=lt;
     try{
     v2=reader.readLine().trim();
     v3=reader.readLine().trim();
     }catch (IOException e){e.printStackTrace();}

     v1s=split(v1,' ');
     v2s=split(v2,' ');
     v3s=split(v3,' ');
     p1=PApplet.parseFloat(v1s[1])*(-m)+xo;
     p2=PApplet.parseFloat(v1s[2])*m+yo;
     p3=PApplet.parseFloat(v1s[3])*n+zo;
     updatebounds(p1,p2,p3);
     p4=PApplet.parseFloat(v2s[1])*(-m)+xo;
     p5=PApplet.parseFloat(v2s[2])*m+yo;
     p6=PApplet.parseFloat(v2s[3])*n+zo;
     updatebounds(p4,p5,p6);
     p7=PApplet.parseFloat(v2s[1])*(-m)+xo;
     p8=PApplet.parseFloat(v2s[2])*m+yo;
     p9=PApplet.parseFloat(v2s[3])*n+zo;
     updatebounds(p7,p8,p9);
     mesh.append(p1);
     mesh.append(p2);
     mesh.append(p3);
     mesh.append(p4);
     mesh.append(p5);
     mesh.append(p6);
     mesh.append(p7);
     mesh.append(p8);
     mesh.append(p9);

    }
   }
  }

}


public void maketriangle(float p1, float p2, float p3, float p4, float p5, float p6, float p7, float p8, float p9){
  beginShape();
     vertex(p1,p2,p3);
     vertex(p4,p5,p6);
     vertex(p7,p8,p9);
   endShape();
}
  public void settings() {  size(800, 800, P3D); }
  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "sketch_200103a" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
