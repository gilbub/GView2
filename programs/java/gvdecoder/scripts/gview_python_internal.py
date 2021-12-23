import gvdecoder.Matrix as Matrix
import JSci.maths.ArrayMath as array
import JSci.maths.EngineerMath as engineermath
import gvdecoder.PowerSpectrum
import gvdecoder.function
import os
import os.path
import gvdecoder.ROI
import gvdecoder.trace
import pickle
#import Gnuplot as Gnuplot
import gvdecoder.traceView
import gvdecoder.ImageUtils
#import org.jibble.epsgraphics
import jarray
import gvdecoder.ViewerParams
import gvdecoder.trace.TraceParams
import gvdecoder.trace
import gvdecoder.NavigatorGraphParams
import gvdecoder.ProgressDisplayer
import java.lang.Thread
import java.lang.Runtime
import java.io
#import gvdecoder.quickSVG
import struct
#import gvdecoder.ColorFilter
#import javax.media.jai.iterator.RandomIterFactory

#imagedir="scratch//"
imagedir=gv.ph.prp.getProperty("TempImages dir")+"\\"

gnuplotexecutable=gv.ph.prp.getProperty("gnuplotexecutable")+"\\pgnuplot.exe"

slow_loop=0
def progress(val):
  if (slow_loop):
    java.lang.Thread.sleep(100)
  return gvdecoder.ProgressDisplayer.getProgressDisplayer().displayProgress(val)


overviewhelpstring="""

"""

matrixhelpstring="""
This is a long rambling discussion of the matrix functions and how to call them
including line breaks"""

propertieshelpstring="""
System and class properties:
  
  GView uses system properties (contained in a java.util.properties object) 
  and 'class properties' (contained in static data structures for a few 
  classes) to hold user changeable parameters. Only the system properties can
  be saved and are automatically loaded the next time the program is run. 
  
  System properties mostly contain information on directories. The data is saved in
  a file called 'viewer.cfg' in the base directory ('decoder') of the program. It is 
  automatically loaded each time the program is run.
  
  'Class' properties are mostly toggles for various program behaviours. These are held
  in  trace_properties,viewer_properties,navigator_properties.
    viewer_properties holds variables dealing with operation of the movie viewer.
    navigator_properties holds variables dealing with the navigator graph object.
    trace_properties holds variables that deal with individual traces in the navigator graph.
   
  All public variables (including but not limited to the ones discussed above) are directly 
  accessible by Jython, and can be set by user scripts.

 Usage:
    
   To see all the properties listed, with their values, type 'property_show_all()'
 
   For system properties, type help 'topic name':
     property_system_show()
     property_system_get()
     property_system_set()
     property_system_save()
   System properties must be set indirectly using a quoted string for the variable name and
   a value. ie property_system_set('framerate',15). Note that if the value is a directory name
   it must be a string (enclosed in quotes).
   

   For other properties, properties can be viewed by typing 'property_show(prop.classname), 
   where classname is either viewer_properties, navigator_properties, or trace_properties. Do
   not use quotes as these are classes not strings.
   Properties can be set directly for these (ie trace_properties.FlipData=1)
"""

helpdict={'clear':'\n gv.jv.clear() clears the console',
          'plot': '\n Low level access to graphs. For simplified access, try \"help graph\" \n. jh.quickPlot(xs,ys) plots xy plot\n jh.pl.addPlot(xs,ys) adds a plot\n jh.pl.addPlot(ys) adds a plot assumes same xs\n jh.pl.p.clear() clears all plots\n jh.pl.newPlot(xs,ys) replaces plot\n jh.quickPlot(ys,start,end,boolean logplot) plots ys in plot\n listplots() lists all plot titles\n findplot(title) sets jh.pl to the desired plot (must spell out \"plot=1\" etc.) \n Change the way the graph is drawn by setting jh.plot_connected(= 1 or 0) and jh.plot_marker (=\"none\",\"points\",\"dots\")\n graph2JPG(filename), graph2BMP(filename), graph2TIFF(filename), graph2EPS(filename,dpi=300) plot the image to the scratch dir\n\n The scratch dir is variable \"imagedir\" in this script.\n See also gp_plot',
          'dir':  '\n os.listdir(path)',
          'traceview':'\n setup with tr=testtraceview(gv) in traceview.py \n findstimtimes(ma,x=2,y=16) returns array of stimtimes\n tr.setGlobalTimes(times) puts timestamps in every trace\n tr.setAPStarts(int, int, float[])\n tr.setAPEnds(int,int, float[])\n tr.setLabels(int,int,float[], String[])\n tr.setLabels(int,int, float[], float[])',  
          'save': '\n savexys(xs[],ys[],filename) (load with readxys(filename) or see pickle or graph)',
          'load': '\n readxys(filename) (save with savexys(xs[],ys[],filename) or see pickle or graph)',
          'pickle': '\n outfile=open(\"out.pickle\",\"w\"), pickle.dump(bigdict,outfile), outfile.close()\n file=open(\"out.pickle\",\"r\"), newdata=pickle.load(file), file.close()',
          'find'  : '\n find(\"text\") searches the present loaded script for text. See doc(find)',
          'search': '\n See find',
          'cast'  : '\n ty tofloat()',
          'casting':'\n try tofloat()',
          'ruler'  :'\n try ruler_sum()',
          'roi':    '\n Try getroidata(vw[0],0), roiData',
          'java':   '\n In your scripts, enclose java code snippets with \"at symbol java myclassname\" and \"at symbol /java" and ensure this is not the first line of your script.\n  Call your routine through myclassname.myfunction().',
          'jai':    '\n see jai_getMatrix, ja_getFilteredPixel, jai_addLookupFilter, jai_addColorFilterTable, jai_addLookupFilterTable, jai_resetIterator',
          '.':     '\n shortcuts for present viewer: \n .v (\"b\",\"r\",\"n\",\"s\",(p),(p,q),(p,q,r)) =viewer background, raw, normalize, scale, (frame),(point),(zoom)\n .r (\"p\",\"d\",\"a\",\"c\",\"l\",\"s\", (x1,y1,x2,y2) = roi process, delete, delete all, copy, load, scale, (makeroi)\n usage: .v 5 (viewer jump to frame 5); .v b (viewer background subtract); .r 100,100,110,110 (make roi)',
          'luts':  '\n see loadlut: loadlut(lutname) where lutname is a file in your LUTs directory, or savelut(filename).',
          'restitution': '\n Try \"import scripts.restitution as rc\" followed by \"help rc\".' 
          
          
      }
      
      

  
def finddoc():
  #called by helptopics
  funcs=globals().keys()
  results=[]
  for i in funcs:
   if (hasattr((eval(i)),"__doc__")):
     doc=getattr((eval(i)),"__doc__")
     if (doc!=None): 
       results.append(i)
  return results 

def helptopics():
  #called by help
  res=helpdict.keys()
  res.append("matrix")
  res.append("overview")
  res.append("properties")
  funcs=dir()
  res.extend(finddoc())
  res.sort() 
  pretty=""
  l=len(res)
  for j in range(l):
    pretty=pretty+res[j].center(20)+"\t"
    if (j%3==0) and (j>0):
      pretty+="\n" 
  return "\nAvailable help topics (type \"help topicname\" for details):\n"+pretty+"\n\n For general topics try \"help overview\", \"help matrix\", or \"help properties\"."  


def help(querry="none"):
  """The help function searches for information on topics."""
  if (querry=="properties"):
    return propertieshelpstring
  if (querry=="matrix"):
    return matrixhelpstring
  if (querry=="overview"):
    return overviewhelpstring
  if (helpdict.has_key(querry)):
    return helpdict[querry]
  try:
    str=querry+".__doc__"
    w.print(eval(str))
    return
  except:
    w.print("documentation not found")  
    return
  return "not found"  
  
def doc(function):
  """ Returns documentation, if it exists for a given loaded function. """
  try:
    return function.__doc__  
  except:
   w.print("the documentation for that command wasn't found")
  
lasttextposition=0
def find(text):
  """ find(\"text\") searches for text in the script window. 
  
      Only the text in the present loaded script window is searched. 
      The position of the window is changed so that the text appears near 
      the bottom. Re application of find(\"text\") will cause the
      window to scroll to the next location. \n Example: find(\"auto\"). """
  global lasttextposition
  pos=lasttextposition+1
  script=gv.jv.editor.getText()
  pos=script.find(text,pos)
  if (pos==-1):
    pos=0
    lasttextposition=pos
    return "not found"
  lasttextposition=pos
  gv.jv.editor.setCaretPosition(pos)


#######################################
# Java Advanced Imaging functions (JAI)
#######################################
jai_iter=None

def jai_resetiterator():
  globals()["jai_iter"]=None

def jai_getFilteredPixel(x,y,band=0):
  """
      obtain pixel values from the viewers imagepanel. 
       usage:
        pixval=jai_getFilteredPixel(10,10,2)
       here the last parameter is the color band (r=0,g=1,b=1, default 0)
       NOTE: call jai_resetiterator() if this image is newly selected
  """
  iter=globals()["jai_iter"]
  if iter==None:
    iter=javax.media.jai.iterator.RandomIterFactory.create(jh.presentviewer.jp.filtered,None)
  val=iter.getSample(x,y,band)
  globals()["jai_iter"]=iter
  return val

def jai_getMatrix(xdim,ydim):
  """
     returns a matrix object populated with data from the viewerws imagepanel.
     usage:
      ma=jai_getMatrix(80,80)
     Note: x,y dimensions should be exactly equal to that of the image panel 
  """
  jai_resetiterator()
  ma=Matrix(1,xdim,ydim)
  for x in range(ma.xdim):
   for y in range(ma.ydim):
     ma.dat.set(0,y,x,jai_getFilteredPixel(x,y))
  return ma

def jai_addLookupFilterTable(lookuptable):
 """
    add a lookup filter (256 element int array)
    see jai_addLookupFilter()
 """
 jh.presentviewer.jp.myfilt.addLookupFilter(lookuptable)

def jai_addColorFilterTable(lookuptable):
 """
    add a color lookup table ([r,g,b], where r,g & b are each 256 element int arrays)
 """
 jh.presentviewer.jp.myfilt.addColorFilter(lookuptable)
 
def jai_addLookupFilter(x1,y1,x2,y2):
  """
    adds a lookup table constructed from 3 line segments: 0,0 to x1,y1, x1,y1 to x2,y2, and
    x2,y2 to 256,256)
    usage:
      jai_addLookupFilter(75,25,150,200)
  """
  res=[]
  #first leg=0,0, to x1,y1
  m1=float(y1)/float(x1)
  leg1=[m1*x for x in range(x1)]
  m2=float(y2-y1)/float(x2-x1)
  leg2=[m2*(x-x1)+leg1[x1-1] for x in range(x1,x2)]
  m3=float(256-y2)/(256-x2)
  leg3=[m3*(x-x2)+leg2[x2-x1-1] for x in range(x2,256)]
  reals= leg1+leg2+leg3
  for r in reals:
   res.append(int(r))
  jai_addLookupFilterTable(res)

