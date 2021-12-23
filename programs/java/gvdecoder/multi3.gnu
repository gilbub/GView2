
unset multiplot
set term win
set size 1.0,1.0
set origin 0.0,0.0
set line style 2 pt 6 ps 2.0
set line style 1 pt 3 ps 1.0
unset label
set term post eps
set out "4by1.eps"
set multiplot
set size 0.25,0.35
set origin 0.1,0.4
set xrange [:50000]
set xtics ("1"10,"2"100,"3"1000,"4"10000) font "Times-Roman,20"
set ytics ("0"0.5 ,"1"10,"2"100) font "Times-Roman,20"
plot "dat//final_8_05_2.dat"   ls 2

#plot "dat/final_2_05_2.dat"  ls 1 
set origin 0.3,0.4
set ytics(" "10," "100)
#plot "dat//final_8_05_2.dat"   ls 2
plot "dat/final_2_05_2.dat"  ls 2 
set origin 0.5,0.4
set label "S" at screen 0.52, screen 0.4 font "Times-Roman,26"
set label "N" at screen 0.08, screen 0.6 font "Times-Roman,26" rotate

#set size 0.295,0.37
plot "k100.dat" u ($1/3):2  ls 2
#plot "k050.dat" u ($1/3):($2)  ls 1  
set origin 0.7,0.4
#plot "k100.dat" u ($1/3):2  ls 2 
plot "k050.dat" u ($1/3):($2)  ls 2
#set label "size" at screen 0.2, screen 0.2 font Arial,16
#set xtics(""0,""10,""100,""1000,""10000)
unset multiplot
set out
