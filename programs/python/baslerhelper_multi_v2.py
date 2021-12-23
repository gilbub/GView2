import os
from register_virtual_stack import Register_Virtual_Stack_MT
import javax.swing
import jarray
from javax.swing import JButton, JInternalFrame, JProgressBar, JPanel, JTextField, JLabel, JToggleButton
from java.awt import GridLayout, BorderLayout
import java.awt.geom
import javax.imageio.ImageIO as imageio


configfilepath="D:\\data\\balsertest\\config.txt"
config_directory="D:\\data\\balsertest\\"
alignment_prefix="align_aug28"

setuptag=0 

def guisetup(evt):
 setup()
 
def guisetupfilter6(evt):
 filt2=gvdecoder.differencefilter6()
 filt2.subnumber=6
 globals()["filt2"]=filt2

def guistart_focus(evt):
 expostime=globals()["exposetime"]
 startfocus(exposetime)

def guistop_focus(evt):
 
 stopfocus()


def guifilter6(evt):
 jh.presentviewer.viewerfilter=globals()["filt2"] 

def guistop_record(evt):
 bas.cancel_save=1
 java.lang.Thread.sleep(100)
 bas.playbackcircularbuffer=1 
 

def guistart_record(evt):
 bas.prepCamera("",1024,1024)
 bas.playbackcircularbuffer=0
 bas.circbuffer()

def guiclose_camera(evt):
 shutdown()

def gui():
 makegui([guisetup,guisetupfilter6,guifilter6])
 filt=gvdecoder.differencefilter2()
 filt.mult=10
 globals()["filt"]=filt


def setup():
 if "hasbeensetup" in globals():
  return
 globals()["hasbeensetup"]=1
 filt2=gvdecoder.differencefilter6()
 filt2.subnumber=6
 globals()["filt2"]=filt2
 bas=gvdecoder.BaslerController(configfilepath)
 bas.gv=gv
 globals()["bas"]=bas
 globals()["exposetime"]=1000
 globals()["focus_on"]=0
 
 bas.prepCamera()
 bas.playbackcircularbuffer=0
 c=camGui(bas)
 globals()["basgui"]=c
 
 camdata=camData(bas.numberofcameras)
 globals()["camdata"]=camdata

 bas.vw.setLocation(5,5)
 bas.vw.setSize(1200,512)
 basgui.frame.setLocation(10,520)
 gv.jythonwindow.iframe.setLocation(250,520)
 gv.jythonwindow.iframe.setSize(500,200)
 

 

def startfocus(t_us):
 if globals()["focus_on"]==0:
  bas.startfocus(t_us)
  globals()["focus_on"]=1

def stopfocus():
 if globals()["focus_on"]==1:
  bas.stopfocus()
  globals()["focus_on"]=0
  
def shutdown():
 bas.closeCamera()

def savedata(filename, number_of_images, t_us):
 if filename.endswith("\\"):
  if os.path.isdir(filename):
    w.print("saving ...")
    bas.saveImages(filename, number_of_images, t_us)
    w.print("done.")
  else:
   w.print("the path given isn't a valid directory")
 else:
   w.print("make sure the directory has two backslashes instead of one, and two at the end")
   
    
#>savedata("d:\\data\\balsertest\\",100,10000)  
#bas.cancel_save=1  
 
def checksanity():
 res=[]
 for x in range(999):
  bas.getCircularBufferFrame(x,bas.ba.arr)
  res.append(bas.ba.arr[2])
 return res

def loadframe(ma,n):
 bas.getCircularBufferFrame(n,bas.ba.arr)
 for y in range(1024):
  for x in range(1024):
   ma.dat.set(0,y,x,bas.ba.arr[y*1024+x])
 ma.vw.JumpToFrame(0)


def ui_change_text(evt):
  w.print("clic")


class camData:
  def __init__(self,ncams):
      self.camparams = []
      self.numberofcameras=ncams
      for i in range(ncams):
       self.camparams.append({})

