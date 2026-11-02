#!/bin/sh
# Run a command and append a block to a log file: "> command", the verbatim output (stdout and stderr),
# "(код завершення: N)". The script itself always exits 0 so a series of runs is not interrupted.
# Usage: runlog.sh [-t] [-d "command to show"] file.txt -- command args...
#   -t   truncate the file first
#   -d   text of the "> ..." line when it differs from the real command (e.g. without docker)
set -u
TRUNC=0
SHOW=""
while [ $# -gt 0 ]; do
  case "$1" in
    -t) TRUNC=1; shift ;;
    -d) SHOW=$2; shift 2 ;;
    *) break ;;
  esac
done
FILE=$1; shift
[ "${1:-}" = "--" ] && shift
[ -n "$SHOW" ] || SHOW="$*"
[ "$TRUNC" = 1 ] && : > "$FILE"
[ -s "$FILE" ] && printf '\n' >> "$FILE"
printf '> %s\n' "$SHOW" >> "$FILE"
"$@" >> "$FILE" 2>&1
CODE=$?
printf '(код завершення: %s)\n' "$CODE" >> "$FILE"
exit 0
