package gvdecoder;

import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.util.*;
import javax.media.jai.iterator.*;


import org.jcodec.api.awt.AWTSequenceEncoder;
import org.jcodec.common.tools.MainUtils;
import org.jcodec.common.tools.MainUtils.Cmd;
import org.jcodec.common.tools.MainUtils.Flag;


public class mp4Encoder implements Runnable{
   // private static final Flag FLAG_FRAMES = new Flag("n-frames", "frames", "Total frames to encode");
   // private static final Flag[] FLAGS = new MainUtils.Flag[] {FLAG_FRAMES};

	Viewer2 vw;
	StatusMonitor monitor;
	boolean WRITEUNFILTERED;
	boolean SHOWEXTRAS;
	int start;
    int end;
    String filename;
    AWTSequenceEncoder enc;


    public mp4Encoder(Viewer2 vw, String filename, int start, int end, int fps, boolean showextras, StatusMonitor monitor){
		this.vw=vw;
		this.filename=filename;
		this.start=start;
		this.end=end;
		this.SHOWEXTRAS=showextras;
		this.monitor=monitor;
		System.out.println("in mp4Encoder constructor");

		try{
		enc = AWTSequenceEncoder.create25Fps(new File(filename));
        }catch (IOException e){e.printStackTrace();}
	}

	public void run(){
	 try{
		  System.out.println("in mp4encoder run");
		  for (int i=start;i<end;i++){
				   if (monitor.isCancelled()) break;
				   monitor.showProgress(((double)(i-start))/(end-start));
				   vw.SilentJumpToFrame(i,SHOWEXTRAS);
                   enc.encodeImage(convert(vw.jp.filtered,BufferedImage.TYPE_3BYTE_BGR));
                   System.out.println("mp4 frame "+i);
			   }
		  enc.finish();

		}catch(Exception e){e.printStackTrace();}
	}

	public static boolean odd(int v){
		return (((double)v)/2!=0);
	}


	public static BufferedImage convert(BufferedImage src, int bufImgType) {
        int nwidth=src.getWidth();
        int nheight=src.getHeight();
        if (odd(nwidth)) nwidth+=1;
        if (odd(nheight)) nheight+=1;
	    BufferedImage img= new BufferedImage(nwidth,nheight, bufImgType);
	    Graphics2D g2d= img.createGraphics();
	    g2d.drawImage(src, 0, 0, null);
	    g2d.dispose();
	    return img;
    }
}