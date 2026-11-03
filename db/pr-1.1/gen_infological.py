#!/usr/bin/env python3
"""Збирає er_infological.dot (нотація Чена) з опису сутностей, зв'язків і атрибутів.
Усі вузли закріплені (pos=x,y!) для компактної діаграми; креслення: neato -Tpng (див. make_diagrams.sh)."""
import math

X, Y = 4.4, 3.3
# сутність: id та кортеж (назва, клас, (col,row), [(атрибут, ключ, кут, відстань)])
ENT = {
    "class":  ("Клас авто",  "основна", (0, 2), [("Код класу", 1, 150, 1.35), ("Назва", 0, 90, 1.0), ("Ставка", 0, 210, 1.2)]),
    "model":  ("Модель",     "основна", (1, 2), [("Код моделі", 1, 235, 1.8), ("Марка", 0, 90, 1.15), ("Назва", 0, 305, 1.6)]),
    "car":    ("Автомобіль", "основна", (2, 2), [("Код авто", 1, 125, 1.55), ("VIN", 0, 80, 1.15), ("Держномер", 0, 35, 1.75), ("Статус", 0, 0, 1.5)]),
    "branch": ("Філіал",     "основна", (2, 1), [("Код філіалу", 1, 20, 1.65), ("Назва", 0, 340, 1.4), ("Місто", 0, 55, 1.2)]),
    "emp":    ("Працівник",  "основна", (2, 0), [("Код працівника", 1, 15, 1.85), ("ПІБ", 0, 320, 1.3), ("Посада", 0, 270, 1.0)]),
    "cust":   ("Клієнт",     "основна", (0, 1), [("Код клієнта", 1, 160, 1.5), ("ПІБ", 0, 110, 1.05), ("Телефон", 0, 215, 1.4)]),
    "rent":   ("Оренда",     "асоціативна", (1, 1), [("Код оренди", 1, 140, 2.5), ("Початок", 0, 112, 1.35), ("Повернення", 0, 72, 1.9)]),
    "pay":    ("Платіж",     "основна", (1, 0), [("Код платежу", 1, 200, 1.6), ("Сума", 0, 270, 0.9), ("Вид", 0, 330, 1.2)]),
    "extra":  ("Послуга",    "основна", (0, 0), [("Код послуги", 1, 160, 1.5), ("Назва", 0, 215, 1.35), ("Ціна", 0, 270, 0.9)]),
}
# зв'язок: id та кортеж (назва, кінці [(сутність, кардинальність)], позиція-діамант (col,row), атрибути зв'язку)
REL = [
    ("r1", "класифікує",       [("class", "1"), ("model", "N")], (0.5, 2)),
    ("r2", "є моделлю",        [("model", "1"), ("car", "N")], (1.5, 2)),
    ("r3", "зберігається",     [("branch", "1"), ("car", "N")], (2, 1.5)),
    ("r4", "працює в",         [("branch", "1"), ("emp", "N")], (2, 0.5)),
    ("r5", "укладає",          [("cust", "1"), ("rent", "N")], (0.5, 1)),
    ("r6", "стосується",       [("car", "1"), ("rent", "N")], (1.5, 1.5)),
    ("r7", "оформляє",         [("emp", "0..1"), ("rent", "N")], (1.5, 0.5)),
    ("r8", "видає/приймає",    [("branch", "1"), ("rent", "N")], (1.5, 1)),
    ("r9", "сплачується",      [("rent", "1"), ("pay", "N")], (1, 0.5)),
    ("r10", "включає",         [("rent", "M"), ("extra", "N")], (0.5, 0.5)),
]
RATTR = {"r10": ("Кількість", 0, 200, 1.5)}

def pos(c, r): return (c * X, r * Y)
print("graph infological {")
print('  layout=neato; splines=false; overlap=true; pad=0.2;')
print('  node [fontname="DejaVu Sans", fontsize=13]; edge [fontname="DejaVu Sans", fontsize=13, fontcolor="#a00000", labeldistance=2.0, labelangle=0];')
for eid, (name, cls, (c, r), attrs) in ENT.items():
    x, y = pos(c, r)
    # асоціативна сутність, як у матеріалах курсу, шестикутник; основні сутності прямокутники
    shape = "hexagon" if cls == "асоціативна" else "box"
    print(f'  {eid} [label="{name}", shape={shape}, style=filled, fillcolor="#e8eef9", penwidth=1.6, width=1.5, height=0.55, fontsize=18, pos="{x:.2f},{y:.2f}!"];')
    for i, (a, key, ang, d) in enumerate(attrs):
        ax, ay = x + d * math.cos(math.radians(ang)) * 1.0, y + d * math.sin(math.radians(ang)) * 0.8
        lab = f"<<U>{a}</U>>" if key else f'"{a}"'
        print(f'  {eid}_a{i} [label={lab}, shape=ellipse, fontsize=14, width=0.9, height=0.4, pos="{ax:.2f},{ay:.2f}!"];')
        print(f'  {eid} -- {eid}_a{i} [penwidth=0.8];')
for rid, name, ends, (c, r) in REL:
    x, y = pos(c, r)
    print(f'  {rid} [label="{name}", shape=diamond, style=filled, fillcolor="#fff4d6", fontsize=13, width=1.6, height=0.85, pos="{x:.2f},{y:.2f}!"];')
    for e, card in ends:
        lab = f'<<TABLE BORDER="0" CELLBORDER="0" CELLPADDING="1" BGCOLOR="white"><TR><TD><FONT POINT-SIZE="17"><B>{card}</B></FONT></TD></TR></TABLE>>'
        print(f'  {e} -- {rid} [taillabel={lab}, labeldistance=2.2, labelangle=0, penwidth=1.4];')
    if rid in RATTR:
        a, key, ang, d = RATTR[rid]
        ax, ay = x + d * math.cos(math.radians(ang)), y + d * math.sin(math.radians(ang)) * 0.8
        print(f'  {rid}_a0 [label="{a}", shape=ellipse, fontsize=14, width=0.9, height=0.4, pos="{ax:.2f},{ay:.2f}!"];')
        print(f'  {rid} -- {rid}_a0 [penwidth=0.8, style=dashed];')
print("}")