class camGui(object):
 def __init__(self,controller):
   self.bas=controller
   self.showgui()

 def showgui(self):
  self.frame=JInternalFrame("basler ui",1,1,1,1)
  self.tpanel=JPanel(GridLayout(0,3))
  
  self.tpanel.add(JLabel("cam #"))

  self.camNumberField=JTextField("0")
  self.tpanel.add(self.camNumberField)

  self.setCamNumberButton=JButton("set", actionPerformed=self.setCameraNumber)
  self.tpanel.add(self.setCamNumberButton)

  self.tpanel.add(JLabel("expose(t)"))
  self.exposeField=JTextField("500")
  self.tpanel.add(self.exposeField)
  self.setExposeButton=JButton("set", actionPerformed=self.changeExposeTime)
  self.tpanel.add(self.setExposeButton)
  
  self.tpanel.add(JLabel("gain(db)"))
  
  self.gainValueField=JTextField("0")
  self.tpanel.add(self.gainValueField)

  self.setGainValueButton=JButton("set", actionPerformed=self.setGainValue)
  self.tpanel.add(self.setGainValueButton)

  self.tpanel.add(JLabel("xoffset"))
  
  self.xOffsetField=JTextField("0")
  self.tpanel.add(self.xOffsetField)

  self.xOffsetButton=JButton("set", actionPerformed=self.setXOffsetValue)
  self.tpanel.add(self.xOffsetButton)

  self.tpanel.add(JLabel("yoffset"))
  
  self.yOffsetField=JTextField("0")
  self.tpanel.add(self.yOffsetField)

  self.yOffsetButton=JButton("set", actionPerformed=self.setYOffsetValue)
  self.tpanel.add(self.yOffsetButton)

  self.tpanel.add(JLabel("FPS"))
  self.fpsfield=JTextField("25")
  self.tpanel.add(self.fpsfield)
  self.forceFPSButton=JToggleButton("lock",actionPerformed=self.setFPS)
  self.tpanel.add(self.forceFPSButton)
  self.tpanel.add(JLabel("Save N"))
  self.saveNField=JTextField("300")
  self.tpanel.add(self.saveNField)
  self.forceSaveNButton=JToggleButton("lock", actionPerformed=self.setSaveOnlyN)
  self.tpanel.add(self.forceSaveNButton)
  self.setGainButton=JToggleButton("gain", actionPerformed=self.setGain)
  self.tpanel.add(self.setGainButton)
  self.setTriggerButton=JToggleButton("trigger", actionPerformed=self.setTrigger)
  self.tpanel.add(self.setTriggerButton)
  self.setFilterButton=JToggleButton("filter",actionPerformed=self.setFilter)
  self.tpanel.add(self.setFilterButton)

  self.setCenterButton=JToggleButton("center", actionPerformed=self.setCenter)
  self.tpanel.add(self.setCenterButton)

  self.setMirrorXButton=JToggleButton("flip x", actionPerformed=self.mirrorX)
  self.tpanel.add(self.setMirrorXButton)

  self.setMirrorYButton=JToggleButton("flip y", actionPerformed=self.mirrorY)
  self.tpanel.add(self.setMirrorYButton)

  
  self.runButton=JButton("run", actionPerformed=self.runCamera)
  self.stopButton=JButton("stop", actionPerformed=self.stopCamera)
  self.saveButton=JButton("save", actionPerformed=self.saveData)
  self.tpanel.add(self.runButton)
  self.tpanel.add(self.stopButton)
  self.tpanel.add(self.saveButton)

  #new
  self.brightfieldButton=JButton("b-field", actionPerformed=self.brightfield)
  self.lowfButton=JButton("low-f", actionPerformed=self.lowfluorescence)
  self.run500hzButton=JButton("r-500hz", actionPerformed=self.run500Hz)
  self.tpanel.add(self.brightfieldButton)
  self.tpanel.add(self.lowfButton)
  self.tpanel.add(self.run500hzButton)
  
  self.slow5Button=JButton("5_ms", actionPerformed=self.slow_5)
  self.slow10Button=JButton("10_ms", actionPerformed=self.slow_10)
  self.slow20Button=JButton("20_ms", actionPerformed=self.slow_20)
  self.tpanel.add(self.slow5Button)
  self.tpanel.add(self.slow10Button)
  self.tpanel.add(self.slow20Button)



  #self.tpanel.add(self.npanel)
  self.cpanel=JPanel(BorderLayout())
  self.cpanel.add(self.tpanel,BorderLayout.CENTER)
  bas.save_progressbar=JProgressBar(0,1000)
  self.bpanel=JPanel(GridLayout(0,2))
  self.statusLabel=JLabel("ready")
  self.bpanel.add(self.statusLabel)
  self.bpanel.add(bas.save_progressbar)
  self.cpanel.add(self.bpanel, BorderLayout.PAGE_END)
  self.frame.add(self.cpanel)


  self.frame.pack() 
  self.frame.visible=1
  self.frame.setSize(220,326)
  gv.desktop.add(self.frame)
 

 def brightfield(self,event):
  brightfield()

 def lowfluorescence(self,event):
  lowfluorescence()
 
 def run500Hz(self,event):
  run500Hz()
  
  
 def slow_5(self,event):
    run200Hz()
 
 def slow_10(self,event):
    run100Hz()
 
 def slow_20(self,event):
    run50Hz()


 def setCenter(self,event):
  if self.setCenterButton.isSelected():
   self.bas.setOffsets(-1,-1)
  else:
   v1=int(self.xOffsetField.getText())
   v2=int(self.yOffsetField.getText())
   self.bas.setOffsets(v1,v2)
  

 def mirrorX(self,event):
  mx=self.setMirrorXButton.isSelected()
  my=self.setMirrorYButton.isSelected()
  self.bas.mirror(mx,my)

 def mirrorY(self,event):
  mx=self.setMirrorXButton.isSelected()
  my=self.setMirrorYButton.isSelected()
  self.bas.mirror(mx,my)

 def setCameraNumber(self,event):
  v=int(self.camNumberField.getText())
  self.bas.setActiveCamera(v)

 def setGainValue(self, event):
  v=int(self.gainValueField.getText())
  self.bas.setGainValue(v)

 def setXOffsetValue(self, event):
  v1=int(self.xOffsetField.getText())
  v2=int(self.yOffsetField.getText())
  self.bas.setOffsets(v1,v2)

 def setYOffsetValue(self, event):
  v1=int(self.xOffsetField.getText())
  v2=int(self.yOffsetField.getText())
  self.bas.setOffsets(v1,v2)


 def changeExposeTime(self,event):
  v=int(self.exposeField.getText())
  res=self.bas.setExposeTime(v)
  self.statusLabel.setText(str(res))
  w.print(res)

 def setGain(self,event):
  if self.setGainButton.isSelected():
   self.bas.setGain(2)
  else:
   self.bas.setGain(0)

 def setTrigger(self,event):
  if self.setTriggerButton.isSelected():
    self.bas.setTrigger(1)
  else:
    self.bas.setTrigger(0)

 def setFilter(self,event):
   if self.setFilterButton.isSelected():
     self.bas.ba.vw.viewerfilter=globals()["filt2"]
     self.bas.ba.vw.JavaCustomFilter()
   else:
     self.bas.ba.vw.useviewerfilter=0

 def setFPS(self,event):
  if self.forceFPSButton.isSelected():
    myfps=int(self.fpsfield.getText())
    self.bas.setFrameRate(myfps)
    w.print("set fps")
  else:
    self.bas.setFrameRate(-1)
    w.print("no set fps")
    
 def setSaveOnlyN(self,event):
  if self.forceSaveNButton.isSelected():
     v=int(self.saveNField.getText())
     w.print(v)
     self.bas.save_only_n_frames=1
     self.bas.save_n_frames=v
  else:
    w.print("free run")
    self.bas.save_only_n_frames=0

 def runCamera(self,event):
   w.print("run camera")
   #self.bas.prepCamera("",512,512,3)
   self.bas.prepCamera()
   self.bas.playbackcircularbuffer=0
   basgui.statusLabel.setText("Running...")
   loadallcameras(alignment_prefix)
   
   self.bas.circbuffer()
   applysettings()
   

 def stopCamera(self,event):
   w.print("stop camera")
   self.bas.cancel_save=1
   java.lang.Thread.sleep(100)
   self.bas.playbackcircularbuffer=1 
   basgui.statusLabel.setText("ready")

 def saveData(self,event):
   w.print("saving data")
   bas.write_bytes_as_shorts=0
   path,file=choosefile()
   basgui.statusLabel.setText("saving...")
   ftotal=bas.getCircularBufferStoredFrameNumber()
   fmax=calcmaxstoredframes()-1
   if (ftotal<fmax):
    bas.saveCircBuffer(path+file)
    w.print(ftotal)
   else:
    bas.saveCircBuffer(path+file,0,fmax)
    w.print("saved subset")
    w.print(fmax)
   basgui.statusLabel.setText("ready")
   w.print("done")

