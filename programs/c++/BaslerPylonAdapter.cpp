/* 
* notes for changing the pylon version
* 1) In configuration manager - remember that each one of the configurations (release, debug, win32 or 64) 
*    has include directories that have to be changed to the new installed version of pylon.
* 2) There is a windows environment variable $PYLON_DEV_DIR$ that has to be changed through windows (not through visual studio)
*    This shows up if you search through the 'macros'
*/

// BaslerPylonAdapter.cpp : Defines the exported functions for the DLL application.
//

#include "stdafx.h"
#include <sstream>
#include <string>
#include <iostream>
#include <cstdlib>
#include "gvdecoder_BaslerController.h"
#include "c:\CD\jdk1.8\include\jni.h"
#include "C:\CD\programs\c\ConfigFile\ConfigFile.h"
#include <pylon/usb/BaslerUsbInstantCameraArray.h>
#include "Header.h"
#include "intrin.h"

// Grab.cpp
/*
Note: Before getting started, Basler recommends reading the Programmer's Guide topic
in the pylon C++ API documentation that gets installed with pylon.
If you are upgrading to a higher major version of pylon, Basler also
strongly recommends reading the Migration topic in the pylon C++ API documentation.

This sample illustrates how to grab and process images using the CInstantCamera class.
The images are grabbed and processed asynchronously, i.e.,
while the application is processing a buffer, the acquisition of the next buffer is done
in parallel.

The CInstantCamera class uses a pool of buffers to retrieve image data
from the camera device. Once a buffer is filled and ready,
the buffer can be retrieved from the camera object for processing. The buffer
and additional image data are collected in a grab result. The grab result is
held by a smart pointer after retrieval. The buffer is automatically reused
when explicitly released or when the smart pointer object is destroyed.
*/

// Include files to use the PYLON API.
#include <pylon/PylonIncludes.h>
#ifdef PYLON_WIN_BUILD
#    include <pylon/PylonGUI.h>
#endif

// Namespace for using pylon objects.
using namespace Pylon;

#include <pylon/usb/BaslerUsbInstantCamera.h>
typedef Pylon::CBaslerUsbInstantCamera Camera_t;
using namespace Basler_UsbCameraParams;

// Namespace for using cout.
using namespace std;

// Number of images to be grabbed.
static const uint32_t c_countOfImagesToGrab = 10000;

boolean pyloninitialized = false;

Camera_t * camera_p;
CBaslerUsbInstantCameraArray * cameras;
int frame_width;
int frame_height;
int circular_buffer_size;
int x_offset;
int y_offset;
int exposetime_us;
bool fixframerate;
int framerate;
string savedirectory;


ofstream savefile;

namespace cb
{
	int size = 100;
	int start = 0;
	int end = 1;
	int index = 0;
	bool isInitialized = false;
	uint8_t *data;
	bool wrote_to_end = false;
	int elements = 128 * 128 * 100;
	int bytes_per_pixel = 2;
	int badframes = 0;

}

namespace cam
{
	int width = 512;
	int height = 512;
	int x_offset=0;
	int y_offset=0;
	int x_bin = 1;
	int y_bin = 1;
	int exposetime_us=500;
	int gainauto=0;
	float gainvalue = 0;
	int trigger=0;
	double fps = -1;
}


namespace group
{
	int numberofcameras = 2;
	int activecamera = 0;
}

void initialize();
void pyterminate();

int setActiveCamera(int camnum) {
	if (camnum < group::numberofcameras) {
		for (size_t i = 0; i < cameras->GetSize(); i++) {
			if (((*cameras)[i]).GetCameraContext() == camnum) {
				camera_p = &(*cameras)[i];
				group::activecamera = camnum;
				break;
			}
		}
	}
	return group::activecamera;

}


boolean setCameraParameters()
{
	try
	{
		boolean closecamera = false;
		if (!camera_p->IsOpen()) {
			camera_p->Open();
			closecamera = true;
		}
		camera_p->MaxNumBuffer = 5;


		if (cam::x_bin != -2) {
			camera_p->BinningHorizontalMode.SetValue(BinningHorizontalMode_Sum);
			camera_p->BinningHorizontal.SetValue(cam::x_bin);
		}
		if (cam::y_bin != -2) {
			camera_p->BinningVerticalMode.SetValue(BinningVerticalMode_Sum);
			camera_p->BinningHorizontal.SetValue(cam::y_bin);
		}

		if (cam::gainauto != -2) {
			if (cam::gainauto == 0) camera_p->GainAuto.SetValue(GainAuto_Off);
			if (cam::gainauto == 1) {
				camera_p->GainAuto.SetValue(GainAuto_Once); cam::gainvalue = 0;
			}
			if (cam::gainauto == 2) {
				camera_p->GainAuto.SetValue(GainAuto_Continuous); cam::gainvalue = 0;
			}
		}
		if (cam::gainvalue != -2) {
			if (cam::gainvalue > 0) {
				camera_p->GainAuto.SetValue(GainAuto_Off);
				camera_p->Gain.SetValue(cam::gainvalue);
			}
		}
		if (cam::trigger != -2) {
			if (cam::trigger == 0) camera_p->TriggerMode.SetValue(TriggerMode_Off);
			if (cam::trigger == 1) {
				camera_p->TriggerMode.SetValue(TriggerMode_On);
				camera_p->TriggerSelector.SetValue(TriggerSelector_FrameStart);
			}
			if (cam::trigger == 2) {
				camera_p->TriggerMode.SetValue(TriggerMode_On);
				camera_p->TriggerSelector.SetValue(TriggerSelector_FrameBurstStart);
			}
		}
		if (cam::fps != -2) {
			if (cam::fps < 0) {
				camera_p->AcquisitionFrameRateEnable.SetValue(false);
			}
			else {
				camera_p->AcquisitionFrameRateEnable.SetValue(true);
				camera_p->AcquisitionFrameRate.SetValue((double)cam::fps);
			}
		}
		if (cam::exposetime_us != -2) {
			camera_p->ExposureTime.SetValue((double)cam::exposetime_us);
		}
		if (cam::width!=-2) camera_p->Width.SetValue(cam::width);
		if (cam::height!=-2) camera_p->Height.SetValue(cam::height);
		if (cam::x_offset != -2) {
			if (cam::x_offset == -1) camera_p->CenterX = true;
			else {
				camera_p->CenterX = false;
				camera_p->OffsetX.SetValue(cam::x_offset);
			}
		}
		if (cam::y_offset != -2) {
			if (cam::y_offset == -1) camera_p->CenterY = true;
			else {
				camera_p->CenterY = false;
				camera_p->OffsetY.SetValue(cam::y_offset);
			}
		}


		std::cout << endl << "camera info (basler DLL): "<< endl;
		std::cout << "x,y offset     =" << camera_p->OffsetX.GetValue() << "," << camera_p->OffsetY.GetValue() << endl;
		std::cout << "x,y dimensions =" << camera_p->Width.GetValue() << "," << camera_p->Height.GetValue() << endl;
		if (cb::bytes_per_pixel != -2) {
			if (cb::bytes_per_pixel == 1)
				camera_p->PixelFormat.SetValue(PixelFormat_Mono8);
			else
				camera_p->PixelFormat.SetValue(PixelFormat_Mono12);
		}
		if (closecamera) camera_p->Close();
	}
	catch(const GenericException &e)
	{
		cerr << "An exception occurred trying to set the camera parameters." << endl
			<< e.GetDescription() << endl;
		return false;
	}
	return true;
}

