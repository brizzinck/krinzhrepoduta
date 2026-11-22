#!/bin/bash
# Semgrep Community Edition над ЛР 3 і ЛР 6 (код монтується тільки для читання).
# Цілі задано явно (src/main, src/test і файли в корені), бо за замовчуванням
# .semgrepignore пропускає src/test, а нам треба просканувати й тести.
set -eu
. "$(dirname "$0")/env.sh"
mkdir -p "$RES"
scan() { # ім'я-ЛР тека-коду кінцеві-аргументи...
  local lr=$1 dir=$2; shift 2
  local targets="src/main src/test $(cd "$dir" && ls | grep -vE '^(src|target|logs)$' | tr '\n' ' ')"
  docker run --rm --name "cw-pr7-semgrep-$lr" -u "$UIDGID" -e HOME=/tmp \
    -v "$dir":/src:ro -v "$RES":/out -v "$PR7/rules":/rules:ro -w /src "$SEMGREP_IMG" \
    semgrep scan --metrics=off --no-git-ignore --exclude target --exclude logs "$@" $targets
}
for lr in lr3 lr6; do
  dir=$LR3; [ "$lr" = lr6 ] && dir=$LR6
  # json і sarif за один прохід
  scan "$lr" "$dir" $SEMGREP_CONFIGS --json-output /out/semgrep-$lr.json --sarif-output /out/semgrep-$lr.sarif --quiet || true
  # текстовий вивід без правки, через runlog
  "$RUNLOG" -t -d "semgrep scan --metrics=off $SEMGREP_CONFIGS ($lr)" "$RES/semgrep-$lr.txt" -- \
    bash -c "$(declare -f scan); . '$PR7/scripts/env.sh'; scan $lr '$dir' $SEMGREP_CONFIGS"
  echo "semgrep $lr готово"
done
# власне правило проєкту, якщо воно є
if [ -f "$PR7/rules/jdbc-sqli.yaml" ]; then
  scan lr3-custom "$LR3" --config /rules/jdbc-sqli.yaml --json-output /out/semgrep-lr3-custom.json --quiet || true
  "$RUNLOG" -t -d "semgrep scan --config rules/jdbc-sqli.yaml (lr3)" "$RES/semgrep-lr3-custom.txt" -- \
    bash -c "$(declare -f scan); . '$PR7/scripts/env.sh'; scan lr3-custom '$LR3' --config /rules/jdbc-sqli.yaml"
  echo "semgrep lr3-custom готово"
fi