def setarray(array,val,st,en):
 for i in range(st,en):
   array[i]=val

def camera_i(camnum,x,y):
 viewmode=bas.ba.viewmode
 if viewmode==0:
   return camnum*bas.xdim*bas.ydim+y*bas.xdim+x
 if viewmode==1:
   return bas.xdim*bas.numberofcameras*y+(camnum*bas.xdim)+x


def setupviewerarrays():
 vw=bas.vw
 le=bas.ba.xdim*bas.ba.ydim
 if vw.normalize==None:
   vw.normalize=jarray.zeros(le,'d')
 if vw.background==None:
   vw.background=jarray.zeros(le,'d')   
 for i in range(le):
   vw.normalize[i]=1.0
  
def rescale(camnum,maxv):
  mx=-1
  mn=maxv+1
  for y in range(bas.ydim):
   for x in range(bas.xdim):
    v=bas.vw.datArray[camera_i(camnum,x,y)]
    if v>mx:
     mx=v
    if v<mn:
     mn=v
  sc=(1.0*maxv)/(mx-mn)
  for y in range(bas.ydim):
   for x  in range(bas.xdim):
    i=camera_i(camnum,x,y)
    bas.vw.background[i]=mn
    bas.vw.normalize[i]=sc 

 
     
  
def applysettings():
  java.lang.Thread.sleep(500)
  loadallcameras(alignment_prefix)
  w.print("no usersettings")
  #bas.switchCameraOrder(0,1)     
  #bas.switchCameraOrder(3,5)  
  #bas.switchCameraOrder(0,6)  
  #bas.switchCameraOrder(5,6)  
  #bas.switchCameraOrder(2,5)      
   

