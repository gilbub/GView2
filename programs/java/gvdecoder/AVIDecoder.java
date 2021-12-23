//import ij.*;
//import ij.plugin.*;
//import ij.process.*;
//import ij.gui.*;
//import ij.io.*;
//import ij.plugin.Animator;
package gvdecoder;

import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.util.*;

public class AVIDecoder extends ImageDecoderAdapter{

	public  long              startTime;
	public  RandomAccessFile  raFile;
	public  int               bytesPerPixel;

	public  File              file;
	public  int               bufferFactor;
	public  int               xDim, yDim, zDim, tDim;
	public  int               lutBufferRemapped[] = null;
	public  int               microSecPerFrame;
	public  int               xPad;
	public  byte[]          bufferWrite;
	public  int               bufferSize;
	public  int               indexA, indexB;
	public  float            opacityPrime;
	public  int               bufferAdr;
	public  byte[]          lutWrite = null;
	public  int[]             dcLength = null;

	public  String           type = "error";
	public  String           fcc = "error";
	public  int               size = -1;

	public  boolean        verbose = true;
	public  boolean        showTimes = false;

	public  int               fileLength;
	public  int               i;

	//From AVI Header Chunk

	public  int               dwMicroSecPerFrame;
	public  int               dwMaxBytesPerSec;
	public  int               dwReserved1;
	public  int               dwFlags;
	public  int               dwTotalFrames;
	public  int               dwInitialFrames;
	public  int               dwStreams;
	public  int               dwSuggestedBufferSize;
	public  int               dwWidth;
	public  int               dwHeight;
	public  int               dwScale;
	public  int               dwRate;
	public  int               dwStart;
	public  int               dwLength;

	//From Stream Header Chunk

	public  String            fccStreamType;
	public  String            fccStreamHandler;
	public  int               dwStreamFlags;
	public  int               dwStreamReserved1;
	public  int               dwStreamInitialFrames;
	public  int               dwStreamScale;
	public  int               dwStreamRate;
	public  int               dwStreamStart;
	public  int               dwStreamLength;
	public  int               dwStreamSuggestedBufferSize;
	public  int               dwStreamQuality;
	public  int               dwStreamSampleSize;

	//From Stream Format Chunk
	//BMP header reader from BMP class

	// Actual contents (40 bytes):
	public  int               BMPsize;// size of this header in bytes
	public  short            BMPplanes;// no. of color planes: always 1
	public  int               BMPsizeOfBitmap;// size of bitmap in bytes (may be 0: if so, calculate)
	public  int               BMPhorzResolution;// horizontal resolution, pixels/meter (may be 0)
	public  int               BMPvertResolution;// vertical resolution, pixels/meter (may be 0)
	public  int               BMPcolorsUsed;// no. of colors in palette (if 0, calculate)
	public  int               BMPcolorsImportant;// no. of important colors (appear first in palette) (0 means all are important)
	public  boolean       BMPtopDown;
	public  int               BMPnoOfPixels;

	public  int               BMPwidth;
	public  int               BMPheight;
	public  short           BMPbitsPerPixel;
	public  int               BMPcompression;

	public  int               BMPactualSizeOfBitmap;
	public  int               BMPscanLineSize;
	public  int               BMPactualColorsUsed;

	public  int[]             intData;
	public  byte[]          byteData;
	public  byte[]          rawData;

	//palette
	public  byte[]            r;
	public  byte[]            g;
	public  byte[]            b;

	public  byte[]            videoData;

	public  ColorModel        cm;
	//public  ImageProcessor    ip;

	//public  ImageStack        stack = new ImageStack(0, 0);

	public  long              pos;

	public  long              bigChunkSize = 0;

	public  String            fileName;

	public  long              lastTime = 0;
	public  int                 lastLine = 0;
	public int FrameNumber=0;


