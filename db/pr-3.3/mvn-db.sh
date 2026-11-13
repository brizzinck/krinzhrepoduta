#!/bin/sh
# Maven + JDK 21 у docker, як tools/mvn.sh, але в мережі контейнера БД (потрібно для hbm2java).
# Приклад: ./mvn-db.sh -q -Preveng generate-sources
docker volume inspect duikt-m2 >/dev/null 2>&1 || {
  docker volume create duikt-m2 >/dev/null
  docker run --rm -v duikt-m2:/m alpine:3 chown "$(id -u):$(id -g)" /m
}
cd "$(dirname "$0")"
exec docker run --rm -u "$(id -u):$(id -g)" -e LANG=C.UTF-8 --network container:duikt-mysql \
  -v duikt-m2:/var/maven/.m2 -e MAVEN_CONFIG=/var/maven/.m2 \
  -v "$PWD":/w -w /w maven:3.9-eclipse-temurin-21 mvn -B -Duser.home=/var/maven "$@"
