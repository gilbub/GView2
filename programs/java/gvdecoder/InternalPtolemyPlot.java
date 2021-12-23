package gvdecoder;
/* A simple plot application with two plots

 Copyright (c) 1998-2001 The Regents of the University of California.
 All rights reserved.
 Permission is hereby granted, without written agreement and without
 license or royalty fees, to use, copy, modify, and distribute this
 software and its documentation for any purpose, provided that the above
 copyright notice and the following two paragraphs appear in all copies
 of this software.

 IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY
 FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
 ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
 THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
 SUCH DAMAGE.

 THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
 PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
 CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
 ENHANCEMENTS, OR MODIFICATIONS.

                                        PT_COPYRIGHT_VERSION_2
                                        COPYRIGHTENDKEY
@ProposedRating red (eal@eecs.berkeley.edu)
@AcceptedRating red (cxh@eecs.berkeley.edu)
*/

//package ptolemy.plot.demo;

// This class is not in the ptolemy.plot package so that it is a
// more realistic example.
import ptolemy.plot.*;

import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import javax.swing.JFrame;
import java.awt.image.*;
import java.awt.image.renderable.*;
import java.awt.*;
import javax.swing.*;

// The java.io imports are only necessary for the right hand plot.
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class InternalPtolemyPlot extends JInternalFrame {
Plot plot;


    /** We use a constructor here so that we can call methods
     *  directly on the Frame.  The main method is static
     *  so getting at the Frame is a little trickier.
     */
   public InternalPtolemyPlot(double[][] pts, int rows, int columns, String[] keys) {

	   super("data",
	                 true, //resizable
	                 true, //closable
	                 true, //maximizable
	                 true);//iconifiable

        // Instantiate the two plots.
        plot = new Plot();

        // Set the size of the toplevel window.
        setSize(320, 320);

        // Create the left plot by calling methods.
     	plot.setSize(319,319);
        plot.setButtons(true);
        plot.setTitle("user data");

        plot.setXLabel("x");
        plot.setYLabel("y");
        plot.setMarksStyle("various");
        for (int k=0;k<keys.length;k++){
			plot.addLegend(k+1,keys[k]);
		}

      int i=0;
      int j=0;
      try{
       for ( i=0; i<rows; i++){
		   for ( j=1;j<columns; j++){
			   plot.addPoint(j,pts[i][0],pts[i][j], false);
			   System.out.println("adding to set="+j+" x="+pts[i][0]+" y="+pts[i][j]);
		   }
	   }}
      catch(Exception e){System.out.println("array error i="+i+" j="+j);}




        // Layout the two plots
        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        getContentPane().setLayout(gridbag);

        // Handle the leftPlot
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 1;
        gridbag.setConstraints(plot, c);
        getContentPane().add(plot);



        show();
    }

public void saveAsImage(){
	//BufferedImage image = new BufferedImage(310,310,BufferedImage.TYPE_BYTE_INDEXED);
	//Graphics2D myG = (Graphics2D)image.getGraphics();
    //plot.paintComponent(myG);
	//ImageUtils.WriteImage("temp.bmp",image,"bmp");
}


}
