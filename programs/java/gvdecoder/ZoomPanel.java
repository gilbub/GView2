package gvdecoder;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.Robot;
import javax.swing.Timer;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ZoomPanel extends JPanel implements ActionListener{
  BufferedImage[] image;
  Dimension size=new Dimension();
   java.awt.Robot rob;
   java.awt.Rectangle rect=new java.awt.Rectangle();
   javax.swing.Timer timer;
   BufferedImage scaled;
   int step=10;
   int span=20;

public void actionPerformed(ActionEvent e) {
	updateImage();
    //repaint();
  }

public void setuprefresh(){

	timer = new Timer(50, this);
	timer.setInitialDelay(190);
    timer.start();
}

public void stoprefresh(){
	timer.stop();
}



    public ZoomPanel(int x, int y, int w, int h,int step,int span){
     this.step=step;
     this.span=span;
     size.setSize(w*step,h);
     rect.setBounds(x,y,w,h);
     try{
	 	   rob=new java.awt.Robot();
	 	   image=new BufferedImage[step];
	 	   for (int i=0;i<step;i++){
              image[i]=rob.createScreenCapture(rect);

		  }
    }catch(java.awt.AWTException e){e.printStackTrace();}
   scaled=new BufferedImage(rect.width*step, rect.height, BufferedImage.TYPE_INT_RGB);

    }

    public void paintComponent(Graphics g){
	Graphics2D g2=scaled.createGraphics();
	g2.setColor(java.awt.Color.yellow);
	for (int i=0;i<step;i++){
      g2.drawImage(image[i],i*rect.width,0,rect.width*10,rect.height*10,null);
      g2.drawLine((int)(i*rect.width+rect.width/2.0),0,  (int)(i*rect.width+rect.width/2.0), rect.height);
    }
    g2.drawLine(0,(int)(rect.height/2.0),rect.width*step,(int)(rect.height/2.0));
    g2.dispose();
    g.drawImage(scaled,0,0,this);
    }

   public void updateImage(){
	   if (rob!=null){
	  java.awt.Rectangle r2=new java.awt.Rectangle();
      for (int i=0;i<step;i++){
	     r2.setBounds(rect.x+i*span, rect.y, rect.width, rect.height);
		 image[i]=rob.createScreenCapture(r2);
		}
	 repaint();
   }
 }





    public Dimension getPreferredSize(){return size;}

    public void showframe(){
     JFrame f=new JFrame();
     f.add(this);
     f.setSize(size);
     f.setVisible(true);
     }



    }

