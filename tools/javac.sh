#!/bin/sh
# javac (JDK 21) in docker, with English compiler messages (default javac locale).
# The current directory is mounted at /w. Example: ../../tools/javac.sh -encoding UTF-8 -d /tmp/out Main.java
exec docker run --rm -i -u "$(id -u):$(id -g)" -e LANG=C.UTF-8 -v "$PWD":/w -w /w \
  --entrypoint javac maven:3.9-eclipse-temurin-21 -J-Duser.language=en "$@"
