package gvdecoder.utilities;

import java.awt.*;
import java.io.*;
import java.awt.image.*;
import gvdecoder.Matrix;


//Based on BMPFile class from http://www.javaworld.com/javaworld/javatips/jw-javatip60-p2.html
//Based on ImageJ Plugin
public class BMP_Writer {
    //--- Private constants
    private final static int BITMAPFILEHEADER_SIZE = 14;
    private final static int BITMAPINFOHEADER_SIZE = 40;
    //--- Private variable declaration
    //--- Bitmap file header
    private byte bitmapFileHeader [] = new byte [14];
    private byte bfType [] =  {(byte)'B', (byte)'M'};
    private int bfSize = 0;
    private int bfReserved1 = 0;
    private int bfReserved2 = 0;
    private int bfOffBits = BITMAPFILEHEADER_SIZE + BITMAPINFOHEADER_SIZE;
    //--- Bitmap info header
    private byte bitmapInfoHeader [] = new byte [40];
    private int biSize = BITMAPINFOHEADER_SIZE;
    private int biWidth = 0;
    private int biHeight = 0;
    private int biPlanes = 1;
    private int biBitCount = 8;
    private int biCompression = 0;
    private int biSizeImage = 0x030000;
    private int biXPelsPerMeter = 0x0;
    private int biYPelsPerMeter = 0x0;
    private int biClrUsed = 256;
    private int biClrImportant = 0;
    //--- Bitmap raw data
    private int intBitmap [];
    private byte byteBitmap [];
    //--- File section
    private FileOutputStream fo;
    private BufferedOutputStream bfo;






    /*
    *	The saveMethod is the main method of the process. This method
    *	will call the convertImage method to convert the memory image to
    *	a byte array; method writeBitmapFileHeader creates and writes
    *	the bitmap file header; writeBitmapInfoHeader creates the
    *	information header; and writeBitmap writes the image.
    *
    */
    private void save (Matrix ma, String parFilename) throws Exception {
        fo = new FileOutputStream (parFilename);
        bfo = new BufferedOutputStream(fo);
        int pad = (4 - ((ma.xdim) % 4)) * ma.ydim;
        biSizeImage = ((ma.xdim * ma.ydim)) + pad;
        bfSize = biSizeImage + BITMAPFILEHEADER_SIZE + BITMAPINFOHEADER_SIZE;
        biWidth = ma.xdim;
        biHeight = ma.ydim;        //calcImage (ma.dat.arr, ma.x_dim, ma.y_dim);
        writeBitmapFileHeader ();
        writeBitmapInfoHeader ();
        writeBitmapPalette ();
        writeBitmap (ma);
        bfo.close();
        fo.close ();
    }

    private void writeBitmapPalette() throws Exception {

        for(int i = 0;i<256;i++) {
            bfo.write((byte)i);
            bfo.write((byte)i);
            bfo.write((byte)i);
            bfo.write(0x00);
        }
     }


    /*
    * writeBitmap converts the image returned from the pixel grabber to
    * the format required. Remember: scan lines are inverted in
    * a bitmap file!
    *
    * Each scan line must be padded to an even 4-byte boundary.
    */
    private void writeBitmap (Matrix ma) throws Exception {
         int value;
        int i;
        int pad;


       pad = 4 - ((biWidth) % 4);
       if (pad == 4)  pad = 0;
       for(int row = biHeight; row>0; row--) {
           for( int col = 0; col<biWidth; col++) {

                       bfo.write((byte)ma.dat.arr[(row-1)*biWidth + col ]);

            }
            for (i = 1; i <= pad; i++)
                bfo.write (0x00);
        }
     }


    /*
    * writeBitmapFileHeader writes the bitmap file header to the file.
    *
    */
    private void writeBitmapFileHeader() throws Exception {
        fo.write (bfType);
        fo.write (intToDWord (bfSize));
        fo.write (intToWord (bfReserved1));
        fo.write (intToWord (bfReserved2));
        fo.write (intToDWord (bfOffBits));


    }

    /*
    *
    * writeBitmapInfoHeader writes the bitmap information header
    * to the file.
    *
    */
    private void writeBitmapInfoHeader () throws Exception {
        fo.write (intToDWord (biSize));
        fo.write (intToDWord (biWidth));
        fo.write (intToDWord (biHeight));
        fo.write (intToWord (biPlanes));
        fo.write (intToWord (biBitCount));
        fo.write (intToDWord (biCompression));
        fo.write (intToDWord (biSizeImage));
        fo.write (intToDWord (biXPelsPerMeter));
        fo.write (intToDWord (biYPelsPerMeter));
        fo.write (intToDWord (biClrUsed));
        fo.write (intToDWord (biClrImportant));


    }

    /*
    *
    * intToWord converts an int to a word, where the return
    * value is stored in a 2-byte array.
    *
    */
    private byte [] intToWord (int parValue) {
        byte retValue [] = new byte [2];
        retValue [0] = (byte) (parValue & 0x00FF);
        retValue [1] = (byte) ((parValue >>	8) & 0x00FF);
        return (retValue);
    }

    /*
    *
    * intToDWord converts an int to a double word, where the return
    * value is stored in a 4-byte array.
    *
    */
    private byte [] intToDWord (int parValue) {
        byte retValue [] = new byte [4];
        retValue [0] = (byte) (parValue & 0x00FF);
        retValue [1] = (byte) ((parValue >>	8) & 0x000000FF);
        retValue [2] = (byte) ((parValue >>	16) & 0x000000FF);
        retValue [3] = (byte) ((parValue >>	24) & 0x000000FF);
        return (retValue);
    }
}