###################################
# Properties
###################################

def property_system_show():
  en=gv.ph.prp.propertyNames()
  while en.hasMoreElements():
    key=en.next()
    w.print("%s = %s"%(key,gv.ph.prp.get(key)))

def property_system_get(key):
  """ Gets the value of a known system property.
      Usage: property_system_get('key') (note that 'key' is a string in quotes).
      Type 'help properties' for a general overview.
  """
  return gv.ph.prp.get(key)

def property_system_set(key,value):
  """ Sets the value of a known system property.
        Usage: property_system_set('key',value) (note that 'key' is a string in quotes).
        Notes: All boolean values are represented in Jython as a 0(False) or 1(True).
               You must save the properties (property_system_save()) in order to have
               them take effect next load time.   
      Type 'help properties' for a general overview.
  """
  gv.ph.prp.setProperty(key,value)

def property_system_save():
  """ Saves system properties (which are automatically loaded next time the program is run.)
      Note that routine specific properties are not saved.
      Type 'help properties' for a general overview on properties.
  """
  #file=open("gvdecoder\properties.cfg","w")
  #gv.pv.prp.store(file,"GView config file")
  gv.ph.saveCFG()


trace_properties=gvdecoder.trace.TraceParams.getInstance()
viewer_properties=gvdecoder.ViewerParams.getInstance()
navigator_properties=gvdecoder.NavigatorGraphParams.getInstance()

def property_show(tp):
  f=tp.getClass().getFields()
  for i in range(len(f)):
    w.print("%s = %s"%(f[i].name,eval("tp.%s"%f[i].name)))

def property_show_all():
  """ Prints all system (directories etc) and routine specific (viewer,navigator,etc)
      parameters, with minimal instructions on how to set them.
      See property_system_save(), property_system_set(), property_system_get()
      
      For a general overview, type: 'help properties' 
 """
  w.print("System properties : set using property_system_set(\"key\",value)")
  w.print("                    ie 'property_system_set(\"framerate\",15)'")
  property_system_show()
  w.print("")
  w.print("Viewer properties : set by typing viewer_properties.'key'=value")
  w.print("                    ie viewer_properties.AutoLoadLastRois=1")
  property_show(viewer_properties)
  w.print("")
  w.print("navigator properties : set by typing navigator_properties.'key'=value")
  w.print("                    ie navigator_properties.AutoLoadLastChartRange=1")
  property_show(navigator_properties)
  w.print("")
  w.print("trace properties : set by typing trace_properties.'key'=value")
  w.print("                    ie trace_properties.FlipData=1")
  property_show(trace_properties)


#simple statistics utility functions
def average(dat):
  """ averages the array (mean) """
  return array.mean(dat)

def median(dat):
  """ median of the array """
  return array.median(dat)

def variance(dat):
  """ variance of the array """
  return array.variance(dat)

def stdev(dat):
  """ standard deviation of the array """
  return array.standardDeviation(dat)

def  sum(dat):
  """ sum of all values in the array """
  return array.mass(dat)

def doubles(dat):
  """ utility function for converting ints to doubles """
  return [d*1.0 for d in dat]

def sumsquares(dat):
  """ sum of squares for values in an array """
  return array.sumSquares(dat)

def ttest(d1,d2):
  """ t-test for two arrays, assumes 0 mean difference. Please use with care... """
  sd2=variance(d1)/len(d1) + variance(d2)/len(d2)
  st=sd2**(0.5)
  t=(average(d1)-average(d2))/st
  if (t<0): t=t*-1
  td=JSci.maths.statistics.TDistribution((len(d1)+len(d2)-2))
  p1=td.inverse(0.05)*-1.0
  p05=td.inverse(0.025)*-1.0
  p01=td.inverse(0.005)*-1.0
  p001=td.inverse(0.0005)*-1.0
  est=(1-td.cumulative(t))*2
  w.print( ("t=%s p(0.1)=%s p(0.05)=%s p(0.01)=%s p(0.001)=%s val=%s"%(t,p1,p05,p01,p001,est)))
  return t,est
  

def findname(param):
  """ helper function for 'table' and 'view'"""
  for name in globals().keys():
   if globals()[name]==param:
    return name
  return None  


def view(ma):
 """ Opens a matrix object in a Viewer2 window, using the variable name as the title """
 nam=findname(ma)
 if nam==None:
   ma.show(gv,"matrix object")
 else:
   ma.show(gv,nam)
   
def opendecoder(path,file,decoder):
 """ Opens a viewer2 window given path, file, and a decoder (e.g. RedShirtSingleFileDecoder) """
 decoder.OpenImageFile(path+file)
 vw=gvdecoder.Viewer2(gv,20,file,file,decoder,0)
 gv.desktop.add(vw)
 vw.setVisible(1)

def table(dat):
 """
  Opens up a table of the data, which can either be in the form of a list of lists 
  or dictionary of lists, a Matrix object or a Viewer2 object. 
  The function returns a pointer to the table object.
  note:
   a)The data format is assumed to be rectangular (an array of arrays of equal length).
   b) Table headers are taken from the dictionary keys, or numbered if the main container is a list.
   c) You can change which columns are displayed, and their order by setting the tables
      'setColumnNames' command 
       ie: table.setColumnNames(['name','period','x1']) in the case of a dictionary,
        or table.setColumnNames([1,5,3]) in the case of a list of lists.
   d) the data can be dynamically updated, but not edited within the table itself for dictionaries, Lists, and Viewer2 objects.
   e) the data in Matrix views are editable.
   f) the 'presenttable' variable holds the most recent clicked on table
   g) setTitle works directly on the returned table.
   
 """
 
 tw=gvdecoder.JythonTableFrame(gv,dat)
 gv.desktop.add(tw)
 tw.show()
 nam=findname(dat)
 if (nam!=None):
   tw.setTitle(nam)
 return tw.getJythonTable()   

# directory and file chooser
lastdirectory=""
lastfilename=""
lastabsolutepath=""
def choosefile():
  """ Opens up a filedialog which allows the user to choose a file.
      The information is saved in lastfilename and lastdirectory
      global variables.
  """    
  jh.gv.fc.chooseFile(None)
  gl=globals()
  gl["lastfilename"]=jh.gv.fc.getName()
  gl["lastdirectory"]=jh.gv.fc.getDirectory()
  return gl["lastdirectory"],gl["lastfilename"]
  
def makegui(functionlist,buttonbarname="buttonbar"):
  """
   Will create a simple buttonbar that activate functions given to it in list form. The functions must get
   passed a single 'event' variable. Since this isn't generally the case, the function assumes that the 
   function is wrapped in another function called guifunction(event): (ie guisave(event): or guiload(event):) and
   trims the prefix 'gui' from the function name prior to creating a button. Document text for the wrapper function
   is loaded as a tooltip.
  """
  f=javax.swing.JInternalFrame(title=buttonbarname, visible=1)  
  #f.size=(500,100)
  gv.desktop.add(f) 
  panel=javax.swing.JPanel()
  panel.setLayout(javax.swing.BoxLayout(panel,javax.swing.BoxLayout.X_AXIS))
  for i in functionlist:
    buttonname=i.__name__
    tmp=buttonname.find("gui")
    if (tmp>-1):
      buttonname=buttonname[3:]
    b1=javax.swing.JButton(buttonname)
    b1.setToolTipText(i.__doc__)
    b1.actionPerformed=i
    panel.add(b1)
  f.getContentPane().add(panel)
  f.pack()
  f.repaint()
  return f    

_gp=None
_svg=None


def gaussian(x,a,b,c):
 """
 Gaussian function:
    x=val, a=amplitude, b=center, c=spread
 usage:
  tmp=[gaussian(x,10,5,2) for x in range(10)]
 """
 return a*2.718281828**(-(((1.0*x)-b)**2)/(2.0*c**2))

def gaussianlut(center,spread,fixmax):
 """
  Utility for generating smooth curves for lookup tables.
  The first value is the center pt, the second the spread, and the third
  instructs whether to hold the max value to the left of the center (-1)
  or to the right (1).
  usage:
  blue=gaussianlut(10,60,-1)
  green=gaussianlut(128,60,0)
  red=gaussianlut(240,60,1)
  jai_addColorFilterTable([blue,green,red])
 see gaussian, jai_addColorFilterTable
 """
 tmp=[0]*256
 for x in range(256):
  tmp[x]=int(gaussian(x,255,center,spread))
 if fixmax==-1:
  for x in range(center):
   tmp[x]=255
 if fixmax==1:
  for x in range(center,256):
   tmp[x]=255
 return tmp
 
imagejluts=['Blue Pale.lut', 'Blue_Hot.lut', 'Brown.lut', 'Cornflower.lut', 'Cyan Hot.lut', 'Cyan Pale.lut', 'Green Fire blue.lut', 'Green Fire blue2.lut', 'Green Fire.lut', 'Green Hot.lut', 'Green Pale.lut', 'HiLo.lut', 'ICA.lut', 'ICA2.lut', 'ICA3.lut', 'Lemon Hot.lut', 'Magenta Hot.lut', 'Magenta Pale.lut', 'Orange Hot.lut', 'Orange.lut', 'Pink Hot.lut', 'Purple.lut', 'Rainbow RGB.lut', 'Rainbow Smooth.lut', 'Ratio.lut', 'Red Fire.lut', 'Red Hot.lut', 'Red Pale.lut', 'Yellow Hot.lut', 'Yellow Pale.lut']

def loadlut(lutname):
 """
 Loads a lut from the users LUT directory (set in preferences).
 (Note: these are mostly copies of imagej luts.)
 usage: loadlut('Cyan Hot.lut')
 imagejluts holds all the LUT names.
 """
 f= open((property_system_get("LUTs dir")+"\\"+lutname),"rb")
 r=[]
 g=[]
 b=[]
 for i in range(256): 
   r.append(struct.unpack("B",f.read(1))[0])
 for i in range(256):
   g.append(struct.unpack("B",f.read(1))[0])
 for i in range(256):  
   b.append(struct.unpack("B",f.read(1))[0])
 jai_addColorFilterTable([r,b,g])
 return r,b,g

def getcolorlookuptable():
 filts=jh.presentviewer.jp.myfilt.filters
 for i in range(len(filts)):
  if isinstance(filts[i],gvdecoder.ColorFilter):
      return filts[i].lt

