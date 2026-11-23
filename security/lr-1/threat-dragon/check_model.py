#!/usr/bin/env python3
"""Перевіряє збережену модель Threat Dragon і порівнює підсумки з числами зі звіту.

Використання:  python3 -I threat-dragon/check_model.py model/rental-threat-model.json
Нічого не записує, лише друкує. Код завершення 1, якщо хоч одне число не збіглося.
"""
import json
import sys
from collections import Counter

ALLOWED = {  # які типи STRIDE дозволяє Threat Dragon для кожного виду елемента
    "tm.Actor": {"Spoofing", "Repudiation"},
    "tm.Store": {"Tampering", "Repudiation", "Information disclosure", "Denial of service"},
    "tm.Process": {"Spoofing", "Tampering", "Repudiation", "Information disclosure", "Denial of service",
                   "Elevation of privilege"},
    "tm.Flow": {"Tampering", "Information disclosure", "Denial of service"},
}
fails = []


def check(name, got, want):
    ok = got == want
    print(f"  {name}: {got}" + ("" if ok else f"  (у звіті {want})"))
    if not ok:
        fails.append(name)


with open(sys.argv[1], encoding="utf-8") as fh:
    model = json.load(fh)
cells = model["detail"]["diagrams"][0]["cells"]
kinds = Counter(c["data"]["type"] for c in cells)
print("Склад діаграми:")
check("зовнішні учасники (EI)", kinds["tm.Actor"], 5)
check("процеси (P)", kinds["tm.Process"], 6)
check("сховища (DS)", kinds["tm.Store"], 4)
check("межі довіри", kinds["tm.BoundaryBox"], 6)
check("потоки (F)", kinds["tm.Flow"], 18)

ids = {c["id"] for c in cells}
broken = [c["data"]["name"] for c in cells if c["shape"] == "flow"
          and (c["source"]["cell"] not in ids or c["target"]["cell"] not in ids)]
check("потоки з обірваним кінцем", len(broken), 0)

entries = []  # (елемент, загроза)
bad = 0
for c in cells:
    for t in c["data"].get("threats", []):
        entries.append(t)
        if t["type"] not in ALLOWED[c["data"]["type"]]:
            bad += 1
print("Загрози:")
check("типи STRIDE поза обмеженнями інструмента", bad, 0)
check("записів за елементами (рядків у Threat Dragon)", len(entries), 55)
st = Counter(t["status"] for t in entries)
check("записи Open / Mitigated / N/A", f"{st['Open']} / {st['Mitigated']} / {st['NotApplicable']}", "37 / 14 / 4")
sev_open = Counter(t["severity"] for t in entries if t["status"] == "Open")
check("відкриті записи High / Medium / Low", f"{sev_open['High']} / {sev_open['Medium']} / {sev_open['Low']}", "7 / 28 / 2")

uniq = {}
for t in entries:
    tid = t["title"].split()[0]
    if tid.startswith("T"):
        uniq[tid] = t
check("різних загроз T", len(uniq), 29)
check("відхилених FP", len({t["title"].split()[0] for t in entries if t["status"] == "NotApplicable"}), 4)
sv = Counter(t["severity"] for t in uniq.values())
check("за рівнем High / Medium / Low", f"{sv['High']} / {sv['Medium']} / {sv['Low']}", "11 / 17 / 1")
su = Counter(t["status"] for t in uniq.values())
check("за статусом Open / Mitigated", f"{su['Open']} / {su['Mitigated']}", "21 / 8")
print("Загрози високого рівня:")
for tid in sorted(k for k, t in uniq.items() if t["severity"] == "High"):
    t = uniq[tid]
    print(f"  {t['title']} | {t['score']} | {t['status']}")
if fails:
    print("Розбіжності:", ", ".join(fails))
    sys.exit(1)
print("Усі числа збігаються зі звітом.")
