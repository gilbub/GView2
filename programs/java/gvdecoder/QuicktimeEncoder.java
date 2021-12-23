package gvdecoder;



import quicktime.*;
import quicktime.io.*;
import quicktime.qd.*;
import quicktime.std.*;
import quicktime.std.movies.*;
import quicktime.std.movies.media.*;
import quicktime.std.image.*;
import quicktime.util.*;

import javax.media.jai.iterator.*;

import java.io.*;
/** QuickTimeEncoder is based on QuickTimeWriter for ImageJby Wayne Rasband, but
simplified to take advantage of BufferedImage.getRGB methods as well as the k32BGRAPixelFormat,
avoiding bitflipping by going directoy from bufferedImage to quicktime. Also finds
a list of codecs from the present quicktime install*/

public class QuickTimeEncoder implements StdQTConstants, Runnable {
	static final int KEY_FRAME_RATE = 30;
	static final int TIME_SCALE = 600;
	public String[] qualityStrings = {"Low", "Normal", "High", "Maximum"};
	public int[] qualityConstants = {codecLowQuality, codecNormalQuality, codecHighQuality, codecMaxQuality};
	public static String quality = "Normal";
    public int codecQuality=codecNormalQuality;
    public int codecType=0x53565133;
    public int fps=20;
    public int start;
    public int end;
    public StatusMonitor monitor;
    public String path;
    public Viewer2 vw;
    public String[] bigCodecList;
    public int[] bigCodecInts;
    public QTCodecs qtcodecs;
    private static QuickTimeEncoder ref;
    private boolean initialized=false;
    public boolean SHOWEXTRAS=false;

public void initialize(Viewer2 vw, String path, int start, int end, int fps, int codectype, int imgquality, boolean extras, StatusMonitor monitor){
	this.vw=vw;
	this.path=path;
	this.start=start;
	this.end=end;
	this.monitor=monitor;
	codecQuality=qualityConstants[imgquality];
	codecType=qtcodecs.codecs[codectype];
	this.SHOWEXTRAS=extras;
	initialized=true;
}

public static synchronized QuickTimeEncoder getQuickTimeEncoder(){
      if (ref == null)
          ref = new QuickTimeEncoder();
      return ref;
    }



private QuickTimeEncoder(){
	  //QTCodecs initializes QuickTime.
	  qtcodecs=new QTCodecs();

}


public void finished(){
	if (QTSession.isInitialized()) QTSession.close();
}


public void run() {
        if (!initialized) return;
		int decimalPlaces = (int) fps == fps?0:1;


		if (fps<1) fps = 1;
		if (fps>100) fps = 100;
		int rate = (int)(TIME_SCALE/fps);
        monitor.showProgress(0.25);  //to suggest somethings happening.
		try {

			if (!QTSession.isInitialized()) QTSession.open();

			writeMovie(vw, path,start,end, codecType, codecQuality, rate, SHOWEXTRAS, monitor);
			monitor.showProgress(1.0);

			} catch (Exception e) {
			monitor.showProgress(0.0);
			javax.swing.JOptionPane.showInternalMessageDialog(vw,"Unexpected Quicktime encoding error.\nCheck your chosen filename, and/or choose a different codec.");
			e.printStackTrace();

		} finally {
			initialized=false;
			finished();//QTSession.close();

		}

}

    public int[] pixels;
	public void writeMovie(gvdecoder.Viewer2 vw, String path, int start, int end, int codecType, int codecQuality, int rate, boolean extras, StatusMonitor monitor) throws QTException, IOException {
		int width = vw.jp.filtered.getWidth();
		int height =vw.jp.filtered.getHeight();
		int frames = end-start;
		QTFile movFile = new QTFile (new java.io.File(path));
		Movie movie = Movie.createMovieFile(movFile, kMoviePlayer, createMovieFileDeleteCurFile|createMovieFileDontCreateResFile);
		int timeScale = TIME_SCALE; // 100 units per second
		Track videoTrack = movie.addTrack (width, height, 0);
		VideoMedia videoMedia = new VideoMedia(videoTrack, timeScale);
		videoMedia.beginEdits();
		ImageDescription imgDesc2 = new ImageDescription(QDConstants.k32BGRAPixelFormat);
		imgDesc2.setWidth(width);
		imgDesc2.setHeight(height);
		QDGraphics gw = new QDGraphics(imgDesc2, 0);
		QDRect bounds = new QDRect (0, 0, width, height);
		int rawImageSize = QTImage.getMaxCompressionSize(gw, bounds, gw.getPixMap().getPixelSize(),
			codecQuality, codecType, CodecComponent.anyCodec);
		QTHandle imageHandle = new QTHandle (rawImageSize, true);
		imageHandle.lock();
		RawEncodedImage compressedImage = RawEncodedImage.fromQTHandle(imageHandle);
		CSequence seq = new CSequence(gw, bounds, gw.getPixMap().getPixelSize(), codecType, CodecComponent.bestFidelityCodec,codecQuality, codecQuality, KEY_FRAME_RATE, null, 0);
		ImageDescription imgDesc = seq.getDescription();

		if (pixels==null) pixels=new int[width*height];

		for (int frame=start; frame<end; frame++) {
			monitor.showProgress((double)(frame-start)/(end-start));
			if (monitor.isCancelled()) {
							  finished();
							  monitor.showProgress(0.0);
							  return;
			                  }
			vw.SilentJumpToFrame(frame,extras);
			vw.jp.filtered.getRGB(0,0,width,height,pixels,0,width);
			RawEncodedImage pixelData = gw.getPixMap().getPixelData();
			pixelData.copyFromArray(0, pixels, 0, width*height); //was intsPerRow instead of width
			CompressedFrameInfo cfInfo = seq.compressFrame (gw, bounds, codecFlagUpdatePrevious, compressedImage);
			boolean syncSample = cfInfo.getSimilarity()==0; // see developer.apple.com/qa/qtmcc/qtmcc20.html
			videoMedia.addSample (imageHandle, 0, cfInfo.getDataSize(), rate, imgDesc, 1, syncSample?0:mediaSampleNotSync);
		}
		videoMedia.endEdits();
		videoTrack.insertMedia (0, 0, videoMedia.getDuration(), 1);
		OpenMovieFile omf = OpenMovieFile.asWrite (movFile);
		movie.addResource (omf, movieInDataForkResID, movFile.getName());
	}



}

