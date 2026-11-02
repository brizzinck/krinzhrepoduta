#!/usr/bin/env python3
"""Витягує з newman-report/run.json справжні запити й відповіді в текстові файли results/NN.txt."""
import json
import os

run = json.load(open("newman-report/run.json", encoding="utf-8"))["run"]
os.makedirs("results", exist_ok=True)
summary = []
for e in run["executions"]:
    name = e["item"]["name"]
    num = name.split(" - ")[0]
    rq, rs = e["request"], e["response"]
    q = rq["url"].get("query") or []
    qs = "&".join(f"{p['key']}={p['value']}" for p in q)
    url = "https://" + ".".join(rq["url"]["host"]) + "/" + "/".join(rq["url"]["path"]) + ("?" + qs if qs else "")
    out = [f"> {name}", f"{rq['method']} {url}"]
    skip = {"cookie", "postman-token", "cache-control", "connection", "accept-encoding", "content-length", "accept"}
    for h in rq.get("header", []):
        if h["key"].lower() in skip:
            continue
        out.append(f"{h['key']}: {h['value']}")
    body = (rq.get("body") or {}).get("raw")
    if body:
        out += ["", "Тіло запиту:", body]
    out += ["", f"Відповідь: {rs['code']} {rs['status']} ({rs['responseTime']} мс)"]
    raw = bytes(rs["stream"]["data"]).decode("utf-8")
    try:
        data = json.loads(raw)
        if "headers" in data:
            # повний набір заголовків відповіді довгий, для читабельності лишаємо ключові
            keep = {k: v for k, v in data["headers"].items() if k.startswith("x-qa") or k in ("host", "content-type", "user-agent")}
            data["headers"] = keep
        out.append(json.dumps(data, indent=2, ensure_ascii=False))
    except ValueError:
        out.append(raw)
    out.append("")
    for a in e["assertions"]:
        out.append(("PASS  " if a.get("error") is None else "FAIL  ") + a["assertion"])
    text = "\n".join(out) + "\n"
    with open(f"results/{num}.txt", "w", encoding="utf-8") as f:
        f.write(text)
    summary.append((name, rq["method"], url, rs["code"], len(e["assertions"]),
                    sum(1 for a in e["assertions"] if a.get("error") is None)))
for s in summary:
    print(s)
