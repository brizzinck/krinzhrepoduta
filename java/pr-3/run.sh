#!/bin/sh
# Запускає досліди через docker і зберігає дослівний вивід у results/. Спершу: ../../tools/mvn.sh -q clean package
cd "$(dirname "$0")" || exit 1
T=../../tools
P=ua.duikt.lavrishko.pr3
$T/runlog.sh -t -d "java -cp target/classes $P.Main 1" results/exp1.txt -- $T/java.sh -cp target/classes $P.Main 1
$T/runlog.sh -t -d "java -cp target/classes $P.Main 2" results/exp2.txt -- $T/java.sh -cp target/classes $P.Main 2
$T/runlog.sh -t -d "java -cp target/classes $P.Main 3" results/exp3.txt -- $T/java.sh -cp target/classes $P.Main 3
$T/runlog.sh -t -d "java -cp target/classes $P.CastDemo" results/cast.txt -- $T/java.sh -cp target/classes $P.CastDemo
./compile-errors/run.sh
