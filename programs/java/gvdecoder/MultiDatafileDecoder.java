package gvdecoder;
 import java.io.*;
 import java.util.*;
/**
 **/
public class MultiDatafileDecoder extends ImageDecoderAdapter{

   public String filetype="ompro";
   public boolean able_to_decode=false;
   public ArrayList datafiles;
   public int FrameNumber; //framenumber for whole dataset
   public int FrameInFile; //present framenumber in the subfile
   public int FramesInOpenFile; //the length of the present file
   public int OpenFileNumber;
   public String imagepath;
   public int TotalFrames;
   public int[] framemap;
   public boolean files_same_size=false;
   public ImageDecoder id;
   public String DirectoryAbsolutePath;
   public int[] inf;

   public MultiDatafileDecoder(String filetype){
	  FrameNumber=0;
	  this.filetype=filetype;
      datafiles=new ArrayList();
      inf=new int[4];
   }

   public  int UpdateImageArray(int[] arr,int xdim,int ydim, int instance){
	   id.UpdateImageArray(arr,xdim,ydim,instance);
	   return 1;
	   }

  public boolean FiletypeSupported(){
	  return true;
  }

   public  int OpenImageFile(String filename){
	   //finds
	   if (FiletypeSupported()){
		   File dir=new File(filename);
 		   String[] tmp=null;
       if (!dir.exists()) {System.out.println("directory not found"); return 0;}
       if (!dir.isDirectory()){
     	 System.out.println("the file isn't a directory");
         datafiles.add(dir.getName());
         imagepath=dir.getParent();
         }

       if (dir.isDirectory()){
         imagepath=filename;
         tmp=dir.list();
         }
       if (filetype.equals("ompro")){
       for (int i=0;i<tmp.length;i++){
	     if (tmp[i].indexOf(".s")>-1) datafiles.add(tmp[i]);
         }
        Collections.sort(datafiles,new gvdecoder.utilities.CompareOmproFiles());
	   }else{
		 for (int k=0;k<tmp.length;k++){
			 datafiles.add(tmp[k]);
		 }
        Collections.sort(datafiles);
	    }
       //construct framemap
        id=ImageDecoderFactory.getDecoder(filetype);
        framemap=new int[datafiles.size()];

        int lastsize=0;
        DirectoryAbsolutePath=dir.getAbsolutePath()+File.separator;
       for (int j=0;j<datafiles.size();j++){

		   String path=DirectoryAbsolutePath+(String)datafiles.get(j);
		   System.out.println("checking path="+path);
		   if (id.OpenImageFile(path)<1) return -1;
		   id.ReturnXYBandsFrames(inf,0);
		   lastsize+=inf[3];
		   framemap[j]=lastsize;
		   TotalFrames+=inf[3];
		   id.CloseImageFile(0);
	   }
       return 1;
	   }
	   else{
	    able_to_decode=false;
	    return -1;
       }
   }

   public  int CloseImageFile(int instance){
	   id.CloseImageFile(instance);
	   return 1;
	   }
   public int FilterOperation(int OperationCode, int startx, int endx, int instance){return 0;}
   public String ReturnSupportedFilters(){return "";}
   public int ReturnFrameNumber(){return FrameNumber;}

   public int lastframenum;
   // 1000 2000 3000
   public int FrameInFileNumber(int framenum){
	   if ((framenum<0)||(framenum>=TotalFrames)) return -1;
	   if (files_same_size){
		   return (int)( ((double)framenum/(double)TotalFrames)*framemap.length);
	   }else{
		   if (framenum<framemap[0]) return 0;
		   for (int j=1;j<framemap.length;j++){
			   if ((framenum<framemap[j])&&(framenum>=framemap[j-1])){
				   return j;
			   }
		   }
	   }
	   return -1;
   }

   public int GetFrameInFile(int framenum){
	   if (OpenFileNumber==0) return framenum;
	   return framenum-framemap[OpenFileNumber-1];
   }

