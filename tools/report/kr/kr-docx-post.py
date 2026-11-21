#!/usr/bin/env python3
"""Finish the DOCX of the coursework note after pandoc (called by build.sh).

  kr-docx-post.py fix   IN.docx OUT.docx NOTE.md [--pages FILE] [--pages-count N]
  kr-docx-post.py pages IN.docx RENDERED.pdf

fix:    builds the table of contents as a real TOC field whose cached result holds every
        heading (hyperlinks to bookmarks, PAGEREF page numbers); fits the tables to the
        text width; writes document properties and settings; drops pandoc leftovers
        (custom properties, empty comments part). No updateFields, no dirty flags.
pages:  prints "index<TAB>page" for every contents heading, read from a PDF that a word
        processor rendered from the DOCX (the pages of the DOCX itself, not of the PDF
        built from HTML). The first line is "toc_end<TAB>N".
"""
import re
import subprocess
import sys
import zipfile
from datetime import datetime, timezone

from lxml import etree

W = "http://schemas.openxmlformats.org/wordprocessingml/2006/main"
XML = "http://www.w3.org/XML/1998/namespace"
NS = {"w": W}
TEXT_WIDTH = 10205  # A4 210 mm - 20 mm - 10 mm, in twips
LISTED = ("Heading1", "Heading2", "Heading3")
TOC_INSTR = ' TOC \\o "1-3" \\h \\z \\u '
MARKER = "KR-TOC-MARKER"


def q(tag):
    return f"{{{W}}}{tag}"


def el(tag, **attrs):
    e = etree.Element(q(tag))
    for k, v in attrs.items():
        e.set(q(k), str(v))
    return e


def pstyle_of(p):
    s = p.find("w:pPr/w:pStyle", NS)
    return s.get(q("val")) if s is not None else None


def text_of(node):
    return "".join(t.text or "" for t in node.iter(q("t")))


def listed_headings(body):
    return [p for p in body.iter(q("p")) if pstyle_of(p) in LISTED]


# ---------------------------------------------------------------- table of contents

def run(text=None, instr=None, fld=None, tab=False):
    r = el("r")
    if fld:
        r.append(el("fldChar", fldCharType=fld))
    if instr:
        t = el("instrText")
        t.set(f"{{{XML}}}space", "preserve")
        t.text = instr
        r.append(t)
    if text is not None:
        t = el("t")
        t.set(f"{{{XML}}}space", "preserve")
        t.text = text
        r.append(t)
    if tab:
        r.append(el("tab"))
    return r


def build_toc(body, pages):
    marker = next((p for p in body.iter(q("p")) if text_of(p) == MARKER), None)
    if marker is None:
        return 0
    heads = listed_headings(body)
    ids = [int(x.get(q("id"))) for x in body.iter(q("bookmarkStart"))]
    next_id = max(ids, default=0) + 1
    rows = []
    for i, p in enumerate(heads, 1):
        name = f"_Toc9{i:06d}"
        ppr = p.find("w:pPr", NS)
        start = el("bookmarkStart", id=next_id, name=name)
        p.insert(list(p).index(ppr) + 1 if ppr is not None else 0, start)
        p.append(el("bookmarkEnd", id=next_id))
        next_id += 1
        level = pstyle_of(p)[-1]
        rows.append((level, text_of(p), name, pages.get(i, "00")))
    parent = marker.getparent()
    at = list(parent).index(marker)
    parent.remove(marker)
    for n, (level, text, name, page) in enumerate(rows):
        p = el("p")
        ppr = el("pPr")
        ppr.append(el("pStyle", val=f"TOC{level}"))
        p.append(ppr)
        if n == 0:
            p.append(run(fld="begin"))
            p.append(run(instr=TOC_INSTR))
            p.append(run(fld="separate"))
        link = el("hyperlink", anchor=name, history="1")
        link.append(run(text))
        link.append(run(tab=True))
        link.append(run(fld="begin"))
        link.append(run(instr=f" PAGEREF {name} \\h "))
        link.append(run(fld="separate"))
        link.append(run(page))
        link.append(run(fld="end"))
        p.append(link)
        if n == len(rows) - 1:
            p.append(run(fld="end"))
        parent.insert(at + n, p)
    return len(rows)


