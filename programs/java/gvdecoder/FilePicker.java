package gvdecoder;
import java.io.*;
import javax.swing.JFileChooser;
import java.awt.FileDialog;

public class FilePicker{

  public final static int DataDir=1;
  public final static int ROIDir=2;
  public final static int UserDir=4;
  public final static int ScriptDir=8;
  public final static int TempImagesDir=16;
  public final static int PermImagesDir=32;
  public final static int NavigatorDir=64;


  public FileDialog fd;
  public JFileChooser fc;;

  public GView gv;

  public boolean USENATIVEFILEDIALOG=false;

  public boolean fullscreen=false;

  public static FilePicker ref;

  public static synchronized FilePicker getFilePicker(){
    if (ref==null) ref=new FilePicker();
    return ref;
  }

  private FilePicker(){
   USENATIVEFILEDIALOG=PropertyHelper.getPropertyHelper().getBooleanProperty("UseNativeFileDialog",false);
   gv=GView.getGView();
   fd=new FileDialog(gv.mainWindow);
   fc=new JFileChooser();
 }

  public void switchToNativeDialog(){
   USENATIVEFILEDIALOG=true;
   PropertyHelper.getPropertyHelper().setBooleanProperty("UseNativeFileDialog",true);
   PropertyHelper.getPropertyHelper().saveCFG();
  }

  public void switchToSwingDialog(){
   USENATIVEFILEDIALOG=false;
   PropertyHelper.getPropertyHelper().setBooleanProperty("UseNativeFileDialog",false);
   PropertyHelper.getPropertyHelper().saveCFG();
  }


  String last_directory="";
  String last_file="";
  String last_path="";


  public String getDirectory(){return last_directory;}
  public String getName(){return last_file;}
  public String getAbsolutePath(){return last_path;}
  public String getFileType(){
	  if (last_file==null) return null;
	  else{
		  int i=last_file.lastIndexOf('.');
		  if (i==-1) return null;
		  else return last_file.substring(i+1);
	  }
  }

  public boolean approved=false;

  public String saveFile(){
	  fc.setDialogType(JFileChooser.SAVE_DIALOG);
	  String filename=openFile(null);
	  fc.setDialogType(JFileChooser.OPEN_DIALOG);
	  return filename;
  }

  public String saveFile(int dirtype){
	  fc.setDialogType(JFileChooser.SAVE_DIALOG);
	  String filename=openFile(dirtype);
	  fc.setDialogType(JFileChooser.OPEN_DIALOG);
	  return filename;
  }

  public String saveFile(String path){
	  fc.setDialogType(JFileChooser.SAVE_DIALOG);
	  String filename=openFile(path);
	  fc.setDialogType(JFileChooser.OPEN_DIALOG);
	  return filename;
  }


 public String saveDirectory(){
	  fc.setDialogType(JFileChooser.SAVE_DIALOG);
	  String filename=openDirectory(null);
	  fc.setDialogType(JFileChooser.OPEN_DIALOG);
	  return filename;
  }

  public String saveDirectory(int dirtype){
	  fc.setDialogType(JFileChooser.SAVE_DIALOG);
	  String filename=openDirectory(dirtype);
	  fc.setDialogType(JFileChooser.OPEN_DIALOG);
	  return filename;
  }

  public String saveDirectory(String path){
	  fc.setDialogType(JFileChooser.SAVE_DIALOG);
	  String filename=openDirectory(path);
	  fc.setDialogType(JFileChooser.OPEN_DIALOG);
	  return filename;
  }



  /*special case when dialog called from script and purpose indeterminate*/
  public String chooseFile(String path){
	 if (USENATIVEFILEDIALOG){
		fd.setMode(FileDialog.LOAD);
		if (path!=null) fd.setDirectory(path);
		fd.show();
		last_file=fd.getFile();
		if (last_file==null){
			approved=false;
			return null;
		}
		last_directory=fd.getDirectory();
		last_path=last_directory+last_file;
		return fd.getDirectory()+fd.getFile();
		}else{
	// fc.setDefaultLookAndFeelDecorated(fullscreen);
	 if (path!=null) fc.setCurrentDirectory(new File(path));
	 fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
	 int returnVal=fc.showDialog(gv.mainWindow,"choose");
	  if (returnVal==JFileChooser.APPROVE_OPTION){
		  File file=fc.getSelectedFile();
		  last_path=file.getAbsolutePath();
		  last_directory=file.getParent()+File.separator;
		  last_file=file.getName();
		  approved=true;
		  return file.getAbsolutePath();
	  }
	  else{
		  approved=false;
		  return null;
	  }
     }
}

