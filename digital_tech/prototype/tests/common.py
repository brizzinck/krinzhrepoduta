"""Спільні помічники для shots.py і checks.py: локальний сервер, перехоплення мережі, кеш плиток OSM."""
import atexit, os, struct, subprocess, sys, time, zlib, urllib.request
from pathlib import Path

PORT = 8099
BASE = f"http://localhost:{PORT}/"
APP = "/p/app"
TILE_DIR = Path("/s/har/tiles")
TG_JS = Path("/s/har/telegram-web-app.js")
DUIKT = {"latitude": 50.4286, "longitude": 30.4765}

# 4 кольори синтетичної плитки: суша, вода, будівля, парк
LAND, WATER, BLDG = (0xf2, 0xef, 0xe9), (0xaa, 0xd3, 0xdf), (0xd9, 0xd0, 0xc9)


def start_server():
    p = subprocess.Popen([sys.executable, "-m", "http.server", str(PORT), "-d", APP, "--bind", "127.0.0.1"],
                         stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL)
    atexit.register(p.terminate)
    for _ in range(50):
        try:
            urllib.request.urlopen(BASE, timeout=1); return
        except Exception:
            time.sleep(0.1)
    raise RuntimeError("server did not start")


def synth_tile() -> bytes:
    """Синтетична плитка 256x256: стовпці 0..84 суша, 85..169 вода, 170..255 будівля (однакова скрізь)."""
    rows = []
    for _ in range(256):
        row = bytearray([0])
        for x in range(256):
            row += bytes(LAND if x < 85 else WATER if x < 170 else BLDG)
        rows.append(bytes(row))
    raw = b"".join(rows)
    def chunk(t, d):
        c = struct.pack(">I", len(d)) + t + d
        return c + struct.pack(">I", zlib.crc32(t + d) & 0xffffffff)
    return b"\x89PNG\r\n\x1a\n" + chunk(b"IHDR", struct.pack(">IIBBBBB", 256, 256, 8, 2, 0, 0, 0)) + chunk(b"IDAT", zlib.compress(raw, 6)) + chunk(b"IEND", b"")


def synth_band(px: int) -> str:
    return "land" if px < 85 else "water" if px < 170 else "building"


class Net:
    """Перехоплювач запитів: сторінка, плитки OSM (кеш на диску), скрипт Telegram; усе інше це порушення."""

    def __init__(self, synth_fallback=False, synth_only=False, no_tiles=False, tg_stub=False):
        self.synth_only = synth_only          # завжди синтетична плитка (перевірка відхилення води й будівель)
        self.no_tiles = no_tiles              # плитки недоступні: запит завершується помилкою
        self.tg_stub = tg_stub                # замість скрипта Telegram віддати порожній файл
        self.synth_fallback = synth_fallback  # немає в кеші: віддати синтетичну плитку без мережі
        self.tile_requests = 0
        self.violations = []
        self.live_fetches = 0
        self.synth = synth_tile()

    def attach(self, context):
        context.route("**/*", self.handle)

    def handle(self, route):
        url = route.request.url
        if url.startswith(BASE):
            return route.continue_()
        if url.startswith("https://tile.openstreetmap.org/"):
            self.tile_requests += 1
            z, x, y = url[len("https://tile.openstreetmap.org/"):-4].split("/")
            f = TILE_DIR / z / x / f"{y}.png"
            if self.no_tiles:
                return route.abort()
            hdr = {"access-control-allow-origin": "*", "cache-control": "max-age=3600", "content-type": "image/png"}
            if f.exists() and not self.synth_only:
                return route.fulfill(status=200, headers=hdr, body=f.read_bytes())
            if self.synth_fallback or self.synth_only:
                return route.fulfill(status=200, headers=hdr, body=self.synth)
            r = route.fetch()
            body = r.body()
            if r.status == 200:
                f.parent.mkdir(parents=True, exist_ok=True); f.write_bytes(body)
                self.live_fetches += 1
            return route.fulfill(status=r.status, headers=hdr, body=body)
        if url.startswith("https://telegram.org/js/telegram-web-app.js"):
            if self.tg_stub:
                return route.fulfill(status=200, headers={"content-type": "application/javascript"}, body="")
            if not TG_JS.exists():
                r = route.fetch(); TG_JS.parent.mkdir(parents=True, exist_ok=True); TG_JS.write_bytes(r.body())
            return route.fulfill(status=200, headers={"content-type": "application/javascript"}, body=TG_JS.read_bytes())
        self.violations.append(url)
        return route.abort()


def new_context(browser, scheme="light", geolocation=DUIKT, permissions=("geolocation",), **kw):
    return browser.new_context(viewport={"width": 390, "height": 844}, device_scale_factor=2, locale="uk-UA",
                               timezone_id="Europe/Kyiv", geolocation=geolocation,
                               permissions=list(permissions), color_scheme=scheme, **kw)
