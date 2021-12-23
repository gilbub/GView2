package gvdecoder;

import java.awt.*;
import java.awt.event.*;
import java.awt.datatransfer.*;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.Date;
import java.io.*;
import java.util.GregorianCalendar;
import javax.media.jai.*;
import java.util.Scanner;




/**
 AnalysisHelper is a singleton class.
  Examples

  AnalysisHelper ah=AnalysisHelper.getAnalysisHelper()
  ah.toClipboard(bi); // saves to system clipboard
  ah.saveImage(bi) //saves to a unique named file

 **/

public class AnalysisHelper implements Transferable, ActionListener {

public Toolkit toolkit;// = Toolkit.getDefaultToolkit();
public  Clipboard clipboard;// = toolkit.getSystemClipboard();
public String freemindprefix="GViewFiles/SavedImages/";
public BufferedImage image = null;
public String tempFilePath;
public String userConfigurationFilePath;
public String permFilePath;
public String tempFileType="jpeg";
public String permFileType="jpeg";
public String prefix="a";
public int tempFileIndex;
public int permFileIndex;
public Date date;
public GregorianCalendar calendar;
public GView gv;
String ConfigFileName="."+File.separator+"gvdecoder"+File.separator+"properties.cfg";
public Properties prp;
public int index=10000;
public int SessionNumber=0;
private static AnalysisHelper ref;
public Viewer2 vw;
javax.swing.Timer timer;


public void Notify(String str){
	if (gv==null) gv=GView.getGView();
	if (gv.jv!=null){
		gv.jv.showNotifyPopup(str);
	}
	else System.out.println("notify="+str);

}


public void initialize_clipboard(){
	toolkit=Toolkit.getDefaultToolkit();
	clipboard=toolkit.getSystemClipboard();
	date=new java.util.Date();
	calendar=new GregorianCalendar();
	calendar.setTime(date);
}


public boolean writeTextFile(String filename, String contents){
	 String mypath=userConfigurationFilePath+File.separator+filename;
	 try{
	 PrintWriter out = new PrintWriter(mypath);
	 out.println(contents);
	 out.close();
   }catch(IOException e){e.printStackTrace(); return false;}
   return true;
}

public String readTextFile(String filename){
   String mypath=userConfigurationFilePath+File.separator+filename;
   String tmp=null;
   try{
   Scanner scanner = new Scanner(new File(mypath));
   scanner.useDelimiter("\\Z");
   tmp=scanner.next();
   scanner.close();
   }catch(FileNotFoundException e){e.printStackTrace(); return null;}
   return tmp;
}


public boolean writePropertyFile(String filename, Properties prop){
	String mypath=userConfigurationFilePath+File.separator+filename;
	try{
	PrintWriter pw=new PrintWriter(mypath);
	prop.store(pw,"property file created by AnalysisHelper");
    pw.close();
    }catch(Exception e){e.printStackTrace(); return false;}
    return true;
}

public Properties readPropertyFile(String filename){
	String mypath=userConfigurationFilePath+File.separator+filename;
	try{
	FileReader fr=new FileReader(mypath);
	Properties prop=new Properties();
	prop.load(fr);
	fr.close();
	return prop;
	}catch(Exception e){e.printStackTrace();return null;}

}


