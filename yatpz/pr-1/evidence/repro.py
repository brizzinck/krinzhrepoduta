#!/usr/bin/env python3
"""Відтворення дефектів сторінки https://demoqa.com/webtables (Playwright, Chromium, headless).

Кожен дефект виконується двічі у свіжому контексті браузера (нове завантаження сторінки).
Дефект вважається відтвореним, якщо умова check виконалась в обох прогонах.
Результати: evidence/repro-BUG-XX.txt, знімки: screenshots/ (другий прогін) і evidence/run1/ (перший).
Запуск з теки роботи: ../_tools/pw.sh evidence/repro.py [BUG-01 BUG-05 ...]
"""
import datetime
import os
import sys

sys.path.insert(0, os.path.dirname(__file__))
from lib import *  # noqa: E402

RUNS = 2
LOG = []


def log(msg):
    LOG.append(str(msg))


def names(pg):
    return [r[0] for r in rows(pg)]


def mark(pg, selector, color="red"):
    highlight(pg, selector, color)


def mark_row(pg, text, nth=0, color="red"):
    pg.evaluate("""([t, n, c]) => { const r = [...document.querySelectorAll('table tbody tr')].filter(x => x.innerText.includes(t))[n];
        if (r) { r.style.outline = '3px solid ' + c; r.style.outlineOffset = '-2px'; } }""", [text, nth, color])


def shot(pg, path, clip="main"):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    if clip == "main":
        pg.screenshot(path=path, clip={"x": 230, "y": 100, "width": 740, "height": 520})
    elif clip == "full":
        pg.wait_for_timeout(800)  # завершення анімації модального вікна
        pg.screenshot(path=path)
    elif clip == "modal":  # лише модальне вікно (форма)
        pg.wait_for_timeout(800)
        pg.locator(".modal-content").screenshot(path=path)
    elif clip == "strip":  # смуга таблиці на всю ширину сторінки з позначкою межі екрана
        sw = pg.evaluate("document.documentElement.scrollWidth"); vw = pg.evaluate("window.innerWidth")
        pg.evaluate("""(vw) => { const d = document.createElement('div');
            d.style.cssText = 'position:absolute;top:0;left:' + vw + 'px;height:' + document.documentElement.scrollHeight + 'px;border-left:6px dashed #0050ff;z-index:9999';
            const l = document.createElement('div'); l.textContent = 'межа екрана ' + vw + ' px';
            l.style.cssText = 'position:absolute;top:290px;left:12px;font:bold 56px sans-serif;color:#0050ff;white-space:nowrap;background:#fff';
            d.appendChild(l); document.body.appendChild(d); }""", vw)
        t = pg.evaluate("(() => { const r = document.querySelector('table').getBoundingClientRect(); return [r.top + window.scrollY, r.height]; })()")
        pg.screenshot(path=path, full_page=True, clip={"x": 230, "y": t[0] - 10, "width": sw - 230, "height": t[1] + 20})
    elif clip == "page":
        pg.screenshot(path=path, full_page=True)
    else:
        pg.screenshot(path=path, clip=clip)


# дефекти
def bug01(pg, out):
    """Delete скидає таблицю до початкового набору (втрата доданих записів, повернення видалених)"""
    add(pg, first="Newbie", last="Lee", email="newbie@example.com", age="30", salary="1000", dept="QA")
    before = names(pg); log(f"після Add: {before}")
    mark_row(pg, "Newbie", color="green"); shot(pg, out("BUG-01-a"))
    pg.click("#delete-record-1"); pg.wait_for_timeout(600)
    after = names(pg); log(f"після Delete (Cierra): {after}")
    mark(pg, "table tbody"); shot(pg, out("BUG-01-b"))
    ok1 = "Newbie" in before and "Newbie" not in after
    # другий сценарій: видалений запис повертається
    pg2 = open_page(pg.context.browser)
    pg2.click("#delete-record-1"); pg2.wait_for_timeout(500); s1 = names(pg2)
    pg2.click("#delete-record-3"); pg2.wait_for_timeout(500); s2 = names(pg2)
    log(f"сценарій 2: після Delete Cierra {s1}; після Delete Kierra {s2}")
    ok2 = "Cierra" not in s1 and "Cierra" in s2
    pg2.context.close()
    return ok1 and ok2


def bug02(pg, out):
    """Форма Add заповнена даними запису з закритої форми Edit"""
    pg.click("#edit-record-1"); pg.wait_for_selector("#userForm"); pg.click(".btn-close"); pg.wait_for_timeout(400)
    pg.click("#addNewRecordButton"); pg.wait_for_selector("#userForm")
    vals = {k: pg.input_value("#" + k) for k in FIELDS}; log(f"поля форми Add: {vals}")
    mark(pg, "#userForm input"); shot(pg, out("BUG-02"), "modal")
    return vals["firstName"] == "Cierra" and vals["userEmail"] == "cierra@example.com"


