set jars=D:/CDjars/
set javahome=c:\CD\jdk1.8
set fijihome=C:/CD/fiji-win64/Fiji.app/
set processinghome=C:/CD/processing/
set processinge=C:/CD/processing_extra/
set gvdecoderhome=D:/CD/programs/
%javahome%\bin\java -Xms3000m -Xmx12G  -classpath   "%gvdecoderhome%gvdecoder/;%jars%jsci-core.jar;%jars%controlsfx-8.0.5.jar;%jars%epsgraphics.jar;%jar%jaicodec.jar;%jars%jai_core.jar;%jars%jcodec-0.2.1.jar;%jars%jcodec-javase-0.2.1.jar;%jars%jimStlMeshImporterJFX.jar;%jars%jython-standalone-2.7.0.jar;%jars%myptolemy.jar;%jars%RXTXcomm.jar;%jars%waterlooFX.jar;%jars%waterlooFX-0.8-SNAPSHOT.jar;%jars%jimStlMeshImporterJFX.jar;%fijihome%plugins/*;%fijihome%jars/*;%fijihome%jars/bio-formats/*;%processinghome%core/library/*;%processinge%*;." gvdecoder/GView
PAUSE
 
 