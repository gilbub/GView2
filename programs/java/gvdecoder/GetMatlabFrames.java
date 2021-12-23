package gvdecoder;
import java.util.Scanner;
import java.util.Arrays;

//class to get frames off the circular buffer
//frame numbers to be specified bu user in command line
//1st number is start frame, 2nd number is end frame

//run as java GetFrames startFrame endFrame using command prompt

public static int[] datArray;
public static int[][] imageData;
BaslerController bas;

public class GetMatlabFrames{

public int[][] getFrames(int firstFrame, int lastFrame){
   if (datArray==null){
	   datArray=new int[bas.xdim*bas.ydim];
   }
   if (imageData==null){
       imageData= new int[lastFrame-firstFrame+1][];
   }
  int a=0;
  for (int i = firstFrame; i<lastFrame+1; i++) {
   getCircularBufferFrame(i,datArray);
   if (imageData[a]==null) imageData[a]=java.util.Arrays.copyOf(datArray,datArray.length);
   else{
	for (int j=0;j<bas.xdim*bas.ydim;j++) imageData[a][j]=datArray[j];
   }
   a++;
  }
  return imageData;
}

public GetMatlabFrames(BaslerControler bas){
	this.bas=bas;
}
}




}