def savelut(filename):
 """ 
 Saves a the first color lut from presentviewer
 usage: savelut('C:\CD\ImageJ\LUTs\gils.lut')
 """
 lt=getcolorlookuptable()
 f=io.FileOutputStream(filename)
 for i in range(256): 
   bt=java.lang.Byte(lt[0][i])
   f.write(bt.byteValue())
 for i in range(256):
   bt=java.lang.Byte(lt[1][i])
   f.write(bt.byteValue())
 for i in range(256):  
   bt=java.lang.Byte(lt[2][i])
   f.write(bt.byteValue())
 f.close()


def addlut_bgr():
 red=gaussianlut(240,46,1)
 green=gaussianlut(128,46,0)
 blue=gaussianlut(10,46,-1)
 jai_addColorFilterTable([red,green,blue])

def addlut_greyred():
 blue=[0]*256
 green=[0]*256
 red=gaussianlut(240,80,1)
 for x in range(150):
  red[x]=int(x*0.9)
  green[x]=int(x*0.9)
  blue[x]=int(x*0.9)
 for x in range(150,256):
  green[x]=0
  blue[x]=0
 jai_addColorFilterTable([red,green,blue]) 
 return blue,green,red
  

 

############################
# Viewer ruler related functions
############################



def getruler():
  """
    returns the last (present) ruler.
  """
  return jh.presentviewer.jp.presentruler  

def ruler_sum(vw,ru,wd):
 """
   Sum points along a ruler object in a viewer window.
   Usage:
     r=getruler()
     sums=ruler_sum(jh.presentviewer,r,3) 
   The last parameter is the width over which to sum.  
     
 """
 x1=ru.firstX
 y1=ru.firstY
 x2=ru.lastX
 y2=ru.lastY
 if x2==-1:
  return -1
 ax1,ay1,ax2,ay2=parallel(x1,y1,x2,y2,wd)
 bx1,by1,bx2,by2=parallel(x1,y1,x2,y2,-wd)
 p=java.awt.Polygon()
 p.addPoint(int(ax1),int(ay1))
 p.addPoint(int(ax2),int(ay2))
 p.addPoint(int(bx2),int(by2))
 p.addPoint(int(bx1),int(by1))
 d=math.sqrt( (x2-x1)**2 + (y2-y1)**2 )
 sums=[0.0]*(int(d))
 cnts=[0]*(int(d))
 re=p.getBounds()
 for x in range(re.x, re.x+re.width):
  for y in range(re.y, re.y+re.height):
    r,dd,Px,Py=point2line(x1,y1,x2,y2,x,y)
    if r>=0 and r<1 and dd<wd:
      val=vw.datArray[vw.X_dim*y+x]
      if val>0:
       sums[ int(r*d)]+=val
       cnts[ int(r*d)]+=1 
 for c in range(len(sums)):
   sums[c]=sums[c]/(1.0*cnts[c])
 return sums


def point2line(Ax,Ay,Bx,By,Cx,Cy):
  """
     A subroutine used by by sum_ruler.
     for a line A, B, finds the distance to a point C 
     returns r (the fractional distance from the projection of C to A)
             d (the distance to the line)
             px,py (the point on the line)
     
  """
  L2=((Bx-Ax)*(Bx-Ax)+(By-Ay)*(By-Ay))
  r=((Ay-Cy)*(Ay-By)-(Ax-Cx)*(Bx-Ax))/(1.0*L2)
  Px=Ax+r*(Bx-Ax)
  Py=Ay+r*(By-Ay)
  d=math.sqrt((Cx-Px)**2+(Cy-Py)**2)
  return r,d,Px,Py
 
   


   
def graph2JPG(filename):
  """ Writes the last viewed plot as a .jpg file.
      
      usage: graph2JPG('filename')
      Notes: The file is saved in 'imagedir' directory. 
             This directory can be changed 'imagedir="c:\\myfiles\\scratch" """
  img=jh.pl.p.getImage()
  tofile=imagedir+filename
  ImageUtils.WriteImage(tofile,img,"jpeg")
  return "saved plot as %s"%tofile

def graph2BMP(filename):
  """ Writes the last viewed plot as a .bmp file.
       
       usage: graph2BMP('filename')
       Notes: The file is saved in 'imagedir' directory. 
              This directory can be changed 'imagedir="c:\\myfiles\\scratch" """

  img=jh.pl.p.getImage()
  tofile=imagedir+filename
  ImageUtils.WriteImage(tofile,img,"bmp")
  return "saved plot as %s"%tofile

def graph2PNG(filename):
  """ Writes the last viewed plot as a .png file.
         
         usage: graph2PNG('filename')
         Notes: The file is saved in 'imagedir' directory. 
                This directory can be changed 'imagedir="c:\\myfiles\\scratch" """
  
  
  img=jh.pl.p.getImage()
  tofile=imagedir+filename
  ImageUtils.WriteImage(tofile,img,"png")
  return "saved plot as %s"%tofile

def graph2TIFF(filename):
  """ Writes the last viewed plot as a .tif file.
         
         usage: graph2TIFF('filename')
         Notes: The file is saved in 'imagedir' directory. 
                This directory can be changed 'imagedir="c:\\myfiles\\scratch" """
  
  img=jh.pl.p.getImage()
  tofile=imagedir+filename
  ImageUtils.WriteImage(tofile,img,"TIFF")
  return "saved plot as %s"%tofile

def graph2EPS(filename,epsdpi=300):
  """ Writes the last viewed plot as a .eps file.
           
           usage: graph2EPS('filename',[optional dpi])
           If epsdpi is not specified, the default is 300. 
           Notes: The file is saved in 'imagedir' directory. 
                  This directory can be changed 'imagedir="c:\\myfiles\\scratch" """
 
  g=org.jibble.epsgraphics.EpsGraphics2D()
  g.scale(72/epsdpi, 72/epsdpi);
  jh.pl.p.paintComponent(g)
  tofile=imagedir+filename
  file=open(tofile,"w")
  file.write(g.toString())
  file.close

def window2EPS(filename,component,epsdpi=300):
  g=org.jibble.epsgraphics.EpsGraphics2D()
  g.scale(72/epsdpi, 72/epsdpi);
  component.paintComponent(g)
  tofile=imagedir+filename
  file=open(tofile,"w")
  file.write(g.toString())
  file.close
  

def plot2EPSv2(filename):
  g=org.jibble.epsgraphics.EpsGraphics2D()
  jh.pl.p.paintComponent(g)
  file=open(filename,"w")
  file.write(g.toString())
  file.close

def plot2EPSv3(filename,painter,epsdpi=300):
  g=org.jibble.epsgraphics.EpsGraphics2D()
  g.scale(72/epsdpi, 72/epsdpi);
  painter.paintComponent(g)
  tofile=imagedir+filename
  file=open(tofile,"w")
  file.write(g.toString())
  file.close  
  

def tofloat(arr):
  """ returns a series of jython numbers (floats) from an array of integers 
      
      usage:
      arr=tofloat(intarray)
      addgraph(tofloat(intarray))
  """	
  tmp=[]
  for x in arr:
    tmp.append(x)
  return tmp 
  

 

def graph(xs,ys=[]):
  """ Displays a scatter plot to the screen (second array optional).
  
      User supplies either a single array of values (in which case they are plotted
      against their index number) or two arrays of valyues (where the first array is the
      x's and the second array are y's) 
      
      Example: 'graph([1,2,3,4,5],[119,29,3,9,94])
      
      Notes:
      Subsequent calls to graph() overplot the last data set on the same graph.
      To add a data set to this graph, use 'addgraph(xs,ys)'. 
      To create a new graph, use 'newgraph(xs,ys)'.
      try 'help addgraph' or 'help newgraph' for more information.  
      """
  if (len(ys)==0):
    if (jh.pl==None):
      if (xs.__class__==gvdecoder.TimeSeries):
        jh.quickPlot(xs.arr,0,len(xs.arr),0)
      else:  
        jh.quickPlot(xs,0,len(xs),0)
      jh.pl.jh=jh
    else:
      if (xs.__class__==gvdecoder.TimeSeries):
        jh.pl.newPlot(xs.arr)
      else:
       jh.pl.newPlot(xs)
  else:
    if (jh.pl==None):
      if (xs.__class__==gvdecoder.TimeSeries):
        jh.quickPlot(xs.arr,ys.arr) 
      else:
        jh.quickPlot(xs,ys)
      jh.pl.jh=jh
    else:
      if (xs.__class__==gvdecoder.TimeSeries):
        jh.pl.newPlot(xs.arr,ys.arr)
      else:
        jh.pl.newPlot(xs,ys)

def addgraph(xs,ys=[]):
   """ Adds a scatter plot to an existing graph (second array optional).
     
         User supplies either a single array of values (in which case they are plotted
         against their index number) or two arrays of values (where the first array is the
         x's and the second array are y's) 
         
         Example: 'addgraph([1,2,3,4,5],[119,29,3,9,94])
         
         Notes:
         The user selects the graph to plot by clicking on the desired graph window,
         or by calling findplot('title') where title is the title of the window itself.
         To create a new graph, use 'newgraph(xs,ys)'.
         A call to graph(xs,ys) overwrites all plots on the selected plot.
         try 'help graph','help newgraph'  or 'help find' for more information.  
   """ 
   if (jh.pl==None):
     if (xs.__class__==gvdecoder.TimeSeries):
      graph(xs.arr,ys.arr) 
     else:
      graph(xs,ys)
     return
   if (len(ys)==0):
     if (xs.__class__==gvdecoder.TimeSeries):
      jh.pl.addPlot(xs.arr)
     else: 
      jh.pl.addPlot(xs)
     return "len ys=0 routine"
   else:
     if (xs.__class__==gvdecoder.TimeSeries):
      jh.pl.addPlot(xs.arr,ys.arr) 
     else:
      jh.pl.addPlot(xs,ys)
     return "lenys > 0 routine to "+jh.pl.getTitle()

def newgraph(xs,ys=[]):
  """ Creates a new graph window, and supplies a title(second array optional).
       
           User supplies either a single array of values (in which case they are plotted
           against their index number) or two arrays of values (where the first array is the
           x's and the second array are y's) 
           
           Example: 'newgraph([119,29,3,9,94])'
           
           Notes:
           See 'help graph' and 'help addgraph' for information on how to  plot
           new data, or add new data to an existing graph window.
  """ 
  if (len(ys)==0): 
   if (xs.__class__==gvdecoder.TimeSeries):
    jh.quickPlot(xs.arr,0,len(xs.arr),0)
   else:
    jh.quickPlot(xs,0,len(xs),0)
   jh.pl.jh=jh
  else:
   if (xs.__class__==gvdecoder.TimeSeries):
    jh.quickPlot(xs.arr,ys.arr)
   else:
    jh.quickPlot(xs,ys)
   jh.pl.jh=jh


