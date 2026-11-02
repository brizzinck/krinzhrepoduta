#!/bin/sh
# Компілює кожен хибний кейс справжнім javac (JDK 21 у docker) і зберігає дослівний вивід у results/.
# Запуск із теки проєкту після: ../../tools/mvn.sh -q clean package
cd "$(dirname "$0")/.." || exit 1
T=../../tools
for case in E1_no_super E1b_no_constructor E1c_fixed E2_downcast E3_no_cast; do
  $T/runlog.sh -t -d "javac -encoding UTF-8 -cp target/classes compile-errors/$case/*.java" \
    results/$case.txt -- $T/javac.sh -encoding UTF-8 -d /tmp/out -cp target/classes compile-errors/$case/*.java
done
