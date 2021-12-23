
unset multiplot
set size 1.0,1.0
set origin 0.0,0.0
set line style 2 pt 6 ps 2.0
set line style 1 pt 3 ps 1.0
unset label
set term post eps
set out "2by1.eps"
set multiplot
set size 0.3,0.37
set origin 0.3,0.4
set xrange [:50000]
set xtics ("1"10,"2"100,"3"1000,"4"10000) font "Times-Roman,20"
set ytics ("1"10,"2"100) font "Times-Roman,20"
plot "dat/final_2_05_2.dat"  ls 1, "dat//final_8_05_2.dat"   ls 2
set origin 0.55,0.4
set label "S" at screen 0.57, screen 0.39 font "Times-Roman,26"
set label "N" at screen 0.3, screen 0.6 font "Times-Roman,26" rotate
set ytics(" "10," "100)
#set size 0.295,0.37
plot "k050.dat" u ($1/3):($2)  ls 1 , "k100.dat" u ($1/3):2  ls 2 
#set label "size" at screen 0.2, screen 0.2 font Arial,16
#set xtics(""0,""10,""100,""1000,""10000)
unset multiplot
set out
