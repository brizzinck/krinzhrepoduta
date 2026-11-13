#!/bin/sh
# Збирає report.jar і запускає звіт двічі (ключ є й ключа немає), потім незалежна перевірка.
# Запуск із теки роботи: sh run.sh
cd "$(dirname "$0")" || exit 1
T=../../tools
$T/mvn.sh -q clean package || exit 1
$T/runlog.sh -t -d "java -jar target/report.jar data/v2_issues.txt SHOP-101" results/run.txt -- \
  $T/java.sh -jar target/report.jar data/v2_issues.txt SHOP-101
$T/runlog.sh -d "java -jar target/report.jar data/v2_issues.txt SHOP-000" results/run.txt -- \
  $T/java.sh -jar target/report.jar data/v2_issues.txt SHOP-000
$T/runlog.sh -t -d "java -jar target/report.jar data/v2_issues.txt" results/usage.txt -- \
  $T/java.sh -jar target/report.jar data/v2_issues.txt
# Перевірка підсумків без Java: унікальні рядки через sort -u, суми через awk
U="tail -n +2 data/v2_issues.txt | tr -d '\\r' | sort -u"
$T/runlog.sh -t -d "tail -n +2 data/v2_issues.txt | wc -l" results/check.txt -- sh -c "tail -n +2 data/v2_issues.txt | wc -l"
$T/runlog.sh -d "$U | wc -l" results/check.txt -- sh -c "$U | wc -l"
$T/runlog.sh -d "$U | awk (кількість і сума за статусами)" results/check.txt -- sh -c "$U | awk -F';' '{c[\$2]++; s[\$2]+=\$4} END {for (k in c) print k, c[k], s[k]}' | sort"
$T/runlog.sh -d "$U | awk (ключі з 13 SP)" results/check.txt -- sh -c "$U | awk -F';' '\$4 == 13 {print \$1}' | sort"
