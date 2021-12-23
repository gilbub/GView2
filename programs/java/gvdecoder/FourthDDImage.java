package gvdecoder;

import java.awt.image.*;
import java.awt.*;
import java.util.*;

public class FourthDDImage {

public BufferedImage bi1;
public BufferedImage bi2;
int width;
int height;
public GView gv;

public Dimension dimension;




public FourthDDImage(GView gv, int width, int height){

  this.width=width;
  this.height=height;
  this.gv=gv;
  dimension=new Dimension(width,height);
  this.bi1=setupBufferedImage1();
  this.bi2=setupBufferedImage2();

}

public Vector timings=new Vector();

public void setupPageFlipping(int delay){
  gv.setIgnoreRepaint(true);
  gv.jythonwindow.setIgnoreRepaint(true);
  gv.jythonwindow.hide();
  gv.createBufferStrategy(2);
  BufferStrategy bufferStrategy = gv.getBufferStrategy();
  Graphics g=bufferStrategy.getDrawGraphics();

  long starttime=System.currentTimeMillis();
  int framenumber=0;
  boolean flip=true;
  for (int ii=0; ii<1000; ii++) {
                for (int i = 0; i < 2; i++) {
					long t=System.currentTimeMillis();
                    //Graphics g = bufferStrategy.getDrawGraphics();
                    if (!bufferStrategy.contentsLost()) {

                        if (ii<5)
                        {

                       if (framenumber%2==0)
                        g.drawImage(bi1,140,12,gv);
                        else
                        g.drawImage(bi2,140,12,gv);

					    }
                        if (flip) bufferStrategy.show();
                        else{
					   try {
                          Thread.sleep(16);
                         } catch (InterruptedException e) {}

						}
                      //  g.dispose();
                      long nt=System.currentTimeMillis();
                      long skip=nt-t;
                      if (skip<1){
						  framenumber+=0;
						  flip=true;
					  }else
                      if (skip>30){
						 framenumber+=2;
						 flip=false;
					  }else{
					  framenumber++;
                      flip=true;
				     }

                      timings.add(new times( (int)(nt-starttime),(int)skip,framenumber));
                    }
                    //try {
                    //    Thread.sleep(delay);
                    //} catch (InterruptedException e) {}
                }
}
 if (g!=null)  g.dispose();
 gv.jythonwindow.show();
 gv.jythonwindow.setIgnoreRepaint(false);
 gv.setIgnoreRepaint(false);
}

public BufferedImage setupBufferedImage1(){
 BufferedImage bi1=new BufferedImage(1000,1000,BufferedImage.TYPE_INT_RGB);
 Graphics g=bi1.getGraphics();
 g.setColor(Color.BLACK);
 g.fillRect(0,0,1000,1000);
 g.setColor(Color.WHITE);
 for (int i=0;i<1000;i+=1){
  for (int j=0;j<1000;j+=1){
	if (i%2==0){
		if (j%2==0){
			g.fillRect(i*1,j*1,1,1);
		}
	}
	if (i%2==1){
		if (j%2==1){
			g.fillRect(i*1,j*1,1,1);
		}
	}
	}
  }


 //g.setColor(Color.RED);
 //g.drawLine(0,0,1000,1000);
 g.dispose();
 return bi1;
}

public BufferedImage setupBufferedImage2(){

 BufferedImage bi2=new BufferedImage(1000,1000,BufferedImage.TYPE_INT_RGB);
 Graphics g=bi2.getGraphics();
 g.setColor(Color.BLACK);
 g.fillRect(0,0,1000,1000);
// g.setColor(Color.BLUE);
// g.drawLine(0,1000,1000,0);
for (int i=0;i<10;i+=1){
  for (int j=0;j<10;j+=1){
	if (i%2==0){
		if (j%2==0){
			g.fillRect(i*100,j*100,100,100);
		}
	}
	if (i%2==1){
		if (j%2==1){
			g.fillRect(i*100,j*100,100,100);
		}
	}
	}
  }

 g.dispose();
 return bi2;
}

}


class times{
  int time;
  int delay;
  int frame;
  public times(int time, int delay, int frame){
	  this.time=time;
	  this.delay=delay;
	  this.frame=frame;
}
}
