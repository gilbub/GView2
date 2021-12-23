package gvdecoder;
 /*
 * Swing version.
 */

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import gvdecoder.trace.*;
import java.util.*;
import java.awt.geom.GeneralPath;
//import java.awt.geom.GeneralPath;
import java.awt.Graphics2D.*;
import com.sun.image.codec.jpeg.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;

/*
 Paints a graph from a file.
 For now, assume the data is a series of ints, not scaled.
 The code should determine the amount of room it has, and
 generate a second x and y array that contains a drawable line.
 It uses drawPolyLine(arrayx, arrayy, length)
 */


public class ContourDrawer extends JPanel {
public GnuplotContourReader ctreader;
public int maxx=320;
public int maxy=320;
public Color[] cols;
public BufferedImage bi;
public float scale=1.0f;
public Image image;

public ContourDrawer(String imagefilename) {
   // ctreader=new GnuplotContourReader(contoursfilename);
   // this.scale=scale;
    Toolkit toolkit=Toolkit.getDefaultToolkit();
    image=toolkit.getImage(imagefilename);
    MediaTracker mediaTracker=new MediaTracker(this);
    mediaTracker.addImage(image,0);
    try{
	    mediaTracker.waitForID(0);
	}catch(InterruptedException e){;}
    maxx=image.getWidth(this);
    maxy=image.getHeight(this);
 }


public Dimension getPreferredSize() {
        return new Dimension(maxx,maxy);
    }


public void createImage(){
	bi = new BufferedImage(maxx, maxy, BufferedImage.TYPE_INT_RGB);
	Graphics2D g2 = bi.createGraphics();
    g2.drawImage(image,0,0,null);
/*
    g2.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );
       // g2.setColor(Color.white);
       // g2.fillRect(0,0,maxx,maxy);
        float[] levels=ctreader.getLevels();
//        if (cols==null){
//			 cols=new Color[levels.length];
//			 int divs=(int)(200.0/levels.length);
//			 for (int c=0;c<levels.length;c++){
//				 cols[c]=new Color(100,c*divs, 50+c*divs);
//			 }
//		}
        for (int l=levels.length-1;l>=0;l--){
	    Vector v=ctreader.getContours(levels[l],scale);
	    for (int i=0;i<v.size();i++){
//		    g2.setColor(cols[l]);
//		    g2.fill((GeneralPath)v.elementAt(i));
		    g2.setColor(Color.black);
			g2.draw((GeneralPath)v.elementAt(i));
		 }

		}
*/
	g2.dispose();

}

public void saveJPG(String filename){
	  try {
		        if (bi==null) createImage();
	            // To write the jpeg to a file uncomment the File* lines and
	            // comment out the ByteArray*Stream lines.
	            File file = new File("images", filename);
	            FileOutputStream out = new FileOutputStream(file);
	            //ByteArrayOutputStream out = new ByteArrayOutputStream();
	            JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(out);
	            System.out.println("bi equals null "+(bi==null));
	            JPEGEncodeParam param = encoder.getDefaultJPEGEncodeParam(bi);
	            param.setQuality(1.0f, false);
	            encoder.setJPEGEncodeParam(param);
                encoder.encode(bi);

		}catch(Exception e){e.printStackTrace();}
	}

public void paintComponent(Graphics g) {
        super.paintComponent(g);  //paint background

        if (bi==null)
          createImage();
        Graphics2D g2=(Graphics2D)g;
	    g2.drawImage(bi, 0, 0, this);


	}


public static void main(String[] args){
	ContourDrawer cd=new ContourDrawer(args[0]);
	JFrame frame=new JFrame();
	frame.getContentPane().add("Center", cd);
	frame.pack();
    frame.setVisible(true);
      cd.saveJPG("newwest.jpg");

}

}


