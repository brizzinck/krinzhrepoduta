#!/usr/bin/env python3
"""Перевіряє requirements.md і report.md ЛР 2 проти реєстру загроз ЛР 1.

Використання (з теки security/lr-2):  python3 -I scripts/check_requirements.py [numbers|rule|coverage|report]
Без аргумента виконуються всі частини. Нічого не записує; код завершення 1, якщо перевірка не пройшла.
"""
import re
import sys
from collections import Counter
from pathlib import Path

HERE = Path(__file__).resolve().parent.parent
REQ = (HERE / "requirements.md").read_text(encoding="utf-8")
REPORT = (HERE / "report.md").read_text(encoding="utf-8")
THREATS = (HERE.parent / "lr-1" / "model" / "threats.md").read_text(encoding="utf-8")
fails = []


def check(name, got, want):
    ok = got == want
    print(f"  {name}: {got}" + ("" if ok else f"  (очікувалось {want})"))
    if not ok:
        fails.append(name)


def cells(line):
    return [c.strip() for c in line.strip().strip("|").split("|")]


def tables(text, section):
    """Рядки таблиць у розділах `## ...`, назва яких містить section: (заголовок ###, клітинки)."""
    sec = sub = ""
    out = []
    for line in text.splitlines():
        if line.startswith("## "):
            sec, sub = line, ""
        elif line.startswith("### "):
            sub = line[4:]
        elif section in sec and line.startswith("|") and not line.startswith("|--") and not line.startswith("| ASVS"):
            out.append((sub, cells(line)))
    return out


ROWS = tables(REQ, "Вимоги за елементами") + tables(REQ, "Вимоги за потоками")
rows = [r for _, r in ROWS]
ids = {r[0] for r in rows}
asvs = {i for i in ids if re.fullmatch(r"\d+\.\d+\.\d+", i)}


def is_mand(r):
    return r[3].startswith("Обов")


def kind(r):
    return "mand" if is_mand(r) else "s3" if "етап 3" in r[3] else "s2"


def numbers():
    print("Числа з requirements.md:")
    k = Counter(kind(r) for r in rows)
    check("рядків (пар елемент і вимога)", len(rows), 208)
    check("різних ID ASVS", len(asvs), 133)
    check("власних вимог (не з ASVS)", sorted(ids - asvs), ["власна"])
    check("обов'язкових / етап 2 / етап 3", f"{k['mand']} / {k['s2']} / {k['s3']}", "101 / 95 / 12")
    na = [r for _, r in tables(REQ, "не застосовано") if re.fullmatch(r"\d+\.\d+\.\d+", r[0])]
    check("вимог L1 не застосовано (з 70)", len(na), 14)
    check("різних вимог L1 застосовано", len({r[0] for r in rows if r[1] == "1"}), 56)


def rule():
    print("Правило поділу:")
    check("L1, які не обов'язкові", [r[0] for r in rows if r[1] == "1" and not is_mand(r)], [])
    check("L3, які обов'язкові", [r[0] for r in rows if r[1] == "3" and is_mand(r)], [])
    check("обов'язкові L2 без високої загрози (В)", [r[0] for r in rows if r[1] == "2" and is_mand(r) and "(В)" not in r[5]], [])


def coverage():
    print("Покриття загроз ЛР 1:")
    reg = {}
    for line in THREATS.splitlines():
        if re.match(r"\| T\d+ \|", line):
            c = cells(line)
            reg[c[0]] = c[6].split()[-1]  # «15 Високий»: рівень ризику
    trace = {}
    for _, r in tables(REQ, "Трасування"):
        m = re.match(r"(T\d+) ", r[0])
        if m:
            trace[m.group(1)] = r
    check("загроз у реєстрі ЛР 1", len(reg), 29)
    check("загроз у трасуванні", len(trace), 29)
    check("загрози без жодної вимоги", [t for t, r in trace.items() if r[2] == "немає" and r[3] == "немає"], [])
    high = sorted(t for t, lvl in reg.items() if lvl == "Високий")
    check("загроз високого ризику", len(high), 11)
    check("високі без обов'язкової вимоги", [t for t in high if trace[t][2] == "немає"], [])
    check("рівні ризику, що не збігаються з ЛР 1", [t for t in trace if trace[t][1] != reg[t]], [])
    used = {x.strip() for r in trace.values() for x in (r[2] + "," + r[3]).split(",")} - {"немає", "власна", ""}
    check("ID з трасування, яких немає в таблицях вимог", sorted(used - asvs), [])
    print("  обов'язкові вимоги для високих загроз:")
    for t in high:
        print(f"    {t}: {trace[t][2]}")


def report():
    print("Таблиця розділу 4 звіту проти requirements.md (обов'язкові / етап 2 / етап 3 / разом):")
    got = {}
    for sub, r in ROWS:
        key = "Потоки" if re.match(r"F\d", sub) else sub.split(" (")[0].split(",")[0].split(" ")[0]
        c = got.setdefault(key, Counter())
        c[kind(r)] += 1
    for line in REPORT.splitlines():
        m = re.match(r"\| (EI-\d|P-\d|DS-\d|Залежності|Потоки)\b[^|]*\| (\d+) \| (\d+) \| (\d+) \| (\d+) \|", line)
        if m:
            c = got[m.group(1)]
            have = f"{c['mand']} / {c['s2']} / {c['s3']} / {sum(c.values())}"
            want = " / ".join(m.groups()[1:])
            check(line.split("|")[1].strip(), have, want)
    m = re.search(r"\| Усього \| (\d+) \| (\d+) \| (\d+) \| (\d+) \|", REPORT)
    k = Counter(kind(r) for r in rows)
    check("Усього", f"{k['mand']} / {k['s2']} / {k['s3']} / {len(rows)}", " / ".join(m.groups()))


PARTS = {"numbers": numbers, "rule": rule, "coverage": coverage, "report": report}
for name in ([sys.argv[1]] if len(sys.argv) > 1 else PARTS):
    PARTS[name]()
if fails:
    print("Не пройшло:", "; ".join(fails))
    sys.exit(1)
print("Перевірку пройдено.")