def errorgraph(xs,ys,es):
  """ Displays a scatter plot to the screen with error bars.
  
      User supplies either two arrays of values (where the first array is the
      x's, the second array are y's, third array holds standard deviations ) 
      
      Example: 'errorgraph([1,2,3,4,5],[119,29,3,9,94],[3,2,4,3,1])
      
      Notes:
      Subsequent calls to errorgraph() overplot the last data set on the same graph.
      To add a data set to this graph, use 'adderrorgraph(xs,ys,errors)'. 
      To create a new graph, use 'newerrorgraph(xs,ys,errors)'.
      try 'help adderrorgraph' or 'help newerrorgraph' for more information.  
      """
  if (jh.pl==None):
      jh.quickPlot(xs,ys,es)
      jh.pl.jh=jh
  else:
      jh.pl.newPlot(xs,ys,es)

def adderrorgraph(xs,ys,es):
   """ Adds a scatter plot to an existing graph with error bars.
     
        
	User supplies either two arrays of values (where the first array is the
	x's, the second array are y's, third array holds standard deviations ) 
      
         Example: 'adderrorgraph([1,2,3,4,5],[119,29,3,9,94],[2,3,1,3,1])
         
         Notes:
         The user selects the graph to plot by clicking on the desired graph window,
         or by calling findplot('title') where title is the title of the window itself.
         To create a new graph, use 'newerrorgraph(xs,ys,errors)'.
         A call to errorgraph(xs,ys) overwrites all plots on the selected plot.
         try 'help errorgraph','help newerrorgraph'  or 'help find' for more information.  
   """ 
   if (jh.pl==None):
     graph(xs,ys,es)
     return
   else:
     jh.pl.addPlot(xs,ys,es)
     return "lenys > 0 routine to "+jh.pl.getTitle()

def newerrorgraph(xs,ys,es):
  """ Creates a new graph window, and supplies a title(second array optional).
       
           User supplies either a single array of values (in which case they are plotted
           against their index number) or two arrays of values (where the first array is the
           x's and the second array are y's) 
           
           Example: 'newerrorgraph([119,29,3,9,94],[119,29,3,9,94],[2,3,1,3,1])'
           
           Notes:
           See 'help errorgraph' and 'help adderrorgraph' for information on how to  plot
           new data, or add new data to an existing graph window.
  """ 
  jh.quickPlot(xs,ys,es)
  jh.pl.jh=jh


def multigraph(xs,ys=[],logx=0,logy=0):
  """ Repeated calls add seperate plots to a scrollable window. 
      usage: multigraph(xs,ys,logx,logy) where ys are optional and logx and logy are toggles for logscale
     
      Each plot is accessible as jh.pl.subplot[index].
      
      see: rescalemultigraph, titlemultigraph, newmultigraph, overmultigraph
   """   
  if (jh.pl==None):
    jh.quickPlot(0,400,200) 
  _domultiplot(xs,ys,logx,logy)

def newmultigraph(xs,ys=[],logx=0,logy=0):
  """ Creates a new multigraph window. Add plots by repeatedly calling multigraph().
      usage: newmultigraph(xs,ys,logx,logy) where ys are optional and logx and logy are toggles for logscale
        see: rescalemultigraph, multigraph, newmultigraph, overmultigraph  
  """
  jh.quickPlot(0,400,200)
  _domultiplot(xs,ys,logx,logy)

def overmultigraph(position,xs,ys=[]):
  """ plots over a graph in a multigraph window. The first argument is the graph positon (0,1,etc).
      It keeps the scaling (log or linear) and title of the original graph.
      
      Usage: overmultigraph(position,xs,ys) where ys are optional
    
      If the multigraph doesnt exist, or the graph doesnt have position number of graphs
      the appropriate object is created.     
 
      see: multigraph, newmultigraph, overmultigraph
  """
      
  if (jh.pl==None) or (jh.pl.subplots==None):
   #create the overmultigraph first with the right number of subplots
   for i in range(position+1):
    multigraph(xs,ys)
  if (position>=len(jh.pl.subplots)):
   for i in range(position-len(jh.pl.subplots)+1):
    multigraph(xs,ys)
  p_n=jh.pl.subplots[position]
  if (len(ys)==0):
   jh.pl.newPlot(p_n,xs)
   jh.pl.p.repaint()
  else:
   jh.pl.newPlot(p_n,xs,ys) 
   jh.pl.p.repaint() 
  jh.pl.repaint()
  
 
def rescalemultigraph(position):
  p_n=jh.pl.subplots[position]
  p_n.fillPlot()

def _domultiplot(xs,ys=[],logx=0,logy=0):
  if (len(ys)==0):
    xaxis=[]
    for x in range (len(xs)):
      if (len(ys)==0):
        xaxis.append(x)
    jh.pl.addPlotWindow(xaxis,xs,logx,logy)
  else:
    jh.pl.addPlotWindow(xs,ys,logx,logy)
  jh.pl.p._topPadding=1
  jh.pl.repaint()


def listplots():
  """ Returns an array of plot names (which can be used by findplot() to focus on a particular plot). """
  titles=[]
  for i in jh.plots:
    titles.append(i.getTitle())
  return titles

def findplot(title):
  """ Sets the focus on a particular plot for saving or modifying.
      
      The title of each plot is the title shown in the window name, NOT
      the title given by the user. Unless otherwise altered, the titles are
      'plot=0', 'plot=1', etc. 
      
      Example: findplot("\plot=3\").
      """
  
  for i in jh.plots:
   if (title==i.getTitle()):
    jh.pl=i 
    return i
  return "not found"



def readGVxys(filename):
  #called from getSectionFromFile
  file=open(filename,"r")
  strs=file.readlines()
  xs=[]
  ys=[]
  for x in strs:
    a,b,c=x.split(" ")
    xs.append(int(a))
    ys.append(int(b))
  file.close()
  return xs,ys

def readGVxyzs(filename):
  #called from getSectionFromFile
  file=open(filename,"r")
  strs=file.readlines()
  xs=[]
  ys=[]
  zs=[]
  for x in strs:
    a,b,c,d=x.split(" ")
    xs.append(int(a))
    ys.append(int(b))
    zs.append(int(c))
  file.close()
  return xs,ys,zs 
   

   
def getSectionFromFile(filename, i1,j1,i2=-1,j2=-1):
   """Pulls out one or two channels, located at index i1,j1 (and i2,j2), from a OMPRO file
      
         Usage example: 
           data=getDataFromFile(10,4,"c://data//RCA//12Aug2005.s3")
           data,stim=getDataFromFile(3,3,2,16,"c://data//RCA//12Aug2005.s3")
   """
   dec=OMPRODecoder()
   dec.OpenImageFile(filename)
   intarray=jarray.zeros(4,'i')
   dec.ReturnXYBandsFrames(intarray,0)
   zdim=intarray[3]
   roi=[]
   roi.append([j1*16+i1])
   if (i2>-1):
     roi.append([j2*16+i2])
   dec.SumROIs(roi,"scratch//tmp.dat",0,zdim,0)
   if (i2>-1):
     x,y,z=readGVxyzs("scratch//tmp.dat")
     return y,z
   else:
     x,y=readGVxys("scratch//tmp.dat")
     return y

def getrois():
 return jh.presentviewer.jp.rois
 
 
def getroidata():
  vw=jh.presentviewer
  roid=vw.getRoiData()
  res=[]
  for i in range(len(roid)):
    res.append(gvdecoder.TimeSeries(roid[i]))
  return res
  
def setroidata(roid):
  vw=jh.presentviewer
  
  res=[]
  for i in range(len(roid)):
    res.append(roid[i].arr)
  vw.setRoiData(res)  
  vw.showRois(0)
  return res
    
  
     
#def getroidata(vw,roi):
#  """Gets the raw data from a given ROI, given the viewer and the ROI index
#     This routine assumes that the ROIs are held in the last opened navigator.
#     Use nav.tg.Traces[index].raw_y for direct access to a navigators data
#     
#     Usage example:
#        roidat=getroidata(jh.vw[0],0)
#  """          
#  return vw.nav.tg.Traces[roi].raw_y     

def testtraceview():
 ma=Matrix()
 ma.initialize("ompro",0,800)
 tr=traceView(ma,50,50)
 gv.desktop.add(tr);
 tr.setVisible(1)
 tr.populate()
 tr.render(0,0,16,16,30,30)
 return tr,ma
 

def bigscript():
  rois=[]
  rois.append(ROI(62,62,20,20,22,22))
  rois.append(ROI(62,62,30,20,32,22))
  rois.append(ROI(62,62,40,20,42,22))
  rois.append(ROI(62,62,20,30,22,32))
  rois.append(ROI(62,62,30,30,32,32))
  rois.append(ROI(62,62,40,30,42,32))
  rois.append(ROI(62,62,20,40,22,42))
  rois.append(ROI(62,62,30,40,32,42))
  rois.append(ROI(62,62,40,40,42,42))
  spes=getfiles(paths[0])
  bigdict={}
  for xx in range(0,10):
    tmp=runbursttests(spes[xx],rois)
    bigdict[xx]=tmp
  outfile=open("bigresult0to10.pickle","w")
  pickle.dump(bigdict,outfile)
  outfile.close()
  return bigdict


#returns a dictionary with statistical results for this file
#roi data is held in dict[1] through dict[n] where n is the total number of rois
def runbursttests(fullfilename,rois):
   im=w.openImageDecoder(fullfilename,"spe")
   traces=sumrois(im,rois,"tmp.txt",w.instance) 
   results={}
   results["filename"]=fullfilename
   results["xdim"]=w.imagefile_info[0]
   results["ydim"]=w.imagefile_info[1]
   results["zdim"]=w.imagefile_info[3]
   results["bands"]=w.imagefile_info[2]
   variance=[]
   #results["roi11"]=traces[1]
   if (len(traces[1])>500):
     for i in range(1,len(traces)):
       bground = engineermath.runningAverage(traces[i],51)   
       traces[i]=[(traces[i][x]-bground[x]) for x in range(len(traces[i]))]
       traces[i]=normalize(traces[i])    
       xs,var=variancefilter(traces[i],20)
       variance=(array.variance(var))
       hist=histogram(var,10)
       dwellhist=dwellhistogram(var,10)
       variancehistogram=array.variance(hist)
       sum=0
       for u in dwellhist: sum+=u
       sumdwellhistogram=u
       clusteredval,clusterhi,clusterlo=clustered(var)
       traces[i]=engineermath.runningAverage(traces[i],3)
       aps=findaps_threshold(traces[i],0.5,5)
       ibis=getibis(aps)
       burststart=findburststart(aps,3,60,25)
       burstend=findburstsend(aps,3,60,25)
       min,max=characterizespikes(aps)
       roidict={"roi":i,"variance":variance,"clustered":clusteredval,"clusterhi":clusterhi,"clusterlo":clusterlo,"burst_starts":burststart,"burst_ends":burstend,"min":min,"max":max,"ibis":ibis}
       #str="roi="+i+" var="+variance+" starts="+len(burststart)+" ends="+len(burstend)+" min="+min+" max="+max 
       str="roi=%s var=%s clustered= %s,%s,%s starts=%s ends=%s min=%s max=%s numibis=%s \n"%(i,variance,clusteredval,clusterhi,clusterlo,len(burststart),len(burstend),min,max,len(ibis))
       roidict["string"]=str
       roidict["histogram"]=hist
       roidict["variancehistogram"]=variancehistogram
       roidict["sumdwellhistogram"]=sumdwellhistogram
       results[i]=roidict
       w.print(str)
       w.print(ibis)
   return results
   
