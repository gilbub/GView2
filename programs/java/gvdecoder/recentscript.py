def dvdt(tr):
  res=[0.0]*len(tr)
  for x in range(1,len(tr)):
    res[x]=tr[x]-tr[x-1]
  return res  

def geteventtimes(trdvdt,threshold):
  return [x for x in range(1,len(trdvdt)) if ((trdvdt[x-1]< threshold) and (trdvdt[x]>threshold))]

def getibis(eventtimes): 
  return [eventtimes[x+1]-eventtimes[x] for x in range(0,len(eventtimes)-1)]

def getibisformatrix(ma,xstart,xend,ystart,yend,threshold):
  res=[]
  for i in range(xstart,xend):
   for j in range(ystart,yend):
    tr=ma.dat.section(j,i)
    trdvdt=dvdt(tr)
    eventtimes=geteventtimes(trdvdt,threshold)
    ibis=getibis(eventtimes)
    res.append(ibis)
    w.print(("appended %s %s len=%s"%(j,i,len(ibis))))
  return res