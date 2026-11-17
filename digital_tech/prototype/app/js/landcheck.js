/**
 * Перевірка точки за кольором плитки OpenStreetMap: вода, будівлі й парки відхиляються,
 * вулиці, тротуари й площі приймаються. Плитки завантажуються з crossOrigin='anonymous'
 * (tile.openstreetmap.org віддає access-control-allow-origin: *), піксельні дані кешуються.
 * Якщо плитка недоступна, точка приймається без перевірки.
 */
import { TILES } from './config.js';
import { offset } from './fleet.js';

const Z = TILES.CHECK_ZOOM;
const cache = new Map(); // "z/x/y" -> Uint8ClampedArray | null (недоступна)

/** Координати точки в пікселях світу на рівні масштабу z. */
export function worldPx(lat, lng, z = Z) {
  const s = 256 * 2 ** z;
  const sin = Math.sin((lat * Math.PI) / 180);
  return { x: ((lng + 180) / 360) * s, y: (0.5 - Math.log((1 + sin) / (1 - sin)) / (4 * Math.PI)) * s };
}

const near = (a, b, tol) => Math.abs(a - b) <= tol;
// Кольори OSM Carto: вода, будівлі, парки, трава, ліс, чагарник, кладовище, сад.
const BAD = [
  [0xaa, 0xd3, 0xdf, 6], [0xd9, 0xd0, 0xc9, 5],
  [0xc8, 0xfa, 0xcc, 6], [0xcd, 0xeb, 0xb0, 6], [0xad, 0xd1, 0x9e, 6],
  [0xc8, 0xd7, 0xab, 5], [0xaa, 0xcb, 0xaf, 5], [0xae, 0xdf, 0xa3, 5],
];

/** Чи піксель належить воді, будівлі чи зеленій зоні. */
export function badPixel(r, g, b) {
  for (const [R, G, B, t] of BAD) if (near(r, R, t) && near(g, G, t) && near(b, B, t)) return true;
  return g - r >= 12 && g - b >= 24; // решта відтінків зелені
}

function loadTile(x, y) {
  return new Promise((resolve) => {
    const img = new Image();
    img.crossOrigin = 'anonymous';
    const timer = setTimeout(() => { img.src = ''; resolve(null); }, TILES.CHECK_TIMEOUT_MS);
    img.onload = () => {
      clearTimeout(timer);
      try {
        const c = document.createElement('canvas');
        c.width = c.height = 256;
        const ctx = c.getContext('2d', { willReadFrequently: true });
        ctx.drawImage(img, 0, 0, 256, 256);
        resolve(ctx.getImageData(0, 0, 256, 256).data);
      } catch { resolve(null); }
    };
    img.onerror = () => { clearTimeout(timer); resolve(null); };
    img.src = TILES.URL.replace('{z}', Z).replace('{x}', x).replace('{y}', y);
  });
}

/**
 * Завантажити плитки, що перекривають круг радіусом R метрів, обмежено TILES.CHECK_MAX_TILES.
 * Помилки не кидаються: недоступні плитки просто не беруть участі в перевірці.
 */
export async function prefetch(center, R) {
  const nw = offset(center, -R, R), se = offset(center, R, -R);
  const a = worldPx(nw.lat, nw.lng), b = worldPx(se.lat, se.lng); // кути охоплюючого квадрата
  const c = worldPx(center.lat, center.lng);
  const tiles = [];
  for (let ty = Math.floor(a.y / 256); ty <= Math.floor(b.y / 256); ty++) {
    for (let tx = Math.floor(a.x / 256); tx <= Math.floor(b.x / 256); tx++) {
      // найближча до центру точка плитки: відкидаємо плитки поза колом
      const nx = Math.max(tx * 256, Math.min(c.x, tx * 256 + 255)), ny = Math.max(ty * 256, Math.min(c.y, ty * 256 + 255));
      const mPerPx = (40075016.686 * Math.cos((center.lat * Math.PI) / 180)) / (256 * 2 ** Z);
      if (Math.hypot(nx - c.x, ny - c.y) * mPerPx <= R) tiles.push({ tx, ty, d: Math.hypot(tx * 256 + 128 - c.x, ty * 256 + 128 - c.y) });
    }
  }
  tiles.sort((p, q) => p.d - q.d);
  const todo = tiles.slice(0, TILES.CHECK_MAX_TILES).filter((t) => !cache.get(`${Z}/${t.tx}/${t.ty}`));
  let i = 0;
  const deadline = Date.now() + TILES.CHECK_BUDGET_MS;
  const worker = async () => {
    while (i < todo.length && Date.now() < deadline) {
      const t = todo[i++];
      cache.set(`${Z}/${t.tx}/${t.ty}`, await loadTile(t.tx, t.ty));
    }
  };
  await Promise.all(Array.from({ length: TILES.CHECK_CONCURRENCY }, worker));
  return { tiles: tiles.length, loaded: todo.filter((t) => cache.get(`${Z}/${t.tx}/${t.ty}`)).length };
}

function pixelAt(gx, gy) {
  const tx = Math.floor(gx / 256), ty = Math.floor(gy / 256);
  const data = cache.get(`${Z}/${tx}/${ty}`);
  if (!data) return null;
  const px = Math.floor(gx - tx * 256), py = Math.floor(gy - ty * 256);
  const o = (py * 256 + px) * 4;
  return [data[o], data[o + 1], data[o + 2]];
}

/**
 * Чи підходить точка для транспорту. Перевіряються сама точка й чотири точки на ±3 пікселі навколо.
 * Невідомий піксель (немає плитки) пропускається.
 */
export function isLand(lat, lng) {
  const { x, y } = worldPx(lat, lng);
  const d = TILES.CHECK_SAMPLE_PX;
  for (const [dx, dy] of [[0, 0], [d, 0], [-d, 0], [0, d], [0, -d]]) {
    const p = pixelAt(x + dx, y + dy);
    if (p && badPixel(p[0], p[1], p[2])) return false;
  }
  return true;
}

/** Чи є хоч одна доступна плитка (для підсумку в журналі). */
export const tilesAvailable = () => [...cache.values()].some(Boolean);
export const clearCache = () => cache.clear();