bool setCircularBuffer() {

	try {
		cb::data = new uint8_t[cb::size*cam::width*cam::height*group::numberofcameras*cb::bytes_per_pixel];
		std::cout << endl << "Allocated circular buffer" << endl;
	}
	catch (std::bad_alloc&) {
		cb::isInitialized = false;
		std::cout <<endl<< "ERROR: Unable to allocate circular buffer. Make sure there is enough free RAM (close other programs) and restart." << endl;
		return false;
	}
	cb::start = 0;
	cb::end = 1;
	cb::index = 0;
	cb::wrote_to_end = false;
	cb::isInitialized = true;
	return true;
}

bool freeCircularBuffer() {
	if (cb::isInitialized) delete[]cb::data;
	cb::isInitialized = false;
	return true;
}


JNIEXPORT jint JNICALL Java_gvdecoder_BaslerController_setOffsets
(JNIEnv *env, jobject obj, jint xoffset, jint yoffset) {
	initialize();
	boolean closecamera = false;
	if (!camera_p->IsOpen()) {
		camera_p->Open();
		closecamera = true;
	}
	if ((xoffset < -1) || (yoffset < -1)) {
		std::cout << endl << "camera info (basler DLL): " << endl;
		std::cout << "unchanged: x,y offset     =" << camera_p->OffsetX.GetValue() << "," << camera_p->OffsetY.GetValue() << endl;
		return -1;
	}
	if (xoffset ==-1) camera_p->CenterX = true; 
	else {
		camera_p->CenterX = false;
		camera_p->OffsetX.SetValue(xoffset);
	}
	if (yoffset ==-1) camera_p->CenterY=true; 
	else {
		camera_p->CenterY = false;
		camera_p->OffsetY.SetValue(yoffset);
	}
	std::cout << endl << "camera info (basler DLL): " << endl;
	std::cout << "x,y offset     =" << camera_p->OffsetX.GetValue() << "," << camera_p->OffsetY.GetValue() << endl;
    if (closecamera)camera_p->Close();
	return 0;
}


JNIEXPORT jint JNICALL Java_gvdecoder_BaslerController_setParameters
(JNIEnv *env, jobject obj, jint camnumber, jintArray jparams) {
	//integers: w, h, xoffset, yoffset, flipx, flipy, trigger, triggerdelay, expose, gain
	//          0  1  2        3        4      5      6        7             8       9
	//if any parameter is (-2), don't set
	// -1 for xoffset and y offset enable centerx and centery to true.
	boolean closecamera = false;
	initialize();
	//camera_p = &(*cameras)[camnumber];
	setActiveCamera(camnumber);
	jint *params = (*env).GetIntArrayElements(jparams, 0);
	try {
		if (!camera_p->IsOpen()) {
			camera_p->Open();
			closecamera = true;
		}
		//x and y offsets have to be set before width and height
		if (params[2] > -1) { camera_p->CenterX = false;  camera_p->OffsetX.SetValue(params[2]); cam::x_offset = params[2]; }
		if (params[3] > -1) { camera_p->CenterY = false;  camera_p->OffsetY.SetValue(params[3]); cam::y_offset = params[3]; }
		if (params[2] == -1) camera_p->CenterX = true;
		if (params[3] == -1) camera_p->CenterY = true;
		if (IsWritable(camera_p->Width)) {
			if (params[0] > -1) {
				camera_p->Width.SetValue(params[0]);
				cam::width = params[0];
			}
		}
		else  std::cout << " camera with is not writeable - value not changed.";

		if (IsWritable(camera_p->Height)) {
			if (params[1] > -1) {
				camera_p->Height.SetValue(params[1]);
				cam::height = params[1];
			}
		}
		else  std::cout << " camera height is not writeable - value not changed.";

		if (params[4] == 0) camera_p->ReverseX = false;
		if (params[4] == 1) camera_p->ReverseX = true;

		if (params[5] == 0) camera_p->ReverseY = false;
		if (params[5] == 1) camera_p->ReverseY = true;

		if (params[6] == 1) camera_p->TriggerMode = TriggerMode_On;
		if (params[6] == 0) camera_p->TriggerMode = TriggerMode_Off;

		if (params[7] > -1) camera_p->TriggerDelay = params[7];

		if (params[8] > -1) camera_p->ExposureTime = params[8];
		if (params[9] > -1) camera_p->Gain = params[9];
		if (closecamera)camera_p->Close();
	} catch (const GenericException &e) {
		cerr << "An exception occurred. " << endl
			<< e.GetDescription() << endl;
		(*env).ReleaseIntArrayElements(jparams, params, 0);
		return -1;
	}
	(*env).ReleaseIntArrayElements(jparams, params, 0);
	return 1;
}

