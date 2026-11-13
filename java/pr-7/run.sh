#!/bin/sh
# Збирає проєкт і запускає досліди в docker; кожен запуск пишеться в results/*.txt.
# Запуск із теки роботи: sh run.sh
cd "$(dirname "$0")" || exit 1
T=../../tools
P=ua.duikt.lavrishko.pr7
$T/mvn.sh -q clean package || exit 1
sh compile-errors/run.sh
run() {  # run <файл результату> <клас> [аргументи]
  f=$1; c=$2; shift 2
  a="$*"
  $T/runlog.sh -t -d "java -cp target/classes $P.$c${a:+ $a}" "results/$f.txt" -- $T/java.sh -cp target/classes $P.$c "$@"
}
run exp1_sort Exp1Sort
run exp2_sum Exp2Sum
run exp2_peek_terminal Exp2Peek terminal
run exp2_peek_lazy Exp2Peek lazy
run exp3_optional Exp3Optional
run exp3_get Exp3Get
