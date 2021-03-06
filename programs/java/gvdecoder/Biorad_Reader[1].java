import ij.*;
import ij.io.*;
import ij.util.Tools;
import ij.process.*;
import ij.plugin.*;
import java.io.*;
import java.util.*;
import ij.measure.*;


/** Imports a Z series(image stack) from a Biorad MRC 600
	confocal microscope.  The width, height and number of images are
	extracted from the first 3 16-bit word in the 76 byte header.
	Use Image/Show Info to display the header information.
*/

public class Biorad_Reader extends ImagePlus implements PlugIn {

	private final int NOTE_SIZE = 96;
	private BufferedInputStream f;
	private String directory;
	private String fileName;
	private String notes = "";

	public void run(String arg) {
		OpenDialog od = new OpenDialog("Open Biorad...", arg);
		directory = od.getDirectory();
		fileName = od.getFileName();
		if (fileName==null)
			return;
		IJ.showStatus("Opening: " + directory + fileName);
		FileInfo fi = null;
		try {fi = getHeaderInfo();}
		catch (Exception e) {
			IJ.showStatus("");
			IJ.showMessage("BioradReader", ""+e);
			return;
		}
		if (fi!=null) {
			FileOpener fo = new FileOpener(fi);
			ImagePlus imp = fo.open(false);
			if (imp==null)
				return;
			if (imp.getStackSize()>1)
				setStack(fileName, imp.getStack());
			else
				setProcessor(fileName, imp.getProcessor());
			imp.setCalibration(getBioRadCalibration(fi.width, fi.height, fi.nImages) );
			if (!notes.equals(""))
				setProperty("Info", notes);
			copyScale(imp);
			if (arg.equals("")) show();
		}
	}

	int getByte() throws IOException {
		int b = f.read();
		if (b ==-1) throw new IOException("unexpected EOF");
		return b;
	}

	int getShort() throws IOException {
		int b0 = getByte();
		int b1 = getByte();
		return ((b1 << 8) + b0);
	}

	FileInfo getHeaderInfo() throws IOException {
		f = new BufferedInputStream(new FileInputStream(directory+fileName));
		FileInfo fi = new FileInfo();
		fi.fileFormat = fi.RAW;
		fi.fileName = fileName;
		fi.directory = directory;
  		fi.width = getShort();
  		fi.height = getShort();
  		fi.nImages = getShort();
  		fi.offset = 76;
		if (fi.width<128||fi.width>2048||fi.height<128||fi.height>2048||fi.nImages<1||fi.nImages>4000)
    		throw new IOException("This does not seem to be a Biorad Z Series");
    		f.close();
  		return fi;
	}