/*
* Class:     gvdecoder_BaslerController
* Method:    getParameters
* Signature: ([I)I
*/
JNIEXPORT jint JNICALL Java_gvdecoder_BaslerController_getParameters
(JNIEnv *env, jobject obj, jint camnumber, jintArray jparams) {
	initialize();
	//camera_p = &(*cameras)[camnumber];
	setActiveCamera(camnumber);
	boolean closecamera = false;
	try {
		if (!camera_p->IsOpen()) {
			camera_p->Open();
			closecamera = true;
		}

		jint *params = (*env).GetIntArrayElements(jparams, 0);
		params[0] = camera_p->Width.GetValue();
		params[1] = camera_p->Height.GetValue();
		params[2] = camera_p->OffsetX.GetValue();
		params[3] = camera_p->OffsetY.GetValue();
		params[4] = (int)camera_p->ReverseX.GetValue();
		params[5] = (int)camera_p->ReverseY.GetValue();
		params[6] = (int)camera_p->TriggerMode.GetValue();
		params[7] = (int)camera_p->TriggerDelay.GetValue();
		params[8] = (int)camera_p->ExposureTime.GetValue();
		params[9] = (int)camera_p->Gain.GetValue();
		
		if (closecamera) camera_p->Close();

		(*env).ReleaseIntArrayElements(jparams, params, 0);
	}catch (const GenericException &e) {
		cerr << "An exception occurred. " << endl
			<< e.GetDescription() << endl;
		return -1;
	}
	return 1;
}

JNIEXPORT jint JNICALL Java_gvdecoder_BaslerController_loadConfigFile
(JNIEnv * env, jobject obj, jstring pathtofile) {

	try {
		bool resizedbuffer = false;
		const char *str = (*env).GetStringUTFChars(pathtofile, 0);
		std::string initfilepath = str;
		std::cout << "passed path=" << initfilepath << endl;
		(*env).ReleaseStringUTFChars(pathtofile, str);
		ConfigFile cf(initfilepath);
		std::cout << "Here is the configuration" << endl;
		std::cout << cf << endl;
		cb::bytes_per_pixel = cf.read<int>("bytes_per_pixel",1);
		frame_width = cf.read<int>("width", 512);
		frame_height = cf.read<int>("height", 512);
		if ((frame_width != cam::width) || (frame_height != cam::height)) resizedbuffer = true;
		cam::height = frame_height;
		cam::width = frame_width;
		cam::x_offset = cf.read<int>("x_offset", 0);
		cam::y_offset = cf.read<int>("y_offset", 0);
		cam::x_bin = cf.read<int>("x_bin", 1);
		cam::y_bin = cf.read<int>("y_bin", 1);
		group::numberofcameras= cf.read<int>("numberofcameras", 2);
		int newbuffersize= cf.read<int>("framebuffers", 1000);
		if ((newbuffersize != cb::size) || (cb::elements != cam::width*cam::height*cb::size)) resizedbuffer = true;
		
		cam::exposetime_us = cf.read<int>("exposetime_us", 1000);
		savedirectory= cf.read<string>("savepath", ".");
		std::cout << "framewidth=" << frame_width << " frameheight=" << frame_height << endl;
		std::cout << "savedirectory=" << savedirectory;
		if (resizedbuffer) { freeCircularBuffer(); cb::size = newbuffersize; cb::elements = cb::size*cam::width*cam::height; std::cout << "free'd circular buffer"; }
		else std::cout << "circular buffer not resized - reusing circular buffer";
}
catch (ConfigFile::file_not_found& e) {
	std::cout << "Error - File '" << e.filename << "' not found.";
	std::cout << endl << endl;
	return -1;
	
}
catch (ConfigFile::key_not_found& e) {
	std::cout << "Error - Key '" << e.key << "' not found.";
	std::cout << endl << endl;
	return -2;
}

	return 1;
}

/*
* Class:     gvdecoder_BaslerController
* Method:    writeStatus
* Signature: (Ljava/lang/String;)I
*/
JNIEXPORT jint JNICALL Java_gvdecoder_BaslerController_writeStatus
(JNIEnv * env, jobject obj, jstring filename) {

	return 1;
}


JNIEXPORT jint JNICALL Java_gvdecoder_BaslerController_openCamera
(JNIEnv *env, jobject obj) {
	initialize();
	return 1;
}

JNIEXPORT jint JNICALL Java_gvdecoder_BaslerController_closeCamera
(JNIEnv * env, jobject obj) {
	pyterminate();
	return 1;
}

void initialize() {
	if (!pyloninitialized) {
		PylonInitialize();
		pyloninitialized = true;
		try
		{
				// Get the transport layer factory.
				CTlFactory& tlFactory = CTlFactory::GetInstance();

				// Get all attached devices and exit application if no device is found.
				DeviceInfoList_t devices;
				if (tlFactory.EnumerateDevices(devices) == 0)
				{
					throw RUNTIME_EXCEPTION("No camera present.");
				}

				// Create an array of instant cameras for the found devices and avoid exceeding a maximum number of devices.
				cameras = new CBaslerUsbInstantCameraArray(min(devices.size(), 10));

				// Create and attach all Pylon Devices.
				for (size_t i = 0; i < cameras->GetSize(); ++i)
				{
					(*cameras)[i].Attach(tlFactory.CreateDevice(devices[i]));

					// Print the model name of the camera.
					cout << "Using device " << (*cameras)[i].GetDeviceInfo().GetModelName() << endl;
				}

				camera_p = &(*cameras)[group::activecamera];
				
		}catch(const GenericException &e)
		{
			// Error handling.
			cerr << "An exception occurred." << endl
				<< e.GetDescription() << endl;
			pyterminate();
		}
		
	}
}

void pyterminate() {
	if (pyloninitialized) {
		PylonTerminate();
		pyloninitialized = false;
	}
}

JNIEXPORT jint JNICALL Java_gvdecoder_BaslerController_startFocus
(JNIEnv * env, jobject obj, jint exposetime_us) {
	CDeviceInfo info;
	info.SetDeviceClass(Camera_t::DeviceClass());
	// Create an instant camera object with the camera device found first.
	//CInstantCamera camera( CTlFactory::GetInstance().CreateFirstDevice(info));
	//Camera_t camera(CTlFactory::GetInstance().CreateFirstDevice(info));
	std::cout << "before creating camera_p" << endl;
	camera_p = new Camera_t(CTlFactory::GetInstance().CreateFirstDevice(info));
	//camera_p = &camera;
	// Print the model name of the camera.
	
	std::cout << "Using device " << camera_p->GetDeviceInfo().GetModelName() << endl;
	camera_p->RegisterConfiguration(new CSoftwareTriggerConfiguration, RegistrationMode_ReplaceAll, Cleanup_Delete);
	// The parameter MaxNumBuffer can be used to control the count of buffers
	// allocated for grabbing. The default value of this parameter is 10.
	camera_p->MaxNumBuffer = 5;
	camera_p->Open();
	cam::exposetime_us = exposetime_us;
	setCameraParameters();
	std::cout << "camera frame rate = " << camera_p->ResultingFrameRate.GetValue();

	if (camera_p->CanWaitForFrameTriggerReady()) {
		camera_p->StartGrabbing();

		return 1;
	}else
	return 0;
}

