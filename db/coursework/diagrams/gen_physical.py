#!/usr/bin/env python3
"""Physical model from information_schema: python3 gen_physical.py > physical.dot

One node per table: MySQL column type, NULL or NOT NULL, PK/FK/UQ, then the indexes (without the primary key).
One edge per foreign key constraint, labelled with the constraint name and the ON DELETE rule.
"""
import schema_info as si

tables, fks, uniques, indexes = si.load()
fk_cols = {(f["table"], c) for f in fks for c in f["cols"]}
single_unique = {(t, cols[0]) for t, lst in uniques.items() for cols in lst if len(cols) == 1}


def esc(text):
    return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")


print("digraph physical {")
print('  rankdir=TB; nodesep=0.5; ranksep=0.9; pad=0.2; splines=true; newrank=true;')
print('  node [shape=plain, fontname="DejaVu Sans", fontsize=16]; '
      'edge [fontname="DejaVu Sans", fontsize=13, dir=both, penwidth=1.3];')

for t, cols in tables.items():
    rows = [f'<TR><TD COLSPAN="4" BGCOLOR="#dbe5f7"><B>{t}</B></TD></TR>']
    for c in cols:
        name = c["name"]
        tags = []
        if c["key"] == "PRI":
            tags.append("PK")
        if (t, name) in fk_cols:
            tags.append("FK")
        if (t, name) in single_unique:
            tags.append("UQ")
        if c["auto"]:
            tags.append("AUTO_INC")
        label = f"<U>{name}</U>" if c["key"] == "PRI" else name
        rows.append(
            f'<TR><TD ALIGN="LEFT" PORT="{name}">{label}</TD>'
            f'<TD ALIGN="LEFT">{esc(c["type"].replace("unsigned", "uns."))}</TD>'
            f'<TD ALIGN="LEFT">{"NULL" if c["nullable"] else "NOT NULL"}</TD>'
            f'<TD ALIGN="LEFT" PORT="{name}_r"><FONT COLOR="#a00000">{", ".join(tags) or " "}</FONT></TD></TR>')
    idx = indexes.get(t, [])
    if idx:
        rows.append('<TR><TD COLSPAN="4" ALIGN="LEFT" BGCOLOR="#f1f1f1"><I>Індекси (крім первинного ключа)</I></TD></TR>')
        for i in idx:
            kind = "UNIQUE " if i["unique"] else ""
            rows.append(f'<TR><TD COLSPAN="4" ALIGN="LEFT"><FONT POINT-SIZE="13">{kind}{i["name"]} '
                        f'({esc(", ".join(i["parts"]))})</FONT></TD></TR>')
    print(f'  {t} [label=<<TABLE BORDER="1" CELLBORDER="0" CELLSPACING="0" CELLPADDING="3" BGCOLOR="white">'
          f'{"".join(rows)}</TABLE>>];')

for f in fks:
    optional = any(c["nullable"] for c in tables[f["table"]] if c["name"] in f["cols"])
    tail = "teeodot" if optional else "teetee"
    label = f'{f["name"]}\\nON DELETE {f["on_delete"]}, ON UPDATE {f["on_update"]}'
    pairs = ", ".join(f"{c} = {p}" for c, p in zip(f["cols"], f["ref_cols"]))
    label += f"\\n({pairs})"  # child column = parent column; two pairs for a composite key
    print(f'  {f["ref_table"]} -> {f["table"]} '
          f'[arrowtail={tail}, arrowhead=crowodot, label="{label}"];')

print(f'  legend [shape=note, fontsize=13, margin="0.15,0.1", label="{si.legend_text()}'
      'PK первинний ключ, FK зовнішній, UQ унікальний, AUTO_INC автоінкремент. Усі зовнішні ключі RESTRICT.\\l"];')
print("  {rank=same; payments; legend}")
print("}")