	public void run(String arg) {
		try {
			//IJ.showProgress(.01);
			//readAVI(arg);
		} catch (OutOfMemoryError e) {
			//IJ.outOfMemory(fileName);
			//stack.trim();
			//IJ.showMessage("AVI Reader", "Out of memory.  " + stack.getSize() + " of " + dwTotalFrames + " frames will be opened.");
			//if (stack != null && stack.getSize() > 0) {
			//	new ImagePlus(fileName, stack).show();
			//}
			//break; // break out of the frame reading loop
		} catch (Exception e) {
			String  msg  = e.getMessage();
			if (msg == null || msg.equals("")) {
				msg = "" + e;
			}

			System.out.println("AVI Reader An error occurred reading the file.\n \n");
		} finally {
			try {
				showTime("Closing File");
				raFile.close();
				if (verbose) {
					System.out.println("File closed.");
				}
			} catch (Exception e) {

			}
			//IJ.showProgress(1);
			//IJ.showProgress(0);
			long  totalTime  = (System.currentTimeMillis() - startTime);
			if (totalTime==0)
				totalTime = 1;
			//System.out.println("Done in " + totalTime + " msec.  " + (stack.getWidth() * stack.getHeight() * stack.getSize() / totalTime * 1000) + " pixels/second");
		}
	}


 public long startposition=0;
 public int OpenImageFile(String filename){
 int j=0;
 //open the file...
 try{
   raFile=new RandomAccessFile(filename,"r");
  readAVI(); //this puts the movie in a state that it can be read from
  readMovieData();
  startposition= raFile.getFilePointer();
  }catch (Exception e){e.printStackTrace();}


  return 1;

}

public int ReturnXYBandsFrames(int[] arr, int instance){
	//String fileName=imagepath+File.separator+(String)images.get(FrameNumber);

    arr[0]=BMPwidth;
    arr[1]=BMPheight;
    arr[2]=(int)(BMPbitsPerPixel/8);
    if (arr[2]==0) arr[2]=1;
    arr[3]=dwTotalFrames;
	return 1;
}

public int CloseImageFile(int instance){
	try{
	raFile.close();
    }catch(IOException e){e.printStackTrace();}
	return 1;
}

public int JumpToFrame(int framenum, int instance){
	  FrameNumber=framenum;

	   try{
	      raFile.seek((BMPsizeOfBitmap*framenum)+startposition+8*framenum);
	      System.out.println("Jumping to frame="+FrameNumber+" pos="+(int)((BMPsizeOfBitmap*framenum)+startposition+8*framenum));
          System.out.println("frame "+FrameNumber+" position="+raFile.getFilePointer());
        }catch(IOException e){System.out.println("error jumping in AVIDecoder");}
	   return FrameNumber;

}


public int UpdateImageArray(int[] arr, int xdim, int ydim, int instance){

   try{
    for (int j=0;j<arr.length;j++)arr[j]=0;
         {
						updateProgress();
						pos = raFile.getFilePointer();
						System.out.println("frame "+FrameNumber+" position="+pos);
						if (1==1) {
							if (verbose) {
								System.out.println("   Video data chunk (" + type + ") detected...");
								System.out.println("      size=" + size);
							}
							readFrame();
							if (BMPbitsPerPixel <= 8){
								for (int i=0;i<byteData.length;i++) {int val=(int)byteData[i];
								                                     if (val<0) val+=256;
								                                     arr[i]=val;}
								//ip.setPixels(byteData);
							}else{
								for (int i=0;i<byteData.length;i++) arr[i]=intData[i];
								//ip.setPixels(intData);
						      }
							if (BMPactualColorsUsed != 0) {
								//ip.setColorModel(cm);
								if (verbose)
									System.out.println("      Color model set");
							}
							//stack.addSlice("", ip);

						} else {
							if (verbose) {
								System.out.println("   Unknown data chunk (" + type + ") detected.  Skipping...");
								System.out.println("      size=" + size);
							}
						}

						//raFile.seek(pos + size);// +1);  // <= quicktime seems to need this for rgb files
						//this is hackish, but I don't see a better way at the moment
						//if (raFile.readByte() == 48) {
						//	if (verbose)
						//		System.out.println("     Skipping last byte");
						//	raFile.seek(raFile.getFilePointer() - 1);
						//}
						readTypeAndSize();
						if (type.equals("JUNK")) {
	 						raFile.seek(raFile.getFilePointer() + size);
							readTypeAndSize();
						}
					}
				}catch (Exception e){e.printStackTrace();}
     return 1;
   }