JNIEXPORT jdouble JNICALL Java_gvdecoder_BaslerController_setFrameRate
(JNIEnv *env, jobject obj, jdouble fps) {
	double fr = -1;
	initialize();
	boolean closecamera = false;
	if (!camera_p->IsOpen()) {
		camera_p->Open();
		closecamera = true;
	}

	try
	{
		if (fps > 0) {
			camera_p->AcquisitionFrameRateEnable.SetValue(true);
			camera_p->AcquisitionFrameRate.SetValue((double)fps);
		}
		else
		{
			camera_p->AcquisitionFrameRateEnable.SetValue(false);
		}
		fr = camera_p->ResultingFrameRate.GetValue();
	}
	catch (const GenericException &e) {
				cerr << "An exception occurred. Unable to set frame rate." << endl
					<< e.GetDescription() << endl;
	}
	if (closecamera) camera_p->Close();
	return (jdouble)fr;
}


JNIEXPORT void JNICALL Java_gvdecoder_BaslerController_setGainAuto
(JNIEnv *env, jobject obj, jint val) {
	//autogain_once=1, autogain_continuous=2, autogain_disable=0
	initialize();
	boolean closecamera = false;
	if (!camera_p->IsOpen()) {
			camera_p->Open();
			closecamera = true;
		}
	if (val == 0) camera_p->GainAuto.SetValue(GainAuto_Off);
	if (val == 1) camera_p->GainAuto.SetValue(GainAuto_Once);
	if (val == 2) camera_p->GainAuto.SetValue(GainAuto_Continuous);
			//camera_p->Gain.SetValue
		
	if (closecamera) camera_p->Close();
	cam::gainauto = val;
	return;
}

JNIEXPORT jint JNICALL Java_gvdecoder_BaslerController_setGainValue
(JNIEnv *env, jobject obj, jdouble val) {
	float nv;
	initialize();
	boolean closecamera = false;
	if (!camera_p->IsOpen()) {
		camera_p->Open();
		closecamera = true;
	}
	if ((val >= 0) && (val <= 35.0)) nv = (float)val;
	if (val < 0) nv = 0;
	if (val > 35) nv = 35;
	camera_p->Gain.SetValue(nv);
	
	if (closecamera) camera_p->Close();
	return (jint)nv;
}

JNIEXPORT jint JNICALL Java_gvdecoder_BaslerController_setTrigger
(JNIEnv *env, jobject obj, jint val) {
	//off=0, each frame =1, burst = 2
	initialize();
	boolean closecamera = false;
	if (!camera_p->IsOpen()) {
		camera_p->Open();
		closecamera = true;
	}
	if (val == 0) camera_p->TriggerMode.SetValue(TriggerMode_Off);
	if (val == 1) {
				camera_p->TriggerMode.SetValue(TriggerMode_On);
				camera_p->TriggerSelector.SetValue(TriggerSelector_FrameStart);
	}
	if (val == 2) {
				camera_p->TriggerMode.SetValue(TriggerMode_On);
				camera_p->TriggerSelector.SetValue(TriggerSelector_FrameBurstStart);
	}
	if (closecamera) camera_p->Close();
	cam::trigger = val;
	return 1;
}

JNIEXPORT jdouble JNICALL Java_gvdecoder_BaslerController_setExposeTime
(JNIEnv * env, jobject obj, jint exposetime_us) {
	initialize();
	boolean closecamera = false;
	if (!camera_p->IsOpen()) {
		camera_p->Open();
		closecamera = true;
	}
	double val = -1;
 	camera_p->ExposureTime.SetValue((double)exposetime_us);
	val = camera_p->ResultingFrameRate.GetValue();
	if (closecamera) camera_p->Close();
	cam::exposetime_us = exposetime_us;
	
	return (jdouble)val;
	
}

JNIEXPORT jint JNICALL Java_gvdecoder_BaslerController_stopFocus
(JNIEnv * env, jobject obj) {
	camera_p->StopGrabbing();
	camera_p->Close();
	return 1;
}

JNIEXPORT jint JNICALL Java_gvdecoder_BaslerController_GetData
(JNIEnv * env, jobject obj, jintArray arr, jint xoffset, jint yoffset, jint xdim, jint ydim, jint skip) {
	std::cout << "in getData" << endl;
	CGrabResultPtr ptrGrabResult;
	cam::width = xdim;
	cam::height = ydim;

	if (camera_p->WaitForFrameTriggerReady(500, TimeoutHandling_ThrowException))
	{
		
		camera_p->ExecuteSoftwareTrigger();
		camera_p->RetrieveResult(5000, ptrGrabResult, TimeoutHandling_ThrowException);

		if (ptrGrabResult->GrabSucceeded())
		{
			
			jsize len = (*env).GetArrayLength(arr);
			
			jint *body = (*env).GetIntArrayElements(arr, 0);
			const uint8_t *pImageBuffer = (uint8_t *)ptrGrabResult->GetBuffer();
			for (int i = 0; i < xdim*ydim; i++) body[i] = pImageBuffer[i];
			
			(*env).ReleaseIntArrayElements( arr, body, 0);
			return 1;
		}
		
	}
	return 0;
}

JNIEXPORT jint JNICALL Java_gvdecoder_BaslerController_getCircularBufferStoredFrameNumber
(JNIEnv *env, jobject obj) {
	if (cb::wrote_to_end) return cb::size-cb::badframes;
	else return cb::end;
}


JNIEXPORT jint JNICALL Java_gvdecoder_BaslerController_getCircularBufferFrame
(JNIEnv *env, jobject obj, jint framenumber, jintArray arr) {
	jint *body = (*env).GetIntArrayElements(arr, 0);
	int cb_loc = ((cb::start+framenumber)%cb::size)*cam::width*cam::height*group::numberofcameras*cb::bytes_per_pixel;
	if (cb::bytes_per_pixel == 1) for (int i = 0; i < cam::width*cam::height*group::numberofcameras; i++)body[i] = cb::data[i + cb_loc];
	else
		if (cb::bytes_per_pixel == 2) for (int i = 0; i < cam::width*cam::height*group::numberofcameras; i++)body[i] = (cb::data[cb_loc + (i * 2) + 1]) << 8 & 0x0FF00 | (cb::data[cb_loc + (i * 2)]) & 0xFF;
	(*env).ReleaseIntArrayElements(arr, body, 0);
	return 1;

}



