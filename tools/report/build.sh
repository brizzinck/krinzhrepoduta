#!/bin/sh
# Build report.md into report.pdf and report.docx next to the source file.
# Usage: tools/report/build.sh path/to/report.md
# Requires pandoc and Google Chrome or Chromium (for PDF).
set -eu

HERE=$(cd "$(dirname "$0")" && pwd)
SRC=$(cd "$(dirname "$1")" && pwd)/$(basename "$1")
DIR=$(dirname "$SRC")
NAME=$(basename "$SRC" .md)

[ -f "$HERE/reference.docx" ] || python3 "$HERE/make_reference.py" >/dev/null

cd "$DIR"

pandoc "$SRC" -f markdown -t docx -o "$NAME.docx" \
  --reference-doc "$HERE/reference.docx" --lua-filter "$HERE/title.lua" --resource-path "$DIR"

TMP=$(mktemp -d "${TMPDIR:-/tmp}/report.XXXXXX")
trap 'rm -rf "$TMP"' EXIT
pandoc "$SRC" -f markdown -t html5 -s --embed-resources -o "$TMP/$NAME.html" \
  --css "$HERE/report.css" --lua-filter "$HERE/title.lua" --resource-path "$DIR" \
  --metadata lang=uk -V document-css=false

# Flatpak Chrome only sees XDG directories (Downloads etc.), so print through a temporary directory there.
DL=$(xdg-user-dir DOWNLOAD 2>/dev/null || echo "$HOME/Downloads")
PRINT=$(mktemp -d "$DL/.duikt-print.XXXXXX")
trap 'rm -rf "$TMP" "$PRINT"' EXIT
cp "$TMP/$NAME.html" "$PRINT/report.html"
CHROME=${CHROME:-$(command -v google-chrome-stable || command -v chromium || command -v google-chrome)}
"$CHROME" --headless --disable-gpu --no-pdf-header-footer --run-all-compositor-stages-before-draw \
  --print-to-pdf="$PRINT/report.pdf" "file://$PRINT/report.html" >/dev/null 2>&1
[ -s "$PRINT/report.pdf" ] || { echo "error: Chrome did not produce a PDF" >&2; exit 1; }
mv "$PRINT/report.pdf" "$DIR/$NAME.pdf"

echo "built: $DIR/$NAME.pdf, $DIR/$NAME.docx"