   public int SumROIs(int[][] rois, String outfile, int startframe, int endframe,int instance){




        System.out.println("in adapter SumROIs start="+startframe+" end="+endframe);
        //determine x,y dimensions
        double averagepixel=0.0;
        int[] dims=new int[4];
        ReturnXYBandsFrames(dims,1);
        int framewidth=dims[0];
        int frameheight=dims[1];
        int numframes=dims[3];
        System.out.println("debug width ="+framewidth+" height ="+frameheight+" num frames ="+numframes);
        //bounds check
        if (endframe<0) endframe=numframes;
   	 if (endframe>numframes) endframe=numframes;
   	 if (startframe<0) startframe=0;
        if (startframe>numframes) startframe=0;

        //create an array to hold the sums from the rois.
        double[][] sum=new double[endframe-startframe][rois.length];

        //create an array to read the data into.
        int[] tmparray=new int[framewidth*frameheight];

        for (int k=startframe;k<endframe;k++){//for each frame
        JumpToFrame(k,0); //goto frame
        UpdateImageArray(tmparray,framewidth,frameheight,0); //load image
         //sum whole frame
         averagepixel=0.0;
         int numpixels=0;
        for (int p=0;p<framewidth*frameheight;p++){
   		 if (tmparray[p]>0) {
   		  averagepixel+=tmparray[p];
   		  numpixels++;
   	     }
   	  }
   	  averagepixel=averagepixel/((double)(numpixels));
         System.out.println("average pixel="+averagepixel);
        for (int i=0;i<rois.length;i++){ //for each roi

          for (int j=0;j<rois[i].length;j++){ //go to each element in the roi
            sum[k-startframe][i]+=(((double)tmparray[rois[i][j]])/((double)rois[i].length));

          }//j
          //System.out.println("sim for frame "+k+" = "+sum[k-startframe][i]);
         }//i
        }//k

       //generate output
       try{
       PrintWriter file=new PrintWriter(new FileWriter(outfile),true);
       System.out.println("printing roi file end "+endframe+" start "+startframe);
       file.println("# rois are average value per pixel minus average value for whole frame");
       for (int j=startframe;j<endframe;j++){
   	 // System.out.println("debug frame="+j);
   	 file.print((j)+" ");
   	// System.out.print("debug "+(j)+" ");
   	 for (int i=0;i<rois.length;i++) {
   		 //System.out.println("debug rois "+i+" out of "+rois.length);
   		 file.print((int)(sum[(j-startframe)][i]-averagepixel)+" "); //get three decimal places
   		 //System.out.print("debug "+sum[(j-startframe)][i]+" ");
   		 }
   	 file.print("\n");
   	 //System.out.print("\n");
   	 //System.out.println("debug frame="+j);
   	 }
       file.close();
       System.out.println("debug closed file");
       }catch(IOException e){System.out.println("error opening file for rois...");}
        catch(Exception e){System.out.println("Some other error in sumROis");e.printStackTrace();}
       return 1;
   }

