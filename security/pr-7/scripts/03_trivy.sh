#!/bin/bash
# Trivy: SCA fat jar і pom.xml обох застосунків; за WHATIF=<версія Boot> ще й перевірка з оновленим parent.
# TRIVY_SKIP_DB=1 лишає базу з кешу без оновлення (для повторних запусків того ж дня).
set -eu
. "$(dirname "$0")/env.sh"
mkdir -p "$CACHE" "$RES"
SKIP=""; [ "${TRIVY_SKIP_DB:-0}" = 1 ] && SKIP="--skip-db-update --skip-java-db-update"
trivy() { # ім'я теки-для-/scan:режим аргументи...
  local name=$1 dir=$2; shift 2
  # локальний репозиторій Maven (том збірки) змонтовано як ~/.m2, щоб не ходити в Maven Central
  docker run --rm --name "$name" -u "$UIDGID" -e HOME=/tmp/h \
    -v "$M2_VOL":/tmp/h/.m2:ro -v "$dir":/scan:ro -v "$RES":/out -v "$CACHE":/cache -w /scan "$TRIVY_IMG" \
    "$@" --cache-dir /cache $SKIP
}
for n in lr-3 lr-6; do
  short=${n/-/}
  # fat jar: що справді їде у збірці, разом із вкладеними бібліотеками
  trivy cw-pr7-trivy-$short-jar "$BUILD/$n/target" rootfs --offline-scan --scanners vuln --format json \
    --output /out/trivy-$short-jar.json /scan/app.jar
  "$RUNLOG" -t -d "trivy rootfs --offline-scan --scanners vuln --format table app.jar ($n)" "$RES/trivy-$short-jar.txt" -- \
    bash -c "$(declare -f trivy); . '$PR7/scripts/env.sh'; SKIP='$SKIP'; trivy cw-pr7-trivy-$short-jart '$BUILD/$n/target' rootfs --offline-scan --scanners vuln --format table /scan/app.jar"
  # pom.xml: що оголошено в маніфесті
  mkdir -p "$WORK/pomonly/$n"
  cp "$BUILD/$n/pom.xml" "$WORK/pomonly/$n/pom.xml"
  trivy cw-pr7-trivy-$short-pom "$WORK/pomonly/$n" fs --offline-scan --scanners vuln --format json \
    --output /out/trivy-$short-pom.json /scan
  "$RUNLOG" -t -d "trivy fs --offline-scan --scanners vuln --format table pom.xml ($n)" "$RES/trivy-$short-pom.txt" -- \
    bash -c "$(declare -f trivy); . '$PR7/scripts/env.sh'; SKIP='$SKIP'; trivy cw-pr7-trivy-$short-pomt '$WORK/pomonly/$n' fs --offline-scan --scanners vuln --format table /scan"
  echo "trivy $n готово"
done

# Перевірка "що, якщо": копія застосунку з іншою версією parent. Реальний код ЛР не змінюється.
# WHATIF="lr-3:3.5.16 lr-3:3.5.16:tomcat.version=10.1.59,jackson-bom.version=2.21.7,log4j2.version=2.25.5" ./scripts/03_trivy.sh
for item in ${WHATIF:-}; do
  IFS=: read -r n ver props <<< "$item"; short=${n/-/}
  tag=$ver; [ -n "$props" ] && tag=$ver-props
  w="$WORK/whatif/$n-$tag"
  rm -rf "$w"; mkdir -p "$w"
  (cd "$BUILD/$n" && tar --exclude=./target -cf - .) | tar -xf - -C "$w"
  # змінюємо версію лише в блоці parent
  sed -i "0,/<version>[^<]*<\/version>/s//<version>$ver<\/version>/" "$w/pom.xml"
  # власні версії Tomcat і Jackson через властивості, які підтримує Spring Boot
  if [ -n "$props" ]; then
    for kv in ${props//,/ }; do
      sed -i "s#<properties>#<properties>\n    <${kv%%=*}>${kv##*=}</${kv%%=*}>#" "$w/pom.xml"
    done
  fi
  "$RUNLOG" -t -d "mvn package ($n, parent $ver $props)" "$RES/build-$short-whatif-$tag.txt" -- \
    bash -c "$(declare -f mvn_run); MVN_IMG=$MVN_IMG; M2_VOL=$M2_VOL; UIDGID=$UIDGID; mvn_run cw-pr7-mvn-$short-w '$w' -DskipTests package"
  if [ -f "$w/target/app.jar" ]; then
    # автотести копії з новими версіями
    "$RUNLOG" -t -d "mvn test ($n, parent $ver $props)" "$RES/tests-$short-whatif-$tag.txt" -- \
      bash -c "$(declare -f mvn_run); MVN_IMG=$MVN_IMG; M2_VOL=$M2_VOL; UIDGID=$UIDGID; mvn_run cw-pr7-mvn-$short-wtest '$w' test"
    trivy cw-pr7-trivy-$short-w "$w/target" rootfs --offline-scan --scanners vuln --format json \
      --output /out/trivy-$short-whatif-$tag.json /scan/app.jar
    "$RUNLOG" -t -d "trivy rootfs --offline-scan --scanners vuln --format table app.jar ($n, parent $ver $props)" \
      "$RES/trivy-$short-whatif-$tag.txt" -- \
      bash -c "$(declare -f trivy); . '$PR7/scripts/env.sh'; SKIP='$SKIP'; trivy cw-pr7-trivy-$short-wt '$w/target' rootfs --offline-scan --scanners vuln --format table /scan/app.jar"
    echo "what-if $n $tag готово"
  else
    echo "what-if $n $tag: збірка не вдалася, див. results/build-$short-whatif-$tag.txt"
  fi
done
