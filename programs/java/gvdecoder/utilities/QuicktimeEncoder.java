package gvdecoder;



import quicktime.*;
import quicktime.io.*;
import quicktime.qd.*;
import quicktime.std.*;
import quicktime.std.movies.*;
import quicktime.std.movies.media.*;
import quicktime.std.image.*;
import quicktime.util.*;

import java.io.*;


public class QuickTimeWriter implements StdQTConstants {
	static final int KEY_FRAME_RATE = 30;
	static final int TIME_SCALE = 600;
	public String[] codecs = {"Cinepak", "Animation", "H.263", "Sorenson", "Sorenson 3", "MPEG-4"};
	public int[] codecTypes = {kCinepakCodecType, kAnimationCodecType, kH263CodecType, kSorensonCodecType, 0x53565133, 0x6d703476};
	public static String codec = "Sorenson";
	public String[] qualityStrings = {"Low", "Normal", "High", "Maximum"};
	public int[] qualityConstants = {codecLowQuality, codecNormalQuality, codecHighQuality, codecMaxQuality};
	public static String quality = "Normal";
    public int codecQuality=codecNormalQuality;
    public int codecType=0x53565133;
    public int fps=20;
    public int start;
    public int end;
    public StatusMonitor monitor;


    public String setQuality(int val){
		String res="Quality not set, choose between 0(low) and 3(max)";
		if ((val<0)||(val>3)) return res;
		codecQuality=qualityConstants[val];
		return "Qaulity = "+qualityStrings[val];
	}

	public String setCodec(int val){
		String res="Codec not set, choose 0(Cinepak),1(Animation),2(H.263),3(Sorensen),4(Sorensen 3),5(MPEG-4)";
		if ((val<0)||(val>5)) return res;
		codecType=codecTypes[val];
		return "Codec = "+codecs[val];
	}

public QuickTimeWriter(Viewer2 vw, String path, int start, int end, int codectype, int imgquality, StatusMonitor monitor){
	this.vw=vw;
	this.path=path;
	this.start=start;
	this.end=end;
	this.monitor=monitor;
	setQuality(imjquality);
	setCodec(codectype);

}


public void run() {

		int decimalPlaces = (int) fps == fps?0:1;


		if (fps<0.1) fps = 0.1;
		if (fps>100.0) fps = 100.0;
		int rate = (int)(TIME_SCALE/fps);

		long start = System.currentTimeMillis();
		try {
			QTSession.open();

			writeMovie(vw, path, codecType, codecQuality, rate);
		} catch (Exception e) {
			//IJ.showProgress(1.0);
			e.printStackTrace();
		} finally {
			QTSession.close();
		}

}

    public int[] pixels;
	public void writeMovie(gvdecoder.Viewer2 vw, String path, int start, int end, int codecType, int codecQuality, int rate, StatusMonitor monitor) throws QTException, IOException {
		int width = vw.jp.filtered.getWidth();
		int height =vw.jp.filtered.getHeight();
		int bands = vw.jp.filtered.getSampleModel().getNumBands();

		int frames = end-start;
		QTFile movFile = new QTFile (new java.io.File(path));
		Movie movie = Movie.createMovieFile(movFile, kMoviePlayer, createMovieFileDeleteCurFile|createMovieFileDontCreateResFile);
		int timeScale = TIME_SCALE; // 100 units per second
		Track videoTrack = movie.addTrack (width, height, 0);
		VideoMedia videoMedia = new VideoMedia(videoTrack, timeScale);
		videoMedia.beginEdits();
		ImageDescription imgDesc2 = new ImageDescription(QDConstants.k32ARGBPixelFormat);
		imgDesc2.setWidth(width);
		imgDesc2.setHeight(height);
		QDGraphics gw = new QDGraphics(imgDesc2, 0);
		QDRect bounds = new QDRect (0, 0, width, height);
		int rawImageSize = QTImage.getMaxCompressionSize(gw, bounds, gw.getPixMap().getPixelSize(),
			codecQuality, codecType, CodecComponent.anyCodec);
		QTHandle imageHandle = new QTHandle (rawImageSize, true);
		imageHandle.lock();
		RawEncodedImage compressedImage = RawEncodedImage.fromQTHandle(imageHandle);
		CSequence seq = new CSequence(gw, bounds, gw.getPixMap().getPixelSize(), codecType, CodecComponent.bestFidelityCodec,
			codecQuality, codecQuality, KEY_FRAME_RATE, null, 0);
		ImageDescription imgDesc = seq.getDescription();
		int[] pixels2 = null;
		for (int frame=start; frame<end; frame++) {
			monitor.showProgress((double)(frame-start)/(end-start));
			vw.SilentJumpToFrame(frame);
			if (pixels==null) pixels=new int[width*height];
			vw.jp.filtered.getRGB(0,0,width,height,pixels,0,1);
						//int[] pixels = (int[])ip.getPixels();
			RawEncodedImage pixelData = gw.getPixMap().getPixelData();
			int intsPerRow = pixelData.getRowBytes()/4;
			if (pixels2==null) pixels2 = new int[intsPerRow*height];
			if (EndianOrder.isNativeLittleEndian()) {
				//EndianOrder.flipBigEndianToNative(pixels, 0, EndianDescriptor.flipAll32);
				int offset1, offset2;
				for (int y=0; y<height; y++) {
					offset1 = y*width;
					offset2 = y* intsPerRow;
					for (int x=0; x<width; x++)
						pixels2[offset2++] = EndianOrder.flipBigEndianToNative32(pixels[offset1++]);
				}
			} else {
				for (int i=0; i<height; i++)
					System.arraycopy(pixels, i*width, pixels2, i*intsPerRow, width);
			}
			pixelData.copyFromArray(0, pixels2, 0, intsPerRow*height);
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

/*
			RandomIter iter = RandomIterFactory.create(vw.jp.filtered, null);

			if (pixels==null) pixels=new int[width*height];
			int index=0;
			for (int y=0; y<height; y++) {
				for (int x=0; x<width; x++) {
					int res=0;
					if (bands==1){
					  byte b=(byte)iter.getSample(x, y, b);
					  res=(255 << 24) | (b << 16) | (b << 8) | b;
					  }else{
				          byte r=(byte)iter.getSample(x,y,0);
				          byte g=(byte)iter.getSample(x,y,1);
				          byte b=(byte)iter.getSample(x,y,2);
				       res=(255 << 24) | (r << 16) | (g << 8) | b;
					}
				 pixels[index++]=res;
		    	 }
		    }
			*/
