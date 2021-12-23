package gvdecoder;

class NativeSPEDecoder implements ImageDecoder{
    public native int UpdateImageArray(int[] arr, int xdim, int ydim, int instance);
	public native int JumpToFrame(int framenum, int instance);
    public native int OpenImageFile(String filename);
	public native int CloseImageFile(int instance);
	public native int FilterOperation(int Opp, int startx, int endx, int instance );
	public native String ReturnSupportedFilters();
	public native int ReturnFrameNumber();
	public native int SumROIs(int[][] rois, String outfile, int startframe, int endframe,int instance);
	public native int ReturnXYBandsFrames(int[] dat);
    static {
        System.loadLibrary("NativeSPEDecoder");
    }


}