def cameraoffset(camnumber,xoffset,yoffset):
  bas.setActiveCamera(camnumber)
  

def zoomCamera(n):
  
  jp=bas.vw.jp
  if (n<0):
   jp.offsetx=0
   bas.vw.AUTOSCALE=1
   bas.vw.setViewScale()
   return
  else:
   scx=(1.0*jp.getWidth())/bas.xdim
   scy=(1.0*jp.getHeight())/bas.ydim
   sc=scx
   if (scy<scx):
     sc=scy
   jp.offsetx=int(-bas.xdim*sc*n)
   jp.SetScale(sc)
   #jp.offsetx=-1*(int)(x*1.0/viewScale-jp.getWidth()/2.0);
 
def crosshair(xc,yc):
  jp=bas.vw.jp
  tx=xc*jp.scale
  ty=10
  bx=tx
  by=bas.ydim*jp.scale-10
  res=[]
  for i in range(bas.numberofcameras):
   dec=gvdecoder.Decoration(java.awt.Color.GREEN,0,0,"hello",java.awt.geom.Rectangle2D.Double(i*bas.xdim*jp.scale,1,bas.xdim*jp.scale-1,bas.ydim*jp.scale-1),0)
   dec_v=gvdecoder.Decoration(java.awt.Color.RED,0,0,"hello",java.awt.geom.Line2D.Double(tx+i*(bas.xdim*jp.scale),ty,bx+i*(bas.xdim*jp.scale),by),0)
   dec_h=gvdecoder.Decoration(java.awt.Color.RED,0,0,"hello",java.awt.geom.Line2D.Double(10+i*(bas.xdim*jp.scale),yc*jp.scale,i*(bas.xdim*jp.scale)+bas.xdim*jp.scale-10,yc*jp.scale),0)
   res.append(dec)
   res.append(dec_v)
   res.append(dec_h)
  jp.decorations=res

def targets():
 crosshair(bas.xdim/2, bas.ydim/2)

def notargets():
   jh.presentviewer.jp.decorations=[]
 

def borders():
 res=[]
 jp=bas.vw.jp
 for i in range(bas.numberofcameras):
  dec=gvdecoder.Decoration(java.awt.Color.GREEN,0,0,"hello",java.awt.geom.Rectangle2D.Double(i*bas.xdim*jp.scale,1,bas.xdim*jp.scale-1,bas.ydim*jp.scale-1),0)
  res.append(dec)
 jp.decorations=res
 

def calcframedif(ma):
   off=0
   on=0
   roff=[]
   ron=[]
   for z in range(1,ma.zdim):
    for y in range(ma.ydim):
     for x in range(ma.xdim):
      if ma.dat.get(z-1,y,x)>0 and ma.dat.get(z,y,x)<-5:
        off+=1
      if ma.dat.get(z-1,y,x)>5 and ma.dat.get(z,y,x)<0:
        on+=1
    roff.append(off)
    ron.append(on)
    w.print(z)
   return roff,ron

