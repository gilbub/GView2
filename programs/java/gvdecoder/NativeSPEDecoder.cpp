#include <jni.h>
#include "NativeSPEDecoder.h"
#include <stdio.h>
 
FILE *stream;
int X_dim;
int Y_dim;
int Y_dimension;
int temp_y_dim;
unsigned long Num_frames;
int datatype;
int framedata;


 


int readHeader(const char* inname){
/*
char header[1026];

if ((stream = fopen(inname,"rb"))==NULL)
	 { cout<< "no file present"<<endl; exit(1);}

 
	fread(header,sizeof(char),1025,stream);

	X_dim = (int)header[42];

	temp_y_dim  = (int)header[34];
	if( temp_y_dim <0 )
	    Y_dimension = (long)header[664];
	else
	    Y_dimension = (long)temp_y_dim;

	if (Y_dimension<1) Y_dimension=1;
	Y_dim = (int)header[656];
	Num_frames   = (unsigned long)Y_dimension/Y_dim;
	datatype = (int)header[108];
	framedata=datatype;
	cout <<"time between frames : "<<(int)header[3]<<endl;
	cout <<"datatype : "<< datatype <<endl;
	cout <<"X dim : " <<X_dim <<endl;
	cout <<"Y dim max : "<< Y_dimension <<endl;
	cout <<"Y dim "<< Y_dim <<endl;
	cout <<"Num_frames "<< Y_dimension/Y_dim<<endl;
*/
return 1;
}



JNIEXPORT jint JNICALL Java_NativeSPEDecoder_UpdateImageArray (JNIEnv *env, jobject obj, jintArray arr, jint xdim, jint ydim, jint depth)
{

jboolean isCopy = JNI_FALSE;
jint len = (*env)->GetArrayLength(env, arr); 
jint* body = (jint*) (*env)->GetPrimitiveArrayCritical(env,arr, &isCopy);

return 1;

}


JNIEXPORT jint JNICALL Java_NativeSPEDecoder_JumpToFrame(JNIEnv *env, jobject obj, jint frame)
{

return 1;

}

JNIEXPORT jint JNICALL Java_NativeSPEDecoder_OpenImageFile(JNIEnv *env, jobject obj, jstring name)
{
const char *str = (*env)->GetStringUTFChars(env, name, 0);
readHeader(str);
return 1;
}
 
JNIEXPORT jint JNICALL Java_NativeSPEDecoder_CloseImageFile(JNIEnv *env, jobject obj)
{

return 1;

}
 
JNIEXPORT jint JNICALL Java_NativeSPEDecoder_FilterOperation(JNIEnv *env, jobject obj, jint val)
{

return 1;

}

JNIEXPORT jstring JNICALL Java_NativeSPEDecoder_ReturnSupportedFilters(JNIEnv *env, jobject obj)
{

char buf[128];
sprintf(buf,"nothing supported yet.");
return (*env)->NewStringUTF(env, buf);


}


JNIEXPORT jint JNICALL Java_NativeSPEDecoder_ReturnFrameNumber(JNIEnv *env, jobject obj)
{

return 1;

}