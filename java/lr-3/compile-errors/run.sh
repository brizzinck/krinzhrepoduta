#!/bin/sh
# Компілює хибні кейси справжнім javac (JDK 21 у docker); вивід у results/. Спершу: ../../tools/mvn.sh -q clean package
cd "$(dirname "$0")/.." || exit 1
T=../../tools
for case in E1_override_final E2_abstract E3_missing_details; do
  $T/runlog.sh -t -d "javac -encoding UTF-8 -cp target/classes compile-errors/$case/*.java" \
    results/$case.txt -- $T/javac.sh -encoding UTF-8 -d /tmp/out -cp target/classes compile-errors/$case/*.java
done
