#!/usr/bin/env python3
"""Генерує JSON-модель OWASP Threat Dragon v2.2.0 з даних rental_model.py.

Використання:  python3 -I threat-dragon/build_model.py threat-dragon/rental-threat-model.input.json
Результат імпортується в Threat Dragon (Open existing model -> вставити JSON), далі модель зберігається з UI.
"""
import json
import sys
import uuid
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))
import rental_model as m  # noqa: E402

NS = uuid.UUID("6f1c7a62-4b0e-4c9a-9d0f-2a7b1e8c5d11")
STRIDE_BY_KIND = {  # які типи загроз дозволяє Threat Dragon для кожного виду елемента
    "actor": {m.SP, m.RE},
    "store": {m.TA, m.RE, m.ID, m.DO},
    "process": {m.SP, m.TA, m.RE, m.ID, m.DO, m.EP},
    "flow": {m.TA, m.ID, m.DO},
}


DY = 60  # зсув усієї схеми вниз, щоб над межами лишилися коридори для довгих потоків
TOP_CORRIDOR = 95  # вершини вище цього значення (коридор над схемою) не зсуваються


def vy(y: int) -> int:
    return y if y <= TOP_CORRIDOR else y + DY


def uid(key: str) -> str:
    return str(uuid.uuid5(NS, key))


def build_threats():
    """element id -> список записів загроз; нумерація наскрізна, як у Threat Dragon."""
    per_element, number = {}, 0
    for tid, title, targets, L, I, status, desc, mitigation, asvs in m.THREATS:
        score = L * I
        for el, kind in targets:
            number += 1
            per_element.setdefault(el, []).append({
                "status": status, "severity": m.risk_level(score),
                "mitigation": f"{mitigation} (ASVS: {asvs})",
                "description": desc, "title": f"{tid} {title}", "type": kind, "modelType": "STRIDE",
                "id": uid(f"threat-{tid}-{el}-{kind}"), "number": number,
                "score": f"L{L} × I{I} = {score}", "new": False})
    for tid, title, el, kind, desc, decision in m.REJECTED:
        number += 1
        per_element.setdefault(el, []).append({
            "status": "NotApplicable", "severity": "Low", "mitigation": decision,
            "description": desc, "title": f"{tid} {title} (відхилено)", "type": kind, "modelType": "STRIDE",
            "id": uid(f"threat-{tid}-{el}"), "number": number, "score": "—", "new": False})
    return per_element, number


def style(has_open: bool, out_of_scope: bool):
    return {"stroke": "red" if has_open else "#333333", "strokeWidth": 2.5 if has_open else 1.5,
            "strokeDasharray": "4 3" if out_of_scope else None}


def main(out: Path):
    threats, top = build_threats()
    cells, z = [], 1

    for bid, name, x, y, w, h in m.BOUNDARIES:
        cells.append({
            "shape": "trust-boundary-box", "id": uid(bid), "zIndex": -50,
            "position": {"x": x, "y": y + DY}, "size": {"width": w, "height": h},
            "attrs": {"headerText": {"text": name},
                      "body": {"stroke": "#4a5a6a", "strokeWidth": 3, "strokeDasharray": "7 5"}},
            "data": {"type": "tm.BoundaryBox", "name": name, "description": "", "isTrustBoundary": True,
                     "hasOpenThreats": False}})

    for eid, kind, label, desc, x, y, w, h, props in m.ELEMENTS:
        own = threats.get(eid, [])
        for t in own:
            assert t["type"] in STRIDE_BY_KIND[kind], (eid, t["title"], t["type"])
        has_open = any(t["status"] == "Open" for t in own)
        oos = bool(props.get("outOfScope"))
        st = style(has_open, oos)
        shape = {"actor": "actor", "process": "process", "store": "store"}[kind]
        data = {"name": label, "description": desc, "type": {"actor": "tm.Actor", "process": "tm.Process", "store": "tm.Store"}[kind],
                "isTrustBoundary": False, "outOfScope": False, "reasonOutOfScope": "",
                "threats": own, "hasOpenThreats": has_open}
        if kind == "actor":
            data["providesAuthentication"] = False
            attrs = {"text": {"text": label}, "body": st}
        elif kind == "process":
            data.update({"handlesCardPayment": False, "handlesGoodsOrServices": False, "isWebApplication": False,
                         "privilegeLevel": ""})
            attrs = {"text": {"text": label}, "body": st}
        else:
            data.update({"isALog": False, "isEncrypted": False, "isSigned": False, "storesCredentials": False,
                         "storesInventory": False})
            attrs = {"text": {"text": label}, "topLine": st, "bottomLine": st}
        data.update(props)
        cells.append({"shape": shape, "id": uid(eid), "zIndex": z, "position": {"x": x, "y": y + DY},
                      "size": {"width": w, "height": h}, "attrs": attrs, "data": data})
        z += 1

    for fid, src, dst, label, desc, proto, enc, public, bidir, verts in m.FLOWS:
        own = threats.get(fid, [])
        for t in own:
            assert t["type"] in STRIDE_BY_KIND["flow"], (fid, t["title"], t["type"])
        has_open = any(t["status"] == "Open" for t in own)
        line = {"stroke": "red" if has_open else "#333333", "strokeWidth": 2.5 if has_open else 1.5,
                "targetMarker": {"name": "block"}, "strokeDasharray": None}
        if bidir:
            line["sourceMarker"] = {"name": "block"}
        cells.append({
            "shape": "flow", "id": uid(fid), "zIndex": 10, "width": 200, "height": 100, "connector": "rounded", "router": {"name": "orth"},
            "attrs": {"line": line},
            "labels": [{"position": 0.5, "attrs": {"label": {"text": label}}}],
            "source": {"cell": uid(src)}, "target": {"cell": uid(dst)},
            "vertices": [{"x": px, "y": vy(py)} for px, py in verts],
            "data": {"type": "tm.Flow", "name": label, "description": f"{desc} Дані: {proto}.",
                     "outOfScope": False, "reasonOutOfScope": "", "protocol": proto, "isEncrypted": enc,
                     "isPublicNetwork": public, "isBidirectional": bidir, "isTrustBoundary": False,
                     "hasOpenThreats": has_open, "threats": own}})

    model = {
        "version": "2.0",
        "summary": {
            "title": "Прокат автомобілів",
            "owner": "Лаврішко Владислав Сергійович",
            "description": "Модель загроз вебсервісу прокату автомобілів: реєстрація, пошук і бронювання авто, оплата, "
                           "сповіщення, адмін-панель. Методика STRIDE, оцінка ризику вручну (L × I).",
            "id": 0},
        "detail": {
            "contributors": [{"name": "Лаврішко В. С."}],
            "diagrams": [{
                "id": 0, "title": "DFD верхнього рівня", "diagramType": "STRIDE", "version": "2.0",
                "thumbnail": "./public/content/images/thumbnail.stride.jpg",
                "description": "Елементи EI/P/DS, 6 меж довіри TB-A…TB-F, потоки F1…F18.",
                "cells": cells}],
            "diagramTop": 0, "reviewer": "Ананченко О. Є.", "threatTop": top}}
    out.parent.mkdir(parents=True, exist_ok=True)
    out.write_text(json.dumps(model, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    print(f"готово: {out} ({len(cells)} клітинок, {top} записів загроз)")


if __name__ == "__main__":
    main(Path(sys.argv[1]))
