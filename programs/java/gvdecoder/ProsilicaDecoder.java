package gvdecoder;

import java.io.*;
import java.nio.*;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;


/*

Header Information:

The header Prosilica files (from BumRac Choi)

struct GenDataHdr {
    int16_t verMajor;
    int16_t verMinor;
    int16_t sTimeMonth;
    int16_t sTimeDate;
    int16_t sTimeYear;
    int16_t sTimeHour;
    int16_t sTimeMin;
    int16_t sTimeSec;
    int16_t sDataType;
    int16_t sDataByte;
    int16_t sNumChns;
    int16_t sChnStcVer;
    int32_t iNumFrames;
    int16_t sAcqDevice;
    int16_t sChnInt;
    float   fScanInt;
    float   fCaAmp;
    float   fAmp;
    int16_t sIsAnalyzed;
    int16_t sAnalType;
    int32_t iCommentSize;
    char    rest[256];
}Prosilica[MAXProsilica];;

two frames is 16x16+16=272
*/




public class ProsilicaDecoder extends ImageDecoderAdapter {

/*
constants are for the Export utility function only
*/


RandomAccessFile Picfile;


String filename;
String imagepath="";
long frameposition=0;
int FrameNumber;


public static String navfilename;

int width;
int height;
int numframes;
int pixelsize;

int Width;
int Height;
int BinningX;
int BinningY;
int RegionX;
int RegionY;
int TotalBytesPerFrame;
float StatFrameRate; //based on calc
float FrameRate;     //based on user setting
int StatPacketsMissed;
int StatFramesDropped;
int StatFramesCompleted;
int GainValue;
char [] FrameStartTriggerMode=new char[128];
char [] FrameStartTriggerEvent=new char[128];
char [] AcqStartTriggerMode=new char[128];
char [] AcqStartTriggerEvent=new char[128];
char []AcqEndTriggerMode=new char[128];
char [] AcqEndTriggerEvent=new char[128];
char [] PixelFormat=new char[128];


char[] rest=new char[256];

MappedByteBuffer buffer;
ShortBuffer shortbuffer;
byte[] framebuffer;

public boolean SHOW_V_ONLY=false;
public boolean PAD=false;
public boolean VERBOSE=false;

public ProsilicaDecoder(){
 FrameNumber=0;
 this.filename=filename;
}

public int CloseImageFile(int instance){
	try{
	//fc.close();
	framebuffer=null;
	Picfile.close();
	}catch(IOException e){
	e.printStackTrace();
	return -1;
	}
	return 1;
	}


/*
typedef struct
{
unsigned long Width;    								4
unsigned long Height;									4
unsigned long BinningX;									4
unsigned long BinningY;									4
unsigned long RegionX;									4
unsigned long RegionY;									4
unsigned long TotalBytesPerFrame;						4
float StatFrameRate; //based on calc					4
float FrameRate;     //based on user setting			4
unsigned long StatPacketsMissed;						4
unsigned long StatFramesDropped;						4
unsigned long StatFramesCompleted;						4
unsigned long GainValue;								4
char FrameStartTriggerMode[128];						128
char  FrameStartTriggerEvent[128];						128
char  AcqStartTriggerMode[128];							128
char  AcqStartTriggerEvent[128];						128
char AcqEndTriggerMode[128];							128
char  AcqEndTriggerEvent[128];							128
char  PixelFormat[128];									128 =948
} iCamera;


*/

public int OpenImageFile(String filename){
 int j=0;
 //open the file...
 try{
  Picfile=new RandomAccessFile(filename,"r");
  Width=getInt(Picfile);
  Height=getInt(Picfile);
  BinningX=getInt(Picfile);
  BinningY=getInt(Picfile);
  RegionX=getInt(Picfile);
  RegionY=getInt(Picfile);
  TotalBytesPerFrame=getInt(Picfile);
  StatFrameRate=getFloat(Picfile);
  FrameRate=getFloat(Picfile);


  framebuffer=new byte[TotalBytesPerFrame];

 }
 catch(EOFException e){System.out.println("eof: number of frames read is "+j); return -1;}
 catch(IOException e){System.out.println("error reading the file, please check..."); e.printStackTrace(); return -1;}
 return 1;
}


public int FilterOperation(int OperationCode, int startx, int endx, int instance){
	  /*
	  if (OperationCode==0){
		   FRAMESTEP=startx;
		   if (FRAMESTEP<1) FRAMESTEP=1;
		   return 1;
	   }*/
	   return -1;


}


public String ReturnSupportedFilters(){return("FRAMESTEP");}

public int ReturnFrameNumber(){return FrameNumber;}

public int JumpToFrame(int framenum, int instance){
	   FrameNumber=framenum;
	   return FrameNumber;
}


public int ReturnXYBandsFrames(int[] arr, int instance){
	arr[0]=Width;
	arr[1]=Height;
	arr[2]=1;
	arr[3]=100; // temp

	return 1;
}


public int UpdateImageArray(int[] arr, int xdim, int ydim, int instance){
int val=0;
try{
	Picfile.seek(FrameNumber*TotalBytesPerFrame+948);
	Picfile.readFully(framebuffer);
    for (int x=0;x<TotalBytesPerFrame; x++){
	  val=(int)framebuffer[x];
	  if (val<0) val=256+val;
	  arr[x]=val;
	}
	FrameNumber++;
}catch(IOException e){e.printStackTrace();}
return 1;
}


public int SumROIs(int[][] rois, String outfile, int startframe, int endframe,int instance){
    /* if (VERBOSE) System.out.println("in adapter SumROIs start="+startframe+" end="+endframe);
     //determine x,y dimensions
     int[] dims=new int[4];
     ReturnXYBandsFrames(dims, 0);
     int framewidth=dims[0];
     int frameheight=dims[1];
     int numframes=dims[3];
     if (VERBOSE) System.out.println("debug width ="+framewidth+" height ="+frameheight+" num frames ="+numframes);
     //bounds check
     if (endframe<0) endframe=numframes;
	 if (endframe>numframes) endframe=numframes;
	 if (startframe<0) startframe=0;
     if (startframe>numframes) startframe=0;

     //create an array to hold the sums from the rois.
     System.out.println("allocating sum array");
     int[][] sum=new int[endframe-startframe][rois.length];

     //create an array to read the data into.
     System.out.println("allocating tmp array");
     int[] tmparray=new int[framewidth*frameheight];

     //this special case checks for unit rois and treats them differently
     boolean unitroi=true;
     for (int i=0;i<rois.length;i++){
		 if (rois[i].length!=1) unitroi=false;
		 }
	 if (unitroi){
		 if (VERBOSE) System.out.println("USING UNIT ROI Prosilica METHOD");
		 for (int k=startframe;k<endframe;k++){
		 for (int i=0;i<rois.length;i++){
		 for (int f=0;f<FRAMESTEP;f++){
		  sum[k-startframe][i]=(int)buffer.getShort((rois[i][0]*2)+(int)(header_size+258+(k*FRAMESTEP+f)*sNumChns*2));//getProsilicaShort(Picfile);
	    }
		}
   		}
	 }else{
     for (int k=startframe;k<endframe;k++){//for each frame
     JumpToFrame(k,0); //goto frame
     UpdateImageArray(tmparray,framewidth,frameheight,0); //load image
     for (int i=0;i<rois.length;i++){ //for each roi
       for (int j=0;j<rois[i].length;j++){ //go to each element in the roi
         sum[k-startframe][i]+=tmparray[rois[i][j]];
       }//j
       //System.out.println("sim for frame "+k+" = "+sum[k-startframe][i]);
      }//i
     }//k
	}//else
    //generate output
    try{
    PrintWriter file=new PrintWriter(new FileWriter(outfile),true);
    if (VERBOSE) System.out.println("printing roi file end "+endframe+" start "+startframe);
    for (int j=startframe;j<endframe;j++){
	 file.print((j)+" ");
	 for (int i=0;i<rois.length;i++) {
		 file.print(sum[(j-startframe)][i]+" ");
		 }
	 file.print("\n");
	 }
    file.close();
    if (VERBOSE) System.out.println("debug closed file");
    }catch(IOException e){System.out.println("error opening file for rois...");}
     catch(Exception e){System.out.println("Some other error in sumROis");e.printStackTrace();}
    */
    return 1;
}

public String export_valseparator=" ";
public String export_lineseparator="\n";
public String export_frameseparator="\n\n";

/** Utility method for export frame data to text files. Set global variables
export_valseparator, export_lineseparator, export_frameseparator to control output.
(export_lineseparator and export_frameseparator can be set to null to generate one
long line of data). Set endfame to -1 to have endframe=maxnumber frames. Set 'mode' variable
as follows (below are constants)
EXPORT_VM=1;   //export data as text (256 vals) representing camera 1 (vm)
EXPORT_CA=2;   //export data as text (256 vals) representing camera 2 (ca) if available
EXPORT_BOTH=3; //export data ast text (516 vals) representing camera 1 followed by 2 if available
EXPORT_ALL=4;  //export data ast text (variable vals) similar to export both but including two 16 val channels representing A/D data if available
*/

public void ExportFrames(String outfile,int startframe, int endframe, int mode){
	/*
	int[] info=new int[4];
	ReturnXYBandsFrames(info, 0);
	int xdim=info[0];
	int ydim=info[1];
	int[] frame=new int[xdim*ydim];
	if (endframe == -1) endframe=info[3];
	if (startframe < 0) startframe=0;
	if (endframe <= startframe) endframe=startframe+1;

	int ystart=0;
	int yend=16;
	if (sNumChns==256) { mode=EXPORT_VM; System.out.println("Error: mode switched to="+mode);}
	if ((sNumChns<512)&&(mode==EXPORT_CA)) {System.out.println("Error: no ca channel present"); return;}
	if ((sNumChns==272)&&(mode!=EXPORT_VM)) mode=EXPORT_ALL;
	boolean dropextrachannels=false;
	switch (mode){
		case EXPORT_VM:   ystart=0; yend=16; break;

		case EXPORT_CA:   if (sNumChns==544){
			               ystart=17;
		                   yend=34;
		                  }else{
						   ystart=16;
						   yend=32;
						  }
						  break;

		case EXPORT_ALL:  ystart=0;
		                  if (sNumChns==544){  yend=34; dropextrachannels=false;}
					      else if (sNumChns==512) yend=32;
						  else if (sNumChns==272) yend=17;
						  else yend=16;
						  break;

		case EXPORT_BOTH: ystart=0;
		                  if (sNumChns==544){yend=34;dropextrachannels=true;}
		                  else if (sNumChns==512)yend=32;
						  else if (sNumChns==272)yend=17;
						  else yend=16;
						  break;
		}

	try{
	    PrintWriter file=new PrintWriter(new FileWriter(outfile),true);
	    for (int j=startframe;j<endframe;j++){
		 UpdateImageArray(frame,xdim,ydim,0);
		 framescan:
		 for (int y=ystart;y<yend;y++){
		  if (dropextrachannels){     // very clumsy way to drop extra non camera channels when present
		    if (y==16) y++;
		    else if (y==33) {y=yend; break framescan;}
		   }
		  for (int x=0;x<xdim;x++) {
			 file.print(frame[y*xdim+x]+export_valseparator);
			 }
		  if(export_lineseparator!=null) file.print(export_lineseparator);
		 }
		 if (export_frameseparator!=null) file.print(export_frameseparator);
		 }
	    file.close();
	    if (VERBOSE) System.out.println("closed file");
	    }catch(IOException e){System.out.println("error opening file for rois...");}
}

public void ExportFramesCSL(String outfile, int startframe, int endframe, int mode){
	export_valseparator=",";
	export_lineseparator=null;
	export_frameseparator=null;
	ExportFrames(outfile,startframe,endframe,mode);
*/
}


/*convenience routines to get individual numbers from a random access file given different endian order*/

/**used in routines below*/
int getByte(RandomAccessFile f) throws IOException {
		int b = f.read();
		if (b ==-1) throw new IOException("unexpected EOF");
		return b;
	}

/**used in obtaining header information*/
int getShort(RandomAccessFile f) throws IOException {
	int b0 = getByte(f);
	int b1 = getByte(f);
	return ((b1 << 8) + b0);
}

/**not used*/
int getProsilicaShort(RandomAccessFile f) throws IOException{
	int tmp=getShort(f);
	if (tmp>=32768) tmp-=65536;
	return tmp;

}

/**used in obtaining header information*/
float getFloat(RandomAccessFile f) throws IOException {
	 int accum=0;
	 for (int shiftBy=0;shiftBy<32;shiftBy+=8){
		 accum|=(getByte(f) & 0xff)<<shiftBy;
	 }
	 return Float.intBitsToFloat(accum);
}

/**used in obtaining header information*/
int getInt(RandomAccessFile f) throws IOException{
	 int accum=0;
		 for (int shiftBy=0;shiftBy<32;shiftBy+=8){
			 accum|=(getByte(f) & 0xff)<<shiftBy;
		 }
		 return accum;
}

/** not used*/
short getShortInt(RandomAccessFile f) throws IOException{
	 short accum=0;
		 for (int shiftBy=0;shiftBy<16;shiftBy+=8){
			 accum|=(getByte(f) & 0xff)<<shiftBy;
		 }
		 return accum;
}

char getChar(RandomAccessFile f) throws IOException{
	int low=getByte(f)&0xff;
	int high=getByte(f);
	return (char)(high<<8|low);

}



}


