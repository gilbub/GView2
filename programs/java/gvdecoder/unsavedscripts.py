def writegnuplotdata(matrix,zindex,filename):
  file=open(filename,"w")
  for x in range(matrix.xdim):
   for y in range(matrix.ydim):
    file.write("%s %s %s\n" % (x,y,matrix.dat.get(zindex,x,y)))
   file.write("\n")
  file.close()

def createmultiplot(M1,M2,M3,start,end,steplength):
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
  g('set cntrparam levels discrete 0.1,0.3,0.5')
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


def suppressNoisyMatrices(ma1,ma2,ma3):
   for i in range(ma1.xdim):
    for j in range (ma1.ydim):
      if ((ma1.dat.get(0,i,j))<0.95):
       ma2.suppress(i,j)
       ma3.suppress(i,j)
       ma1.suppress(i,j)
 
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

def savelist(xs,filename):
   file=open(filename,'w')
   for i in range(len(xs)):
      str="%s\n" % xs[i]
      file.write(str)
   file.close()	

    
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
  
 