	public void readAVI() throws Exception, IOException {
		if (verbose)
			System.out.println("/--------------------------/");

		showTime("Reading File Header");
		readFileHeader();

		byte[]      list        = new byte[4];
		String      listString;

		//IJ.write("bigChunkSize="+bigChunkSize);

		while (raFile.read(list) == 4) {
			raFile.seek(raFile.getFilePointer() - 4);

			listString = new String(list);

			updateProgress();

			if (listString.equals("JUNK")) {
				showTime("Skipping JUNK");
				skipJUNK();
			} else if (listString.equals("LIST")) {
				readTypeAndSizeAndFcc();
				raFile.seek(raFile.getFilePointer() - 12);

				if (fcc.equals("hdrl")) {

					readAviHeader();

				} else if (fcc.equals("strl")) {

					showTime("Reading Video Stream");

					long  startPos    = raFile.getFilePointer();
					long  streamSize  = size;

					readVideoStream();

					raFile.seek(startPos + 8 + streamSize);

				} else if (fcc.equals("movi")) {

					showTime("Reading Movie Data");
				//	readMovieData();

				//	showTime("Creating ImagePlus to display stack");
					//if (stack != null && stack.getSize() > 0) {
					//	new ImagePlus(fileName, stack).show();
					//}

				//	raFile.close();
					return;
				} else {

					showTime("Skipping unknown block");
					if (verbose) {
						System.out.println("**Unsupported LIST fcc '" + fcc + "' detected at " + raFile.getFilePointer());
					}
					if (verbose) {
						System.out.println("   size=" + size);
						System.out.println("   Skipping...");
					}
					raFile.seek(raFile.getFilePointer() + 8 + size);
					if (verbose) {
						System.out.println("   Now at " + raFile.getFilePointer());
					}
				}

			} else {

				showTime("Skipping unknown block");

				if (verbose) {
					System.out.println("**Unsupported type '" + listString + "' detected at " + raFile.getFilePointer());
				}
				readTypeAndSize();
				if (verbose) {
					System.out.println("   size=" + size);
					System.out.println("   Skipping...");
				}
				raFile.seek(raFile.getFilePointer() + size);
			}

			if (verbose) {
				System.out.println("");
			}
		}

		raFile.close();
		return;
		/*
		 *  readAviHeader();
		 *  readVideoStream();
		 *  skipJUNK();
		 *  checkList("movi");
		 *  readMovieData();
		 */

	}



	void readFileHeader() throws Exception, IOException {
		readTypeAndSizeAndFcc();

		if (type.equals("RIFF")) {
			if (verbose) {
				System.out.println("RIFF format detected...");
				System.out.println("   size=" + size);
				System.out.println("   fcc=" + fcc);
			}
			bigChunkSize = size;
			if (!fcc.equals("AVI ")) {
				if (verbose) {
					System.out.println("Unsupported format '" + fcc + "'");
					System.out.println("Expected format 'AVI '");
				}
				throw new Exception("Unsupported file type.  AVI RIFF form required.");
			}
		} else {
			if (verbose) {
				System.out.println("Unexpected '" + type + "'");
				System.out.println("Expected 'RIFF' not found");
			}
			throw new Exception("The file does not appear to be in AVI format.");
		}

		if (verbose) {
			System.out.println("");
		}
	}


	void readAviHeader() throws IOException {
		readTypeAndSizeAndFcc();

		if (type.equals("LIST")) {
			if (verbose) {
				System.out.println("AVI header detected...");
				System.out.println("   size=" + size);
				System.out.println("   fcc=" + fcc);
			}
			if (fcc.equals("hdrl")) {
				readTypeAndSize();
				if (type.equals("avih")) {
					if (verbose) {
						System.out.println("   AVI header chunk (avih) detected...");
					}

					pos = raFile.getFilePointer();

					dwMicroSecPerFrame = readInt();
					dwMaxBytesPerSec = readInt();
					dwReserved1 = readInt();
					dwFlags = readInt();
					dwTotalFrames = readInt();
					dwInitialFrames = readInt();
					dwStreams = readInt();
					dwSuggestedBufferSize = readInt();
					dwWidth = readInt();
					dwHeight = readInt();
					dwScale = readInt();
					dwRate = readInt();
					dwStart = readInt();
					dwLength = readInt();

					if (verbose) {
						System.out.println("      dwMicroSecPerFrame=" + dwMicroSecPerFrame);
						System.out.println("      dwMaxBytesPerSec=" + dwMaxBytesPerSec);
						System.out.println("      dwReserved1=" + dwReserved1);
						System.out.println("      dwFlags=" + dwFlags);
						System.out.println("      dwTotalFrames=" + dwTotalFrames);
						System.out.println("      dwInitialFrames=" + dwInitialFrames);
						System.out.println("      dwStreams=" + dwStreams);
						System.out.println("      dwSuggestedBufferSize=" + dwSuggestedBufferSize);
						System.out.println("      dwWidth=" + dwWidth);
						System.out.println("      dwHeight=" + dwHeight);
						System.out.println("      dwScale=" + dwScale);
						System.out.println("      dwRate=" + dwRate);
						System.out.println("      dwStart=" + dwStart);
						System.out.println("      dwLength=" + dwLength);
					}

					raFile.seek(pos + size);

				}
			} else {
				System.out.println("**Unsupported fcc '" + fcc + "'");
				System.out.println("**Expected fcc 'hdrl'");
				return;
			}

		} else {
			System.out.println("**Unexpected '" + type + "'");
			System.out.println("**Expected 'LIST' not found");
			return;
		}

		if (verbose) {
			System.out.println("");
		}
	}



