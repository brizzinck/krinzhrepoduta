#!/usr/bin/env python3
"""Generate the pandoc reference.docx for report formatting.

Times New Roman 14, line spacing 1.5, first-line indent 1.25 cm, justified text.
Margins: left 30 mm, right 15 mm, top and bottom 20 mm.
Page number bottom right; none on the title page.

Based on pandoc's default reference.docx; styles.xml and page setup are replaced.
Run: python3 make_reference.py  (writes reference.docx next to the script)
"""
import io
import re
import subprocess
import tempfile
import zipfile
from pathlib import Path

HERE = Path(__file__).resolve().parent
OUT = HERE / "reference.docx"

FONT = '<w:rFonts w:ascii="Times New Roman" w:hAnsi="Times New Roman" w:eastAsia="Times New Roman" w:cs="Times New Roman"/>'
MONO = '<w:rFonts w:ascii="Courier New" w:hAnsi="Courier New" w:eastAsia="Courier New" w:cs="Courier New"/>'
LINE_15 = 'w:line="360" w:lineRule="auto"'
LINE_1 = 'w:line="240" w:lineRule="auto"'
INDENT = '<w:ind w:firstLine="709"/>'


def pstyle(sid, name, ppr="", rpr="", based="Normal", next_=None, char=False, custom=True):
    kind = "character" if char else "paragraph"
    cust = ' w:customStyle="1"' if custom else ""
    parts = [f'<w:style w:type="{kind}"{cust} w:styleId="{sid}"><w:name w:val="{name}"/>']
    if based:
        parts.append(f'<w:basedOn w:val="{based}"/>')
    if next_:
        parts.append(f'<w:next w:val="{next_}"/>')
    parts.append("<w:qFormat/>")
    if ppr:
        parts.append(f"<w:pPr>{ppr}</w:pPr>")
    if rpr:
        parts.append(f"<w:rPr>{rpr}</w:rPr>")
    parts.append("</w:style>")
    return "".join(parts)


def heading(level, centered):
    jc = '<w:jc w:val="center"/>' if centered else '<w:jc w:val="left"/>'
    ind = '<w:ind w:firstLine="0"/>' if centered else INDENT
    ppr = (f'<w:keepNext/><w:keepLines/><w:spacing w:before="240" w:after="120" {LINE_15}/>'
           f'{ind}{jc}<w:outlineLvl w:val="{level - 1}"/>')
    rpr = '<w:b/><w:bCs/><w:sz w:val="28"/><w:szCs w:val="28"/>'
    return pstyle(f"Heading{level}", f"heading {level}", ppr, rpr, next_="BodyText", custom=False)


