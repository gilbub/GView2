package gvdecoder;

public TiffStackDecoder{

public ImagePlus openTiff(String directory, String name) {
		TiffDecoder td = new TiffDecoder(directory, name);
		if (IJ.debugMode) td.enableDebugging();
		FileInfo[] info=null;
		try {info = td.getTiffInfo();}
		catch (IOException e) {
			IJ.showMessage("TiffDecoder", e.getMessage());
			return null;
		}
		if (info==null)
			return null;
		return openTiff2(info);
	}
	
}