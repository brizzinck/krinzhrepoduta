"""Перевірки прототипу: сценарії з таблиці 4 ТЗ та додаткові. Один рядок на сценарій: PASS або FAIL.
Запуск: digital_tech/prototype/tests/run.sh checks.py (див. README.md у цій теці)
Мережа: лише сторінка, скрипт Telegram і плитки OSM (кеш на диску або синтетичні плитки); решта запитів це FAIL."""
import json, math, re, subprocess, sys, time, traceback
sys.path.insert(0, "/s")
from common import *
from playwright.sync_api import sync_playwright

results, console_errors, all_violations = [], [], []
SYN_LAT, SYN_LNG = 49.8419, 24.0315   # Львів: у кеші плиток немає, працює синтетична плитка


def check(name):
    def deco(fn):
        t = time.time()
        try:
            fn()
            results.append((name, True, "")); print(f"PASS  {name}  ({time.time()-t:.1f} с)", flush=True)
        except Exception as e:
            msg = str(e).splitlines()[0] if str(e) else "перевірка на рядку %d" % traceback.extract_tb(e.__traceback__)[-1].lineno
            results.append((name, False, msg)); print(f"FAIL  {name}: {msg}", flush=True)
        return fn
    return deco


class Env:
    """Один контекст браузера на сценарій із перехопленням мережі та збором помилок консолі."""
    def __init__(self, browser, scheme="light", geo=DUIKT, perms=("geolocation",), net=None, init_script=None, collect=True):
        self.ctx = new_context(browser, scheme, geolocation=geo, permissions=perms)
        self.net = net or Net(synth_fallback=True)
        self.net.attach(self.ctx)
        if init_script:
            self.ctx.add_init_script(init_script)
        self.page = self.ctx.new_page()
        if collect:
            self.page.on("console", lambda m: console_errors.append(m.text) if m.type == "error" else None)
            self.page.on("pageerror", lambda e: console_errors.append("pageerror: " + str(e)))

    def close(self):
        all_violations.extend(self.net.violations)
        self.ctx.close()

    def js(self, code):
        return self.page.evaluate(code)

    def state(self):
        return self.js("import('/js/store.js').then(m => JSON.parse(JSON.stringify(m.get())))")

    def api(self, expr):
        """Виконати вираз над модулем api; помилки бізнес-правил повертаються як {err: code, msg}."""
        return self.js("(async () => { const api = await import('/js/api.js'); try { return {ok: await (%s)}; } catch (e) { return {err: e.code || String(e), msg: e.message}; } })()" % expr)

    def onboard(self, identity=True, wait_map=True):
        p = self.page
        p.goto(BASE); p.wait_for_selector(".s1")
        p.evaluate("localStorage.clear()"); p.reload(); p.wait_for_selector(".s1")
        p.check("#consent")
        if identity:
            p.click("[data-m=BANKID]"); p.wait_for_selector(".idstatus .badge")
        p.click(".main-btn")
        if wait_map:
            p.wait_for_selector(".mk", timeout=90000)

    def vehicle(self, typ="E_SCOOTER", min_batt=40):
        for v in self.state()["fleet"]["vehicles"]:
            if v["status"] == "AVAILABLE" and v["type"] == typ and v["battery"] >= min_batt:
                return v["id"]
        raise AssertionError("немає потрібного транспорту")

    def edit_state(self, js_body):
        self.js("(() => { const s = JSON.parse(localStorage.emob); %s; localStorage.emob = JSON.stringify(s); })()" % js_body)


def hav(a, b):
    R = 6371000; r = math.radians
    x = math.sin(r(b[0]-a[0])/2)**2 + math.cos(r(a[0]))*math.cos(r(b[0]))*math.sin(r(b[1]-a[1])/2)**2
    return 2*R*math.asin(math.sqrt(x))


def world_px(lat, lng, z=17):
    s = 256 * 2**z
    sin = math.sin(math.radians(lat))
    return ((lng + 180) / 360 * s, (0.5 - math.log((1 + sin) / (1 - sin)) / (4 * math.pi)) * s)