def findmax(m1,m2):
  co=gvdecoder.Correlate()
  mv=-1
  offx=0
  offy=0
  cx=m1.xdim/2
  cy=m1.ydim/2
  for x in range(-cx,cx):
   w.print(x)
   for y in range(-cy,cy):
    v=co.value(m1,m2,x,y)
    if v>mv:
     mv=v
     offx=x
     offy=y
  return offx,offy,mv 

def autofindoffset(refcamnumber):
 mref=Matrix(1,bas.ydim, bas.xdim)
 ma=Matrix(mref)
 bas.getSingleCameraFrame(mref,refcamnumber)
 mv=0
 for i in range(bas.numberofcameras):
  bas.getSingleCameraFrame(ma,i)
  xo,yo,mv=findmax(mref,ma)
  camdata.camparams[i]["xoffset"]=xo
  camdata.camparams[i]["yoffset"]=yo

def autooffset():
 for i in range(bas.numberofcameras):
  bas.setActiveCamera(i)
  tmp=getparameters(i)

  xo= -camdata.camparams[i]["xoffset"] + tmp[2]
  yo= -camdata.camparams[i]["yoffset"] + tmp[3]

  xo=xo-(xo%4)
  yo=yo-(yo%2)
  w.print("xo=%d yo=%d"%(xo,yo))
  
  if ((bas.xdim+xo>=0) and (bas.xdim+xo<728) and (bas.ydim+yo>=0) and (bas.ydim+yo<540)):
    bas.setOffsets(xo,yo)

def suggestmaxsize():
 maxyoffset=-1000
 maxxoffset=-1000
 for i in range(camdata.numberofcameras):
  xo=java.lang.Math.abs(camdata.camparams[i]["xoffset"])
  yo=java.lang.Math.abs(camdata.camparams[i]["yoffset"])
  if xo>maxxoffset:
   maxxoffset=xo
  if yo>maxyoffset:
   maxyoffset=yo
 return 720-maxxoffset, 540-maxyoffset

 
 
def saveimage():
 path,file=choosefile()
 jh.presentviewer.jp.saveImage(path+file)

def setdimensions(xdim,ydim):
 bas.cf.map["width"]=xdim
 bas.cf.map["height"]=ydim
 bas.cf.write()
 #java.lang.Thread.sleep(500)
 #bas.prepCamera()
 

def sanity():
 bas.prepCamera()


source_dir_fiji = "D:/data/balsertest/fijiinput/"
target_dir_fiji = "D:/data/balsertest/fijioutput/"
transf_dir_fiji = "D:/data/balsertest/fijitransforms/"
refr_img_fiji   = "image6.png"

def exportallimages():
  bas.vw.setVisualScale(1.0)
  bi=bas.vw.jp.getImage()
  for i in range(bas.numberofcameras):
   bs=bi.getSubimage(i*bas.xdim,0,bas.xdim,bas.ydim)
   file="image%d.png"%i
   fname=java.io.File(source_dir_fiji+file)
   imageio.write(bs,"png",fname)

def parseoffsets(filename):
  f=open(filename,"r")
  str=f.readline()   
  f.close
  si=str.find("data=")
  sq1=str.find("\"",si)
  sq2=str.find("\"",sq1+1)
  numstr= str[sq1+1:sq2]
  w.print(numstr)
  n1=0
  n2=0
  n3=0
  tmp=numstr.split(" ")
  if len(tmp)==3:
   n1,n2,n3=tmp
  else:
   n2,n3=tmp
  return int(round(float(n2))), int(round(float(n3)))

def findoffsetrange():
 xmax=-1000
 xmin=1000
 ymax=-1000
 ymin=1000
 res=[]
 for i in range(bas.numberofcameras):
   fname="image%d.xml"%i
   xo,yo=parseoffsets(transf_dir_fiji+fname)
   res.append((xo,yo))
   if (xo>xmax):
    xmax=xo
   if (xo<xmin): 
    xmin=xo
   if (yo>ymax):
    ymax=yo
   if (yo<ymin):
    ymin=yo
 xs=(xmax+xmin)/2
 ys=(ymax+ymin)/2
 for i in range(bas.numberofcameras):
   xo,yo=res[i]
   camdata.camparams[i]["xoffset"]=xo-xs
   camdata.camparams[i]["yoffset"]=yo-ys
 return res
  