	void readVideoStream() throws Exception, IOException {
		readTypeAndSizeAndFcc();

		if (type.equals("LIST")) {
			if (fcc.equals("strl")) {
				if (verbose) {
					System.out.println("Stream header (strl) detected...");
					System.out.println("   size=" + size);
					System.out.println("   fcc=" + fcc);
				}

				readTypeAndSize();
				if (type.equals("strh")) {
					if (verbose) {
						System.out.println("   Stream header chunk (strh) detected...");
					}

					pos = raFile.getFilePointer();

					String  fccStreamTypeOld;
					fccStreamTypeOld = fccStreamType;
					fccStreamType = readStringBytes();

					if (!fccStreamType.equals("vids")) {
						if (verbose) {
							System.out.println("      Not video stream (fcc '" + fccStreamType + "')");
						}

						fccStreamType = fccStreamTypeOld;

						return;
					}

					readStrh();

					raFile.seek(pos + size);
				} else {
					System.out.println("**Expected fcc 'strh', found fcc '" + fcc + "'");
					return;
				}

				readTypeAndSize();
				if (type.equals("strf")) {
					if (verbose) {
						System.out.println("   Stream format chunk (strf) detected...");
					}

					pos = raFile.getFilePointer();

					readStrf();

					raFile.seek(pos + size);
				} else {
					System.out.println("**Expected fcc 'strf', found fcc '" + fcc + "'");
					return;
				}

			} else {
				System.out.println("**Expected fcc 'strl', found fcc '" + fcc + "'");
				return;
			}

			readTypeAndSize();
			if (type.equals("strd")) {
				if (verbose) {
					System.out.println("   Stream 'strd' chunk detected and skipped");
				}
				raFile.seek(raFile.getFilePointer() + size);

			} else {
				if (verbose) {
					System.out.println("   Type '" + type + "' detected.  Backing up.");
				}
				raFile.seek(raFile.getFilePointer() - 8);
			}

			readTypeAndSize();
			if (type.equals("strn")) {
				if (verbose) {
					System.out.println("   Stream 'strn' chunk detected and skipped");
				}
				raFile.seek(raFile.getFilePointer() + size);
			} else {
				if (verbose) {
					System.out.println("   Type '" + type + "' detected.  Backing up.");
				}
				raFile.seek(raFile.getFilePointer() - 8);
			}

		} else {
			System.out.println("**Unexpected '" + type + "'");
			System.out.println("**Expected 'LIST' not found");
			return;
		}

		if (verbose) {
			System.out.println("");
		}
	}


	void readStrh() throws IOException {
		fccStreamHandler = readStringBytes();
		dwStreamFlags = readInt();
		dwStreamReserved1 = readInt();
		dwStreamInitialFrames = readInt();
		dwStreamScale = readInt();
		dwStreamRate = readInt();
		dwStreamStart = readInt();
		dwStreamLength = readInt();
		dwStreamSuggestedBufferSize = readInt();
		dwStreamQuality = readInt();
		dwStreamSampleSize = readInt();
		if (verbose) {
			System.out.println("      fccStreamType=" + fccStreamType);
			System.out.println("      fccStreamHandler=" + fccStreamHandler);
			System.out.println("      dwStreamFlags=" + dwStreamFlags);
			System.out.println("      dwStreamReserved1=" + dwStreamReserved1);
			System.out.println("      dwStreamInitialFrames=" + dwStreamInitialFrames);
			System.out.println("      dwStreamScale=" + dwStreamScale);
			System.out.println("      dwStreamRate=" + dwStreamRate);
			System.out.println("      dwStreamStart=" + dwStreamStart);
			System.out.println("      dwStreamLength=" + dwStreamLength);
			System.out.println("      dwStreamSuggestedBufferSize=" + dwStreamSuggestedBufferSize);
			System.out.println("      dwStreamQuality=" + dwStreamQuality);
			System.out.println("      dwStreamSampleSize=" + dwStreamSampleSize);
		}
	}


