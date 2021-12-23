/*************************************************/
/* class_stack_stream.c                          */
/*                                               */
/*                                               */
/*                                               */
/*                                               */
/*************************************************/

#include <iostream.h>
#include <stdio.h>
#include <stdlib.h>
#include "class_frame.h"

/* Constructor */
CFrame::CFrame()
{
}

/* Destructor */
CFrame::~CFrame()
{
  int i;

  switch (info.datatype) {
  case 0:
    for (i = 0; i < info.NumFrames; i++)
      free(fImage[i].data);
    free(fImage);
    break;

  case 1:
    for (i = 0; i < info.NumFrames; i++)
      free(lImage[i].data);
    free(lImage);
    break;

  case 2:
    for (i = 0; i < info.NumFrames; i++)
      free(sImage[i].data);
    free(sImage);
    break;
    
  case 3:
    for (i = 0; i < info.NumFrames; i++)
      free(usImage[i].data);
    free(usImage);
    break;
  }
}

/*reading in the header on the spe file. */
void CFrame::read_header(FILE *stream)
{
  fseek(stream, 4, SEEK_SET);
  fread(&info.exposure, sizeof(short), 1, stream);
  
  fseek(stream, 10, SEEK_SET);
  fread(&info.exp_sec, sizeof(float), 1, stream);  // alternative exposure

  fseek(stream, 20, SEEK_SET);
  fread(info.date, sizeof(char), 10, stream);      // date as MM/DD/YY

  fseek(stream, 30, SEEK_SET);
  fread(&info.ehour, sizeof(short), 1, stream);    // experiment time

  fseek(stream, 32, SEEK_SET);
  fread(&info.eminute, sizeof(short), 1, stream);  // experiment time

  fseek(stream, 34, SEEK_SET);
  fread(&info.noscan, sizeof(short), 1, stream);

  fseek(stream, 38, SEEK_SET);
  fread(&info.seconds, sizeof(short), 1, stream);  // experiment time

  fseek(stream, 42, SEEK_SET);
  fread(&info.xdim, sizeof(WORD), 1, stream);

  fseek(stream, 108, SEEK_SET);
  fread(&info.datatype, sizeof(short), 1, stream); // experiment data type

  fseek(stream, 656, SEEK_SET);
  fread(&info.ydim, sizeof(WORD), 1, stream);

  fseek(stream, 660, SEEK_SET);
  fread(&info.lexpos, sizeof(long), 1, stream);

  fseek(stream, 664, SEEK_SET);
  fread(&info.lnoscan, sizeof(long), 1, stream);

  fseek(stream, 704, SEEK_SET);
  fread(&info.type, sizeof(short), 1, stream);

  fseek(stream, 1446, SEEK_SET);
  fread(&info.NumFrames, sizeof(long), 1, stream);
  
  fseek(stream, 1992, SEEK_SET);
  fread(&info.software_ver, sizeof(float), 1, stream);

  fseek(stream, ONSETDATA, SEEK_SET);

  //lNumFrames = (info.noscan == -1) ? (long)info.lnoscan : (long)info.noscan;
  //lNumFrames /= (long)info.ydim;
}

void CFrame::print_header()
{
  switch (info.type) {
  case 1:
    cout << "type                 = new120 (Type II)\n";
    break;
  case 2:
    cout << "type                 = new120 (Type I)\n";
    break;
  case 3:
    cout << "type                 = ST130\n";
    break;
  case 4:
    cout << "type                 = ST121\n";
    break;
  case 5:
    cout << "type                 = ST138\n";
    break;
  case 6:
    cout << "type                 = DC131 (PentaMAX)\n";
    break;
  case 7:
    cout << "type                 = ST133 (MicroMAX/SpectroMax)\n";
    break;
  case 8:
    cout << "type                 = ST135 (GPIB)\n";
    break;
  case 9:
    cout << "type                 = VICCD\n";
    break;
  case 10:
    cout << "type                 = ST116 (GPIB)\n";
    break;
  case 11:
    cout << "type                 = OMA3 (GPIB)\n";
    break;
  case 12:
    cout << "type                 = OMA4\n";
    break;
  default:
    cout << "unknown type\n";
    break;
  }

  cout << "date                 = " << info.date << "\n";
  cout << "experiment time      = " << info.ehour << ":"
       << info.eminute << ":" 
       << info.seconds << "\n";
  if (info.exposure > 0)
    cout << "exposure             = " << info.exposure << "\n";
  else
    cout << "exposure             = " << info.lexpos << "\n";
  cout << "alternative exposure = " << info.exp_sec * 1000 << "ms\n";
  cout << "xdim x ydim x frames = " << info.xdim << " x " 
       << info.ydim << " x " 
       << info.NumFrames << "\n";
  switch (info.datatype) {
  case 0:
    cout << "datatype             = Floating point\n";
    break;
  case 1:
    cout << "datatype             = Long integer\n";
    break;
  case 2:
    cout << "datatype             = Integer\n";
    break;
  case 3:
    cout << "datatype             = Unsigned integer\n";
    break;
  default:
    break;
  }

  cout << "noscan               = " << info.noscan << "\n";
  cout << "lnoscan              = " << info.lnoscan << "\n";
  cout << "software version     = " << info.software_ver << "\n";
}

