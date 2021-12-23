package gvdecoder;

import java.io.OutputStream;
import java.io.File;
import java.io.DataOutputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.FileOutputStream;

public class cameraSaver extends Thread{

 public int lastsavedindex=-1;
 public int savetimeinterval;
 public String filename;
 PhotometricsCamera cam;
 DataOutputStream out;
 String dataFile;
 boolean file_ok=false;
 int intended_number_of_frames=10000;
 int ms_delay_between_saves=50;
 int currentFrame;
 int currentBuffer;

 public cameraSaver(PhotometricsCamera cam, int total_to_save, String filename, int wait_time){
	 this.cam=cam;
	 this.intended_number_of_frames=total_to_save;
	 setupSaveFile(filename);
	 ms_delay_between_saves=wait_time;

	 if (file_ok){
  	   System.out.println("cameraSaver ready, start the thread.");
     }else{
	  System.out.println("cameraSaver not ready, file can't open? don't start the thread.");
	 }
 }

 public boolean setupSaveFile(String dataFile){
  //open file for writing binary data
  //add a header
  this.dataFile=dataFile;
  try{
  out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(dataFile)));
  out.writeInt(1); //version, saved as shorts.
  out.writeInt(intended_number_of_frames);
  out.writeInt(cam.dat.ydim);
  out.writeInt(cam.dat.xdim);
  file_ok=true;
  }catch(IOException e){ file_ok=false; e.printStackTrace(); return false;}
  return true;
 }

 public boolean save(int z){
  //get cams buffer at the z position, and store in binary format
  //this is the slowest way, but might work as output is buffered
  if (!file_ok){
   System.out.println("ERROR in cameraSaver save: file_ok flag is false at z="+z);
   return false;
  }
  try{
  for (int y=0;y<cam.dat.ydim;y++){
   for (int x=0;x<cam.dat.xdim;x++){
     double v=cam.dat.get(z,y,x);
      //out.writeShort((int)v);
      //comment out the above line and uncomment the writeDouble line below to get the same format as before
      out.writeDouble(v);
     }
    }
   }catch (IOException e){e.printStackTrace(); file_ok=false; return false;}
   return true;
 }

 public void force_stop(){
  file_ok=false;
 }
 int lostBuffers=0;
 public void run(){
 //every savetimeinterval ms, check cam's currentFrame. If currentframe != to (lastsavedindex%CameraCircularBufferSize),
 //then save(lastsavedindex%CameraCircularBufferSize) and  increment lastsavedindex.
  int z,c;
  int totalsaved=0;
  currentBuffer=cam.currentBuffer;
  System.out.println("thread started");
  while ((file_ok)&&(totalsaved<intended_number_of_frames)){
	int cameras_currentFrame=cam.currentFrame;
	int cameras_currentBuffer=cam.currentBuffer;
    if ((cameras_currentFrame>currentFrame)&&(cameras_currentBuffer==currentBuffer)){
      c=0;
      for (z=currentFrame;z<cameras_currentFrame;z++) {save(z); c++; totalsaved++;}
      currentFrame+=c;
    }else
    if (cameras_currentBuffer==currentBuffer+1){
       c=0;
       for (z=currentFrame;z<=cam.dat.zdim;z++) {save(z); c++; totalsaved++;}
       for (z=0;z<cameras_currentFrame;z++) {save(z); c++; totalsaved++;}
       currentBuffer+=1;
    }else
    if (cameras_currentBuffer>currentBuffer+1){
		System.out.println("ERROR: lost data at frame "+totalsaved);
		lostBuffers+=cameras_currentBuffer-currentBuffer;
		System.out.println("ERROR: lost data at frame number "+totalsaved+" lost buffers = "+lostBuffers);
		currentBuffer=cameras_currentBuffer;
	}

   try{
   Thread.sleep(ms_delay_between_saves);
   }catch(Exception e){e.printStackTrace();}
  }
    try{
	  System.out.println("finished saving "+totalsaved+" frames with "+lostBuffers+" lost buffers");
      out.close();
  }catch(IOException e){e.printStackTrace();}
 }

}