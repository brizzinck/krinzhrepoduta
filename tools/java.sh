#!/bin/sh
# Run java (JDK 21) in docker, no JDK install on the host.
# The current directory is mounted at /w. Example: ../../tools/java.sh -jar target/importer.jar in.txt out.txt
exec docker run --rm -u "$(id -u):$(id -g)" -e LANG=C.UTF-8 -v "$PWD":/w -w /w \
  --entrypoint java maven:3.9-eclipse-temurin-21 "$@"