JNIEXPORT jint JNICALL Java_gvdecoder_BaslerController_setActiveCamera
(JNIEnv *env, jobject obj, jint camnum) {
	initialize();
	return setActiveCamera(camnum);
	
}


JNIEXPORT jint JNICALL Java_gvdecoder_BaslerController_setPosition
(JNIEnv *env, jobject obj, jstring jserialnumberstring, jint position) {
	initialize();
	const char *serialnumberstring = env->GetStringUTFChars(jserialnumberstring, 0);
	int res = -1;
	boolean closecamera = false;
	for (size_t i = 0; i < cameras->GetSize(); i++) {
		closecamera = false;
		//camera_p->DeviceSerialNumber.ToString()
		camera_p = &(*cameras)[i];
		if (!camera_p->IsOpen()) {
			camera_p->Open();
			closecamera = true;
		}
		if (strcmp(serialnumberstring, camera_p->DeviceSerialNumber.ToString()) == 0) {
			camera_p->SetCameraContext(position);
			std::cout << " camera " << serialnumberstring << " position set to " << position << endl;
			res = position;
			break;
		}
		if (closecamera) camera_p->Close();
	}
		env->ReleaseStringUTFChars(jserialnumberstring, serialnumberstring);
		return res;
}


JNIEXPORT jstring JNICALL Java_gvdecoder_BaslerController_getSerialNumber
(JNIEnv *env, jobject obj) {
	initialize();
	boolean closecamera = false;
	if (!camera_p->IsOpen()) {
		camera_p->Open();
		closecamera = true;
	}
	jstring str =  env->NewStringUTF(camera_p->DeviceSerialNumber.ToString());
	if (closecamera) camera_p->Close();
	return str;
}


JNIEXPORT jint JNICALL Java_gvdecoder_BaslerController_switchCameraOrder
(JNIEnv *env, jobject obj, jint cam1, jint cam2) {
	initialize();
	boolean closecameras = false;
	size_t i;
	int returnval = 0;
	CBaslerUsbInstantCamera * pcam1 = NULL;
	CBaslerUsbInstantCamera * pcam2 = NULL;
	bool foundcam1 = false;
	bool foundcam2 = false;
	if (cameras == NULL) {
		//this shouldn't happen, can erase.
		std::cout << "cameras not initialized, can't perform function. " << endl;
		return -1;
	}

	for (i = 0; i < cameras->GetSize(); i++) {
		if (!((*cameras)[i]).IsOpen()) {
			closecameras = true;
			((*cameras)[i]).Open();
		}
	}

	for (i = 0; i < cameras->GetSize(); i++) {
		if (((*cameras)[i]).GetCameraContext() == cam1) {
			pcam1 = &(*cameras)[i];
			foundcam1 = true;
		}
		if (((*cameras)[i]).GetCameraContext() == cam2) {
			pcam2 = &(*cameras)[i];
			foundcam2 = true;
		}
	}
	if ((foundcam1)&&(foundcam2)){
		std::cout << "switching camera order" << endl;
		intptr_t camera1oldvalue = pcam1->GetCameraContext();
		intptr_t camera2oldvalue = pcam2->GetCameraContext();
		pcam1->SetCameraContext(camera2oldvalue);
		pcam2->SetCameraContext(camera1oldvalue);
		std::cout << " camera " << camera1oldvalue << " switched to " << pcam1->GetCameraContext() << endl;
		std::cout << " camera " << camera2oldvalue << " switched to " << pcam2->GetCameraContext() << endl;
		returnval =  1;
		
	}
	//pcam1->ReverseX();
	if (closecameras) {
		for (i = 0; i < cameras->GetSize(); i++) ((*cameras)[i]).Close();
	}
	return returnval;
}


JNIEXPORT void JNICALL Java_gvdecoder_BaslerController_mirror
(JNIEnv *env, jobject obj, jboolean horizontal, jboolean vertical) {
	initialize();
	boolean closecamera = false;
	if (!camera_p->IsOpen()) {
		camera_p->Open();
		closecamera = true;
	}
	if (horizontal) camera_p->ReverseX = true;   else camera_p->ReverseX = false;
	if (vertical)   camera_p->ReverseY = true; else camera_p->ReverseY = false;
	if (closecamera) camera_p->Close();
}