	void readStrf() throws Exception, IOException {
		//BMP header reader from BMP class

		BMPsize = readInt();
		BMPwidth = readInt();
		BMPheight = readInt();
		BMPplanes = readShort();
		BMPbitsPerPixel = readShort();
		BMPcompression = readInt();
		BMPsizeOfBitmap = readInt();
		BMPhorzResolution = readInt();
		BMPvertResolution = readInt();
		BMPcolorsUsed = readInt();
		BMPcolorsImportant = readInt();

		BMPtopDown = (BMPheight < 0);
		BMPnoOfPixels = BMPwidth * BMPheight;

		// Scan line is padded with zeroes to be a multiple of four bytes
		BMPscanLineSize = ((BMPwidth * BMPbitsPerPixel + 31) / 32) * 4;

		if (BMPsizeOfBitmap != 0) {
			BMPactualSizeOfBitmap = BMPsizeOfBitmap;
		} else {
			// a value of 0 doesn't mean zero - it means we have to calculate it
			BMPactualSizeOfBitmap = BMPscanLineSize * BMPheight;
		}

		if (BMPcolorsUsed != 0) {
			BMPactualColorsUsed = BMPcolorsUsed;
		} else
		// a value of 0 means we determine this based on the bits per pixel
				if (BMPbitsPerPixel < 16) {
			BMPactualColorsUsed = 1 << BMPbitsPerPixel;
		} else {
			BMPactualColorsUsed = 0;
		}            // no palette

		if (verbose) {
			System.out.println("      BMPsize=" + BMPsize);
			System.out.println("      BMPwidth=" + BMPwidth);
			System.out.println("      BMPheight=" + BMPheight);
			System.out.println("      BMPplanes=" + BMPplanes);
			System.out.println("      BMPbitsPerPixel=" + BMPbitsPerPixel);
			System.out.println("      BMPcompression=" + BMPcompression);
			System.out.println("      BMPsizeOfBitmap=" + BMPsizeOfBitmap);
			System.out.println("      BMPhorzResolution=" + BMPhorzResolution);
			System.out.println("      BMPvertResolution=" + BMPvertResolution);
			System.out.println("      BMPcolorsUsed=" + BMPcolorsUsed);
			System.out.println("      BMPcolorsImportant=" + BMPcolorsImportant);
			System.out.println("      >BMPnoOfPixels=" + BMPnoOfPixels);
			System.out.println("      >BMPscanLineSize=" + BMPscanLineSize);
			System.out.println("      >BMPactualSizeOfBitmap=" + BMPactualSizeOfBitmap);
			System.out.println("      >BMPactualColorsUsed=" + BMPscanLineSize);

			System.out.println("      Read up to " + raFile.getFilePointer());
			System.out.println("      Format ends at " + (pos + size));
		}

		if (BMPcompression != 0) {
			if (verbose) {
				System.out.println("**Unsupported compression");
			}
			//IJ.showMessage("AVI Reader", "Warning: AVI appears to be compressed.  Fatal error may follow.");
			//throw new Exception("Unsupported bits-per-pixel value");
			throw new Exception("AVI file must be uncompressed.");
		}

		if (!(BMPbitsPerPixel==8 || BMPbitsPerPixel==24 || BMPbitsPerPixel==32)) {
			throw new Exception("Unsupported bits-per-pixel value: "+BMPbitsPerPixel);
		}

		if (BMPactualColorsUsed != 0) {
			if (verbose) {
				System.out.println("      Now reading palette...");
			}           // + (pos + size) );

			long    pos1  = raFile.getFilePointer();

			byte[]  pr    = new byte[BMPcolorsUsed];
			byte[]  pg    = new byte[BMPcolorsUsed];
			byte[]  pb    = new byte[BMPcolorsUsed];
			for (int i = 0; i < BMPcolorsUsed; i++) {
				pb[i] = raFile.readByte();
				pg[i] = raFile.readByte();
				pr[i] = raFile.readByte();
				raFile.readByte();
			}
			if (verbose) {
				System.out.println("      Palette was " + (raFile.getFilePointer() - pos1) + " bytes long");
				System.out.println("      Palette ended at " + (raFile.getFilePointer()) + " and was expected to end at " + (pos + size));
			}

			cm = new IndexColorModel(BMPbitsPerPixel, BMPcolorsUsed, pr, pg, pb);

			if (verbose) {
				System.out.println("      Creating stack with palette...");
			}
			//stack = new ImageStack(dwWidth, BMPheight, cm);

		} else {
			if (verbose) {
				System.out.println("      Creating stack...");
			}
			//stack = new ImageStack(dwWidth, BMPheight);
		}
	}



