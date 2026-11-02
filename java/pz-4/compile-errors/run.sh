#!/bin/sh
# Компілює хибні кейси справжнім javac (JDK 21 у docker); вивід у results/. Спершу: ../../tools/mvn.sh -q clean package
cd "$(dirname "$0")/.." || exit 1
T=../../tools
for case in E1_new_abstract E2_not_implemented E2b_implemented E3_interface_view E3b_exportable_view E4_default_conflict; do
  $T/runlog.sh -t -d "javac -encoding UTF-8 -cp target/classes compile-errors/$case/*.java" \
    results/$case.txt -- $T/javac.sh -encoding UTF-8 -d /tmp/out -cp target/classes compile-errors/$case/*.java
done
