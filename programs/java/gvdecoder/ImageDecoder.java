package gvdecoder;
/**
	The ImageDecoder interface is used by any raw datafile decoder thats delivered
	by the ImageDecoderFactory. The image decoder updates an array of integers when
	given the x and y dimensions and a depth, where the depth is the number of consecutive
	images to return. The array must have dimensions equal to xdim*ydim.
	This organization is designed to allow relatively easy use of native methods where available.
    The method should return the present framenumber on success and '-1' on failure (eof, etc).

	The method OpenImageFile() opens a data file, and returns a instance number. The program should
	not rely on this method to return usable errors (again, a way of using native methods with less
	bother), rather the calling program should check the file is OK before passing it to the ImageDecoder.

	The method CloseImageFile(instance) closes a data file. The method should return 1 on success and -1 on failure.

    The method FilterOperation() is a flexible way of toggling various background, median (etc)
	filters without enforcing that each has to be supplied by the child method. A positive integer
	toggles the given filter 'on', and a negative toggles it 'off'. If the filter isn't supported
	the method returns a -1 else it returns a 1.
	Because its not transparent, this method is only supposed to be used for operations that are
	fast when performed by the native decoder implementations. Functional equivalents should be available
	in the java software.

	The method ReturnSupportedFilters() should return a comma delimted string of supported filters

	The method ReturnFrameNumber returns the present frame number, -1 if eof.

	The method JumpToFrame(int framenum) causes the file pointer to move to the chosen frame, returns the frame number
	on success, and -1 on failure.

    The method returnXYBandsFrames(int[] dat) is a method for obtaining information about the images being retreived.
	It returns 4 integers; the first is the width, second is the height, third is the number of bands (1 or 3) and the fourth is
	the number of frames. Any value thats unknown should read -1. The method returns 1 on success, -1 on failure.
**/

public interface ImageDecoder{
   public int UpdateImageArray(int[] arr,int xdim,int ydim, int instance);
   public int OpenImageFile(String filename);
   public int CloseImageFile(int instance);
   public int FilterOperation(int OperationCode, int startx, int endx, int instance);
   public String ReturnSupportedFilters();
   public int ReturnFrameNumber();
   public int JumpToFrame(int framenum, int instance);
   public int SumROIs(int[][] rois, String outfile, int startframe, int endframe,int instance);
   public int ReturnXYBandsFrames(int[] dat, int instance);
}