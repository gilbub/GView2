#include <jni.h>
#include "NativeSPEDecoder.h"
#include <stdio.h>
#define MAXSPES 10
#define MAXROIS 100


struct spe{
  FILE *stream;
  FILE *outfile;

  int X_dim;
  int Y_dim;
  int len; /*X_dim x Y_dim*/
  long Num_frames;
  int datatype;
  int sizeval;

  int* longframe;
  float* floatframe;
  short* shortframe;
  unsigned short* ushortframe;

  int* back;

}spes[MAXSPES];


struct roi{
 int *address;
 int len;
 };


int* openspes;

int Y_dimension;
int temp_y_dim;


void readFrame(int num){
int i;
switch (spes[num].datatype) {
  case 0: // float
    fread(spes[num].floatframe, sizeof(float), spes[num].len, spes[num].stream);
    break;

  case 1: // long
    fread(spes[num].longframe, sizeof(long), spes[num].len, spes[num].stream);
    break;

  case 2: // short
    fread(spes[num].shortframe, sizeof(short), spes[num].len, spes[num].stream);
	break;

  case 3:  // unsigned short
	fread(spes[num].ushortframe, sizeof(unsigned short), spes[num].len, spes[num].stream);
    break;
  }
}


int findOpenSPE(){
int i;
for (i=0;i<MAXSPES;i++){
 if (openspes[i]==0){openspes[i]=1; break;}
}
return i;
}


int readHeader(const char* inname){


unsigned short tempushort;
short tempshort;
int instance;
int counter;

instance=findOpenSPE();

printf("instance updated %d\n",instance);

if ((spes[instance].stream = fopen(inname,"rb"))==NULL)
	 {printf("no file present\n");}

fseek(spes[instance].stream,0,SEEK_SET);


     fseek(spes[instance].stream, 42, SEEK_SET);
     fread(&tempushort, sizeof(unsigned short), 1, spes[instance].stream);
	 spes[instance].X_dim=(int)tempushort;

	 fseek(spes[instance].stream, 656, SEEK_SET);
	 fread(&tempushort, sizeof(unsigned short), 1, spes[instance].stream);
	 spes[instance].Y_dim=(int)tempushort;

	 fseek(spes[instance].stream, 108, SEEK_SET);
	 fread(&tempshort, sizeof(short), 1, spes[instance].stream);
	 spes[instance].datatype=(int)tempshort;


	spes[instance].len=spes[instance].X_dim*spes[instance].Y_dim;
    switch (spes[instance].datatype){
		 case(0): spes[instance].sizeval=sizeof(float); break;
		 case(1): spes[instance].sizeval=sizeof(long); break;
		 case(2): spes[instance].sizeval=sizeof(short); break;
		 case(3): spes[instance].sizeval=sizeof(unsigned short); break;
		 }


	/*find the end of the file*/
	fseek(spes[instance].stream,0,SEEK_END);

	spes[instance].Num_frames=(int)(((double)(ftell(spes[instance].stream)-4100)/spes[instance].sizeval)/(spes[instance].X_dim*spes[instance].Y_dim));

	fseek(spes[instance].stream,4100,SEEK_SET);

	/*alocate data*/

	if (spes[instance].floatframe!=NULL) {free(spes[instance].floatframe);printf("free'd data\n");}
	if (spes[instance].longframe!=NULL) {free(spes[instance].longframe);printf("free'd data\n");}
	if (spes[instance].shortframe!=NULL) {free(spes[instance].shortframe);printf("free'd data\n");}
	if (spes[instance].ushortframe!=NULL) {free(spes[instance].ushortframe);printf("free'd data\n");}

	if (spes[instance].back!=NULL) free(spes[instance].back);

	//alocate the background frame.
	spes[instance].back=(long *)malloc(spes[instance].len * sizeof(long));
	//initialize the background to 0
	for (counter=0;counter<spes[instance].len;counter++) spes[instance].back[counter]=0;

	switch (spes[instance].datatype) {
	  case 0: // float
	     spes[instance].floatframe = (float *)malloc(spes[instance].len * sizeof(float));
	     if (spes[instance].floatframe==NULL) printf("allocation err\n");
	   break;

	  case 1: // long
	      spes[instance].longframe = (long *)malloc(spes[instance].len * sizeof(long));
		  if (spes[instance].longframe==NULL) printf("allocation err\n");
	    break;

	  case 2: // short
	      spes[instance].shortframe = (short *)malloc(spes[instance].len * sizeof(short));
		  if (spes[instance].shortframe==NULL) printf("allocation err\n");
	    break;

	  case 3: // unsigned short
	      spes[instance].ushortframe= (unsigned short *)malloc(spes[instance].len* sizeof(unsigned short));
	      if (spes[instance].ushortframe==NULL) printf("allocation err\n");
		break;

	  default:
	    printf("nothing alocated, error\n");
  }

 return instance;
}