/* memory allocation */
void CFrame::allocmemory()
{
  int i;

  switch (info.datatype) {
  case 0: // float
    fImage = (fImageData *)malloc(info.NumFrames * sizeof(fImageData));
    for (i = 0; i < info.NumFrames; i++)
      fImage[i].data = (GLfloat *)malloc(info.xdim * info.ydim *
					 sizeof(GLfloat));
    break;

  case 1: // long
    lImage = (lImageData *)malloc(info.NumFrames * sizeof(lImageData));
    for (i = 0; i < info.NumFrames; i++)
      lImage[i].data = (GLint *)calloc(info.xdim * info.ydim,
				       sizeof(GLint));
    break;

  case 2: // short
    sImage = (sImageData *)malloc(info.NumFrames * sizeof(sImageData));
    for (i = 0; i < info.NumFrames; i++)
      sImage[i].data = (GLshort *)malloc(info.xdim * info.ydim *
					 sizeof(GLshort));
    break;

  case 3: // unsigned short
    usImage = (usImageData *)malloc(info.NumFrames * sizeof(usImageData));
    for (i = 0; i < info.NumFrames; i++)
      usImage[i].data = (GLushort *)malloc(info.xdim * info.ydim *
					   sizeof(GLushort));
    break;

  default:
    cout << "unrecognizable datatype" << endl;
    exit(1);
  }
}

void CFrame::allocmemory_float()
{
  int i;

  fImage = (fImageData *)malloc(info.NumFrames * sizeof(fImageData));
  if (fImage == NULL) {
    fprintf(stderr, "memory allocation error\n");
    exit(1);
  }

  for (i = 0; i < info.NumFrames; i++)
    fImage[i].data=(GLfloat *)malloc(info.xdim * info.ydim * sizeof(GLfloat));
}

void CFrame::make_image(char *record)
{
  FILE *fp;
  int y, z;
  
  if ((fp = fopen(record, "rb")) == NULL) { 
    cout << "no such file exist" << endl; 
    exit(1);
  }

  fseek(fp, ONSETDATA, SEEK_SET); /* skip header information */

  switch (info.datatype) {
  case 0: // float
    for (z = 0; z < info.NumFrames; z++)
      for (y = info.xdim * info.ydim - info.xdim; y > -1; y -= info.xdim)
	fread(&fImage[z].data[y], sizeof(GLfloat), info.xdim, fp);
    break;

  case 1: // long
    for (z = 0; z < info.NumFrames; z++)
      for (y = info.xdim * info.ydim - info.xdim; y > -1; y -= info.xdim)
	fread(&lImage[z].data[y], sizeof(GLint), info.xdim, fp);
    break;

  case 2: // short
    for (z = 0; z < info.NumFrames; z++)
      for (y = info.xdim * info.ydim - info.xdim; y > -1; y -= info.xdim)
	fread(&sImage[z].data[y], sizeof(GLshort), info.xdim, fp);
    break;

  case 3:  // unsigned short
    for (z = 0; z < info.NumFrames; z++)
      for (y = info.xdim * info.ydim - info.xdim; y > -1; y -= info.xdim)
	fread(&usImage[z].data[y], sizeof(GLushort), info.xdim, fp);
    break;

  default:
    fprintf(stderr, "unrecognizable data type\n");
    exit(1);
  }

  fclose(fp);
}
