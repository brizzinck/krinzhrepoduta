#!/bin/sh
# Прогони приймальних тестів проти шести збірок: звичайний, і з іншими часовими поясами комп'ютера.
# Модуль підрядника виконується лише в docker без мережі (tools/mvn-sandbox.sh), без запису в репозиторій Maven.
# Передумова: jar встановлено командою з розділу 3 звіту, а mvn test-compile уже виконано онлайн
# (tools/mvn.sh test-compile), щоб усі плагіни були в кеші.
# Запуск із теки роботи: sh run.sh
cd "$(dirname "$0")" || exit 1
T=../../tools
S=$T/mvn-sandbox.sh
for n in 1 2 3 4 5 6; do
  $T/runlog.sh -t -d "mvn test -Dbuild=$n" results/build-$n.txt -- $S test -Dbuild=$n
done
for tz in America/Los_Angeles Pacific/Kiritimati; do
  f=results/tz-$(echo "$tz" | tr '/' '_').txt
  : > "$f"
  for n in 1 2 3 4 5 6; do
    $T/runlog.sh -d "mvn test -Dbuild=$n -DargLine=-Duser.timezone=$tz" "$f" -- $S test -Dbuild=$n -DargLine=-Duser.timezone=$tz
  done
done
# Випадковий порядок тестів: JUnit перемішує методи (три різні зерна), результат має збігатися зі звичайним.
f=results/order.txt
: > "$f"
for seed in 1 2 3; do
  for n in 1 2 3 4 5 6; do
    P='-DargLine=-Djunit.jupiter.testmethod.order.default=org.junit.jupiter.api.MethodOrderer$Random -Djunit.jupiter.execution.order.random.seed='$seed
    $T/runlog.sh -d "mvn test -Dbuild=$n $P" "$f" -- $S test -Dbuild=$n "$P"
  done
done
