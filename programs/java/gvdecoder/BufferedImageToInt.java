package gvdecoder;
import java.io.*;
import java.util.*;


import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.awt.geom.*;

import java.awt.image.*;
import java.awt.image.renderable.*;
import javax.media.jai.*;
import javax.media.jai.iterator.*;
import javax.media.jai.widget.*;
import java.awt.color.*;


public class BufferedImageToInt{
public static int[] go(BufferedImage bi){

int height = bi.getHeight();
int width = bi.getWidth();
int[] arr=new int[width*height];
BufferedImage dst = null;
dst=bi;

RandomIter iter = RandomIterFactory.create(dst, null);
int bands = dst.getSampleModel().getNumBands();
for (int line=0; line < height; line++) {
   for (int samp=0; samp < width; samp++) {
     int val=0;
      for (int band=0; band < bands; band++) {
	  val += iter.getSample(samp, line, band);
	}
     arr[line*width+samp]=val;
  }
}
return arr;
}
}