	void skipJUNK() throws IOException {
		readTypeAndSize();
		if (type.equals("JUNK")) {
			if (verbose)
				System.out.println("JUNK stream detected and skipped");
			raFile.seek(raFile.getFilePointer() + size);
		}
	}


	void checkList(String thefcc) throws IOException {
		readTypeAndSizeAndFcc();
		if (!fcc.equals(thefcc)) {
			if (verbose)
				System.out.println("Type '" + type + "' with fcc '" + fcc + "' detected and skipped");
			raFile.seek(raFile.getFilePointer() + size);
		} else
			raFile.seek(raFile.getFilePointer() - 12);
		if (verbose)
			System.out.println("");
	}


	void readMovieData() throws Exception, IOException {
		readTypeAndSizeAndFcc();

		if (type.equals("LIST")) {
			if (verbose)
				System.out.println("Type 'LIST' detected of fcc '" + fcc + "'...");
			if (fcc.equals("movi")) {
				if (verbose)
					System.out.println("Movie data detected...");
				readTypeAndSizeAndFcc();
				if (type.equals("LIST") && fcc.equals("rec ")) {
					if (verbose)
						System.out.println("   Movie record detected and skipped");
				} else {
					if (verbose)
						System.out.println("   Type '" + type + "' and fcc '" + fcc + "' detected.  Backing up.");
					raFile.seek(raFile.getFilePointer() - 12);
				}
				readTypeAndSize();
				long  startPos  = raFile.getFilePointer();
				if (BMPbitsPerPixel <= 8){
					//ip = new ByteProcessor(dwWidth, BMPheight);
				}else{
				}
					//ip = new ColorProcessor(dwWidth, BMPheight);

				showTime(">>Entering while-loop to read chunks");

				showTime(">>Exiting while-loop to read chunks");
				if (verbose)
					System.out.println("End of video data reached with type '" + type + "'.  Backing up.");
				//raFile.seek(raFile.getFilePointer() - 8);
			} else
				System.out.println("**Expected fcc 'movi', but found fcc '" + fcc + "'");
		} else
			System.out.println("**Expected 'LIST', but found '" + type + "'");
		if (verbose)
			System.out.println("");
	}

	void unpack(byte[] rawData, int rawOffset, int bpp, byte[] byteData, int byteOffset, int w) throws Exception {
		if (byteData==null) System.out.println("bytedata null");
		if (rawData==null) System.out.println("rawdata null");
		for (int i = 0; i < w; i++)
			byteData[byteOffset + i] = rawData[rawOffset + i];
	}