# ---------------------------------------------------------------- tables

def char_w(ch, mono):
    """Rough width of a character in twips at 11 pt (mono: 9 pt Courier)."""
    if mono:
        return 108
    if ch == " ":
        return 55
    if ch.isdigit():
        return 110
    if ch.isupper():
        return 165
    if ch.isalpha():
        return 118
    return 70


def par_metrics(p):
    """(full width, longest word) of a paragraph, in twips."""
    words, cur, widths = [], 0, []
    for r in p.iter(q("r")):
        mono = r.find("w:rPr/w:rStyle", NS) is not None and r.find("w:rPr/w:rStyle", NS).get(q("val")) == "VerbatimChar"
        for t in r.iter(q("t")):
            for ch in t.text or "":
                if ch.isspace():
                    if cur:
                        widths.append(cur)
                    cur = 0
                else:
                    cur += char_w(ch, mono)
    if cur:
        widths.append(cur)
    full = sum(widths) + 55 * max(len(widths) - 1, 0)
    return full, (max(widths) if widths else 0)


def fit_columns(mins, prefs, total):
    n = len(mins)
    mins = [int(m) for m in mins]
    if sum(mins) >= total:
        return [max(int(m * total / sum(mins)), 400) for m in mins]
    want = [max(p, m) for p, m in zip(prefs, mins)]
    if sum(want) <= total:
        return [int(w * total / sum(want)) for w in want]
    spare = total - sum(mins)
    extra = [w - m for w, m in zip(want, mins)]
    return [m + int(spare * e / sum(extra)) for m, e in zip(mins, extra)]


def fix_tables(body):
    count = 0
    for tbl in body.iter(q("tbl")):
        rows = tbl.findall("w:tr", NS)
        if not rows:
            continue
        ncol = len(tbl.findall("w:tblGrid/w:gridCol", NS))
        mins, prefs = [300] * ncol, [0] * ncol
        for ri, tr in enumerate(rows):
            for ci, tc in enumerate(tr.findall("w:tc", NS)):
                if ci >= ncol:
                    break
                full = longest = 0
                for p in tc.findall("w:p", NS):
                    f, lw = par_metrics(p)
                    full += f
                    longest = max(longest, lw)
                bold = 1.12 if ri == 0 else 1.0
                cap = 14 * 108  # a long identifier in a body cell may wrap, a word of the header never
                word = longest * bold if ri == 0 else min(longest, cap)
                mins[ci] = max(mins[ci], word + 260)
                prefs[ci] = max(prefs[ci], min(full * bold, 80 * 108) + 230)
        widths = fit_columns(mins, prefs, TEXT_WIDTH)
        widths[-1] += TEXT_WIDTH - sum(widths)
        tblpr = tbl.find("w:tblPr", NS)
        tblw = tblpr.find("w:tblW", NS)
        tblw.set(q("type"), "dxa")
        tblw.set(q("w"), str(TEXT_WIDTH))
        grid = tbl.find("w:tblGrid", NS)
        for gc, w in zip(grid.findall("w:gridCol", NS), widths):
            gc.set(q("w"), str(w))
        for ri, tr in enumerate(rows):
            trpr = tr.find("w:trPr", NS)
            if trpr is None:
                trpr = el("trPr")
                tr.insert(0, trpr)
            if trpr.find("w:cantSplit", NS) is None:
                trpr.insert(0, el("cantSplit"))
            for ci, tc in enumerate(tr.findall("w:tc", NS)):
                tcpr = tc.find("w:tcPr", NS)
                if tcpr is None:
                    tcpr = el("tcPr")
                    tc.insert(0, tcpr)
                old = tcpr.find("w:tcW", NS)
                if old is not None:
                    tcpr.remove(old)
                tcpr.insert(0, el("tcW", w=widths[min(ci, ncol - 1)], type="dxa"))
                for r in tc.iter(q("r")):
                    rpr = r.find("w:rPr", NS)
                    style = rpr.find("w:rStyle", NS) if rpr is not None else None
                    mono = style is not None and style.get(q("val")) == "VerbatimChar"
                    if ri == 0 or mono:
                        if rpr is None:
                            rpr = el("rPr")
                            r.insert(0, rpr)
                        if ri == 0:
                            rpr.insert(1 if style is not None else 0, el("b"))
                            rpr.insert(2 if style is not None else 1, el("bCs"))
                        if mono:
                            rpr.append(el("sz", val=18))
                            rpr.append(el("szCs", val=18))
        count += 1
    return count


