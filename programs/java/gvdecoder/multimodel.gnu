
unset multiplot
set size 1.0,1.0
set origin 0.0,0.0
set xrange [0.9:100000]
set yrange [0.5:100000]
#set term post eps
#set out "3by3model.eps"
set multiplot
set size 0.3,0.37
set ytics(""0,""10,""100,""1000,""10000)
#col 1
set origin 0.6,0.6
set xtics(""0,""10,""100,""1000,""10000)
plot "e://dat//g_dist_3.000_dens_1.000.dat"  ls 1
set origin 0.6,0.3
plot "e://dat//g_dist_3.000_dens_0.750.dat" ls 1
set origin 0.6,0.0
set xtics("0"1,"1"10,"2"100,"3"1000,"4"10000)
plot "e://dat//g_dist_3.000_dens_0.500.dat" ls 1

#col 2
set xtics(""0,""10,""100,""1000,""10000)
set origin 0.35,0.6
plot "e://dat//g_dist_2.500_dens_1.000.dat" ls 1
set origin 0.35,0.3
plot "e://dat//g_dist_2.500_dens_0.750.dat" ls 1
set origin 0.35,0.0
set xtics("0"1,"1"10,"2"100,"3"1000,"4"10000)
plot "e://dat//g_dist_2.500_dens_0.500.dat" ls 1

#col 3
#set ytics("0"1,"1"10,"2"100,"3"1000)
set size 0.31,0.37
set xtics(""0,""10,""100,""1000,""10000)
set origin 0.09,0.6
plot "e://dat//g_dist_2.000_dens_1.000.dat" ls 1
set origin 0.09,0.3
plot "e://dat//g_dist_2.000_dens_0.750.dat" ls 1
set origin 0.09,0.0
set xtics("0"1,"1"10,"2"100,"3"1000,"4"10000)
plot "e://dat//g_dist_2.000_dens_0.500.dat" ls 1
#unset multiplot
#set out