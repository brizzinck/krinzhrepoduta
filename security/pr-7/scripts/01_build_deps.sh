#!/bin/bash
# Збирає fat jar у копії та пише дерева залежностей.
set -eu
. "$(dirname "$0")/env.sh"
for n in lr-3 lr-6; do
  short=${n/-/}
  mvn_run cw-pr7-mvn-$short "$BUILD/$n" -DskipTests package -q
  echo "збірка $n: $(ls -l "$BUILD/$n/target/app.jar" 2>/dev/null | awk '{print $5}') байт"
  # дерево runtime і повне (з test)
  "$RUNLOG" -t -d "mvn dependency:tree -Dscope=runtime ($n)" "$RES/deps-$short-tree.txt" -- \
    bash -c "$(declare -f mvn_run); MVN_IMG=$MVN_IMG; M2_VOL=$M2_VOL; UIDGID=$UIDGID; mvn_run cw-pr7-mvn-$short '$BUILD/$n' dependency:tree -Dscope=runtime"
  "$RUNLOG" -d "mvn dependency:tree ($n, усі області)" "$RES/deps-$short-tree.txt" -- \
    bash -c "$(declare -f mvn_run); MVN_IMG=$MVN_IMG; M2_VOL=$M2_VOL; UIDGID=$UIDGID; mvn_run cw-pr7-mvn-$short '$BUILD/$n' dependency:tree"
done