def histogram_range(res,binstart,binend,numbins):
  """ 
  generate a histogram from an array of data, given start, end, and number of divisions
  usage bins=histogram_range(data,startrange,endrange,numberofbins)
  see also histogram, and dwellhistogram
  """
  bins=[]
  for a in range(numbins):
    bins.append(0)
  binwidth=(binend-binstart)/numbins
  for i in res:
   for j in range(numbins):
    bs=j*binwidth+binstart
    be=bs+binwidth
    if (i>=bs) and (i<be):  
       bins[j]+=1
  return bins   

def histogram(arr,divs):
   """ 
   generate a histogram from an array of data, given a number of divisions
   usage bins=histogram(data,numberofbins)
   see also histogram_range
   """
   mmax=max(arr)
   mmin=min(arr)
   hist=[0.0]*int(divs)
   binsize=(mmax-mmin)/divs
   for x in arr:
      for i in range(int(divs)):
         if (x>mmin+binsize*i) and (x<mmin+binsize*(i+1)):
            hist[i]+=(1.0/len(arr))
   return hist

def dwellhistogram(arr,divs):
   """
   used in entropy calculations 
   
   """
   mmax=max(arr)
   mmin=min(arr)
   hist=[0.0]*int(divs)
   binsize=(mmax-mmin)/divs
   for j in range(5,len(arr)):
      x=arr[j]
      xl=arr[j-1]
      for i in range(int(divs)):
         if (x>mmin+binsize*i) and (x<mmin+binsize*(i+1)):
            last5=0.0
            for h in range(j-5,j):
               xl=arr[h]              
               if (xl>mmin+binsize*i) and (xl<mmin+binsize*(i+1)):
                  last5+=1.0
            if (last5>4): hist[i]+=(1.0/len(arr))
   return hist


def clustered(arr):
   #med=engineermath.runningMedian(arr,3)
   min=100000
   max=-100000
   for x in arr:
       if (x<min): min=x
       if (x>max): max=x
   clustered=0.0  
   nonclustered=0.0
   hi=0.0
   lo=0.0
   for i in range(1,len(arr)):
      dif=arr[i]-arr[i-1]
      if (dif<0): dif=dif*(-1.0)
      if (dif < ((max-min)*0.2)): #this doesnt work, if the variance is low
        clustered+=1
        if ((arr[i]-min)>((max-min)*0.5)): hi+=1
        if ((arr[i]-min)<((max-min)*0.2)): lo+=1
      else:
        nonclustered+=1 
   return clustered/(clustered+nonclustered), hi/len(arr), lo/len(arr)    

def getframe(zindex):
	frame=[]
	for x in range(0,ma.xdim):
		for y in range(0,ma.ydim):
			frame.append(ma.dat.get(zindex,y,x))
	return frame

def smoothsub(smoothed):
    #w.print("started with smoothsub")
    for z in range(0,ma.zdim):
        #w.print(z)
        fr=getframe(z)
        fr_new=[(val-smoothed[z]) for val in fr]
        for x in range(0,ma.xdim):
            for y in range(0,ma.ydim):
                ma.dat.set(z,y,x,fr_new[y*ma.xdim+x])

class myfunction(gvdecoder.function):
   dat=[]
   def set(self,data):
     for p in data:
       self.dat.append(p)
   def compute(self,val):
     return self.dat[val]*val
     
def saveandcompile(filename):
	w.savetextfile(filename)
	str="compile ",filename
	os.system(str)

	
def savedict(dict,filename):
   file=open(filename,'w')
   keys=dict.keys()
   vals=dict.values()
   for i in range(len(keys)):
      str="%s %s\n" % (keys[i],vals[i])
      file.write(str)
   file.close()	


def doJungCalculation(ma):
  ma.processByTrace("n")
  ma.BlobCount(400)
  blobs1=findblobsize(ma.dat.arr)
  blobs2=groupblobs(blobs1)
  xs,ys=sortDictionary(blobs2)
  return xs,ys

def doJungPrecounted(ys):
  blobs1=findblobsize(ys)
  #blobs2=groupblobs(blobs1)
  xs1,ys1=sortDictionary(blobs1)
  return xs1,ys1



def savexys(xs,ys,filename):
   """
     usage:
       savexys(xs,ys,"filename.dat")
       
    """   
   file=open(filename,'w')
   for i in range(len(xs)):
      str="%s %s\n" % (xs[i],ys[i])
      file.write(str)
   file.close()	


def makecolumns(*arg):
 """
   Returns a set of sequences as a single string in column format. The number of columns is equal to the number of arguments. 
   Each argument must be a sequence of equal length.
   usage:
     makecolumns([1,2,3],[4,5,6])
   returns:
    1 4
    2 5
    3 6
 """
   
 cols=len(arg)
 rows=len(arg[0])
 str=java.lang.StringBuilder()
 for x in range(rows):
  for y in range(cols-1): 
   str.append(arg[y][x])
   str.append(" ")
  str.append(arg[cols-1][x])
  str.append("\n") 
 return str.toString()

def readxys(filename):
  file=open(filename,"r")
  strs=file.readlines()
  xs=[]
  ys=[]
  for x in strs:
    a,b=x.split(" ")
    xs.append(int(a))
    ys.append(int(b))
  file.close()
  return xs,ys


def readcols(filename):
 file=open(filename,"r")
 strs=file.readlines()
 split=strs[0].split(" ")
 numcols=len(split)
 goodcols=[]
 w.print(numcols)
 for j in range((numcols)):
   if len(split[j])>0:
    goodcols.append(j)
 w.print(("numcols=%s goodcols=%s"%(numcols,goodcols)))
 res=[]
 for i in range(len(goodcols)):
  res.append([0]*len(strs))
 for s in range(len(strs)):
   vals=strs[s].split(" ")
   cnt=0
   for g in goodcols:
    res[cnt][s]=float(vals[g])
    cnt+=1 
 return res
 

def savelist(xs):
   file=open(filename,'w')
   for i in range(len(xs)):
      str="%s\n" % xs
      file.write(str)
   file.close()	
	
def analyze(v):
   res=[0]*10000
   for i in range (len(v)):
      if (v[i]<len(res)):
         res[v[i]]+=1
   return res


def dvdt(tr):
  #(trace) returns the derivative of a trace
  res=[0.0]*len(tr)
  for x in range(1,len(tr)):
    res[x]=tr[x]-tr[x-1]
  return res  

def geteventtimes(trdvdt,threshold):
  #(trace,threshold) returns event times from a trace based on threshold passing in the positive direction
  return [x for x in range(2,len(trdvdt)) if ((trdvdt[x-2]<threshold) and (trdvdt[x-1]>threshold) and (trdvdt[x]>threshold))]

def getibis(eventtimes): 
  #(eventtimes) returns interbeat intervals from eventtimes
  return [eventtimes[x+1]-eventtimes[x] for x in range(0,len(eventtimes)-1)]

def geteventsformatrix(ma,xstart,xend,ystart,yend,threshold):
  res=[]
  for i in range(xstart,xend):
   for j in range(ystart,yend):
    tr=ma.dat.section(j,i)
    trdvdt=dvdt(tr)
    eventtimes=geteventtimes(trdvdt,threshold)
    #ibis=getibis(eventtimes)
    res.append(eventtimes)
    w.print(("appended %s %s len=%s"%(j,i,len(eventtimes))))
  return res
   

def mi_calculatemi(arr):
  #find the probability density for a given column,row
  sumrows=[0.0]*10
  sumcols=[0.0]*10
  sumtotal=0.0
  for i in range(10):
    for j in range(10):
      sumrows[i]+=arr[i*10+j]
      sumcols[i]+=arr[j*10+i]
      sumtotal+=arr[i*10+j]
  normrows=[sumrows[i]/sumtotal for i in range(10)]
  normcols=[sumcols[i]/sumtotal for i in range(10)]
  #return normrows,normcols,sumtotal
  mi=0.0
  for i in range(10):
    for j in range(10):
     Pij=arr[i*10+j]/sumtotal
     PixPj=normrows[i]*normcols[j]
     mi+=Pij*math.log10(Pij/PixPj)
  return mi



def mi_buildarray(v_trace,c_trace):
  #setup 10x10 array
  arr=[0]*100
  #for each vtrace and ctrace, find max/min values
  maxv=max(v_trace)
  minv=min(v_trace)
  maxc=max(c_trace)
  minc=min(c_trace)
  #if there are 10 divisions, and max=10, min 5, max-min=5 5/10=0.5
  #if there are 10 divisions, and max=10, min=1, max-min=9, 9/10=0.9
  divv=(maxv-minv)/10.0
  divc=(maxc-minc)/10.0
  w.print(("divv=%s divc=%s"%(divv,divc)))
  for i in range(len(v_trace)):
    v=v_trace[i]
    c=c_trace[i]
    #what are the indices?
    iv=int((v-minv-0.00001)/divv) 
    ic=int((c-minc-0.00001)/divc) 
    #w.print(("%s %s %s %s %s %s"%(v,c,(v-minv),(c-minc),iv,ic)))
    arr[ic*10+iv]+=1
  return arr
    

#setup for reading files using a script, gives a listing of the directory
paths=[]
paths.append("c:\\data\\sharepoint\\god\\4and2\\")
paths.append("c:\\data\\sharepoint\\god\\")
def getfiles(path):
   listing=os.listdir(path)
   spes=[]
   for x in listing:
     if x.endswith("SPE"):
       spes.append(x)    
   fullpath=[path+x for x in spes]
   return fullpath

#find bursts based on the idea that theres silence of minibursti length
#followed by several beats (number = minburstlength) with duration
# of maxibi (that is, the ibi<maxibi during the normal beating
def findburststart(aps,minburstlength,minibursti,maxibi):
   bursts=0
   arr=getibis(aps)
   res=[]
   for i in range(0,len(arr)-minburstlength):
      if (arr[i]>minibursti):
         burstdetected=1
         for j in range(i+1,i+minburstlength):
            if (arr[j]>maxibi):
              burstdetected=0
         if (burstdetected==1):
            bursts+=1
            res.append(aps[i])
   return res
   
