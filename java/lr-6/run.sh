#!/bin/sh
# Збирає проєкт і запускає програму в docker; результат пишеться в results/main.txt.
# Запуск із теки роботи: sh run.sh
cd "$(dirname "$0")" || exit 1
T=../../tools
$T/mvn.sh -q clean package || exit 1
$T/runlog.sh -t results/main.txt -- $T/java.sh -cp target/classes ua.duikt.lavrishko.lr6.Main data/v2_issues.txt data/v2_members.txt
