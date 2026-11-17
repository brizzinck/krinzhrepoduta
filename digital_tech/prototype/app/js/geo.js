/**
 * Позиція користувача: Telegram LocationManager (Bot API 8.0), далі геолокація браузера,
 * далі точка біля ДУІКТ. Позиція нікуди не зберігається.
 */
import * as tg from './tg.js';
import { CFG } from './config.js';
import { haversine } from './fleet.js';

let last = null;
const listeners = new Set();
let watchId = null, pollTimer = null;

const fallback = () => ({ lat: CFG.DEFAULT_POS.lat, lng: CFG.DEFAULT_POS.lng, accuracy: 0, source: 'default' });

function browserPos() {
  return new Promise((resolve) => {
    if (!('geolocation' in navigator)) return resolve(null);
    navigator.geolocation.getCurrentPosition(
      (p) => resolve({ lat: p.coords.latitude, lng: p.coords.longitude, accuracy: p.coords.accuracy || 0, source: 'browser' }),
      () => resolve(null),
      { timeout: 8000, maximumAge: 60000, enableHighAccuracy: false },
    );
  });
}

/** Отримати позицію за ланцюжком Telegram, браузер, ДУІКТ. */
export async function getPosition() {
  let pos = null, denied = false;
  if (tg.geoTg.supported()) {
    await tg.geoTg.init();
    const st = tg.geoTg.state();
    if (st.available) {
      const d = await tg.geoTg.get();
      if (d) pos = { lat: d.latitude, lng: d.longitude, accuracy: d.horizontal_accuracy || 0, source: 'tg' };
      else denied = true;
    } else denied = true;
  }
  if (!pos) pos = await browserPos();
  if (!pos) pos = fallback();
  pos.denied = denied && pos.source === 'default';
  last = pos;
  return pos;
}

export const current = () => last;
export const isFallback = () => !last || last.source === 'default';

/** Підписка на зміну позиції (понад 25 м). */
export function onMove(cb) { listeners.add(cb); return () => listeners.delete(cb); }

function push(p) {
  if (last && haversine(last, p) < 25) { last = { ...last, accuracy: p.accuracy }; return; }
  last = p;
  listeners.forEach((cb) => { try { cb(p); } catch { /* підписник не ламає стеження */ } });
}

/** Стежити за позицією, поки видно карту або поїздку. */
export function watch() {
  if (!last || last.source === 'default') return;
  if (last.source === 'browser' && 'geolocation' in navigator && watchId == null) {
    watchId = navigator.geolocation.watchPosition(
      (p) => push({ lat: p.coords.latitude, lng: p.coords.longitude, accuracy: p.coords.accuracy || 0, source: 'browser' }),
      () => {}, { maximumAge: 10000, timeout: 20000 });
  } else if (last.source === 'tg' && pollTimer == null) {
    pollTimer = setInterval(async () => {
      const d = await tg.geoTg.get();
      if (d) push({ lat: d.latitude, lng: d.longitude, accuracy: d.horizontal_accuracy || 0, source: 'tg' });
    }, 20000);
  }
}

export function unwatch() {
  if (watchId != null) { navigator.geolocation.clearWatch(watchId); watchId = null; }
  if (pollTimer != null) { clearInterval(pollTimer); pollTimer = null; }
}

/** Заборонено в Telegram, але доступ уже запитувався: пропонуємо відкрити налаштування. */
export const canOpenSettings = () => tg.geoTg.supported() && tg.geoTg.state().requested && !tg.geoTg.state().granted;
