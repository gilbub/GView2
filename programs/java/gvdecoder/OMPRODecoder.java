package gvdecoder;

import java.io.*;
import java.nio.*;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;


/*

Header Information:

The header OMPRO files (from BumRac Choi)

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
}OMPRO[MAXOMPRO];;

two frames is 16x16+16=272
*/




public class OMPRODecoder extends ImageDecoderAdapter {

/*
constants are for the Export utility function only
*/
public static final int EXPORT_VM=1;   //export data as text (256 vals) representing camera 1 (vm)
public static final int EXPORT_CA=2;   //export data as text (256 vals) representing camera 2 (ca) if available
public static final int EXPORT_BOTH=3; //export data ast text (516 vals) representing camera 1 followed by 2 if available
public static final int EXPORT_ALL=4;  //export data ast text (variable vals) similar to export both but including two 16 val channels representing A/D data if available


RandomAccessFile Picfile;
public int FRAMESTEP=1;
int FrameNumber=0;

String filename;
String imagepath="";
long frameposition=0;
int orientation=2;
byte[] internalframe=new byte[528];
public static String navfilename;

int width;
int height;
int numframes;
int pixelsize;

long header_size=50;

int verMajor;
int verMinor;
int sTimeMonth;
int sTimeDate;
int sTimeYear;
int sTimeHour;
int sTimeMin;
int sTimeSec;
int sDataType;
int sDataByte;
int sNumChns;
int sChnStcVer;
int iNumFrames;
int sAcqDevice;
int sChnInt;
float fScanInt;
float fCamp;
float fAmp;
int sIsAnalyzed;
int sAnalType;
int iCommentSize;
char[] rest=new char[256];

MappedByteBuffer buffer;
ShortBuffer shortbuffer;
short[] singleframe;

public boolean SHOW_V_ONLY=false;
public boolean PAD=false;
public boolean VERBOSE=false;

public OMPRODecoder(){
 FrameNumber=0;
 this.filename=filename;
}

public int CloseImageFile(int instance){ try{
	//fc.close();
	buffer=null;
	shortbuffer=null;
	singleframe=null;
	Picfile.close();
	}catch(IOException e){
	e.printStackTrace();
	return -1;
	}
	return 1;
	}

public int OpenImageFile(String filename){
 int j=0;
 //open the file...
 try{
  Picfile=new RandomAccessFile(filename,"r");
  verMajor=getShort(Picfile);
  verMinor=getShort(Picfile);
  sTimeMonth=getShort(Picfile);
  sTimeDate=getShort(Picfile);
  sTimeYear=getShort(Picfile);
  sTimeHour=getShort(Picfile);
  sTimeMin=getShort(Picfile);
  sTimeSec=getShort(Picfile);
  sDataType=getShort(Picfile);
  sDataByte=getShort(Picfile);
  sNumChns=getShort(Picfile);
  sChnStcVer=getShort(Picfile);
  iNumFrames=getInt(Picfile);
  sAcqDevice=getShort(Picfile);
  sChnInt=getShort(Picfile);
  fScanInt=getFloat(Picfile);
  fCamp=getFloat(Picfile);
  fAmp=getFloat(Picfile);
  sIsAnalyzed=getShort(Picfile);
  sAnalType=getShort(Picfile);
  iCommentSize=getShort(Picfile);
  header_size=Picfile.getFilePointer();
  if (VERBOSE){
  System.out.println("header = "+header_size+ " total = "+Picfile.length());
  System.out.println("verMajor="+verMajor+
  					"\n verMinor="+verMinor+
   					"\n sTimeMonth="+sTimeMonth+
   					"\n sTimeDate="+sTimeDate+
   					"\n sTimeYear="+sTimeYear+
   					"\n sTimeHour="+sTimeHour+
   					"\n sTimeSec="+sTimeSec+
   					"\n sDataType="+sDataType+
   					"\n sDataByte="+sDataByte+
   					"\n sNumChns="+sNumChns+
   					"\n ChnStcVer="+sChnStcVer+
   					"\n iNumFrames="+iNumFrames+
   					"\n sAcqDevice="+sAcqDevice+
   					"\n sChnInt="+sChnInt +
   					"\n fScanInt"+fScanInt+
   					"\n sCamp="+fCamp+
   					"\n sAmp="+fAmp+
   					"\n sIsAnalyzed="+sIsAnalyzed+
   					"\n sAnalType="+sAnalType+
   					"\n sCommentSize="+iCommentSize
   					);
  for (int jj=0;jj<iCommentSize;jj++){System.out.print(rest[jj]);}
 }

 FileChannel fc = Picfile.getChannel();
 System.out.println("reading file into memory ="+fc.size());
 try{
 buffer = fc.map (FileChannel.MapMode.READ_ONLY, 0, fc.size());
 buffer.order(ByteOrder.LITTLE_ENDIAN);
 shortbuffer=buffer.asShortBuffer();

 }catch(Exception e){
	 System.out.println("Unable to map the file, trying alternate method");
     ByteBuffer tmp = ByteBuffer.allocate((int)fc.size());
     fc.read(tmp);
     tmp.order(ByteOrder.LITTLE_ENDIAN);
     shortbuffer=tmp.asShortBuffer();
 }



 singleframe=new short[sNumChns];
 fc.close();

 }
 catch(EOFException e){System.out.println("eof: number of frames read is "+j); return -1;}
 catch(IOException e){System.out.println("error reading the file, please check..."); e.printStackTrace(); return -1;}
 return 1;
}

/**todo**/
public void stich(){
	//keep header of the first one, load in successive frames

}

public int FilterOperation(int OperationCode, int startx, int endx, int instance){
	   if (OperationCode==0){
		   FRAMESTEP=startx;
		   if (FRAMESTEP<1) FRAMESTEP=1;
		   return 1;
	   }
	   return -1;
}


public String ReturnSupportedFilters(){return("FRAMESTEP");}

public int ReturnFrameNumber(){return FrameNumber;}

public int JumpToFrame(int framenum, int instance){
	   FrameNumber=framenum;
	   return FrameNumber;
}


public int ReturnXYBandsFrames(int[] arr, int instance){
	if (PAD) {
		arr[0]=18;
		arr[1]=18;
	}
	else{
	arr[0]=16;
	arr[1]=16;
	}
	if (!SHOW_V_ONLY){
	if (sNumChns==544) arr[1]=34;
	else if (sNumChns==512) arr[1]=32;
	else if (sNumChns==256) arr[1]=16;
	else if (sNumChns==272) arr[1]=17;
	else {
		System.out.println("UNHANDLED OMPRO CHANEL COUNT ERROR");
		return -1;}
    }
	arr[2]=2;
	arr[3]=(int)((double)iNumFrames/FRAMESTEP);
	return 1;
}


public int UpdateImageArray(int[] arr, int xdim, int ydim, int instance){
    int val=0;
    for (int j=0;j<arr.length;j++)arr[j]=0;
	if (PAD){
    try{
     for (int k=0;k<FRAMESTEP;k++){
	 for (int y=0;y<ydim-2;y++){
		 for (int x=0;x<xdim-2;x++){
	       int from=y*(xdim-2)+x;
	       int to=(y+1)*xdim+(x+1);
		       val=(int)buffer.getShort((from*2)+(int)(header_size+258+(FrameNumber*FRAMESTEP+k)*sNumChns*2));
		       arr[to]+=val;
		   }
	   }
	  for (int x=0;x<xdim;x++){arr[x]=arr[x+ydim];arr[x+ydim*(xdim-1)]=arr[x+ydim*(xdim-2)];}
	  for (int y=0;y<ydim;y++){arr[y*ydim]=arr[y*ydim+1];arr[y*ydim+ydim-1]=arr[y*ydim+ydim-2];}
    }
     FrameNumber++;
    }  catch(Exception e){
		System.out.println("error reading into filebuffer in PicDecoder");
		e.printStackTrace();
		return -1;
		}
   }//end pad
   else{
	  try{
     for (int k=0;k<FRAMESTEP;k++){
	 for (int i=0;i<xdim*ydim;i++){
		       val=(int)buffer.getShort((i*2)+(int)(header_size+258+(FrameNumber*FRAMESTEP+k)*sNumChns*2));
		       arr[i]+=val;
		       }
    }
     FrameNumber++;
    }  catch(Exception e){
		System.out.println("error reading into filebuffer in PicDecoder 2");
		e.printStackTrace();
		return -1;
		}
  }
return 1;
}


public int SumROIs(int[][] rois, String outfile, int startframe, int endframe,int instance){
     if (VERBOSE) System.out.println("in adapter SumROIs start="+startframe+" end="+endframe);
     //determine x,y dimensions
     int[] dims=new int[4];
     ReturnXYBandsFrames(dims, 0);
     int framewidth=dims[0];
     int frameheight=dims[1];
     int numframes=dims[3];
     if (VERBOSE) System.out.println("debug width ="+framewidth+" height ="+frameheight+" num frames ="+numframes);
     //bounds check
     if (endframe<0) endframe=numframes; /*a shortcut for scanning whole record*/
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
		 if (VERBOSE) System.out.println("USING UNIT ROI OMPRO METHOD");
		 for (int k=startframe;k<endframe;k++){
		 for (int i=0;i<rois.length;i++){
		 for (int f=0;f<FRAMESTEP;f++){
		  sum[k-startframe][i]=(int)buffer.getShort((rois[i][0]*2)+(int)(header_size+258+(k*FRAMESTEP+f)*sNumChns*2));//getOMPROShort(Picfile);
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
int getOMPROShort(RandomAccessFile f) throws IOException{
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


public static void main(String[] args){
System.out.println("OMPRO reader");
if (args.length!=5){
	System.out.println(
		 "export OMPRO files to ascii.  Usage:\n"+
		 "java OMPRODecoder infile, outfile, startframenumber, endframenumber, mode \n"+
		 "where infile is a raw data file from Guy Salama's optical mapping system\n"+
		 "      start and endframenumber give the range of frames to export to ascii\n"+
		 "      (*** where endframenumber==-1 is short for max number of frames)\n"+
		 "      and mode=1 (Vm only) mode=2 (Ca only) mode=3 (Vm and Ca) mode=4 (Vm,Ca and added non camera channels)\n");
  return;
}
OMPRODecoder pd=new OMPRODecoder();

int success=pd.OpenImageFile(args[0]);
if (success==0) {
	System.out.println("Error reading input file");
	return;
}
int mode=Integer.parseInt(args[4]);
if ((mode>0)&&(mode<5))
 pd.ExportFrames(args[1],Integer.parseInt(args[2]),Integer.parseInt(args[3]),Integer.parseInt(args[4]));
else
 System.out.println("mode must be either 1,2,3, or 4");
}

}


