#!/bin/bash
# Записує дату, версії інструментів і дати баз у results/versions.txt.
set -eu
. "$(dirname "$0")/env.sh"
F=$RES/versions.txt
{
  echo "дата запуску: $(date -u '+%Y-%m-%d %H:%M UTC')"
  for img in "$SEMGREP_IMG" "$TRIVY_IMG" "$MVN_IMG"; do
    echo "образ $img: $(docker image inspect --format '{{index .RepoDigests 0}}' "$img" 2>/dev/null || echo 'без digest (локальний образ)')"
  done
  echo
  echo "> semgrep --version"
  docker run --rm --name cw-pr7-semgrep-ver -u "$UIDGID" -e HOME=/tmp "$SEMGREP_IMG" semgrep --version 2>&1
  echo
  echo "> trivy version"
  docker run --rm --name cw-pr7-trivy-ver -u "$UIDGID" -e HOME=/tmp -v "$CACHE":/cache "$TRIVY_IMG" version --cache-dir /cache 2>&1
  echo
  echo "> mvn -v"
  docker run --rm --name cw-pr7-mvn-ver -u "$UIDGID" -e HOME=/tmp "$MVN_IMG" mvn -v 2>&1
} > "$F"
echo "versions.txt записано"
