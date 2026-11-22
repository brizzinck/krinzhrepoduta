#!/usr/bin/env python3
"""Калькулятор CVSS v3.1 (базова й середовищна оцінка) за специфікацією FIRST, лише стандартна бібліотека.

Запуск (python3 -I scripts/cvss31.py ...):
  vector CVSS:3.1/AV:N/AC:L/PR:N/UI:N/S:U/C:H/I:H/A:H [--env CR:H/IR:H/AR:L/MAV:A/...]
  --selftest файл.json [файл.json ...]   звіряє вектори зі звітів Trivy з оцінками сканера
"""
import json
import math
import sys

AV = {"N": 0.85, "A": 0.62, "L": 0.55, "P": 0.2}
AC = {"L": 0.77, "H": 0.44}
PR_U = {"N": 0.85, "L": 0.62, "H": 0.27}
PR_C = {"N": 0.85, "L": 0.68, "H": 0.5}
UI = {"N": 0.85, "R": 0.62}
CIA = {"H": 0.56, "L": 0.22, "N": 0.0}
REQ = {"L": 0.5, "M": 1.0, "H": 1.5, "X": 1.0}


def roundup(x, v31=True):
    """Roundup з додатка A специфікації v3.1 (проти похибок double)."""
    if not v31:
        return math.ceil(x * 10) / 10
    n = int(round(x * 100000))
    if n % 10000 == 0:
        return n / 100000.0
    return (math.floor(n / 10000) + 1) / 10.0


def parse(vector):
    parts = vector.strip().split("/")
    if not parts[0].startswith("CVSS:3."):
        raise ValueError("очікується вектор CVSS:3.x")
    version = parts[0].split(":")[1]
    metrics = {}
    for p in parts[1:]:
        k, v = p.split(":")
        metrics[k] = v
    return version, metrics


def severity(score):
    if score == 0:
        return "None"
    if score < 4.0:
        return "Low"
    if score < 7.0:
        return "Medium"
    if score < 9.0:
        return "High"
    return "Critical"


def base(metrics, v31=True):
    changed = metrics["S"] == "C"
    pr = (PR_C if changed else PR_U)[metrics["PR"]]
    iss = 1 - (1 - CIA[metrics["C"]]) * (1 - CIA[metrics["I"]]) * (1 - CIA[metrics["A"]])
    if changed:
        impact = 7.52 * (iss - 0.029) - 3.25 * (iss - 0.02) ** 15
    else:
        impact = 6.42 * iss
    expl = 8.22 * AV[metrics["AV"]] * AC[metrics["AC"]] * pr * UI[metrics["UI"]]
    if impact <= 0:
        return 0.0
    total = 1.08 * (impact + expl) if changed else impact + expl
    return roundup(min(total, 10), v31)


def environmental(metrics, v31=True):
    """Середовищна оцінка: змінені базові метрики (M*) і вимоги CR, IR, AR; часові метрики за замовчуванням X."""
    def m(name):
        v = metrics.get("M" + name, "X")
        return metrics[name] if v == "X" else v
    s = m("S")
    changed = s == "C"
    pr = (PR_C if changed else PR_U)[m("PR")]
    miss = min(1 - (1 - CIA[m("C")] * REQ[metrics.get("CR", "X")])
               * (1 - CIA[m("I")] * REQ[metrics.get("IR", "X")])
               * (1 - CIA[m("A")] * REQ[metrics.get("AR", "X")]), 0.915)
    if changed:
        impact = 7.52 * (miss - 0.029) - 3.25 * (miss * 0.9731 - 0.02) ** 13
    else:
        impact = 6.42 * miss
    expl = 8.22 * AV[m("AV")] * AC[m("AC")] * pr * UI[m("UI")]
    if impact <= 0:
        return 0.0
    total = 1.08 * (impact + expl) if changed else impact + expl
    return roundup(roundup(min(total, 10), v31), v31)


def score_vector(vector, env=None):
    version, metrics = parse(vector)
    v31 = version == "1"
    b = base(metrics, v31)
    result = {"base": b, "severity": severity(b)}
    if env:
        for p in env.split("/"):
            k, v = p.split(":")
            metrics[k] = v
        e = environmental(metrics, v31)
        result["env"] = e
        result["env_severity"] = severity(e)
    return result


def selftest(files):
    seen = {}
    for path in files:
        with open(path, encoding="utf-8") as f:
            data = json.load(f)
        for res in data.get("Results") or []:
            for vuln in res.get("Vulnerabilities") or []:
                for source, c in (vuln.get("CVSS") or {}).items():
                    vec, sc = c.get("V3Vector"), c.get("V3Score")
                    if not vec or sc is None:
                        continue
                    key = (vec, sc)
                    seen.setdefault(key, set()).add(f"{vuln['VulnerabilityID']}[{source}]")
    bad = 0
    for (vec, sc), ids in sorted(seen.items(), key=lambda kv: kv[0]):
        got = score_vector(vec)["base"]
        ok = abs(got - sc) < 1e-9
        bad += 0 if ok else 1
        print(f"{'збіг' if ok else 'РОЗБІЖНІСТЬ'}: {vec} сканер {sc} скрипт {got} ({', '.join(sorted(ids))})")
    # контрольний вектор Log4Shell, CVE-2021-44228
    log4 = score_vector("CVSS:3.1/AV:N/AC:L/PR:N/UI:N/S:C/C:H/I:H/A:H")["base"]
    ok = log4 == 10.0
    bad += 0 if ok else 1
    print(f"{'збіг' if ok else 'РОЗБІЖНІСТЬ'}: Log4Shell CVSS:3.1/AV:N/AC:L/PR:N/UI:N/S:C/C:H/I:H/A:H очікується 10.0, скрипт {log4}")
    print(f"перевірено різних пар вектор-оцінка зі звітів: {len(seen)}; розбіжностей: {bad}")
    return 1 if bad else 0


def main(argv):
    if len(argv) >= 2 and argv[0] == "--selftest":
        return selftest(argv[1:])
    if not argv:
        print(__doc__)
        return 2
    env = None
    if "--env" in argv:
        env = argv[argv.index("--env") + 1]
    r = score_vector(argv[0], env)
    print(f"базова оцінка {r['base']} ({r['severity']})")
    if env:
        print(f"середовищна оцінка {r['env']} ({r['env_severity']})")
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
