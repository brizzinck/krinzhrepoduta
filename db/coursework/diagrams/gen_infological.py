#!/usr/bin/env python3
"""Infological model (Chen notation) from a hand-written description: python3 gen_infological.py > infological.dot

Adapted from the practical work 1.1 generator. Entities are rectangles, the associative entity (rides)
is a hexagon, relations are diamonds. Every node is pinned (pos=x,y!); draw with: neato -Tpng infological.dot
Relation names follow db-schema.md section 3, in Ukrainian for the reader.
"""
import math

X, Y = 4.6, 3.4
# id: (title, kind, (col, row), [(attribute, is_key, angle in degrees, distance)])
ENT = {
    "op":   ("Оператор",   "основна", (0, 2), [("Код оператора", 1, 160, 2.1), ("Назва", 0, 90, 1.1), ("Телефон", 0, 210, 1.7), ("Комісія", 0, 270, 1.05)]),
    "tar":  ("Тариф",      "основна", (1, 2), [("Код тарифу", 1, 135, 2.2), ("Тип засобу", 0, 15, 2.4), ("Ціна хвилини", 0, 70, 2.0), ("Розблокування", 0, 330, 2.2)]),
    "st":   ("Станція",    "основна", (2, 2), [("Код станції", 1, 90, 1.15), ("Назва", 0, 25, 1.8), ("Адреса", 0, 345, 2.0), ("Місткість", 0, 300, 1.9)]),
    "veh":  ("Засіб",      "основна", (1, 1), [("Код засобу", 1, 215, 1.9), ("Номер", 0, 180, 1.5), ("Модель", 0, 20, 1.8), ("Заряд", 0, 345, 1.7), ("Статус", 0, 310, 1.5)]),
    "cus":  ("Клієнт",     "основна", (0, 0), [("Код клієнта", 1, 180, 1.8), ("Прізвище", 0, 135, 2.0), ("Імʼя", 0, 90, 1.1), ("Телефон", 0, 215, 2.3), ("Верифікація", 0, 270, 1.2)]),
    "ride": ("Поїздка",    "асоціативна", (1, 0), [("Код поїздки", 1, 40, 2.0), ("Початок", 0, 5, 2.1), ("Завершення", 0, 325, 2.1), ("Комісія", 0, 150, 1.9)]),
    "pay":  ("Платіж",     "основна", (1, -1), [("Код платежу", 1, 195, 1.8), ("Вид", 0, 270, 1.0), ("Сума", 0, 330, 1.3), ("Час", 0, 20, 1.2)]),
}
# id: (name, ends [(entity, cardinality)], diamond position (col, row))
REL = [
    ("r1", "встановлює",   [("op", "1"), ("tar", "N")], (0.5, 2)),
    ("r2", "володіє",      [("op", "1"), ("veh", "N")], (0.5, 1.5)),
    ("r3", "оцінює",       [("tar", "1"), ("veh", "N")], (1, 1.5)),
    ("r4", "розміщує",     [("st", "1"), ("veh", "N")], (1.5, 1.5)),
    ("r5", "здійснює",     [("cus", "1"), ("ride", "N")], (0.5, 0)),
    ("r6", "використовується в", [("veh", "1"), ("ride", "N")], (1, 0.5)),
    ("r7", "має гаманець", [("cus", "1"), ("pay", "N")], (0.5, -0.5)),
    ("r8", "оплачується",  [("ride", "1"), ("pay", "N")], (1, -0.5)),
]


def pos(c, r):
    return (c * X, r * Y)


print("graph infological {")
print('  layout=neato; splines=false; overlap=true; pad=0.2;')
print('  node [fontname="DejaVu Sans", fontsize=13]; edge [fontname="DejaVu Sans", fontsize=13, fontcolor="#a00000"];')
for eid, (name, kind, (c, r), attrs) in ENT.items():
    x, y = pos(c, r)
    shape = "hexagon" if kind == "асоціативна" else "box"
    print(f'  {eid} [label="{name}", shape={shape}, style=filled, fillcolor="#e8eef9", penwidth=1.6, width=1.5, height=0.55, '
          f'fontsize=18, pos="{x:.2f},{y:.2f}!"];')
    for i, (a, key, ang, d) in enumerate(attrs):
        ax, ay = x + d * math.cos(math.radians(ang)), y + d * math.sin(math.radians(ang)) * 0.8
        lab = f"<<U>{a}</U>>" if key else f'"{a}"'
        print(f'  {eid}_a{i} [label={lab}, shape=ellipse, fontsize=14, width=0.9, height=0.4, pos="{ax:.2f},{ay:.2f}!"];')
        print(f'  {eid} -- {eid}_a{i} [penwidth=0.8];')
for rid, name, ends, (c, r) in REL:
    x, y = pos(c, r)
    print(f'  {rid} [label="{name}", shape=diamond, style=filled, fillcolor="#fff4d6", fontsize=13, width=1.6, height=0.85, '
          f'pos="{x:.2f},{y:.2f}!"];')
    for e, card in ends:
        lab = (f'<<TABLE BORDER="0" CELLBORDER="0" CELLPADDING="1" BGCOLOR="white"><TR><TD><FONT POINT-SIZE="17"><B>{card}</B>'
               '</FONT></TD></TR></TABLE>>')
        print(f'  {e} -- {rid} [taillabel={lab}, labeldistance=2.2, labelangle=0, penwidth=1.4];')
print("}")