JNIEXPORT jint JNICALL Java_gvdecoder_BaslerController_saveToCircularBufferMulti
(JNIEnv *env, jobject obj, jstring savepath, jint numberofframes) {
	std::cout << "in saveToCircularBuffer multi dll call. " << endl;
	jclass cls = NULL;
	int exitCode = 1;
	jmethodID frameReady_id = NULL;
	jmethodID pushFrame_id = NULL;
	if (!cb::isInitialized) {
		if (!setCircularBuffer()) return 0;
	}
	
	cb::index = 0;
	cb::start = 0;
	cb::end = 1;
	cb::wrote_to_end = false;

	bool cancel_save = false;
	jsize len = (jint)(cam::width*cam::height*group::numberofcameras*cb::bytes_per_pixel);
	jbyteArray framearray = env->NewByteArray(len);
	jint frameready_result = 0;
	uint8_t * frame = new uint8_t[cam::width*cam::height*group::numberofcameras*cb::bytes_per_pixel];

	int *framecounter=new int[group::numberofcameras];
	for (int p = 0; p < group::numberofcameras; p++) framecounter[p] = 0;

	uint8_t * saveframe = new uint8_t[cam::width*cam::height*group::numberofcameras * 2];;

	DWORD dw = GetTickCount();

	// Before using any pylon methods, the pylon runtime must be initialized. 
	initialize();

	int savecounter = 0;
	if (numberofframes > 0) {
		const char *str = (*env).GetStringUTFChars(savepath, 0);
		std::string mypath = str;
		cout << "passed path=" << mypath << endl;
		(*env).ReleaseStringUTFChars(savepath, str);
		savefile.open(mypath, ios::binary | ios::out);
		uint32_t mode = _byteswap_ulong(1); //htonl makes sure the byte order is java friendly (bigendian)
		uint32_t zdim = _byteswap_ulong(numberofframes);
		uint32_t xdim = _byteswap_ulong(group::numberofcameras*cam::width);
		uint32_t ydim = _byteswap_ulong(cam::height);
		
		savefile.write(reinterpret_cast<char*>(&mode), sizeof(uint32_t));
		savefile.write(reinterpret_cast<char*>(&zdim), sizeof(uint32_t));
		savefile.write(reinterpret_cast<char*>(&xdim), sizeof(uint32_t));
		savefile.write(reinterpret_cast<char*>(&ydim), sizeof(uint32_t));
	}

	try
	{
		
		for (size_t i = 0; i < cameras->GetSize(); i++)
		{
			camera_p = &(*cameras)[i];	
			setCameraParameters();
		    camera_p->RegisterImageEventHandler(new CImageEventPrinter, RegistrationMode_Append, Cleanup_Delete);
			if (GenApi::IsWritable(camera_p->ChunkModeActive))
			{
				camera_p->ChunkModeActive.SetValue(true);
				camera_p->ChunkSelector.SetValue(ChunkSelector_Timestamp);
				camera_p->ChunkEnable.SetValue(true);
				
			}
			else { cout << "Camera chunk not writable" << endl; }

		}
		
		
		setActiveCamera(group::activecamera);

		// Starts grabbing for all cameras starting with index 0. The grabbing
		// is started for one camera after the other. That's why the images of all
		// cameras are not taken at the same time.
		// However, a hardware trigger setup can be used to cause all cameras to grab images synchronously.
		// According to their default configuration, the cameras are
		// set up for free-running continuous acquisition.
		(*cameras).StartGrabbing();

		// This smart pointer will receive the grab result data.
		CGrabResultPtr ptrGrabResult;

		cls = env->GetObjectClass(obj);
		if (cls != 0)
		{
			cout << "before getMethodID call" << endl << endl;
			pushFrame_id = env->GetMethodID(cls, "pushFrame", "([B)V");
			frameReady_id = env->GetMethodID(cls, "frameReady", "(I)I");
			cout << "after getMethodID call" << endl << endl;
		}
		else
		{
			//shouldn't happen
			cout << endl << "Error: unable to call java class from c++, exiting circular buffer routine." << endl;
			return -1;
		}
		if ((frameReady_id == 0) || (pushFrame_id == 0))
		{
			//shouldn't happen
			cout << endl << "Error: unable to get class names from c++ dll, exiting circular buffer routine." << endl;
			return -1;
		}


		int lastframesent = 0;

		SYSTEMTIME nt;
		while ((*cameras).IsGrabbing())
		{
			
			(*cameras).RetrieveResult(5000, ptrGrabResult, TimeoutHandling_ThrowException);

			// When the cameras in the array are created the camera context value
			// is set to the index of the camera in the array.
			// The camera context is a user settable value.
			// This value is attached to each grab result and can be used
			// to determine the camera that produced the grab result.
			
			if ((GetTickCount() - dw) > 2000)
			{

				//if (IsReadable(ptrGrabResult->ChunkTimestamp))
				
					//cout << "TimeStamp (Result): " << ptrGrabResult->ChunkTimestamp.GetValue() << endl;

 

				intptr_t cameraContextValue = ptrGrabResult->GetCameraContext();
				cout << "TimeStamp (Result):" <<cameraContextValue<<" "<< ptrGrabResult->GetChunkDataNodeMap().GetNode("ChunkTimestamp") << endl;

				framecounter[cameraContextValue] += 1;

				const uint8_t *pImageBuffer = (uint8_t *)ptrGrabResult->GetBuffer();

				//old
				//int loc = cameraContextValue * cam::width*cam::height * cb::bytes_per_pixel;

				//new
				int imgsize = cam::width*cam::height*cb::bytes_per_pixel;
				int rowsize = imgsize * group::numberofcameras;
				int loc = (framecounter[cameraContextValue] % cb::size)*rowsize + cameraContextValue * imgsize;

				for (int i = 0; i < imgsize; i++) { cb::data[i + loc] = pImageBuffer[i]; } //copies into the right location`

				int minvalue = 2147483647; //big number
				int maxvalue = -1;
				int tmp = 0;
				for (int j = 0; j < group::numberofcameras; j++) {
					tmp = framecounter[j];
					if (tmp < minvalue) minvalue = tmp;
					if (tmp > maxvalue) maxvalue = tmp;
				}
				if (minvalue > lastframesent)
				{

					lastframesent = minvalue;

					cb::index = minvalue % cb::size;
					cb::start = maxvalue % cb::size;
					cb::end = minvalue % cb::size;
					cb::badframes = maxvalue - minvalue;
					if (minvalue > cb::size) cb::wrote_to_end = true;



					frameready_result = env->CallIntMethod(obj, frameReady_id, cb::index);

					if (frameready_result == 2)
					{
						cancel_save = true;
					}
					else
						if (frameready_result == 1)
						{
							for (int k = 0; k < rowsize; k++) frame[k] = cb::data[cb::index*rowsize + k];
							const jbyte *pframeBuffer = (jbyte *)frame;
							env->SetByteArrayRegion(framearray, 0, len, pframeBuffer);
							env->CallVoidMethod(obj, pushFrame_id, framearray);
							for (int q = 0; q < group::numberofcameras; q++) std::cout << framecounter[q] << " ";
							cout << "st=" << cb::start << " en=" << cb::end;
							std::cout << endl;
						}

					//if camera is last camera
					if (!cancel_save)
					{
						//need to figure out cb::start, cb::end, cb::endex
						if (numberofframes > 0) 
						{
							if (savecounter < numberofframes)
							{
								if (cb::bytes_per_pixel == 2)
								{
									for (int q = 0; q < rowsize; q += 2) {
										saveframe[q+1] = cb::data[cb::index*rowsize + q];
										saveframe[q] = cb::data[cb::index*rowsize + q + 1];
									}
									savefile.write(reinterpret_cast<char*>(saveframe), rowsize);
								}
								else
								{
									for (int q = 0; q < rowsize; q++) {
										saveframe[2 * q + 1] = cb::data[cb::index*rowsize + q];
										saveframe[2 * q] = 0;
									}
									savefile.write(reinterpret_cast<char*>(saveframe), rowsize*2);
								}
								savecounter++;
								
							}
						}
						
					}
					else
					{
						if (numberofframes > 0) {
							savefile.close();
						}


						camera_p->StopGrabbing();
						cb::end = cb::index;
						std::cout << endl << "dll: cancelled save operation..." << endl;


					}
				} 
			}
		}//while
	}
	catch (const GenericException &e)
	{
		// Error handling
		cerr << "An exception occurred." << endl
			<< e.GetDescription() << endl;
		
	
	}

	// Comment the following two lines to disable waiting on exit.
	

	// Releases all pylon resources. 
	pyterminate();
	return 1;
}