JNIEXPORT jint JNICALL Java_NativeSPEDecoder_UpdateImageArray (JNIEnv *env, jobject obj, jintArray arr, jint xdim, jint ydim, jint spenum)
{
int i;
jboolean isCopy = JNI_FALSE;
jint len = (*env)->GetArrayLength(env, arr);


jint* body = (jint*) (*env)->GetPrimitiveArrayCritical(env,arr, &isCopy);


if (body!=NULL){
readFrame(spenum);

switch (spes[spenum].datatype) {
  case 0: // float
   for (i=0;i<len;i++){body[i]=(int)spes[spenum].floatframe[i] - spes[spenum].back[i];}
    break;

  case 1: // long
   for (i=0;i<len;i++){body[i]=(int)spes[spenum].longframe[i] - spes[spenum].back[i];}

  break;

  case 2: // short
    for (i=0;i<len;i++){body[i]=(int)spes[spenum].shortframe[i] - spes[spenum].back[i];}
	break;

  case 3:  // unsigned short
     for (i=0;i<len;i++){body[i]=(int)spes[spenum].ushortframe[i]- spes[spenum].back[i];}
    break;


  }

(*env)->ReleasePrimitiveArrayCritical(env, arr, body, 0);


}
return 1;

}


JNIEXPORT jint JNICALL Java_NativeSPEDecoder_JumpToFrame(JNIEnv *env, jobject obj, jint frame, jint instance)
{
printf("trying to jump to ... %d instance %d\n",frame,instance);
fseek(spes[instance].stream,frame*spes[instance].sizeval*spes[instance].len+4100,SEEK_SET);
return frame;

}

JNIEXPORT jint JNICALL Java_NativeSPEDecoder_OpenImageFile(JNIEnv *env, jobject obj, jstring name)
{
int i;
const char *str = (*env)->GetStringUTFChars(env, name, 0);

if (openspes==NULL){
 /*initialize*/
 openspes=(int *)malloc((MAXSPES+1)*sizeof(int));
 for (i=0;i<MAXSPES;i++){openspes[i]=0;}
 }

return readHeader(str);
}

JNIEXPORT jint JNICALL Java_NativeSPEDecoder_CloseImageFile(JNIEnv *env, jobject obj, jint instance)
{
 fclose(spes[instance].stream);
 openspes[instance]=0;
return 1;

}

JNIEXPORT jint JNICALL Java_NativeSPEDecoder_FilterOperation(JNIEnv *env, jobject obj, jint val, jint startx, jint endx, jint instance)
{
 int i;
 int len;
 len=spes[instance].len;
 printf("filter native\n");
 switch(val){
  case 0: //background subtract
   switch (spes[instance].datatype) {
     case 0: // float
      for (i=0;i<len;i++){spes[instance].back[i]=(int)spes[instance].floatframe[i];}
       break;

     case 1: // long
      for (i=0;i<len;i++){spes[instance].back[i]=(int)spes[instance].longframe[i];}
     break;

     case 2: // short
       for (i=0;i<len;i++){spes[instance].back[i]=(int)spes[instance].shortframe[i];}
   	break;

     case 3:  // unsigned short
        for (i=0;i<len;i++){spes[instance].back[i]=(int)spes[instance].ushortframe[i];}
       break;


     }



  }
return 1;

}