def findburstsend(aps,minburstlength,minibursti,maxibi):
   bursts=0
   arr=getibis(aps)
   res=[]
   for i in range(minburstlength,len(arr)):
      if (arr[i]>minibursti):
         burstdetected=1
         for j in range(i-minburstlength,i-1):
            if (arr[j]>maxibi):
              burstdetected=0
         if (burstdetected==1):
            bursts+=1
            res.append(aps[i])
   return res


def getibis(aps):
  arr=[]
  arr.append(aps[0])
  for k in range (1,len(aps)):
    arr.append(aps[k]-aps[k-1])
  return arr

def characterizespikes(aps):
   #find shortest spikes
   ibis=getibis(aps)
   min=1000000
   max=0
   for x in ibis:
       if (x<min): min=x
       if (x>max): max=x
   return min,max
   
def variancefilter(trace,windowwidth):
   xs=[]
   ys=[]
   l=len(trace)
   #steps=(l*1.0)/windowwidth
   index=0
   for s in range(windowwidth,len(trace)-windowwidth):
      v=array.variance(trace[s-windowwidth:s+windowwidth])
      ys.append(v)
      xs.append(s)
   return xs,ys
 

def normalize(trace):
   """ Utility function for normalizing a series of numbers between 1 and 1000
       
      Usage: norms=normalize(trace)

   """
   min=10000000
   max=-10000000
   norm=[]
   for x in trace:
       if (x<min): min=x
       if (x>max): max=x
   range=max-min
  
   for x in trace:
       norm.append((((x*1.0)-min)/range)*1000)
   return norm


 

#find action potentials by threhsold crossing. Use on relatively clean
#data that has 0 baseline. Thresh=0 to 1 fraction of total height. mintime is the min time between activations
def findaps_threshold(arr,thresh,mintime):
   aplast=0
   max=array.max(arr)
   min=array.min(arr)
   thresh=min+((max-min)*thresh)
   w.print(thresh)
   aps=[]
   for i in range(0,len(arr)-4):
      if arr[i]<thresh: 
       if (arr[i+1]<thresh): 
        if (arr[i+2]>thresh): 
          if(arr[i+3]>thresh):
           if (i-aplast>mintime):
              aplast=i
              aps.append(i)
   return aps


def derivative(arr):
   res=[]
   res.append(0)#ensures same length
   for x in range(1,len(arr)):
      res.append(arr[x]-arr[x-1])
   return res   

#instance in most case 0, unless a .spe file
def sumrois(im,rois,filename,instance):
   roiints=[]
   for r in range(len(rois)):
      roiints.append(rois[r].arrs)
   im.SumROIs(roiints,filename,0,-1,instance)
   ga=trace.getArrayFromFile(filename);
   intarrs=ga.returnTransposedArray()
   doublearrs=[]
   for i in range (len(intarrs)):
      doublearrs.append([x*1.0 for x in intarrs[i]])
   return doublearrs   

 
#call w.setShortcuts(index) where index is the window value first
def processrois(vw):
   return sumrois(vw.im, vw.jp.rois, "tmp.txt", vw.instance)
 
 

#same as sum dict, but used to cluster a summed dictionary
def sumdict_group(dict,arg,size):
   if (dict.has_key(arg)):
      val=dict[arg]
      val=val+size
      dict[arg]=val
   else:
      dict[arg]=size


#before determining the slope of a log log line, it might make sense to cluster the results
def logNbin(xs,ys,N):
  res={} 
  for j in range(len(xs)):
     x=xs[j]
     y=ys[j]
     for i in range(30):
      if (x>=math.pow(N,i)) and (x<math.pow(N,i+1)):
         nx=(math.pow(N,i)+math.pow(N,i+1))/2.0+math.pow(N,i)
         sumdict_group(res,nx,y)
         break
  return res
   
   
#sort the dictionary, run by using keys,values=sortDictionary(dict)
def sortDictionary(dict):
  keys=dict.keys()
  keys.sort()
  values=[dict[i] for i in keys]
  return keys,values


#finds the number of blobs of a given size, called after findblobsize
def groupblobs(dict):
   vals=dict.values()
   newdict={}
   for i in vals:
     sumdict(newdict,i)
   return newdict
   

#run findblobsize on an array after matrix.BlobCount(thresh) has been run
#returns a dictionary of blob indexes and their sizes, in no order
#in order to analyze this, must group the results so that we know how
#many blobs there are of a given size
def findblobsize(arr):
   cnt=[]
   dict={}
   for i in range(0,len(arr)):
      if (arr[i]>0):
         sumdict(dict,arr[i])
   return dict               

#sums using a dictionary. if the arg isn't found its added, else a value
#of one is added, (useful for histograms).
def sumdict(dict,arg):
   if (dict.has_key(arg)):
      val=dict[arg]
      val=val+1
      dict[arg]=val
   else:
      dict[arg]=1



#(39)>m2.BlobCount(400)
#(40)>v2blobs1=findblobsize(m2.dat.arr)
#(41)>v2blobs2=groupblobs(v2blobs1)
#(42)>xs,ys=sortDictionary(v2blobs2)
#(43)>S2=entropy(xs,ys)
#(44)>w.print(S2)
#>	3.746166499548436
#(45)>lxs=[math.log10(x) for x in xs]
#(46)>lys=[math.log10(y) for y in ys]
#(47)>cf=CurveFitter(lxs,lys)
#(48)>cf.doFit(cf.STRAIGHT_LINE,0)
#(49)>w.print(cf.getResultString())
# entropy measurement from Jung 2000, takes two lists, xs= blobsize, ys= blobfrequency
def entropy(xs,ys):
   v=[]
   vtot=0
   for i in range(0,len(xs)):
       v.append(xs[i]*ys[i])
       vtot+=xs[i]*ys[i]
   vn=[]
   for j in v:
      vn.append((j*1.0)/(vtot*1.0))
   S=0 
   for k in vn:
      # str="k=",k," math.log(k)=",math.log(k)
      #w.print(str)
      S+=k*math.log(k)
   return -1*S
   
   

def runningfft(trace,threshold,windowwidth,step):
  resy=[]
  resx=[]
  #fftpower=[]
  #ffts=[]
  step=step*1.0
  for i in range(int(len(trace)/step)):
    j=i*step 
    slice=trace[j:j+windowwidth]
    slicefft=ma.fft(slice)
    xs,ys=highestperiods(slicefft)
    for k in range(3):
      if (ys[k]>threshold):
       resx.append(j)
       resy.append(xs[k])
      else:
       resx.append(j)
       resy.append(0)
  return resx,resy 
  
  
#def gnuplot(matrix,zindex,filename):
 # file=open(filename,"w")
 ## for x in range(matrix.xdim):
  # for y in range(matrix.ydim):
  #  file.write("%s %s %s\n" % (x,y,matrix.dat.get(zindex,x,y)))
  # file.write("\n")
  #file.close()


class gnuplotparams:
  """ set this up for calls to gp_plot (see gp_plot) """
  def __init__(self,filename="tmp",filenumber=0,keep=0, *precommands):
     self.filecounter=filenumber
     self.filename=filename  
     self.keep=keep
     self.precommands=precommands

class gnuplot:
  """
   Opens up a interactive gnuplot session in gview.
   Usage:
     g=gnuplot()
     g("set title font 'arial,20' 'SLs'")
     g.plot(g.histogram,[0.1,0.2,0.3,0.4],[100,200,80,40])
   
   
   notes: 
   The plot command can be passed any string containing commands. See g.xyplot or g.histogram
   	  for the format.
   Calls to gnuplot (ie g("set title 'title'") get inserted into the command string prior
   	  to the plot or splot command (where '#extras' is)
   The window can be imported into a HTML report window.
   
  """
  extras=[]
  xyplot="""
unset key
set terminal png font arial 12 size 500,400
set title font "arial,14" 'x y plot'
set xlabel font "arial,14" 'xs'
set ylabel font "arial,14" 'ys'
set out 'gvdecoder/scratch/gnuplotimage.png'
#extras
plot 'gvdecoder/scratch/gnuplotdata.dat' u 1:2 w linesp lt -1 lw 2
set out
save 'gvdecoder/scratch/gnuplotscript.plt'
  """

  histogram="""
unset key
set terminal png font arial 12 size 500,400
set title font "arial,14" 'histogram'
set xlabel font "arial,14" 'bins'
set ylabel font "arial,14" 'freq'
set out 'gvdecoder/scratch/gnuplotimage.png'
set style fill pattern
set style data histograms
set yrange[0:]
#extras
plot 'gvdecoder/scratch/gnuplotdata.dat' u 2:xticlabels(1) lt -1, "" u 3 lt -1
set out 
save 'gvdecoder/scratch/gnuplotscript.plt'
  """
 
  def __init__(self):
   if globals()["_gp"]==None:
    globals()["_gp"]=setupGnuplot()
   files=os.listdir("gvdecoder/scratch")
   gnuf=[]
   for f in files:
     if f.find("gnu")>-1:
      gnuf.append("gvdecoder/scratch/%s"%f)
   for g in gnuf:
    os.remove(g)
   cleanedscratch=1
  
   
  cleanedscratch=0
  def cleanscratch(self):
   files=os.listdir("gvdecoder/scratch")
   gnuf=[]
   for f in files:
     if f.find("gnu")>-1:
      gnuf.append("gvdecoder/scratch/%s"%f)
   for g in gnuf:
    os.remove(g)
   cleanedscratch=1
  
  def __call__(self,str):
   self.extras.append(str)
  
  def reset(self):
    self.extras=[]

  gpindex=1
  def plot(self,commandstring,xs,ys=[],zs=[]):
   if self.cleanedscratch==0:
    self.cleanscratch()
   imgstr="gnuplotimage%s.png"%self.gpindex
   datstr="gnuplotdata%s.dat"%self.gpindex
   scriptstr="gnuplotscript%s.plt"%self.gpindex
   file=open("gvdecoder/scratch/%s"%datstr,"w")
   if len(ys)==0:
     for i in range(len(xs)):
       file.write("%s\n"%xs[i])
   elif len(zs)==0:
     for i in range(len(xs)):
       file.write("%s %s\n"%(xs[i],ys[i]))
   else:
     for i in range(len(xs)):
       file.write("%s %s %s\n"%(xs[i],ys[i],zs[i]))
   file.close()
   strs=commandstring.split("\n")
   for i in range(len(strs)):
     if strs[i].find("#extras")>-1:
      for j in self.extras:
       _gp.println(j)
     else:
      str=strs[i].replace("gnuplotimage.png",imgstr)
      str1=str.replace("gnuplotdata.dat",datstr)
      str2=str1.replace("gnuplotscript.plt",scriptstr)
      _gp.println(str2)
   _gp.flush()
   java.lang.Thread.sleep(250)
   qgp=gvdecoder.quickGnuplot( "gvdecoder/scratch/%s"%datstr, "gvdecoder/scratch/%s"%scriptstr, "gvdecoder/scratch/%s"%imgstr)
   qgp.jh=jh
   gv.desktop.add(qgp)
   qgp.show()
   self.gpindex=self.gpindex+1
   return qgp
  


