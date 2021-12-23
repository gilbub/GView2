
unset multiplot
set size 1.0,1.0
set origin 0.0,0.0
#set term post eps
#set out "3by33.eps"
set multiplot
set size 0.3,0.37
set ytics(""0,""10,""100,""1000,""10000)
#col 1
set origin 0.6,0.6
set xtics(""0,""10,""100,""1000,""10000)
plot "final_8_cont_0.dat"  ls 1
set origin 0.6,0.3
plot "final_4_cont_0.dat" ls 1
set origin 0.6,0.0
set xtics("0"1,"1"10,"2"100,"3"1000,"4"10000)
plot "final_2_cont_0.dat" ls 1

#col 2
set xtics(""0,""10,""100,""1000,""10000)
set origin 0.35,0.6
plot "final_8_05_0.dat" ls 1
set origin 0.35,0.3
plot "final_4_05_2.dat" ls 1
set origin 0.35,0.0
set xtics("0"1,"1"10,"2"100,"3"1000,"4"10000)
plot "final_2_05_0.dat" ls 1

#col 3
#set ytics("0"1,"1"10,"2"100,"3"1000)
set size 0.31,0.37
set xtics(""0,""10,""100,""1000,""10000)
set origin 0.09,0.6
plot "final_8_10_0.dat" ls 1
set origin 0.09,0.3
plot "final_4_10_0.dat" ls 1
set origin 0.09,0.0
set xtics("0"1,"1"10,"2"100,"3"1000,"4"10000)
plot "final_2_10_0.dat" ls 1
unset multiplot
set out