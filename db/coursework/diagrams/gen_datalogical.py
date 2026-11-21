#!/usr/bin/env python3
"""Datalogical model (crow's foot) from information_schema: python3 gen_datalogical.py > datalogical.dot

One node per table (columns, keys, composite unique keys), one edge per foreign key constraint.
A composite foreign key is a single edge that names both column pairs.
"""
import schema_info as si

tables, fks, uniques, _indexes = si.load()
fk_cols = {(f["table"], c) for f in fks for c in f["cols"]}
single_unique = {(t, cols[0]) for t, lst in uniques.items() for cols in lst if len(cols) == 1}

print("digraph datalogical {")
print('  rankdir=TB; nodesep=0.5; ranksep=0.9; pad=0.2; splines=true; newrank=true;')
print('  node [shape=plain, fontname="DejaVu Sans", fontsize=18]; '
      'edge [fontname="DejaVu Sans", fontsize=14, dir=both, penwidth=1.3];')

for t, cols in tables.items():
    rows = [f'<TR><TD COLSPAN="2" BGCOLOR="#dbe5f7"><B>{t}</B></TD></TR>']
    for c in cols:
        name = c["name"]
        tags = []
        if c["key"] == "PRI":
            tags.append("PK")
        if (t, name) in fk_cols:
            tags.append("FK")
        if (t, name) in single_unique:
            tags.append("UQ")
        if si.code_key(t, name):
            tags.append("код")
        label = f"<U>{name}</U>" if c["key"] == "PRI" else name
        if c["nullable"]:
            label += " *"  # the star marks a column that allows NULL
        rows.append(f'<TR><TD ALIGN="LEFT" PORT="{name}">{label}</TD>'
                    f'<TD ALIGN="LEFT" PORT="{name}_r"><FONT COLOR="#a00000">{", ".join(tags) or " "}</FONT></TD></TR>')
    for cols_u in uniques.get(t, []):
        if len(cols_u) > 1:
            rows.append(f'<TR><TD COLSPAN="2" ALIGN="LEFT"><FONT COLOR="#a00000">UQ ({", ".join(cols_u)})</FONT></TD></TR>')
    print(f'  {t} [label=<<TABLE BORDER="1" CELLBORDER="0" CELLSPACING="0" CELLPADDING="3" BGCOLOR="white">'
          f'{"".join(rows)}</TABLE>>];')

for f in fks:
    down, up, card = si.RELATIONS[f["name"]]
    optional = any(c["nullable"] for c in tables[f["table"]] if c["name"] in f["cols"])
    tail = "teeodot" if optional else "teetee"          # parent side: exactly one, or zero or one
    head = "crowtee" if card == "1..N" else "crowodot"  # child side: one or many, or zero or many
    label = f"{down}\\n{up}"
    pairs = ", ".join(f"{c} = {p}" for c, p in zip(f["cols"], f["ref_cols"]))
    label += f"\\n({pairs})"  # child column = parent column; two pairs for a composite key
    print(f'  {f["ref_table"]} -> {f["table"]} '
          f'[arrowtail={tail}, arrowhead={head}, label="{label}"];')

print(f'  legend [shape=note, fontsize=13, margin="0.15,0.1", label="{si.legend_text()}'
      'Стовпець із зірочкою (*) допускає NULL. PK первинний ключ, FK зовнішній ключ, UQ унікальний.\\l"];')
print("  {rank=same; payments; legend}")
print("}")