 public void actionPerformed(ActionEvent e) {
        //Advance the animation frame.

		vw.frameNumber++;
		vw.im.UpdateImageArray(vw.datArray,vw.X_dim,vw.Y_dim,vw.instance);
		vw.jp.ARRAYUPDATED=true;
        vw.rescale();
		vw.jp.set(vw.frameNumber);
		if (vw.nav!=null) vw.nav.setFrame(vw.frameNumber);
		//vw.NotifyFrameListeners(frameNumber);
		//System.out.print(frameNumber+" ");
	  }


public void initialize_properties(){
	PropertyHelper ph=PropertyHelper.getPropertyHelper();
	tempFilePath=ph.getStringProperty("TempImages dir",".");
	permFilePath=ph.getStringProperty("SavedImages dir",".");
	SessionNumber=ph.getIntProperty("SessionNumber",0);
	userConfigurationFilePath=ph.getStringProperty("User dir",".");
	}


public static synchronized AnalysisHelper getAnalysisHelper(){
      if (ref == null)
          ref = new AnalysisHelper();
      return ref;
    }


//utility method
/*
private AnalysisHelper(BufferedImage image){
	this.image=image;
	toolkit=Toolkit.getDefaultToolkit();
	clipboard=toolkit.getSystemClipboard();
	clipboard.setContents(this,null);
}
*/

private AnalysisHelper(){

	initialize_clipboard();
	initialize_properties();
}

public String create_unique_directory(String originalname){
	//replace dots with underscores in name
	String res="";
	String name=null;
	boolean success=false;
	try{
	int dot=originalname.lastIndexOf('.');
	if (dot>0){
	  name=originalname.substring(0,dot);
    }else name=originalname;

	//determine if the directory called name exists in savedimages directory
	int index=0;
	String mypath=tempFilePath+File.separator+name;
	File myfile=new File(mypath+"_"+index);
	while (myfile.exists()){
		index++;
		myfile=new File(mypath+"_"+index);
	}
	success=(new File(mypath+"_"+index)).mkdir();
	res=mypath+"_"+index;
    }catch(Exception e){e.printStackTrace();}

	if (success) return res;
	return null;
}

/**
 Generates a unique image name in tempFilePath. If the image is a series and postfix is
 a bmp, jpg, tif, gif, or png file, a unique directory is created, which must be erased if not
 used by the calling function.Returns a String array with directory and filename in it.
 Usage example create_unique_image_name("dat1.spe","jpg",true)
**/
public String[] create_unique_image_name(String inputtedname, String postfix, boolean series_requested){
String[] imagepostfixes={"jpg","gif","bmp","png","tif"};
String[] moviepostfixes={"mov","avi","swf"};
boolean series_appropriate=false;
String directory=null;
String filename=null;
String tmp=inputtedname.replaceAll("\\\\","_");
String originalname=tmp.replaceAll(":","_");
if (series_requested){
	for (int i=0;i<imagepostfixes.length;i++){
		if (postfix.equalsIgnoreCase(imagepostfixes[i])){
			series_appropriate=true;
			break;
		}
	}
 }
 if (series_appropriate)
  directory=create_unique_directory(originalname);
 else{
  directory=tempFilePath;
  //strip off postfix
  String base=null;
  int dot=originalname.lastIndexOf('.');
  if (dot>0){
	base=originalname.substring(0,dot);
    }else base=originalname;
  int index=0;
  try{
  String mypath=tempFilePath+File.separator+base;
  File myfile=new File(mypath+"_"+index+"."+postfix);
	while (myfile.exists()){
		index++;
		myfile=new File(mypath+"_"+index+"."+postfix);
	}
  }catch(Exception e){return null;}
   filename=base+"_"+index+"."+postfix;
}
String [] res=new String[2];
res[0]=directory;
res[1]=filename;
return res;
}

public String last_name="";
public String unique_name(String filetype){
  String ft;
  index++;
  if (filetype=="jpeg") ft="jpg";
  else if (filetype=="tiff") ft="tif";
  else ft=filetype;
  last_name=prefix+calendar.get(calendar.YEAR)+"_"+calendar.get(calendar.DAY_OF_YEAR)+"_"+SessionNumber+"_"+index+"."+ft;
  return permFilePath+File.separator+last_name;
  }


public void saveMovThread(Viewer2 vw, String filename , int start, int end, int fps, int codec, int quality, boolean showextras, StatusMonitor monitor){
  System.out.println("in analysishelper, savemovthread");
  Thread thread=new Thread(new mp4Encoder(vw,filename,start,end,fps,showextras,monitor));
  thread.setDaemon(true);
  thread.start();
}

public String unique_html_name(String basename, boolean addpath){
  index++;
  last_name=basename+"_"+calendar.get(calendar.YEAR)+"_"+calendar.get(calendar.DAY_OF_YEAR)+"_"+SessionNumber+"_"+index+".html";
  if (addpath) return userConfigurationFilePath+File.separator+last_name;
  else return last_name;
  }

/*

public void saveMovThread(Viewer2 vw, String filename , int start, int end, int fps, int codec, int quality, boolean showextras, StatusMonitor monitor){
	try{
		this.getClass().getClassLoader().loadClass("quicktime.QTSession");
	}catch(ClassNotFoundException e){
		javax.swing.JOptionPane.showInternalMessageDialog(vw,"QuickTime (QTJava.zip) must be installed before saving .mov files.\n\n"+
		                                                     "Please go to:\n"+
		                                                     "developer.apple.com/quicktime/qtjava/installation.html\n"+
		                                                     "and follow the directions (but if the 'custom' option does\n"+
		                                                     "not appear, its still OK)\n"+
		                                                     "Please note, if you use more than one jre, you will have\n"+
		                                                     "to make sure QTJava.zip is in your JAVAHOME/lib/ext directory.\n\n"+
	 	                                                     "(the apple web address has been pasted into your clipboard)");
	toClipboard("http://developer.apple.com/quicktime/qtjava/installation.html");
	return;
	}
	QuickTimeEncoder qte=QuickTimeEncoder.getQuickTimeEncoder();
	qte.initialize(vw,filename,start,end,fps,codec,quality,showextras,monitor);
	Thread thread=new Thread(qte);
	thread.setDaemon(true);
	try{
	thread.start();
    }catch(Exception e){
	   e.printStackTrace();
	   javax.swing.JOptionPane.showInternalMessageDialog(vw,"There may be a problem with your quicktime for java install,\n"+
	   														"or an issue writing the quicktime movie due to file errors.\n\n"+
	   														"File errors: check that the filename you chose is compatible\n"+
	   														"with writing a .mov file (ie not a directory).\n\n"+
		                                                    "For quicktime install errors: \n"+
		                                                    "Please double check that CLASSPATH and QTJAVA environment\n"+
		                                                    "variables exist and point to QTJava.zip. Then ensure that\n"+
		                                                    "the java version that runs GView is the same one refered\n"+
		                                                    "to in the CLASSPATH and QTJAVA env. variables. Then confirm\n"+
		                                                    "that the classpath variable in the java command (if present-see\n"+
		                                                    "gview.bat) is compatible with the env. variables.") ;

   }
}
*/

public void saveAVIThread(Viewer2 vw, String filename, int start, int end, int fps, boolean filtered, boolean showextras, StatusMonitor monitor){
 Thread thread=new Thread(new AVIEncoder(vw,filename,start,end,fps,filtered,showextras,monitor));
  thread.setDaemon(true);
  thread.start();
}

public void saveImageSeriesThread(Viewer2 vi, int start, int end, String postfix, String directory, boolean showextras, StatusMonitor monitor){
	Thread thread=new Thread(new SaveImageSeries(this,vi,start,end,postfix,directory,showextras, monitor));
	//SaveImageSeries si=new SaveImageSeries(this,vi,start,end,postfix,directory,progress);
    thread.setDaemon(true);
    thread.start();
}

StatusMonitor monitor=null;
public void saveImageSeries(Viewer2 vi, int start, int end, String postfix, String directory,boolean showextras, StatusMonitor monitor){
	String type=null;
	this.monitor=monitor;
	if (postfix.equalsIgnoreCase("bmp")) type="BMP";
	if (postfix.equalsIgnoreCase("gif")) type="GIF";
	if (postfix.equalsIgnoreCase("jpg")) type="JPEG";
	if (postfix.equalsIgnoreCase("png")) type="PNG";
	if (postfix.equalsIgnoreCase("tif")) type="TIFF";

	String filename=null;
	String filestring="";
	    for (int i=start;i<end;i++){
		   if (monitor.isCancelled()) break;
		   monitor.showProgress(((double)(i-start))/(end-start));
		   vi.SilentJumpToFrame(i,showextras);

		if (i<10) filestring="000";
		else
		if (i<100) filestring="00";
		else
	    if (i<1000) filestring="0";
		filename=directory+File.separator+"img"+filestring+i+"."+postfix;
		JAI.create("filestore", vi.jp.filtered, filename, type, null);
   }
	monitor.showProgress(1.0);

  }





public String copyImageData(String datfilename,String pltfilename,  String imagename){
 String suffix=imagename.substring(imagename.lastIndexOf(".")+1,imagename.length());

 String newpltfilename=unique_name("plt");
 index-=1;
 String newdatname=unique_name("dat");
 index-=1;
 String newimagename=unique_name(suffix);
 System.out.println("orig "+imagename+" new image name="+newimagename);
 System.out.println("orig "+datfilename+" new dat name="+newdatname);
 System.out.println("orig "+pltfilename+" new plt name="+newpltfilename);
 copy(imagename,newimagename);
 copy(pltfilename,newpltfilename);
 copy(datfilename,newdatname);
 return newdatname+","+newpltfilename+","+newimagename;
}

public String copyImageData(String imagename){
 String suffix=imagename.substring(imagename.lastIndexOf("."),imagename.length());
 String newimagename=unique_name(suffix);
 copy(imagename,newimagename);
 return newimagename;
}

public void copy(String srcname, String dstname){
    System.out.println("copying "+srcname+" to "+dstname);
	try{
        int len=0;
		File src=new File(srcname);
		File dst=new File(dstname);
		InputStream in=new FileInputStream(src);
		OutputStream out=new FileOutputStream(dst);
		byte[] buf=new byte[1024];
		while((len=in.read(buf))>0){ out.write(buf,0,len);}
		in.close();
		out.close();
	}catch (IOException e){e.printStackTrace();}
}



public void alertJythonImageSaved(String name){
	if (gv!=null){
	if (gv.jv!=null){
		gv.jv.interp.exec("imageSaved('"+name+"')");
	}
   }
}

public String saveImage(BufferedImage image){
	String name=unique_name(permFileType);
	ImageUtils.WriteImage(name,image,permFileType);
	alertJythonImageSaved(name);
	toClipboard("<html><img src=\""+freemindprefix+last_name+"\">");
    //<html><img src="GViewFiles/SavedImages/a2006_239_1008_10009.jpg">
	//toClipboard(name);
	return name;
}

public String saveImage(BufferedImage image, String imagetype){
	String name=unique_name(imagetype);
	ImageUtils.WriteImage(name,image,imagetype);
    alertJythonImageSaved(name);
	toClipboard("<html><img src=\""+freemindprefix+last_name+"\">");
	return name;
}

public void toClipboard(BufferedImage image){
	this.image=image;
	clipboard.setContents(this,null);
}

public void toClipboard(String str){
	StringSelection ss = new StringSelection(str);
	clipboard.setContents(ss, null);
}

public DataFlavor[] getTransferDataFlavors() {
return new DataFlavor[] {DataFlavor.imageFlavor };
}

public boolean isDataFlavorSupported(DataFlavor flavor) {
return DataFlavor.imageFlavor.equals(flavor);
}

public Object getTransferData(DataFlavor flavor)
throws UnsupportedFlavorException {
if (!isDataFlavorSupported(flavor)) {
throw new UnsupportedFlavorException(flavor);
}
 return image;

}
public static void main(String[] args){

	AnalysisHelper ah=new AnalysisHelper();
	System.out.println(ah.unique_name("jpeg"));
	System.out.println(ah.unique_name("jpeg"));
}


}

class SaveImageSeries implements Runnable{
	AnalysisHelper ah;
	Viewer2 vi;
	int start,end;
	String postfix;
	String directory;
	StatusMonitor monitor;
    boolean SHOWEXTRAS;

	SaveImageSeries(AnalysisHelper ah,Viewer2 vi,int start, int end, String postfix,String directory, boolean showextras, StatusMonitor monitor){
		this.ah=ah;
		this.vi=vi;
		this.start=start;
		this.end=end;
		this.postfix=postfix;
		this.directory=directory;
		this.monitor=monitor;
		this.SHOWEXTRAS=showextras;

	}
	public void run(){
		ah.saveImageSeries(vi,start,end,postfix,directory,SHOWEXTRAS,monitor);
	}


}