	/** Extracts the calibration info from the ASCII "notes" at the end of Biorad pic files. */
	Calibration getBioRadCalibration(int width, int height, int nImages)  {
		Calibration BioRadCal = new Calibration();
		long NoteFlag;
		int NoteType, Offset;
		String NoteContent = new String();
		byte[] TempByte = new byte[NOTE_SIZE];
		double ScaleX, ScaleY, ScaleZ;

   		int maxNotes = nImages*2+50;
		FileInfo fi = new FileInfo();
		fi.fileFormat = fi.RAW;
		fi.fileName = fileName;
		fi.directory = directory;
  		fi.width = maxNotes*NOTE_SIZE;
  		fi.height = 1;
  		fi.offset = 76+height*width*nImages;
		ImagePlus imp = new FileOpener(fi).open(false);
		if (imp==null)
			return BioRadCal;
		ImageProcessor ip = imp.getProcessor();

		Offset = 0;
		// Do ... While : cycle through notes until you reach the last note (indicated by bytes 2-5)
		// For each note, different from 'live note', see if it contains axis calibration data.
		// of so, extract the necessary values.
		do {
			// Decode bytes 2-5 (32-bit integer), if = 0, last note
			NoteFlag = ip.getPixel(Offset+2,0) + (ip.getPixel(Offset+3,0) << 8)+(ip.getPixel(Offset+4,0) << 16)+(ip.getPixel(Offset+5,0) << 24);
			// Decode bytes 10-11 (16-bit integer), contains type of note
			NoteType = ip.getPixel(Offset+10,0) + (ip.getPixel(Offset+11,0) << 8);
			// store bytes 16-95 in a byte array, stopping at first illegal character....
			int byteCount = 0;
			for (int i=0; i<NOTE_SIZE; i++) {
				byte ch = (byte)ip.getPixel(Offset+16+i,0);
				byteCount++;
				if (ch<32 || ch>127) {
					TempByte[i]  = 32;
					break;
				}
				TempByte[i] = (ch>=32 && ch<=126)?ch:32;
			}
			String Note = new String(TempByte, 0, byteCount);
			notes += Note + "\n";	// save note for Image/Show Info command

			// only analyze notes != 1, so skip all the notes that say 'Live ...', they don't contain calibration info
			if (NoteType!=1) {
				NoteContent = getField(Note,1);
				// See if first field contains keyword "AXIS_2" --> X-axis calibration (don't know why Biorad calls it '2'.
				if (NoteContent.indexOf("AXIS_2") >= 0 ) {
					//IJ.showMessage(Note);
					//IJ.showMessage(getField(Note, 4));
					ScaleX = s2d(getField(Note, 4));
					BioRadCal.pixelWidth = ScaleX;
					// fifth field contains units (mostly microns)
					BioRadCal.setUnit(getField(Note, 5));
				} else if ( NoteContent.indexOf("AXIS_3") >= 0 )  { // contains Y-axis calibration
					//IJ.showMessage(Note);
					//IJ.showMessage(getField(Note, 4));
					ScaleY = s2d( getField(Note, 4));
					BioRadCal.pixelHeight = ScaleY;
					BioRadCal.setUnit(getField(Note, 5));
				} else if ( NoteContent.indexOf("AXIS_4") >= 0 )  { // contains Z-axis calibration
					//IJ.showMessage(Note);
					//IJ.showMessage(getField(Note, 4));
					ScaleZ = s2d( getField(Note, 4));
					BioRadCal.pixelDepth = ScaleZ;
					/** ImageJ does not contain seperate units for the Z-direction. This could be a usefull extension
					 because for a stack you can have 'space' or 'time' as the extra dimension. In the time-case, the
					 fifth field of the Z-scaling contains the time units */
				}

			}
			Offset += NOTE_SIZE; // Jump to next note
		} while ( NoteFlag != 0 ); // stop if this was the last note

		return  BioRadCal; // return the filled biorad calibration
	}

	/* Extracts a certain field from a string. A space is considered as the field delimiter. */
	String getField(String str, int fieldIndex) {
		char delimiter = ' ';
		int startIndex=0, endIndex;
		for (int i=1; i<fieldIndex; i++)
			startIndex = str.indexOf(delimiter, startIndex+1);
		endIndex = str.indexOf(delimiter, startIndex+1);
		if (startIndex>=0 && endIndex>=0)
			return str.substring(startIndex, endIndex);
		else
			return "";
	}

	// Converts a string to a double. Returns 1.0 if the string does not contain a valid number. */
	double s2d(String s) {
		Double d = null;
		try {d = new Double(s);}
		catch (NumberFormatException e) {}
		return d!=null?d.doubleValue():1.0;
	}

}