def gp_plot(gp,trace1,trace2=[],tstamps1=[],tstamps2=[],type="ts",coms=[]):
  """ autoplots a trace using Gnuplot by writing then displaying a .png file, stored in the scratch directory.\n
      g=Gnuplot.Gnuplot() and gp=gnuplotparams() must be called first"""   
  gp.filecounter+=1
  if os.path.exists("scratch\%s%s.png"%(gp.filename,gp.filecounter)):
    os.unlink("scratch\%s%s.png"%(gp.filename,gp.filecounter))
  if (gp.keep==0):
    if os.path.exists("scratch\%s%s.png"%(gp.filename,(gp.filecounter-1))):
       os.unlink("scratch\%s%s.png"%(gp.filename,(gp.filecounter-1)))
  file=open("scratch\gnudata.dat","w")
  numberoflines=len(trace1)
  if (len(trace2)>len(trace1)):
    numberoflines=lentrace2
  for i in range(0,numberoflines):
    val1="   "
    val2="   "
    if (i<len(trace1)):
       val1=trace1[i]
    if (i<len(trace2)):
       val2=trace2[i]
    file.write("%s %s %s\n"%(i,val1,val2))
  file.close()
  g('reset')
  for j in range(len(gp.precommands)):
    g(gp.precommands[j])
  for k in coms:
    g(k)
  #g('set style line 1 lt 2 lw 2')
  #g('set style line 2 lt 3 lw 2')
  for ts in tstamps1:
   str="set arrow from %s, graph 0 to %s, graph 1 nohead front lt 2 lw 2"%(ts,ts)
   g(str)
  for ts in tstamps2:
   str="set arrow from %s, graph 0 to %s, graph 1 nohead lt 4 lw 4"%(ts,ts)
   g(str)
  g('unset key')
  g('set term png size 400,200')
  str = 'set out "scratch/%s%s.png"'%(gp.filename,gp.filecounter)
  g(str)
  if (type=="ts"):
    command="u 1:2 w lines"
    if (len(trace2)>0):
       command="u 1:2 w lines, 'scratch/gnudata.dat' u 1:3 w lines"
  if (type=="xy"):
    command="u 2:3 w points"
  
  str="plot 'scratch/gnudata.dat' %s"%command
  g(str)
  g('set out')
  jh.quickGnuplot("scratch//%s%s.png"%(gp.filename,gp.filecounter))

  



def setupfftanalysis():
   id=w.openImageDecoder("ompro")
   mV=Matrix()
   mC=Matrix()
   mV.initialize(id,0,820,0,16,0,16)
   mC.initialize(id,0,820,0,16,16,32)
   traceV=mV.dat.section(5,5)
   traceC=mC.dat.section(5,5)
   savegnuplottraces(traceV,traceC,10,"omprotracedata.dat")
   mV.processInSpace() #get rid of holes
   mdv,mdc,mcorr=fftDomFreq(mV,mC,128,32,0.01)
   suppressNoisyMatrices(mcorr,mdv,mdc,0.9)
   delta(mcorr,0)
   mdc.subtract(mdv)
   absval(mdc)
   g=Gnuplot.Gnuplot(debug=1)
   createmultiplot(mcorr,mdv,mdc,7,14,32,"omprotracedata.dat")
   return mcorr,mdc,mdv
   


def fftDomFreq(mV, mC, fftlength, steplength,timestep):
   #for each trace in matrix, generate a running fft
   steps=int(mV.zdim/(steplength*1.0))
   w.print(steps)
   mDomFreqCa=Matrix()
   mDomFreqV=Matrix()
   mCorrel=Matrix()
   mDomFreqCa.create(steps,mV.xdim,mV.ydim)
   mDomFreqV.create(steps,mV.xdim,mV.ydim)
   mCorrel.create(steps,mV.xdim,mV.ydim)
   for zindex in range(steps):
     z=zindex*steplength
     for x in range(mV.xdim):
       for y in range(mV.ydim):
          trV=mV.dat.section(y,x,z,z+fftlength)
          trC=mC.dat.section(y,x,z,z+fftlength)
          fftV=mV.fft(trV)
          fftC=mC.fft(trC)
          #dominant frequency?
          xV,yV=highestfrequencies(fftV,1)
          xC,yC=highestfrequencies(fftC,1)    
          mDomFreqV.dat.set(zindex,x,y,xV[0]/timestep)
          mDomFreqCa.dat.set(zindex,x,y,xC[0]/timestep)
          mCorrel.dat.set(zindex,x,y,array.correlation(fftV,fftC))
   return mDomFreqV,mDomFreqCa,mCorrel

def writegnuplotdata(matrix,zindex,filename):
  file=open(filename,"w")
  for x in range(matrix.xdim):
   for y in range(matrix.ydim):
    file.write("%s %s %s\n" % (x,y,matrix.dat.get(zindex,x,y)))
   file.write("\n")
  file.close()

def savegnuplottraces(tr1,tr2,timestep,filename):
   trn1=normalize(tr1)
   trn2=normalize(tr2)
   file=open(filename,'w')
   for i in range(len(tr1)):
      str="%s %s %s\n" % ((i*timestep), trn1[i], trn2[i])
      file.write(str)
   file.close()  

def suppressNoisyMatrices(ma1,ma2,ma3,mincorrelation):
   for i in range(ma1.xdim):
    for j in range (ma1.ydim):
      if ((ma1.dat.get(0,i,j))<mincorrelation):
       ma2.suppress(i,j)
       ma3.suppress(i,j)
       ma1.suppress(i,j)
       
def delta(ma,index):
  tmp=[]
  for y in range(ma.ydim):
    for x in range (ma.xdim):
      tmp.append(ma.dat.get(index,y,x))
  for i in range(ma.zdim):
    for y in range (ma.ydim):
      for x in range (ma.xdim):
         ma.dat.set(i,y,x,(ma.dat.get(i,y,x)-tmp[y*ma.xdim+x]))
         if (ma.dat.get(i,y,x)<0):
           ma.dat.set(i,y,x,ma.dat.get(i,y,x)*-1.0)       
       
def absval(ma):
  for i in range(ma.zdim):
    for y in range (ma.ydim):
      for x in range (ma.xdim):
         if (ma.dat.get(i,y,x)<0):
           ma.dat.set(i,y,x,ma.dat.get(i,y,x)*-1.0)
       
def activationmap(timestamps,timestart,timeend,startx=0,endx=16,starty=0,endy=16,xdim=16,ydim=16):
  file=open("tmpgnucontour.dat","w")
  for i in range(starty,endy):
    for j in range(startx,endx):
      times=timestamps[i*ydim+j]
      val=timestart
      for t in times:
        if ((t>timestart) and (t<timeend)):
          val=t
          break
      file.write("%s %s %s\n" %(i,j,val))
    file.write("\n") 
  file.close()  

def showcontour():
  g('reset')
  g('set yrange[15:0]')
  g('set xrange[0:15]')
  g('set term png crop size 300,300')
  g('set out "contour.png"')
  g('set multiplot')
  g('unset xtics; unset ytics; unset key')
  g('unset colorbox')
  g('set pm3d map')
  g('set pm3d explicit')
  g('set dgrid3d 100,100')
  g('set isosamples 100; set samples 100')
  g('splot "tmpgnucontour.dat" with pm3d')
  g('unset dgrid3d')
  g('unset pm3d')
  g('unset xtics; unset ytics; unset key')
  g('set contour')
  g('unset surface')
  g('unset grid')
  g('set cntrparam bspline; set cntrparam order 8')
  g('splot "tmpgnucontour.dat" with lines') 
  g('unset multiplot')
  g('set out')


def createmultiplot(M1,M2,M3,start,end,steplength,tracefilename):
  g('reset')
  g('unset multiplot')
  g('set nosurface')
  g('set contour')
  g('set cntrparam bspline')
  g('set cntrparam order 8')
  g('set cntrparam levels 10')
  g('set clabel')
  g('unset xtics')
  g('unset ytics')
  g('set yrange[15:0]')
  g('set origin 0.01,0.1')
  g('set size 0.9,0.9')
  g('unset key')
  g('set multiplot')
  g('set size 0.3,0.55')
  g('set cntrparam levels discrete 0.01,0.03,0.05')
  g('set view 0,0,1,1')
  for z in range(start,end):
    filestr="tmp%s.dat"%(z)
    writegnuplotdata(M1,z,filestr)
    str="set origin %s,%s"%(((z-start)*0.155),0.50)
    g(str)
    strtitle="set title '%s'"%(z*steplength*10)
    g(strtitle)
    cmdstr='splot "tmp%s.dat" w l'%(z)
    g(cmdstr)
    g('unset title')

  g('set cntrparam levels discrete 1,2,3,4,5')
  for z in range(start,end):
    filestr="vtmp%s.dat"%(z)
    writegnuplotdata(M2,z,filestr)
    str="set origin %s,%s"%(((z-start)*0.155),0.25)
    g(str)
    #strtitle="set title '%s'"%(z*steplength)
    #g(strtitle)
    cmdstr='splot "vtmp%s.dat" w l'%(z)
    g(cmdstr)
    #g('unset title')
 
  g('set cntrparam levels discrete 1,2,3,4,5')
  for z in range(start,end):
    filestr="ctmp%s.dat"%(z)
    writegnuplotdata(M3,z,filestr)
    str="set origin %s,%s"%(((z-start)*0.155),0.0)
    g(str)
    #strtitle="set title '%s'"%(z*steplength)
    #g(strtitle)
    cmdstr='splot "ctmp%s.dat" w l'%(z)
    g(cmdstr)
    #g('unset title')
  xstart=start*steplength*10
  xend=end*steplength*10
  g('set xrange[*:*]')
  g('set yrange[0:15]')
  g('set autoscale y')
  g('set xtics')
  str='set xrange[%s:%s]'%(xstart,xend)
  g(str)
  g('set origin 0.04,0')
  g('set size 0.99,0.2')
  str='plot "%s" u 1:2 w l, "%s" u 1:3 w l'%(tracefilename,tracefilename) 
  g(str)

def savegnuplottraces(tr1,tr2,timestep,filename):
   trn1=normalize(tr1)
   trn2=normalize(tr2)
   file=open(filename,'w')
   for i in range(len(tr1)):
      str="%s %s %s\n" % ((i*timestep), trn1[i], trn2[i])
      file.write(str)
   file.close()


 


