#!/bin/sh
# Компілює хибний фрагмент справжнім javac (JDK 21) у docker; результат у results/<назва>.txt.
# Запуск із теки роботи після `mvn package`: sh compile-errors/run.sh
cd "$(dirname "$0")/.." || exit 1
T=../../tools
for d in compile-errors/E*/; do
  n=$(basename "$d")
  $T/runlog.sh -t -d "javac -encoding UTF-8 -cp target/classes -d /tmp/out ${d}*.java" "results/$n.txt" -- \
    $T/javac.sh -encoding UTF-8 -cp target/classes -d /tmp/out ${d}*.java
done
