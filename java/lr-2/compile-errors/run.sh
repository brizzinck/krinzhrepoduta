#!/bin/sh
# Компілює кожен хибний фрагмент справжнім javac (JDK 21) у docker.
# Запуск із теки роботи після `mvn package`: sh compile-errors/run.sh
# Результат кожного фрагмента пишеться в results/<назва>.txt: команда, вивід javac, код завершення.
cd "$(dirname "$0")/.." || exit 1
T=../../tools
for d in compile-errors/E*/; do
  n=$(basename "$d")
  $T/runlog.sh -t -d "javac -encoding UTF-8 -cp target/classes -d /tmp/out ${d}*.java" "results/$n.txt" -- \
    $T/javac.sh -encoding UTF-8 -cp target/classes -d /tmp/out ${d}*.java
done
# Робочий код (усі класи проєкту) компілюється без помилок
$T/runlog.sh -t -d "javac -encoding UTF-8 -d /tmp/out \$(find src/main/java -name '*.java')" results/compile_ok.txt -- \
  sh -c "$T/javac.sh -encoding UTF-8 -d /tmp/out \$(find src/main/java -name '*.java')"