JNIEXPORT jstring JNICALL Java_NativeSPEDecoder_ReturnSupportedFilters(JNIEnv *env, jobject obj)
{

char buf[128];
sprintf(buf,"0-background subtract.");
return (*env)->NewStringUTF(env, buf);


}


JNIEXPORT jint JNICALL Java_NativeSPEDecoder_ReturnFrameNumber(JNIEnv *env, jobject obj)
{

return 1;

}



JNIEXPORT jint JNICALL Java_NativeSPEDecoder_ReturnXYBandsFrames(JNIEnv *env , jobject obj, jintArray arr)
{
jboolean isCopy = JNI_FALSE;
jint* body = (jint*) (*env)->GetPrimitiveArrayCritical(env,arr, &isCopy);
body[0]= spes[instance].X_dim;
body[1]= spes[instance].Y_dim;
body[2]= 1;
body[3]= (int)spes[instance].Num_frames;

return 1;
}






JNIEXPORT jint JNICALL Java_NativeSPEDecoder_SumROIs(JNIEnv *env, jobject obj, jobjectArray rois, jstring outfile, jint startframe, jint endframe, jint instance){
/*decode rois, print them out*/
FILE* roifile;
int i;
int j;
int k;
jintArray oneDim;
jint* element;
jint numberOfROIs = (*env)->GetArrayLength(env, rois);
const char *str = (*env)->GetStringUTFChars(env, outfile, 0);
int sum=0;
struct roi* aroi;
jint innerlen;
int* result;
long tmppos;

if (endframe<0) endframe=spes[instance].Num_frames; /*a shortcut for scanning whole record*/
if (endframe>spes[instance].Num_frames) endframe=spes[instance].Num_frames;
if (startframe<0) startframe=0;
if (startframe>spes[instance].Num_frames) startframe=0;

/*copy 2d array to rois structure*/
aroi=malloc(numberOfROIs*sizeof(struct roi));
result=malloc((numberOfROIs+1)*(endframe-startframe)*sizeof(int));

for (i=0;i<numberOfROIs;i++){
      oneDim=(jintArray)((*env)->GetObjectArrayElement(env,rois, i));
	  innerlen=(*env)->GetArrayLength(env, oneDim);
	  aroi[i].len=(*env)->GetArrayLength(env, oneDim);
	  aroi[i].address=malloc(aroi[i].len*sizeof(int));
      element=(*env)->GetIntArrayElements(env,oneDim, 0);
	  for (j=0;j<innerlen;j++) {aroi[i].address[j]=element[j];}

}

 /*jump to first frame*/
 tmppos=ftell(spes[instance].stream);
 fseek(spes[instance].stream,4100+(spes[instance].len*spes[instance].sizeval*startframe),SEEK_SET);

 for (k=0;k<endframe-startframe;k++){

 readFrame(instance);



   for (i=0;i<numberOfROIs;i++){
      sum=0;
    for (j=0;j<aroi[i].len;j++){

	 switch(spes[instance].datatype){
      case 0:
       sum+=(int)spes[instance].floatframe[aroi[i].address[j]];
	   break;
	  case 1:
	   sum+=(int)spes[instance].longframe[aroi[i].address[j]];
	   break;
      case 2:
	   sum+=(int)spes[instance].shortframe[aroi[i].address[j]];
	   break;
	  case 3:
	   sum+=(int)spes[instance].ushortframe[aroi[i].address[j]];
	   break;
	   }/*end case */
      }/*end j, we've summed up one roi*/

     result[k*numberOfROIs+i]=sum;
	 }/*weve finished with the rois for this frame*/
	}

if ((roifile = fopen(str,"w"))==NULL)
	 {printf("cant open the outfile %s\n",str);}
else{
 for (j=0;j<endframe-startframe;j++){
 fprintf(roifile,"%d ",j+startframe);
 for (i=0;i<numberOfROIs;i++) fprintf(roifile,"%d ",result[j*numberOfROIs+i]);
 fprintf(roifile,"\n");
 }
 fclose(roifile);
}
fseek(spes[instance].stream,tmppos,SEEK_SET);
 free(result);
 free(aroi);
return 1;
}

