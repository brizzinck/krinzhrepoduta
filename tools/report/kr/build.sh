#!/bin/sh
# Build the coursework note: note.md into note.pdf and note.docx next to the source file.
# Usage: tools/report/kr/build.sh path/to/note.md
# Requires pandoc, Google Chrome or Chromium (PDF), pdftotext and python3 (page numbers of the contents).
# The PDF is built twice: the first pass finds the page of every heading, the second prints them
# into the table of contents.
# The DOCX gets a real TOC field whose cached result holds the pages of the DOCX itself: pandoc builds
# the document, kr-docx-post.py finishes it, LibreOffice in Docker (image KR_LO_IMAGE, built from
# Dockerfile.lo when missing) renders it to PDF, the headings are looked up there and written into the
# field; repeated until the numbers are stable. Without Docker the numbers of the HTML PDF are used.
set -eu

HERE=$(cd "$(dirname "$0")" && pwd)
SRC=$(cd "$(dirname "$1")" && pwd)/$(basename "$1")
DIR=$(dirname "$SRC")
NAME=$(basename "$SRC" .md)

python3 "$HERE/make_kr_reference.py" >/dev/null

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

# lo_pdf IN.docx OUT.pdf: render with LibreOffice in a throwaway container (mounts only a scratch copy).
lo_image() {
  command -v docker >/dev/null 2>&1 || return 1
  for img in "${KR_LO_IMAGE:-}" kr-lo:1 tz-libreoffice:latest; do
    [ -n "$img" ] && docker image inspect "$img" >/dev/null 2>&1 && { echo "$img"; return 0; }
  done
  docker build -q -t kr-lo:1 -f "$HERE/Dockerfile.lo" "$HERE" >/dev/null 2>&1 && { echo kr-lo:1; return 0; }
  return 1
}
lo_pdf() {
  rm -rf "$TMP/lo" && mkdir -p "$TMP/lo" && cp "$1" "$TMP/lo/in.docx"
  docker run --rm --name "kr-lo-$$" --user "$(id -u):$(id -g)" -e HOME=/tmp/h -v "$TMP/lo:/w" "$LO_IMG" \
    sh -c 'mkdir -p /tmp/h && cd /w && soffice -env:UserInstallation=file:///tmp/h/lo --headless --convert-to pdf in.docx >/dev/null 2>&1'
  [ -s "$TMP/lo/in.pdf" ] || { echo "error: LibreOffice did not produce a PDF" >&2; return 1; }
  cp "$TMP/lo/in.pdf" "$2"
}

unset KR_TOC_OUT KR_TOC_PAGES 2>/dev/null || true
pandoc "$SRC" -f markdown -t docx -o "$TMP/raw.docx" \
  --reference-doc "$HERE/kr-reference.docx" --lua-filter "$HERE/kr-title.lua" --resource-path "$DIR"

POST="python3 $HERE/kr-docx-post.py"
: > "$TMP/dpages.tsv"
if [ "$HAS_TOC" = 1 ] && LO_IMG=$(lo_image); then
  $POST fix "$TMP/raw.docx" "$TMP/d.docx" "$SRC"
  i=0
  while :; do
    lo_pdf "$TMP/d.docx" "$TMP/d.pdf"
    $POST pages "$TMP/d.docx" "$TMP/d.pdf" > "$TMP/dpages.all" 2> "$TMP/count.txt"
    grep -v '^toc_end' "$TMP/dpages.all" > "$TMP/dpages.new"
    TOTAL=$(sed -n 's/^total\t//p' "$TMP/count.txt")
    cmp -s "$TMP/dpages.new" "$TMP/dpages.tsv" && break
    i=$((i + 1))
    [ "$i" -le 4 ] || { echo "warning: DOCX page numbers did not settle" >&2; break; }
    cp "$TMP/dpages.new" "$TMP/dpages.tsv"
    $POST fix "$TMP/raw.docx" "$TMP/d.docx" "$SRC" --pages "$TMP/dpages.tsv" --pages-count "$TOTAL"
  done
  echo "docx: $TOTAL pages in LibreOffice (PDF: $(pdfinfo "$DIR/$NAME.pdf" | sed -n 's/^Pages: *//p'))"
else
  [ "$HAS_TOC" = 1 ] && echo "warning: no Docker or LibreOffice image: contents of the DOCX use the page numbers of the PDF" >&2
  PAGES_ARG=""
  [ -s "$TMP/pages.tsv" ] && PAGES_ARG="--pages $TMP/pages.tsv"
  $POST fix "$TMP/raw.docx" "$TMP/d.docx" "$SRC" $PAGES_ARG
fi
cp "$TMP/d.docx" "$DIR/$NAME.docx"

echo "built: $DIR/$NAME.pdf, $DIR/$NAME.docx"
