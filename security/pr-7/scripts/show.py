#!/usr/bin/env python3
"""Короткі зведення зі звітів Semgrep і Trivy для `make` (лише стандартна бібліотека).

Використання (python3 -I scripts/show.py ...):
  semgrep НОВИЙ.json ЗБЕРЕЖЕНИЙ.json      знахідки Semgrep і порівняння зі збереженим результатом зі звіту
  trivy НОВИЙ.json ЗБЕРЕЖЕНИЙ.json        кількості Trivy за серйозністю й головні пакети, порівняння зі збереженим
  same ПЕРШИЙ.json ДРУГИЙ.json           чи однакові кількості в двох звітах Trivy (jar і pom.xml)
  counts ФАЙЛ.json [ЗБЕРЕЖЕНИЙ.json]     лише кількості (для перевірок «що, якщо»)
  tracked ФАЙЛ.json                      лише кількості збереженого результату
"""
import json
import sys
from collections import Counter

SEV = ["CRITICAL", "HIGH", "MEDIUM", "LOW"]


def load(path):
    with open(path, encoding="utf-8") as f:
        return json.load(f)


def vulns(data):
    out = []
    for res in data.get("Results") or []:
        out.extend(res.get("Vulnerabilities") or [])
    return out


def counts(data):
    vs = vulns(data)
    c = Counter(v["Severity"] for v in vs)
    return len(vs), [c[s] for s in SEV], len({v["VulnerabilityID"] for v in vs})


def fmt(c):
    total, sev, uniq = c
    return f"всього {total}, C/H/M/L {'/'.join(map(str, sev))}, унікальних CVE {uniq}"


def semgrep(new, tracked):
    d, t = load(new), load(tracked)
    key = lambda r: (r["check_id"].split(".")[-1], r["path"], r["start"]["line"])
    sev = Counter(r["extra"]["severity"] for r in d["results"])
    print(f"Просканованих файлів: {len(d['paths']['scanned'])}, знахідок: {len(d['results'])} "
          f"(ERROR {sev['ERROR']}, WARNING {sev['WARNING']}), помилок сканера: {len(d['errors'])}")
    for r in sorted(d["results"], key=key):
        rule, path, line = key(r)
        print(f"  {rule:22} {path}:{line}  {r['extra']['severity']}")
    a, b = {key(r) for r in d["results"]}, {key(r) for r in t["results"]}
    if a == b and len(d["paths"]["scanned"]) == len(t["paths"]["scanned"]):
        print(f"Збігається зі збереженим {tracked}: знахідок {len(b)}, файлів {len(t['paths']['scanned'])}.")
    else:
        print(f"Відрізняється від збереженого {tracked}: нових {sorted(a - b)}, зниклих {sorted(b - a)}")


def trivy(new, tracked):
    d, t = load(new), load(tracked)
    print("Тепер:     ", fmt(counts(d)))
    print("У звіті:   ", fmt(counts(t)))
    pk = {}
    for v in vulns(d):
        pk.setdefault((v["PkgName"], v["InstalledVersion"]), Counter())[v["Severity"]] += 1
    print("Головні пакети (C/H/M/L):")
    top = sorted(pk.items(), key=lambda kv: (-kv[1]["CRITICAL"], -kv[1]["HIGH"], -sum(kv[1].values()), kv[0]))[:5]
    for (name, ver), c in top:
        print(f"  {name} {ver}: {'/'.join(str(c[s]) for s in SEV)}")
    if counts(d) == counts(t):
        print("Числа збігаються зі звітом.")
    else:
        print("Числа відрізняються від звіту: база Trivy новіша за ту, на якій складено звіт (нові CVE).")


def same(a, b):
    ca, cb = counts(load(a)), counts(load(b))
    print(f"jar:     {fmt(ca)}")
    print(f"pom.xml: {fmt(cb)}")
    print("jar і pom.xml збігаються." if ca == cb else "jar і pom.xml відрізняються.")


def main(argv):
    cmd = argv[0]
    if cmd == "semgrep":
        semgrep(argv[1], argv[2])
    elif cmd == "trivy":
        trivy(argv[1], argv[2])
    elif cmd == "same":
        same(argv[1], argv[2])
    elif cmd == "counts":
        now = counts(load(argv[1]))
        print("Тепер:  ", fmt(now))
        if len(argv) > 2:
            want = counts(load(argv[2]))
            print("У звіті:", fmt(want))
            print("Числа збігаються зі звітом." if now == want else "Числа відрізняються від звіту: база Trivy новіша (нові CVE).")
    elif cmd == "tracked":
        print(fmt(counts(load(argv[1]))))
    else:
        print(__doc__)
        return 2
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
