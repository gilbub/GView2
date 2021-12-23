from visad.python.JPythonMethods import *
import GView
import Matrix
import graph
import JSci.maths.ArrayMath as array
import PowerSpectrum

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


gv=GView()	
gv.show()