def bug03(pg, out):
    """Ім'я з одних пробілів приймається"""
    add(pg, first="   ", last="Lee")
    r = rows(pg); log(f"рядків: {len(r)}; новий рядок: {r[-1] if len(r) > 3 else None}; модальне вікно відкрите: {modal_open(pg)}")
    mark_row(pg, "Lee"); shot(pg, out("BUG-03"))
    return len(r) == 4 and r[-1][0] == "" and not modal_open(pg)


def bug04(pg, out):
    """Вік 0 приймається"""
    add(pg, first="Zero", age="0")
    r = rows(pg); log(f"новий рядок: {r[-1] if len(r) > 3 else None}")
    mark_row(pg, "Zero"); shot(pg, out("BUG-04"))
    return len(r) == 4 and r[-1][2] == "0"


def bug05(pg, out):
    """Коректні email відхиляються"""
    res = {}
    for em in ["john+test@example.com", "user@example.company"]:
        p2 = open_page(pg.context.browser)
        add(p2, first="Mail", email=em)
        res[em] = (len(rows(p2)) == 3 and modal_open(p2))
        if em.startswith("john"):
            mark(p2, "#userEmail"); shot(p2, out("BUG-05"), "modal")
        p2.context.close()
    log(f"відхилено (модальне вікно лишилось відкритим, рядок не додано): {res}")
    return all(res.values())


def bug06(pg, out):
    """Некоректні email приймаються"""
    res = {}
    for em in ["a..b@example.com", "user@.example.com", "user@example..com"]:
        p2 = open_page(pg.context.browser)
        add(p2, first="Mail", email=em)
        r = rows(p2); res[em] = len(r) == 4 and r[-1][3] == em
        if em == "a..b@example.com":
            mark_row(p2, "a..b@example.com"); shot(p2, out("BUG-06"))
        p2.context.close()
    log(f"збережено як коректні: {res}")
    return all(res.values())


def bug07(pg, out):
    """Email без обмеження довжини ламає верстку"""
    base = pg.evaluate("document.documentElement.scrollWidth"); long_email = "e" * 200 + "@example.com"
    add(pg, first="Long", email=long_email)
    tw = pg.evaluate("document.querySelector('table').getBoundingClientRect().width"); sw = pg.evaluate("document.documentElement.scrollWidth")
    maxlen = pg.evaluate("(() => { document.getElementById('addNewRecordButton').click(); return document.getElementById('userEmail')?.getAttribute('maxlength') })()")
    log(f"довжина email: {len(long_email)}; ширина таблиці: {round(tw)} px; scrollWidth сторінки: {sw} px (до додавання: {base}); атрибут maxlength поля Email: {maxlen}")
    pg.keyboard.press("Escape"); pg.wait_for_timeout(300)
    mark(pg, "table"); shot(pg, out("BUG-07"), "strip")
    return tw > 1500 and sw > base and maxlen is None


def bug08(pg, out):
    """Немає текстових повідомлень про помилки"""
    open_add(pg); pg.click("#submit"); pg.wait_for_timeout(400)
    txt = [t for t in pg.inner_text("#userForm").split("\n") if t.strip()]
    borders = pg.evaluate("[...document.querySelectorAll('#userForm input')].map(i => getComputedStyle(i).borderColor)")
    msgs = pg.locator(".invalid-feedback, .text-danger, [role=alert]").count()
    log(f"текст форми після Submit: {txt}; елементів-повідомлень: {msgs}; кольори рамок полів: {set(borders)}; модальне вікно відкрите: {modal_open(pg)}")
    mark(pg, "#userForm", "blue"); shot(pg, out("BUG-08"), "modal")
    return modal_open(pg) and msgs == 0 and txt == ["First Name", "Last Name", "Email", "Age", "Salary", "Department", "Submit"]


def bug09(pg, out):
    """Дублікати записів приймаються"""
    add(pg, first="Twin", last="User", email="twin@example.com"); add(pg, first="Twin", last="User", email="twin@example.com")
    r = rows(pg); log(f"рядки: {r[3:]}")
    mark_row(pg, "Twin", 0); mark_row(pg, "Twin", 1); shot(pg, out("BUG-09"))
    return len(r) == 5 and r[3] == r[4]


def bug10(pg, out):
    """Іконки Edit/Delete недоступні з клавіатури"""
    pg.focus("#searchBox"); seq = []
    for _ in range(40):
        pg.keyboard.press("Tab")
        seq.append(pg.evaluate("document.activeElement.id || document.activeElement.tagName"))
    attrs = pg.evaluate("(() => { const e = document.getElementById('edit-record-1'); return {tag: e.tagName, tabindex: e.getAttribute('tabindex'), role: e.getAttribute('role'), ariaLabel: e.getAttribute('aria-label')} })()")
    log(f"фокус після 40 натискань Tab з поля пошуку: {seq}; атрибути іконки Edit: {attrs}")
    mark(pg, "[id^=edit-record-], [id^=delete-record-]"); shot(pg, out("BUG-10"))
    return not any(s.startswith(("edit-record", "delete-record")) for s in seq) and attrs["tag"] == "SPAN" and attrs["tabindex"] is None