JNIEXPORT jint JNICALL Java_gvdecoder_BaslerController_saveToCircularBuffer
(JNIEnv *env, jobject obj) {
	std::cout << "in saveToCircularBuffer dll call. " << endl;
	jclass cls = NULL;
	int exitCode = 1;
	jmethodID frameReady_id = NULL;
	jmethodID pushFrame_id = NULL;
	if (!cb::isInitialized) {
		if (!setCircularBuffer()) return 0;
	}
	cb::index = 0;
	cb::start = 0;
	cb::end = 1;
	cb::wrote_to_end = false;

	PylonInitialize();
	
	try
	{
		cout << endl << "in saveToCicularBuffer try block" << endl;
		CDeviceInfo info;
		info.SetDeviceClass(Camera_t::DeviceClass());
		cout << "before creating camera_p" << endl;
		camera_p = new Camera_t(CTlFactory::GetInstance().CreateFirstDevice(info));
		// Print the model name of the camera.
		cout << "Using device " << camera_p->GetDeviceInfo().GetModelName() << endl;
		camera_p->MaxNumBuffer = 5;
		camera_p->Open();
		if (!setCameraParameters()) { cout << endl << "Error: error setting parameters" << endl; camera_p->Close(); PylonTerminate(); return -1; }
		else cout << endl << "...camera parameters set correctly" << endl;
		camera_p->StartGrabbing();
		CGrabResultPtr ptrGrabResult;
		int counter = 0;
		jsize len = (jint)(cam::width*cam::height);
		jbyteArray framearray = env->NewByteArray(len);
		jint frameready_result = 0;
		bool cancel_save = false;
		cls = env->GetObjectClass(obj);
		if (cls != 0) 
		{
			cout << "before getMethodID call" << endl << endl;
			pushFrame_id = env->GetMethodID(cls, "pushFrame", "([B)V");
			frameReady_id = env->GetMethodID(cls, "frameReady", "(I)I");
			cout << "after getMethodID call" << endl << endl;
		}
		else 
		{
			//shouldn't happen
			cout << endl << "Error: unable to call java class from c++, exiting circular buffer routine." << endl;
			return -1;
		}
		if ((frameReady_id == 0) || (pushFrame_id == 0)) 
		{
			//shouldn't happen
			cout << endl << "Error: unable to get class names from c++ dll, exiting circular buffer routine." << endl;
			return -1;
		}
		while (camera_p->IsGrabbing())
		{
			// Wait for an image and then retrieve it. A timeout of 5000 ms is used.
			camera_p->RetrieveResult(5000, ptrGrabResult, TimeoutHandling_ThrowException);

			// Image grabbed successfully?
			if (ptrGrabResult->GrabSucceeded())
			{
				frameready_result = env->CallIntMethod(obj, frameReady_id,cb::index);
				if (frameready_result == 2)
				{
					cancel_save = true;
				}
				else
					if (frameready_result == 1)
					{
						const jbyte *pImageBuffer = (jbyte *)ptrGrabResult->GetBuffer();
						env->SetByteArrayRegion(framearray, 0, len, pImageBuffer);
						env->CallVoidMethod(obj, pushFrame_id, framearray);
					}

			}
			if (!cancel_save)
			{
				//store in cicular buffer
				const uint8_t *pImageBuffer = (uint8_t *)ptrGrabResult->GetBuffer();
				int start_loc = cb::index*cam::width*cam::height;
				for (int i = 0; i < cam::width*cam::height; i++) cb::data[i + start_loc] = pImageBuffer[i];
				cb::end = cb::index;
				if (cb::wrote_to_end)
				{
					cb::start = cb::index + 1;
					if (cb::start >= cb::size) cb::start = 0;
				}
				cb::index++;
				if (cb::index >= cb::size)
				{
					if (!cb::wrote_to_end) cb::wrote_to_end = true;
					cb::index = 0;
					cb::start = 1;
				}
			}
			else
			{
				camera_p->StopGrabbing();
				cb::end = cb::index;
				std::cout << endl << "dll: cancelled save operation..." << endl;

			}
		}
		camera_p->Close();
	}
	catch (const GenericException &e)
	{
		// Error handling.
		cerr << "An exception occurred." << endl
			<< e.GetDescription() << endl;
		exitCode = 0;
	}
	PylonTerminate();
	return exitCode;
}

