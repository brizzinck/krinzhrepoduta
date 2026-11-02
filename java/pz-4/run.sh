#!/bin/sh
# Запуски дослідів і доказів у results/. Спершу: ../../tools/mvn.sh -q clean package
# Результати етапу 1 досліду 3 (exp3_after_stage1.txt, exp3_hash_stage1.txt) записано до додавання SmsNotifier
# і цим скриптом не перезаписуються (знімок сервісу: snapshots/NotificationService.stage1.java).
cd "$(dirname "$0")" || exit 1
T=../../tools
P=ua.duikt.lavrishko.pr4
J="$T/java.sh -cp target/classes"
SVC=src/main/java/ua/duikt/lavrishko/pr4/exp3/after/NotificationService.java
$T/runlog.sh -t -d "java -cp target/classes $P.exp1.Main" results/exp1.txt -- $J $P.exp1.Main
$T/runlog.sh -t -d "java -cp target/classes $P.exp2.Main" results/exp2.txt -- $J $P.exp2.Main
$T/runlog.sh -t -d "java -cp target/classes $P.exp3.before.Main" results/exp3_before.txt -- $J $P.exp3.before.Main
$T/runlog.sh -t -d "java -cp target/classes $P.exp3.naive.Main" results/exp3_naive.txt -- $J $P.exp3.naive.Main
$T/runlog.sh -t -d "java -cp target/classes $P.exp3.after.Main" results/exp3_after_stage2.txt -- $J $P.exp3.after.Main
$T/runlog.sh -t results/exp3_hash_stage2.txt -- sha256sum $SVC
$T/runlog.sh -t results/exp3_diff_naive.txt -- diff -u src/main/java/ua/duikt/lavrishko/pr4/exp3/before/NotificationService.java src/main/java/ua/duikt/lavrishko/pr4/exp3/naive/NotificationService.java
$T/runlog.sh -t results/exp3_diff_after.txt -- diff snapshots/NotificationService.stage1.java $SVC
./compile-errors/run.sh
