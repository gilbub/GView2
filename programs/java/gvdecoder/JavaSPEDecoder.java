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



import gvdecoder.log.*;
import gvdecoder.utilities.*;


class JavaSPEDecoder implements ImageDecoder{
    public int UpdateImageArray(int[] arr, int xdim, int ydim, int instance){System.out.println("obtained request"); return 1; }
    public int OpenImageFile(String filename){
		  return 1;
		}


	public int CloseImageFile(int instance){System.out.println("close file request"); return 1;}
 	public int FilterOperation(int Opp, int startx, int endx, int instance){ return Opp;}
	public String ReturnSupportedFilters(){return "none supported\n";}
	public int ReturnFrameNumber(){return 1;}
	public int JumpToFrame(int framenum, int instance){return 1;}
	public int SumROIs(int[][] rois, String outfile, int startframe, int endframe,int instance){return 1;}
    public int ReturnXYBandsFrames(int[] dat, int i){return -1;}
}