# ---------------------------------------------------------------- parts

def front_matter(path):
    meta = {}
    text = open(path, encoding="utf-8").read()
    m = re.match(r"---\n(.*?)\n---", text, re.S)
    for line in (m.group(1).splitlines() if m else []):
        k = re.match(r'^([\w-]+):\s*"(.*)"\s*$', line)
        if k:
            meta[k.group(1)] = k.group(2)
    return meta


def core_xml(meta):
    now = datetime.now(timezone.utc).strftime("%Y-%m-%dT%H:%M:%SZ")
    esc = lambda s: s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
    title = meta.get("pagetitle") or meta.get("topic", "")
    author = meta.get("student-name", "")
    return ('<?xml version="1.0" encoding="UTF-8" standalone="yes"?>'
            '<cp:coreProperties xmlns:cp="http://schemas.openxmlformats.org/package/2006/metadata/core-properties" '
            'xmlns:dc="http://purl.org/dc/elements/1.1/" xmlns:dcterms="http://purl.org/dc/terms/" '
            'xmlns:dcmitype="http://purl.org/dc/dcmitype/" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">'
            f'<dc:title>{esc(title)}</dc:title><dc:subject>{esc(meta.get("discipline", ""))}</dc:subject>'
            f'<dc:creator>{esc(author)}</dc:creator><cp:lastModifiedBy>{esc(author)}</cp:lastModifiedBy>'
            '<dc:language>uk-UA</dc:language>'
            f'<dcterms:created xsi:type="dcterms:W3CDTF">{now}</dcterms:created>'
            f'<dcterms:modified xsi:type="dcterms:W3CDTF">{now}</dcterms:modified></cp:coreProperties>')


def app_xml(meta, pages):
    esc = lambda s: s.replace("&", "&amp;").replace("<", "&lt;")
    return ('<?xml version="1.0" encoding="UTF-8" standalone="yes"?>'
            '<Properties xmlns="http://schemas.openxmlformats.org/officeDocument/2006/extended-properties">'
            f'<Template>Normal.dotm</Template>{f"<Pages>{pages}</Pages>" if pages else ""}'
            f'<DocSecurity>0</DocSecurity><ScaleCrop>false</ScaleCrop><Company>{esc(meta.get("university", ""))}</Company>'
            '<LinksUpToDate>false</LinksUpToDate><SharedDoc>false</SharedDoc><HyperlinksChanged>false</HyperlinksChanged>'
            '</Properties>')


def fix_settings(text):
    text = text.replace("<w:embedSystemFonts />", "")
    text = re.sub(r"<w:rsids>.*?</w:rsids>", "", text, flags=re.S)
    text = text.replace('<w:themeFontLang w:val="en-US" />', '<w:themeFontLang w:val="uk-UA" />')
    text = text.replace('<w:decimalSymbol w:val="." />', '<w:decimalSymbol w:val="," />')
    text = text.replace('<w:listSeparator w:val="," />', '<w:listSeparator w:val=";" />')
    compat = ('<w:compat><w:compatSetting w:name="compatibilityMode" '
              'w:uri="http://schemas.microsoft.com/office/word" w:val="15"/></w:compat>')
    if "<w:compat>" not in text:
        text = text.replace("<w:themeFontLang", compat + "<w:themeFontLang", 1)
    assert "updateFields" not in text
    return text