	void unpack(byte[] rawData, int rawOffset, int[] intData, int intOffset, int w) {
		int  j     = intOffset;
		int  k     = rawOffset;
		int  mask  = 0xff;
		for (int i = 0; i < w; i++) {
			int  b0  = (((int) (rawData[k++])) & mask);
			int  b1  = (((int) (rawData[k++])) & mask) << 8;
			int  b2  = (((int) (rawData[k++])) & mask) << 16;
			if (BMPbitsPerPixel==32) k++; // ignore 4th byte (alpha value)
			intData[j] = 0xff000000 | b0 | b1 | b2;
			j++;
		}
	}



	void readFrame() throws Exception, IOException {
		int  len = BMPscanLineSize;
		if (BMPbitsPerPixel > 8){
			if ((intData==null)||(intData.length!=dwWidth*BMPheight)) intData = new int[dwWidth * BMPheight];}
		else
			{if ((byteData==null) || (byteData.length!=dwWidth*BMPheight)) byteData = new byte[dwWidth * BMPheight];}
		System.out.println("bytedata=null?"+(byteData==null)+" BMPbitsPerPixel="+BMPbitsPerPixel);
		if ((rawData==null)||(rawData.length!=BMPactualSizeOfBitmap)) rawData = new byte[BMPactualSizeOfBitmap];
		int  rawOffset  = 0;
		int  offset     = (BMPheight - 1) * dwWidth;
		for (int i = BMPheight - 1; i >= 0; i--) {
			int  n  = raFile.read(rawData, rawOffset, len);
			if (n < len)
				throw new Exception("Scan line ended prematurely after " + n + " bytes");
			if (BMPbitsPerPixel > 8)
				unpack(rawData, rawOffset, intData, offset, dwWidth);
			else
				unpack(rawData, rawOffset, BMPbitsPerPixel, byteData, offset, dwWidth);
			rawOffset += len;
			offset -= dwWidth;
		}
	}

	void getPalette() throws IOException {
		int  noOfEntries  = BMPactualColorsUsed;
		//System.out.println("noOfEntries: " + noOfEntries);
		if (noOfEntries > 0) {
			r = new byte[noOfEntries];
			g = new byte[noOfEntries];
			b = new byte[noOfEntries];

			int  reserved;
			for (int i = 0; i < noOfEntries; i++) {
				b[i] = (byte) raFile.read();
				g[i] = (byte) raFile.read();
				r[i] = (byte) raFile.read();
				reserved = raFile.read();
			}
		}
	}


	String readStringBytes() throws IOException {
		//reads next  4 bytes and returns them as a string
		byte[]  list;
		list = new byte[4];
		raFile.read(list);
		return new String(list);
	}



	int readInt() throws IOException {
		// 4 bytes
		// http://mindprod.com/endian.html
		int  accum    = 0;
		int  shiftBy;
		for (shiftBy = 0; shiftBy < 32; shiftBy += 8) {
			accum |= (raFile.readByte() & 0xff) << shiftBy;
		}
		return accum;
	}


	short readShort() throws IOException {
		// 2 bytes
		int  low   = raFile.readByte() & 0xff;
		int  high  = raFile.readByte() & 0xff;
		return (short) (high << 8 | low);
	}

	void readTypeAndSize() throws IOException {//(String listString, int fileLength) throws IOException{
		type = readStringBytes();
		size = readInt();
		//System.out.println("readTypeAndSize: "+type + " " + size+ " "+raFile.getFilePointer() );
	}


	void readTypeAndSizeAndFcc() throws IOException {// String listString, int fileLength, String fccString) throws IOException{
		//listString = readStringBytes();
		//fileLength = readInt();
		//fccString = readStringBytes();
		type = readStringBytes();
		size = readInt();
		fcc = readStringBytes();
	}


	void updateProgress() throws IOException {
		//IJ.showProgress((double) raFile.getFilePointer() / bigChunkSize);
		//IJ.write("update");
	}


	void showTime(String w) {
		if (showTimes) {
			long  thisTime  = System.currentTimeMillis();
			if (lastTime > 0) {
				System.out.println("    " + (thisTime - lastTime) + " spent between");
			}
			System.out.println((thisTime - startTime) + ": " + w);
			lastTime = thisTime;
		}
	}

}
