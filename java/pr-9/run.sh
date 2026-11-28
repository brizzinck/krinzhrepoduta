#!/bin/sh
# Збирає класи й запускає досліди в docker; кожен запуск пишеться в results/*.txt.
# Запуск із теки роботи: sh run.sh
cd "$(dirname "$0")" || exit 1
T=../../tools
P=ua.duikt.lavrishko.pr9
IMG=maven:3.9-eclipse-temurin-21
NAME=pr9-deadlock
mkdir -p results
$T/javac.sh -encoding UTF-8 -d target/classes src/main/java/ua/duikt/lavrishko/pr9/*.java || exit 1

# Дослід 1: по три запуски без synchronized і з ним, усі блоки в одному файлі
for mode in plain synced; do
  case $mode in plain) f=exp1_plain ;; synced) f=exp1_synced ;; esac
  $T/runlog.sh -t -d "java -cp target/classes $P.Exp1Race $mode" "results/$f.txt" -- $T/java.sh -cp target/classes $P.Exp1Race $mode
  $T/runlog.sh -d "java -cp target/classes $P.Exp1Race $mode" "results/$f.txt" -- $T/java.sh -cp target/classes $P.Exp1Race $mode
  $T/runlog.sh -d "java -cp target/classes $P.Exp1Race $mode" "results/$f.txt" -- $T/java.sh -cp target/classes $P.Exp1Race $mode
done

# Дослід 2, виправлена версія
$T/runlog.sh -t -d "java -cp target/classes $P.Exp2Deadlock ordered" results/exp2_ordered.txt -- $T/java.sh -cp target/classes $P.Exp2Deadlock ordered

# Дослід 2, зависання: програма працює в окремому контейнері, дамп знімається через docker exec.
# trap видаляє контейнер і target/ навіть після помилки чи Ctrl+C
trap 'docker rm -f $NAME >/dev/null 2>&1; rm -rf target' EXIT INT TERM
docker rm -f $NAME >/dev/null 2>&1
docker run -d --name $NAME -e LANG=C.UTF-8 -v "$PWD":/w:ro -w /w --entrypoint java $IMG -cp target/classes $P.Exp2Deadlock naive >/dev/null
sleep 2
F=results/exp2_deadlock.txt
$T/runlog.sh -t -d "docker logs $NAME" $F -- docker logs $NAME
$T/runlog.sh -d "docker ps --filter name=$NAME --format '{{.Status}}'   (Up означає, що програма зависла)" $F -- docker ps --filter name=$NAME --format '{{.Status}}'
$T/runlog.sh -d "docker exec $NAME jps -l" $F -- docker exec $NAME jps -l
$T/runlog.sh -d "docker exec $NAME jstack 1" $F -- docker exec $NAME jstack 1
