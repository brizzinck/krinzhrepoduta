#!/bin/sh
# Gradle wrapper + Android SDK 34 (JDK 21) in docker. The Gradle cache lives in the docker volume duikt-gradle.
# Run from the Gradle project directory. Example: ../../tools/android.sh assembleDebug
# Other SDK tools (aapt2, adb) via ANDROID_CMD: ANDROID_CMD=aapt2 android.sh dump badging app.apk
docker volume inspect duikt-gradle >/dev/null 2>&1 || {
  docker volume create duikt-gradle >/dev/null
  docker run --rm -v duikt-gradle:/g alpine:3 chown "$(id -u):$(id -g)" /g
}
CMD=${ANDROID_CMD:-./gradlew}
exec docker run --rm -u "$(id -u):$(id -g)" -e LANG=C.UTF-8 \
  -e HOME=/gradle-home -e GRADLE_USER_HOME=/gradle-home -e ANDROID_USER_HOME=/gradle-home/.android \
  -v duikt-gradle:/gradle-home \
  -v "$PWD":/w -w /w ghcr.io/cirruslabs/android-sdk:34 "$CMD" "$@"
