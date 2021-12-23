package gvdecoder;import java.awt.*;import java.awt.image.*;import java.io.*;import java.util.*;import javax.media.jai.iterator.*;/**This is based on (copied in large part from) a plugin for ImageJ. .... GBThis plugin saves stacks in AVI format. It is based on the FileAvi class written byWilliam Gandler. The FileAvi class is part of Matthew J. McAuliffe's MIPAV program,available from http://mipav.cit.nih.gov/.*/public class AVIEncoder implements Runnable{	private RandomAccessFile raFile;	private int bytesPerPixel;    private File            file;    private int             bufferFactor;    public int              xDim,yDim,zDim,tDim;    private int             lutBufferRemapped[] = null;    private int             microSecPerFrame;    private int             xPad;    private byte[]          bufferWrite;    private int             bufferSize;    private int             indexA, indexB;    private float           opacityPrime;    private int             bufferAdr;    private byte[]          lutWrite = null;    private int[]           dcLength = null;    //public ImageDecoder imp;    public int[] singleframe;    public int fps=20;    Viewer2 vw;    StatusMonitor monitor;    boolean WRITEUNFILTERED;    boolean SHOWEXTAS;    int start;    int end;    public AVIEncoder(Viewer2 vw, String filename, int start, int end, int fps, boolean filtered, boolean showextras, StatusMonitor monitor){		this.vw=vw;		this.monitor=monitor;		this.WRITEUNFILTERED=filtered;		this.SHOWEXTRAS=showextras;		this.start=start;		this.end=end;		this.fps=fps;		vw.SilentJumpToFrame(start,SHOWEXTRAS);//do this to make sure that initialization has right number of bands. This command can change the color depth.	   try{		   file=new File(filename);	   }catch(Exception e){e.printStackTrace();}	}    public void run(){		try{		writeImage();	  }catch(Exception e){e.printStackTrace();}	}    public void writeImage() throws IOException{        byte[] signature;        long saveFileSize; // location of file size in bytes not counting first 8 bytes        byte[] RIFFtype;        byte[] CHUNKsignature;        long saveLIST1Size; // location of length of CHUNK with first LIST - not including                            // first 8 bytes with LIST and size.  JUNK follows the end of                            // this CHUNK        byte[] CHUNKtype;        byte[] avihSignature;        int[] extents;        long saveLIST1subSize; // location of length of CHUNK with second LIST        byte[] strhSignature;        byte[] type;        byte[] handler;        byte[] strfSignature;        long savestrfSize; // location of lenght of strf CHUNK - not including the first                           // 8 bytes with strf and size.  strn follows the end of this                           // CHUNK.        int resXUnit = 0;        int resYUnit = 0;        float xResol = 0.0f; // in distance per pixel        float yResol = 0.0f; // in distance per pixel        long biXPelsPerMeter = 0L;        long biYPelsPerMeter = 0L;        byte[] strnSignature;        byte[] text;        long savestrnPos;        byte[] JUNKsignature;        long saveJUNKsignature;        int paddingBytes;        int i;        long saveLIST2Size;        byte[] dataSignature;        byte[] idx1Signature;        long savedbLength[];        long savedcLength[];        long idx1Pos;        long endPos;        long saveidx1Length;        int t,z;        long savemovi;        int xMod;        bytesPerPixel= vw.jp.filtered.getSampleModel().getNumBands();        lutBufferRemapped = new int[1];        raFile = new RandomAccessFile(file, "rw");        signature = new byte[4];        signature[0] = 82; // R        signature[1] = 73; // I        signature[2] = 70; // F        signature[3] = 70; // F        raFile.write(signature);        saveFileSize = raFile.getFilePointer();        // Bytes 4 thru 7 contain the length of the file.  This length does        // not include bytes 0 thru 7.        writeInt(0); // for now write 0 in the file size location        RIFFtype = new byte[4];        RIFFtype[0] = 65; // A        RIFFtype[1] = 86; // V        RIFFtype[2] = 73; // I        RIFFtype[3] = 32; // space        raFile.write(RIFFtype);        // Write the first LIST chunk, which contains information on data decoding        CHUNKsignature = new byte[4];        CHUNKsignature[0] = 76; // L        CHUNKsignature[1] = 73; // I        CHUNKsignature[2] = 83; // S        CHUNKsignature[3] = 84; // T        raFile.write(CHUNKsignature);        // Write the length of the LIST CHUNK not including the first 8 bytes with LIST and        // size.  Note that the end of the LIST CHUNK is followed by JUNK.        saveLIST1Size = raFile.getFilePointer();        writeInt(0); // for now write 0 in avih sub-CHUNK size location        // Write the chunk type        CHUNKtype = new byte[4];        CHUNKtype[0] = 104; // h        CHUNKtype[1] = 100; // d        CHUNKtype[2] = 114; // r        CHUNKtype[3] = 108; // l        raFile.write(CHUNKtype);        // Write the avih sub-CHUNK        avihSignature = new byte[4];        avihSignature[0] = 97; // a        avihSignature[1] = 118; // v        avihSignature[2] = 105; // i        avihSignature[3] = 104; // h        raFile.write(avihSignature);        writeInt(0x38); // Write the length of the avih sub-CHUNK (38H) not including the                        // the first 8 bytes for avihSignature and the length        microSecPerFrame = (int)((1.0/fps)*1.0e6);        writeInt(microSecPerFrame); // dwMicroSecPerFrame - Write the microseconds per frame        writeInt(0); // dwMaxBytesPerSec                                    // Write the maximum data rate of the file in bytes per second        writeInt(0); // dwReserved1 - Reserved1 field set to zero        writeInt(0x10); // dwFlags - just set the bit for AVIF_HASINDEX       tDim = 1;       zDim = end-start;       if (WRITEUNFILTERED){		 int[] arr=new int[4];         vw.im.ReturnXYBandsFrames(arr, vw.instance);         yDim = arr[0];         xDim = arr[1];	    }	    else{		 //get dimensions from image		 xDim=vw.jp.filtered.getWidth();		 yDim=vw.jp.filtered.getHeight();		}       singleframe=new int[xDim*yDim];       xPad = 0;       xMod = xDim%4;       if (xMod != 0) {           xPad = 4 - xMod;           xDim = xDim + xPad;       }       writeInt(zDim*tDim); // dwTotalFrames - total frame number       writeInt(0); // dwInitialFrames -Initial frame for interleaved files.                    // Noninterleaved files should specify 0.       writeInt(1); // dwStreams - number of streams in the file - here 1 video and zero audio.       writeInt(0); // dwSuggestedBufferSize - Suggested buffer size for reading the file.                    // Generally, this size should be large enough to contain the largest                    // chunk in the file.       writeInt(xDim); // dwWidth - image width in pixels       writeInt(yDim); // dwHeight - image height in pixels                       // dwReserved[4] - Microsoft says to set the following 4 values to 0.       writeInt(0);       writeInt(0);       writeInt(0);       writeInt(0);       // Write the Stream line header CHUNK       raFile.write(CHUNKsignature); // Write LIST to the file       // Write the size of the first LIST subCHUNK not including the first 8 bytes with       // LIST and size.  Note that saveLIST1subSize = saveLIST1Size + 76, and that       // the length written to saveLIST1subSize is 76 less than the length written to saveLIST1Size.       // The end of the first LIST subCHUNK is followed by JUNK.       saveLIST1subSize = raFile.getFilePointer();       writeInt(0); // for now write 0 in CHUNK size location       // Write the chunk type        CHUNKtype[0] = 115; // s        CHUNKtype[1] = 116; // t        CHUNKtype[2] = 114; // r        CHUNKtype[3] = 108; // l        raFile.write(CHUNKtype);        // Write the strh sub-CHUNK        strhSignature = new byte[4];        strhSignature[0] = 115; // s        strhSignature[1] = 116; // t        strhSignature[2] = 114; // r        strhSignature[3] = 104; // h        raFile.write(strhSignature);        writeInt(56); // Write the length of the strh sub-CHUNK        // fccType - Write the type of data stream - here vids for video stream        type = new byte[4];        type[0] = 118; // v        type[1] = 105; // i        type[2] = 100; // d        type[3] = 115; // s        raFile.write(type);        handler = new byte[4];          // Write DIB for Microsoft Device Independent Bitmap.  Note: Unfortunately,        // at least 3 other four character codes are sometimes used for uncompressed        // AVI videos: 'RGB ', 'RAW ', 0x00000000         handler[0] = 68; // D         handler[1] = 73; // I         handler[2] = 66; // B         handler[3] = 32; // space         raFile.write(handler);        writeInt(0); // dwFlags        writeInt(0); // dwPriority - priority of a stream type.        writeInt(0);       writeInt(1); // dwScale       writeInt(fps); //  dwRate - frame rate for video streams       writeInt(0); // dwStart - this field is usually set to zero       writeInt(tDim*zDim); // dwLength - playing time of AVI file as defined by scale and rate       writeInt(0); // dwSuggestedBufferSize - Suggested buffer size for reading the stream.       writeInt(-1); // dwQuality - encoding quality given by an integer between       writeInt(0); // dwSampleSize #       writeShort((short)0); // left       writeShort((short)0); // top       writeShort((short)0); // right       writeShort((short)0); // bottom       // Write the stream format chunk       strfSignature = new byte[4];       strfSignature[0] = 115; // s       strfSignature[1] = 116; // t       strfSignature[2] = 114; // r       strfSignature[3] = 102; // f       raFile.write(strfSignature);          savestrfSize = raFile.getFilePointer();       writeInt(0); // for now write 0 in the strf CHUNK size location       writeInt(40); // biSize - Write header size of BITMAPINFO header structure       writeInt(xDim);  // biWidth - image width in pixels       writeInt(yDim);  // biHeight - image height in pixels.  If height is positive,       writeShort(1); // biPlanes - number of color planes in which the data is stored                                // This must be set to 1.       int bitsPerPixel = (bytesPerPixel==3) ? 24 : 8;       writeShort((short)bitsPerPixel); // biBitCount - number of bits per pixel #                               // 0L for BI_RGB, uncompressed data as bitmap       //writeInt(bytesPerPixel*xDim*yDim*zDim*tDim); // biSizeImage #       writeInt(0); // biSizeImage #       writeInt(0); // biCompression - type of compression used       writeInt(0); // biXPelsPerMeter - horizontal resolution in pixels       writeInt(0); // biYPelsPerMeter - vertical resolution in pixels                                             // per meter       if (bitsPerPixel==8)		writeInt(256); // biClrUsed	   else		writeInt(0); // biClrUsed       writeInt(0); // biClrImportant       if (bytesPerPixel==1) {          createLUT();          raFile.write(lutWrite);       }       // Use strn to provide a zero terminated text string describing the stream       savestrnPos = raFile.getFilePointer();       raFile.seek(savestrfSize);       writeInt((int)(savestrnPos - (savestrfSize+4)));       raFile.seek(savestrnPos);       strnSignature = new byte[4];       strnSignature[0] = 115; // s       strnSignature[1] = 116; // t       strnSignature[2] = 114; // r       strnSignature[3] = 110; // n       raFile.write(strnSignature);       writeInt(16); // Write the length of the strn sub-CHUNK       text = new byte[16];       text[0] = 70; // F       text[1] = 105; // i       text[2] = 108; // l       text[3] = 101; // e       text[4] = 65; // A       text[5] = 118; // v       text[6] = 105; // i       text[7] = 32; // space       text[8] = 119; // w       text[9] = 114; // r       text[10] = 105; // i       text[11] = 116; // t       text[12] = 101; // e       text[13] = 32; // space       text[14] = 32; // space       text[15] = 0; // termination byte       raFile.write(text);       // write a JUNK CHUNK for padding       saveJUNKsignature = raFile.getFilePointer();       raFile.seek(saveLIST1Size);       writeInt((int)(saveJUNKsignature - (saveLIST1Size+4)));       raFile.seek(saveLIST1subSize);       writeInt((int)(saveJUNKsignature - (saveLIST1subSize+4)));       raFile.seek(saveJUNKsignature);       JUNKsignature = new byte[4];       JUNKsignature[0] = 74; // J       JUNKsignature[1] = 85; // U       JUNKsignature[2] = 78; // N       JUNKsignature[3] = 75; // K       raFile.write(JUNKsignature);       paddingBytes = (int)(4084 - (saveJUNKsignature + 8));       writeInt(paddingBytes);       for (i = 0; i < (paddingBytes/2); i++) {         writeShort((short)0);       }       // Write the second LIST chunk, which contains the actual data        CHUNKsignature = new byte[4];        CHUNKsignature[0] = 76; // L        CHUNKsignature[1] = 73; // I        CHUNKsignature[2] = 83; // S        CHUNKsignature[3] = 84; // T        raFile.write(CHUNKsignature);        // Write the length of the LIST CHUNK not including the first 8 bytes with LIST and        // size.  The end of the second LIST CHUNK is followed by idx1.        saveLIST2Size = raFile.getFilePointer();        writeInt(0);  // For now write 0        savemovi = raFile.getFilePointer();        // Write CHUNK type 'movi'        CHUNKtype[0] = 109; // m        CHUNKtype[1] = 111; // 0        CHUNKtype[2] = 118; // v        CHUNKtype[3] = 105; // i        raFile.write(CHUNKtype);        savedbLength = new long[tDim*zDim];        savedcLength = new long[tDim*zDim];        dcLength = new int[tDim*zDim];        dataSignature = new byte[4];        dataSignature[0] = 48; // 0        dataSignature[1] = 48; // 0        dataSignature[2] = 100; // d        dataSignature[3] = 98; // b        bufferWrite = new byte[bytesPerPixel*xDim*yDim];        System.out.println("created buffer with len="+bufferWrite.length+" bytesperpixel="+bytesPerPixel+" xdim,ydim="+xDim+","+yDim);        monitor.showProgress(0.0);        for (z = start; z < end; z++) {              if (monitor.isCancelled()) {				  raFile.close();				  monitor.showProgress(0.0);				  return;			  }			  monitor.showProgress((double)(z-start)/(end-start));              raFile.write(dataSignature);              savedbLength[z-start] = raFile.getFilePointer();              writeInt(bytesPerPixel*xDim*yDim); // Write the data length              if (WRITEUNFILTERED) writeUnfilteredByteFrame(z);              else writeFilteredFrame(z);         }        // Write the idx1 CHUNK        // Write the 'idx1' signature        idx1Pos = raFile.getFilePointer();        raFile.seek(saveLIST2Size);        writeInt((int)(idx1Pos - (saveLIST2Size + 4)));        raFile.seek(idx1Pos);        idx1Signature = new byte[4];        idx1Signature[0] = 105; // i        idx1Signature[1] = 100; // d        idx1Signature[2] = 120; // x        idx1Signature[3] = 49; // 1        raFile.write(idx1Signature);        // Write the length of the idx1 CHUNK not including the idx1 signature and the 4 length        // bytes. Write 0 for now.        saveidx1Length = raFile.getFilePointer();        writeInt(0);        for (z = 0; z < zDim; z++) {          // In the ckid field write the 4 character code to identify the chunk 00db or 00dc           raFile.write(dataSignature);           if (z == 0) {                writeInt(0x10); // Write the flags - select AVIIF_KEYFRAME           }           else {             writeInt(0x00);           }                         // AVIIF_KEYFRAME 0x00000010L           writeInt((int)(savedbLength[z]- 4 - savemovi));             // Write the offset (relative to the 'movi' field) to the relevant CHUNK          writeInt(bytesPerPixel*xDim*yDim); // Write the length of the relevant                                            // CHUNK.  Note that this length is                                            // also written at savedbLength          }  // for (z = 0; z < zDim; z++)        endPos = raFile.getFilePointer();        raFile.seek(saveFileSize);        writeInt((int)(endPos - (saveFileSize+4)));        raFile.seek(saveidx1Length);        writeInt((int)(endPos - (saveidx1Length+4)));        raFile.close();        monitor.showProgress(1.0);    }    public void writeUnfilteredByteFrame(int slice) throws IOException {        /*this part updates viewArray*/		vw.im.JumpToFrame(slice, vw.instance);		vw.im.UpdateImageArray(vw.datArray, vw.speinfo.X_dim, vw.speinfo.Y_dim, vw.instance);		vw.rescale();		int c, offset, index = 0;		for (int y=vw.Y_dim-1; y>=0; y--) {			offset = y*vw.X_dim;			for (int x=0; x<vw.X_dim; x++)				bufferWrite[index++] = (byte)vw.viewArray[offset++];			for (int i = 0; i<xPad; i++)				bufferWrite[index++] = (byte)0;		}		raFile.write(bufferWrite);	}    public boolean SHOWEXTRAS=false;	public void writeFilteredFrame(int slice) throws IOException {			vw.SilentJumpToFrame(slice,SHOWEXTRAS);			RandomIter iter = RandomIterFactory.create(vw.jp.filtered, null);            int bands = vw.jp.filtered.getSampleModel().getNumBands();            int by=vw.jp.filtered.getHeight();            int bx=vw.jp.filtered.getWidth();			int index=0;			for (int y=by-1; y>=0; y--) {				for (int x=0; x<bx; x++) {					for (int b=bands-1;b>=0;b--){						bufferWrite[index++] = (byte)iter.getSample(x, y, b);				     }				 }				for (int i = 0; i<xPad; i++) {					for (int b=0;b<bands;b++){						bufferWrite[index++] = (byte)0;				     }				}			}			raFile.write(bufferWrite);	}	public void createLUT() {		lutWrite = new byte[4*256];		for (int i = -127; i<128; i++) {				lutWrite[4*(127+i)] = (byte)(i+127);				lutWrite[4*(127+i)+1] = (byte)(i+127);				lutWrite[4*(127+i)+2] = (byte)(i+127);				lutWrite[4*(127+i)+3] = (byte)0;		}	} 	final void writeInt(int v) throws IOException {		raFile.write(v & 0xFF);		raFile.write((v >>>  8) & 0xFF);		raFile.write((v >>> 16) & 0xFF);		raFile.write((v >>> 24) & 0xFF);	}	final void writeShort(int v) throws IOException {		raFile.write(v& 0xFF);		raFile.write((v >>> 8) & 0xFF);	}}