def styles_xml():
    body_ppr = f'<w:spacing w:before="0" w:after="0" {LINE_15}/>{INDENT}<w:jc w:val="both"/>'
    no_ind = '<w:ind w:firstLine="0"/>'
    center = '<w:jc w:val="center"/>'
    small = '<w:sz w:val="24"/><w:szCs w:val="24"/>'
    styles = [
        # Normal: everything inherits TNR 14, 1.5 spacing, 1.25 cm indent.
        f'<w:style w:type="paragraph" w:default="1" w:styleId="Normal"><w:name w:val="Normal"/><w:qFormat/>'
        f'<w:pPr>{body_ppr}</w:pPr><w:rPr>{FONT}<w:sz w:val="28"/><w:szCs w:val="28"/><w:lang w:val="uk-UA"/></w:rPr></w:style>',
        pstyle("BodyText", "Body Text", custom=False),
        pstyle("FirstParagraph", "First Paragraph", next_="BodyText"),
        # Compact: tight list items.
        pstyle("Compact", "Compact", f'<w:spacing w:before="0" w:after="0" {LINE_15}/>{no_ind}<w:jc w:val="left"/>'),
        # Table cells (title.lua wraps cell content in this style): 12 pt, single spacing.
        pstyle("TableText", "Table Text", f'<w:spacing w:before="0" w:after="0" {LINE_1}/>{no_ind}<w:jc w:val="left"/>', small),
        pstyle("Title", "Title", f"{no_ind}{center}", '<w:b/><w:sz w:val="32"/>', custom=False),
        pstyle("Subtitle", "Subtitle", f"{no_ind}{center}", custom=False),
        pstyle("Author", "Author", f"{no_ind}{center}"),
        pstyle("Date", "Date", f"{no_ind}{center}"),
        pstyle("AbstractTitle", "Abstract Title", f"{no_ind}{center}", "<w:b/>"),
        pstyle("Abstract", "Abstract"),
        pstyle("Bibliography", "Bibliography"),
        heading(1, centered=True),
        heading(2, centered=False),
        heading(3, centered=False),
        pstyle("BlockText", "Block Text", '<w:ind w:left="709" w:firstLine="0"/>', "<w:i/>"),
        pstyle("FootnoteText", "footnote text", f'<w:spacing w:after="0" {LINE_1}/>{no_ind}', small, custom=False),
        pstyle("FootnoteBlockText", "Footnote Block Text", f'<w:spacing w:after="0" {LINE_1}/>{no_ind}', small),
        # Listings: Courier New 10, single spacing, no indent.
        pstyle("SourceCode", "Source Code",
               '<w:shd w:val="clear" w:color="auto" w:fill="F4F4F4"/><w:wordWrap w:val="off"/>'
               f'<w:spacing w:before="60" w:after="60" {LINE_1}/>{no_ind}<w:jc w:val="left"/>',
               f'{MONO}<w:sz w:val="20"/><w:szCs w:val="20"/>', next_="BodyText"),
        # Listings with the .small class (built by title.lua): Courier New 8.
        pstyle("SmallCode", "Small Code",
               '<w:shd w:val="clear" w:color="auto" w:fill="F4F4F4"/>'
               f'<w:spacing w:before="60" w:after="60" {LINE_1}/>{no_ind}<w:jc w:val="left"/>',
               f'{MONO}<w:sz w:val="16"/><w:szCs w:val="16"/>', next_="BodyText"),
        pstyle("VerbatimChar", "Verbatim Char", rpr=f'{MONO}<w:sz w:val="20"/><w:szCs w:val="20"/>',
               based=None, char=True),
        pstyle("Caption", "caption", f'<w:spacing w:before="60" w:after="120" {LINE_1}/>{no_ind}{center}',
               small, custom=False),
        pstyle("TableCaption", "Table Caption", f'<w:keepNext/><w:spacing w:before="120" w:after="60" {LINE_1}/>{no_ind}<w:jc w:val="left"/>',
               based="Caption"),
        pstyle("ImageCaption", "Image Caption", based="Caption"),
        pstyle("Figure", "Figure", f'<w:keepNext/>{no_ind}{center}'),
        pstyle("CaptionedFigure", "Captioned Figure", f'<w:keepNext/>{no_ind}{center}', based="Figure"),
        pstyle("DefinitionTerm", "Definition Term", f"<w:keepNext/>{no_ind}", "<w:b/>"),
        pstyle("Definition", "Definition"),
        pstyle("Hyperlink", "Hyperlink", rpr='<w:color w:val="1F4E9D"/><w:u w:val="single"/>', based=None, char=True),
        pstyle("SectionNumber", "Section Number", based=None, char=True),
        pstyle("FootnoteReference", "Footnote Reference", rpr='<w:vertAlign w:val="superscript"/>', based=None, char=True),
        pstyle("TOCHeading", "TOC Heading", based="Heading1", next_="BodyText"),
        # Title page (custom-style paragraphs from title.lua).
        pstyle("TitleTop", "Title Top", f'<w:spacing w:before="0" w:after="0" {LINE_15}/>{no_ind}{center}'),
        pstyle("TitleOrg", "Title Org", f'<w:spacing w:before="0" w:after="0" {LINE_15}/>{no_ind}{center}', "<w:b/><w:bCs/>"),
        pstyle("TitleReport", "Title Report", f'<w:spacing w:before="1800" w:after="0" {LINE_15}/>{no_ind}{center}',
               '<w:b/><w:bCs/><w:caps/><w:spacing w:val="40"/><w:sz w:val="36"/><w:szCs w:val="36"/>'),
        pstyle("TitleLine", "Title Line", f'<w:spacing w:before="0" w:after="0" {LINE_15}/>{no_ind}{center}'),
        pstyle("TitleTopic", "Title Topic", f'<w:spacing w:before="120" w:after="0" {LINE_15}/>{no_ind}{center}', "<w:b/><w:bCs/>"),
        pstyle("TitleSignFirst", "Title Sign First", f'<w:spacing w:before="1800" w:after="0" {LINE_15}/><w:ind w:left="5103" w:firstLine="0"/><w:jc w:val="left"/>'),
        pstyle("TitleSign", "Title Sign", f'<w:spacing w:before="0" w:after="0" {LINE_15}/><w:ind w:left="5103" w:firstLine="0"/><w:jc w:val="left"/>'),
        pstyle("TitleCity", "Title City", f'<w:spacing w:before="1400" w:after="0" {LINE_15}/>{no_ind}{center}'),
        # Tables: solid borders, 12 pt cell text.
        '<w:style w:type="table" w:default="1" w:styleId="Table"><w:name w:val="Table"/><w:qFormat/>'
        '<w:pPr><w:spacing w:before="0" w:after="0" w:line="240" w:lineRule="auto"/><w:ind w:firstLine="0"/><w:jc w:val="left"/></w:pPr>'
        '<w:rPr><w:sz w:val="24"/><w:szCs w:val="24"/></w:rPr>'
        '<w:tblPr><w:tblInd w:w="0" w:type="dxa"/><w:tblBorders>'
        '<w:top w:val="single" w:sz="4" w:space="0" w:color="000000"/><w:left w:val="single" w:sz="4" w:space="0" w:color="000000"/>'
        '<w:bottom w:val="single" w:sz="4" w:space="0" w:color="000000"/><w:right w:val="single" w:sz="4" w:space="0" w:color="000000"/>'
        '<w:insideH w:val="single" w:sz="4" w:space="0" w:color="000000"/><w:insideV w:val="single" w:sz="4" w:space="0" w:color="000000"/>'
        '</w:tblBorders><w:tblCellMar><w:top w:w="40" w:type="dxa"/><w:left w:w="100" w:type="dxa"/>'
        '<w:bottom w:w="40" w:type="dxa"/><w:right w:w="100" w:type="dxa"/></w:tblCellMar></w:tblPr>'
        '<w:tblStylePr w:type="firstRow"><w:rPr><w:b/><w:bCs/></w:rPr></w:tblStylePr></w:style>',
        '<w:style w:type="character" w:default="1" w:styleId="DefaultParagraphFont"><w:name w:val="Default Paragraph Font"/><w:uiPriority w:val="1"/><w:semiHidden/></w:style>',
    ]
    return ('<?xml version="1.0" encoding="UTF-8" standalone="yes"?>'
            '<w:styles xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships" '
            'xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main">'
            f'<w:docDefaults><w:rPrDefault><w:rPr>{FONT}<w:sz w:val="28"/><w:szCs w:val="28"/>'
            '<w:lang w:val="uk-UA" w:eastAsia="en-US" w:bidi="ar-SA"/></w:rPr></w:rPrDefault>'
            '<w:pPrDefault><w:pPr><w:spacing w:after="0"/></w:pPr></w:pPrDefault></w:docDefaults>'
            + "".join(styles) + "</w:styles>")