def genimages(bi):
 for i in range(7):
  bs=bi.getSubimage(i*10,0,512,512)
  file="image%d.png"%i
  fname=java.io.File(source_dir_fiji+file)
  imageio.write(bs,"png",fname)
    

def fijiregister():
  use_shrinking_constraint=1
  p = Register_Virtual_Stack_MT.Param()
  p.sift.maxOctaveSize = 1024
  p.minInlierRatio=0.05
  p.registrationModelIndex=0
  Register_Virtual_Stack_MT.exec(source_dir_fiji, target_dir_fiji, transf_dir_fiji, refr_img_fiji, p, use_shrinking_constraint)

def testpattern(ma):
 ma.dat.set(0)
 for i in range(7):
   for x in range(-25,25):
    for y in range(-25,25):
     m=100
     ch=10
     ma.dat.set(0,y+i*ch+25,x+i*256+i*ch+25,m)
     ma.dat.set(0,y+i*ch+50,x+i*256+i*ch+75,m)
     ma.dat.set(0,y+i*ch+120,x+i*256+i*ch+125,m)
     ma.dat.set(0,y+i*ch+180,x+i*256+i*ch+125,m)

#method for autooffset cameras
# 1) grab single frame with n camera subimages. Stop acquisition
# 2) exportallimages() (dumps n images into source_dir_fiji)
# 3) fijiregister() (runs plugin)
# 4) findoffsetrange() (populates camdata.camparams[]{} with xoffset and yoffset pairs based on plugin output
# 5) potentially run suggestmaxsize() to confirm that image dimensions are compatible with offsets
# 6) Start acquisition
# 7) autooffset()
      

def getparameters(camnumber):
  #integers: w, h, xoffset, yoffset, flipx, flipy, trigger, triggerdelay, expose, gain
  #          0  1  2        3        4      5      6        7             8       9
  tmp=jarray.zeros(10,"i")
  bas.getParameters(camnumber,tmp)
  bas.setActiveCamera(camnumber)
  sn = bas.getSerialNumber()
  w.print("sn    = %s"%sn) 
  w.print("w,h   = %d,%d"%(tmp[0],tmp[1]))
  w.print("xo,yo = %d,%d"%(tmp[2],tmp[3]))
  w.print("mx,my = %d,%d"%(tmp[4],tmp[5]))
  w.print("tr,td = %d,%d"%(tmp[6],tmp[7]))
  w.print("e,g   = %d,%d"%(tmp[8],tmp[9]))
  return tmp


def saveparameters(camnumber,filename):
  tmp=jarray.zeros(10,"i")
  bas.getParameters(camnumber,tmp)
  f=open(filename,"w")
  bas.setActiveCamera(camnumber)
  sn=bas.getSerialNumber()
  f.write("serialnumber = %s\n"%sn)
  f.write("x_offset = %d\n"%tmp[2])
  f.write("y_offset = %d\n"%tmp[3])
  f.write("x_mirror = %d\n"%tmp[4])
  f.write("y_mirror = %d\n"%tmp[5])
  f.close()
  
def parse_int(str,val):
  i=str.index(val)
  e=str.index("=",i)
  n=str.index("\n",e)
  nv=str[e+1:n]  
  return int(nv)

def parse_str(str,val):
  i=str.index(val)
  e=str.index("=",i)
  n=str.index("\n",e)
  nv=str[e+1:n]  
  return nv.strip()

def readparameters(camnumber,filename):
  tmp=jarray.zeros(10,"i")
  f=open(filename,"r")
  str=f.read()
  f.close()
  sn=parse_str(str,"serialnumber") 
  bas.setPosition(sn,camnumber)
  bas.getParameters(camnumber,tmp)
  x_offset=parse_int(str,"x_offset")
  y_offset=parse_int(str,"y_offset")
  x_mirror=parse_int(str,"x_mirror")
  y_mirror=parse_int(str,"y_mirror")
  tmp[2]=x_offset
  tmp[3]=y_offset
  tmp[4]=x_mirror
  tmp[5]=y_mirror
  bas.setParameters(camnumber,tmp)
  return x_offset,y_offset,x_mirror,y_mirror

def saveallcameras(prefix):
 for i in range(bas.numberofcameras):
  filename="%s%s_%d.txt"%(config_directory,prefix,i)
  w.print(filename)
  saveparameters(i,filename)
    

