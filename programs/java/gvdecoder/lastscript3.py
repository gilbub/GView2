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
   xs,ys=ma.BlobCount(500,10000,4,0)
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
  

def calc4cont(id,ma,start,stop,num):
  ma.initialize(id,start,stop,0,61,0,61)
  cleandata(ma)
  ma.processByTrace("n")
  ma.CAFilter(1.2,0.3,1.2,1,1.4,0)
  xs,ys=ma.BlobCount(700,10000,0,0)
  str="4contdatpt%s.dat"%num
  savexys(xs,ys,str)
  ma.undo()
  xs,ys=ma.BlobCount(1000,10000,0,0)
  str="4contdatpt%sb.dat"%num
  savexys(xs,ys,str)

def run4cont(id,ma):
  for i in range(1,2):
   calc2cont(id,ma,500*i+100,500*i+600,i+1)
   w.print("%s %s"%(500*i+100,500*i+600))



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


def linearBin(xs,ys,bins):
  res={}
  maxx=max(xs)
  N=maxx/bins
  for j in range(len(xs)):
    x=xs[j]
    y=ys[j]
    nx=int((x/N))
    sumdict_group(res,nx*N+1,y) 
  return res

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
maxits=7000
startstatistics=100
statisticsperiod=25
outfile=c_9900_22_30.dat
gui=0"""

def autorun(id2,ma,density,distance,seed):
  file=open("paramfile3.txt","w")
  str=commandstr+"\ndensity=%s"%density
  str=str+"\ndistance=%s"%distance
  str=str+"\nrandomseed=%s\n"%seed
  file.write(str)
  file.close()
  os.system("c:\\programs\\cpp\\caalt9a.exe parameterfile=paramfile3.txt")
  w.print("done!\n")
  xs,ys=BlobCount(id2,ma,500,0,3000)
  strtmp="__dist_%2.2f_dens_%2.2f_%s.dat"%(distance,density,seed)
  savexys(xs,ys,strtmp)
  #xe,ye=evens(xs,ys)
  #strtmp="__dist_%2.2f_dens_%2.2feven.dat"%(distance,density)
  #savexys(xe,ye,strtmp)
  return xs,ys

def bigscript(id,ma):
 dict={}
 density=0.5
 distance=1.4
 #21,27
 for i in range(1):
  for j in range(1):
   for k in range(5):
    # density=0.5+i*0.025
    density=1.0
    distance=1.4+j*0.2
    seed=k*10
    w.print(("doing density=%s distance=%s seed=%s\n"%(density,distance,seed)))
    x,y=autorun(id,ma,density,distance,seed)
    w.print(x)
    #x2,y2=evens(x,y)
    #str="entropy_dist_%2.3f_dens_%2.3f"%(distance,density)
    #dict[str]=entropy(x,y)
    #str="e_entropy_dist_%2.3f_dens_%2.3f"%(distance,density)
    #dict[str]=entropy(x2,y2)
    #str="t_%2.3f_dens_%2.3f"%(distance,density)
    #dict[str]=binentropy(x,y)
    #str="slope_dist_%2.3f_dens_%2.3f"%(distance,density)
    #if len(x2>4):
    #   dict[str]=getslope(x2,y2)   
 return dict   
      
def surface(dict):
  file=open("gnudict.dat","w")
  for i in range(10):
   for j in range (17):
     density=0.5+i*0.05
     distance=1.4+j*0.1
     bstr="b_entropy_dist_%2.3f_dens_%2.3f"%(distance,density)
     estr="e_entropy_dist_%2.3f_dens_%2.3f"%(distance,density)
     sstr="entropy_dist_%2.3f_dens_%2.3f"%(distance,density)
     slope="slope_dist_%2.3f_dens_%2.3f"%(distance,density)
     file.write("%2.3f %2.3f %s %s %s %s\n"%(density,distance, dict[sstr],dict[bstr],dict[estr],dict[slope]))
   file.write("\n") 
  file.close()
    

def recalcsurface():
  newfile=open("gnusurface.dat","w")
  for i in range(10):
   for j in range(17):
    density=0.5+i*0.05
    distance=1.4+j*0.1
    strtmp="dist_%2.3f_dens_%2.3f.dat"%(distance,density)
    xs,ys=readxys(strtmp)
    xe,ye=evens(xs,ys)
    dic=linearBin(xe,ye,10.0)
    xb,yb=sortDictionary(dic)
    ent10=entropy(xb,yb)
    dic=linearBin(xe,ye,100.0)
    xb,yb=sortDictionary(dic)
    ent100=entropy(xb,yb)
    newfile.write("%2.3f %2.3f %s %s %s\n"%(density,distance, entropy(xe,ye), ent10, ent100))
   newfile.write("\n") 
  newfile.close()
    
   

def evens(xs,ys):
  xe=[]
  ye=[]
  for i in range(len(xs)):
    if (xs[i]%2==0):
      xe.append(xs[i])
      ye.append(ys[i])
  return xe,ye


def str(theta, r):
  cells=int((2*r)*(2*r))
  amp=cells/9.0
  return amp


def combine(dict,xs,ys):
  for i in range(len(xs)):
    sumdict_group(dict,xs[i],ys[i])
  return dict

def cleanscript(ma):
 cleandata(ma)
 ma.processByTrace("n")
 ma.CAFilter(1.2,0.3,1.2,1,1.5,0)
 ma.processByTrace("t",1,800,None,None)
 xs,ys=ma.BlobCount(500,1000,3,0)
 return xs,ys