   public  int JumpToFrame(int framenum, int instance){
	   //find out if the frame is in the presently opened file
	   if ((framenum==FrameNumber+1)&&(FrameInFile+1<FramesInOpenFile)){
		  FrameInFile++;
		  FrameNumber++;
		  id.JumpToFrame(FrameInFile,0);
		  return FrameNumber;
	   }
	   //is the present requested frame in the presently opened file?
	   int filenum=FrameInFileNumber(framenum);
	   if ((filenum==OpenFileNumber)&&(FrameInFile<FramesInOpenFile)){
		  FrameNumber=framenum;
		  FrameInFile=GetFrameInFile(framenum);
		  id.JumpToFrame(FrameInFile,0);
		  return FrameNumber;
	   }
	   //a new file has to be opened
	   id.CloseImageFile(0);
	   OpenFileNumber=filenum;
	   String path=DirectoryAbsolutePath+(String)datafiles.get(OpenFileNumber);
	   System.out.println("opening="+path);
	   id.OpenImageFile(path);
	   if (!files_same_size){
		   id.ReturnXYBandsFrames(inf,0);
		   FramesInOpenFile=inf[3];

	   }
	   //if (OpenFileNumber==0){
	   // FrameInFile=framenum;
	   //}else
	   FrameInFile=GetFrameInFile(framenum);//framenum-framemap[OpenFileNumber-1];
	   FrameNumber=framenum;
	   id.JumpToFrame(FrameInFile,0);
	   return FrameNumber;
	   }

   public int ReturnXYBandsFrames(int[] dat, int instance){
	   id.ReturnXYBandsFrames(inf,0);
	   dat[0]=inf[0];
	   dat[1]=inf[1];
	   dat[2]=inf[2];
	   dat[3]=TotalFrames;
	   return 1;
	   }

   public int SumROIs(int[][] rois, String outfile, int startframe, int endframe,int instance){
     System.out.println("in adapter SumROIs start="+startframe+" end="+endframe);
     //determine x,y dimensions
     int[] dims=new int[4];
     ReturnXYBandsFrames(dims, 0);
     int framewidth=dims[0];
     int frameheight=dims[1];
     int numframes=dims[3];
     System.out.println("debug width ="+framewidth+" height ="+frameheight+" num frames ="+numframes);
     //bounds check
     if (endframe<0) endframe=numframes; /*a shortcut for scanning whole record*/
	 if (endframe>numframes) endframe=numframes;
	 if (startframe<0) startframe=0;
     if (startframe>numframes) startframe=0;

     //create an array to hold the sums from the rois.
     int[][] sum=new int[endframe-startframe][rois.length];

     //create an array to read the data into.
     int[] tmparray=new int[framewidth*frameheight];

     for (int k=startframe;k<endframe;k++){//for each frame
     JumpToFrame(k,0); //goto frame
     if (FrameInFile==0){
	 		//new file just opened, recheck size
	 		ReturnXYBandsFrames(dims,0);
	 		if ((dims[0]!=framewidth)||(dims[1]!=frameheight)){
	 			System.out.println("Warning: dimension change in MultiDatafileDecoder!");
	 			framewidth=dims[0];
	 			frameheight=dims[1];
	 			tmparray=new int[framewidth*frameheight];
	 		}
	 }
     if (UpdateImageArray(tmparray,framewidth,frameheight,0)<0){
		 break;
		 }; //load image


     for (int i=0;i<rois.length;i++){ //for each roi

       for (int j=0;j<rois[i].length;j++){ //go to each element in the roi
         sum[k-startframe][i]+=tmparray[rois[i][j]];

       }//j
       //System.out.println("sim for frame "+k+" = "+sum[k-startframe][i]);
      }//i
     }//k

    //generate output
    try{
    PrintWriter file=new PrintWriter(new FileWriter(outfile),true);
    System.out.println("printing roi file end "+endframe+" start "+startframe);
    for (int j=startframe;j<endframe;j++){
	 // System.out.println("debug frame="+j);
	 file.print((j)+" ");
	// System.out.print("debug "+(j)+" ");
	 for (int i=0;i<rois.length;i++) {
		 //System.out.println("debug rois "+i+" out of "+rois.length);
		 file.print(sum[(j-startframe)][i]+" ");
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

}