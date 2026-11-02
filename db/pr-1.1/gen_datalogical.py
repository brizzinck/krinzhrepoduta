#!/usr/bin/env python3
"""Збирає er_datalogical.dot (ER, «вороняча лапка») із реальної бази car_rental (information_schema).
Запуск: python3 gen_datalogical.py > er_datalogical.dot  (потрібен контейнер duikt-mysql і створена база)."""
import subprocess
from collections import OrderedDict
from pathlib import Path

M = str(Path(__file__).resolve().parent.parent / "_shared" / "mysql.sh")

def q(sql):
    out = subprocess.run([M, "-N", "-B", "-e", sql], capture_output=True, text=True, check=True).stdout
    return [l.split("\t") for l in out.rstrip("\n").split("\n") if l]

cols = q("SELECT table_name, column_name, column_type, is_nullable, column_key FROM information_schema.columns "
         "WHERE table_schema='car_rental' ORDER BY table_name, ordinal_position")
fks = q("SELECT k.table_name, k.column_name, k.referenced_table_name, r.delete_rule, c.is_nullable "
        "FROM information_schema.key_column_usage k "
        "JOIN information_schema.referential_constraints r ON r.constraint_schema=k.table_schema AND r.constraint_name=k.constraint_name "
        "JOIN information_schema.columns c ON c.table_schema=k.table_schema AND c.table_name=k.table_name AND c.column_name=k.column_name "
        "WHERE k.table_schema='car_rental' AND k.referenced_table_name IS NOT NULL ORDER BY k.table_name, k.column_name")
fkcols = {(t, c) for t, c, *_ in fks}
tables = OrderedDict()
for t, c, ty, nul, key in cols:
    tables.setdefault(t, []).append((c, ty, nul, key))

# назви зв'язків англійською (за вимогою інструкції)
NAMES = {
    ("car_models", "class_id"): "classifies", ("cars", "model_id"): "is_model_of", ("cars", "branch_id"): "is_stored_in",
    ("employees", "branch_id"): "employs", ("rentals", "customer_id"): "makes", ("rentals", "car_id"): "is_rented_in",
    ("rentals", "employee_id"): "serves", ("rentals", "pickup_branch_id"): "hands_over", ("rentals", "return_branch_id"): "receives",
    ("payments", "rental_id"): "is_paid_by", ("rental_extras", "rental_id"): "includes", ("rental_extras", "extra_id"): "is_added_in",
}
print("digraph datalogical {")
print('  rankdir=LR; nodesep=0.25; ranksep=0.7; pad=0.2; splines=true;')
print('  node [shape=plain, fontname="DejaVu Sans", fontsize=18]; edge [fontname="DejaVu Sans", fontsize=15, dir=both, penwidth=1.3];')
for t, cs in tables.items():
    rows = [f'<TR><TD COLSPAN="2" BGCOLOR="#dbe5f7"><B>{t}</B></TD></TR>']
    for c, ty, nul, key in cs:
        tag = "PK" if key == "PRI" else ("FK" if (t, c) in fkcols else "")
        if key == "PRI" and (t, c) in fkcols:
            tag = "PK, FK"
        elif key == "UNI" and not tag:
            tag = "UQ"
        name = f"<U>{c}</U>" if key == "PRI" else c
        if nul == "YES":
            name += " *"   # зірочка позначає стовпець, що допускає NULL
        ty = ty.replace("unsigned", "uns.")
        rows.append(f'<TR><TD ALIGN="LEFT" PORT="{c}">{name}</TD><TD ALIGN="LEFT" PORT="{c}_r"><FONT COLOR="#a00000">{tag or " "}</FONT></TD></TR>')
    print(f'  {t} [label=<<TABLE BORDER="1" CELLBORDER="0" CELLSPACING="0" CELLPADDING="3" BGCOLOR="white">{"".join(rows)}</TABLE>>];')
for t, c, parent, rule, nul in fks:
    optional = nul == "YES"
    tail = "teeodot" if optional else "teetee"
    pk = [x for x in tables[parent] if x[3] == "PRI"][0][0]
    print(f'  {parent}:{pk}_r:e -> {t}:{c}:w [arrowtail={tail}, arrowhead=crowodot, label="{NAMES.get((t, c), "")}"];')
print("}")
