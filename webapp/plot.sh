#!/bin/bash

INPUT=$1
OUTPUT=$2

gnuplot <<EOF

reset
set datafile separator ","
set term png size 1280,720 enhanced
# set term svg size 1600,1000 enhanced
# svg pngcairo transparent font "/usr/share/fonts/pfa/Helvetica.pfa"
set output "$OUTPUT"
# set multiplot

# Line style for axes
set style line 80 lt rgb "#808080"
set border 1+2+4+8 back linestyle 80

# Line style for grid
set style line 81 lt 0  # dashed
set style line 81 lt rgb "#808080"  # grey
set grid back linestyle 81
set grid

# X
set xtics nomirror
set xdata time
set timefmt "%.3s"
# set timefmt "%Y%m%d-%H%M%S"
set format x "%H:%M"
# set xlabel "time"

# Y
set ytics nomirror 1000 textcolor rgb "#dd0000"
set format y "%gft"
# set yrange [0:31]
# set ylabel "altitude"

# Y2
set y2tics 10 textcolor rgb "#0000dd"
set format y2 "%gmph";
# set y2range [0:130]


# set title "Jump Profile"
set key top right

# Data
set style line 1 lt rgb "#aa0000" lw 1 pt 1
set style line 2 lt rgb "#dd0000" lw 2 pt 1
set style line 3 lt rgb "#0000dd" lw 2 pt 1
set style line 12 lt rgb "#00a000" lw 2 pt 6
set style line 13 lt rgb "#5060d0" lw 2 pt 2
set style line 14 lt rgb "#f25900" lw 2 pt 9
set style data linespoints


plot "< grep Alti $INPUT" using (\$2 / 1000) : (\$5 * 3.28084) notitle with lines linestyle 2, \
     "$INPUT" using (\$2 / 1000) : (\$6 * -2.23694) notitle with lines linestyle 3 axes x1y2


EOF


