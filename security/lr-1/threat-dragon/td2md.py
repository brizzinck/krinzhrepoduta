#!/usr/bin/env python3
"""Читає JSON-модель, збережену з Threat Dragon, і формує таблиці для звіту.

Використання:  python3 -I threat-dragon/td2md.py model/rental-threat-model.json model/
Створює model/threats.md (реєстр загроз із оцінкою ризику) та model/architecture.md (елементи, межі, потоки).
Так таблиці у звіті збігаються з тим, що лежить у моделі, і їх не доводиться переписувати вручну.
"""
import json
import re
import sys
from collections import Counter, OrderedDict
from pathlib import Path

LEVEL_UA = {"High": "Високий", "Medium": "Середній", "Low": "Низький"}
STATUS_UA = {"Open": "Відкрита", "Mitigated": "Зменшена (передбачено захід)", "NotApplicable": "Прийнято / поза межами"}
KIND_UA = {"tm.Actor": "Зовнішній учасник", "tm.Process": "Процес", "tm.Store": "Сховище даних"}
SHORT = {"Spoofing": "S", "Tampering": "T", "Repudiation": "R", "Information disclosure": "I",
         "Denial of service": "D", "Elevation of privilege": "E"}


def esc(text: str) -> str:
    return " ".join(str(text).split()).replace("|", "\\|")


def main(src: Path, out: Path):
    model = json.loads(src.read_text(encoding="utf-8"))
    cells = model["detail"]["diagrams"][0]["cells"]
    name_of = {c["id"]: c["data"]["name"].split("\n")[0].split(" ")[0] for c in cells if c.get("data") and c["data"].get("type") != "tm.BoundaryBox"}

    # ---- загрози: групуємо записи за ідентифікатором «T01…»
    groups = OrderedDict()
    for c in cells:
        for t in (c.get("data") or {}).get("threats", []):
            tid = t["title"].split()[0]
            g = groups.setdefault(tid, {"title": t["title"][len(tid) + 1:], "elements": [], "stride": [], "t": t})
            el = name_of[c["id"]]
            if el not in g["elements"]:
                g["elements"].append(el)
            s = SHORT[t["type"]]
            if s not in g["stride"]:
                g["stride"].append(s)
    real = {k: v for k, v in groups.items() if k.startswith("T")}
    rejected = {k: v for k, v in groups.items() if k.startswith("FP")}

    rows, levels, statuses = [], Counter(), Counter()
    for tid, g in sorted(real.items()):
        t = g["t"]
        m = re.match(r"L(\d) × I(\d) = (\d+)", t["score"])
        L, I, score = (int(x) for x in m.groups())
        levels[t["severity"]] += 1
        statuses[t["status"]] += 1
        mit = t["mitigation"]
        rows.append(f"| {tid} | {esc(', '.join(sorted(g['elements'], key=lambda e: (e[0] != 'E', e))))} | {', '.join(g['stride'])} | "
                    f"{esc(g['title'])} | {L} | {I} | {score} {LEVEL_UA[t['severity']]} | {STATUS_UA[t['status']]} | {esc(mit)} |")

    lines = ["# Реєстр загроз «Прокат автомобілів» (STRIDE)", "",
             "Таблиця згенерована з `model/rental-threat-model.json` скриптом `threat-dragon/td2md.py`.", "",
             "Ризик = Імовірність (L, 1–5) × Наслідок (I, 1–5): 1–6 низький, 7–14 середній, 15–25 високий.", "",
             "| ID | Елементи | STRIDE | Загроза | L | I | Ризик | Статус | Захід (ASVS) |", "|---|------|---|---------|-|-|-----|------|-------------|"] + rows
    lines += ["", f"Усього загроз: {len(real)} (записів у моделі за елементами: {sum(len(c['data'].get('threats', [])) for c in cells if c.get('data'))}).", "",
              "Підсумок за рівнем: " + ", ".join(f"{LEVEL_UA[k]} {levels[k]}" for k in ("High", "Medium", "Low")) + ".", "",
              "Підсумок за статусом: " + ", ".join(f"{STATUS_UA[k]} {statuses[k]}" for k in ("Open", "Mitigated")) + ".", "",
              "## Розглянуті й відхилені", "", "| ID | Елемент | STRIDE | Опис | Рішення |", "|---|-----|---|-----------|----------|"]
    for tid, g in sorted(rejected.items()):
        t = g["t"]
        lines.append(f"| {tid} | {', '.join(g['elements'])} | {', '.join(g['stride'])} | {esc(t['title'].split(' ', 1)[1].replace(' (відхилено)', ''))}: {esc(t['description'])} | {esc(t['mitigation'])} |")
    (out / "threats.md").write_text("\n".join(lines) + "\n", encoding="utf-8")

    # ---- архітектура
    arch = ["# Опис архітектури «Прокат автомобілів»", "", "## Елементи", "", "| ID | Тип | Назва | Опис і властивості |", "|--|-----|-------|---------------------------|"]
    for c in cells:
        d = c.get("data") or {}
        if d.get("type") in KIND_UA:
            props = [lab for key, lab in (("outOfScope", "поза межами аналізу"), ("storesCredentials", "зберігає облікові дані"),
                                           ("isEncrypted", "шифрування"), ("isALog", "журнал"), ("isWebApplication", "вебзастосунок"),
                                           ("handlesCardPayment", "картки: лише через сторінку шлюзу")) if d.get(key)]
            if d.get("privilegeLevel"):
                props.append("привілеї: " + d["privilegeLevel"])
            if d.get("reasonOutOfScope"):
                props.append(d["reasonOutOfScope"])
            nm = d["name"].split("\n")
            arch.append(f"| {nm[0]} | {KIND_UA[d['type']]} | {esc(' '.join(nm[1:]))} | {esc(d['description'])} ({esc('; '.join(props))}) |" if props
                        else f"| {nm[0]} | {KIND_UA[d['type']]} | {esc(' '.join(nm[1:]))} | {esc(d['description'])} |")
    arch += ["", "## Межі довіри", "", "| Межа | Назва |", "|---|-----------------|"]
    for c in cells:
        d = c.get("data") or {}
        if d.get("type") == "tm.BoundaryBox":
            tb, _, rest = d["name"].partition(" ")
            arch.append(f"| {tb} | {esc(rest)} |")
    arch += ["", "## Потоки даних", "", "| ID | Напрямок | Дані | Протокол і захист |", "|--|-------|-----------|-----------|"]
    for c in cells:
        d = c.get("data") or {}
        if d.get("type") == "tm.Flow":
            fid = d["name"].split()[0]
            a, b = name_of[c["source"]["cell"]], name_of[c["target"]["cell"]]
            way = f"між {a} і {b}" if d.get("isBidirectional") else f"з {a} до {b}"
            desc = d["description"].split(" Дані:")[0]
            arch.append(f"| {fid} | {way} | {esc(desc)} | {esc(d['protocol'])} |")
    (out / "architecture.md").write_text("\n".join(arch) + "\n", encoding="utf-8")
    print(f"готово: {out / 'threats.md'}, {out / 'architecture.md'}; загроз {len(real)}, рівні {dict(levels)}, статуси {dict(statuses)}")


if __name__ == "__main__":
    main(Path(sys.argv[1]), Path(sys.argv[2]))