JNIEXPORT jint JNICALL Java_gvdecoder_BaslerController_saveImages
(JNIEnv * env, jobject obj, jstring savepath, jint numberofimages, jint exposetime_us) {
	cout << "in save images" << endl;

   jclass cls=NULL;
   jmethodID frameReady_id=NULL;
   jmethodID pushFrame_id = NULL;

	int exitCode = 1;

	const char *str = (*env).GetStringUTFChars(savepath, 0);
	std::string mypath = str;
	cout << "passed path=" << mypath << endl;
	(*env).ReleaseStringUTFChars(savepath, str);

	
	// Before using any pylon methods, the pylon runtime must be initialized. 
	PylonInitialize();
	try 
	{
		CDeviceInfo info;
		info.SetDeviceClass(Camera_t::DeviceClass());
		camera_p = new Camera_t(CTlFactory::GetInstance().CreateFirstDevice(info));
		// Print the model name of the camera.
		 
		cout << "Using device " << camera_p->GetDeviceInfo().GetModelName() << endl;

		// The parameter MaxNumBuffer can be used to control the count of buffers
		// allocated for grabbing. The default value of this parameter is 10.
		cam::exposetime_us = exposetime_us;
		camera_p->MaxNumBuffer = 5;
		camera_p->Open();
		setCameraParameters();
	    cout << "camera frame rate = " << camera_p->AcquisitionFrameRate.GetValue();
		camera_p->StartGrabbing(numberofimages);
		CGrabResultPtr ptrGrabResult;
		int counter = 0;
		jsize len = (jint)(cam::width*cam::height);
		jbyteArray framearray = env->NewByteArray(len);
		jint frameready_result = 0;
		bool cancel_save = false;

		// Camera.StopGrabbing() is called automatically by the RetrieveResult() method
		// when c_countOfImagesToGrab images have been retrieved.

		cls = env->GetObjectClass(obj);
		if (cls != 0) {
			cout << "before getMethodID call" << endl << endl;
			pushFrame_id = env->GetMethodID(cls, "pushFrame", "([B)V");
			frameReady_id = env->GetMethodID(cls, "frameReady", "(I)I");
			cout << "after getMethodID call" << endl << endl;
		}
		while (camera_p->IsGrabbing())
		{
			// Wait for an image and then retrieve it. A timeout of 5000 ms is used.
			camera_p->RetrieveResult(5000, ptrGrabResult, TimeoutHandling_ThrowException);

			// Image grabbed successfully?
			if (ptrGrabResult->GrabSucceeded()) {

				if ((frameReady_id != 0) && (pushFrame_id != 0)) {

					frameready_result = env->CallIntMethod(obj, frameReady_id,-1);
					if (frameready_result == 2) {
						cancel_save = true;
					}
					else
						if (frameready_result == 1) {
							const jbyte *pImageBuffer = (jbyte *)ptrGrabResult->GetBuffer();
							env->SetByteArrayRegion(framearray, 0, len, pImageBuffer);
							env->CallVoidMethod(obj, pushFrame_id, framearray);
						}

				}
				if (!cancel_save) 
				{
				ostringstream os;
				os << mypath << "img";
				if (counter < 10) os << "00000";
				else if (counter < 100) os << "0000";
				else if (counter < 1000) os << "000";
				else if (counter < 10000) os << "00";
				else if (counter < 100000) os << "0";
				os << counter << ".png";
				counter = counter + 1;
				std::string newstr = os.str();
				CImagePersistence::Save(ImageFileFormat_Png, &newstr[0], ptrGrabResult);
				}
				else {
					camera_p->StopGrabbing();
					cout <<endl<< "dll: cancelled save operation..." << endl;
				}

			}
		}
		camera_p->Close();
	}
	catch (const GenericException &e)
	{
		// Error handling.
		cerr << "An exception occurred." << endl
			<< e.GetDescription() << endl;
		exitCode = 0;
	}
	PylonTerminate();
	return exitCode;
}

void setup_circular_buffer() 
{



}

void test()
{
	int msexpose = 1000;
	// The exit code of the sample application.
	int exitCode = 0;

	// Before using any pylon methods, the pylon runtime must be initialized. 
	PylonInitialize();

	try
	{
		//CPylonImage backgroundImage(CPylonImage::Create(PixelType_Mono8, 720, 540));
		//uint8_t* backgroundbuffer = (uint8_t*)backgroundImage.GetBuffer();
		CDeviceInfo info;
		info.SetDeviceClass(Camera_t::DeviceClass());
		// Create an instant camera object with the camera device found first.
		//CInstantCamera camera( CTlFactory::GetInstance().CreateFirstDevice(info));
		Camera_t camera(CTlFactory::GetInstance().CreateFirstDevice(info));
		// Print the model name of the camera.
		cout << "Using device " << camera.GetDeviceInfo().GetModelName() << endl;

		// The parameter MaxNumBuffer can be used to control the count of buffers
		// allocated for grabbing. The default value of this parameter is 10.
		camera.MaxNumBuffer = 5;
		camera.Open();
		camera.GainAuto.SetValue(GainAuto_Off);
		camera.ExposureTime.SetValue((double)msexpose);
		cout<<"frame rate = "<<camera.AcquisitionFrameRate.GetValue();
		// Start the grabbing of c_countOfImagesToGrab images.
		// The camera device is parameterized with a default configuration which
		// sets up free-running continuous acquisition.
		camera.StartGrabbing(c_countOfImagesToGrab);

		// This smart pointer will receive the grab result data.
		CGrabResultPtr ptrGrabResult;

		// Camera.StopGrabbing() is called automatically by the RetrieveResult() method
		// when c_countOfImagesToGrab images have been retrieved.
		while (camera.IsGrabbing())
		{
			// Wait for an image and then retrieve it. A timeout of 5000 ms is used.
			camera.RetrieveResult(5000, ptrGrabResult, TimeoutHandling_ThrowException);

			// Image grabbed successfully?
			if (ptrGrabResult->GrabSucceeded())
			{
				// Access the image data.
				cout << "SizeX: " << ptrGrabResult->GetWidth() << endl;
				cout << "SizeY: " << ptrGrabResult->GetHeight() << endl;
				const uint8_t *pImageBuffer = (uint8_t *)ptrGrabResult->GetBuffer();

				cout << "Gray value of first pixel: " << (uint32_t)pImageBuffer[0] << endl << endl;
				//backgroundbuffer[10] = 100;
				/*
				for (int i = 0; i < 1920; i++) {
					for (int j = 0; j < 1200; j += 10) {
						backgroundbuffer[j * 1920 + i] = pImageBuffer[j * 1920 + i];
					}
				}
				*/
#ifdef PYLON_WIN_BUILD
				// Display the grabbed image.
				// Pylon::DisplayImage(1, ptrGrabResult);
				Pylon::DisplayImage(1, ptrGrabResult);

#endif
			}
			else
			{
				cout << "Error: " << ptrGrabResult->GetErrorCode() << " " << ptrGrabResult->GetErrorDescription() << endl;
			}
		}
		cout << "frame rate = " << camera.ResultingFrameRate.GetValue();
	}
	catch (const GenericException &e)
	{
		// Error handling.
		cerr << "An exception occurred." << endl
			<< e.GetDescription() << endl;
		exitCode = 1;
	}

	// Comment the following two lines to disable waiting on exit.
	
	cerr << endl << "Press Enter to exit." << endl;
	while (cin.get() != '\n');

	// Releases all pylon resources. 
	PylonTerminate();

	//return exitCode;
}

JNIEXPORT void JNICALL Java_gvdecoder_BaslerController_testCamera
(JNIEnv * env, jobject obj) {
	test();
}

JNIEXPORT void JNICALL Java_gvdecoder_BaslerController_sayHello
(JNIEnv * env, jobject obj) {
	cout << "hello from dll";

}