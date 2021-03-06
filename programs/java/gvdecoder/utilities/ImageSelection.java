package gvdecoder.utilities;
import java.awt.event.*;
import java.awt.*;
import java.awt.datatransfer.*;
import java.io.*;

public  class ImageSelection implements Transferable{


 private Image image;

 public ImageSelection(Image image){
	 this.image=image;
 }

 public DataFlavor[] getTransferDataFlavors(){
	 return new DataFlavor[]{DataFlavor.imageFlavor};
 }

 public boolean isDataFlavorSupported(DataFlavor flavor){
	 return DataFlavor.imageFlavor.equals(flavor);
 }

 public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException{
  if (!DataFlavor.imageFlavor.equals(flavor)){
	  throw new UnsupportedFlavorException(flavor);
  }
  return image;

 }
}