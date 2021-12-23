package gvdecoder.utilities;

import java.io.*;
import gvdecoder.log.*;
import gvdecoder.prefs.*;

public class FileNameManager{

public String lastDirectory;
public String lastFilename;
public static boolean UseLocalDirForNavCreation=true;

public String FindNavFileName(String filename,String filetype){

String navfilename=filename+".nav";
if (UseLocalDirForNavCreation){
try{
       File tmp=new File(filename+".nav");
       LogManager.getInstance().log("absolute path "+tmp.getAbsolutePath());
       LogManager.getInstance().log("canonical path "+tmp.getCanonicalPath());
       LogManager.getInstance().log("get name "+tmp.getName());
       LogManager.getInstance().log("absolute parent "+tmp.getParent());
       LogManager.getInstance().log("get path"+tmp.getPath());

       //generate a filename based on a local mirror
       String localPath =  PrefManager.getInstance().getString("CWRULocalMirrorPath");
       LogManager.getInstance().log("local Path is "+localPath);

       if (localPath!=null){
	LogManager.getInstance().log("constructing local path");
	String oldPath=tmp.getParent();
	oldPath=oldPath.replace(':','_');//get rid of colons.
	oldPath=oldPath.replace('\\','-');
	LogManager.getInstance().log("the new path is "+oldPath+File.separator+tmp.getName());
	LogManager.getInstance().log("try to create directory ");
	String newDir=localPath+File.separator+oldPath;
	File newPath=new File(newDir);
	if (newPath.mkdirs()) LogManager.getInstance().log("created new dir");

        navfilename=newDir+File.separator+tmp.getName();

        }

  }catch(IOException e){e.printStackTrace();}
 }
  else LogManager.getInstance().log("not using local directory");

  LogManager.getInstance().log("returning "+navfilename);

  return navfilename;
 }


static private FileNameManager sm_instance;
static public FileNameManager getInstance(){
         if ( sm_instance == null ){
         sm_instance = new FileNameManager();
         PrefManager.getInstance().register("Files",sm_instance);
	 }
         return sm_instance;
     }











}