  public String openFile(){
	  return openFile(null);
  }


  public String openDirectory(){
	  return openDirectory(null);
  }

  public int lastdirtype;
  public String openFile(int dirtype){
	  String returnstring="";
	  if (dirtype==lastdirtype) returnstring=openFile(null);
	  else{
	  String gotodir=null;
	  lastdirtype=dirtype;
	  returnstring=openFile(gotodir(dirtype));
      }
      return returnstring;
   }

  public String openDirectory(int dirtype){
	  String returnstring="";
	  if (dirtype==lastdirtype) returnstring=openDirectory(null);
	  else{
	  String gotodir=null;
	  lastdirtype=dirtype;
	  returnstring=openDirectory(gotodir(dirtype));
      }
      return returnstring;
   }


 private String gotodir(int dirtype){
	String gotodir=null;
	PropertyHelper ph=PropertyHelper.getPropertyHelper();
	  switch(dirtype){
		case ROIDir:       gotodir=ph.getProperty("ROI dir"); break;
		case DataDir:      gotodir=ph.getProperty("DataFiles dir"); break;
		case ScriptDir:    gotodir=ph.getProperty("Script dir");break;
		case TempImagesDir:gotodir=ph.getProperty("TempImages dir");break;
		case PermImagesDir:gotodir=ph.getProperty("SavedImages dir");break;
		case NavigatorDir: gotodir=ph.getProperty("Navigator dir");break;
		case UserDir:      gotodir=ph.getProperty("User dir");break;
	   }
     return gotodir;

 }


  public String openFile(String path){
	 if (USENATIVEFILEDIALOG){
		fd.setMode(FileDialog.LOAD);
		if (path!=null) fd.setDirectory(path);
		fd.show();
		last_file=fd.getFile();
		if (last_file==null){
			approved=false;
			return null;
		}
		last_directory=fd.getDirectory();
		last_path=last_directory+last_file;
		approved=true;
		return fd.getDirectory()+fd.getFile();
		}else{
	if (gv.isFullScreen) gv.toggleFullScreen();
	 if (path!=null) fc.setCurrentDirectory(new File(path));
	 fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
	 int returnVal=fc.showDialog(gv.mainWindow,null);
	  if (returnVal==JFileChooser.APPROVE_OPTION){
		  File file=fc.getSelectedFile();
		  last_path=file.getAbsolutePath();
		  last_directory=file.getParent()+File.separator;
		  last_file=file.getName();
		  approved=true;
		  return file.getAbsolutePath();
	  }
	  else{
		  approved=false;
		  return null;
	  }
     }

  }

  public String openDirectory(String path){
     if (USENATIVEFILEDIALOG){
		fd.setMode(FileDialog.LOAD);
		if (path!=null) fd.setDirectory(path);
		fd.show();
		if (fd.getDirectory()!=null){
		  approved=true;
		  last_directory=fd.getDirectory();
		  last_file=null;
		  last_path=last_directory;
		  return last_directory;
	    }else{
		 approved=false;
		 return null;
		}

	 }else{

	 if (path!=null) fc.setCurrentDirectory(new File(path));
	 fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
  	 int returnVal=fc.showDialog(gv.mainWindow,null);
  	  if (returnVal==JFileChooser.APPROVE_OPTION){
  		  File file=fc.getSelectedFile();
  		  last_path=file.getAbsolutePath();
		  last_directory=file.getParent()+File.separator;
		  last_file=null;
		  approved=true;
  		  return file.getAbsolutePath();
  	  }
  	  else{
		  approved=false;
		  return null;
	  }
  }
}




}


