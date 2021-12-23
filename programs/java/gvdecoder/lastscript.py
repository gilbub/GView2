class gnuplotparams:
  
  def __init__(self,filename="tmp",filenumber=0,keep=0, *precommands):
     self.filecounter=filenumber
     self.filename=filename  
     self.keep=keep
     self.precommands=precommands



def gp_plot(gp,trace1,trace2=[],tstamps1=[],tstamps2=[],type="ts",coms=[]):
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

  
