package gvdecoder;
/* a utility class that holds information about a spe file. */
import java.io.*;
public class SPEInfo implements Serializable{

int X_dim;
int Y_dim;
int NumberOfFrames;
int DataType;
String Filename;
String NavFile;
boolean HasNavFile=false;
String Directory="";
String Note="";			//user adds a note to the file, potentially stored.
String ExtraDirectory;  //user adds extra info, potentially stored in this directory
boolean Error=false;

public String toString(){
	return "file: "+(Directory+Filename)+"\n dimension (x,y):("+X_dim+","+Y_dim+")\n  Frames: "+NumberOfFrames+"\n "+Note;
}

public String gnuplot(){
return "#file: "+(Directory+Filename)+"\n  # dimension (x,y):("+X_dim+","+Y_dim+")\n  # Frames: "+NumberOfFrames+"\n  #"+Note+"\n set Title"+Filename+"\n plot \""+Filename+"\" u 1:2 w l";

}

public String shortSummary(){
 return "("+X_dim+","+Y_dim+","+NumberOfFrames+"): "+Note;
}

public String longSummary(){
 return ""+(Directory+Filename)+" ("+X_dim+","+Y_dim+","+NumberOfFrames+")";
}


public SPEInfo(int x, int y, int num, String filename, String Directory){
 X_dim=x; Y_dim=y; NumberOfFrames=num; Filename=filename; Directory=Directory;
}

public SPEInfo(){
}


}