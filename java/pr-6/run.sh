#!/bin/sh
# Збирає проєкт і запускає досліди в docker; кожен запуск пишеться в results/*.txt.
# Запуск із теки роботи: sh run.sh
cd "$(dirname "$0")" || exit 1
T=../../tools
P=ua.duikt.lavrishko.pr6
$T/mvn.sh -q clean package || exit 1
sh compile-errors/run.sh
run() {  # run <файл результату> <клас>
  $T/runlog.sh -t -d "java -cp target/classes $P.$2" "results/$1.txt" -- $T/java.sh -cp target/classes $P.$2
}
run exp1_cast Exp1Cast
run exp1_total Exp1Total
run exp2_timing Exp2Timing
run exp2_cme Exp2Cme
run exp2_iterator Exp2Iterator
run exp3_equality Exp3Equality
