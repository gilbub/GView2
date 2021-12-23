set nosurface
set contour
set cntrparam bspline
set cntrparam order 8
set cntrparam levels 10
set noclabel

#set term table
#set out 'tmpxxx'
set view 0,0,1,1
set size 0.66,1.0
splot "c:/programs/java/decoder/tmpgnudat.dat" w l
#set term win
#plot 'tmpxxx' w l
pause -1