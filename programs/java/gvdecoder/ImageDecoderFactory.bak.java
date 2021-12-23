
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
if (ImageType.equals("ompro")) return new OMPRODecoder();
else
if (ImageType.equals("pic")) return new BioRadPicDecoder();
else
if (ImageType.equals("log")) return new CWRUDecoder(ImageType);
else
if (ImageType.equals("red")) return new RedShirtDecoder();
else
if (ImageType.equals("spe")) return getDecoder();
else {
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