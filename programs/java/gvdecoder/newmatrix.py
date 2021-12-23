maV=Matrix()
maC=Matrix()
id=w.openImageDecoder("ompro")
maV.initialize(id,0,500,0,16,0,16)
maC.initialize(id,0,500,0,16,16,32)
gv.openImageFile(maV,"maV")
gv.openImageFile(maC,"maC")
def fftDomPeriod(Matrix mV, Matrix mC, fftlength, steplength )
   #for each trace in matrix, generate a running fft
   steps=int(mV.zdim/(fftlength*1.0))
   for zindex in range(steps):
     z=zindex*fftlength 
     for x in range(mV.xdim):
       for y in range(mV.ydim):
          trV=mV.arr.section(y,x,z,z+fftlength)
          trC=mC.arr.section(y,x,z,z+fftlength)
          fftV=mV.fft(trV)
          fftC=mC.fft(trC)
     