package gvdecoder;
/* a class that holds information pertinent to an open spe file.*/
import java.io.*;
public class JSPE extends SPEInfo{

RandomAccessFile spefile;
int[] rawdata;
byte[] spedata;
int[] flippeddata;
int[] background;
int[] normalframe;
boolean isOpen;
boolean isCurrent;
int framenumber=0;
int framesize;
int DataType;

public JSPE(){
}

public boolean  readFrame(){
try{
 spefile.readFully(spedata);
}catch(IOException e){return false;}
return true;
}

public void close(){
try{
spefile.close();
}catch(IOException e){;}
}

public JSPE(String filename, boolean Initialize){
try{

	Filename=filename;
	byte[] twobts=new byte[2];
	byte bt1;
	if (spefile!=null) spefile.close();

	spefile=new RandomAccessFile(filename,"r");
    long filesize=spefile.length();

    X_dim=SPEUtils.readDOSInt(spefile,42);
	DataType=SPEUtils.readDOSInt(spefile,108);
	Y_dim = SPEUtils.readDOSInt(spefile,656);
    NumberOfFrames=(int)(((filesize-4100)/4)/(X_dim*Y_dim));

    if (Initialize){
    spedata =new byte[X_dim*Y_dim*4];
    rawdata=new int[X_dim*Y_dim];
	flippeddata=new int[X_dim*Y_dim];
	framesize=4*X_dim*Y_dim;
    }
    spefile.seek(4100);

 }catch(IOException e){
  e.printStackTrace();
 }


}

}