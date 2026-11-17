/**
 * Сховище стану в localStorage, ключ `emob`. Усі звернення до сховища в try/catch:
 * якщо localStorage недоступний, стан живе в пам'яті, а Профіль про це повідомляє.
 */
import { CFG } from './config.js';

const KEY = 'emob';
const KEY_BAK = 'emob.bak';
export const VERSION = 1;

/** Ланцюжок міграцій: ключ це версія, з якої мігруємо. */
export const MIGRATIONS = {
  // 1: (s) => ({ ...s, v: 2 }),
};

let state = null;
let memoryOnly = false;
let notice = null; // повідомлення для тосту після завантаження
const subs = new Set();

function read(key) { try { return localStorage.getItem(key); } catch { memoryOnly = true; return null; } }
function write(key, val) { try { localStorage.setItem(key, val); return true; } catch { memoryOnly = true; return false; } }

const uid = (p) => `${p}-${Date.now().toString(36)}${Math.random().toString(36).slice(2, 6)}`;
export const newId = uid;

/** Початковий стан першого запуску: бонус 200 грн, історія порожня. */
export function seedState(user) {
  const now = new Date().toISOString();
  const s = {
    v: VERSION,
    user: { tgId: user.id, firstName: user.firstName, username: user.username || null, isGuest: !!user.isGuest, consentAt: null, identity: null },
    wallet: { balance: CFG.BONUS, tx: [{ id: uid('tx'), type: 'BONUS', amount: CFG.BONUS, at: now, rideId: null, balanceAfter: CFG.BONUS }] },
    fleet: null,
    activeRide: null,
    rides: [],
    events: [],
    settings: { timeFactor: 1, failNext: null },
  };
  s.events.push({ id: uid('ev'), at: now, type: 'BONUS', details: { amount: CFG.BONUS } });
  return s;
}

function valid(s) {
  return s && typeof s === 'object' && s.v && s.user && s.wallet && Array.isArray(s.wallet.tx)
    && Array.isArray(s.rides) && Array.isArray(s.events) && s.settings && Number.isInteger(s.wallet.balance);
}

function migrate(s) {
  let guard = 0;
  while (s && s.v < VERSION && guard++ < 20) {
    const fn = MIGRATIONS[s.v];
    if (!fn) return null;
    s = fn(s);
  }
  return s && s.v === VERSION ? s : null;
}

/** Завантаження: пошкоджений чи чужий стан іде в резервний ключ і замінюється початковим. */
export function init(user) {
  const raw = read(KEY);
  let loaded = null;
  if (raw) {
    try { loaded = migrate(JSON.parse(raw)); } catch { loaded = null; }
    if (!valid(loaded) || loaded.user.tgId !== user.id) {
      write(KEY_BAK, raw);
      loaded = null;
      notice = 'Дані застосунку оновлено';
    }
  }
  state = loaded || seedState(user);
  if (!loaded) save();
  else {
    // ім'я могло змінитися в Telegram
    state.user.firstName = user.firstName;
    state.user.username = user.username || null;
  }
  return state;
}

export const get = () => state;
export const isMemoryOnly = () => memoryOnly;
export const takeNotice = () => { const n = notice; notice = null; return n; };

function save() {
  if (!state) return;
  const ok = write(KEY, JSON.stringify(state));
  if (!ok) memoryOnly = true;
}

/** Змінити стан функцією й зберегти. */
export function update(fn) {
  fn(state);
  save();
  subs.forEach((cb) => { try { cb(state); } catch { /* підписник не повинен ламати збереження */ } });
  return state;
}

export const subscribe = (cb) => { subs.add(cb); return () => subs.delete(cb); };

/** Додати подію журналу (лише додавання, не більше 300 записів). */
export function logEvent(type, details = {}) {
  update((s) => {
    s.events.push({ id: uid('ev'), at: new Date().toISOString(), type, details });
    if (s.events.length > CFG.EVENTS_CAP) s.events.splice(0, s.events.length - CFG.EVENTS_CAP);
  });
}

/** Скинути все: новий початковий стан, журнал починається з RESET. */
export function reset(user) {
  state = seedState(user);
  state.events.push({ id: uid('ev'), at: new Date().toISOString(), type: 'RESET', details: {} });
  save();
  subs.forEach((cb) => { try { cb(state); } catch { /* ігноруємо */ } });
  return state;
}
