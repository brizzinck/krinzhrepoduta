"""Допоміжні функції для відтворення дефектів на https://demoqa.com/webtables (Playwright, Chromium, headless)."""
from playwright.sync_api import sync_playwright

URL = "https://demoqa.com/webtables"
FIELDS = ["firstName", "lastName", "userEmail", "age", "salary", "department"]


def open_page(browser, width=1280, height=900):
    ctx = browser.new_context(viewport={"width": width, "height": height})
    # рекламні та аналітичні запити не потрібні для перевірки таблиці
    ctx.route("**/*", lambda r: r.abort() if any(h in r.request.url for h in
              ("criteo", "doubleclick", "googlesyndication", "adsystem", "google-analytics", "googletagmanager", "adservice", "amazon-adsystem", "pubmatic", "rubiconproject")) else r.continue_())
    pg = ctx.new_page()
    pg.goto(URL, wait_until="domcontentloaded", timeout=60000)
    pg.wait_for_selector("text=Cierra")
    # прибираємо фіксований футер і банери, що можуть перекривати елементи (лише оформлення)
    pg.add_style_tag(content="#fixedban, footer {display:none !important}")
    return pg


def rows(pg):
    """Вміст рядків таблиці (без колонки Action)."""
    out = []
    for tr in pg.locator("table tbody tr").all():
        cells = [c.inner_text().strip() for c in tr.locator("td").all()]
        out.append(cells[:6])
    return out


def open_add(pg):
    pg.click("#addNewRecordButton")
    pg.wait_for_selector("#userForm")


def fill_form(pg, **vals):
    for k, v in vals.items():
        pg.fill("#" + k, v)


def add(pg, first="Test", last="User", email="test@example.com", age="30", salary="5000", dept="QA", submit=True):
    open_add(pg)
    fill_form(pg, firstName=first, lastName=last, userEmail=email, age=age, salary=salary, department=dept)
    if submit:
        pg.click("#submit")
        pg.wait_for_timeout(300)


def modal_open(pg):
    return pg.locator("#userForm").count() > 0


def highlight(pg, selector, color="red"):
    pg.evaluate("""([s, c]) => document.querySelectorAll(s).forEach(e => { e.style.outline = '3px solid ' + c; e.style.outlineOffset = '2px'; })""", [selector, color])
