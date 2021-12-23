The software in these directories is used to  run and display data from 7 machine
vision cameras,  register (autoalign) the cameras, save data, and analyse the data from 
the lightsheet ('SPEED') microscope. The microscope uses 7 Basler cameras as well as an 
Arduino Uno board. The cameras communicate with the software via a dynamic link library (dll)
that uses java native interface (JNI) calls.  The software runs on windows computers (due to 
use of a windows dll). The java based software uses Jython (a java port of python) to perform
various operations (Jython is included in the CDjars directory). The main scripts for camera 
control and analysis are in the python directory.

The program has the following dependencies that have to be downloaded  on the users machine,
and the corresponding directories in GView64.bat and compile64.bat have to be changed
to reflect the directories of these packages:

 1) Fiji (or, Reconstruct_Reader-2.0.3.jar from a fiji install)
 2) Processing ( https://processing.org/download version 3.5.3)  and https://github.com/jdf/peasycam
 3) Java development kit jdk1.8
 
The .bat 
set jars=D:/CDjars/
set javahome=c:\CD\jdk1.8
set fijihome=C:/CD/fiji-win64/Fiji.app/plugins/
set processinghome=C:/CD/processing/
set processinge=C:/CD/processing_extra/

In addition:
 1) Microsoft Visual Studio Community 2019 is needed to compile the dll 
    for camera control (the code in the 'c++' directory).
 2) Basler camera drivers and SDK need to be downloaded from Basler.com
 3) The Arduino SDK has to be downloaded and installed.

Running the software:
 1) Open a dos command window in the CD/programs/java directory and type 'GView64'<enter>.
 2) After the main application opens, open a jython window (top menu: scripts->jython window)
 3) In this new window, open the control script ('script' tab, followed by clicking the 'load' button)
    open 'baslerhelper_multi_v2.py'
 4) Click 'run all'
 5) type 'setup() in the text entry bar at the bottom of the jython window and hit 'enter'.

