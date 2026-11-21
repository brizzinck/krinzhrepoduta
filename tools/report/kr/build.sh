#!/bin/sh
# Build the coursework note: note.md into note.pdf and note.docx next to the source file.
# Usage: tools/report/kr/build.sh path/to/note.md
# Requires pandoc, Google Chrome or Chromium (PDF), pdftotext and python3 (page numbers of the contents).
# The PDF is built twice: the first pass finds the page of every heading, the second prints them
# into the table of contents. The DOCX gets the same numbers as cached text of a TOC field that
# Word refreshes on open.
set -eu

HERE=$(cd "$(dirname "$0")" && pwd)
SRC=$(cd "$(dirname "$1")" && pwd)/$(basename "$1")
DIR=$(dirname "$SRC")
NAME=$(basename "$SRC" .md)

[ -f "$HERE/kr-reference.docx" ] || python3 "$HERE/make_kr_reference.py" >/dev/null

TMP=$(mktemp -d "${TMPDIR:-/tmp}/kr.XXXXXX")
DL=$(xdg-user-dir DOWNLOAD 2>/dev/null || echo "$HOME/Downloads")
PRINT=$(mktemp -d "$DL/.duikt-print.XXXXXX")
trap 'rm -rf "$TMP" "$PRINT"' EXIT
CHROME=${CHROME:-$(command -v google-chrome-stable || command -v chromium || command -v google-chrome)}
cd "$DIR"

# html_pdf OUT.pdf: the note through pandoc, kr.css and headless Chrome.
html_pdf() {
  pandoc "$SRC" -f markdown -t html5 -s --embed-resources -o "$TMP/note.html" \
    --css "$HERE/kr.css" --lua-filter "$HERE/kr-title.lua" --resource-path "$DIR" \
    --metadata lang=uk -V document-css=false
  # Flatpak Chrome only sees XDG directories, so print inside a temporary directory there.
  cp "$TMP/note.html" "$PRINT/note.html"
  rm -f "$PRINT/note.pdf"
  "$CHROME" --headless --disable-gpu --no-pdf-header-footer --run-all-compositor-stages-before-draw \
    --print-to-pdf="$PRINT/note.pdf" "file://$PRINT/note.html" >/dev/null 2>&1
  [ -s "$PRINT/note.pdf" ] || { echo "error: Chrome did not produce a PDF" >&2; exit 1; }
  cp "$PRINT/note.pdf" "$1"
}

# find_pages PDF [TOC_END]: print "index<TAB>page" for the listed headings of $TMP/headings.tsv.
# Without TOC_END the contents pages are those holding rows that end with the placeholder 00.
find_pages() {
  python3 -I - "$1" "$TMP/headings.tsv" "${2:-}" <<'PY'
import re, subprocess, sys
pdf, tsv, toc_end = sys.argv[1], sys.argv[2], sys.argv[3]
text = subprocess.run(["pdftotext", "-layout", pdf, "-"], capture_output=True, text=True, check=True).stdout
pages = text.split("\f")
norm = lambda s: re.sub(r"\s+", " ", s).strip().casefold()
heads = [l.rstrip("\n").split("\t") for l in open(tsv, encoding="utf-8") if l.strip()]
if toc_end:
    start = int(toc_end) + 1
else:
    toc = [i + 1 for i, p in enumerate(pages) if any(re.search(r"\s00$", l) for l in p.splitlines())]
    if not toc:
        sys.exit("error: contents page not found in the PDF")
    start = max(toc) + 1
    print(f"toc_end\t{max(toc)}")
cur = start
for idx, _lvl, title in heads:
    key = norm(title)[:30]
    for n in range(cur, len(pages) + 1):
        if any(norm(l).startswith(key) for l in pages[n - 1].splitlines()):
            cur = n
            print(f"{idx}\t{n}")
            break
    else:
        sys.exit(f"error: heading not found in the PDF: {title}")
PY
}

HAS_TOC=0
if grep -q '^::: *toc' "$SRC" 2>/dev/null; then HAS_TOC=1; fi

if [ "$HAS_TOC" = 1 ]; then
  export KR_TOC_OUT="$TMP/headings.tsv"
  html_pdf "$TMP/pass1.pdf"
  find_pages "$TMP/pass1.pdf" > "$TMP/pages1.tsv"
  TOC_END=$(sed -n 's/^toc_end\t//p' "$TMP/pages1.tsv")
  grep -v '^toc_end' "$TMP/pages1.tsv" > "$TMP/pages.tsv"
  export KR_TOC_PAGES="$TMP/pages.tsv"
  html_pdf "$DIR/$NAME.pdf"
  find_pages "$DIR/$NAME.pdf" "$TOC_END" > "$TMP/pages2.tsv"
  cmp -s "$TMP/pages.tsv" "$TMP/pages2.tsv" || echo "warning: page numbers of the contents changed between passes" >&2
else
  unset KR_TOC_OUT KR_TOC_PAGES 2>/dev/null || true
  html_pdf "$DIR/$NAME.pdf"
fi

pandoc "$SRC" -f markdown -t docx -o "$NAME.docx" \
  --reference-doc "$HERE/kr-reference.docx" --lua-filter "$HERE/kr-title.lua" --resource-path "$DIR"

echo "built: $DIR/$NAME.pdf, $DIR/$NAME.docx"
