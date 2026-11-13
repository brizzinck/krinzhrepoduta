#!/bin/sh
# Зведення з логів results/: для кожної збірки кількість тестів, що впали, і їхні назви;
# потім порівняння наборів тестів, що впали, у трьох часових поясах комп'ютера.
cd "$(dirname "$0")/results" || exit 1

# Назви тестів, що впали, з одного блоку логу (вхід: stdin)
failed() {
  grep -E '<<< (FAILURE|ERROR)!$' | grep -v ' in ua.duikt' |
    sed -E 's/^\[ERROR\] ua\.duikt\.lavrishko\.lr8\.OverduePolicyTest\.//; s/\(String, LocalDate, LocalDate(, IssueStatus)?\)//; s/ -- Time elapsed.*//' | sort
}
total() { grep -E '^\[(INFO|ERROR)\] Tests run: [0-9]+, Failures: [0-9]+, Errors: [0-9]+, Skipped: [0-9]+$' | tail -1 | sed -E 's/^\[[A-Z]+\] //'; }

echo "Звичайний запуск (пояс контейнера Etc/UTC):"
for n in 1 2 3 4 5 6; do
  echo "Збірка $n: $(total < build-$n.txt)"
  failed < build-$n.txt | sed 's/^/    /'
done

# Блок n-ї збірки з файлу, де шість запусків підряд
block() { awk -v n="$2" '/^> mvn test -Dbuild=/ { cur = $4; sub(/-Dbuild=/, "", cur) } cur == n' "$1"; }
echo
for tz in America_Los_Angeles Pacific_Kiritimati; do
  same=так
  for n in 1 2 3 4 5 6; do
    [ "$(block tz-$tz.txt $n | failed)" = "$(failed < build-$n.txt)" ] || same=ні
  done
  echo "Пояс $tz: набори тестів, що впали, у всіх шести збірках збігаються зі звичайним запуском: $same"
done

# Випадковий порядок методів (зерна 1-3): набори тестів, що впали, збігаються зі звичайним запуском
oblock() { awk -v n="$2" -v s="$3" '/^> mvn test/ { show = ($0 ~ ("-Dbuild=" n " ") && $0 ~ ("seed=" s "$")) } show' "$1"; }
for seed in 1 2 3; do
  same=так
  for n in 1 2 3 4 5 6; do
    [ "$(oblock order.txt $n $seed | failed)" = "$(failed < build-$n.txt)" ] || same=ні
  done
  echo "Випадковий порядок методів, зерно $seed: набори тестів, що впали, збігаються зі звичайним запуском: $same"
done
