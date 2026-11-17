/**
 * Автопарк імітованих операторів навколо користувача. Чисті функції, без мережі.
 * Позиції детерміновані: той самий користувач, день і район дають той самий автопарк.
 */
import { OPERATORS, CFG } from './config.js';

/** Хеш FNV-1a (32 біти) від рядка. */
export function fnv1a(str) {
  let h = 0x811c9dc5;
  for (let i = 0; i < str.length; i++) {
    h ^= str.charCodeAt(i);
    h = Math.imul(h, 0x01000193) >>> 0;
  }
  return h >>> 0;
}

/** Генератор псевдовипадкових чисел mulberry32. */
export function mulberry32(a) {
  return function () {
    a |= 0; a = (a + 0x6D2B79F5) | 0;
    let t = Math.imul(a ^ (a >>> 15), 1 | a);
    t = (t + Math.imul(t ^ (t >>> 7), 61 | t)) ^ t;
    return ((t ^ (t >>> 14)) >>> 0) / 4294967296;
  };
}

const R_EARTH = 6371000;
const rad = (d) => (d * Math.PI) / 180;

/** Відстань між двома точками в метрах (гаверсинус, R = 6371 км). */
export function haversine(a, b) {
  const dLat = rad(b.lat - a.lat), dLng = rad(b.lng - a.lng);
  const x = Math.sin(dLat / 2) ** 2 + Math.cos(rad(a.lat)) * Math.cos(rad(b.lat)) * Math.sin(dLng / 2) ** 2;
  return 2 * R_EARTH * Math.asin(Math.min(1, Math.sqrt(x)));
}

/** Зсув точки на dx (схід) і dy (північ) метрів. */
export function offset(p, dx, dy) {
  return {
    lat: p.lat + (dy / R_EARTH) * (180 / Math.PI),
    lng: p.lng + (dx / (R_EARTH * Math.cos(rad(p.lat)))) * (180 / Math.PI),
  };
}

const round = (v, d) => Math.round(v * 10 ** d) / 10 ** d;

/** Комірка сітки 0,005° для зерна. */
export const cellOf = (c) => `${Math.round(c.lat / CFG.CELL_DEG)}:${Math.round(c.lng / CFG.CELL_DEG)}`;

/** Зерно: Telegram id, дата за Києвом і комірка центру. */
export const seedFor = ({ tgId, date, center }) => fnv1a(`${tgId}|${date}|${cellOf(center)}`);

/** Чи потрібно переформувати автопарк (дата змінилась або користувач далі за 600 м від центру). */
export function needsRegen(fleet, pos, date) {
  if (!fleet || !fleet.vehicles || !fleet.vehicles.length) return true;
  if (fleet.date !== date) return true;
  return haversine(pos, fleet.center) > CFG.REGEN_M;
}

/** Рівномірна точка в крузі радіусом R навколо центру. */
function drawPoint(rng, center, R) {
  const r = R * Math.sqrt(rng());
  const th = rng() * 2 * Math.PI;
  return offset(center, r * Math.cos(th), r * Math.sin(th));
}

/**
 * Сформувати автопарк.
 * @param {{center:{lat:number,lng:number}, tgId:number, date:string, centerSource:string, isLand?:(lat:number,lng:number)=>boolean}} o
 *   isLand: перевірка, що точка не у воді, не в будівлі й не в парку (за плитками); повертає true, якщо плитки недоступні.
 */
export function generate({ center, tgId, date, centerSource, isLand = () => true, tries = 24 }) {
  const seed = seedFor({ tgId, date, center });
  const rng = mulberry32(seed);
  const total = CFG.FLEET_TOTAL, reserveN = CFG.RESERVE_POINTS;
  const pts = [];
  for (let i = 0; i < total + reserveN; i++) {
    const R = i < CFG.NEAR_COUNT ? CFG.NEAR_M : CFG.RADIUS_M;
    let chosen = null, last = null;
    for (let t = 0; t < tries; t++) {
      const p = drawPoint(rng, center, R);
      last = p;
      if (t < 5 && pts.some((q) => haversine(p, q) < CFG.MIN_SPACING_M)) continue;
      if (t < 8 && haversine(p, center) < 40) continue; // не накладати транспорт на точку користувача
      if (!isLand(p.lat, p.lng)) continue;
      chosen = p; break;
    }
    pts.push(chosen || last);
  }
  // оператор і тип за кількостями з конфігурації, порядок перемішано
  const slots = [];
  for (const op of OPERATORS) for (const [type, t] of Object.entries(op.types)) for (let k = 0; k < t.count; k++) slots.push({ op: op.code, type });
  for (let i = slots.length - 1; i > 0; i--) { const j = Math.floor(rng() * (i + 1)); [slots[i], slots[j]] = [slots[j], slots[i]]; }
  const reservedIdx = new Set();
  while (reservedIdx.size < CFG.FLEET_RESERVED) reservedIdx.add(CFG.NEAR_COUNT + Math.floor(rng() * (total - CFG.NEAR_COUNT)));
  const counters = {};
  const vehicles = slots.slice(0, total).map((s, i) => {
    const roll = rng();
    const battery = roll < 0.70 ? 40 + Math.floor(rng() * 61) : roll < 0.92 ? 20 + Math.floor(rng() * 20) : 6 + Math.floor(rng() * 14);
    counters[s.op] = (counters[s.op] || 0) + 1;
    return {
      id: `${s.op}-${String(counters[s.op]).padStart(4, '0')}`,
      op: s.op, type: s.type,
      lat: round(pts[i].lat, 6), lng: round(pts[i].lng, 6),
      battery,
      status: reservedIdx.has(i) ? 'RESERVED' : 'AVAILABLE',
    };
  });
  const reserve = pts.slice(total).map((p) => ({ lat: round(p.lat, 6), lng: round(p.lng, 6) }));
  return { seed, center: { lat: round(center.lat, 3), lng: round(center.lng, 3) }, centerSource, date, vehicles, reserve };
}

/**
 * Рух після завершення поїздки: транспорт стає на місце користувача й втрачає заряд,
 * два інших транспорти «їдуть» до резервних точок. Змінює fleet.
 */
export function afterRide(fleet, { vehicleId, minutes, pos }) {
  const rng = mulberry32(fnv1a(`${fleet.seed}|${vehicleId}|${fleet.reserve.length}`));
  const v = fleet.vehicles.find((x) => x.id === vehicleId);
  if (v) {
    v.status = 'AVAILABLE';
    if (pos) {
      // точне місце завершення не зберігається: зсув на 15-30 м і округлення до 4 знаків
      const d = 15 + rng() * 15, th = rng() * 2 * Math.PI;
      const p = offset(pos, d * Math.cos(th), d * Math.sin(th));
      v.lat = round(p.lat, 4); v.lng = round(p.lng, 4);
    }
    v.battery = Math.max(1, Math.round(v.battery - Math.max(6, minutes * 0.4)));
  }
  for (let k = 0; k < 2 && fleet.reserve.length; k++) {
    const pool = fleet.vehicles.filter((x) => x.status === 'AVAILABLE' && x.id !== vehicleId);
    if (!pool.length) break;
    const mv = pool[Math.floor(rng() * pool.length)];
    const rp = fleet.reserve.shift();
    mv.lat = rp.lat; mv.lng = rp.lng;
  }
}
