import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.io.RandomAccessFile;

public class GenericReader implements ImageDecoderAdapter {
 public abstract int UpdateImageArray(int[] arr,int xdim,int ydim, int instance);
 public abstract int OpenImageFile(String filename);
 public abstract int CloseImageFile(int instance);
 public int FilterOperation(int OperationCode, int startx, int endx, int instance){return 0;}
 public String ReturnSupportedFilters(){return "";}
 public int ReturnFrameNumber(){return 0;}
 public abstract int JumpToFrame(int framenum, int instance);
 public abstract int ReturnXYBandsFrames(int[] dat, int instance);

public class BufferMap
{
	public static void main (String [] argv)
		throws Exception
	{
		if (argv.length < 1) {
			System.out.println ("Need a file name");
			return;
		}

		RandomAccessFile raf = new RandomAccessFile (argv [0], "r");
		FileChannel fc = raf.getChannel();
		MappedByteBuffer buffer = fc.map (
			FileChannel.MapMode.READ_ONLY, 0, fc.size());

		buffer.clear();
		buffer.flip();

		System.out.println ("hasArray=" + buffer.hasArray());
		System.out.println (buffer.toString());

		System.out.flush();
	}
}
