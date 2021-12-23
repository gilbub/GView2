 package gvdecoder;
 import java.awt.*;
 import java.awt.image.BufferedImage;
 import java.io.*;
 import com.sun.image.codec.jpeg.*;
 import javax.swing.*;

 public class SaveComponentAsJPG{
 public void go(ImageIcon ii, String filename) {
       Dimension size = ii.getSize();
       BufferedImage myImage =
         new BufferedImage(size.width, size.height,
         BufferedImage.TYPE_INT_RGB);
       Graphics2D g2 = myImage.createGraphics();
       ii.paint(g2);
       try {
         OutputStream out = new FileOutputStream(filename);
         JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(out);
         encoder.encode(ii);
         out.close();
       } catch (Exception e) {
         System.out.println(e);
       }
     }
     }