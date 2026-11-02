#!/bin/sh
# Build reports (PDF and DOCX) and submission archives.
#   ./build.sh              all reports
#   ./build.sh java/lr-5    one report
#   ./build.sh archives     arch project archives (CarRental-ПЗN.zip)
# Requires pandoc and Google Chrome or Chromium (for PDF).
set -eu
cd "$(dirname "$0")"

build_one() {
  tools/report/build.sh "$1/report.md"
}

archives() {
  for d in arch/pz-*; do
    [ -d "$d/CarRental" ] || continue
    git ls-files --error-unmatch "$d/report.md" >/dev/null 2>&1 || continue
    n=${d##*-}
    (cd "$d" && rm -f "CarRental-ПЗ$n.zip" && zip -qr "CarRental-ПЗ$n.zip" CarRental \
      -x '*/bin/*' '*/obj/*' '*/.data/*' '*.db' '*.db-*')
    echo "built: $d/CarRental-ПЗ$n.zip"
  done
}

case "${1:-all}" in
  all)
    for f in */*/report.md; do build_one "$(dirname "$f")"; done ;;
  archives)
    archives ;;
  *)
    build_one "${1%/}" ;;
esac
