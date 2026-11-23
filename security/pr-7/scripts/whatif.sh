#!/bin/sh
# Перевірка «що, якщо»: копія застосунку з іншою версією parent і власними версіями бібліотек.
# Збірка з тестами в docker, потім Trivy на новому app.jar і порівняння зі збереженим результатом зі звіту.
# Використання (з теки pr-7):  scripts/whatif.sh lr-3 3.5.16 tomcat.version=10.1.59,jackson-bom.version=2.21.7 3.5.16-props results/trivy-lr3-whatif-3.5.16-props.json
#   параметри: ЛР, версія Boot, властивості через кому або "-", мітка копії, збережений json Trivy
# Змінні середовища (їх задає Makefile): C, M2, MVN_IMG, TRIVY_IMG.
set -eu
N=$1; VER=$2; PROPS=$3; TAG=$4; TRACKED=$5
UID_GID="$(id -u):$(id -g)"
W=target/whatif/$N-$TAG
rm -rf "$W"; mkdir -p "$W"
# копія без target і logs: вихідна тека ЛР не змінюється
(cd "../$N/app" && tar --exclude=./target --exclude=./logs -cf - .) | tar -xf - -C "$W"
# версія parent змінюється лише в першому тегу version
sed -i "0,/<version>[^<]*<\/version>/s//<version>$VER<\/version>/" "$W/pom.xml"
if [ "$PROPS" != - ]; then
  for kv in $(echo "$PROPS" | tr , ' '); do
    sed -i "s#<properties>#<properties>\n    <${kv%%=*}>${kv#*=}</${kv%%=*}>#" "$W/pom.xml"
  done
fi
echo "Копія $N: parent $VER, властивості: $PROPS"
if ! docker run --rm --name "$C-mvn-$N-$TAG" -u "$UID_GID" -e LANG=C.UTF-8 \
    -v "$M2":/var/maven/.m2 -e MAVEN_CONFIG=/var/maven/.m2 -v "$PWD/$W":/w -w /w \
    "$MVN_IMG" mvn -B -Duser.home=/var/maven package > "$W/mvn.log" 2>&1; then
  tail -n 15 "$W/mvn.log"
  echo "Збірка копії не вдалася, повний журнал: $W/mvn.log"
  exit 1
fi
grep -E 'Tests run:.*Skipped: [0-9]+$' "$W/mvn.log" | tail -n 1 | sed 's/^\[INFO\] /Тести: /'
grep -E 'BUILD' "$W/mvn.log" | sed 's/^\[INFO\] /Збірка: /'
docker run --rm --name "$C-trivy-$N-$TAG" -u "$UID_GID" -e HOME=/tmp/h \
  -v "$PWD/$W/target":/scan:ro -v "$PWD/target":/out -v "$PWD/target/cache/trivy":/cache -w /scan "$TRIVY_IMG" \
  --quiet rootfs --offline-scan --scanners vuln --format json --output "/out/trivy-$N-$TAG.json" --cache-dir /cache /scan/app.jar
docker run --rm -u "$UID_GID" -v "$PWD":/w -w /w python:3.12-slim \
  python -I scripts/show.py counts "target/trivy-$N-$TAG.json" "$TRACKED"
