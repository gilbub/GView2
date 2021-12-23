package gvdecoder;
/**
	ImageDecoderFactory returns the appropriate image decoding object (that implements
	the ImageDecoder interface) for decoding image files. It can be extended to import
	other movie type files in the future.
	At the present time it tries to locate the native dll, if installed. If it fails,
	it delivers the slower java decoder.
	It defaults to trying to get SPE files.

**/

public class ImageDecoderFactory{

public static ImageDecoder getDecoder(String ImageType){
System.out.println("In ImageDecoderFactory with ImageType="+ImageType);
if (ImageType.equals("pda")) return new HamamatsuDecoder();
else
if (ImageType.equals("ompro")) return new OMPRODecoder();
else
if (ImageType.equals("pic")) return new BioRadPicDecoder();
else
if (ImageType.equals("log")) return new CWRUDecoder(ImageType);
else
if (ImageType.equals("red")) return new RedShirtDecoder();
else
if (ImageType.equals("red")) return new RedShirtDecoder();
else
if (ImageType.equals("bred")) return new BigRedShirtDecoder();
else
if (ImageType.equals("gv")) return new GViewFormatDecoder();
else
if (ImageType.equals("ca")) return new CADecoder();
else
if (ImageType.equals("cas")) return new CascadeDecoder();
else
if (ImageType.equals("spe")) return getDecoder();
else
if (ImageType.equals("avi")) {return new AVIDecoder();}
else
if (ImageType.equals("pro")) {return new ProsilicaDecoder();}
else
if (ImageType.equals("dat")) {return new NeoDecoder(ImageType);}
else
if (ImageType.equals("mtf")) {System.out.println("returning multitiffdecoder"); return new MultiTiffDecoder();}
else
if (ImageType.equals("newred")) return new NewRedShirtDecoder();

else {
 System.out.println("returning javaseriesdecoder");
 return new JavaSeriesDecoder(ImageType);
 }
}

public static ImageDecoder getDecoder(){
//by default, tries to get the native method, else delivers the java version
 try{return new NativeSPEDecoder();
  } catch(Exception e){System.out.println("WARNING... native method unavailable...");
  return new JavaSPEDecoder();
  }
 }


}