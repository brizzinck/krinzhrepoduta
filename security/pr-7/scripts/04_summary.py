#!/usr/bin/env python3
"""Збирає числа зі звітів Semgrep і Trivy в results/summary.md (запуск: python3 -I scripts/04_summary.py)."""
import glob
import json
import os
import re

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
RES = os.path.join(ROOT, "results")
SEV = ["CRITICAL", "HIGH", "MEDIUM", "LOW", "UNKNOWN"]


def load(name):
    with open(os.path.join(RES, name), encoding="utf-8") as f:
        return json.load(f)


def vkey(v):
    return tuple(int(x) if x.isdigit() else 0 for x in re.split(r"[.\-]", v.split("::")[0]) if x)


def line_of(v):
    return ".".join(v.split(".")[:2])


def rows(data):
    out = []
    for res in data.get("Results") or []:
        out.extend(res.get("Vulnerabilities") or [])
    return out


def counts(vs):
    c = {s: 0 for s in SEV}
    for v in vs:
        c[v["Severity"]] += 1
    return c


lines = []
w = lines.append
w("# Підсумок запусків ПР 7\n")
w("Усі числа взяті зі збережених файлів у results/ скриптом scripts/04_summary.py.\n")

w("## Semgrep\n")
w("| ЛР | файлів скановано | правил | знахідок | ERROR | WARNING | INFO | помилок сканера |")
w("|---|---|---|---|---|---|---|---|")
for lr in ("lr3", "lr6"):
    d = load(f"semgrep-{lr}.json")
    txt = open(os.path.join(RES, f"semgrep-{lr}.txt"), encoding="utf-8").read()
    rules = re.search(r"Rules run:\s*(\d+)", txt).group(1)
    sev = {"ERROR": 0, "WARNING": 0, "INFO": 0}
    for r in d["results"]:
        sev[r["extra"]["severity"]] += 1
    w(f"| {lr} | {len(d['paths']['scanned'])} | {rules} | {len(d['results'])} | {sev['ERROR']} | {sev['WARNING']} | {sev['INFO']} | {len(d['errors'])} |")
w("")
w("Знахідки:\n")
w("| ЛР | правило | файл:рядок | серйозність |")
w("|---|---|---|---|")
for lr in ("lr3", "lr6"):
    for r in load(f"semgrep-{lr}.json")["results"]:
        rid = r["check_id"].split(".")[-1]
        w(f"| {lr} | {rid} | {r['path']}:{r['start']['line']} | {r['extra']['severity']} |")
w("")

w("## Trivy: кількість знахідок (рядків звіту, один рядок на пакет і CVE)\n")
w("| ЛР | ціль | всього | CRITICAL | HIGH | MEDIUM | LOW | унікальних ідентифікаторів |")
w("|---|---|---|---|---|---|---|---|")
for lr in ("lr3", "lr6"):
    for kind in ("jar", "pom"):
        vs = rows(load(f"trivy-{lr}-{kind}.json"))
        c = counts(vs)
        w(f"| {lr} | {kind} | {len(vs)} | {c['CRITICAL']} | {c['HIGH']} | {c['MEDIUM']} | {c['LOW']} | {len({v['VulnerabilityID'] for v in vs})} |")
w("")

for lr in ("lr3", "lr6"):
    vs = rows(load(f"trivy-{lr}-jar.json"))
    w(f"## Пакети з вразливостями, {lr} (jar)\n")
    w("| пакет | встановлено | CRIT | HIGH | MED | LOW | найвища виправлена версія в тій самій лінії |")
    w("|---|---|---|---|---|---|---|")
    pk = {}
    for v in vs:
        pk.setdefault((v["PkgName"], v["InstalledVersion"]), []).append(v)
    for (name, ver), items in sorted(pk.items(), key=lambda kv: (-counts(kv[1])["CRITICAL"], -counts(kv[1])["HIGH"], kv[0])):
        c = counts(items)
        best = None
        for v in items:
            same = [x.strip() for x in v.get("FixedVersion", "").split(",") if line_of(x.strip()) == line_of(ver)]
            if same:
                top = max(same, key=vkey)
                best = top if best is None or vkey(top) > vkey(best) else best
            else:
                best = best or None
        w(f"| {name} | {ver} | {c['CRITICAL']} | {c['HIGH']} | {c['MEDIUM']} | {c['LOW']} | {best or 'у цій лінії немає'} |")
    w("")

w("## Перевірки \"що, якщо\" (збірка копії з іншим parent)\n")
w("| копія | всього | CRITICAL | HIGH | MEDIUM | LOW |")
w("|---|---|---|---|---|---|")
for f in sorted(glob.glob(os.path.join(RES, "trivy-*-whatif-*.json"))):
    name = os.path.basename(f)[len("trivy-"):-len(".json")]
    vs = rows(json.load(open(f, encoding="utf-8")))
    c = counts(vs)
    w(f"| {name} | {len(vs)} | {c['CRITICAL']} | {c['HIGH']} | {c['MEDIUM']} | {c['LOW']} |")
for f in sorted(glob.glob(os.path.join(RES, "build-*-whatif-*.txt"))):
    txt = open(f, encoding="utf-8").read()
    status = "BUILD SUCCESS" if "BUILD SUCCESS" in txt else ("BUILD FAILURE" if "BUILD FAILURE" in txt else "невідомо")
    w(f"| збірка {os.path.basename(f)[len('build-'):-4]} | {status} | | | | |")
w("")

with open(os.path.join(RES, "summary.md"), "w", encoding="utf-8") as f:
    f.write("\n".join(lines) + "\n")
print("results/summary.md записано")