def loadallcameras(prefix):
 for i in range(bas.numberofcameras):
  filename="%s%s_%d.txt"%(config_directory,prefix,i)
  readparameters(i,filename)

def loadcamera(n,prefix):
  filename="%s%s_%d.txt"%(config_directory,prefix,n)
  readparameters(n,filename)
    


def autocenter():
 for i in range(bas.numberofcameras):
    bas.setActiveCamera(i)
    bas.setOffsets(-1,-1)

def autooffset_n(i):
  bas.setActiveCamera(i)
  tmp=getparameters(i)

  xo= -camdata.camparams[i]["xoffset"] + tmp[2]
  yo= -camdata.camparams[i]["yoffset"] + tmp[3]
  xo=xo-(xo%4)
  yo=yo-(yo%2)
  w.print("xo=%d yo=%d"%(xo,yo))
  
  if ((bas.xdim+xo>=0) and (bas.xdim+xo<728) and (bas.ydim+yo>=0) and (bas.ydim+yo<540)):
    bas.setOffsets(xo,yo)

def fixalignment():
  jh.presentviewer.jp.decorations=[]
  w.print("resetting to center of sensor")
  autocenter()
  w.print("exporting images")
  exportallimages()
  w.print("registering images using fiji")
  fijiregister() 
  w.print("extracting results from fiji transform files")
  java.lang.Thread.sleep(500)
  findoffsetrange()
  java.lang.Thread.sleep(500)
  w.print("applying calculated offset values")
  autooffset()
  targets()
  w.print("done")

def align(step):
 if step==1:
   jh.presentviewer.jp.decorations=[]
   w.print("resetting to center of sensor")
   autocenter()
 if step==2:
   w.print("exporting images")
   exportallimages()
 if step==3:
   w.print("registering images using fiji")
   fijiregister() 
 if step==4:
   findoffsetrange()
 if step==5:  
   autooffset()
   targets()
 

def saveSerialNumbers():
 res=[] 
 for i in range(bas.numberofcameras):
   bas.setActiveCamera(i)
   res.append(bas.getSerialNumber())
 globals()["serialnumbers"]=res   


def switchCameraPositions():
  for i in range(len(serialnumbers)):
    s=serialnumbers[i]
    n=i+1
    bas.setPosition(s,n%7)


def testreload(str):
 loadallcameras(str)
 loadallcameras(str)

def dn():
 vw=jh.presentviewer
 max=-1
 min=260
 for i in range(len(vw.datArray)):
  v=vw.datArray[i]
  if v>max:
   max=v
  if v<min:
   min=v
 return min,max
 
def copyroi(roinum=0):
 roic=[(255,125,50),(0,255,0),(0,0,255),(255,255,0),(0,255,255),(255,0,255),(125,0,200)]
 rois=jh.presentviewer.jp.rois
 r=rois[0].poly.getBounds()
 fx=r.x - bas.xdim*roinum
 fy=r.y
 lx=r.x+r.width -bas.xdim*roinum
 ly=r.y+r.height
 rois
 for i in range(bas.numberofcameras):
  if i!=roinum:
   nr=gvdecoder.ROI(bas.xdim*bas.numberofcameras,bas.ydim,fx+(i*bas.xdim),fy,lx+(i*bas.xdim),ly)
   mc=roic[i]
   nr.color=java.awt.Color(mc[0],mc[1],mc[2])
   nr.vi=jh.presentviewer
   rois.add(nr)

def gain(val):
 for i in range(bas.numberofcameras):
   bas.setActiveCamera(i)
   bas.setGainValue(val)

#exposure(950) for 1000 fps at 128x128
#must run this while cameras are OFF or desynchronizes cameras.
def exposure(val): 
 for i in range(bas.numberofcameras):
   bas.setActiveCamera(i)
   bas.setExposeTime(val)

def trigger(val): 
 for i in range(bas.numberofcameras):
   bas.setActiveCamera(i)
   bas.setTrigger(val)

def setupArduino():
  arduino=gvdecoder.ArduinoCom()
  globals()["arduino"]=arduino
  arduino.initialize()
  arduino.writeData('b')

def test1000():
 exposure(950)
 trigger(1)
 basgui.runCamera(None) 
 java.lang.Thread.sleep(1000)
 arduino.writeData('0')

def run1000():
 bas.cancel_save=0
 exposure(850)
 trigger(1)
 basgui.runCamera(None) 
 java.lang.Thread.sleep(1000)
 arduino.writeData('1')
 
 