/*
Bio-Rad(TM) .PIC Image File Information
(taken from: "Introductory Edited Version 1.0", issue 1/12/93.)
(Location of Image Calibration Parameters in Comos 6.03 and MPL .PIC files)

The general structure of Bio-Rad .PIC files is as follows:

    HEADER (76 bytes)
    Image data (#1)
    .
    .
    Image data (#npic)
    NOTE (#1)
    .                       ; NOTES are optional.
    .
    NOTE (#notes)
    RGB LUT (color Look Up Table)


Header Information:

The header of Bio-Rad .PIC files is fixed in size, and is 76 bytes.

------------------------------------------------------------------------------
'C' Definition              byte    size    Information
                                   (bytes)
------------------------------------------------------------------------------
int nx, ny;                 0       2*2     image width and height in pixels
int npic;                   4       2       number of images in file
int ramp1_min, ramp1_max;   6       2*2     LUT1 ramp min. and max.
NOTE *notes;                10      4       no notes=0; has notes=non zero
BOOL byte_format;           14      2       bytes=TRUE(1); words=FALSE(0)
int n;                      16      2       image number within file
char name[32];              18      32      file name
int merged;                 50      2       merged format
unsigned color1;            52      2       LUT1 color status
unsigned file_id;           54      2       valid .PIC file=12345
int ramp2_min, ramp2_max;   56      2*2     LUT2 ramp min. and max.
unsigned color2;            60      2       LUT2 color status
BOOL edited;                62      2       image has been edited=TRUE(1)
int _lens;                  64      2       Integer part of lens magnification
float mag_factor;           66      4       4 byte real mag. factor (old ver.)
unsigned dummy[3];          70      6       NOT USED (old ver.=real lens mag.)
------------------------------------------------------------------------------

Additional information about the HEADER structure:

Bytes   Description     Details
------------------------------------------------------------------------------
0-9     nx, ny, npic, ramp1_min, ramp1_max; (all are 2-byte integers)

10-13   notes           NOTES are present in the file, otherwise there are
                        none.  NOTES follow immediately after image data at
                        the end of the file.  Each note os 96 bytes long.

14-15   byte_format     Read as a 2 byte integer.  If this is set to 1, then
                        each pixel is 8-bits; otherwise pixels are 16-bits.

16-17   n               Only used in COMOS/SOM when the file is loaded into
                        memory.

18-49   name            The name of the file (without path); zero terminated.

50-51   merged          see Note 1.

52-53   colour1

54-55   file_id         Read as a 2 byte integer.  Aways set to 12345.
                        Just a check that the file is in Bio-Rad .PIC format.

56-59   ramp2_min/max   Read as 2 byte integers.

60-61   color2          Read as a 2 byte integer.

62-63   edited          Not used in disk files.

64-65   int_lens        Read as a 2 byte integer.
                        Integer part of the objective lens used.

66-69   mag_factor      Read as a 4-byte real.

        mag. factor=(float)(dispbox.dy*2)/(float)(512.0*scandata.ly)

        where:  dispbox.dy = the width of the image.
                scandata.ly = the width of the scan region.

        the pixel size in microns can be calculated as follows:

        pixel size = scale_factor/lens/mag_factor

        where:  lens = the objective lens used as a floating pt. number
                scale_factor = the scaling number setup for the system
                               on which the image was collected.

70-75   dummy[3]    Last 6 bytes not used in current version of disk file
                    format. (older versions stored a 4 byte real lens mag
                    here.)
------------------------------------------------------------------------------

Note 1 : Values stored in bytes 50-51 :

0        : Merge off
1        : 4-bit merge
2        : Alternate 8-bit merge
3        : Alternate columns merge
4        : Alternate rows merge
5        : Maximum pixel intensity merge
6        : 256 colour optimised merge with RGB LUT saved at the end
           of each merge.
7        : As 6 except that RGB LUT saved after all the notes.


Information about NOTE structure and the RGB LUT are not included in this
file.  Please see the Bio-Rad manual for more information.


==============================================================================

Info added by Geert Meesen from MRC-600 and MRC-1024 Manuals.

-------------------------------------------------------------

Note Structure :

Bytes   Description     Details
------------------------------------------------------------------------------
0-1     Display level of this note

2-5     =0 if this is the last note, else there is another note (32 bit integer)

10-11   Note type := 1 for live collection note,
                  := 2 for note including file name,
                  := 3 if note for multiplier file,
                  := 4, 5, etc.,; additional descriptive notes

16-95   Text of note (80 bytes)


=============================================================================

Info added by Geert Meesen from personal experiments.

------------------------------------------------------------

- Until now I only have experience with 8-bit images from the MRC-1024 confocal microscope.
The newer microscopes (Radiance 2000, for example) are capable of generating 16 bit images,
I think. I have access to such a microscope and will try to find out later. For now it
should be possible to look at the byte-word flag in the header.

- I have experience with two types of images :
--- One slice in the Z-direction, 3 channels of recording. This type is stored as a three-slice image
    with the 3 channels in consecutive layers. (Single-Slice)
--- Different Z slices with only one channel. (Z-stack)

- The header should contain some info about the pixel-size, but until now I was not really
able to interpret this info. It's easier to extract the info from the notes at the end.
You can find 3 notes saying something like (from AUPCE.NOT, a Z-stack file)

AXIS_2 001 0.000000e+00 2.999667e-01 microns
AXIS_3 001 0.000000e+00 2.999667e-01 microns
AXIS_4 001 0.000000e+00 1.000000e+00 microns
AXIS_9 011 0.000000e+00 1.000000e+00 RGB channel

These lines give the pixelsize for the X (axis_2), Y (axis_3) and Z (axis_4) axis in the units mentioned. I don't
know if this unit is always 'microns'.

For a Single-Slice images you get ( from AB003A.NOT, a Single-Slice image) :

AXIS_2 001 0.000000e+00 1.799800e+00 microns
AXIS_3 001 0.000000e+00 1.799800e+00 microns
AXIS_4 011 0.000000e+00 1.000000e+00 RGB channel

It seems that AXIS_4 is used for indicating an RGB channel image.
*/