def bug11(pg, out):
    """Підписи полів не повʼязані з полями"""
    open_add(pg); pg.click("#firstName-label"); pg.wait_for_timeout(200)
    act = pg.evaluate("document.activeElement.id || document.activeElement.tagName"); forattr = pg.get_attribute("#firstName-label", "for")
    log(f"після кліку на підпис 'First Name' активний елемент: {act}; атрибут for у label: {forattr}")
    mark(pg, "#firstName-label"); shot(pg, out("BUG-11"), "full")
    return act != "firstName" and forattr is None


def bug12(pg, out):
    """Пошук не обрізає пробіли"""
    pg.fill("#searchBox", "Cierra"); pg.wait_for_timeout(300); a = names(pg)
    mark(pg, "#searchBox", "green"); shot(pg, out("BUG-12-a"))
    pg.fill("#searchBox", "Cierra "); pg.wait_for_timeout(300); b = names(pg)
    pg.fill("#searchBox", " Cierra"); pg.wait_for_timeout(300); c = names(pg)
    log(f"'Cierra' дає {a}; 'Cierra ' (пробіл у кінці) дає {b}; ' Cierra' (пробіл на початку) дає {c}")
    pg.fill("#searchBox", "Cierra "); pg.wait_for_timeout(300)
    mark(pg, "#searchBox"); shot(pg, out("BUG-12-b"))
    return a == ["Cierra"] and b == [] and c == []


def bug13(pg, out):
    """Порожній результат пошуку без повідомлення, Page 1 of 0"""
    pg.fill("#searchBox", "zzz"); pg.wait_for_timeout(300)
    tb = pg.inner_text("table tbody").strip(); pag = pg.inner_text(".pagination").replace("\n", " | ")[:45]
    log(f"текст тіла таблиці: {tb!r}; пагінація: {pag}; 'No rows' на сторінці: {'No rows' in pg.inner_text('body')}")
    mark(pg, ".pagination strong"); mark(pg, "table tbody", "blue"); shot(pg, out("BUG-13"))
    return tb == "" and "Page 1 of 0" in pg.inner_text(".pagination")


def bug14(pg, out):
    """Сторінка не адаптується до 375 px"""
    sw = pg.evaluate("document.documentElement.scrollWidth"); iw = pg.evaluate("window.innerWidth")
    right = pg.evaluate("document.querySelector('table thead th:last-child').getBoundingClientRect().right")
    log(f"ширина вікна: {iw} px; scrollWidth: {sw} px; права межа колонки Action: {round(right)} px")
    mark(pg, "table"); shot(pg, out("BUG-14"), "full")
    return sw > iw and right > iw


def bug15(pg, out):
    """Заголовок форми редагування Registration Form"""
    pg.click("#edit-record-1"); pg.wait_for_selector("#userForm")
    title = pg.inner_text(".modal-title"); log(f"заголовок вікна редагування запису: {title!r}")
    mark(pg, ".modal-title"); shot(pg, out("BUG-15"), "modal")
    return title == "Registration Form"


BUGS = {"BUG-01": (bug01, {}), "BUG-02": (bug02, {}), "BUG-03": (bug03, {}), "BUG-04": (bug04, {}), "BUG-05": (bug05, {}),
        "BUG-06": (bug06, {}), "BUG-07": (bug07, {}), "BUG-08": (bug08, {}), "BUG-09": (bug09, {}), "BUG-10": (bug10, {}),
        "BUG-11": (bug11, {}), "BUG-12": (bug12, {}), "BUG-13": (bug13, {}), "BUG-14": (bug14, {"width": 375, "height": 800}),
        "BUG-15": (bug15, {})}

if __name__ == "__main__":
    want = sys.argv[1:] or list(BUGS)
    with sync_playwright() as p:
        browser = p.chromium.launch()
        for bid in want:
            fn, kw = BUGS[bid]
            LOG.clear(); results = []
            log(f"{bid}: {fn.__doc__ or ''}".strip()); log(f"дата запуску: {datetime.datetime.now():%Y-%m-%d %H:%M:%S}; браузер: Chromium {browser.version}")
            for run in range(1, RUNS + 1):
                log(f"Прогін {run}:")
                pg = open_page(browser, **kw)
                out = (lambda n, r=run: f"screenshots/{n}.png" if r == RUNS else f"evidence/run1/{n}.png")
                try:
                    ok = bool(fn(pg, out))
                except Exception as e:  # збій скрипта не рахуємо як відтворення
                    ok = False; log(f"ПОМИЛКА: {e!r}")
                log(f"прогін {run}: {'ВІДТВОРЕНО' if ok else 'НЕ ВІДТВОРЕНО'}")
                results.append(ok); pg.context.close()
            log(f"ПІДСУМОК {bid}: {sum(results)}/{RUNS} " + ("ВІДТВОРЕНО" if all(results) else "НЕ ПІДТВЕРДЖЕНО"))
            with open(f"evidence/repro-{bid}.txt", "w", encoding="utf-8") as f:
                f.write("\n".join(LOG) + "\n")
            print(bid, sum(results), "/", RUNS)
        browser.close()