def read_pages(path):
    pages = {}
    if path:
        for line in open(path, encoding="utf-8"):
            m = re.match(r"^(\d+)\t(\d+)$", line.strip())
            if m:
                pages[int(m.group(1))] = m.group(2)
    return pages


def cmd_fix(args):
    src, dst, note = args[:3]
    pages_file = args[args.index("--pages") + 1] if "--pages" in args else None
    count = args[args.index("--pages-count") + 1] if "--pages-count" in args else None
    meta = front_matter(note)
    zin = zipfile.ZipFile(src)
    out = {}
    order = []
    for info in zin.infolist():
        order.append(info.filename)
        out[info.filename] = zin.read(info.filename)
    root = etree.fromstring(out["word/document.xml"])
    body = root.find("w:body", NS)
    n_toc = build_toc(body, read_pages(pages_file))
    n_tbl = fix_tables(body)
    out["word/document.xml"] = etree.tostring(root, xml_declaration=True, encoding="UTF-8", standalone=True)
    out["word/settings.xml"] = fix_settings(out["word/settings.xml"].decode("utf-8")).encode("utf-8")
    out["docProps/core.xml"] = core_xml(meta).encode("utf-8")
    out["docProps/app.xml"] = app_xml(meta, count).encode("utf-8")
    # pandoc leftovers: custom properties (all the metadata of the note) and an empty comments part
    for name, rel_type in (("docProps/custom.xml", "custom-properties"), ("word/comments.xml", "comments")):
        if name in out:
            del out[name]
            order.remove(name)
    out["[Content_Types].xml"] = re.sub(rb'<Override PartName="/(docProps/custom|word/comments)\.xml"[^>]*/>', b"",
                                        out["[Content_Types].xml"])
    out["_rels/.rels"] = re.sub(rb'<Relationship [^>]*Target="docProps/custom\.xml"[^>]*/>', b"", out["_rels/.rels"])
    out["word/_rels/document.xml.rels"] = re.sub(rb'<Relationship [^>]*Target="comments\.xml"[^>]*/>', b"",
                                                 out["word/_rels/document.xml.rels"])
    with zipfile.ZipFile(dst, "w", zipfile.ZIP_DEFLATED) as zout:
        for name in order:
            zout.writestr(name, out[name])
    print(f"post: {n_toc} contents entries, {n_tbl} tables", file=sys.stderr)


def cmd_pages(args):
    docx, pdf = args[:2]
    root = etree.fromstring(zipfile.ZipFile(docx).read("word/document.xml"))
    heads = [text_of(p) for p in listed_headings(root.find("w:body", NS))]
    text = subprocess.run(["pdftotext", "-layout", pdf, "-"], capture_output=True, text=True, check=True).stdout
    pages = text.split("\f")
    norm = lambda s: re.sub(r"\s+", " ", s).strip().casefold()
    toc = [i + 1 for i, p in enumerate(pages) if sum(1 for l in p.splitlines() if re.search(r"\.{6,}\s*\d+$", l)) >= 3]
    if not toc:
        sys.exit("error: contents page not found in the rendered PDF")
    toc_end = max(toc)
    print(f"toc_end\t{toc_end}")
    cur = toc_end + 1
    for i, title in enumerate(heads, 1):
        key = norm(title)[:30]
        for n in range(cur, len(pages) + 1):
            if any((norm(l) == key) if len(norm(title)) <= 30 else norm(l).startswith(key)
                   for l in pages[n - 1].splitlines()):
                cur = n
                print(f"{i}\t{n}")
                break
        else:
            sys.exit(f"error: heading not found in the rendered PDF: {title}")
    print(f"total\t{len([p for p in pages if p.strip()])}", file=sys.stderr)


if __name__ == "__main__":
    cmd, rest = sys.argv[1], sys.argv[2:]
    {"fix": cmd_fix, "pages": cmd_pages}[cmd](rest)
