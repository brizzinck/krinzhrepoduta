#!/bin/sh
# Maven у docker БЕЗ мережі (--network none) і в офлайн-режимі (-o): для запуску чужого коду.
# Репозиторій duikt-m2 змонтовано лише для читання, тож залежності мають уже бути в кеші
# (спершу один раз онлайн: mvn.sh test-compile). Запуск із теки проєкту:
#   ../../tools/mvn-sandbox.sh test -Dbuild=3
exec docker run --rm --network none -u "$(id -u):$(id -g)" -e LANG=C.UTF-8 \
  -v duikt-m2:/var/maven/.m2:ro -e MAVEN_CONFIG=/var/maven/.m2 \
  -v "$PWD":/w -w /w maven:3.9-eclipse-temurin-21 mvn -B -o -Duser.home=/var/maven "$@"
