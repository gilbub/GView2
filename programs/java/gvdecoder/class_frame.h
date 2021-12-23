/*
 * class_frame.h
 * from WinView - User's manual  pp.308-311
 *
 */

#define BYTE unsigned char     // 1 byte under Linux
#define WORD unsigned short    // 2 byte
#define DWORD unsigned long    // 4 byte

#define ONSETDATA 4100
#define LABELMAX 16

#include <stdio.h>
#include <GL/gl.h>

typedef struct stack_header
{
  /*                       Decimal byte offset and comments        */
  WORD dioden;          /* 0 num of physical pixels (X axis)       */
  short avgexp;         /* 2 number of accumulation per scan       */
  short exposure;       /* 4 exposure time (in miliseconds)        */
  WORD xDimDet;         /* 6 Detector x dimension of chip          */
  short mode;           /* 8 tming mode                            */
  float exp_sec;        /* 10 alternative exposure, in secs.       */
  short asyavg;         /* 14 number of asynchron averages         */
  short asyseq;         /* 16 number of asynchron sequential       */
  WORD yDimDet;         /* 18 y dimension of CCD or detector       */
  char date[10];        /* 20 date as MM/DD/YY                     */
  short ehour;          /* 30 Experiment time: Hours (as binary)   */
  short eminute;        /* 32 Experiment time: Minutes (as binary) */
  short noscan;         /* 34 number of multiple scans
			   if noscan == -1 use lnoscan             */
  short fastacc;        /* 36                                      */
  short seconds;        /* 38 Experiment time: Seconds (as binary) */
  short DetType;        /* 40 CCD/Diodearray type                  */
  WORD xdim;            /* 42 actual # of pixels on x axis         */
  short stdiode;        /* 44 trigger diode                        */
  float nanox;          /* 46                                      */
  float calibdio[10];   /* 50 calibration diodes                   */
  char fastfile[16];    /* 90 nameof pixel control file            */
  short asynen;         /* 106 asynchron enable flag 0 = off       */
  short datatype;       /* 108 experiment data type          
			   0 = FLOATING POINT,
			   1 = LONG INTEGER,
			   2 = INTEGER,
			   3 = UNSIGNED INTEGER                    */
  float caliban[10];    /* 110 calibration nanometer               */
  short BackGrndApplied;/* 150 set to 1 if background sub done     */
  short astdiode;       /* 152                                     */
  WORD minblk;          /* 154 min. # of strips per skips          */
  WORD numminblk;       /* 156 # of min-blocks before geo skps     */
  double calibpol[4];   /* 158 calibration coefficients            */
  WORD ADCrate;         /* 190 ADC rate                            */
  WORD ADCtype;         /* 192 ADC type                            */
  WORD ADCresolution;   /* 194 ADC resolution                      */
  WORD ADCbitAdjust;    /* 196 ADC bit adjust                      */
  WORD gain;            /* 198 gain                                */
  char exprem[5][80];   /* 200 experiment remarks                  */
  WORD geometric;       /* 600 geometric operations rotate 0x01    
			   reverse 0x02, flip 0x04                 */
  char xlable[16];      /* Intensitv display string                */
  WORD cleans;          /* 618 cleans                              */
  WORD NumSkpPerCln;    /* 620 number of skips per clean           */
  char califile[16];    /* 622 calibration file name (CSMA)        */
  char bkgdfile[16];    /* 638 background file name                */
  short strcmp;         /* 654 number of source comp. diodes       */
  WORD ydim;            /* 656 y dimension of raw data             */
  short scramble;       /* 658 0 = scrambled, 1 = unscrambled      */
  long lexpos;          /* 660 long exposure in milliseconds       
			   used if exposure set to -1              */
  long lnoscan;         /* 664 long num of scans                   
			   used if noscan set to -1                */
  long lavgexp;         /* 668 long num of accumulation            
			   used if avgexp set to -1                */
  char stripfil[16];    /* 672 strip file (st130)                  */
  char version[16];     /* 688 version & data:"01.000 02/01/90"    */
  short type;           /* 704 1 = new120 (Type II)
			   2 = old120 (TYpe I)
			   3 = ST130
			   4 = ST121
			   5 = ST138
			   6 = DC131 (PentaMAX)
			   7 = ST131 (MicroMAX/SpectroMax)
			   8 = ST135
			   9 = VICCD
			   10 = ST116
			   11 = OMA3 (GPIB)
			   12 = OMA4                               */
  short flatFieldApplied; /* 706 Set to 1 if flat field was applied */
  short spare[8];       /* 708 reverved                            */
  short kin_trig_mode;  /* 724 Kinetics Trigger Mode               */
  short empty[702];     /* 726 EMPTY BLOCK FOR EXPANSION           */
  float clkspd_us;      /* 1428 Vert Clock Speed in micro-sec      */
  short HWaccumflag;    /* 1432 set to 1 if accum done by Hardware */
  short StoreSync;      /* 1434 set to 1 if store sync used        */
  short BlemishApplied; /* 1436 set to 1 if blemish removal applied*/
  short CosmicApplied;  /* 1438 set to 1 if cosmic ray removel done*/
  short CosmicType;     /* 1440 if cosmic ray applied, this is type*/
  float CosmicThreshold;/* 1442 Threshold of cosmic ray removal    */
  long NumFrames;       /* 1446 number of frames in file           */
  float MaxIntensity;   /* 1450 max intensity of data (future)     */
  float MinIntensity;   /* 1454 min intensity of data (future)     */
  char ylabel[LABELMAX];/* 1458 y axis label                       */
  WORD ShutterType;     /* 1474 shutter type                       */
  float shutterComp;    /* 1476 shutter compensation time          */
  WORD readoutMode;     /* 1480 Readout mode, full, kinetics, etc  */
  WORD WindowSize;      /* 1482 Window size for kinetics only      */
  WORD clkspd;          /* 1484 clock speed for kinetics &
			   frame transfer                          */
  WORD interface_type;  /* 1486 computer interface (isa-taxi, pci,
			   eisa, etc.)                             */
  DWORD ioAdd1;         /* 1488 I/O address of interface card      */
  DWORD ioAdd2;         /* 1492 if more than one address for card  */
  DWORD ioAdd3;         /* 1996                                    */
  WORD intLevel;        /* 1500 interrupt level interface card     */
  WORD GPIBadd;         /* 1502 GPIB address (if used)             */
  WORD ControlAdd;      /* 1504 GPIB controller address (if used)  */
  WORD controllerNum;   /* 1506 if multiple controller system will
			   have controller # data came from.    
			   (Future item)                           */
  WORD SWmade;          /* 1508 Software which created this file   */
  short NumROI;         /* 1510 number of ROIs used. if 0 assume 1
			   1512 - 1630 ROI information             */
  struct ROIinfo {      /*                                         */
    WORD startx;        /* left x start value                      */
    WORD endx;          /* right x value                           */
    WORD groupx;        /* amount x is binned/grouped in hw        */
    WORD starty;        /* top y start value                       */
    WORD endy;          /* bottom y value                          */
    WORD groupy;        /* amount y is binned/grouped in hw        */
  } ROIinfoblk[10];     /* ROI Starting Offsets:                    
			   ROI 1 = 1512
			   ROI 2 = 1524
			   ROI 3 = 1536
			   ROI 4 = 1548
			   ROI 5 = 1560
			   ROI 6 = 1572
			   ROI 7 = 1584
			   ROI 8 = 1596
			   ROI 9 = 1608
			   ROI 10 = 1620                           */
  char FlatField[120];  /* 1632 Flat field file name               */
  char background[120]; /* 1752 Background sub. file name          */
  char blemish[120];    /* 1872 Blemish file name                  */
  float software_ver;   /* 1992 Software version                   */
  char UserInfo[1000];  /* 1996-2995 user data                     */
  long WinView_id;      /* 2996 Set to 0x01234567L if file was
			   created by WinX                         */
  BYTE tmp[1100];
} StackHeader;

class CFrame
{
 private:
  void *elem;                 // Stack a frame

  struct fImageData { GLfloat *data; };
  struct lImageData { GLint *data; };
  struct sImageData { GLshort *data; };
  struct usImageData { GLushort *data; };

 public:
  StackHeader info;           // Header information

  fImageData *fImage;
  lImageData *lImage;
  sImageData *sImage;
  usImageData *usImage;

  CFrame();                  // Constructor
  ~CFrame();           // Destructor

  void read_header(FILE *);
  void allocmemory();
  void allocmemory_float();
  void make_image(char *record);

  void print_header();
};