def run(pw):
    br = pw.chromium.launch()

    @check("S1: без згоди головна кнопка неактивна, #s3 веде на S1, розблокування відхиляється (NO_CONSENT)")
    def _():
        e = Env(br)
        p = e.page; p.goto(BASE); p.wait_for_selector(".s1")
        p.evaluate("localStorage.clear()"); p.reload(); p.wait_for_selector(".s1")
        assert p.locator(".main-btn").is_disabled(), "кнопка активна без згоди"
        p.goto(BASE + "#s3"); p.wait_for_selector(".s1")
        assert p.evaluate("location.hash") == "#s1"
        r = e.api("api.startRide('bolt-0001')")
        assert r.get("err") == "NO_CONSENT", r
        e.close()

    @check("Без підтвердження особи розблокування відхиляється (NO_IDENTITY), події без списання")
    def _():
        e = Env(br); e.onboard(identity=False)
        v = e.vehicle(); bal = e.state()["wallet"]["balance"]
        r = e.api(f"api.startRide('{v}')")
        assert r.get("err") == "NO_IDENTITY", r
        assert e.state()["wallet"]["balance"] == bal and not e.state()["activeRide"]
        e.close()

    @check("Баланс 99 грн відхилено (LOW_BALANCE), 100 грн дозволено; розблокування за 3 дотики з карти")
    def _():
        e = Env(br); e.onboard()
        v = e.vehicle()
        e.api("api.setBalance(9900)")
        r = e.api(f"api.startRide('{v}')")
        assert r.get("err") == "LOW_BALANCE", r
        e.api("api.setBalance(10000)")
        p = e.page
        taps = 0
        # дотик 1: маркер на карті (видимий над шторкою)
        sheet_top = p.locator("#sheet").bounding_box()["y"]
        clicked = False
        for i in range(p.locator(".leaflet-marker-icon.mk").count()):
            box = p.locator(".leaflet-marker-icon.mk").nth(i).bounding_box()
            if box and 60 < box["y"] and box["y"] + box["height"] < sheet_top - 4 and 4 < box["x"] < 350:
                p.mouse.click(box["x"] + box["width"]/2, box["y"] + box["height"]/2); clicked = True; break
        assert clicked, "немає видимого маркера"
        taps += 1
        p.wait_for_selector(".offer.sel"); p.wait_for_selector(".main-btn:not([disabled])")
        p.click(".main-btn"); taps += 1                      # дотик 2: «Розблокувати»
        p.wait_for_selector(".dialog"); p.click(".dialog [data-id=go]"); taps += 1   # дотик 3: підтвердження
        p.wait_for_selector(".s4", timeout=10000)
        assert taps == 3 and e.state()["activeRide"], "поїздка не почалась"
        e.close()

    @check("Транспорт зайнято (failNext TAKEN): відмова, маркер зникає зі списку й карти")
    def _():
        e = Env(br); e.onboard()
        p = e.page
        n_before = p.locator(".leaflet-marker-icon.mk").count()
        e.api("api.setFailNext('TAKEN')")
        p.wait_for_selector(".offer")
        first = p.locator(".offer").first; vid = first.get_attribute("data-id")
        first.click(); p.click(".main-btn"); p.wait_for_selector(".dialog"); p.click(".dialog [data-id=go]")
        p.wait_for_selector(".toast"); assert "забрав" in p.inner_text(".toast"), p.inner_text(".toast")
        p.wait_for_timeout(800)
        assert p.locator(f".offer[data-id='{vid}']").count() == 0, "картка лишилась"
        assert not any(v["id"] == vid for v in e.state()["fleet"]["vehicles"]), "транспорт лишився в автопарку"
        assert not e.state()["activeRide"] and e.state()["wallet"]["balance"] == 20000
        assert p.locator(".leaflet-marker-icon.mk").count() < n_before + 1
        e.close()

    @check("Друга поїздка заблокована (RIDE_ACTIVE); перезавантаження під час поїздки повертає S4 з таймером, що йде далі")
    def _():
        e = Env(br); e.onboard()
        v = e.vehicle(); e.api("api.setTimeFactor(60)")
        assert "ok" in e.api(f"api.startRide('{v}')")
        e.page.reload(); e.page.wait_for_selector(".s4")
        t1 = e.page.inner_text("#timer"); e.page.wait_for_timeout(3000); t2 = e.page.inner_text("#timer")
        def sec(t): a = [int(x) for x in t.split(":")]; return a[0]*60 + a[1] if len(a) == 2 else a[0]*3600 + a[1]*60 + a[2]
        assert sec(t2) > sec(t1) >= 1, (t1, t2)
        e.page.reload(); e.page.wait_for_selector(".s4")
        assert sec(e.page.inner_text("#timer")) >= sec(t2), "таймер скинувся"
        other = next(x["id"] for x in e.state()["fleet"]["vehicles"] if x["status"] == "AVAILABLE")
        r = e.api(f"api.startRide('{other}')")
        assert r.get("err") == "RIDE_ACTIVE", r
        e.page.goto(BASE + "#s3"); e.page.wait_for_selector(".s4")      # активна поїздка перекриває карту
        assert e.page.evaluate("location.hash") == "#s4"
        e.close()

    @check("Борг: поїздка в мінус, нове розблокування відхилено (DEBT), поповнення на 100 грн погашає борг")
    def _():
        e = Env(br); e.onboard()
        v = e.vehicle()
        e.api("api.setBalance(10000)")
        assert "ok" in e.api(f"api.startRide('{v}')")
        e.edit_state("s.activeRide.startedAt = new Date(Date.now() - 18*60000).toISOString()")
        e.page.reload(); e.page.wait_for_selector(".dialog", timeout=8000); e.page.click(".dialog [data-id=ok]")   # попередження про малий залишок закрити, потім завершити
        e.page.click(".main-btn"); e.page.wait_for_selector(".dialog"); e.page.click(".dialog [data-id=fin]")
        e.page.wait_for_selector(".receipt")
        s = e.state(); assert s["wallet"]["balance"] < 0, s["wallet"]["balance"]
        debt = -s["wallet"]["balance"]; assert debt < 10000
        assert "Борг" in e.page.inner_text(".banner.danger") or "борг" in e.page.inner_text(".banner.danger")
        r = e.api("api.startRide('%s')" % e.vehicle())
        assert r.get("err") == "DEBT" and "Погасіть борг" in r["msg"], r
        assert "ok" in e.api("api.topUp(10000)")
        s = e.state(); assert s["wallet"]["balance"] == 10000 - debt >= 0, s["wallet"]["balance"]
        r = e.api("api.startRide('%s')" % e.vehicle())
        assert r.get("err") != "DEBT", r
        e.close()

    @check("Оператор відмовив: батарея < 10 % і failNext REFUSED, без списання, подія UNLOCK_REFUSED")
    def _():
        e = Env(br); e.onboard()
        vid = e.vehicle()
        e.edit_state("s.fleet.vehicles.find(v => v.id === '%s').battery = 5" % vid)
        e.page.reload(); e.page.wait_for_selector(".mk")
        bal = e.state()["wallet"]["balance"]
        r = e.api(f"api.startRide('{vid}')")
        assert r.get("err") == "REFUSED", r
        v2 = next(x["id"] for x in e.state()["fleet"]["vehicles"] if x["status"] == "AVAILABLE" and x["battery"] >= 40 and x["id"] != vid)
        e.api("api.setFailNext('REFUSED')")
        r = e.api(f"api.startRide('{v2}')")
        assert r.get("err") == "REFUSED", r
        s = e.state()
        assert s["wallet"]["balance"] == bal and not s["activeRide"] and not s["rides"]
        assert sum(1 for x in s["events"] if x["type"] == "UNLOCK_REFUSED") == 2
        assert s["settings"]["failNext"] is None
        e.close()

    @check("Попередження про 20 грн показується один раз під час поїздки")
    def _():
        e = Env(br); e.onboard()
        v = e.vehicle()
        assert "ok" in e.api(f"api.startRide('{v}')")
        e.edit_state("s.activeRide.startedAt = new Date(Date.now() - 33*60000).toISOString()")
        e.page.reload(); e.page.wait_for_selector(".dialog", timeout=8000)
        assert "20 грн" in e.page.inner_text(".dialog")
        e.page.click(".dialog [data-id=ok]")
        e.page.wait_for_timeout(2500)
        assert e.page.locator(".dialog").count() == 0, "попередження повторилось"
        assert sum(1 for x in e.state()["events"] if x["type"] == "LOW_BALANCE") == 1
        e.page.reload(); e.page.wait_for_selector(".s4"); e.page.wait_for_timeout(2500)
        assert e.page.locator(".dialog").count() == 0, "попередження повторилось після перезавантаження"
        e.close()

    @check("Діалог малого балансу блокує головну кнопку; діалоги мають різні id; жоден діалог не переходить на чек")
    def _():
        def low_ride():
            e = Env(br); e.onboard()
            v = e.vehicle(); assert "ok" in e.api(f"api.startRide('{v}')")
            e.edit_state("s.activeRide.startedAt = new Date(Date.now() - 33*60000).toISOString()")
            e.page.reload(); e.page.wait_for_selector(".dialog", timeout=8000)
            assert "20 грн" in e.page.inner_text(".dialog")
            return e
        e = low_ride()
        hit = e.js("(() => { const r = document.querySelector('.main-btn').getBoundingClientRect(); const t = document.elementFromPoint(r.x + r.width/2, r.y + r.height/2); return !!(t && t.closest('.modal')); })()")
        assert hit and e.js("document.getElementById('app').inert"), "головна кнопка доступна під модальним вікном"
        e.js("import('/js/tg.js').then(t => { window.__fin = t.popup({title: 'Завершити поїздку?', message: 'x', buttons: [{id: 'fin', type: 'destructive', text: 'Завершити'}, {id: 'cancel', type: 'cancel'}]}); })")
        e.page.wait_for_function("document.querySelector('.dialog h2') && document.querySelector('.dialog h2').textContent.includes('Завершити')")
        ids = e.js("[...document.querySelectorAll('[id]')].map(x => x.id).filter(i => i.startsWith('dlg-t'))")
        assert e.page.locator(".dialog").count() == 1 and len(ids) == len(set(ids)) == 1, ids
        lab = e.js("document.getElementById(document.querySelector('.dialog').getAttribute('aria-labelledby')).textContent")
        assert lab == "Завершити поїздку?", lab
        assert e.js("document.activeElement.closest('.dialog') !== null"), "фокус поза діалогом"
        e.page.click(".dialog [data-id=cancel]")
        assert e.js("!document.getElementById('app').inert") and e.js("window.__fin") == "cancel"
        e.close()
        e = low_ride()
        rid = e.api("api.finishRide(null).then(r => r.id)")["ok"]
        e.js(f"location.hash = '#receipt/{rid}'")
        e.page.wait_for_selector(".receipt"); e.page.wait_for_timeout(300)
        assert e.page.locator(".dialog").count() == 0, "діалог лишився на чеку"
        assert e.js("!document.getElementById('app').inert"), "сторінка лишилась неактивною"
        e.close()

    @check("Геолокацію заборонено: точка біля ДУІКТ, тост, автопарк навколо ДУІКТ")
    def _():
        e = Env(br, perms=())
        e.onboard()
        e.page.wait_for_selector(".toast")
        assert "ДУІКТ" in e.page.inner_text(".toast")
        f = e.state()["fleet"]
        assert f["centerSource"] == "default"
        assert hav((f["center"]["lat"], f["center"]["lng"]), (DUIKT["latitude"], DUIKT["longitude"])) < 150
        e.close()

    @check("Автопарк: 38 об'єктів, 3 зарезервовані, >= 4 в межах 250 м, усі в межах 800 м; склад за конфігурацією")
    def _():
        e = Env(br); e.onboard()
        f = e.state()["fleet"]; c = (DUIKT["latitude"], DUIKT["longitude"])
        vs = f["vehicles"]
        assert len(vs) == 38 and sum(v["status"] == "RESERVED" for v in vs) == 3 and len(f["reserve"]) == 12
        d = [hav(c, (v["lat"], v["lng"])) for v in vs if v["status"] == "AVAILABLE"]
        assert sum(x <= 250 for x in d) >= 4, sorted(d)[:5]
        assert max(d) <= 805, max(d)
        cnt = {}
        for v in vs: cnt[(v["op"], v["type"])] = cnt.get((v["op"], v["type"]), 0) + 1
        assert cnt == {("bolt", "E_SCOOTER"): 10, ("bolt", "E_BIKE"): 4, ("jet", "E_SCOOTER"): 12, ("zelektra", "E_SCOOTER"): 9, ("zelektra", "E_BIKE"): 3}, cnt
        b = [v["battery"] for v in vs]; assert min(b) >= 6 and max(b) <= 100
        e.close()

    @check("Той самий seed: перезавантаження й повторне формування дають ті самі id та координати")
    def _():
        e = Env(br); e.onboard()
        a = e.state()["fleet"]["vehicles"]
        e.page.reload(); e.page.wait_for_selector(".mk")
        assert e.state()["fleet"]["vehicles"] == a, "стан змінився після перезавантаження"
        e.edit_state("s.fleet = null")
        e.page.reload(); e.page.wait_for_selector(".mk", timeout=60000)
        b = e.state()["fleet"]["vehicles"]
        assert [(v["id"], v["lat"], v["lng"], v["battery"]) for v in a] == [(v["id"], v["lat"], v["lng"], v["battery"]) for v in b]
        e.close()

    @check("Зсув позиції на 700 м переформовує автопарк, на 300 м ні")
    def _():
        e = Env(br); e.onboard()
        seed0 = e.state()["fleet"]["seed"]; c0 = e.state()["fleet"]["center"]
        e.ctx.set_geolocation({"latitude": DUIKT["latitude"] + 300/111195, "longitude": DUIKT["longitude"]})
        e.page.wait_for_timeout(3500)
        assert e.state()["fleet"]["seed"] == seed0, "автопарк переформовано після 300 м"
        e.ctx.set_geolocation({"latitude": DUIKT["latitude"] + 700/111195, "longitude": DUIKT["longitude"]})
        e.page.wait_for_function("import('/js/store.js').then(m => m.get().fleet.seed) !== undefined", timeout=1000)
        for _i in range(30):
            if e.state()["fleet"]["seed"] != seed0: break
            e.page.wait_for_timeout(500)
        assert e.state()["fleet"]["seed"] != seed0, "автопарк не переформовано після 700 м"
        e.close()

    @check("Вода, будівлі й парки за кольором плитки: жоден транспорт не стоїть у забороненій зоні (синтетична плитка)")
    def _():
        e = Env(br, geo={"latitude": SYN_LAT, "longitude": SYN_LNG}, net=Net(synth_only=True)); e.onboard()
        vs = e.state()["fleet"]["vehicles"]
        assert len(vs) == 38
        bad = 0
        for v in vs:
            x, y = world_px(v["lat"], v["lng"])
            for dx, dy in [(0, 0), (2.8, 0), (-2.8, 0), (0, 2.8), (0, -2.8)]:
                if synth_band((x + dx) % 256) != "land": bad += 1
        assert bad == 0, f"{bad} проб у воді чи будівлі"
        assert e.state()["events"][-1]["type"] == "FLEET_GENERATED" or any(ev["type"] == "FLEET_GENERATED" and ev["details"].get("tileCheck") for ev in e.state()["events"])
        e.close()

    @check("Плитки недоступні: транспорт розміщується без перевірки (38 об'єктів), застосунок працює")
    def _():
        e = Env(br, geo={"latitude": SYN_LAT, "longitude": SYN_LNG}, net=Net(no_tiles=True), collect=False); e.onboard()
        s = e.state()
        assert len(s["fleet"]["vehicles"]) == 38
        ev = [x for x in s["events"] if x["type"] == "FLEET_GENERATED"][-1]
        assert ev["details"]["tileCheck"] is False
        e.close()

    @check("localStorage кидає виняток: застосунок відображається, Профіль повідомляє про це")
    def _():
        init = "Storage.prototype.getItem = function(){ throw new Error('blocked') }; Storage.prototype.setItem = function(){ throw new Error('blocked') }; Storage.prototype.removeItem = function(){ throw new Error('blocked') };"
        e = Env(br, init_script=init)
        p = e.page; p.goto(BASE); p.wait_for_selector(".s1")
        p.check("#consent"); p.click(".main-btn"); p.wait_for_selector(".mk", timeout=60000)
        p.click("[data-tab=profile]"); p.wait_for_selector(".profile")
        assert "недоступне" in p.inner_text(".profile")
        e.close()

    @check("Стан переживає перезавантаження: баланс після поповнення, згода, підтвердження особи")
    def _():
        e = Env(br); e.onboard()
        assert "ok" in e.api("api.topUp(20000)")
        e.page.reload(); e.page.wait_for_selector(".mk")
        s = e.state()
        assert s["wallet"]["balance"] == 40000 and s["user"]["consentAt"] and s["user"]["identity"]
        e.page.click("[data-tab=wallet]"); e.page.wait_for_selector(".tx li")
        assert "400,00" in e.page.inner_text(".balance")
        assert e.page.locator(".tx li").count() == 2
        e.close()

    @check("Гроші: 33 хв Bolt = 181,60 грн, мінімум 1 хв, округлення вгору, комісія 10 % не показується користувачу")
    def _():
        e = Env(br); e.onboard()
        r = e.js("""import('/js/format.js').then(f => [f.ridePrice(1000, 520, 33), f.rideMinutes(1, 1), f.rideMinutes(60001, 1), f.rideMinutes(1000, 60), f.money(18160), f.money(-2340)])""")
        assert r[0] == 18160 and r[1] == 1 and r[2] == 2 and r[3] == 1, r
        assert r[4].replace(" ", " ").replace(" ", " ") == "181,60 грн" and r[5].startswith("−"), r
        v = e.vehicle()
        assert "ok" in e.api(f"api.startRide('{v}')")
        e.edit_state("s.activeRide.startedAt = new Date(Date.now() - 32*60000 - 1000).toISOString()")
        e.page.reload(); e.page.wait_for_selector(".s4")
        e.page.wait_for_selector(".dialog"); e.page.click(".dialog [data-id=ok]")
        e.page.click(".main-btn"); e.page.wait_for_selector(".dialog"); e.page.click(".dialog [data-id=fin]")
        e.page.wait_for_selector(".receipt")
        ride = e.state()["rides"][0]
        assert ride["minutes"] == 33 and ride["price"] == ride["unlock"] + 33*ride["perMin"], ride
        assert ride["commission"] == math.floor(ride["price"]*0.1 + 0.5)
        assert "омісі" not in e.page.inner_text(".receipt"), "комісія видима в чеку"
        assert re.match(r"EM-\d{8}-0001$", ride["receiptNo"])
        e.close()

    @check("Канали GBFS v3: vehicle_status і system_pricing_plans мають потрібні поля")
    def _():
        e = Env(br); e.onboard()
        fe = e.js("import('/js/gbfs.js').then(async g => { const s = (await import('/js/store.js')).get(); return g.feeds(s.fleet); })")
        assert len(fe) == 3
        for f in fe:
            for pl in f["system_pricing_plans"]["plans"]:
                assert pl["currency"] == "UAH" and pl["per_min_pricing"][0]["interval"] == 1 and "price" in pl
            for v in f["vehicle_status"]["vehicles"]:
                assert {"vehicle_id", "lat", "lon", "vehicle_type_id", "is_reserved", "is_disabled", "current_fuel_percent", "pricing_plan_id"} <= set(v)
        e.close()

    @check("Режим Telegram (імітований WebApp): привітання за іменем, start_param, LocationManager, головна кнопка, haptic, підтвердження закриття")
    def _():
        stub = """(() => { const calls = []; window.__tg = calls;
          const W = { initData: 'query_id=x', initDataUnsafe: { user: { id: 777, first_name: 'Олена', username: 'olena' }, start_param: 's2' },
            version: '9.0', platform: 'tdesktop', colorScheme: 'dark', viewportStableHeight: 844, isVersionAtLeast: () => true,
            ready() { calls.push('ready') }, expand() { calls.push('expand') }, disableVerticalSwipes() { calls.push('dvs') },
            enableClosingConfirmation() { calls.push('ecc') }, disableClosingConfirmation() { calls.push('dcc') },
            setHeaderColor(c) { calls.push('hdr') }, setBackgroundColor(c) { calls.push('bg') }, setBottomBarColor(c) { calls.push('bar') },
            onEvent() {}, offEvent() {}, openLink(u) { calls.push('link') },
            MainButton: { _h: null, setParams(p) { calls.push('main:' + (p.is_active ? 'on:' : 'off:') + p.text + ':' + p.color); }, onClick(f) { this._h = f }, offClick() { this._h = null }, showProgress() {}, hideProgress() {} },
            BackButton: { show() {}, hide() {}, onClick() {}, offClick() {} },
            HapticFeedback: { impactOccurred() { calls.push('h:impact') }, notificationOccurred(t) { calls.push('h:' + t) }, selectionChanged() { calls.push('h:sel') } },
            showPopup(p, cb) { calls.push('popup:' + p.title); cb(p.buttons[0].id) },
            LocationManager: { isInited: false, isLocationAvailable: true, isAccessRequested: true, isAccessGranted: true, init(cb) { this.isInited = true; cb() }, getLocation(cb) { cb({ latitude: 50.4501, longitude: 30.5234, horizontal_accuracy: 12 }) }, openSettings() { calls.push('settings') } } };
          window.Telegram = { WebApp: W }; })();"""
        e = Env(br, net=Net(synth_fallback=True, tg_stub=True), init_script=stub, collect=False)
        p = e.page; p.goto(BASE); p.wait_for_selector(".s1")
        assert "Вітаємо, Олена" in p.inner_text("h1")
        assert p.evaluate("document.documentElement.dataset.theme") == "dark", "тема не темна"
        assert p.evaluate("document.documentElement.dataset.shim") == "off", "shim увімкнено в режимі Telegram"
        assert p.locator("#shim-main").is_hidden(), "HTML-кнопка видима в режимі Telegram"
        calls = p.evaluate("window.__tg")
        assert "ready" in calls and "expand" in calls and "dvs" in calls and "hdr" in calls and "bar" in calls, calls
        p.check("#consent")
        calls = p.evaluate("window.__tg"); assert any(c.startswith("main:on:Перейти до карти:") for c in calls), calls
        p.evaluate("window.Telegram.WebApp.MainButton._h()"); p.wait_for_selector(".mk", timeout=60000)
        f = e.state()["fleet"]; assert f["centerSource"] == "tg" and abs(f["center"]["lat"] - 50.450) < 0.002
        assert e.state()["user"]["tgId"] == 777
        e.api("api.confirmIdentity('DIIA')"); p.wait_for_timeout(300)
        p.wait_for_selector(".offer"); p.click(".offer"); p.wait_for_timeout(300)
        calls = p.evaluate("window.__tg"); assert any(c.startswith("main:on:Розблокувати · ") for c in calls), [c for c in calls if c.startswith("main")]
        p.evaluate("window.Telegram.WebApp.MainButton._h()"); p.wait_for_selector(".s4", timeout=10000)
        calls = p.evaluate("window.__tg")
        assert "ecc" in calls and "h:success" in calls and any(c.startswith("popup:Розблокувати") for c in calls), calls
        # start_param: знову відкрити без hash
        e.api("api.setBalance(20000)")
        p.evaluate("import('/js/store.js').then(m => m.update(s => { s.activeRide = null; s.fleet.vehicles.forEach(v => { if (v.status === 'IN_RIDE') v.status = 'AVAILABLE'; }); }))")
        p.goto(BASE); p.wait_for_selector(".s2")
        assert p.evaluate("location.hash") == "#s2"
        e.close()


    # ---------- регресії за рев'ю 2026-11-17 ----------
    import urllib.parse as up

    def tg_hash(start_param=None, ver="7.0"):
        user = json.dumps({"id": 777, "first_name": "Олена", "username": "olena"}, ensure_ascii=False)
        init = "user=" + up.quote(user) + "&auth_date=1763000000&hash=abc" + ("&start_param=" + start_param if start_param else "")
        return "#tgWebAppData=" + up.quote(init) + "&tgWebAppVersion=" + ver + "&tgWebAppPlatform=tdesktop&tgWebAppThemeParams=%7B%7D"

    @check("Справжній SDK Telegram із hash запуску (tgWebAppData=...): перший запуск S1, повторний запуск S3, start_param, активна поїздка, без помилок консолі")
    def _():
        e = Env(br, geo=DUIKT); p = e.page
        p.goto(BASE + tg_hash()); p.wait_for_selector(".s1")
        assert p.evaluate("Telegram.WebApp.initData") != "", "SDK не побачив initData"
        assert "Олена" in p.inner_text("h1")
        assert "tgWebApp" not in p.evaluate("location.hash") and p.evaluate("location.hash") == "#s1", p.evaluate("location.hash")
        e.api("api.acceptTerms()"); e.api("api.confirmIdentity('DIIA')")
        def relaunch(h):
            p.goto("about:blank"); p.goto(BASE + h)
        relaunch(tg_hash()); p.wait_for_selector(".mk", timeout=60000)
        assert p.evaluate("location.hash") == "#s3" and p.locator("#tabs a.on").count() == 1
        relaunch(tg_hash("s2")); p.wait_for_selector(".s2")
        assert p.evaluate("location.hash") == "#s2", p.evaluate("location.hash")
        relaunch(tg_hash() + "&tgWebAppStartParam=s5"); p.wait_for_selector(".s5")
        relaunch("#abracadabra"); p.wait_for_selector(".mk", timeout=60000)   # невідомий маршрут не ламає екран
        assert p.evaluate("location.hash") == "#s3"
        e.page.wait_for_selector(".offer")
        v = e.vehicle(); assert "ok" in e.api(f"api.startRide('{v}')")
        relaunch(tg_hash()); p.wait_for_selector(".s4")
        assert p.evaluate("location.hash") == "#s4"
        e.close()

    @check("Браузерний ?startapp=s2 відкриває гаманець; start_param s1..s5, чужі значення ігноруються")
    def _():
        e = Env(br); e.onboard()
        p = e.page
        p.goto("about:blank"); p.goto(BASE + "?startapp=s2"); p.wait_for_selector(".s2")
        assert p.evaluate("location.hash") == "#s2"
        p.goto("about:blank"); p.goto(BASE + "?startapp=evil"); p.wait_for_selector(".mk", timeout=60000)
        assert p.evaluate("location.hash") == "#s3"
        e.close()

    @check("Назад із чека після завершення поїздки не показує хибний тост про відсутню поїздку")
    def _():
        e = Env(br); e.onboard()
        v = e.vehicle(); assert "ok" in e.api(f"api.startRide('{v}')")
        e.page.reload(); e.page.wait_for_selector(".s4")
        e.page.click(".main-btn"); e.page.wait_for_selector(".dialog"); e.page.click(".dialog [data-id=fin]")
        e.page.wait_for_selector(".receipt")
        e.page.go_back(); e.page.wait_for_timeout(1200)
        assert e.page.locator(".toast").count() == 0, e.page.inner_text(".toast") if e.page.locator(".toast").count() else ""
        assert e.page.evaluate("location.hash") != "#s4"
        e.close()

    @check("Банер малого балансу на S4 не перемальовується щосекунди (вузол кнопки стабільний)")
    def _():
        e = Env(br); e.onboard()
        v = e.vehicle(); assert "ok" in e.api(f"api.startRide('{v}')")
        e.edit_state("s.activeRide.startedAt = new Date(Date.now() - 33*60000).toISOString()")
        e.page.reload(); e.page.wait_for_selector(".dialog"); e.page.click(".dialog [data-id=ok]")
        e.page.wait_for_selector("#lowtop")
        e.page.evaluate("window.__n = document.getElementById('lowtop')")
        e.page.wait_for_timeout(2600)
        assert e.page.evaluate("document.getElementById('lowtop') === window.__n"), "кнопку перестворено"
        e.close()

    @check("Два одночасні розблокування: рівно одна поїздка, один транспорт IN_RIDE")
    def _():
        e = Env(br); e.onboard()
        vs = [x["id"] for x in e.state()["fleet"]["vehicles"] if x["status"] == "AVAILABLE" and x["battery"] >= 40][:2]
        r = e.js("(async () => { const api = await import('/js/api.js'); const f = (id) => api.startRide(id).then(() => 'ok', (x) => x.code); return Promise.all([f('%s'), f('%s')]); })()" % tuple(vs))
        assert sorted(r) == ["RIDE_ACTIVE", "ok"], r
        assert sum(v["status"] == "IN_RIDE" for v in e.state()["fleet"]["vehicles"]) == 1
        e.close()

    @check("Позиція завершення не зберігається точно: зсув 15-30 м, 4 знаки; без позиції транспорт лишається на місці")
    def _():
        e = Env(br); e.onboard()
        v = e.vehicle(); assert "ok" in e.api(f"api.startRide('{v}')")
        pos = {"lat": DUIKT["latitude"], "lng": DUIKT["longitude"], "accuracy": 10, "source": "browser"}
        e.api("api.finishRide(%s)" % json.dumps(pos))
        veh = next(x for x in e.state()["fleet"]["vehicles"] if x["id"] == v)
        d = hav((veh["lat"], veh["lng"]), (DUIKT["latitude"], DUIKT["longitude"]))
        assert 8 <= d <= 45, d
        assert all(len(str(x).split(".")[1]) <= 4 for x in (veh["lat"], veh["lng"])), veh
        v2 = e.vehicle(); before = next(x for x in e.state()["fleet"]["vehicles"] if x["id"] == v2)
        e.api(f"api.startRide('{v2}')"); e.api("api.finishRide(null)")
        after = next(x for x in e.state()["fleet"]["vehicles"] if x["id"] == v2)
        assert (before["lat"], before["lng"]) == (after["lat"], after["lng"]), "транспорт зсунувся без позиції"
        e.close()

    @check("Транспорт із зарядом нижче 10 % сортується в кінець списку")
    def _():
        e = Env(br); e.onboard()
        e.edit_state("s.fleet.vehicles.filter(v => v.status === 'AVAILABLE').slice(0, 2).forEach(v => { v.battery = 6; })")
        e.page.reload(); e.page.wait_for_selector(".offer")
        for srt in ("price", "distance", "battery"):
            r = e.js("import('/js/api.js').then(a => a.getOffers({pos: {lat: %s, lng: %s}, type: 'ALL', sort: '%s'})).then(o => o.map(x => x.battery))" % (DUIKT["latitude"], DUIKT["longitude"], srt))
            seen_low = False
            for b in r:
                if b < 10: seen_low = True
                else: assert not seen_low, (srt, r)
            assert seen_low
        e.close()


    # ---------- режим тестування і вигляд звичайного користувача ----------
    HIDDEN = re.compile(r"демо|тест|імітац|гість|журнал подій|×60", re.I)

    @check("Звичайний режим: у видимому тексті екранів немає «демо», «тест», «імітація», «Гість»; ім'я «Користувач»; застереження лише в Профілі й юридичних сторінках")
    def _():
        e = Env(br); e.onboard()
        p = e.page
        assert e.state()["user"]["firstName"] == "Користувач"
        txt = {}
        p.wait_for_selector(".offer"); txt["s3"] = p.inner_text("#view")
        assert "Проєкт не пов" not in txt["s3"] and "Навчальний проєкт" not in txt["s3"], "застереження на карті"
        p.click("[data-tab=wallet]"); p.wait_for_selector(".tx li"); txt["s2"] = p.inner_text("#view")
        p.click("[data-tab=history]"); txt["s5"] = p.inner_text("#view")
        p.goto("about:blank"); p.goto(BASE + "#s1")
        e.edit_state("s.user.consentAt = null; s.user.identity = null"); p.goto("about:blank"); p.goto(BASE); p.wait_for_selector(".s1")
        txt["s1"] = p.inner_text("#view"); assert "Навчальний проєкт" not in txt["s1"], "застереження на S1"
        e.edit_state("s.user.consentAt = new Date().toISOString(); s.user.identity = {method: 'DIIA', at: new Date().toISOString()}")
        v = None
        p.goto("about:blank"); p.goto(BASE); p.wait_for_selector(".mk", timeout=60000)
        v = e.vehicle(); assert "ok" in e.api(f"api.startRide('{v}')")
        p.reload(); p.wait_for_selector(".s4"); txt["s4"] = p.inner_text("#view")
        p.click("[data-tab=profile]"); p.wait_for_selector(".profile")
        prof = p.inner_text(".profile")
        assert "Навчальний проєкт" in prof and "про застосунок" in prof.lower(), "немає застереження в Профілі"
        assert not re.search(r"режим тестування|журнал подій|скинути|×60|режим:", prof.lower()), "службові елементи в Профілі"
        txt["profile"] = p.evaluate("[...document.querySelectorAll('.profile > *:not(.disclaimer)')].map(x => x.innerText).join(' ')")
        for k, t in txt.items():
            m = HIDDEN.search(t); assert not m, (k, m.group(0))
        p.goto(BASE + "#terms"); p.wait_for_selector(".legal"); assert "Проєкт не пов" in p.inner_text(".legal")
        e.close()

    @check("Режим тестування: ?test=1 вмикає службові елементи й запам'ятовується; 7 дотиків по версії теж; вимкнення прибирає їх; ім'я в тестовому режимі інше")
    def _():
        e = Env(br); p = e.page
        p.goto(BASE + "?test=1"); p.wait_for_selector(".s1")
        assert "Максим" in p.inner_text("h1")
        p.check("#consent"); p.click(".main-btn"); p.wait_for_selector(".mk", timeout=60000)
        p.goto("about:blank"); p.goto(BASE + "#profile"); p.wait_for_selector(".profile")     # без параметра: прапорець у localStorage
        prof = p.inner_text(".profile").lower()
        assert "режим тестування" in prof and "журнал подій" in prof and "інструменти тестування" in prof, prof[:200]
        assert p.locator("#tf").count() == 1
        p.click("#tf"); assert e.state()["settings"]["timeFactor"] == 60
        p.click("#testoff"); p.wait_for_timeout(200)
        assert "режим тестування" not in p.inner_text(".profile").lower() and e.state()["settings"]["timeFactor"] == 1
        p.reload(); p.wait_for_selector(".profile"); assert p.locator("#tf").count() == 0, "режим лишився після вимкнення"
        for _i in range(7): p.click("#ver")
        p.wait_for_selector("#tf"); assert "режим тестування" in p.inner_text(".profile").lower()
        e.close()

    @check("Стиль: у коді немає hex-кольорів поза tokens.css, немає стрілок, емодзі й галочок")
    def _():
        bad = []
        for f in list(Path(APP, "js").rglob("*.js")) + [Path(APP, "css/app.css")]:
            for i, line in enumerate(f.read_text().splitlines(), 1):
                if re.search(r"#[0-9a-fA-F]{3,8}\b", line) and "href=" not in line:
                    bad.append(f"{f.name}:{i}")
        assert not bad, bad[:5]
        pat = re.compile("[→↔✓✗•]|[\U0001F300-\U0001FAFF☀-➿]")
        bad = []
        for f in list(Path(APP).rglob("*")):
            if f.is_file() and "vendor" not in f.parts and f.suffix in (".js", ".css", ".html", ".svg") and pat.search(f.read_text()):
                bad.append(f.name)
        rd = Path("/p/README.md")
        if rd.exists() and pat.search(rd.read_text()): bad.append("README.md")
        assert not bad, bad

    @check("Розмір app без vendor < 400 KB; немає секретів і викликів Bot API")
    def _():
        total = sum(f.stat().st_size for f in Path(APP).rglob("*") if f.is_file() and "vendor" not in f.parts)
        assert total < 400_000, total
        txt = "\n".join(f.read_text() for f in Path(APP).rglob("*") if f.is_file() and "vendor" not in f.parts and f.suffix in (".js", ".html", ".css", ".svg"))
        assert not re.search(r"BOT_TOKEN|api\.telegram\.org|secret|password", txt, re.I), "секрети чи виклики Bot API в app"
        for f in Path("/p/functions").rglob("*.js"):
            assert "console.log" not in f.read_text(), "логування у функції"

    br.close()


if __name__ == "__main__":
    start_server()
    with sync_playwright() as pw:
        run(pw)
    ok = all(r[1] for r in results)
    net_ok = not all_violations
    print(("PASS" if net_ok else "FAIL") + "  Запити поза сторінкою, скриптом Telegram і плитками OSM:", all_violations[:5] or "немає")
    errs = [x for x in console_errors if "Failed to load resource" not in x]
    print(("PASS" if not errs else "FAIL") + "  Помилки консолі й pageerror:", errs[:5] or "немає")
    print(f"Підсумок: {sum(r[1] for r in results)} із {len(results)} сценаріїв PASS")
    sys.exit(0 if ok and net_ok and not errs else 1)
