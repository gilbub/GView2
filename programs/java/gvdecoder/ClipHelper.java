import java.awt.*;
import java.awt.event.*;
import java.awt.datatransfer.*;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.Date;
import java.util.GregorianCalendar;


/*have a default directory for storing images permanently with unique file names.
  also allow the user to open up an analysis directory of their choice
 */

public class AnalysisHelper implements Transferable {

public Toolkit toolkit;// = Toolkit.getDefaultToolkit();
public  Clipboard clipboard;// = toolkit.getSystemClipboard();
public BufferedImage image = null;
public String tempFilePath;
public String permFilePath;
public String tempFileType="jpg";
public String permFileType="jpg";
public int tempFileIndex;
public int permFileIndex;
public Date date;
public GregorianCalendar calendar;
public GView gv;
String ConfigFileName="."+File.separator+"viewer.cfg";
public Properties prp;

public void initialize_clipboard(){
	toolkit=Toolkit.getDefaultToolkit();
	clipboard=toolkit.getSystemClipboard();
	date=java.util.Date();
	calendar=GregorianCalendar(date);
}

public void initialize_properties(){
	prp=new Properties();
			try{
			 FileInputStream istream=new FileInputStream(new File(filename));
			 prp.load(istream);
			 tempFilePath=prp.get("TempImages dir");
	         permFilePath=prp.get("SavedImages dir");
			}catch(Exception e){System.out.println("can't open configuration file...");}
	}

}

//utility method
public AnalysisHelper(BufferedImage image){
	this.image=image;
	toolkit=Toolkit.getDefaultToolkit();
	clipboard=toolkit.getSystemClipboard();
	clipboard.setContents(this,null);
}

public AnalysisHelper(){
	initialize_clipboard();

	initialize_properties();
}

public void toClipboard(BufferedImage image){
	this.image=image;
	clipboard.setContents(this,null);
}

public String unique_name(){
  return perFilePath+File.separator+prefix+calendar.get(calendar.YEAR)+"_"+calendar.get(calendar.DAY_OF_YEAR)+"_"+index+"."+permFileType;
}

public void saveAsImage(){
	BufferedImage image = new BufferedImage(
			   200,200,BufferedImage.TYPE_BYTE_INDEXED);

			Graphics2D myG = (Graphics2D)image.getGraphics();

			leftPlot.paintComponent(myG);

		ImageUtils.WriteImage("temp.bmp",image,"bmp");


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
}