def highestfrequencies(fft,cutoff=5):
  r=len(fft)*1.0
  fft=fft[cutoff:int(r/2.0)]
  myfft=[]
  for i in fft:
   myfft.append(i)
  xs=[]
  ys=[] 
  for i in range(20):
    hi=max(myfft)
    loc=myfft.index(hi)
    frequency=(loc+cutoff)
    xs.append(frequency/r)
    ys.append(hi)
    myfft.remove(hi)
  return xs,ys
   

def prettyprint(ma):
  for z in range(ma.zdim):
    for y in range (ma.ydim):
      str=""
      for x in range (ma.xdim):
        str+=" %2.2f" % ma.dat.get(z,y,x)
      w.print(str)
    w.print("")
    
        
 
def highestperiods(fft,cutoff=5):
  r=len(fft)*1.0
  fft=fft[cutoff:int(r/2.0)]
  myfft=[]
  for i in fft:
   myfft.append(i)
  xs=[]
  ys=[] 
  for i in range(20):
    hi=max(myfft)
    loc=myfft.index(hi)
    period=1.0/(loc+cutoff)*r
    xs.append(period)
    ys.append(hi)
    myfft.remove(hi)
  return xs,ys
  
def subtractbackground(trace):
  sub=engineermath.runningAverage(trace,51)
  subbed=[(trace[i]-sub[i]) for i in range(len(trace))]
  return subbed

def plottimeseries(trace):
  jh.quickPlot([x for x in range(len(trace))],trace)
  
def plotaddtimestamps(timestamps,height):
  ys=[height]*len(timestamps)
  jh.pl.p.setImpulses(1,jh.pl.dataset)
  jh.pl.addPlot(timestamps,ys)
  jh.pl.repaint()

def performfftvartest(trace,plot=0):
   xvar,yvar=variancefilter(trace,20)
   xfft,yfft=runningfft(trace,3000000)
   #find spikes in yvar
   mean=array.mean(yvar)
   mmax=max(yvar)
   mmin=min(yvar)
   threshold=mmin+((mmax-mmin)/2.0)
   bursts=[]
   #spikes are vals in yvar that are
   for k in range(1,len(yvar)):
     if ((yvar[k-1]<threshold) and (yvar[k]>threshold)):
          #check to see whether fft picks up any periods in the range 8 to 20.
          for j in range(1,len(xfft)):
             if ((k>=xfft[j-1]) and (k<xfft[j])):
                   if ((yfft[j]<22) and (yfft[j]>8)):
                      bursts.append(k)
   if (plot==1):
     jh.quickPlot(xvar,yvar)
     jh.quickPlot(xfft,yfft)
   return bursts,xvar,yvar,xfft,yfft

                

def curvetest(xs,ys,a,b):
     for x in range (1,30):
        xs.append(x)
        ys.append(a*(x*1.0)**b) 

def correlate(x,y):
	tmpV=ma.getDoubles(x,y)
	tmpC=ma.getDoubles(x,(y+16))
	cor=array.correlation(tmpV,tmpC)
	gv.jh.print(cor)
	return cor

def plot(x,y):
	vt=gv.jh.vw[gv.jh.index]
	vt.jp.showCursor(x,y)
	vt.jp.showCursor(x,(y+16))
	tmpV=ma.getDoubles(x,y)
	tmpC=ma.getDoubles(x,(y+16))
	if gv.jh.pl is None:
		gv.jh.quickPlot(tmpV,0,1000,0)
    	gv.jh.pl.p.clear(0)
	gv.jh.pl.p.clear(1)
	gv.jh.pl.p.clear(2)
	gv.jh.pl.dataset=1
	gv.jh.pl.addPlot(tmpV)
	gv.jh.pl.addPlot(tmpC)

def plotlast():
	x=gv.jh.getX()
	y=gv.jh.getY()
	plot(x,y)



#def print(x):
#	w.print(x.__str__(self))	
	

def corrmap():
	arr=[]
	for x in range (0,15):
		ar2=[]
		for y in range (0,15):
			val=correlate(x,y)
			if (val<0):
				 val=-1*val
			ar2.append(val)
		arr.append(ar2)
	graph.image(arr)
	return arr


def getmatrix(start,end):
	ma=Matrix()
	ma.initialize(gv.jh.vw[gv.jh.index],start,end)
	gv.openImageFile(ma,"matrix")
	return ma

def getmatrix(index,start,end):
	ma=Matrix()
	ma.initialize(gv.jh.vw[index],start,end)
	gv.openImageFile(ma,"matrix")
	return ma
	
	

def getrois():
      return gv.jh.vw[gv.jh.index].jp.rois

def regetmatrix(start,end,index):
	ma=Matrix()
	ma.initialize(gv.jh.vw[index],start,end)
	gv.openImageFile(ma,"matrix")
	return ma

def fftcormap(arg):
	ma.processByTrace("s",101)
	ma.processByTrace("n")
	ma.processByTrace("a",11)
	ma.processByTrace("m",5)
	ma.processByTrace("n")
	ma.FFTcorrelation()
	ma.SaveCorrelation(arg)


def compile(str,name):
  filename="c:\\CD\\programs\\java\\gvdecoder\\scripts\\anonclasses\\%s.java"%name
  file=open(filename,"w") 
  header="""
package gvdecoder.scripts.anonclasses;

import gvdecoder.Matrix;
import gvdecoder.array.doubleArray3D;

"""             
  file.write(header)
  file.write("public class %s{\n"%name)
  file.write(str)
  file.write("}\n")
  file.close()
  compilecommand="C:\\CD\\jdk1.6\\bin\\javac %s"%(filename)
  p=java.lang.Runtime.getRuntime().exec(compilecommand)
  inputstream=p.getErrorStream()   
  ja=java.io.BufferedReader(java.io.InputStreamReader(inputstream))
  w.print(compilecommand)
  java.lang.Thread.sleep(2000)
  if (ja.ready()):
   for i in range(20):
    if ja.ready():
     w.print(ja.readLine())
   return None
  classname="gvdecoder.scripts.anonclasses.%s"%name
  classinstance="c=%s()"%classname
  gv.jv.editor_runString(classinstance)
  return c
  
  
"""
 convenience functions for viewers
 called from console .v followed by one number or 3. If one, then jump to frame, if 4 then frame,zoom sx,sy
"""

def viewerframe(val):
 if jh.presentviewer==None:
   return "viewer: none selected"
 jh.presentviewer.JumpToFrame(val)
 jh.presentviewer.updateTitle()

def viewerzoom(scale,xoff,yoff):
 if jh.presentviewer==None:
   return "viewer: none selected"
 vw=jh.presentviewer

 vw.jp.offsetx=-1*int(xoff*scale)
 vw.jp.offsety=-1*int(yoff*scale)
 if ((scale>0.1) and (scale<12.1)):
    vw.setVisualScale(scale)
 #cp.scalefield.setText("done")
 vw.updateTitle()
 return "viewer: zoomed"
 
def viewerscale():
 if jh.presentviewer==None:
   return "viewer: none selected"
 jh.presentviewer.findScaleOffset() 
 return "viewer: rescaled"
 
def viewersubtract():
 if jh.presentviewer==None:
   return "viewer: none selected"
 jh.presentviewer.JavaBackground()
 return "viewer: subtracted background"

def viewernormalize():
 if jh.presentviewer==None:
   return "viewer: none selected"
 jh.presentviewer.JavaNormalize()
 return "viewer: normalized values"

def viewerraw():
 if jh.presentviewer==None:
   return "viewer: none selected"
 jh.presentviewer.JavaRaw()
 return "viewer: displaying raw"

def viewervalue(y,x):
 if jh.presentviewer==None:
   return "viewer: none selected"
 vw=jh.presentviewer 
 return vw.datArray[y*vw.X_dim+x]

def makeroi(x1,y1,x2,y2):
 vw=jh.presentviewer
 if vw==None:
  return "roi: no viewer open"
 if x2<=x1 or y2<=y1:
  return "roi: dimensions less than 1 (use coordinates, not width,height)"
 brightColor=vw.getRoiColor()
 vw.jp.presentroi=gvdecoder.ROI(vw,brightColor);
 vw.jp.presentroi.poly.addPoint(x1,y1)
 vw.jp.presentroi.poly.addPoint(x2,y1)
 vw.jp.presentroi.poly.addPoint(x2,y2)
 vw.jp.presentroi.poly.addPoint(x1,y2)
 count=vw.jp.presentroi.findAllPixels(vw.X_dim,vw.Y_dim)
 vw.jp.rois.add(vw.jp.presentroi)
 pixels=vw.jp.presentroi.getAverageVal(vw.X_dim,vw.Y_dim)
 return ("roi: average=%s, n=%s"%(pixels,count))

def roideletelast():
 vw=jh.presentviewer
 if (vw==None):
  return "rois: no viewer selected"
 r=vw.jp.rois
 l=len(vw.jp.rois)
 if l<1:
  return "rois: roi list empty"
 vw.jp.rois.remove(l-1)
 return "rois: deleted last"

def roideleteall():
 vw=jh.presentviewer
 if (vw==None):
  return "rois: no viewer selected"
 r=vw.jp.rois
 l=len(vw.jp.rois)
 if l<1:
  return "rois: roi list empty"
 vw.jp.deleteAllROIs()
 return "rois: deleted all"




def roiscale():
 vw=jh.presentviewer
 if (vw==None):
  return "rois: no viewer selected"
 r=vw.jp.rois
 l=len(vw.jp.rois)
 if l<1:
  return "rois: roi list empty"
 vw.jp.rois[l-1].findAllPixels(vw.X_dim, vw.Y_dim)
 vw.findScaleOffsetRoi(vw.jp.rois[l-1])
 vw.rescale()
 vw.repaint()
 return "roi: scaled to roi"


def roisave():
 vw=jh.presentviewer
 if (vw==None):
  return "rois: no viewer selected"
 r=vw.jp.rois
 l=len(vw.jp.rois)
 if l<1:
  return "rois: roi list empty"
 vw.saveROIs()
 return "roi: saved"

def roiload():
 vw=jh.presentviewer
 if (vw==None):
  return "rois: no viewer selected"
 vw.loadROIs()
 return "roi: loaded"

def roiprocess():
 vw=jh.presentviewer
 if vw==None:
  return "rois: no viewer selected"
 vw.processRois(0,-1)
 return vw.getRoiData()
 
"""
Function that is called if an image is saved. Can override in script
"""
ah=gvdecoder.AnalysisHelper().getAnalysisHelper()
ah.gv=gv
def imageSaved(imagename):
 w.print("image saved %s"%imagename)
 