FOOTER = ('<?xml version="1.0" encoding="UTF-8" standalone="yes"?>'
          '<w:ftr xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships" '
          'xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main">'
          '<w:p><w:pPr><w:ind w:firstLine="0"/><w:jc w:val="right"/></w:pPr>'
          '<w:r><w:fldChar w:fldCharType="begin"/></w:r><w:r><w:instrText xml:space="preserve"> PAGE </w:instrText></w:r>'
          '<w:r><w:fldChar w:fldCharType="separate"/></w:r><w:r><w:t>2</w:t></w:r><w:r><w:fldChar w:fldCharType="end"/></w:r>'
          '</w:p></w:ftr>')

# A4; margins in twips (1 mm = 56.7 twips): top/bottom 20 mm, left 30 mm, right 15 mm.
SECT = ('<w:sectPr><w:footerReference w:type="default" r:id="rIdFooter1"/>'
        '<w:pgSz w:w="11906" w:h="16838"/>'
        '<w:pgMar w:top="1134" w:right="850" w:bottom="1134" w:left="1701" w:header="567" w:footer="567" w:gutter="0"/>'
        '<w:titlePg/></w:sectPr>')


def main():
    with tempfile.TemporaryDirectory() as tmp:
        default = Path(tmp) / "default.docx"
        subprocess.run(["pandoc", "-o", str(default), "--print-default-data-file", "reference.docx"], check=True)
        base = default.read_bytes()
    src = zipfile.ZipFile(io.BytesIO(base))
    buf = io.BytesIO()
    with zipfile.ZipFile(buf, "w", zipfile.ZIP_DEFLATED) as dst:
        for item in src.infolist():
            data = src.read(item.filename)
            if item.filename == "word/styles.xml":
                data = styles_xml().encode()
            elif item.filename == "word/document.xml":
                text = data.decode()
                text = re.sub(r"<w:sectPr\b.*?</w:sectPr>|<w:sectPr\s*/>", "", text, flags=re.S)
                text = text.replace("</w:body>", SECT + "</w:body>")
                data = text.encode()
            elif item.filename == "word/_rels/document.xml.rels":
                data = data.decode().replace(
                    "</Relationships>",
                    '<Relationship Id="rIdFooter1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/footer" Target="footer1.xml"/></Relationships>'
                ).encode()
            elif item.filename == "[Content_Types].xml":
                data = data.decode().replace(
                    "</Types>",
                    '<Override PartName="/word/footer1.xml" ContentType="application/vnd.openxmlformats-officedocument.wordprocessingml.footer+xml"/></Types>'
                ).encode()
            dst.writestr(item, data)
        dst.writestr("word/footer1.xml", FOOTER)
    OUT.write_bytes(buf.getvalue())
    print(f"built: {OUT}")


if __name__ == "__main__":
    main()