def run500(): 
 bas.cancel_save=0
 gain(33)
 exposure(4950)
 trigger(1)
 basgui.runCamera(None) 
 java.lang.Thread.sleep(1000)
 arduino.writeData('5')
 
def run100(): 
 bas.cancel_save=0
 gain(33)
 exposure(9950)
 trigger(1)
 basgui.runCamera(None) 
 java.lang.Thread.sleep(1000)
 arduino.writeData('6')
 
def run50(): 
 bas.cancel_save=0
 gain(33)
 exposure(19950)
 trigger(1)
 basgui.runCamera(None) 
 java.lang.Thread.sleep(1000)
 arduino.writeData('7')
 
 



def run2000():
 gain(33)
 exposure(1950)
 trigger(1)
 basgui.runCamera(None) 
 java.lang.Thread.sleep(1000)
 arduino.writeData('2')

def run5000():
 gain(33)
 exposure(4950)
 trigger(1)
 basgui.runCamera(None) 
 java.lang.Thread.sleep(1000)
 arduino.writeData('2')

def test2000():
 exposure(1950)
 trigger(1)
 basgui.runCamera(None) 
 java.lang.Thread.sleep(1000)
 arduino.writeData('4')
 
def freeruntest():
 trigger(0)
 basgui.runCamera(None) 
 java.lang.Thread.sleep(1000)
 arduino.writeData('4')
 
def ledon():
 arduino.writeData('l')

def ledoff():
 arduino.writeData('o')

def blink():
 arduino.writeData('b')
 
  
  
def calcmaxstoredframes():
  _block=bas.xdim*bas.ydim*bas.numberofcameras*bas.bytes_per_pixel
  return 2**31/_block


def traceview(zs,ze,fs,fe):
  jh.presentviewer.nav.np.ROI_width=20
  ma=Matrix()
  ma.initialize(bas.vw.im,zs,ze,0,400,fs*400,fs*400+(fe-fs+1)*400)
  globals()["ma"]=ma
  ma.show(gv,"ma")

def traceview_data(zs,ze,fs,fe):
  jh.presentviewer.nav.np.ROI_width=20
  ma=Matrix()
  ma.initialize(jh.presentviewer.im,zs,ze,0,400,fs*400,fs*400+(fe-fs+1)*400)
  globals()["ma"]=ma
  ma.show(gv,"ma")

def runcamera():
 basgui.runCamera(None)
  

fdelay=200
def brightfield():
  bas.cancel_save=0
  trigger(0)
  java.lang.Thread.sleep(fdelay)
  gain(0)
  java.lang.Thread.sleep(fdelay)
  exposure(1950)
  java.lang.Thread.sleep(fdelay)
  ledon()
  java.lang.Thread.sleep(fdelay)
  runcamera()

def lowfluorescence():
  bas.cancel_save=0
  trigger(0)
  java.lang.Thread.sleep(fdelay)
  gain(33)
  java.lang.Thread.sleep(fdelay)
  ledoff()
  java.lang.Thread.sleep(fdelay)
  exposure(50000)
  java.lang.Thread.sleep(fdelay)
  runcamera()
  #self.runCamera(None)
 
def run500Hz():
  bas.cancel_save=0
  gain(33)
  ledoff()
  java.lang.Thread.sleep(500)
  run2000()

def run200Hz():
  bas.cancel_save=0
  gain(33)
  ledoff()
  java.lang.Thread.sleep(500)
  run200()

def run100Hz():
  bas.cancel_save=0
  gain(33)
  ledoff()
  java.lang.Thread.sleep(500)
  run100()

def run50Hz():
  bas.cancel_save=0
  gain(33)
  ledoff()
  java.lang.Thread.sleep(500)
  run50()



def sn():
  tmp=getroidata()
  v=tmp[0]
  return (max(v)-min(v))/max(v)


def ave(tr):
 s=0
 for i in range(len(tr)):
   s+=tr[i]
 return s*1.0/len(tr)

def sacconi():
  tmp=getroidata()
  res=[]
  for i in range(len(tmp)):
    tr=tmp[i]
    a=ave(tr)
    nt=[]
    for j in range(len(tr)):
      v=(1.0*tr[j])/a
      nt.append(v)
    res.append(nt)
  newgraph(res[0])
  for p in range(1,len(res)):
   addgraph(res[p])
  return res

  
  