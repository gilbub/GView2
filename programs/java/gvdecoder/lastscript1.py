def doJungCalculation(ma):
  ma.processByTrace("n")
  ma.BlobCount(400)
  blobs1=findblobsize(ma.dat.arr)
  blobs2=groupblobs(blobs1)
  xs,ys=sortDictionary(blobs2)
  return xs,ys

def normalize(arr):
  maxx=max(arr)*1.0
  return [x/maxx for x in arr]

def BlobCount(id,ma,thresh,indexstarts,indexends):
  dic={}
  d=int((indexends-indexstarts)/500.0) 
  w.print("d=%s\n"%d)
  for i in range(d):
   start=indexstarts+i*500
   end=indexstarts+(i+1)*500
   w.print("%s %s\n"%(start,end))
   ma.initialize(id,start,end,0,50,0,50)
   ma.processByTrace("n")
   xs,ys=ma.BlobCount(500,10000,2,0)
   for i in range(len(xs)):
     sumdict_group(dic,xs[i],ys[i])
  xs,ys=sortDictionary(dic)
  return xs,ys
   
 

def doJungCalculation2(ma,thresh):
  ma.BlobCount(thresh)
  blobs1=findblobsize(ma.dat.arr)
  blobs2=groupblobs(blobs1)
  xs,ys=sortDictionary(blobs2)
  w.print(("Entropy = %s\n"%entropy(xs,ys)))
  getslope(xs,ys)
  return xs,ys

def cleandata(ma):
  mback=Matrix(ma)
  #mback.initialize(ma)
  mback.processInSpace()
  mback.processByTrace("a",75)
  ma.subtract(mback)
  ma.processByTrace("n")
  ma.suppressEdges(31,31,31)
  



def findblobsize(arr):
   cnt=[]
   dict={}
   for i in range(0,len(arr)):
      if (arr[i]>0):
         sumdict(dict,arr[i])
   return dict   

def doJungPrecounted(ys):
  blobs1=findblobsize(ys)
  #blobs2=groupblobs(blobs1)
  xs1,ys1=sortDictionary(blobs1)
  return xs1,ys1

def sumdict_group(dict,arg,size):
   if (dict.has_key(arg)):
      val=dict[arg]
      val=val+size
      dict[arg]=val
   else:
      dict[arg]=size

def savexys(xs,ys,filename):
   file=open(filename,'w')
   for i in range(len(xs)):
      str="%s %s\n" % (xs[i],ys[i])
      file.write(str)
   file.close()	

def getslope(xs,ys):
  trimto=len(xs)
  for i in range(len(xs)):
    if (xs[i]>100):
      trimto=i
      break
  w.print(("trim from %s to %s\n"%(len(xs),trimto)))
  xs=xs[:trimto]
  ys=ys[:trimto]
  lx=[math.log10(x) for x in xs]
  ys=[math.log10(y) for y in ys]
  cf=CurveFitter(lx,ys)
  cf.doFit(cf.STRAIGHT_LINE,0)
  w.print(cf.getResultString())
  a,b,c=cf.getParams()
  return a,b,c,cf.getSD(),cf.getFitGoodness()


def logNbin(xs,ys,N):
  res={} 
  for j in range(len(xs)):
     x=xs[j]
     y=ys[j]
     for i in range(30):
      if (x>=math.pow(N,i)) and (x<math.pow(N,i+1)):
         #nx=(math.pow(N,i)+math.pow(N,i+1))/2.0+math.pow(N,i)
         nx=(math.pow(N,i))
         sumdict_group(res,nx,y)
         break
  return res


def readxys(filename):
  file=open(filename,"r")
  strs=file.readlines()
  xs=[]
  ys=[]
  for x in strs:
    a,b=x.split(" ")
    xs.append(float(a))
    ys.append(float(b))
  file.close()
  return xs,ys

def readxys_5000(filename):
  file=open(filename,"r")
  xs=[]
  ys=[]
  for x in range(6000):
    str=file.readline()
    if (x>=1000):
      a,b=str.split(" ")
      xs.append(int(a))
      ys.append(int(b))
  file.close()
  return xs,ys
  
def binentropy(xs,ys):
  dic=logNbin(xs,ys,2)
  x2,y2=sortDictionary(dic)
  return entropy(x2,y2)

commandstr="""# CA caalt2.cpp 
# ARRAY
xdim=50
ydim=50
distancejigglewidth=1.000000
strengthjigglewidth=1.000000
randomseed=8
# PARAMETERS
A=6
R=7
theta=0.35
fatAdd=0.020000
fatSub=0.99
fatMin=0.9
probability=0.999
#distance=2.500
fractionoscillators=0.0
oscillatormin=180
oscillatorrange=40
fractionclamped=0.0
edgeclamped=1
# PROGRAM
initcond=none
paramname_I=fatSub
start_I=0.9850
end_I=0.98501
increment_I=0.1
paramname_J=fatAdd
start_J=0.02
end_J=0.0201
increment_J=0.05
# TEST (1=spiralperiod, 2=planarwavespeed, 3=spiralexists)
test=1
testonly=0
checkdead=0
maxits=15000
startstatistics=100
statisticsperiod=25
outfile=c_9900_22_30.dat
gui=0"""

def autorun(id,ma,density,distance):
  file=open("paramfile3.txt","w")
  str=commandstr+"\ndensity=%s"%density
  str=str+"\ndistance=%s\n"%distance
  file.write(str)
  file.close()
  os.system("c:\\programs\\cpp\\caalt9a.exe parameterfile=paramfile3.txt")
  w.print("done!\n")
  xs,ys=BlobCount(id,ma,500,0,9000)
  strtmp="dist_%s_dens_%s.dat"%(distance,density)
  savexys(xs,ys,strtmp)
  return xs,ys

def str(theta, r):
  cells=int((2*r)*(2*r))
  amp=cells/9.0
  return amp
