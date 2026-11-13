#!/bin/sh
# Прогони mvn test для кожного кроку роботи; кожен прогін пишеться в results/*.txt.
# Зламані стани створюються тимчасовою правкою src/main, після кожного кроку код повертається.
# Запуск із теки роботи: sh run.sh
cd "$(dirname "$0")" || exit 1
T=../../tools
M=src/main/java/ua/duikt/lavrishko/pr8
BAK=$(mktemp -d)
cp "$M/Sprint.java" "$M/IssueService.java" "$BAK/"
restore() { cp "$BAK/Sprint.java" "$BAK/IssueService.java" "$M/"; }
trap 'restore; rm -rf "$BAK"' EXIT

log() {  # log <файл результату> <аргументи mvn.sh>
  f=$1; shift
  $T/runlog.sh -t -d "mvn $*" "results/$f.txt" -- $T/mvn.sh "$@"
}

# Дослід 1: SprintTest
log s1_green test -Dtest=SprintTest
sed -i 's/CAPACITY = 40/CAPACITY = 30/' "$M/Sprint.java"            # зламано: 40 замінено на 30
log s1_red test -Dtest=SprintTest
restore

# Дослід 2: SprintBoundaryTest
log s2_green test -Dtest=SprintBoundaryTest
sed -i 's/current + points <= CAPACITY/current + points < CAPACITY/' "$M/Sprint.java"   # зламано: <= замінено на <
log s2_red test -Dtest=SprintBoundaryTest
restore

# Дослід 3: IssueServiceTest. Спочатку без argLine (попередження агента), потім з argLine
log s3_noagent -f pom-without-agent.xml test -Dtest=IssueServiceTest
log s3_green test -Dtest=IssueServiceTest
sed -i 's|^        notifier.send(login, text);|        // notifier.send(login, text);|' "$M/IssueService.java"   # зламано: виклик закоментовано
log s3_red test -Dtest=IssueServiceTest
restore

# Усі тести разом на відновленому коді
log final_green test
